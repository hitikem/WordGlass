package com.wordglass.app

import com.wordglass.app.data.PreviewData
import com.wordglass.app.model.WordMastery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewDataTest {

    @Test
    fun studyOrderPointsAtRealWords() {
        val ids = PreviewData.words.map { it.id }.toSet()
        assertEquals(PreviewData.studyWordIds.size, PreviewData.studyWordIds.distinct().size)
        assertTrue(PreviewData.studyWordIds.all { it in ids })
    }

    @Test
    fun masteryHasSixDocumentedLevels() {
        assertEquals(6, WordMastery.entries.size)
        assertEquals("待学习", WordMastery.from(0).label)
        assertEquals("熟悉", WordMastery.from(3).label)
        assertEquals("已掌握", WordMastery.from(4).label)
        assertEquals("精通", WordMastery.from(5).label)
        assertEquals("精通", WordMastery.from(99).label)
    }

    @Test
    fun previewDataHasNoDuplicateIds() {
        assertEquals(PreviewData.words.size, PreviewData.words.map { it.id }.distinct().size)
        assertEquals(PreviewData.words.size, PreviewData.words.map { it.word }.distinct().size)
    }
}
