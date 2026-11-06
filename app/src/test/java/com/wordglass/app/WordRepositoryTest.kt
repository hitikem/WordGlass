package com.wordglass.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.wordglass.app.data.local.WordDatabase
import com.wordglass.app.data.repository.WordRepository
import com.wordglass.app.model.StudyMode
import com.wordglass.app.model.StudyRating
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WordRepositoryTest {

    private lateinit var database: WordDatabase
    private lateinit var repository: WordRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WordDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WordRepository(database.wordDao(), context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun seedLoadsAll882SourceRows() = runTest {
        repository.seedIfNeeded()
        val words = repository.words.first()
        assertEquals(882, words.size)
        assertEquals(882, database.wordDao().wordCount())
        assertEquals("change", words.first().word)
        assertTrue(words.any { it.id == 667 })
        assertTrue(words.any { it.id == 673 })
        assertTrue(words.none { it.id in 668..672 })
    }

    @Test
    fun duplicatePresentKeepsBothPronunciations() = runTest {
        repository.seedIfNeeded()
        val words = repository.words.first()
        val verb = words.first { it.id == 96 }
        val noun = words.first { it.id == 97 }
        assertEquals("present", verb.word)
        assertEquals("present", noun.word)
        assertEquals("prɪˈzent", verb.phonetic)
        assertEquals("ˈpreznt", noun.phonetic)
    }

    @Test
    fun favoriteAndRatingArePersisted() = runTest {
        repository.seedIfNeeded()
        repository.toggleFavorite(1, true)
        val favorite = repository.words.first().first { it.id == 1 }
        assertTrue(favorite.favorite)

        val before = System.currentTimeMillis()
        repository.submitRating(1, StudyRating.GOOD, StudyMode.CARD)
        val state = database.wordDao().state(1)
        assertNotNull(state)
        assertEquals(1, state!!.reviewCount)
        assertEquals(1, state.correctCount)
        assertTrue(state.nextReviewAt != null && state.nextReviewAt!! > before)
        assertEquals(1, database.wordDao().reviewLogCount())
    }

    @Test
    fun studySessionIsRecordedOnlyWithReviews() = runTest {
        repository.seedIfNeeded()
        val ended = System.currentTimeMillis()
        val started = ended - 120_000
        repository.recordSession(
            mode = StudyMode.CARD,
            startedAt = started,
            endedAt = ended,
            plannedCount = 20,
            reviewedCount = 3,
            correctCount = 2,
        )
        val sessions = repository.sessions.first()
        assertEquals(1, sessions.size)
        assertEquals(3, sessions.first().reviewedCount)
        assertEquals(2, sessions.first().correctCount)

        repository.recordSession(
            mode = StudyMode.CARD,
            startedAt = ended,
            endedAt = ended + 5_000,
            plannedCount = 20,
            reviewedCount = 0,
            correctCount = 0,
        )
        assertEquals(1, repository.sessions.first().size)
    }

    @Test
    fun againSchedulesTenMinutes() = runTest {
        repository.seedIfNeeded()
        repository.submitRating(1, StudyRating.AGAIN, StudyMode.CARD)
        val state = database.wordDao().state(1)!!
        assertEquals(10, state.intervalMinutes)
        assertEquals(1, state.lapses)
    }
}
