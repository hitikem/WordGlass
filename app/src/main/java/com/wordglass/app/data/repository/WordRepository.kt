package com.wordglass.app.data.repository

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.wordglass.app.data.local.dao.WordDao
import com.wordglass.app.data.local.entity.LearningStateEntity
import com.wordglass.app.data.local.entity.ReviewLogEntity
import com.wordglass.app.data.local.entity.WordEntity
import com.wordglass.app.model.StudyMode
import com.wordglass.app.model.StudyRating
import com.wordglass.app.model.Word
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import org.json.JSONObject

class WordRepository(
    private val dao: WordDao,
    private val context: Context,
) {
    val words: Flow<List<Word>> = combine(
        dao.observeWords(),
        dao.observeStates(),
    ) { entities, states ->
        val stateById = states.associateBy { it.wordId }
        val now = System.currentTimeMillis()
        entities.map { entity ->
            val state = stateById[entity.id]
            entity.toDomain(
                state = state,
                now = now,
            )
        }
    }

    @WorkerThread
    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) {
        if (dao.wordCount() > 0) return@withContext
        val json = context.assets.open("words.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val array = root.getJSONArray("words")
        val now = System.currentTimeMillis()
        val words = ArrayList<WordEntity>(array.length())

        for (index in 0 until array.length()) {
            val item = array.getJSONObject(index)
            val rawWord = item.optString("word").trim()
            if (rawWord.isBlank()) continue
            val id = item.optInt("id", index + 1)
            words += WordEntity(
                id = id,
                word = rawWord,
                normalizedWord = normalize(rawWord),
                phonetic = item.optStringOrNull("phonetic"),
                meaning = item.optStringOrNull("meaning"),
                example = item.optStringOrNull("example"),
                translation = item.optStringOrNull("translation"),
                root = item.optStringOrNull("root"),
                affix = item.optStringOrNull("affix"),
                usage = item.optStringOrNull("usage"),
                audioUsMale = item.optStringOrNull("audioUsMale"),
                audioUsFemale = item.optStringOrNull("audioUsFemale"),
                source = "pdf-887",
                sourceNumber = item.optInt("sourceNumber", id),
                sourcePage = item.optInt("sourcePage", 0).takeIf { it > 0 },
                createdAt = now,
                updatedAt = now,
            )
        }
        val states = words.map { word ->
            LearningStateEntity(
                wordId = word.id,
                updatedAt = now,
            )
        }
        dao.seed(words, states)
        Log.i(
            "WordGlass",
            "Seeded ${words.size} words; source rows=${array.length()}",
        )
    }

    suspend fun toggleFavorite(id: Int, favorite: Boolean) {
        dao.updateFavorite(id, favorite, System.currentTimeMillis())
    }

    suspend fun setMastery(id: Int, level: Int) {
        dao.updateMastery(id, level.coerceIn(0, 5), System.currentTimeMillis())
    }

    suspend fun bumpWrong(id: Int) {
        dao.bumpWrong(id, System.currentTimeMillis())
    }

    /**
     * SM-2-inspired scheduler. Intervals are intentionally simple and versioned here so the
     * algorithm can be tested and tuned without touching the UI.
     */
    suspend fun submitRating(
        id: Int,
        rating: StudyRating,
        mode: StudyMode,
        responseTimeMs: Long = 0L,
    ) {
        val now = System.currentTimeMillis()
        val previous = dao.state(id) ?: LearningStateEntity(wordId = id, updatedAt = now)
        val intervalBefore = previous.intervalMinutes
        val quality = when (rating) {
            StudyRating.AGAIN -> 0
            StudyRating.HARD -> 3
            StudyRating.GOOD -> 4
            StudyRating.EASY -> 5
        }
        val easeDelta = when (rating) {
            StudyRating.AGAIN -> -0.20
            StudyRating.HARD -> -0.10
            StudyRating.GOOD -> 0.0
            StudyRating.EASY -> 0.10
        }
        val easeAfter = (previous.easeFactor + easeDelta).coerceIn(1.3, 3.2)
        val intervalAfter = when (rating) {
            StudyRating.AGAIN -> 10
            StudyRating.HARD -> when {
                previous.repetitions <= 0 -> 10
                else -> max(10, (previous.intervalMinutes * 1.2).toInt())
            }
            StudyRating.GOOD -> when (previous.repetitions) {
                0 -> 10
                1 -> 24 * 60
                else -> max(24 * 60, (previous.intervalMinutes * easeAfter).toInt())
            }
            StudyRating.EASY -> when (previous.repetitions) {
                0 -> 24 * 60
                else -> max(24 * 60, (previous.intervalMinutes * easeAfter * 1.35).toInt())
            }
        }.coerceAtMost(365 * 24 * 60)

        val positive = rating == StudyRating.GOOD || rating == StudyRating.EASY
        val mastery = when (rating) {
            StudyRating.AGAIN -> max(0, previous.masteryLevel - 1)
            StudyRating.HARD -> max(1, previous.masteryLevel)
            StudyRating.GOOD -> min(5, previous.masteryLevel + 1)
            StudyRating.EASY -> min(5, previous.masteryLevel + 2)
        }
        val next = previous.copy(
            masteryLevel = mastery,
            correctCount = previous.correctCount + if (positive) 1 else 0,
            wrongCount = previous.wrongCount + if (positive) 0 else 1,
            reviewCount = previous.reviewCount + 1,
            easeFactor = easeAfter,
            intervalMinutes = intervalAfter,
            repetitions = if (rating == StudyRating.AGAIN) 0 else previous.repetitions + 1,
            lapses = previous.lapses + if (rating == StudyRating.AGAIN) 1 else 0,
            consecutiveCorrect = if (positive) previous.consecutiveCorrect + 1 else 0,
            lastRating = rating.name,
            lastReviewedAt = now,
            nextReviewAt = now + intervalAfter * 60_000L,
            updatedAt = now,
        )
        dao.upsertState(next)
        dao.insertReview(
            ReviewLogEntity(
                wordId = id,
                mode = mode.name,
                rating = rating.name,
                correct = positive,
                responseTimeMs = responseTimeMs,
                reviewedAt = now,
                intervalBeforeMinutes = intervalBefore,
                intervalAfterMinutes = intervalAfter,
                easeAfter = easeAfter,
            ),
        )
        // Quality is retained in the log even when a future scheduler version changes.
        check(quality in 0..5)
    }

    private fun WordEntity.toDomain(
        state: LearningStateEntity?,
        now: Long,
    ): Word = Word(
        id = id,
        word = word,
        phonetic = phonetic.orEmpty(),
        meaning = meaning.orEmpty(),
        example = example,
        translation = translation,
        root = root,
        affix = affix,
        usage = usage,
        mastery = state?.masteryLevel ?: 0,
        favorite = state?.isFavorite ?: false,
        correctCount = state?.correctCount ?: 0,
        wrongCount = state?.wrongCount ?: 0,
        reviewCount = state?.reviewCount ?: 0,
        lastReviewedAt = state?.lastReviewedAt,
        nextReviewAt = state?.nextReviewAt,
        due = state?.nextReviewAt?.let { it <= now } ?: false,
        sourceNumber = sourceNumber,
    )

    private fun normalize(value: String): String =
        value.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")
}

private fun JSONObject.optStringOrNull(key: String): String? {
    if (!has(key) || isNull(key)) return null
    return optString(key).trim().takeIf { it.isNotEmpty() }
}
