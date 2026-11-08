package com.wordglass.app.voice

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlin.math.max

/**
 * Pronunciation scoring: uses speech recognition to transcribe user's pronunciation,
 * then compares with the target word using edit distance to calculate accuracy score.
 */
object PronunciationScorer {

    data class ScoreResult(
        val score: Int,          // 0-100
        val transcript: String,  // what the recognizer heard
        val target: String,      // the correct word
        val feedback: String,    // human-readable feedback
    )

    /**
     * Score pronunciation by comparing recognized speech with target word.
     */
    fun score(target: String, transcript: String): ScoreResult {
        val t = target.lowercase().trim()
        val s = transcript.lowercase().trim()

        if (s.isBlank()) {
            return ScoreResult(0, transcript, target, "没有听到你的发音，请重试")
        }

        // Calculate similarity using normalized edit distance
        val similarity = similarity(t, s)
        val score = (similarity * 100).toInt().coerceIn(0, 100)

        val feedback = when {
            score >= 95 -> "🎉 完美！发音非常标准"
            score >= 85 -> "😊 很好！发音基本准确"
            score >= 70 -> "🙂 不错！再注意一下细节"
            score >= 50 -> "😐 还行，需要多练习"
            score >= 30 -> "😟 发音差距较大，建议多听标准发音"
            else -> "❌ 发音不正确，请听标准发音后重试"
        }

        return ScoreResult(score, transcript, target, feedback)
    }

    /**
     * Normalized similarity (0.0 ~ 1.0) using Levenshtein distance.
     */
    fun similarity(a: String, b: String): Double {
        if (a == b) return 1.0
        if (a.isEmpty() || b.isEmpty()) return 0.0

        val dist = editDistance(a, b)
        val maxLen = max(a.length, b.length)
        return 1.0 - dist.toDouble() / maxLen
    }

    /**
     * Levenshtein edit distance.
     */
    private fun editDistance(a: String, b: String): Int {
        val m = a.length
        val n = b.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        return dp[m][n]
    }

    /**
     * Phoneme-level scoring: compare individual sounds.
     */
    fun detailedScore(target: String, transcript: String): ScoreResult {
        val base = score(target, transcript)

        // Word-level breakdown
        val tWords = target.lowercase().split(" ")
        val sWords = transcript.lowercase().split(" ")

        val wordScores = tWords.mapIndexed { i, tw ->
            val sw = sWords.getOrNull(i) ?: ""
            (similarity(tw, sw) * 100).toInt()
        }

        val weakest = wordScores.minOrNull() ?: 0
        val detail = if (tWords.size > 1) {
            val weakestWord = tWords[wordScores.indexOf(weakest)]
            "${base.feedback}\n最需改进: \"$weakestWord\" ($weakest%)"
        } else base.feedback

        return base.copy(feedback = detail)
    }

    /**
     * Start speech recognition for pronunciation scoring.
     */
    fun recognize(
        context: Context,
        language: String = "en-US",
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("此设备不支持语音识别")
            return
        }

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val texts = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!texts.isNullOrEmpty()) {
                    onResult(texts[0])
                } else {
                    onError("没有识别到语音")
                }
                recognizer.destroy()
            }

            override fun onError(error: Int) {
                val msg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "没有匹配的语音，请再说一次"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "没有听到声音"
                    SpeechRecognizer.ERROR_AUDIO -> "录音错误"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别器繁忙"
                    else -> "识别错误: $error"
                }
                onError(msg)
                recognizer.destroy()
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        recognizer.startListening(intent)
    }
}
