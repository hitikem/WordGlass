package com.wordglass.app.model

/**
 * Word book (词书) model for multi-book support.
 * Each word book has its own word list and metadata.
 */
data class WordBook(
    val id: String,
    val name: String,
    val description: String,
    val wordCount: Int,
    val source: String,
    val isBuiltIn: Boolean = true,
)

object WordBooks {
    val PEP_SENIOR_1 = WordBook(
        id = "pep_senior_1",
        name = "人教版高中必修一",
        description = "人教版高中英语必修第一册词汇",
        wordCount = 0,
        source = "PEP",
    )

    val PEP_SENIOR_2 = WordBook(
        id = "pep_senior_2",
        name = "人教版高中必修二",
        description = "人教版高中英语必修第二册词汇",
        wordCount = 0,
        source = "PEP",
    )

    val PEP_SENIOR_3 = WordBook(
        id = "pep_senior_3",
        name = "人教版高中必修三",
        description = "人教版高中英语必修第三册词汇",
        wordCount = 0,
        source = "PEP",
    )

    val PEP_SELECTIVE_1 = WordBook(
        id = "pep_selective_1",
        name = "人教版选择性必修一",
        description = "人教版高中英语选择性必修第一册词汇",
        wordCount = 0,
        source = "PEP",
    )

    val PEP_SELECTIVE_2 = WordBook(
        id = "pep_selective_2",
        name = "人教版选择性必修二",
        description = "人教版高中英语选择性必修第二册词汇",
        wordCount = 0,
        source = "PEP",
    )

    val PEP_SELECTIVE_3 = WordBook(
        id = "pep_selective_3",
        name = "人教版选择性必修三",
        description = "人教版高中英语选择性必修第三册词汇",
        wordCount = 0,
        source = "PEP",
    )

    val PEP_SELECTIVE_4 = WordBook(
        id = "pep_selective_4",
        name = "人教版选择性必修四",
        description = "人教版高中英语选择性必修第四册词汇",
        wordCount = 0,
        source = "PEP",
    )

    val CORE_882 = WordBook(
        id = "core_882",
        name = "核心882词",
        description = "高中英语核心词汇882条",
        wordCount = 882,
        source = "CORE",
    )

    val ALL = listOf(
        CORE_882,
        PEP_SENIOR_1,
        PEP_SENIOR_2,
        PEP_SENIOR_3,
        PEP_SELECTIVE_1,
        PEP_SELECTIVE_2,
        PEP_SELECTIVE_3,
        PEP_SELECTIVE_4,
    )

    fun byId(id: String): WordBook = ALL.firstOrNull { it.id == id } ?: CORE_882
}
