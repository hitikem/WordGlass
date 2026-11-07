package com.wordglass.app.model

/**
 * Voice settings: gender + specific TTS voice.
 *
 * The system TTS exposes a list of installed voices ([android.speech.tts.Voice]).
 * We classify them by gender using name heuristics and expose each distinct voice so the
 * user can pick a specific one. The real-human Youdao pronunciation is preferred and
 * the system TTS voices are fallback.
 */
data class VoiceProfile(
    val id: String,
    val gender: VoiceGender,
    val displayName: String,
    val locale: String,
    val isRealHuman: Boolean = false,
) {
    val label: String get() = "$genderLabel · $displayName"
    val genderLabel: String get() = when (gender) {
        VoiceGender.FEMALE -> "女声"
        VoiceGender.MALE -> "男声"
    }
}

/** Preset voice profiles. */
object VoicePresets {
    /** Real human American English from Youdao dictionary (preferred). */
    val YOUDAO_REAL = VoiceProfile(
        id = "youdao_real",
        gender = VoiceGender.MALE,
        displayName = "真人发音（美式）",
        locale = "en-US",
        isRealHuman = true,
    )

    /** Common system TTS voices. */
    val SYSTEM_FEMALE = VoiceProfile(
        id = "system_female",
        gender = VoiceGender.FEMALE,
        displayName = "系统女声",
        locale = "en-US",
    )

    val SYSTEM_MALE = VoiceProfile(
        id = "system_male",
        gender = VoiceGender.MALE,
        displayName = "系统男声",
        locale = "en-US",
    )

    val SYSTEM_FEMALE_BRITISH = VoiceProfile(
        id = "system_female_gb",
        gender = VoiceGender.FEMALE,
        displayName = "英式女声",
        locale = "en-GB",
    )

    val SYSTEM_MALE_BRITISH = VoiceProfile(
        id = "system_male_gb",
        gender = VoiceGender.MALE,
        displayName = "英式男声",
        locale = "en-GB",
    )

    val SYSTEM_FEMALE_AUSTRALIAN = VoiceProfile(
        id = "system_female_au",
        gender = VoiceGender.FEMALE,
        displayName = "澳式女声",
        locale = "en-AU",
    )

    val SYSTEM_MALE_AUSTRALIAN = VoiceProfile(
        id = "system_male_au",
        gender = VoiceGender.MALE,
        displayName = "澳式男声",
        locale = "en-AU",
    )

    val SYSTEM_FEMALE_INDIAN = VoiceProfile(
        id = "system_female_in",
        gender = VoiceGender.FEMALE,
        displayName = "印式女声",
        locale = "en-IN",
    )

    val SYSTEM_MALE_INDIAN = VoiceProfile(
        id = "system_male_in",
        gender = VoiceGender.MALE,
        displayName = "印式男声",
        locale = "en-IN",
    )

    /** All presets in the order shown to the user. */
    val ALL: List<VoiceProfile> = listOf(
        YOUDAO_REAL,
        SYSTEM_FEMALE,
        SYSTEM_MALE,
        SYSTEM_FEMALE_BRITISH,
        SYSTEM_MALE_BRITISH,
        SYSTEM_FEMALE_AUSTRALIAN,
        SYSTEM_MALE_AUSTRALIAN,
        SYSTEM_FEMALE_INDIAN,
        SYSTEM_MALE_INDIAN,
    )

    fun byId(id: String): VoiceProfile = ALL.firstOrNull { it.id == id } ?: SYSTEM_FEMALE
}
