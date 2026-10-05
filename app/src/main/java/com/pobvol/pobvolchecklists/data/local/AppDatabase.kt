package com.pobvol.pobvolchecklists.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        RecordEntity::class,
        LanguageEntity::class,
        CategoryEntity::class,
        AnswerTypeEntity::class,
        ChecklistEntity::class,
        ChecklistQuestionEntity::class,
        ChecklistSubmissionEntity::class,
        ChecklistAnswerEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDAO
    abstract fun languageDao(): LanguageDAO
    abstract fun answertypeDao(): AnswerTypeDAO
    abstract fun categoryDao(): CategoryDAO
    abstract fun checklistDao(): ChecklistDAO
    abstract fun checklistquestionDao(): ChecklistQuestionDAO
    abstract fun checklistsubmissionDao(): ChecklistSubmissionDAO
    abstract fun checklistanswerDao(): ChecklistAnswerDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database.sqlite3",
                )
                    .addCallback(
                        object : Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                db.execSQL("INSERT OR IGNORE INTO languages (language, title) VALUES ('de', 'Deutsch')")
                                db.execSQL("INSERT OR IGNORE INTO languages (language, title) VALUES ('en', 'English')")
                                db.execSQL("INSERT OR IGNORE INTO languages (language, title) VALUES ('es', 'Español')")
                                db.execSQL("INSERT OR IGNORE INTO languages (language, title) VALUES ('fr', 'French')")

                                db.execSQL("INSERT OR IGNORE INTO answer_types (type, title) VALUES ('checkbox', 'Checkbox')")
                                db.execSQL("INSERT OR IGNORE INTO answer_types (type, title) VALUES ('combobox', 'Combobox')")
                                db.execSQL("INSERT OR IGNORE INTO answer_types (type, title) VALUES ('number', 'Number')")
                                db.execSQL("INSERT OR IGNORE INTO answer_types (type, title) VALUES ('text', 'Text')")

                                db.execSQL("INSERT OR IGNORE INTO categories (category, title) VALUES ('HEALTH', 'Health')")
                                db.execSQL("INSERT OR IGNORE INTO categories (category, title) VALUES ('UVV', 'UVV')")
                            }
                        },
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
