package com.wordglass.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.wordglass.app.data.local.dao.WordDao
import com.wordglass.app.data.local.entity.LearningStateEntity
import com.wordglass.app.data.local.entity.ReviewLogEntity
import com.wordglass.app.data.local.entity.StudySessionEntity
import com.wordglass.app.data.local.entity.WordEntity

@Database(
    entities = [
        WordEntity::class,
        LearningStateEntity::class,
        ReviewLogEntity::class,
        StudySessionEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class WordDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao

    companion object {
        fun create(context: Context): WordDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                WordDatabase::class.java,
                "wordglass.db",
            ).build()
    }
}
