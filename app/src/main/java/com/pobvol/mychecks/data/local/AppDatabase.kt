package com.pobvol.mychecks.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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
    version = 14,
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

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `checklist_questions_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `checklistid` INTEGER NOT NULL,
                        `sortno` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT,
                        `type` TEXT NOT NULL,
                        `options` TEXT,
                        `required` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        FOREIGN KEY(`checklistid`) REFERENCES `checklists`(`id`) ON DELETE CASCADE
                    )
                """)
                db.execSQL("""
                    INSERT INTO `checklist_questions_new` (`id`, `checklistid`, `sortno`, `title`, `description`, `type`, `options`, `required`, `timestamp`)
                    SELECT `id`, `checklistid`, `sortno`, `title`, `description`, `type`, `options`, `required`, `timestamp`
                    FROM `checklist_questions`
                """)
                db.execSQL("DROP TABLE `checklist_questions`")
                db.execSQL("ALTER TABLE `checklist_questions_new` RENAME TO `checklist_questions`")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_categories_category` ON `categories` (`category`)")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_answer_types_type` ON `answer_types` (`type`)")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("INSERT OR IGNORE INTO answer_types (type, title) VALUES ('nfc', 'NFC')")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_checklist_questions_checklistid` ON `checklist_questions` (`checklistid`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `checklist_submissions_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `checklistid` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `inspector` TEXT,
                        `notes` TEXT,
                        `date` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        FOREIGN KEY(`checklistid`) REFERENCES `checklists`(`id`) ON DELETE CASCADE
                    )
                """)
                db.execSQL("""
                    INSERT INTO `checklist_submissions_new` (`id`, `checklistid`, `status`, `inspector`, `notes`, `date`, `timestamp`)
                    SELECT `id`, `checklistid`, `status`, `inspector`, `notes`, `date`, `timestamp`
                    FROM `checklist_submissions`
                """)
                db.execSQL("DROP TABLE `checklist_submissions`")
                db.execSQL("ALTER TABLE `checklist_submissions_new` RENAME TO `checklist_submissions`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_checklist_submissions_checklistid` ON `checklist_submissions` (`checklistid`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `checklist_answers_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `submissionid` INTEGER NOT NULL,
                        `questionid` INTEGER NOT NULL,
                        `value` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        FOREIGN KEY(`submissionid`) REFERENCES `checklist_submissions`(`id`) ON DELETE CASCADE,
                        FOREIGN KEY(`questionid`) REFERENCES `checklist_questions`(`id`) ON DELETE CASCADE
                    )
                """)
                db.execSQL("""
                    INSERT INTO `checklist_answers_new` (`id`, `submissionid`, `questionid`, `value`, `timestamp`)
                    SELECT `id`, `submissionid`, `questionid`, `value`, `timestamp`
                    FROM `checklist_answers`
                """)
                db.execSQL("DROP TABLE `checklist_answers`")
                db.execSQL("ALTER TABLE `checklist_answers_new` RENAME TO `checklist_answers`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_checklist_answers_submissionid` ON `checklist_answers` (`submissionid`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_checklist_answers_questionid` ON `checklist_answers` (`questionid`)")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("INSERT OR IGNORE INTO answer_types (type, title) VALUES ('qr-code', 'QR-Code')")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `checklist_answers` ADD COLUMN `notes` TEXT")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `checklist_questions` ADD COLUMN `minValue` REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE `checklist_questions` ADD COLUMN `maxValue` REAL DEFAULT NULL")
            }
        }


        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mychecksdb.sqlite3",
                )
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14)
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
                                db.execSQL("INSERT OR IGNORE INTO answer_types (type, title) VALUES ('number', 'Number')")
                                db.execSQL("INSERT OR IGNORE INTO answer_types (type, title) VALUES ('text', 'Text')")
                                db.execSQL("INSERT OR IGNORE INTO answer_types (type, title) VALUES ('nfc', 'NFC')")
                                db.execSQL("INSERT OR IGNORE INTO answer_types (type, title) VALUES ('qr-code', 'QR-Code')")

                                db.execSQL("INSERT OR IGNORE INTO categories (category, title) VALUES ('HEALTH', 'Health')")
                                db.execSQL("INSERT OR IGNORE INTO categories (category, title) VALUES ('UVV', 'UVV')")

                                db.execSQL("INSERT OR IGNORE INTO checklists (id, title, language, description, category, icon, timestamp) VALUES (1,'UVV Prüfung Flurförderzeuge','English','Beispiel für eine UVV Checkliste','UVV','',1791566533405)")

                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES (1,1,2,'Abgas','','combobox','ohne erkennbare Mängel, Reparatur erforderlich, Mängel beseitigt',1,1791565513627,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES (2,1,3,'Bereifung','','combobox','ohne erkennbare Mängel, Reparatur erforderlich, Mängel beseitigt',1,1791565540674,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES (3,1,4,'DGUV V3 Ladegerät','','combobox','ohne erkennbare Mängel, Reparatur erforderlich, Mängel beseitigt',1,1791565566843,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES (4,1,5,'Elektrische Ausrüstung','','combobox','ohne erkennbare Mängel, Reparatur erforderlich, Mängel beseitigt',1,1791565590303,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES (5,1,6,'Fahrersitz und Bedienelemente','','combobox','ohne erkennbare Mängel, Reparatur erforderlich, Mängel beseitigt',1,1791565648815,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES (6,1,7,'Gasanlage','','combobox','ohne erkennbare Mängel, Reparatur erforderlich, Mängel beseitigt',1,1791565668282,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES (7,1,8,'Hubwerk','','combobox','ohne erkennbare Mängel, Reparatur erforderlich, Mängel beseitigt',1,1791565686399,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES (8,1,9,'Hydrauliksystem','','combobox','ohne erkennbare Mängel, Reparatur erforderlich, Mängel beseitigt',1,1791565707843,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES (9,1,10,'Ketten','','combobox','ohne erkennbare Mängel, Reparatur erforderlich, Mängel beseitigt',1,1791565730095,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES (10,1,11,'Rahmen','','combobox','ohne erkennbare Mängel, Reparatur erforderlich, Mängel beseitigt',1,1791565750737,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES (11,1,13,'Plakette erteilt?','','checkbox',NULL,1,1791565490961,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES (12,1,12,'Sicherheitsausrüstung','','combobox','ohne erkennbare Mängel, Reparatur erforderlich, Mängel beseitigt',1,1791565796128,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES (13,1,1,'Kunde und Flurförderzeug','Daten zu Kunde und Flurförderzeug erfassn. Diese können auch von einem NFC Tag gelesen werden, wenn das benutzte Smartphen/Tablet NFC unterstützt.','nfc',NULL,1,1791719679758,NULL,NULL)")

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
