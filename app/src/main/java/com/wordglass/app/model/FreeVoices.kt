package com.wordglass.app.model

/**
 * Free voice sources from all platforms.
 * All voices are free to use.
 */
object FreeVoices {

    data class VoiceOption(
        val id: String,
        val displayName: String,
        val platform: String,
        val gender: String,
        val locale: String,
        val isRealHuman: Boolean = false,
        val isCloud: Boolean = false,
        val url: String? = null,
    ) {
        val label: String get() = "[$platform] $displayName"
    }

    /** All free voices from all platforms */
    val ALL_FREE_VOICES: List<VoiceOption> = listOf(
        // ========== 有道真人发音 ==========
        VoiceOption(
            id = "youdao_real_us",
            displayName = "真人发音·美式",
            platform = "有道",
            gender = "男",
            locale = "en-US",
            isRealHuman = true,
        ),
        VoiceOption(
            id = "youdao_real_uk",
            displayName = "真人发音·英式",
            platform = "有道",
            gender = "男",
            locale = "en-GB",
            isRealHuman = true,
        ),

        // ========== Google Translate TTS（免费） ==========
        VoiceOption(
            id = "google_us_female",
            displayName = "Google 女声·美式",
            platform = "Google",
            gender = "女",
            locale = "en-US",
            isCloud = true,
            url = "https://translate.google.com/translate_tts?ie=UTF-8&q={text}&tl=en-US&client=tw-ob",
        ),
        VoiceOption(
            id = "google_uk_female",
            displayName = "Google 女声·英式",
            platform = "Google",
            gender = "女",
            locale = "en-GB",
            isCloud = true,
            url = "https://translate.google.com/translate_tts?ie=UTF-8&q={text}&tl=en-GB&client=tw-ob",
        ),
        VoiceOption(
            id = "google_au_female",
            displayName = "Google 女声·澳洲",
            platform = "Google",
            gender = "女",
            locale = "en-AU",
            isCloud = true,
            url = "https://translate.google.com/translate_tts?ie=UTF-8&q={text}&tl=en-AU&client=tw-ob",
        ),
        VoiceOption(
            id = "google_in_female",
            displayName = "Google 女声·印度",
            platform = "Google",
            gender = "女",
            locale = "en-IN",
            isCloud = true,
            url = "https://translate.google.com/translate_tts?ie=UTF-8&q={text}&tl=en-IN&client=tw-ob",
        ),

        // ========== Microsoft Edge TTS（免费） ==========
        VoiceOption(
            id = "ms_aria_female",
            displayName = "Aria·女声·自然",
            platform = "微软",
            gender = "女",
            locale = "en-US",
            isCloud = true,
            url = "https://speech.platform.bing.com/consumer/speech/synthesize/readaloud/voices/list",
        ),
        VoiceOption(
            id = "ms_guy_male",
            displayName = "Guy·男声·自然",
            platform = "微软",
            gender = "男",
            locale = "en-US",
            isCloud = true,
        ),
        VoiceOption(
            id = "ms_jenny_female",
            displayName = "Jenny·女声·亲切",
            platform = "微软",
            gender = "女",
            locale = "en-US",
            isCloud = true,
        ),
        VoiceOption(
            id = "ms_michelle_female",
            displayName = "Michelle·女声·温柔",
            platform = "微软",
            gender = "女",
            locale = "en-US",
            isCloud = true,
        ),
        VoiceOption(
            id = "ms_ryan_male",
            displayName = "Ryan·男声·沉稳",
            platform = "微软",
            gender = "男",
            locale = "en-GB",
            isCloud = true,
        ),
        VoiceOption(
            id = "ms_sonia_female",
            displayName = "Sonia·女声·英式",
            platform = "微软",
            gender = "女",
            locale = "en-GB",
            isCloud = true,
        ),

        // ========== 系统 TTS（设备内置，离线可用） ==========
        VoiceOption(
            id = "system_female_us",
            displayName = "系统女声·美式",
            platform = "系统",
            gender = "女",
            locale = "en-US",
        ),
        VoiceOption(
            id = "system_male_us",
            displayName = "系统男声·美式",
            platform = "系统",
            gender = "男",
            locale = "en-US",
        ),
        VoiceOption(
            id = "system_female_gb",
            displayName = "系统女声·英式",
            platform = "系统",
            gender = "女",
            locale = "en-GB",
        ),
        VoiceOption(
            id = "system_male_gb",
            displayName = "系统男声·英式",
            platform = "系统",
            gender = "男",
            locale = "en-GB",
        ),
        VoiceOption(
            id = "system_female_au",
            displayName = "系统女声·澳洲",
            platform = "系统",
            gender = "女",
            locale = "en-AU",
        ),
        VoiceOption(
            id = "system_male_au",
            displayName = "系统男声·澳洲",
            platform = "系统",
            gender = "男",
            locale = "en-AU",
        ),
        VoiceOption(
            id = "system_female_in",
            displayName = "系统女声·印度",
            platform = "系统",
            gender = "女",
            locale = "en-IN",
        ),
        VoiceOption(
            id = "system_male_in",
            displayName = "系统男声·印度",
            platform = "系统",
            gender = "男",
            locale = "en-IN",
        ),
    )

    /** Group voices by platform */
    fun byPlatform(): Map<String, List<VoiceOption>> = ALL_FREE_VOICES.groupBy { it.platform }

    fun byId(id: String): VoiceOption = ALL_FREE_VOICES.firstOrNull { it.id == id } ?: ALL_FREE_VOICES[0]
}
