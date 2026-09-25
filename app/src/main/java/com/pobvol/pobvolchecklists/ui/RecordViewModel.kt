package com.pobvol.pobvolchecklists.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pobvol.pobvolchecklists.data.local.RecordEntity
import com.pobvol.pobvolchecklists.data.repository.RecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecordViewModel(
    private val repository: RecordRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _recordToEdit = MutableStateFlow<RecordEntity?>(null)
    val recordToEdit: StateFlow<RecordEntity?> = _recordToEdit.asStateFlow()

    private val _isAddEditDialogVisible = MutableStateFlow(false)
    val isAddEditDialogVisible: StateFlow<Boolean> = _isAddEditDialogVisible.asStateFlow()

    private val _recordToDelete = MutableStateFlow<RecordEntity?>(null)
    val recordToDelete: StateFlow<RecordEntity?> = _recordToDelete.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private data class DialogState(
        val isAddEditVisible: Boolean,
        val recordToEdit: RecordEntity?,
        val recordToDelete: RecordEntity?,
        val message: String?
    )

    private val _dialogState = combine(
        _isAddEditDialogVisible,
        _recordToEdit,
        _recordToDelete,
        _userMessage
    ) { isAddEditVisible, recordToEdit, recordToDelete, message ->
        DialogState(isAddEditVisible, recordToEdit, recordToDelete, message)
    }

    val uiState: StateFlow<RecordUiState> = combine(
        repository.getAllRecords(),
        _searchQuery,
        _selectedCategory,
        _dialogState
    ) { records, query, category, dialogState ->
        val filteredRecords = records.filter { record ->
            val matchesQuery = query.isBlank() ||
                    record.title.contains(query, ignoreCase = true) ||
                    record.description.contains(query, ignoreCase = true) ||
                    record.category.contains(query, ignoreCase = true)

            val matchesCategory = category == null || category.equals("All", ignoreCase = true) ||
                    record.category.equals(category, ignoreCase = true)

            matchesQuery && matchesCategory
        }

        RecordUiState(
            records = filteredRecords,
            searchQuery = query,
            selectedCategory = category,
            isAddEditDialogVisible = dialogState.isAddEditVisible,
            recordToEdit = dialogState.recordToEdit,
            recordToDelete = dialogState.recordToDelete,
            userMessage = dialogState.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecordUiState()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategoryFilterChange(category: String?) {
        _selectedCategory.value = category
    }

    fun openAddDialog() {
        _recordToEdit.value = null
        _isAddEditDialogVisible.value = true
    }

    fun openEditDialog(record: RecordEntity) {
        _recordToEdit.value = record
        _isAddEditDialogVisible.value = true
    }

    fun dismissAddEditDialog() {
        _isAddEditDialogVisible.value = false
        _recordToEdit.value = null
    }

    fun saveRecord(title: String, description: String, category: String) {
        val trimmedTitle = title.trim()
        val trimmedCategory = if (category.isBlank()) "General" else category.trim()
        val trimmedDesc = description.trim()

        if (trimmedTitle.isBlank()) return

        val currentEdit = _recordToEdit.value
        viewModelScope.launch {
            if (currentEdit == null) {
                val newRecord = RecordEntity(
                    title = trimmedTitle,
                    description = trimmedDesc,
                    category = trimmedCategory,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertRecord(newRecord)
                _userMessage.value = "Record added successfully"
            } else {
                val updatedRecord = currentEdit.copy(
                    title = trimmedTitle,
                    description = trimmedDesc,
                    category = trimmedCategory,
                    timestamp = System.currentTimeMillis()
                )
                repository.updateRecord(updatedRecord)
                _userMessage.value = "Record updated successfully"
            }
            dismissAddEditDialog()
        }
    }

    fun requestDeleteConfirmation(record: RecordEntity) {
        _recordToDelete.value = record
    }

    fun dismissDeleteConfirmation() {
        _recordToDelete.value = null
    }

    fun confirmDelete() {
        val record = _recordToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteRecord(record)
            _recordToDelete.value = null
            _userMessage.value = "Record deleted successfully"
        }
    }

    fun userMessageShown() {
        _userMessage.value = null
    }

    companion object {
        fun Factory(repository: RecordRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RecordViewModel(repository) as T
                }
            }
    }
}
