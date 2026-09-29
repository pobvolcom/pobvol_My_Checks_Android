package com.pobvol.pobvolchecklists.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pobvol.pobvolchecklists.data.local.ChecklistEntity
import com.pobvol.pobvolchecklists.data.repository.ChecklistRepository
import com.pobvol.pobvolchecklists.data.repository.DataRepository
import com.pobvol.pobvolchecklists.data.repository.DataRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChecklistViewModel(
    /* private val repository: ChecklistRepository */
    private val repository: DataRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _checklistToEdit = MutableStateFlow<ChecklistEntity?>(null)
    val checklistToEdit: StateFlow<ChecklistEntity?> = _checklistToEdit.asStateFlow()

    private val _isAddEditDialogVisible = MutableStateFlow(false)
    val isAddEditDialogVisible: StateFlow<Boolean> = _isAddEditDialogVisible.asStateFlow()

    private val _checklistToDelete = MutableStateFlow<ChecklistEntity?>(null)
    val checklistToDelete: StateFlow<ChecklistEntity?> = _checklistToDelete.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private data class DialogState(
        val isAddEditVisible: Boolean,
        val checklistToEdit: ChecklistEntity?,
        val checklistToDelete: ChecklistEntity?,
        val message: String?
    )

    private val _dialogState = combine(
        _isAddEditDialogVisible,
        _checklistToEdit,
        _checklistToDelete,
        _userMessage
    ) { isAddEditVisible, checklistToEdit, checklistToDelete, message ->
        DialogState(isAddEditVisible, checklistToEdit, checklistToDelete, message)
    }

    val uiState: StateFlow<ChecklistUiState> = combine(
        repository.getAllChecklists(),
        _searchQuery,
        _selectedCategory,
        _dialogState
    ) { checklists, query, category, dialogState ->
        val filteredChecklists = checklists.filter { checklist ->
            val matchesQuery = query.isBlank() ||
                    checklist.title.contains(query, ignoreCase = true) ||
                    checklist.description!!.contains(query, ignoreCase = true) ||
                    checklist.category.contains(query, ignoreCase = true)

            val matchesCategory = category == null || category.equals("All", ignoreCase = true) ||
                    checklist.category.equals(category, ignoreCase = true)

            matchesQuery && matchesCategory
        }

        ChecklistUiState(
            checklists = filteredChecklists,
            searchQuery = query,
            selectedCategory = category,
            isAddEditDialogVisible = dialogState.isAddEditVisible,
            checklistToEdit = dialogState.checklistToEdit,
            checklistToDelete = dialogState.checklistToDelete,
            userMessage = dialogState.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChecklistUiState()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategoryFilterChange(category: String?) {
        _selectedCategory.value = category
    }

    fun openAddDialog() {
        _checklistToEdit.value = null
        _isAddEditDialogVisible.value = true
    }

    fun openEditDialog(checklist: ChecklistEntity) {
        _checklistToEdit.value = checklist
        _isAddEditDialogVisible.value = true
    }

    fun dismissAddEditDialog() {
        _isAddEditDialogVisible.value = false
        _checklistToEdit.value = null
    }

    fun saveChecklist(title: String, language: String, description: String, category: String, icon: String) {
        val trimmedTitle = title.trim()
        val trimmedLanguage = language.trim()
        val trimmedCategory = if (category.isBlank()) "General" else category.trim()
        val trimmedDesc = description.trim()
        val trimmedIcon = icon.trim()

        if (trimmedTitle.isBlank()) return

        val currentEdit = _checklistToEdit.value
        viewModelScope.launch {
            if (currentEdit == null) {
                val newChecklist = ChecklistEntity(
                    title = trimmedTitle,
                    language = trimmedLanguage,
                    description = trimmedDesc,
                    category = trimmedCategory,
                    icon = trimmedIcon,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertChecklist(newChecklist)
                _userMessage.value = "Checklist added successfully"
            } else {
                val updatedChecklist = currentEdit.copy(
                    title = trimmedTitle,
                    language = trimmedLanguage,
                    description = trimmedDesc,
                    category = trimmedCategory,
                    icon = trimmedIcon,
                    timestamp = System.currentTimeMillis()
                )
                repository.updateChecklist(updatedChecklist)
                _userMessage.value = "Checklist updated successfully"
            }
            dismissAddEditDialog()
        }
    }

    fun requestDeleteConfirmation(checklist: ChecklistEntity) {
        _checklistToDelete.value = checklist
    }

    fun dismissDeleteConfirmation() {
        _checklistToDelete.value = null
    }

    fun confirmDelete() {
        val checklist = _checklistToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteChecklist(checklist)
            _checklistToDelete.value = null
            _userMessage.value = "Checklist deleted successfully"
        }
    }

    fun userMessageShown() {
        _userMessage.value = null
    }

    companion object {
        fun Factory(repository: DataRepositoryImpl): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChecklistViewModel(repository) as T
                }
            }
    }
}
