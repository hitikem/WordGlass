package com.wordglass.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.wordglass.app.data.local.entity.LearningStateEntity
import com.wordglass.app.data.local.entity.ReviewLogEntity
import com.wordglass.app.data.local.entity.StudySessionEntity
import com.wordglass.app.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Query(
        "SELECT * FROM words ORDER BY " +
            "CASE WHEN sourceNumber IS NULL THEN 1 ELSE 0 END, sourceNumber, id",
    )
    fun observeWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM learning_states")
    fun observeStates(): Flow<List<LearningStateEntity>>

    @Query("SELECT COUNT(*) FROM words")
    suspend fun wordCount(): Int

    @Query("SELECT COUNT(*) FROM review_logs")
    suspend fun reviewLogCount(): Int

    @Query(
        "SELECT * FROM words ORDER BY " +
            "CASE WHEN sourceNumber IS NULL THEN 1 ELSE 0 END, sourceNumber, id",
    )
    suspend fun allWords(): List<WordEntity>

    @Query("SELECT * FROM learning_states")
    suspend fun allStates(): List<LearningStateEntity>

    @Query("SELECT * FROM review_logs ORDER BY reviewedAt")
    suspend fun allReviews(): List<ReviewLogEntity>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun word(id: Int): WordEntity?

    @Query("SELECT * FROM learning_states WHERE wordId = :id")
    suspend fun state(id: Int): LearningStateEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWords(words: List<WordEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStates(states: List<LearningStateEntity>)

    @Upsert
    suspend fun upsertState(state: LearningStateEntity)

    @Insert
    suspend fun insertReview(log: ReviewLogEntity)

    @Insert
    suspend fun insertSession(session: StudySessionEntity): Long

    @Query("SELECT * FROM study_sessions ORDER BY startedAt DESC LIMIT 500")
    fun observeSessions(): Flow<List<StudySessionEntity>>

    @Query("UPDATE learning_states SET isFavorite = :favorite, updatedAt = :now WHERE wordId = :id")
    suspend fun updateFavorite(id: Int, favorite: Boolean, now: Long)

    @Query("UPDATE learning_states SET masteryLevel = :level, updatedAt = :now WHERE wordId = :id")
    suspend fun updateMastery(id: Int, level: Int, now: Long)

    @Query(
        "UPDATE learning_states SET wrongCount = wrongCount + 1, reviewCount = reviewCount + 1, " +
            "updatedAt = :now WHERE wordId = :id",
    )
    suspend fun bumpWrong(id: Int, now: Long)

    @Query(
        "SELECT * FROM learning_states WHERE nextReviewAt IS NOT NULL " +
            "AND nextReviewAt <= :now ORDER BY nextReviewAt LIMIT :limit",
    )
    suspend fun dueStates(now: Long, limit: Int): List<LearningStateEntity>

    @Transaction
    suspend fun seed(words: List<WordEntity>, states: List<LearningStateEntity>) {
        insertWords(words)
        insertStates(states)
    }
}
