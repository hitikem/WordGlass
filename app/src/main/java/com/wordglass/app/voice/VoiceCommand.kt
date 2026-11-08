package com.wordglass.app.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Voice command integration (语音控制).
 * Supports "开始背单词", "听发音", "下一个" etc.
 * Works with any system voice assistant including 小爱同学 (Xiao AI).
 *
 * Usage: user says trigger phrase to Xiao AI → opens WordGlass →
 * app listens for command → performs action.
 */
object VoiceCommand {

    sealed class Command {
        data object StartStudy : Command()
        data object NextWord : Command()
        data object SpeakWord : Command()
        data object ShowAnswer : Command()
        data object StartReview : Command()
        data object Unknown : Command()
    }

    /**
     * Parse voice command text.
     */
    fun parse(text: String): Command {
        val t = text.lowercase().trim()
        return when {
            t.contains("背单词") || t.contains("开始学习") || t.contains("学习") -> Command.StartStudy
            t.contains("下一个") || t.contains("继续") -> Command.NextWord
            t.contains("发音") || t.contains("读") || t.contains("听") -> Command.SpeakWord
            t.contains("答案") || t.contains("显示") || t.contains("释义") -> Command.ShowAnswer
            t.contains("复习") -> Command.StartReview
            else -> Command.Unknown
        }
    }

    /**
     * Create speech recognition intent for voice input.
     */
    fun createRecognitionIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出指令")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
    }

    /**
     * Start listening for voice commands.
     */
    fun startListening(
        context: Context,
        onResult: (Command) -> Unit,
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
                    onResult(parse(texts[0]))
                }
                recognizer.destroy()
            }

            override fun onError(error: Int) {
                onError("语音识别错误: $error")
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

        recognizer.startListening(createRecognitionIntent())
    }

    /**
     * Get intent to launch WordGlass via voice assistant.
     * Works with 小爱同学 and other Android voice assistants.
     */
    fun getLaunchIntent(context: Context, action: String = "start_study"): Intent {
        return context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            putExtra("voice_action", action)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } ?: Intent()
    }
}
