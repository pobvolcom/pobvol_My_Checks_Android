package com.pobvol.pobvolchecklists.data.repository

import com.pobvol.pobvolchecklists.data.local.RecordDao
import com.pobvol.pobvolchecklists.data.local.ChecklistDao
import com.pobvol.pobvolchecklists.data.local.LanguageDao
import com.pobvol.pobvolchecklists.data.local.RecordEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistEntity
import com.pobvol.pobvolchecklists.data.local.LanguageEntity
import com.pobvol.pobvolchecklists.data.repository.LanguageRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RepositoryTest {

    private lateinit var fakerecorddao: FakeRecordDao
    private lateinit var fakechecklistdao: FakeChecklistDao
    private lateinit var fakelanguagedao: FakeLanguageDao
    private lateinit var recordrepository: RecordRepository
    private lateinit var checklistrepository: ChecklistRepository

    private lateinit var languagerepository: LanguageRepository


    @Before
    fun setUp() {
        fakerecorddao = FakeRecordDao()
        recordrepository = RecordRepositoryImpl(fakerecorddao)
        fakechecklistdao = FakeChecklistDao()
        checklistrepository = ChecklistRepositoryImpl(fakechecklistdao)
        fakelanguagedao = FakeLanguageDao()
        languagerepository = LanguageRepositoryImpl(fakelanguagedao)
    }

    @Test
    fun insertAndGetRecord() = runTest {
        val record = RecordEntity(id = 1, title = "Test Record", description = "Description", category = "Test")
        val insertedId = recordrepository.insertRecord(record)

        assertEquals(1L, insertedId)
        val fetchedRecord = recordrepository.getRecordById(1)
        assertEquals(record, fetchedRecord)
    }

    @Test
    fun insertAndGetChecklist() = runTest {
        val checklist = ChecklistEntity(id = 1, title = "Test Record", language = "English", description = "Description", category = "Test", icon = "")
        val insertedId = checklistrepository.insertChecklist(checklist)

        assertEquals(1L, insertedId)
        val fetchedChecklist = checklistrepository.getChecklistById(1)

        assertEquals(checklist, fetchedChecklist)
    }

    @Test
    fun insertAndGetLanguage() = runTest {
        LanguageEntity(language = "de", title = "Deutsch")
        LanguageEntity(language = "es", title = "Español")
        LanguageEntity(language = "fr", title = "French")
        val language = LanguageEntity(language = "en", title = "English")
        val insertedLanguage = languagerepository.insertLanguage(language)

        assertEquals(1L, insertedLanguage)
        val fetchedLanguage = languagerepository.getLanguageByLang("en")
        assertEquals(language, fetchedLanguage)
    }

    @Test
    fun getAllRecordsReturnsFlow() = runTest {
        val record1 = RecordEntity(id = 1, title = "Record 1", description = "Desc 1")
        val record2 = RecordEntity(id = 2, title = "Record 2", description = "Desc 2")

        recordrepository.insertRecord(record1)
        recordrepository.insertRecord(record2)

        val records = recordrepository.getAllRecords().first()
        assertEquals(2, records.size)
    }

    @Test
    fun getAllChecklistsReturnsFlow() = runTest {
        val record1 = ChecklistEntity(id = 1, language = "English", title = "Record 1", description = "Desc 1", icon = "")
        val record2 = ChecklistEntity(id = 2, language = "Deutsch", title = "Record 2", description = "Desc 2", icon = "")

        checklistrepository.insertChecklist(record1)
        checklistrepository.insertChecklist(record2)

        val records = checklistrepository.getAllChecklists().first()
        assertEquals(2, records.size)
    }

    @Test
    fun updateRecordUpdatesData() = runTest {
        val record = RecordEntity(id = 1, title = "Original Title", description = "Desc")
        recordrepository.insertRecord(record)

        val updatedRecord = record.copy(title = "Updated Title")
        recordrepository.updateRecord(updatedRecord)

        val fetched = recordrepository.getRecordById(1)
        assertEquals("Updated Title", fetched?.title)
    }

    @Test
    fun updateChecklistUpdatesData() = runTest {
        val checklist = ChecklistEntity(id = 1, language = "English", title = "Original Title", description = "Desc", icon = "")
        checklistrepository.insertChecklist(checklist)

        val updatedChecklist = checklist.copy(title = "Updated Title")
        checklistrepository.updateChecklist(updatedChecklist)

        val fetched = checklistrepository.getChecklistById(1)
        assertEquals("Updated Title", fetched?.title)
    }

    @Test
    fun deleteRecordRemovesData() = runTest {
        val record = RecordEntity(id = 1, title = "To Delete", description = "Desc")
        recordrepository.insertRecord(record)

        recordrepository.deleteRecord(record)

        val fetched = recordrepository.getRecordById(1)
        assertNull(fetched)
    }

    @Test
    fun deleteChecklistRemovesData() = runTest {
        val checklist = ChecklistEntity(id = 1, language = "English", title = "To Delete", description = "Desc", icon = "")
        checklistrepository.insertChecklist(checklist)

        checklistrepository.deleteChecklist(checklist)

        val fetched = checklistrepository.getChecklistById(1)
        assertNull(fetched)
    }

    private class FakeRecordDao : RecordDao {
        private val recordsMap = mutableMapOf<Int, RecordEntity>()
        private val recordsFlow = MutableStateFlow<List<RecordEntity>>(emptyList())

        private fun updateFlow() {
            recordsFlow.value = recordsMap.values.toList().sortedByDescending { it.timestamp }
        }

        override fun getAllRecords(): Flow<List<RecordEntity>> = recordsFlow

        override suspend fun getRecordById(id: Int): RecordEntity? {
            return recordsMap[id]
        }

        override suspend fun insertRecord(record: RecordEntity): Long {
            val id = if (record.id == 0) (recordsMap.keys.maxOrNull() ?: 0) + 1 else record.id
            val newRecord = record.copy(id = id)
            recordsMap[id] = newRecord
            updateFlow()
            return id.toLong()
        }

        override suspend fun updateRecord(record: RecordEntity) {
            recordsMap[record.id] = record
            updateFlow()
        }

        override suspend fun deleteRecord(record: RecordEntity) {
            recordsMap.remove(record.id)
            updateFlow()
        }
    }

    private class FakeLanguageDao : LanguageDao {
        private val languagesMap = mutableMapOf<String, LanguageEntity>()
        private val languagesFlow = MutableStateFlow<List<LanguageEntity>>(emptyList())

        override fun getAllLanguages(): Flow<List<LanguageEntity>> = languagesFlow

        override suspend fun getLanguageByLang(lang: String): LanguageEntity? {
            return languagesMap[lang]
        }

        override suspend fun insertLanguage(language: LanguageEntity): Long {
            val lang = if (language.language == "Null") "en" else "en"
            val newLanguage = language.copy(language = lang)
            languagesMap[lang] = newLanguage
            return lang.toLong()
        }

    }

    private class FakeChecklistDao : ChecklistDao {
        private val checklistsMap = mutableMapOf<Int, ChecklistEntity>()
        private val checklistsFlow = MutableStateFlow<List<ChecklistEntity>>(emptyList())

        private fun updateFlow() {
            checklistsFlow.value = checklistsMap.values.toList().sortedByDescending { it.timestamp }
        }

        override fun getAllChecklists(): Flow<List<ChecklistEntity>> = checklistsFlow

        override suspend fun getChecklistById(id: Int): ChecklistEntity? {
            return checklistsMap[id]
        }

        override suspend fun insertChecklist(checklist: ChecklistEntity): Long {



            val id =
                if (checklist.id == 0) (checklistsMap.keys.maxOrNull() ?: 0) + 1 else checklist.id
            val newChecklist = checklist.copy(id = id)
            checklistsMap[id] = newChecklist
            updateFlow()
            return id.toLong()
        }

        override suspend fun updateChecklist(checklist: ChecklistEntity) {
            checklistsMap[checklist.id] = checklist
            updateFlow()
        }

        override suspend fun deleteChecklist(checklist: ChecklistEntity) {
            checklistsMap.remove(checklist.id)
            updateFlow()
        }
    }

}
