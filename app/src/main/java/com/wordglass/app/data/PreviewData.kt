package com.wordglass.app.data

import com.wordglass.app.model.DashboardStats
import com.wordglass.app.model.Word

/**
 * PROTOTYPE DATA ONLY.
 *
 * Word spellings, phonetics, core meanings and usage notes below are taken from the supplied
 * 887-word source PDF; the example sentences are short UI copy for this prototype and are not
 * part of the final seeded database. No production code reads this object after the Room stage.
 */
object PreviewData {

    val words: List<Word> = listOf(
        Word(
            id = 1,
            word = "change",
            phonetic = "/tʃeɪndʒ/",
            meaning = "v. 改变  n. 变化",
            example = "Small changes can make a big difference.",
            translation = "微小的改变也能带来巨大的不同。",
            mastery = 3,
            sourceNumber = 1,
        ),
        Word(
            id = 2,
            word = "interest",
            phonetic = "/ˈɪntrəst/",
            meaning = "vt. 使感兴趣  n. 兴趣",
            example = "He has a strong interest in artificial intelligence.",
            translation = "他对人工智能有着浓厚的兴趣。",
            usage = "熟词僻义：n. 利益",
            mastery = 2,
            sourceNumber = 2,
        ),
        Word(
            id = 3,
            word = "last",
            phonetic = "/lɑːst/",
            meaning = "adj. & adv. 最后（的）；上一次（的）",
            example = "The meeting lasted longer than expected.",
            translation = "会议持续的时间比预期更长。",
            usage = "熟词僻义：v. 持续",
            mastery = 4,
            sourceNumber = 3,
        ),
        Word(
            id = 4,
            word = "mean",
            phonetic = "/miːn/",
            meaning = "v. 意味着；打算；对……当真",
            example = "I didn't mean to interrupt you.",
            translation = "我不是有意打断你的。",
            usage = "熟词僻义：adj. 吝啬的；刻薄的",
            mastery = 2,
            wrongCount = 2,
            sourceNumber = 4,
        ),
        Word(
            id = 5,
            word = "hard",
            phonetic = "/hɑːrd/",
            meaning = "adj. 严苛的，努力的，困难的  adv. 努力地，用力地",
            example = "She works hard to master English.",
            translation = "她努力学习英语。",
            mastery = 3,
            sourceNumber = 5,
        ),
        Word(
            id = 6,
            word = "offer",
            phonetic = "/ˈɔːfər/",
            meaning = "n. & v. 提议；提供",
            example = "They offered him a place on the course.",
            translation = "他们给了他一个参加课程的名额。",
            mastery = 1,
            due = true,
            sourceNumber = 6,
        ),
        Word(
            id = 7,
            word = "face",
            phonetic = "/feɪs/",
            meaning = "n. 脸；面孔；表面  v. 面对；面向",
            example = "We have to face the truth.",
            translation = "我们必须面对真相。",
            mastery = 3,
            sourceNumber = 7,
        ),
        Word(
            id = 8,
            word = "train",
            phonetic = "/treɪn/",
            meaning = "v. 培训；训练  n. 火车",
            example = "The program trains students to think critically.",
            translation = "该项目训练学生批判性思考。",
            mastery = 2,
            sourceNumber = 8,
        ),
        Word(
            id = 9,
            word = "place",
            phonetic = "/pleɪs/",
            meaning = "n. 地方；职位  v. 放置",
            example = "The event will take place next Friday.",
            translation = "活动将在下周五举行。",
            usage = "短语：take place 发生",
            mastery = 4,
            due = true,
            sourceNumber = 9,
        ),
        Word(
            id = 10,
            word = "class",
            phonetic = "/klæs/",
            meaning = "n. 班级；课；等级",
            example = "She is the best student in her class.",
            translation = "她是班上最优秀的学生。",
            usage = "熟词僻义：n. 种类",
            mastery = 5,
            favorite = true,
            sourceNumber = 10,
        ),
        Word(
            id = 11,
            word = "experience",
            phonetic = "/ɪkˈspɪriəns/",
            meaning = "n. 经验；经历  v. 体验；经历",
            example = "Experience is the teacher of all things.",
            translation = "经验是一切事物之师。",
            usage = "不可数名词指“经验”，可数名词指“经历”。",
            mastery = 3,
            due = true,
            sourceNumber = 12,
        ),
        Word(
            id = 12,
            word = "study",
            phonetic = "/ˈstʌdi/",
            meaning = "v. 研究；学习  n. 研究；书房；学问",
            example = "He studies English for an hour every morning.",
            translation = "他每天早上学一小时英语。",
            mastery = 2,
            wrongCount = 1,
            sourceNumber = 13,
        ),
    )

    val studyWordIds: List<Int> = listOf(11, 2, 4, 5, 3, 6, 7, 8, 9, 10)

    val dashboard: DashboardStats = DashboardStats()

    fun word(id: Int): Word = words.first { it.id == id }
}
