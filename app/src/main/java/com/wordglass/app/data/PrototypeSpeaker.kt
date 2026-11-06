package com.wordglass.app.data

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.wordglass.app.model.VoiceGender
import java.util.Locale

/**
 * Temporary prototype speaker using the device TTS engine.
 *
 * Production will use the isolated SpeechService backed by Microsoft/Aliyun. Keeping this class
 * behind a tiny speak() API means screens never contain provider-specific code and replacing it
 * later will not touch the UI.
 */
class PrototypeSpeaker(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var pendingGender: VoiceGender = VoiceGender.FEMALE
    private var pendingRate: Float = 0.9f

    var isReady by mutableStateOf(false)
        private set

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        val engine = tts ?: return
        engine.language = Locale.US
        applyVoice(engine)
        engine.setSpeechRate(pendingRate)
        isReady = true
    }

    fun setGender(gender: VoiceGender) {
        pendingGender = gender
        tts?.let(::applyVoice)
    }

    fun setRate(rate: Float) {
        pendingRate = rate
        tts?.setSpeechRate(rate)
    }

    fun speak(text: String) {
        val engine = tts ?: return
        if (!isReady) return
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "wordglass-${text.hashCode()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        isReady = false
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    private fun applyVoice(engine: TextToSpeech) {
        val usVoices = engine.voices.orEmpty()
            .filter { it.locale.language == Locale.ENGLISH.language && it.locale.country == "US" }
        if (usVoices.isEmpty()) {
            engine.language = Locale.US
            return
        }
        val preferred = when (pendingGender) {
            VoiceGender.FEMALE -> usVoices.firstOrNull { it.looksFemale() } ?: usVoices.first()
            VoiceGender.MALE -> usVoices.firstOrNull { it.looksMale() } ?: usVoices.last()
        }
        runCatching { engine.voice = preferred }
    }

    private fun Voice.looksFemale(): Boolean {
        val n = name.lowercase(Locale.US)
        return n.contains("female") || n.contains("aria") || n.contains("jenny") ||
            n.contains("samantha") || n.contains("victoria") || n.contains("salli")
    }

    private fun Voice.looksMale(): Boolean {
        val n = name.lowercase(Locale.US)
        return n.contains("male") || n.contains("guy") || n.contains("david") ||
            n.contains("alex") || n.contains("daniel") || n.contains("fred")
    }
}

@Composable
fun rememberPrototypeSpeaker(gender: VoiceGender, rate: Float): PrototypeSpeaker {
    val context = LocalContext.current
    val speaker = remember { PrototypeSpeaker(context.applicationContext) }
    LaunchedEffect(gender) { speaker.setGender(gender) }
    LaunchedEffect(rate) { speaker.setRate(rate) }
    DisposableEffect(Unit) {
        onDispose { speaker.shutdown() }
    }
    return speaker
}
