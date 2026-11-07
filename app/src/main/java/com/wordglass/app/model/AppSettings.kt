package com.wordglass.app.model

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val voice: VoiceGender = VoiceGender.FEMALE,
    val voiceProfileId: String = "youdao_real",
    val speechRate: Float = 0.9f,
    val autoPlay: Boolean = false,
    val dailyGoal: Int = 25,
    val profileName: String = "Alex Chen",
    val profileBio: String = "WordGlass Learner",
    val profileAvatarUri: String? = null,
    val profileAvatarSeed: String = "0",
    val planStartDate: Long = 0L,
    val planTotalDays: Int = 0,
    val alibabaTtsApiKey: String = "",
)

