package com.wordglass.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "words",
    indices = [
        Index(value = ["normalizedWord"]),
        Index(value = ["sourceNumber"]),
    ],
)
data class WordEntity(
    @PrimaryKey val id: Int,
    val word: String,
    val normalizedWord: String,
    val phonetic: String?,
    val meaning: String?,
    val example: String?,
    val translation: String?,
    val root: String?,
    val affix: String?,
    val usage: String?,
    val audioUsMale: String?,
    val audioUsFemale: String?,
    val source: String?,
    val sourceNumber: Int?,
    val sourcePage: Int?,
    val createdAt: Long,
    val updatedAt: Long,
)
