package com.wordglass.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "review_logs",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("wordId"), Index("reviewedAt")],
)
data class ReviewLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wordId: Int,
    val mode: String,
    val rating: String,
    val correct: Boolean,
    val responseTimeMs: Long,
    val reviewedAt: Long,
    val intervalBeforeMinutes: Int,
    val intervalAfterMinutes: Int,
    val easeAfter: Double,
)
