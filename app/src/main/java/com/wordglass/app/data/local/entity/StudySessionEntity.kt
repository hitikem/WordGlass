package com.wordglass.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_sessions",
    indices = [Index("startedAt")],
)
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String,
    val startedAt: Long,
    val endedAt: Long?,
    val plannedCount: Int,
    val reviewedCount: Int,
    val correctCount: Int,
)
