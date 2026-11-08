package com.wordglass.app.data.speech

import android.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Free cloud TTS that ACTUALLY WORKS.
 * Uses multiple free endpoints with fallback.
 */
object CloudTtsService {

    /** Play audio from a local file */
    private suspend fun playFile(file: File): Boolean = withContext(Dispatchers.Main) {
        try {
            val player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    it.release()
                    file.delete()
                }
                setOnErrorListener { _, _, _ -> true }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    /** Download and play from URL */
    private suspend fun downloadAndPlay(url: String, cacheDir: File, headers: Map<String, String> = emptyMap()): Boolean {
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }

            if (conn.responseCode != 200) return false

            val audioFile = File(cacheDir, "cloud_tts_${System.currentTimeMillis()}.mp3")
            audioFile.outputStream().use { output ->
                conn.inputStream.copyTo(output)
            }
            conn.disconnect()

            if (audioFile.length() < 100) {
                audioFile.delete()
                return false
            }
            playFile(audioFile)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Speak text using Google Translate TTS (free).
     * Multiple endpoint variants for reliability.
     */
    suspend fun speakGoogle(text: String, locale: String = "en-US", cacheDir: File): Boolean {
        val encoded = java.net.URLEncoder.encode(text, "UTF-8")

        // Try multiple Google TTS endpoints
        val endpoints = listOf(
            "https://translate.google.com/translate_tts?ie=UTF-8&tl=$locale&client=tw-ob&q=$encoded",
            "https://translate.google.co.uk/translate_tts?ie=UTF-8&tl=$locale&client=tw-ob&q=$encoded",
            "https://translate.google.com.au/translate_tts?ie=UTF-8&tl=$locale&client=tw-ob&q=$encoded",
        )

        for (url in endpoints) {
            if (downloadAndPlay(url, cacheDir)) return true
        }

        // Fallback: try Youdao
        return speakYoudao(text, cacheDir)
    }

    /**
     * Speak using Youdao dictionary audio (free, real human).
     */
    suspend fun speakYoudao(text: String, cacheDir: File): Boolean {
        val url = "https://dict.youdao.com/dictvoice?audio=${java.net.URLEncoder.encode(text, "UTF-8")}&type=2"
        return downloadAndPlay(url, cacheDir)
    }

    /**
     * Speak using the selected cloud voice.
     * Routes to the correct service based on voice ID.
     */
    suspend fun speakVoice(text: String, voiceId: String, locale: String, cacheDir: File): Boolean {
        return when {
            voiceId.startsWith("youdao") -> speakYoudao(text, cacheDir)
            voiceId.startsWith("google") -> speakGoogle(text, locale, cacheDir)
            voiceId.startsWith("ms_") -> {
                // Microsoft voices use Google TTS with different locale mapping
                val msLocale = when {
                    locale.contains("GB") -> "en-GB"
                    locale.contains("AU") -> "en-AU"
                    else -> "en-US"
                }
                speakGoogle(text, msLocale, cacheDir)
            }
            else -> speakGoogle(text, locale, cacheDir)
        }
    }
}
