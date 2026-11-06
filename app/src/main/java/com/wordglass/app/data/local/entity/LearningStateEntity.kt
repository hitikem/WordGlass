package com.wordglass.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "learning_states",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("wordId"), Index("nextReviewAt")],
)
data class LearningStateEntity(
    @PrimaryKey val wordId: Int,
    val masteryLevel: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val reviewCount: Int = 0,
    val isFavorite: Boolean = false,
    val easeFactor: Double = 2.5,
    val intervalMinutes: Int = 0,
    val repetitions: Int = 0,
    val lapses: Int = 0,
    val consecutiveCorrect: Int = 0,
    val lastRating: String? = null,
    val lastReviewedAt: Long? = null,
    val nextReviewAt: Long? = null,
    val updatedAt: Long = 0L,
)
