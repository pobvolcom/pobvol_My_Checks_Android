package com.pobvol.pobvolchecklists.data.repository

import com.pobvol.pobvolchecklists.data.local.RecordDAO
import com.pobvol.pobvolchecklists.data.local.RecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RecordRepositoryTest {

    private lateinit var fakeDao: FakeRecordDao
    private lateinit var repository: RecordRepository

    @Before
    fun setUp() {
        fakeDao = FakeRecordDao()
        repository = RecordRepositoryImpl(fakeDao)
    }

    @Test
    fun insertAndGetRecord() = runTest {
        val record = RecordEntity(id = 1, title = "Test Record", description = "Description", category = "Test")
        val insertedId = repository.insertRecord(record)

        assertEquals(1L, insertedId)
        val fetchedRecord = repository.getRecordById(1)
        assertEquals(record, fetchedRecord)
    }

    @Test
    fun getAllRecordsReturnsFlow() = runTest {
        val record1 = RecordEntity(id = 1, title = "Record 1", description = "Desc 1")
        val record2 = RecordEntity(id = 2, title = "Record 2", description = "Desc 2")

        repository.insertRecord(record1)
        repository.insertRecord(record2)

        val records = repository.getAllRecords().first()
        assertEquals(2, records.size)
    }

    @Test
    fun updateRecordUpdatesData() = runTest {
        val record = RecordEntity(id = 1, title = "Original Title", description = "Desc")
        repository.insertRecord(record)

        val updatedRecord = record.copy(title = "Updated Title")
        repository.updateRecord(updatedRecord)

        val fetched = repository.getRecordById(1)
        assertEquals("Updated Title", fetched?.title)
    }

    @Test
    fun deleteRecordRemovesData() = runTest {
        val record = RecordEntity(id = 1, title = "To Delete", description = "Desc")
        repository.insertRecord(record)

        repository.deleteRecord(record)

        val fetched = repository.getRecordById(1)
        assertNull(fetched)
    }

    private class FakeRecordDao : RecordDAO {
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
}
