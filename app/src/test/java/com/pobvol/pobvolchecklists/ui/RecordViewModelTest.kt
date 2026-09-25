package com.pobvol.pobvolchecklists.ui

import com.pobvol.pobvolchecklists.data.local.RecordDao
import com.pobvol.pobvolchecklists.data.local.RecordEntity
import com.pobvol.pobvolchecklists.data.repository.RecordRepository
import com.pobvol.pobvolchecklists.data.repository.RecordRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeDao: FakeRecordDao
    private lateinit var repository: RecordRepository
    private lateinit var viewModel: RecordViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeRecordDao()
        repository = RecordRepositoryImpl(fakeDao)
        viewModel = RecordViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addRecordInsertsToRepositoryAndUpdatesState() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.openAddDialog()
        viewModel.saveRecord("Title 1", "Desc 1", "Work")

        val records = repository.getAllRecords().first()
        assertEquals(1, records.size)
        assertEquals("Title 1", records[0].title)
        assertEquals("Desc 1", records[0].description)
        assertEquals("Work", records[0].category)

        val uiState = viewModel.uiState.value
        assertEquals("Record added successfully", uiState.userMessage)
        assertFalse(uiState.isAddEditDialogVisible)
    }

    @Test
    fun editRecordUpdatesRepository() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val original = RecordEntity(id = 1, title = "Original", description = "Old Desc", category = "Personal")
        repository.insertRecord(original)

        viewModel.openEditDialog(original)
        viewModel.saveRecord("Updated Title", "New Desc", "Personal")

        val fetched = repository.getRecordById(1)
        assertNotNull(fetched)
        assertEquals("Updated Title", fetched?.title)
        assertEquals("New Desc", fetched?.description)

        val uiState = viewModel.uiState.value
        assertEquals("Record updated successfully", uiState.userMessage)
    }

    @Test
    fun deleteRecordRemovesFromRepository() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val record = RecordEntity(id = 1, title = "To Delete", description = "Desc", category = "General")
        repository.insertRecord(record)

        viewModel.requestDeleteConfirmation(record)
        assertEquals(record, viewModel.uiState.value.recordToDelete)

        viewModel.confirmDelete()

        val fetched = repository.getRecordById(1)
        assertNull(fetched)
        assertNull(viewModel.uiState.value.recordToDelete)
        assertEquals("Record deleted successfully", viewModel.uiState.value.userMessage)
    }

    @Test
    fun searchQueryFiltersRecords() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        repository.insertRecord(RecordEntity(id = 1, title = "Apple", description = "Fruit", category = "Food"))
        repository.insertRecord(RecordEntity(id = 2, title = "Banana", description = "Yellow Fruit", category = "Food"))
        repository.insertRecord(RecordEntity(id = 3, title = "Carrot", description = "Vegetable", category = "Food"))

        viewModel.onSearchQueryChange("Banana")

        val filtered = viewModel.uiState.value.records
        assertEquals(1, filtered.size)
        assertEquals("Banana", filtered[0].title)
    }

    @Test
    fun categoryFilterFiltersRecords() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        repository.insertRecord(RecordEntity(id = 1, title = "Task 1", description = "Work task", category = "Work"))
        repository.insertRecord(RecordEntity(id = 2, title = "Task 2", description = "Personal task", category = "Personal"))

        viewModel.onCategoryFilterChange("Work")

        val filtered = viewModel.uiState.value.records
        assertEquals(1, filtered.size)
        assertEquals("Task 1", filtered[0].title)
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
}
