package com.pobvol.pobvolchecklists.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pobvol.pobvolchecklists.data.local.CategoryEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistAnswerEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistQuestionEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistSubmissionEntity
import com.pobvol.pobvolchecklists.data.local.LanguageEntity
import com.pobvol.pobvolchecklists.data.repository.CategoryRepository
import com.pobvol.pobvolchecklists.data.repository.ChecklistAnswerRepository
import com.pobvol.pobvolchecklists.data.repository.ChecklistQuestionRepository
import com.pobvol.pobvolchecklists.data.repository.ChecklistRepository
import com.pobvol.pobvolchecklists.data.repository.ChecklistSubmissionRepository
import com.pobvol.pobvolchecklists.data.repository.LanguageRepository
import com.pobvol.pobvolchecklists.data.repository.SettingsRepository
import com.pobvol.pobvolchecklists.data.repository.ThemeMode
import com.pobvol.pobvolchecklists.data.repository.UserSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChecklistViewModel(
    private val repository: ChecklistRepository,
    private val questionRepository: ChecklistQuestionRepository,
    private val languageRepository: LanguageRepository,
    private val settingsRepository: SettingsRepository,
    private val categoryRepository: CategoryRepository,
    private val submissionRepository: ChecklistSubmissionRepository,
    private val answerRepository: ChecklistAnswerRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedSubmissionStatus = MutableStateFlow<String?>(null)
    val selectedSubmissionStatus: StateFlow<String?> = _selectedSubmissionStatus.asStateFlow()

    private val _scannedNfcTag = MutableStateFlow<String?>(null)
    val scannedNfcTag: StateFlow<String?> = _scannedNfcTag.asStateFlow()

    fun onNfcTagScanned(tagId: String) {
        _scannedNfcTag.value = tagId
        _userMessage.value = "NFC Tag scanned: $tagId"
    }

    private val _checklistToEdit = MutableStateFlow<ChecklistEntity?>(null)
    val checklistToEdit: StateFlow<ChecklistEntity?> = _checklistToEdit.asStateFlow()

    private val _isAddEditDialogVisible = MutableStateFlow(false)
    val isAddEditDialogVisible: StateFlow<Boolean> = _isAddEditDialogVisible.asStateFlow()

    private val _checklistToDelete = MutableStateFlow<ChecklistEntity?>(null)
    val checklistToDelete: StateFlow<ChecklistEntity?> = _checklistToDelete.asStateFlow()

    private val _selectedChecklistForQuestions = MutableStateFlow<ChecklistEntity?>(null)
    val selectedChecklistForQuestions: StateFlow<ChecklistEntity?> = _selectedChecklistForQuestions.asStateFlow()

    private val _selectedChecklistToAnswer = MutableStateFlow<ChecklistEntity?>(null)
    val selectedChecklistToAnswer: StateFlow<ChecklistEntity?> = _selectedChecklistToAnswer.asStateFlow()

    private val _isChecklistListVisible = MutableStateFlow(false)
    val isChecklistListVisible: StateFlow<Boolean> = _isChecklistListVisible.asStateFlow()

    private val _isSubmissionsOverviewVisible = MutableStateFlow(false)
    val isSubmissionsOverviewVisible: StateFlow<Boolean> = _isSubmissionsOverviewVisible.asStateFlow()

    private val _selectedChecklistForSubmissions = MutableStateFlow<ChecklistEntity?>(null)
    val selectedChecklistForSubmissions: StateFlow<ChecklistEntity?> = _selectedChecklistForSubmissions.asStateFlow()

    private val _selectedSubmissionToEdit = MutableStateFlow<ChecklistSubmissionEntity?>(null)
    val selectedSubmissionToEdit: StateFlow<ChecklistSubmissionEntity?> = _selectedSubmissionToEdit.asStateFlow()

    private val _submissionToDelete = MutableStateFlow<ChecklistSubmissionEntity?>(null)
    val submissionToDelete: StateFlow<ChecklistSubmissionEntity?> = _submissionToDelete.asStateFlow()

    private val _questions = MutableStateFlow<List<ChecklistQuestionEntity>>(emptyList())

    private val _isAddEditQuestionDialogVisible = MutableStateFlow(false)
    val isAddEditQuestionDialogVisible: StateFlow<Boolean> = _isAddEditQuestionDialogVisible.asStateFlow()

    private val _questionToEdit = MutableStateFlow<ChecklistQuestionEntity?>(null)
    val questionToEdit: StateFlow<ChecklistQuestionEntity?> = _questionToEdit.asStateFlow()

    private val _questionToDelete = MutableStateFlow<ChecklistQuestionEntity?>(null)
    val questionToDelete: StateFlow<ChecklistQuestionEntity?> = _questionToDelete.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _isSettingsDialogVisible = MutableStateFlow(false)
    val isSettingsDialogVisible: StateFlow<Boolean> = _isSettingsDialogVisible.asStateFlow()

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
        val selectedChecklistToAnswer: ChecklistEntity?,
        val submissionToDelete: ChecklistSubmissionEntity?,
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
        _selectedChecklistToAnswer,
        _submissionToDelete,
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
            selectedChecklistToAnswer = states[9] as ChecklistEntity?,
            submissionToDelete = states[10] as ChecklistSubmissionEntity?,
        )
    }

    private data class SettingsState(
        val settings: UserSettings,
        val languages: List<LanguageEntity>,
        val categories: List<CategoryEntity>,
        val isVisible: Boolean,
    )

    private val _settingsState = combine(
        settingsRepository.userSettings,
        languageRepository.getAllLanguages(),
        categoryRepository.getAllCategories(),
        _isSettingsDialogVisible,
    ) { settings, languages, categories, isVisible ->
        SettingsState(settings, languages, categories, isVisible)
    }

    private data class SubmissionState(
        val submissions: List<ChecklistSubmissionEntity>,
        val answers: List<ChecklistAnswerEntity>,
        val isOverviewVisible: Boolean,
        val selectedSubmissionToEdit: ChecklistSubmissionEntity?,
        val isChecklistListVisible: Boolean,
    )

    private val _submissionState = combine(
        submissionRepository.getAllChecklistSubmissions(),
        answerRepository.getAllChecklistAnswers(),
        _isSubmissionsOverviewVisible,
        _selectedSubmissionToEdit,
        _isChecklistListVisible,
    ) { submissions, answers, isOverviewVisible, selectedToEdit, isChecklistListVisible ->
        SubmissionState(
            submissions,
            answers,
            isOverviewVisible,
            selectedToEdit,
            isChecklistListVisible,
        )
    }

    private data class GlobalState(
        val settingsState: SettingsState,
        val submissionState: SubmissionState,
    )

    private val _globalState = combine(_settingsState, _submissionState) { settings, submissions ->
        GlobalState(settings, submissions)
    }

    val uiState: StateFlow<ChecklistUiState> = combine(
        repository.getAllChecklists(),
        _searchQuery,
        _selectedCategory,
        _dialogState,
        _globalState,
    ) { checklists, query, category, dialogState, globalState ->
        val selectedStatus = _selectedSubmissionStatus.value
        val filteredChecklists = checklists.filter { checklist ->
            val matchesQuery = query.isBlank() ||
                    checklist.title.contains(query, ignoreCase = true) ||
                    checklist.description!!.contains(query, ignoreCase = true) ||
                    checklist.category.contains(query, ignoreCase = true)

            val matchesCategory = category == null || category.equals("All", ignoreCase = true) ||
                    checklist.category.equals(category, ignoreCase = true) ||
                    globalState.settingsState.categories.any { cat ->
                        (cat.title.equals(category, ignoreCase = true) || cat.category.equals(category, ignoreCase = true)) &&
                                (checklist.category.equals(cat.category, ignoreCase = true) || checklist.category.equals(cat.title, ignoreCase = true))
                    }

            matchesQuery && matchesCategory
        }

        val selectedChecklistSub = _selectedChecklistForSubmissions.value
        val checklistMap = checklists.associateBy { it.id }
        val filteredSubmissions = globalState.submissionState.submissions.filter { submission ->
            val matchesChecklist = selectedChecklistSub == null || submission.checklistid == selectedChecklistSub.id

            val matchesSearch = query.isBlank() ||
                    submission.inspector?.contains(query, ignoreCase = true) == true ||
                    submission.notes?.contains(query, ignoreCase = true) == true ||
                    submission.date.contains(query, ignoreCase = true) == true ||
                    (checklistMap[submission.checklistid]?.title?.contains(query, ignoreCase = true) == true)

            val matchesStatus = selectedStatus == null || selectedStatus.equals("All", ignoreCase = true) ||
                    submission.status.equals(selectedStatus, ignoreCase = true)

            matchesChecklist && matchesSearch && matchesStatus
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
            userSettings = globalState.settingsState.settings,
            availableLanguages = globalState.settingsState.languages,
            isSettingsDialogVisible = globalState.settingsState.isVisible,
            categories = globalState.settingsState.categories,
            selectedChecklistToAnswer = dialogState.selectedChecklistToAnswer,
            submissions = filteredSubmissions,
            answers = globalState.submissionState.answers,
            isSubmissionsOverviewVisible = globalState.submissionState.isOverviewVisible,
            selectedSubmissionToEdit = globalState.submissionState.selectedSubmissionToEdit,
            submissionToDelete = dialogState.submissionToDelete,
            isChecklistListVisible = globalState.submissionState.isChecklistListVisible,
            selectedSubmissionStatus = selectedStatus,
            scannedNfcTag = _scannedNfcTag.value,
            selectedChecklistForSubmissions = selectedChecklistSub,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChecklistUiState(),
    )

    fun onSubmissionStatusFilterChange(status: String?) {
        _selectedSubmissionStatus.value = status
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategoryFilterChange(category: String?) {
        _selectedCategory.value = category
    }

    fun openChecklistsScreen() {
        _isChecklistListVisible.value = true
    }

    fun closeChecklistsScreen() {
        _isChecklistListVisible.value = false
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
            categoryRepository.insertCategory(CategoryEntity(category = trimmedCategory, title = trimmedCategory))

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

    fun addCategory(categoryName: String) {
        val trimmed = categoryName.trim()
        if (trimmed.isNotBlank()) {
            viewModelScope.launch {
                categoryRepository.insertCategory(CategoryEntity(category = trimmed, title = trimmed))
                _userMessage.value = "Category added successfully"
            }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
            _userMessage.value = "Category deleted successfully"
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
            val questions = questionRepository.getChecklistQuestionsByChecklistId(checklist.id).first()
            questions.forEach { question ->
                questionRepository.deleteChecklistQuestion(question)
            }
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

    fun openFillChecklist(checklist: ChecklistEntity) {
        _selectedChecklistToAnswer.value = checklist
        questionsJob?.cancel()
        questionsJob = viewModelScope.launch {
            questionRepository.getChecklistQuestionsByChecklistId(checklist.id).collect { questionsList ->
                _questions.value = questionsList
            }
        }
    }

    fun closeFillChecklist() {
        questionsJob?.cancel()
        _selectedChecklistToAnswer.value = null
        _questions.value = emptyList()
    }

    fun submitChecklistAnswers(
        checklistId: Int,
        inspector: String?,
        notes: String?,
        date: String = "",
        answers: Map<Int, String>
    ) {
        viewModelScope.launch {
            val formattedDate = date.ifBlank {
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            }

            val submission = ChecklistSubmissionEntity(
                checklistid = checklistId,
                status = "COMPLETED",
                inspector = inspector?.ifBlank { null },
                notes = notes?.ifBlank { null },
                date = formattedDate,
                timestamp = System.currentTimeMillis(),
            )

            val submissionId = submissionRepository.insertChecklistSubmission(submission)

            answers.forEach { (questionId, value) ->
                if (value.isNotBlank()) {
                    val answer = ChecklistAnswerEntity(
                        submissionid = submissionId.toInt(),
                        questionid = questionId,
                        value = value,
                        timestamp = System.currentTimeMillis(),
                    )
                    answerRepository.insertChecklistAnswer(answer)
                }
            }

            _userMessage.value = "Checklist submitted successfully"
            closeFillChecklist()
        }
    }

    fun openSubmissionsOverview() {
        _isSubmissionsOverviewVisible.value = true
    }

    fun openSubmissionsForChecklist(checklist: ChecklistEntity) {
        _selectedChecklistForSubmissions.value = checklist
        _isSubmissionsOverviewVisible.value = true
    }

    fun clearChecklistSubmissionsFilter() {
        _selectedChecklistForSubmissions.value = null
    }

    fun closeSubmissionsOverview() {
        _isSubmissionsOverviewVisible.value = false
        _selectedChecklistForSubmissions.value = null
    }

    fun openEditSubmission(submission: ChecklistSubmissionEntity) {
        _selectedSubmissionToEdit.value = submission
        questionsJob?.cancel()
        questionsJob = viewModelScope.launch {
            questionRepository.getChecklistQuestionsByChecklistId(submission.checklistid).collect { questionsList ->
                _questions.value = questionsList
            }
        }
    }

    fun closeEditSubmission() {
        questionsJob?.cancel()
        _selectedSubmissionToEdit.value = null
        _questions.value = emptyList()
    }

    fun updateSubmissionAndAnswers(
        updatedSubmission: ChecklistSubmissionEntity,
        updatedAnswersMap: Map<Int, String>,
    ) {
        viewModelScope.launch {
            submissionRepository.updateChecklistSubmission(updatedSubmission)

            val existingAnswers = answerRepository.getChecklistAnswersListBySubmissionId(updatedSubmission.id)
            val existingAnswerByQuestionMap = existingAnswers.associateBy { it.questionid }

            updatedAnswersMap.forEach { (questionId, newAnswerValue) ->
                val existingAnswer = existingAnswerByQuestionMap[questionId]
                if (existingAnswer != null) {
                    if (newAnswerValue != existingAnswer.value) {
                        val modifiedAnswer = existingAnswer.copy(
                            value = newAnswerValue,
                            timestamp = System.currentTimeMillis(),
                        )
                        answerRepository.updateChecklistAnswer(modifiedAnswer)
                    }
                } else if (newAnswerValue.isNotBlank()) {
                    val newAnswer = ChecklistAnswerEntity(
                        submissionid = updatedSubmission.id,
                        questionid = questionId,
                        value = newAnswerValue,
                        timestamp = System.currentTimeMillis(),
                    )
                    answerRepository.insertChecklistAnswer(newAnswer)
                }
            }

            _userMessage.value = "Submission updated successfully"
            closeEditSubmission()
        }
    }

    fun requestDeleteSubmission(submission: ChecklistSubmissionEntity) {
        _submissionToDelete.value = submission
    }

    fun dismissDeleteSubmission() {
        _submissionToDelete.value = null
    }

    fun confirmDeleteSubmission() {
        val submission = _submissionToDelete.value ?: return
        viewModelScope.launch {
            submissionRepository.deleteChecklistSubmission(submission)
            _submissionToDelete.value = null
            _userMessage.value = "Submission deleted successfully"
        }
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

    fun saveQuestion(
        title: String,
        description: String,
        type: String,
        options: String,
        required: Boolean,
        sortno: Int = 0,
    ) {
        val checklistId = _selectedChecklistForQuestions.value?.id ?: return
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) return

        val currentEdit = _questionToEdit.value
        viewModelScope.launch {
            if (currentEdit == null) {
                val currentQuestions = _questions.value.filter { it.checklistid == checklistId }
                val autoSortNo = if (sortno > 0) sortno else ((currentQuestions.maxOfOrNull { it.sortno } ?: 0) + 1)
                val newQuestion = ChecklistQuestionEntity(
                    checklistid = checklistId,
                    sortno = autoSortNo,
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
                val autoSortNo = if (sortno > 0) sortno else currentEdit.sortno
                val updatedQuestion = currentEdit.copy(
                    sortno = autoSortNo,
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

    fun moveQuestionUp(question: ChecklistQuestionEntity) {
        val currentQuestions = _questions.value
            .filter { it.checklistid == question.checklistid }
            .sortedWith(compareBy({ it.sortno }, { it.id }))

        val index = currentQuestions.indexOfFirst { it.id == question.id }
        if (index > 0) {
            val newList = currentQuestions.toMutableList()
            val temp = newList[index]
            newList[index] = newList[index - 1]
            newList[index - 1] = temp

            viewModelScope.launch {
                newList.forEachIndexed { idx, q ->
                    val newSortNo = idx + 1
                    if (q.sortno != newSortNo) {
                        questionRepository.updateChecklistQuestion(q.copy(sortno = newSortNo))
                    }
                }
            }
        }
    }

    fun moveQuestionDown(question: ChecklistQuestionEntity) {
        val currentQuestions = _questions.value
            .filter { it.checklistid == question.checklistid }
            .sortedWith(compareBy({ it.sortno }, { it.id }))

        val index = currentQuestions.indexOfFirst { it.id == question.id }
        if (index >= 0 && index < currentQuestions.size - 1) {
            val newList = currentQuestions.toMutableList()
            val temp = newList[index]
            newList[index] = newList[index + 1]
            newList[index + 1] = temp

            viewModelScope.launch {
                newList.forEachIndexed { idx, q ->
                    val newSortNo = idx + 1
                    if (q.sortno != newSortNo) {
                        questionRepository.updateChecklistQuestion(q.copy(sortno = newSortNo))
                    }
                }
            }
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

    fun openSettingsDialog() {
        _isSettingsDialogVisible.value = true
    }

    fun dismissSettingsDialog() {
        _isSettingsDialogVisible.value = false
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(themeMode)
        }
    }

    fun updatePreferredLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.updatePreferredLanguage(language)
        }
    }

    fun updateUserName(userName: String) {
        viewModelScope.launch {
            settingsRepository.updateUserName(userName)
        }
    }

    fun userMessageShown() {
        _userMessage.value = null
    }

    companion object {
        fun Factory(
            repository: ChecklistRepository,
            questionRepository: ChecklistQuestionRepository,
            languageRepository: LanguageRepository,
            settingsRepository: SettingsRepository,
            categoryRepository: CategoryRepository,
            submissionRepository: ChecklistSubmissionRepository,
            answerRepository: ChecklistAnswerRepository,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChecklistViewModel(
                        repository,
                        questionRepository,
                        languageRepository,
                        settingsRepository,
                        categoryRepository,
                        submissionRepository,
                        answerRepository,
                    ) as T
                }
            }
    }
}
