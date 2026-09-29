package com.pobvol.pobvolchecklists.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities =
    [
        RecordEntity::class,
        LanguageEntity::class,
        AnswerTypeEntity::class,
        CategoryEntity::class,
        ChecklistEntity::class,
        ChecklistQuestionEntity::class,
        ChecklistSubmissionEntity::class,
        ChecklistAnswerEntity::class
    ],
    version = 3,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao
    abstract fun languageDao(): LanguageDao
    abstract fun answertypeDao(): AnswerTypeDao
    abstract fun categoryDao(): CategoryDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun checklistquestionDao(): ChecklistQuestionDao
    abstract fun checklistsubmissionDao(): ChecklistSubmissionDao
    abstract fun checklistanswerDao(): ChecklistAnswerDao
    abstract fun appDao(): AppDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database.sqlite3"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
