package com.wordglass.app.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.wordglass.app.data.local.dao.WordDao
import com.wordglass.app.data.local.entity.LearningStateEntity
import com.wordglass.app.data.local.entity.ReviewLogEntity
import com.wordglass.app.data.local.entity.WordEntity
import com.wordglass.app.model.AppSettings
import com.wordglass.app.model.ThemeMode
import com.wordglass.app.model.VoiceGender
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class BackupRepository(
    private val dao: WordDao,
    private val settingsRepository: SettingsRepository,
) {
    suspend fun export(
        resolver: ContentResolver,
        uri: Uri,
        now: Long = System.currentTimeMillis(),
    ): Int = withContext(Dispatchers.IO) {
        val words = dao.allWords()
        val states = dao.allStates()
        val reviews = dao.allReviews()
        val settings = settingsRepository.settings.first()
        val root = JSONObject().apply {
            put("schemaVersion", SCHEMA_VERSION)
            put("exportedAt", now)
            put("app", "WordGlass")
            put("wordCount", words.size)
            put("settings", settings.toJson())
            put("words", JSONArray().apply { words.forEach { put(it.toJson()) } })
            put("states", JSONArray().apply { states.forEach { put(it.toJson()) } })
            put("reviews", JSONArray().apply { reviews.forEach { put(it.toJson()) } })
        }
        resolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
            writer.write(root.toString(2))
        } ?: throw IOException("Cannot open backup destination")
        words.size
    }

    suspend fun importBackup(
        resolver: ContentResolver,
        uri: Uri,
        includeLearning: Boolean,
    ): Int = withContext(Dispatchers.IO) {
        val text = resolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            ?: throw IOException("Cannot open backup source")
        val root = JSONObject(text)
        require(root.optInt("schemaVersion", 0) == SCHEMA_VERSION) {
            "Unsupported backup schema"
        }
        val wordsJson = root.optJSONArray("words") ?: JSONArray()
        require(wordsJson.length() > 0) { "Backup has no words" }
        val now = System.currentTimeMillis()
        val words = ArrayList<WordEntity>(wordsJson.length())
        for (index in 0 until wordsJson.length()) {
            val item = wordsJson.getJSONObject(index)
            val id = item.optInt("id", 0)
            val word = item.optString("word").trim()
            if (id <= 0 || word.isBlank()) continue
            words += WordEntity(
                id = id,
                word = word,
                normalizedWord = normalize(word),
                phonetic = item.optStringOrNull("phonetic"),
                meaning = item.optStringOrNull("meaning"),
                example = item.optStringOrNull("example"),
                translation = item.optStringOrNull("translation"),
                root = item.optStringOrNull("root"),
                affix = item.optStringOrNull("affix"),
                usage = item.optStringOrNull("usage"),
                audioUsMale = item.optStringOrNull("audioUsMale"),
                audioUsFemale = item.optStringOrNull("audioUsFemale"),
                source = item.optStringOrNull("source"),
                sourceNumber = item.optInt("sourceNumber", id),
                sourcePage = item.optInt("sourcePage", 0).takeIf { it > 0 },
                createdAt = item.optLong("createdAt", now),
                updatedAt = now,
            )
        }
        dao.insertWords(words)
        // Imported words need an initial state row even when only the dictionary is imported.
        dao.insertStates(
            words.map { LearningStateEntity(wordId = it.id, updatedAt = now) },
        )
        if (includeLearning) {
            val statesJson = root.optJSONArray("states") ?: JSONArray()
            for (index in 0 until statesJson.length()) {
                val item = statesJson.getJSONObject(index)
                val wordId = item.optInt("wordId", 0)
                if (wordId <= 0) continue
                dao.upsertState(
                    LearningStateEntity(
                        wordId = wordId,
                        masteryLevel = item.optInt("masteryLevel", 0).coerceIn(0, 5),
                        correctCount = item.optInt("correctCount", 0),
                        wrongCount = item.optInt("wrongCount", 0),
                        reviewCount = item.optInt("reviewCount", 0),
                        isFavorite = item.optBoolean("isFavorite", false),
                        easeFactor = item.optDouble("easeFactor", 2.5).coerceIn(1.3, 3.2),
                        intervalMinutes = item.optInt("intervalMinutes", 0),
                        repetitions = item.optInt("repetitions", 0),
                        lapses = item.optInt("lapses", 0),
                        consecutiveCorrect = item.optInt("consecutiveCorrect", 0),
                        lastRating = item.optStringOrNull("lastRating"),
                        lastReviewedAt = item.optLongOrNull("lastReviewedAt"),
                        nextReviewAt = item.optLongOrNull("nextReviewAt"),
                        updatedAt = now,
                    ),
                )
            }
            val reviewsJson = root.optJSONArray("reviews") ?: JSONArray()
            for (index in 0 until reviewsJson.length()) {
                val item = reviewsJson.getJSONObject(index)
                val wordId = item.optInt("wordId", 0)
                if (wordId <= 0) continue
                dao.insertReview(
                    ReviewLogEntity(
                        wordId = wordId,
                        mode = item.optString("mode", "CARD"),
                        rating = item.optString("rating", "GOOD"),
                        correct = item.optBoolean("correct", true),
                        responseTimeMs = item.optLong("responseTimeMs", 0),
                        reviewedAt = item.optLong("reviewedAt", now),
                        intervalBeforeMinutes = item.optInt("intervalBeforeMinutes", 0),
                        intervalAfterMinutes = item.optInt("intervalAfterMinutes", 0),
                        easeAfter = item.optDouble("easeAfter", 2.5),
                    ),
                )
            }
            root.optJSONObject("settings")?.let { settings ->
                settings.optString("theme").let { name ->
                    ThemeMode.entries.firstOrNull { it.name == name }?.let {
                        settingsRepository.setTheme(it)
                    }
                }
                settings.optString("voice").let { name ->
                    VoiceGender.entries.firstOrNull { it.name == name }?.let {
                        settingsRepository.setVoice(it)
                    }
                }
                settings.optDouble("speechRate", 0.9).toFloat().let {
                    settingsRepository.setSpeechRate(it)
                }
                settingsRepository.setAutoPlay(settings.optBoolean("autoPlay", false))
                settingsRepository.setDailyGoal(settings.optInt("dailyGoal", 25))
            }
        }
        words.size
    }

    private fun WordEntity.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("word", word)
        put("normalizedWord", normalizedWord)
        put("phonetic", phonetic)
        put("meaning", meaning)
        put("example", example)
        put("translation", translation)
        put("root", root)
        put("affix", affix)
        put("usage", usage)
        put("audioUsMale", audioUsMale)
        put("audioUsFemale", audioUsFemale)
        put("source", source)
        put("sourceNumber", sourceNumber)
        put("sourcePage", sourcePage)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

    private fun LearningStateEntity.toJson(): JSONObject = JSONObject().apply {
        put("wordId", wordId)
        put("masteryLevel", masteryLevel)
        put("correctCount", correctCount)
        put("wrongCount", wrongCount)
        put("reviewCount", reviewCount)
        put("isFavorite", isFavorite)
        put("easeFactor", easeFactor)
        put("intervalMinutes", intervalMinutes)
        put("repetitions", repetitions)
        put("lapses", lapses)
        put("consecutiveCorrect", consecutiveCorrect)
        put("lastRating", lastRating)
        put("lastReviewedAt", lastReviewedAt)
        put("nextReviewAt", nextReviewAt)
        put("updatedAt", updatedAt)
    }

    private fun ReviewLogEntity.toJson(): JSONObject = JSONObject().apply {
        put("wordId", wordId)
        put("mode", mode)
        put("rating", rating)
        put("correct", correct)
        put("responseTimeMs", responseTimeMs)
        put("reviewedAt", reviewedAt)
        put("intervalBeforeMinutes", intervalBeforeMinutes)
        put("intervalAfterMinutes", intervalAfterMinutes)
        put("easeAfter", easeAfter)
    }

    private fun AppSettings.toJson(): JSONObject = JSONObject().apply {
        put("theme", themeMode.name)
        put("voice", voice.name)
        put("speechRate", speechRate)
        put("autoPlay", autoPlay)
        put("dailyGoal", dailyGoal)
    }

    private fun normalize(value: String): String =
        value.trim().lowercase(java.util.Locale.ROOT).replace(Regex("\\s+"), " ")

    private fun JSONObject.optStringOrNull(key: String): String? {
        if (!has(key) || isNull(key)) return null
        return optString(key).trim().takeIf { it.isNotEmpty() }
    }

    private fun JSONObject.optLongOrNull(key: String): Long? {
        if (!has(key) || isNull(key)) return null
        return optLong(key).takeIf { it > 0L }
    }

    private companion object {
        const val SCHEMA_VERSION = 1
    }
}
