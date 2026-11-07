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
import com.wordglass.app.data.speech.SpeechService
import com.wordglass.app.model.VoiceGender
import java.util.Locale

/**
 * Device TTS implementation of [SpeechService].
 *
 * It is the offline fallback and the current internal-beta provider. A cloud provider can be
 * swapped in without changing any screen because this class is the only engine-specific code.
 */
class SystemSpeechService(context: Context) :
    TextToSpeech.OnInitListener,
    SpeechService {

    private var tts: TextToSpeech? = null
    private var pendingGender: VoiceGender = VoiceGender.FEMALE
    private var pendingRate: Float = 0.9f

    override var isReady by mutableStateOf(false)
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

    override fun speak(text: String) {
        val engine = tts ?: return
        if (!isReady) return
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "wordglass-${text.hashCode()}")
    }

    override fun stop() {
        tts?.stop()
    }

    override fun shutdown() {
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

    /**
     * Enumerate all installed English TTS voices as free, selectable profiles.
     * Each voice is classified by name heuristics for gender and locale.
     */
    fun enumerateFreeVoices(): List<com.wordglass.app.model.VoiceProfile> {
        val engine = tts ?: return emptyList()
        val voices = engine.voices.orEmpty().toList()
        return voices
            .filter { it.locale.language == Locale.ENGLISH.language }
            .map { v ->
                val gender = if (v.looksFemale()) VoiceGender.FEMALE else VoiceGender.MALE
                val isReal = false
                com.wordglass.app.model.VoiceProfile(
                    id = "sys_${v.name}",
                    gender = gender,
                    displayName = "${v.name} (${v.locale.toLanguageTag()})",
                    locale = v.locale.toLanguageTag(),
                    isRealHuman = isReal,
                )
            }
            .sortedWith(compareBy({ it.locale }, { it.displayName }))
    }

    /**
     * Speak text using a specific voice name (system TTS voice).
     */
    fun speakWithVoice(text: String, voiceName: String) {
        val engine = tts ?: return
        if (!isReady) return
        val voice = engine.voices.orEmpty().firstOrNull { it.name == voiceName }
        if (voice != null) {
            runCatching { engine.voice = voice }
        }
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "wordglass-preview")
    }

    /** Whether the TTS engine is ready to enumerate and speak. */
    val engineReady: Boolean get() = isReady

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
fun rememberSystemSpeechService(gender: VoiceGender, rate: Float): SystemSpeechService {
    val context = LocalContext.current
    val speaker = remember { SystemSpeechService(context.applicationContext) }
    LaunchedEffect(gender) { speaker.setGender(gender) }
    LaunchedEffect(rate) { speaker.setRate(rate) }
    DisposableEffect(Unit) {
        onDispose { speaker.shutdown() }
    }
    return speaker
}

