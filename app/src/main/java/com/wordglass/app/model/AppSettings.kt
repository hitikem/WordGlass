package com.wordglass.app.model

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val voice: VoiceGender = VoiceGender.FEMALE,
    val speechRate: Float = 0.9f,
    val autoPlay: Boolean = false,
    val dailyGoal: Int = 25,
)
