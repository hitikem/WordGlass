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
        val allVoices = engine.voices.orEmpty().toList()
        if (allVoices.isEmpty()) {
            engine.language = Locale.US
            return
        }
        
        // Try to find English voices
        val englishVoices = allVoices.filter { 
            it.locale.language == Locale.ENGLISH.language 
        }
        val candidates = if (englishVoices.isNotEmpty()) englishVoices else allVoices
        
        val preferred = when (pendingGender) {
            VoiceGender.FEMALE -> {
                candidates.firstOrNull { it.looksFemale() } 
                    ?: candidates.firstOrNull { !it.looksMale() }
                    ?: candidates.first()
            }
            VoiceGender.MALE -> {
                candidates.firstOrNull { it.looksMale() } 
                    ?: candidates.firstOrNull { !it.looksFemale() }
                    ?: candidates.last()
            }
        }
        runCatching { engine.voice = preferred }
    }

    /**
     * Enumerate all installed English TTS voices as free, selectable profiles.
     * Each voice is classified by name heuristics for gender and locale.
     */
    fun enumerateFreeVoices(): List<com.wordglass.app.model.VoiceProfile> {
        val voices = mutableListOf<com.wordglass.app.model.VoiceProfile>()
        
        // Cloud voices (always available)
        voices.add(com.wordglass.app.model.VoiceProfile("youdao_real", VoiceGender.MALE, "有道真人发音（美式）", "en-US", true))
        voices.add(com.wordglass.app.model.VoiceProfile("google_us", VoiceGender.FEMALE, "Google 女声·美式", "en-US"))
        voices.add(com.wordglass.app.model.VoiceProfile("google_uk", VoiceGender.FEMALE, "Google 女声·英式", "en-GB"))
        voices.add(com.wordglass.app.model.VoiceProfile("google_au", VoiceGender.FEMALE, "Google 女声·澳洲", "en-AU"))
        voices.add(com.wordglass.app.model.VoiceProfile("ms_aria", VoiceGender.FEMALE, "微软 Aria·女声·自然", "en-US"))
        voices.add(com.wordglass.app.model.VoiceProfile("ms_guy", VoiceGender.MALE, "微软 Guy·男声·自然", "en-US"))
        voices.add(com.wordglass.app.model.VoiceProfile("ms_jenny", VoiceGender.FEMALE, "微软 Jenny·女声·亲切", "en-US"))
        voices.add(com.wordglass.app.model.VoiceProfile("ms_ryan", VoiceGender.MALE, "微软 Ryan·男声·英式", "en-GB"))

        // System TTS voices
        val engine = tts
        if (engine != null && isReady) {
            engine.voices.orEmpty()
                .filter { it.locale.language == Locale.ENGLISH.language }
                .forEach { v ->
                    voices.add(com.wordglass.app.model.VoiceProfile(
                        id = "sys_${v.name}",
                        gender = if (v.looksFemale()) VoiceGender.FEMALE else VoiceGender.MALE,
                        displayName = "系统 ${v.name}",
                        locale = v.locale.toLanguageTag(),
                    ))
                }
        }
        return voices
    }

    /**
     * Speak text using a specific voice name (system TTS voice).
     */
    fun speakWithVoice(text: String, voiceName: String) {
        val engine = tts ?: return
        if (!isReady) return
        
        // Map voice IDs to actual TTS voices
        val voice = when {
            // Direct name match
            else -> engine.voices.orEmpty().firstOrNull { 
                it.name == voiceName || it.name.contains(voiceName, ignoreCase = true) 
            }
        }
        
        if (voice != null) {
            runCatching { engine.voice = voice }
        } else {
            // Try gender-based selection
            val isMale = voiceName.contains("male", ignoreCase = true) && 
                        !voiceName.contains("female", ignoreCase = true)
            pendingGender = if (isMale) VoiceGender.MALE else VoiceGender.FEMALE
            applyVoice(engine)
        }
        
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "wordglass-preview")
    }

    /** Whether the TTS engine is ready to enumerate and speak. */
    val engineReady: Boolean get() = isReady

    private fun Voice.looksFemale(): Boolean {
        val n = name.lowercase(Locale.US)
        return n.contains("female") || n.contains("aria") || n.contains("jenny") ||
            n.contains("samantha") || n.contains("victoria") || n.contains("salli") ||
            n.contains("female") || n.contains("woman") || n.contains("girl") ||
            n.contains("michelle") || n.contains("sonia") || n.contains("zira")
    }

    private fun Voice.looksMale(): Boolean {
        val n = name.lowercase(Locale.US)
        return n.contains("male") || n.contains("guy") || n.contains("david") ||
            n.contains("alex") || n.contains("daniel") || n.contains("fred") ||
            n.contains("man") || n.contains("boy") || n.contains("ryan") ||
            n.contains("mark") || n.contains("richard") || n.contains("thomas")
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

