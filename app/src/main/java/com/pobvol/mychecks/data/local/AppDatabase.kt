package com.pobvol.mychecks.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        DeviceEntity::class,
        RecordEntity::class,
        LanguageEntity::class,
        CategoryEntity::class,
        AnswerTypeEntity::class,
        TextBlockEntity::class,
        ChecklistEntity::class,
        ChecklistQuestionEntity::class,
        ChecklistSubmissionEntity::class,
        ChecklistAnswerEntity::class,
    ],
    version = 6,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun deviceDao(): DeviceDAO
    abstract fun recordDao(): RecordDAO
    abstract fun languageDao(): LanguageDAO
    abstract fun answertypeDao(): AnswerTypeDAO

    abstract fun textblockDao(): TextBlockDAO
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
                    "mychecksdb.sqlite3",
                )
                    .addMigrations(
                        //MIGRATION_1_2,
                        //MIGRATION_2_3,
                    )
                    .fallbackToDestructiveMigration(true)
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
                                db.execSQL("INSERT OR IGNORE INTO answer_types (type, title) VALUES ('multiselect', 'Multi-select')")
                                db.execSQL("INSERT OR IGNORE INTO answer_types (type, title) VALUES ('number', 'Number')")
                                db.execSQL("INSERT OR IGNORE INTO answer_types (type, title) VALUES ('text', 'Text')")
                                db.execSQL("INSERT OR IGNORE INTO answer_types (type, title) VALUES ('nfc', 'NFC')")
                                db.execSQL("INSERT OR IGNORE INTO answer_types (type, title) VALUES ('qr-code', 'QR-Code')")

                                db.execSQL("INSERT OR IGNORE INTO categories (category, title) VALUES ('HEALTH', 'Health')")
                                db.execSQL("INSERT OR IGNORE INTO categories (category, title) VALUES ('UVV', 'UVV')")
                                db.execSQL("INSERT OR IGNORE INTO categories (category, title) VALUES ('PERSONAL', 'Personal')")

                            }
                        },
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun resetDatabaseInstance() {
            synchronized(this) {
                try {
                    INSTANCE?.close()
                } catch (_: Exception) {
                }
                INSTANCE = null
            }
        }
    }
}
