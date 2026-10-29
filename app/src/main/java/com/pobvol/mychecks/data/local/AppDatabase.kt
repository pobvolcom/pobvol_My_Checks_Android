package com.pobvol.mychecks.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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
    version = 4,
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

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `text_blocks` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sortno` INTEGER NOT NULL,
                        `text` TEXT NOT NULL
                    )
                """)
                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (1, 1, 'Wir haben keine Mängel gefunden. Einer Nutzung stehen keine Bedenken entgegen. Sie bekommen die neue Prüfplakette.')")
                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (2, 2, 'Wir haben Mängel beseitigt. Einer Nutzung stehen keine Bedenken entgegen. Sie bekommen die neue Prüfplakette.')")
                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (3, 3, 'Wir haben wenige kleine Mängel gefunden. Einer Nutzung stehen keine Bedenken entgegen. Sie bekommen die neue Prüfplakette. Gekennzeichnete Mängel sollten jedoch zeitnah beseitigt werden!')")
                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (4, 4, 'Wegen erheblichen Sicherheitsmängeln stehen einer Nutzung Bedenken entgegen! Sie erhalten daher keine Plakette.')")
                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (5, 5, 'Wegen gefährlichen Sicherheitsmängeln, die eine direkte und unmittelbare Gefährdung darstellen oder die Umwelt beeinträchtigen, stehen einer Nutzung Bedenken entgegen! Sie erhalten daher keine Plakette.')")
                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (6, 6, 'We did not find any defects. There are no objections to its use. You will receive the new inspection sticker.')")
                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (7, 7, 'We eliminated any defects found. There are no objections to its use. You will receive the new inspection sticker.')")
                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (8, 8, 'We found a few small defects. There are no objections to its use. You will receive the new inspection sticker. However, marked defects should be eliminated promptly!')")
                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (9, 9, 'Due to considerable safety deficiencies, there are concerns about its use! Therefore, you will not receive a sticker.')")
                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (10, 10, 'Due to dangerous safety deficiencies that pose a direct and immediate hazard or affect the environment, there are concerns about its use! Therefore, you will not receive a sticker.')")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `checklist_answers` ADD COLUMN `pictureSize` TEXT NOT NULL DEFAULT 'medium'")
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("INSERT OR IGNORE INTO checklists (id, title, language, description, category, icon, timestamp) VALUES ('checklist-3','UVV Prüfung Flurförderzeuge Leitern und Tritte','Deutsch','Geprüft werden alle Komponenten. Ist eine mangelhaft, darf die Leiter/der Tritt in der Regel nicht weiter genutzt werden.','UVV','',1791566533405)")
                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-1','checklist-3',1,'NFC-Tag','Wurde ein NFC-Tag angebracht, können die Daten übernommen werden.','nfc',NULL,0,1791566533405,NULL,NULL)")
                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-2','checklist-3',3,'Holme/Stufen/Sprossen/Plattform','Geprüft wird auf Verformung, Beschädigung, Scharfe Kanten, Schutzbehandlung, Verbindung zum Holm, Abnutzung, Korrosion','combobox','in Ordnung [grün], mangelhaft [rot]',1,1791566533405,NULL,NULL)")
                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-3','checklist-3',4,'Spreizsicherung','Geprüft wird auf Vollständigkeit/Befestigung, Funktionsfähigkeit, Beschädigung','combobox','in Ordnung [grün], mangelhaft [rot]',1,1791566533405,NULL,NULL)")
                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-4','checklist-3',5,'Beschlagteile','Geprüft wird auf Beschädigung/Korrosion, Vollständigkeit/Befestigung, Funktionsfähigkeit, Abnutzung, Schmierung (mechanische Teile)','combobox','in Ordnung [grün], mangelhaft [rot]',1,1791566533405,NULL,NULL)")
                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-5','checklist-3',6,'Leiter-/Trittfüße/Rollen','Geprüft wird auf Vollständigkeit/Befestigung, Abnutzung/Beschädigung, Funktionsfähigkeit','combobox','in Ordnung [grün], mangelhaft [rot]',1,1791566533405,NULL,NULL)")
                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-6','checklist-3',7,'Zubehör (z.B. Holmverlängerung, Fußverbreiterung, Wandabstützung)','Geprüft wird auf Vollständigkeit/Befestigung/Beschädigung','combobox','in Ordnung [grün], mangelhaft [rot]',1,1791566533405,NULL,NULL)")
                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-7','checklist-3',8,'Kennzeichnung','Geprüft wird die Betriebsanleitung (z.B. Piktogramm)','combobox','in Ordnung [grün], mangelhaft [rot]',1,1791566533405,NULL,NULL)")
                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-8','checklist-3',9,'Kontrollergebnis','','combobox','Leiter/Tritt i.O. und verwendungsfähig [grün], Reparatur notwendig [gelb], Leiter/Tritt sofort aussondern [rot]',1,1791566533405,NULL,NULL)")
                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-9','checklist-3',10,'Sicherheitsrelevante Kommentare','','text',NULL,0,1791566533405,NULL,NULL)")
                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-10','checklist-3',2,'QR-Code','Wurde ein QR-Code angebracht, können die Daten übernommen werden.','qr-code',NULL,0,1791566533405,NULL,NULL)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mychecksdb.sqlite3",
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
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

                                db.execSQL("INSERT OR IGNORE INTO checklists (id, title, language, description, category, icon, timestamp) VALUES ('checklist-1','UVV Prüfung Flurförderzeuge','Deutsch','Beispiel für eine UVV Checkliste','UVV','',1791566533405)")
                                db.execSQL("INSERT OR IGNORE INTO checklists (id, title, language, description, category, icon, timestamp) VALUES ('checklist-2','Blutdruckmessung','Deutsch','Beispiel für eine Blutdruckmessung','Health','',1791566533405)")
                                db.execSQL("INSERT OR IGNORE INTO checklists (id, title, language, description, category, icon, timestamp) VALUES ('checklist-3','UVV Prüfung Flurförderzeuge Leitern und Tritte','Deutsch','Geprüft werden alle Komponenten. Ist eine mangelhaft, darf die Leiter/der Tritt in der Regel nicht weiter genutzt werden.','UVV','',1791566533405)")

                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question1-1','checklist-1',1,'NFC-Tag','Sie können Daten von einem NFC-Tag lesen. Das Gerät muss NFC unterstützen und Sie müssen NFC einschalten.','nfc',NULL,0,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question1-2','checklist-1',2,'Abgas','','combobox','ohne erkennbare Mängel [grün], Reparatur erforderlich [rot], Mängel beseitigt [gelb]',1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question1-3','checklist-1',3,'Bereifung','','combobox','ohne erkennbare Mängel [grün], Reparatur erforderlich [rot], Mängel beseitigt [gelb]',1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question1-4','checklist-1',4,'DGUV V3 Ladegerät','','combobox','ohne erkennbare Mängel [grün], Reparatur erforderlich [rot], Mängel beseitigt [gelb]',1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question1-5','checklist-1',5,'Elektrische Ausrüstung','','combobox','ohne erkennbare Mängel [grün], Reparatur erforderlich [rot], Mängel beseitigt [gelb]',1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question1-6','checklist-1',6,'Fahrersitz und Bedienelemente','','combobox','ohne erkennbare Mängel [grün], Reparatur erforderlich [rot], Mängel beseitigt [gelb]',1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question1-7','checklist-1',7,'Gasanlage','','combobox','ohne erkennbare Mängel [grün], Reparatur erforderlich [rot], Mängel beseitigt [gelb]',1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question1-8','checklist-1',8,'Hubwerk','','combobox','ohne erkennbare Mängel [grün], Reparatur erforderlich [rot], Mängel beseitigt [gelb]',1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question1-9','checklist-1',9,'Hydrauliksystem','','combobox','ohne erkennbare Mängel [grün], Reparatur erforderlich [rot], Mängel beseitigt [gelb]',1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question1-10','checklist-1',10,'Ketten','','combobox','ohne erkennbare Mängel [grün], Reparatur erforderlich [rot], Mängel beseitigt [gelb]',1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question1-11','checklist-1',11,'Rahmen','','combobox','ohne erkennbare Mängel [grün], Reparatur erforderlich [rot], Mängel beseitigt [gelb]',1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question1-12','checklist-1',12,'Sicherheitsausrüstung','','combobox','ohne erkennbare Mängel [grün], Reparatur erforderlich [rot], Mängel beseitigt [gelb]',1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question1-13','checklist-1',13,'Plakette erteilt?','','checkbox',NULL,1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question1-14','checklist-1',14,'Sicherheitsrelevante Kommentare','','text',NULL,0,1791566533405,NULL,NULL)")

                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question2-1','checklist-2',1,'Wert1','','number',NULL,1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question2-2','checklist-2',2,'Wert2','','number',NULL,1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question2-3','checklist-2',3,'Puls','','number',NULL,0,1791566533405,NULL,NULL)")

                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-1','checklist-3',1,'NFC-Tag','Wurde ein NFC-Tag angebracht, können die Daten übernommen werden.','nfc',NULL,0,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-2','checklist-3',3,'Holme/Stufen/Sprossen/Plattform','Geprüft wird auf Verformung, Beschädigung, Scharfe Kanten, Schutzbehandlung, Verbindung zum Holm, Abnutzung, Korrosion','combobox','in Ordnung [grün], mangelhaft [rot]',1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-3','checklist-3',4,'Spreizsicherung','Geprüft wird auf Vollständigkeit/Befestigung, Funktionsfähigkeit, Beschädigung','combobox','in Ordnung [grün], mangelhaft [rot]',1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-4','checklist-3',5,'Beschlagteile','Geprüft wird auf Beschädigung/Korrosion, Vollständigkeit/Befestigung, Funktionsfähigkeit, Abnutzung, Schmierung (mechanische Teile)','combobox','in Ordnung [grün], mangelhaft [rot]',1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-5','checklist-3',6,'Leiter-/Trittfüße/Rollen','Geprüft wird auf Vollständigkeit/Befestigung, Abnutzung/Beschädigung, Funktionsfähigkeit','combobox','in Ordnung [grün], mangelhaft [rot]',1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-6','checklist-3',7,'Zubehör (z.B. Holmverlängerung, Fußverbreiterung, Wandabstützung)','Geprüft wird auf Vollständigkeit/Befestigung/Beschädigung','combobox','in Ordnung [grün], mangelhaft [rot]',1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-7','checklist-3',8,'Kennzeichnung','Geprüft wird die Betriebsanleitung (z.B. Piktogramm)','combobox','in Ordnung [grün], mangelhaft [rot]',1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-8','checklist-3',9,'Kontrollergebnis','','combobox','Leiter/Tritt i.O. und verwendungsfähig [grün], Reparatur notwendig [gelb], Leiter/Tritt sofort aussondern [rot]',1,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-9','checklist-3',10,'Sicherheitsrelevante Kommentare','','text',NULL,0,1791566533405,NULL,NULL)")
                                db.execSQL("INSERT OR IGNORE INTO checklist_questions (id, checklistid, sortno, title, description, type, options, required, timestamp, minValue, maxValue) VALUES ('question3-10','checklist-3',2,'QR-Code','Wurde ein QR-Code angebracht, können die Daten übernommen werden.','qr-code',NULL,0,1791566533405,NULL,NULL)")

                                db.execSQL("INSERT OR IGNORE INTO `checklist_submissions` (`id`, `checklistid`, `status`, `inspector`, `notes`, `date`, `timestamp`) VALUES ('submission2-1','checklist-2','COMPLETED', 'Dummy', '', '10.10.2026', 1791566533405)")
                                db.execSQL("INSERT OR IGNORE INTO `checklist_submissions` (`id`, `checklistid`, `status`, `inspector`, `notes`, `date`, `timestamp`) VALUES ('submission2-2','checklist-2','COMPLETED', 'Dummy', '', '11.10.2026', 1791566533405)")
                                db.execSQL("INSERT OR IGNORE INTO `checklist_submissions` (`id`, `checklistid`, `status`, `inspector`, `notes`, `date`, `timestamp`) VALUES ('submission2-3','checklist-2','COMPLETED', 'Dummy', '', '12.10.2026', 1791566533405)")

                                db.execSQL("INSERT OR IGNORE INTO`checklist_answers` (`id`, `submissionid`, `questionid`, `value`, `notes`, `imagePath`, `timestamp`) VALUES ('answer1-1', 'submission2-1', 'question2-1', '120', '', '', 1791566533405)")
                                db.execSQL("INSERT OR IGNORE INTO`checklist_answers` (`id`, `submissionid`, `questionid`, `value`, `notes`, `imagePath`, `timestamp`) VALUES ('answer1-2', 'submission2-1', 'question2-2', '80', '', '', 1791566533405)")
                                db.execSQL("INSERT OR IGNORE INTO`checklist_answers` (`id`, `submissionid`, `questionid`, `value`, `notes`, `imagePath`, `timestamp`) VALUES ('answer2-1', 'submission2-2', 'question2-1', '125', '', '', 1791566533405)")
                                db.execSQL("INSERT OR IGNORE INTO`checklist_answers` (`id`, `submissionid`, `questionid`, `value`, `notes`, `imagePath`, `timestamp`) VALUES ('answer2-2', 'submission2-2', 'question2-2', '85', '', '', 1791566533405)")
                                db.execSQL("INSERT OR IGNORE INTO`checklist_answers` (`id`, `submissionid`, `questionid`, `value`, `notes`, `imagePath`, `timestamp`) VALUES ('answer3-1', 'submission2-3', 'question2-1', '130', '', '', 1791566533405)")
                                db.execSQL("INSERT OR IGNORE INTO`checklist_answers` (`id`, `submissionid`, `questionid`, `value`, `notes`, `imagePath`, `timestamp`) VALUES ('answer3-2', 'submission2-3', 'question2-2', '90', '', '', 1791566533405)")

                                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (1, 1, 'Wir haben keine Mängel gefunden. Einer Nutzung stehen keine Bedenken entgegen. Sie bekommen die neue Prüfplakette.')")
                                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (2, 2, 'Wir haben Mängel beseitigt. Einer Nutzung stehen keine Bedenken entgegen. Sie bekommen die neue Prüfplakette.')")
                                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (3, 3, 'Wir haben wenige kleine Mängel gefunden. Einer Nutzung stehen keine Bedenken entgegen. Sie bekommen die neue Prüfplakette. Gekennzeichnete Mängel sollten jedoch zeitnah beseitigt werden!')")
                                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (4, 4, 'Wegen erheblichen Sicherheitsmängeln stehen einer Nutzung Bedenken entgegen! Sie erhalten daher keine Plakette.')")
                                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (5, 5, 'Wegen gefährlichen Sicherheitsmängeln, die eine direkte und unmittelbare Gefährdung darstellen oder die Umwelt beeinträchtigen, stehen einer Nutzung Bedenken entgegen! Sie erhalten daher keine Plakette.')")
                                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (6, 6, 'We did not find any defects. There are no objections to its use. You will receive the new inspection sticker.')")
                                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (7, 7, 'We eliminated any defects found. There are no objections to its use. You will receive the new inspection sticker.')")
                                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (8, 8, 'We found a few small defects. There are no objections to its use. You will receive the new inspection sticker. However, marked defects should be eliminated promptly!')")
                                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (9, 9, 'Due to considerable safety deficiencies, there are concerns about its use! Therefore, you will not receive a sticker.')")
                                db.execSQL("INSERT OR IGNORE INTO text_blocks (`id`, `sortno`, `text`) VALUES (10, 10, 'Due to dangerous safety deficiencies that pose a direct and immediate hazard or affect the environment, there are concerns about its use! Therefore, you will not receive a sticker.')")

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
