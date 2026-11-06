package com.wordglass.app.model

/**
 * Prototype word model.
 *
 * This mirrors the future Room domain shape, but this file is intentionally backed by
 * [com.wordglass.app.data.PreviewData] until the database stage. Fields that the source PDF
 * does not provide (examples, roots, affixes) are nullable and never synthesized by production
 * code. The prototype sample contains clearly marked UI copy only.
 */
data class Word(
    val id: Int,
    val word: String,
    val phonetic: String,
    val meaning: String,
    val example: String? = null,
    val translation: String? = null,
    val root: String? = null,
    val affix: String? = null,
    val usage: String? = null,
    val mastery: Int = 0,
    val favorite: Boolean = false,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val reviewCount: Int = 0,
    val lastReviewedAt: Long? = null,
    val nextReviewAt: Long? = null,
    val due: Boolean = false,
    val sourceNumber: Int? = null,
)

enum class WordMastery(val label: String) {
    NEW("待学习"),
    INTRO("初识"),
    VAGUE("模糊"),
    FAMILIAR("熟悉"),
    MASTERED("已掌握"),
    EXPERT("精通"),
    ;

    companion object {
        fun from(value: Int): WordMastery = entries[value.coerceIn(0, entries.lastIndex)]
    }
}

enum class StudyMode(val label: String, val eyebrow: String) {
    CARD("卡片", "FLIP"),
    SWIPE("滑动", "SWIPE"),
    CHOICE("选择", "CHOICE"),
    DICTATION("听写", "TYPE"),
}

enum class StudyRating(val label: String, val shortLabel: String) {
    AGAIN("完全不认识", "忘记"),
    HARD("模糊", "模糊"),
    GOOD("认识", "认识"),
    EASY("非常熟练", "熟练"),
}

enum class LibraryFilter(val label: String) {
    ALL("全部"),
    DUE("待复习"),
    LEARNING("学习中"),
    MASTERED("已掌握"),
    WRONG("错词本"),
    FAVORITE("收藏"),
}

enum class LibrarySort(val label: String) {
    ALPHABETICAL("A–Z"),
    MASTERY("掌握度"),
    RECENT("最近复习"),
}

data class DashboardStats(
    val dailyGoal: Int = 25,
    val completedToday: Int = 17,
    val dueCount: Int = 24,
    val totalLearned: Int = 563,
    val mastered: Int = 318,
    val streakDays: Int = 12,
    val minutesToday: Int = 38,
    val weeklyMinutes: List<Int> = listOf(18, 26, 14, 34, 28, 9, 17),
)

enum class ThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色"),
    DARK("深色"),
}

enum class VoiceGender(val label: String) {
    FEMALE("女声"),
    MALE("男声"),
}
