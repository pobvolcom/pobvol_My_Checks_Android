package com.pobvol.pobvolchecklists.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pobvol.pobvolchecklists.data.local.ChecklistEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistQuestionEntity
import com.pobvol.pobvolchecklists.data.repository.ChecklistQuestionRepository
import com.pobvol.pobvolchecklists.data.repository.ChecklistRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChecklistViewModel(
    private val repository: ChecklistRepository,
    private val questionRepository: ChecklistQuestionRepository,
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

    private val _selectedChecklistForQuestions = MutableStateFlow<ChecklistEntity?>(null)
    val selectedChecklistForQuestions: StateFlow<ChecklistEntity?> = _selectedChecklistForQuestions.asStateFlow()

    private val _questions = MutableStateFlow<List<ChecklistQuestionEntity>>(emptyList())

    private val _isAddEditQuestionDialogVisible = MutableStateFlow(false)
    val isAddEditQuestionDialogVisible: StateFlow<Boolean> = _isAddEditQuestionDialogVisible.asStateFlow()

    private val _questionToEdit = MutableStateFlow<ChecklistQuestionEntity?>(null)
    val questionToEdit: StateFlow<ChecklistQuestionEntity?> = _questionToEdit.asStateFlow()

    private val _questionToDelete = MutableStateFlow<ChecklistQuestionEntity?>(null)
    val questionToDelete: StateFlow<ChecklistQuestionEntity?> = _questionToDelete.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private var questionsJob: Job? = null

    private data class DialogState(
        val isAddEditVisible: Boolean,
        val checklistToEdit: ChecklistEntity?,
        val checklistToDelete: ChecklistEntity?,
        val selectedChecklistForQuestions: ChecklistEntity?,
        val questions: List<ChecklistQuestionEntity>,
        val isAddEditQuestionVisible: Boolean,
        val questionToEdit: ChecklistQuestionEntity?,
        val questionToDelete: ChecklistQuestionEntity?,
        val message: String?,
    )

    private val _dialogState = combine(
        _isAddEditDialogVisible,
        _checklistToEdit,
        _checklistToDelete,
        _selectedChecklistForQuestions,
        _questions,
        _isAddEditQuestionDialogVisible,
        _questionToEdit,
        _questionToDelete,
        _userMessage,
    ) { states ->
        DialogState(
            isAddEditVisible = states[0] as Boolean,
            checklistToEdit = states[1] as ChecklistEntity?,
            checklistToDelete = states[2] as ChecklistEntity?,
            selectedChecklistForQuestions = states[3] as ChecklistEntity?,
            questions = states[4] as List<ChecklistQuestionEntity>,
            isAddEditQuestionVisible = states[5] as Boolean,
            questionToEdit = states[6] as ChecklistQuestionEntity?,
            questionToDelete = states[7] as ChecklistQuestionEntity?,
            message = states[8] as String?,
        )
    }

    val uiState: StateFlow<ChecklistUiState> = combine(
        repository.getAllChecklists(),
        _searchQuery,
        _selectedCategory,
        _dialogState,
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
            selectedChecklistForQuestions = dialogState.selectedChecklistForQuestions,
            questionsForSelectedChecklist = dialogState.questions,
            isAddEditQuestionDialogVisible = dialogState.isAddEditQuestionVisible,
            questionToEdit = dialogState.questionToEdit,
            questionToDelete = dialogState.questionToDelete,
            userMessage = dialogState.message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChecklistUiState(),
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
                    timestamp = System.currentTimeMillis(),
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
                    timestamp = System.currentTimeMillis(),
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

    fun openQuestionsForChecklist(checklist: ChecklistEntity) {
        _selectedChecklistForQuestions.value = checklist
        questionsJob?.cancel()
        questionsJob = viewModelScope.launch {
            questionRepository.getChecklistQuestionsByChecklistId(checklist.id).collect { questionsList ->
                _questions.value = questionsList
            }
        }
    }

    fun closeQuestionsScreen() {
        questionsJob?.cancel()
        _selectedChecklistForQuestions.value = null
        _questions.value = emptyList()
    }

    fun openAddQuestionDialog() {
        _questionToEdit.value = null
        _isAddEditQuestionDialogVisible.value = true
    }

    fun openEditQuestionDialog(question: ChecklistQuestionEntity) {
        _questionToEdit.value = question
        _isAddEditQuestionDialogVisible.value = true
    }

    fun dismissAddEditQuestionDialog() {
        _isAddEditQuestionDialogVisible.value = false
        _questionToEdit.value = null
    }

    fun saveQuestion(title: String, description: String, type: String, options: String, required: Boolean) {
        val checklistId = _selectedChecklistForQuestions.value?.id ?: return
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) return

        val currentEdit = _questionToEdit.value
        viewModelScope.launch {
            if (currentEdit == null) {
                val newQuestion = ChecklistQuestionEntity(
                    checklistid = checklistId,
                    title = trimmedTitle,
                    description = description.trim(),
                    type = type,
                    options = if (type == "combobox") options.trim() else null,
                    required = required,
                    timestamp = System.currentTimeMillis(),
                )
                questionRepository.insertChecklistQuestion(newQuestion)
                _userMessage.value = "Question added successfully"
            } else {
                val updatedQuestion = currentEdit.copy(
                    title = trimmedTitle,
                    description = description.trim(),
                    type = type,
                    options = if (type == "combobox") options.trim() else null,
                    required = required,
                    timestamp = System.currentTimeMillis(),
                )
                questionRepository.updateChecklistQuestion(updatedQuestion)
                _userMessage.value = "Question updated successfully"
            }
            dismissAddEditQuestionDialog()
        }
    }

    fun requestDeleteQuestionConfirmation(question: ChecklistQuestionEntity) {
        _questionToDelete.value = question
    }

    fun dismissDeleteQuestionConfirmation() {
        _questionToDelete.value = null
    }

    fun confirmDeleteQuestion() {
        val question = _questionToDelete.value ?: return
        viewModelScope.launch {
            questionRepository.deleteChecklistQuestion(question)
            _questionToDelete.value = null
            _userMessage.value = "Question deleted successfully"
        }
    }

    fun userMessageShown() {
        _userMessage.value = null
    }

    companion object {
        fun Factory(
            repository: ChecklistRepository,
            questionRepository: ChecklistQuestionRepository,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChecklistViewModel(repository, questionRepository) as T
                }
            }
    }
}
