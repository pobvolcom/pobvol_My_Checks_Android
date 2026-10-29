package com.pobvol.mychecks.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pobvol.mychecks.R
import com.pobvol.mychecks.data.local.CategoryEntity
import com.pobvol.mychecks.data.local.ChecklistAnswerEntity
import com.pobvol.mychecks.data.local.ChecklistEntity
import com.pobvol.mychecks.data.local.ChecklistQuestionEntity
import com.pobvol.mychecks.data.local.ChecklistSubmissionEntity
import com.pobvol.mychecks.data.local.DeviceEntity
import com.pobvol.mychecks.data.local.LanguageEntity
import com.pobvol.mychecks.data.local.TextBlockEntity
import com.pobvol.mychecks.data.repository.CategoryRepository
import com.pobvol.mychecks.data.repository.ChecklistAnswerRepository
import com.pobvol.mychecks.data.repository.ChecklistQuestionRepository
import com.pobvol.mychecks.data.repository.ChecklistRepository
import com.pobvol.mychecks.data.repository.ChecklistSubmissionRepository
import com.pobvol.mychecks.data.repository.DeviceRepository
import com.pobvol.mychecks.data.repository.LanguageRepository
import com.pobvol.mychecks.data.repository.SettingsRepository
import com.pobvol.mychecks.data.repository.TextBlockRepository
import com.pobvol.mychecks.data.repository.ThemeMode
import com.pobvol.mychecks.data.repository.UserSettings
import com.pobvol.mychecks.util.PdfExporter
import com.pobvol.mychecks.util.SqlExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChecklistViewModel(
    private val repository: ChecklistRepository,
    private val questionRepository: ChecklistQuestionRepository,
    private val deviceRepository: DeviceRepository,
    private val languageRepository: LanguageRepository,
    private val settingsRepository: SettingsRepository,
    private val categoryRepository: CategoryRepository,
    private val submissionRepository: ChecklistSubmissionRepository,
    private val answerRepository: ChecklistAnswerRepository,
    private val textBlockRepository: TextBlockRepository,
    private val context: Context,
) : ViewModel() {

    init {
        viewModelScope.launch {
            try {
                val devices = deviceRepository.getDeviceId().first()
                val deviceModel = android.os.Build.MODEL ?: "Android Device"
                val userName = settingsRepository.userSettings.first().userName.ifBlank { "User" }
                val deviceNotes = "$deviceModel-$userName"

                if (devices.isEmpty()) {
                    deviceRepository.insertDevice(DeviceEntity(notes = deviceNotes))
                } else {
                    val existing = devices.first()
                    if (existing.notes != deviceNotes) {
                        deviceRepository.insertDevice(existing.copy(notes = deviceNotes))
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

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
        _userMessage.value = context.getString(R.string.msg_nfc_scanned, tagId)
    }

    fun clearNfcTag() {
        _scannedNfcTag.value = null
    }

    private val _checklistToEdit = MutableStateFlow<ChecklistEntity?>(null)
    val checklistToEdit: StateFlow<ChecklistEntity?> = _checklistToEdit.asStateFlow()

    private val _isAddEditDialogVisible = MutableStateFlow(false)
    val isAddEditDialogVisible: StateFlow<Boolean> = _isAddEditDialogVisible.asStateFlow()

    private val _checklistToDelete = MutableStateFlow<ChecklistEntity?>(null)
    val checklistToDelete: StateFlow<ChecklistEntity?> = _checklistToDelete.asStateFlow()

    private val _checklistToCopy = MutableStateFlow<ChecklistEntity?>(null)
    val checklistToCopy: StateFlow<ChecklistEntity?> = _checklistToCopy.asStateFlow()

    private val _selectedChecklistForQuestions = MutableStateFlow<ChecklistEntity?>(null)
    val selectedChecklistForQuestions: StateFlow<ChecklistEntity?> = _selectedChecklistForQuestions.asStateFlow()

    private val _selectedChecklistToAnswer = MutableStateFlow<ChecklistEntity?>(null)
    val selectedChecklistToAnswer: StateFlow<ChecklistEntity?> = _selectedChecklistToAnswer.asStateFlow()

    private val _isChecklistListVisible = MutableStateFlow(false)
    val isChecklistListVisible: StateFlow<Boolean> = _isChecklistListVisible.asStateFlow()

    private val _isHelpVisible = MutableStateFlow(false)
    val isHelpVisible: StateFlow<Boolean> = _isHelpVisible.asStateFlow()

    private val _isChecklistHelpVisible = MutableStateFlow(false)
    val isChecklistHelpVisible: StateFlow<Boolean> = _isChecklistHelpVisible.asStateFlow()

    private val _isSubmissionsOverviewVisible = MutableStateFlow(false)
    val isSubmissionsOverviewVisible: StateFlow<Boolean> = _isSubmissionsOverviewVisible.asStateFlow()

    private val _selectedChecklistForSubmissions = MutableStateFlow<ChecklistEntity?>(null)
    val selectedChecklistForSubmissions: StateFlow<ChecklistEntity?> = _selectedChecklistForSubmissions.asStateFlow()

    private val _submissionLimit = MutableStateFlow(30)
    val submissionLimit: StateFlow<Int> = _submissionLimit.asStateFlow()

    private val _selectedChecklistForChart = MutableStateFlow<ChecklistEntity?>(null)
    val selectedChecklistForChart: StateFlow<ChecklistEntity?> =
        _selectedChecklistForChart.asStateFlow()

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

    private val _isPdfSettingsVisible = MutableStateFlow(false)
    val isPdfSettingsVisible: StateFlow<Boolean> = _isPdfSettingsVisible.asStateFlow()

    private val _isLicenseScreenVisible = MutableStateFlow(false)
    val isLicenseScreenVisible: StateFlow<Boolean> = _isLicenseScreenVisible.asStateFlow()

    private val _isTextBlocksVisible = MutableStateFlow(false)
    val isTextBlocksVisible: StateFlow<Boolean> = _isTextBlocksVisible.asStateFlow()

    private val _isAddEditTextBlockDialogVisible = MutableStateFlow(false)
    val isAddEditTextBlockDialogVisible: StateFlow<Boolean> =
        _isAddEditTextBlockDialogVisible.asStateFlow()

    private val _textBlockToEdit = MutableStateFlow<TextBlockEntity?>(null)
    val textBlockToEdit: StateFlow<TextBlockEntity?> = _textBlockToEdit.asStateFlow()

    private val _textBlockToDelete = MutableStateFlow<TextBlockEntity?>(null)
    val textBlockToDelete: StateFlow<TextBlockEntity?> = _textBlockToDelete.asStateFlow()

    val textBlocks: StateFlow<List<TextBlockEntity>> = textBlockRepository.getAllTextBlocks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var questionsJob: Job? = null

    private data class DialogState(
        val isAddEditVisible: Boolean,
        val checklistToEdit: ChecklistEntity?,
        val checklistToDelete: ChecklistEntity?,
        val checklistToCopy: ChecklistEntity?,
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
        _checklistToCopy,
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
            checklistToCopy = states[3] as ChecklistEntity?,
            selectedChecklistForQuestions = states[4] as ChecklistEntity?,
            questions = states[5] as List<ChecklistQuestionEntity>,
            isAddEditQuestionVisible = states[6] as Boolean,
            questionToEdit = states[7] as ChecklistQuestionEntity?,
            questionToDelete = states[8] as ChecklistQuestionEntity?,
            message = states[9] as String?,
            selectedChecklistToAnswer = states[10] as ChecklistEntity?,
            submissionToDelete = states[11] as ChecklistSubmissionEntity?,
        )
    }

    private data class SettingsState(
        val settings: UserSettings,
        val languages: List<LanguageEntity>,
        val categories: List<CategoryEntity>,
        val isVisible: Boolean,
        val isPdfVisible: Boolean,
        val devices: List<DeviceEntity>,
    )

    private val _settingsState = combine(
        combine(
            settingsRepository.userSettings,
            languageRepository.getAllLanguages(),
            categoryRepository.getAllCategories(),
            _isSettingsDialogVisible,
        ) { settings, languages, categories, isVisible ->
            Triple(settings, languages, Pair(categories, isVisible))
        },
        combine(
            _isPdfSettingsVisible,
            deviceRepository.getDeviceId(),
        ) { isPdfVisible, devices ->
            Pair(isPdfVisible, devices)
        }
    ) { t1, t2 ->
        SettingsState(
            settings = t1.first,
            languages = t1.second,
            categories = t1.third.first,
            isVisible = t1.third.second,
            isPdfVisible = t2.first,
            devices = t2.second,
        )
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
        val isHelpVisible: Boolean,
        val isChecklistHelpVisible: Boolean,
        val isLicenseScreenVisible: Boolean,
        val selectedSubmissionStatus: String?,
    )

    private val _globalState = combine(
        _settingsState,
        _submissionState,
        _isHelpVisible,
        _isChecklistHelpVisible,
        _isLicenseScreenVisible,
        _selectedSubmissionStatus,
    ) { flows ->
        val settings = flows[0] as SettingsState
        val submissions = flows[1] as SubmissionState
        val isHelpVisible = flows[2] as Boolean
        val isChecklistHelpVisible = flows[3] as Boolean
        val isLicenseScreenVisible = flows[4] as Boolean
        val selectedStatus = flows[5] as String?
        GlobalState(
            settings,
            submissions,
            isHelpVisible,
            isChecklistHelpVisible,
            isLicenseScreenVisible,
            selectedStatus
        )
    }

    private data class TextBlockState(
        val isVisible: Boolean,
        val isAddEditVisible: Boolean,
        val textBlockToEdit: TextBlockEntity?,
        val textBlockToDelete: TextBlockEntity?,
        val textBlocks: List<TextBlockEntity>,
    )

    private val _textBlockState = combine(
        _isTextBlocksVisible,
        _isAddEditTextBlockDialogVisible,
        _textBlockToEdit,
        _textBlockToDelete,
        textBlocks,
    ) { isVisible, isAddEditVisible, textBlockToEdit, textBlockToDelete, textBlocks ->
        TextBlockState(
            isVisible = isVisible,
            isAddEditVisible = isAddEditVisible,
            textBlockToEdit = textBlockToEdit,
            textBlockToDelete = textBlockToDelete,
            textBlocks = textBlocks,
        )
    }

    val uiState: StateFlow<ChecklistUiState> = combine(
        repository.getAllChecklists(),
        _searchQuery,
        _selectedCategory,
        _dialogState,
        _globalState,
        _textBlockState,
        _selectedChecklistForSubmissions,
        _submissionLimit,
    ) { flows ->
        val checklists = flows[0] as List<ChecklistEntity>
        val query = flows[1] as String
        val category = flows[2] as String?
        val dialogState = flows[3] as DialogState
        val globalState = flows[4] as GlobalState
        val textBlockState = flows[5] as TextBlockState
        val selectedChecklistSub = flows[6] as ChecklistEntity?
        val limit = flows[7] as Int
        val selectedStatus = globalState.selectedSubmissionStatus
        val preferredLang = globalState.settingsState.settings.preferredLanguage
        val checklistsInLang = checklists.filter { it.language.equals(preferredLang, ignoreCase = true) }
        val effectiveChecklists = if (checklistsInLang.isNotEmpty()) checklistsInLang else checklists

        val filteredChecklists = effectiveChecklists.filter { checklist ->
            val matchesQuery = query.isBlank() ||
                    checklist.title.contains(query, ignoreCase = true) ||
                    checklist.description.contains(query, ignoreCase = true) ||
                    checklist.category.contains(query, ignoreCase = true)

            val matchesCategory = category == null || category.equals("All", ignoreCase = true) ||
                    checklist.category.equals(category, ignoreCase = true) ||
                    globalState.settingsState.categories.any { cat ->
                        (cat.title.equals(category, ignoreCase = true) || cat.category.equals(category, ignoreCase = true)) &&
                                (checklist.category.equals(cat.category, ignoreCase = true) || checklist.category.equals(cat.title, ignoreCase = true))
                    }

            matchesQuery && matchesCategory
        }

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

        val totalMatchingCount = filteredSubmissions.size
        val displayedSubmissions = filteredSubmissions.take(limit)

        ChecklistUiState(
            checklists = filteredChecklists,
            searchQuery = query,
            selectedCategory = category,
            isAddEditDialogVisible = dialogState.isAddEditVisible,
            checklistToEdit = dialogState.checklistToEdit,
            checklistToDelete = dialogState.checklistToDelete,
            checklistToCopy = dialogState.checklistToCopy,
            selectedChecklistForQuestions = dialogState.selectedChecklistForQuestions,
            questionsForSelectedChecklist = dialogState.questions,
            isAddEditQuestionDialogVisible = dialogState.isAddEditQuestionVisible,
            questionToEdit = dialogState.questionToEdit,
            questionToDelete = dialogState.questionToDelete,
            userMessage = dialogState.message,
            userSettings = globalState.settingsState.settings,
            availableDevices = globalState.settingsState.devices,
            availableLanguages = globalState.settingsState.languages,
            isSettingsDialogVisible = globalState.settingsState.isVisible,
            isPdfSettingsVisible = globalState.settingsState.isPdfVisible,
            categories = globalState.settingsState.categories,
            selectedChecklistToAnswer = dialogState.selectedChecklistToAnswer,
            submissions = displayedSubmissions,
            totalMatchingSubmissionsCount = totalMatchingCount,
            submissionLimit = limit,
            allSubmissions = globalState.submissionState.submissions,
            answers = globalState.submissionState.answers,
            isSubmissionsOverviewVisible = globalState.submissionState.isOverviewVisible,
            selectedSubmissionToEdit = globalState.submissionState.selectedSubmissionToEdit,
            submissionToDelete = dialogState.submissionToDelete,
            isChecklistListVisible = globalState.submissionState.isChecklistListVisible,
            selectedSubmissionStatus = selectedStatus,
            scannedNfcTag = _scannedNfcTag.value,
            selectedChecklistForSubmissions = selectedChecklistSub,
            selectedChecklistForChart = _selectedChecklistForChart.value,
            textBlocks = textBlockState.textBlocks,
            isTextBlocksVisible = textBlockState.isVisible,
            isAddEditTextBlockDialogVisible = textBlockState.isAddEditVisible,
            textBlockToEdit = textBlockState.textBlockToEdit,
            textBlockToDelete = textBlockState.textBlockToDelete,
            isHelpVisible = globalState.isHelpVisible,
            isChecklistHelpVisible = globalState.isChecklistHelpVisible,
            isLicenseScreenVisible = globalState.isLicenseScreenVisible,
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

    fun openHelpScreen() {
        _isHelpVisible.value = true
    }

    fun closeChecklistsScreen() {
        _isChecklistListVisible.value = false
    }

    fun closeHelpScreen() {
        _isHelpVisible.value = false
    }

    fun openChecklistHelpScreen() {
        _isChecklistHelpVisible.value = true
    }

    fun closeChecklistHelpScreen() {
        _isChecklistHelpVisible.value = false
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

    fun saveChecklist(
        title: String,
        language: String,
        description: String,
        category: String,
        icon: String,
        templateChecklistId: String? = null,
    ) {
        val trimmedTitle = title.trim()
        val trimmedLanguage = language.trim().ifBlank { "en" }
        val trimmedCategory = if (category.isBlank()) "General" else category.trim()
        val trimmedDesc = description.trim()
        val trimmedIcon = icon.trim()

        if (trimmedTitle.isBlank()) return

        val currentEdit = _checklistToEdit.value
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.insertCategory(CategoryEntity(category = trimmedCategory, title = trimmedCategory))

            if (currentEdit == null) {
                val newChecklistId = UUID.randomUUID().toString()
                val newChecklist = ChecklistEntity(
                    id = newChecklistId,
                    title = trimmedTitle,
                    language = trimmedLanguage,
                    description = trimmedDesc,
                    category = trimmedCategory,
                    icon = trimmedIcon,
                    timestamp = System.currentTimeMillis(),
                )
                repository.insertChecklist(newChecklist)

                if (!templateChecklistId.isNullOrBlank()) {
                    val templateQuestions =
                        questionRepository.getChecklistQuestionsByChecklistId(templateChecklistId)
                            .first()
                    templateQuestions.forEach { q ->
                        val copiedQuestion = q.copy(
                            id = UUID.randomUUID().toString(),
                            checklistid = newChecklistId,
                            timestamp = System.currentTimeMillis(),
                        )
                        questionRepository.insertChecklistQuestion(copiedQuestion)
                    }
                }

                withContext(Dispatchers.Main) {
                    _userMessage.value = context.getString(R.string.msg_checklist_added)
                }
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
                withContext(Dispatchers.Main) {
                    _userMessage.value = context.getString(R.string.msg_checklist_updated)
                }
            }
            withContext(Dispatchers.Main) {
                dismissAddEditDialog()
            }
        }
    }

    fun openCopyChecklistDialog(checklist: ChecklistEntity) {
        _checklistToCopy.value = checklist
    }

    fun dismissCopyChecklistDialog() {
        _checklistToCopy.value = null
    }

    fun copyChecklist(sourceChecklist: ChecklistEntity, newTitle: String) {
        val trimmedTitle = newTitle.trim()
        if (trimmedTitle.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            val newChecklistId = UUID.randomUUID().toString()
            val newChecklist = sourceChecklist.copy(
                id = newChecklistId,
                title = trimmedTitle,
                timestamp = System.currentTimeMillis(),
            )
            repository.insertChecklist(newChecklist)

            val sourceQuestions =
                questionRepository.getChecklistQuestionsByChecklistId(sourceChecklist.id).first()
            sourceQuestions.forEach { q ->
                val newQuestion = q.copy(
                    id = UUID.randomUUID().toString(),
                    checklistid = newChecklistId,
                    timestamp = System.currentTimeMillis(),
                )
                questionRepository.insertChecklistQuestion(newQuestion)
            }

            withContext(Dispatchers.Main) {
                dismissCopyChecklistDialog()
                _userMessage.value = context.getString(R.string.msg_checklist_copied)
            }
        }
    }

    fun addCategory(categoryName: String) {
        val trimmed = categoryName.trim()
        if (trimmed.isNotBlank()) {
            viewModelScope.launch {
                categoryRepository.insertCategory(CategoryEntity(category = trimmed, title = trimmed))
                _userMessage.value = context.getString(R.string.msg_category_added)
            }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
            _userMessage.value = context.getString(R.string.msg_category_deleted)
        }
    }

    fun requestDeleteConfirmation(checklist: ChecklistEntity) {
        viewModelScope.launch {
            val allSubmissions = submissionRepository.getAllChecklistSubmissions().first()
            val hasSubmissions = allSubmissions.any { it.checklistid == checklist.id }
            if (hasSubmissions) {
                _userMessage.value =
                    context.getString(R.string.cannot_delete_checklist_with_submissions)
            } else {
                _checklistToDelete.value = checklist
            }
        }
    }

    fun dismissDeleteConfirmation() {
        _checklistToDelete.value = null
    }

    fun confirmDelete() {
        val checklist = _checklistToDelete.value ?: return
        viewModelScope.launch {
            val allSubmissions = submissionRepository.getAllChecklistSubmissions().first()
            val hasSubmissions = allSubmissions.any { it.checklistid == checklist.id }
            if (hasSubmissions) {
                _checklistToDelete.value = null
                _userMessage.value =
                    context.getString(R.string.cannot_delete_checklist_with_submissions)
                return@launch
            }
            val questions = questionRepository.getChecklistQuestionsByChecklistId(checklist.id).first()
            questions.forEach { question ->
                questionRepository.deleteChecklistQuestion(question)
            }
            repository.deleteChecklist(checklist)
            _checklistToDelete.value = null
            _userMessage.value = context.getString(R.string.msg_checklist_deleted)
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
        checklistId: String,
        inspector: String?,
        notes: String?,
        date: String = "",
        status: String = "IN_PROGRESS",
        answers: Map<String, AnswerInput>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val formattedDate = date.ifBlank {
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            }

            val questions = questionRepository.getChecklistQuestionsByChecklistId(checklistId).first()
            val allRequiredAnswered = questions.all { q ->
                !q.required || answers[q.id]?.value?.isNotBlank() == true
            }
            val finalStatus = if (status.equals("COMPLETED", ignoreCase = true) || allRequiredAnswered) "COMPLETED" else status

            val submission = ChecklistSubmissionEntity(
                checklistid = checklistId,
                status = finalStatus,
                inspector = inspector?.ifBlank { null },
                notes = notes?.ifBlank { null },
                date = formattedDate,
                timestamp = System.currentTimeMillis(),
            )

            submissionRepository.insertChecklistSubmission(submission)

            val questionsMap = questions.associateBy { it.id }
            answers.forEach { (questionId, answerInput) ->
                if (answerInput.value.isNotBlank() || !answerInput.notes.isNullOrBlank() || !answerInput.imageUri.isNullOrBlank()) {
                    val q = questionsMap[questionId]
                    val normalizedVal =
                        if (q?.type?.lowercase() == "number") answerInput.value.trim()
                            .replace(',', '.') else answerInput.value
                    val answer = ChecklistAnswerEntity(
                        submissionid = submission.id,
                        questionid = questionId,
                        value = normalizedVal,
                        notes = answerInput.notes?.ifBlank { null },
                        imagePath = answerInput.imagePath,
                        pictureSize = answerInput.pictureSize,
                        timestamp = System.currentTimeMillis(),
                    )
                    answerRepository.insertChecklistAnswer(answer)
                    if (!answerInput.imageUri.isNullOrBlank()) {
                        val savedFileName = SqlExporter.savePictureToMyChecks(
                            context,
                            submission.id,
                            answer.id,
                            Uri.parse(answerInput.imageUri)
                        )
                        if (savedFileName != null) {
                            val updatedAnswer =
                                answer.copy(imagePath = savedFileName)
                            answerRepository.updateChecklistAnswer(updatedAnswer)
                        }
                    }
                }
            }

            if (finalStatus == "COMPLETED") {
                val checklist = repository.getAllChecklists().first().find { it.id == checklistId }
                val allAnswers = answerRepository.getAllChecklistAnswers().first()
                val settings = settingsRepository.userSettings.first()
                PdfExporter.savePdfToMyChecks(
                    context,
                    submission,
                    checklist,
                    questions,
                    allAnswers,
                    settings
                )
                SqlExporter.saveSqlToMyChecks(context, submission, checklist, questions, allAnswers)
                _userMessage.value = context.getString(R.string.msg_submission_completed)
            } else {
                _userMessage.value = context.getString(R.string.msg_submission_in_progress)
            }
            closeFillChecklist()
        }
    }

    fun loadMoreSubmissions() {
        _submissionLimit.value += 30
    }

    fun resetSubmissionLimit() {
        _submissionLimit.value = 30
    }

    fun openSubmissionsOverview() {
        resetSubmissionLimit()
        _isSubmissionsOverviewVisible.value = true
    }

    fun openSubmissionsForChecklist(checklist: ChecklistEntity) {
        resetSubmissionLimit()
        _selectedChecklistForSubmissions.value = checklist
        _isSubmissionsOverviewVisible.value = true
    }

    fun clearChecklistSubmissionsFilter() {
        _selectedChecklistForSubmissions.value = null
    }

    fun openChartForChecklist(checklist: ChecklistEntity) {
        resetSubmissionLimit()
        _selectedChecklistForChart.value = checklist
        questionsJob?.cancel()
        questionsJob = viewModelScope.launch {
            questionRepository.getChecklistQuestionsByChecklistId(checklist.id)
                .collect { questionsList ->
                    _questions.value = questionsList
                }
        }
    }

    fun closeChartScreen() {
        _selectedChecklistForChart.value = null
        _questions.value = emptyList()
    }

    fun closeSubmissionsOverview() {
        _isSubmissionsOverviewVisible.value = false
        _selectedChecklistForSubmissions.value = null
    }

    fun exportSubmissionPdf(context: Context, submission: ChecklistSubmissionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val checklist = repository.getAllChecklists().first().find { it.id == submission.checklistid }
            val questions = questionRepository.getChecklistQuestionsByChecklistId(submission.checklistid).first()
            val answers = answerRepository.getAllChecklistAnswers().first()
            val settings = settingsRepository.userSettings.first()
            PdfExporter.generateAndOpenSubmissionPdf(
                context = context,
                submission = submission,
                checklist = checklist,
                questions = questions,
                answers = answers,
                settings = settings,
            )
        }
    }

    fun exportSubmissionSql(context: Context, submission: ChecklistSubmissionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val checklist = repository.getAllChecklists().first().find { it.id == submission.checklistid }
            val questions = questionRepository.getChecklistQuestionsByChecklistId(submission.checklistid).first()
            val answers = answerRepository.getAllChecklistAnswers().first()
            SqlExporter.exportSubmissionAsSql(
                context = context,
                submission = submission,
                checklist = checklist,
                questions = questions,
                answers = answers,
            )
            _userMessage.value = context.getString(R.string.msg_sql_export_saved)
        }
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
        updatedAnswersMap: Map<String, AnswerInput>,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val questions = questionRepository.getChecklistQuestionsByChecklistId(updatedSubmission.checklistid).first()
            val allRequiredAnswered = questions.all { q ->
                !q.required || updatedAnswersMap[q.id]?.value?.isNotBlank() == true
            }
            val finalStatus = if (updatedSubmission.status.equals(
                    "COMPLETED",
                    ignoreCase = true
                ) || allRequiredAnswered
            ) "COMPLETED" else updatedSubmission.status

            val finalSubmission = updatedSubmission.copy(status = finalStatus)
            submissionRepository.updateChecklistSubmission(finalSubmission)

            val existingAnswers = answerRepository.getChecklistAnswersListBySubmissionId(finalSubmission.id)
            val existingAnswerByQuestionMap = existingAnswers.associateBy { it.questionid }
            val questionsMap = questions.associateBy { it.id }

            updatedAnswersMap.forEach { (questionId, newAnswerInput) ->
                val existingAnswer = existingAnswerByQuestionMap[questionId]
                val q = questionsMap[questionId]
                val normalizedVal =
                    if (q?.type?.lowercase() == "number") newAnswerInput.value.trim()
                        .replace(',', '.') else newAnswerInput.value

                if (existingAnswer != null) {
                    var imagePathToUse = existingAnswer.imagePath
                    if (!newAnswerInput.imageUri.isNullOrBlank() && newAnswerInput.imageUri != existingAnswer.imagePath) {
                        val savedFileName = SqlExporter.savePictureToMyChecks(
                            context,
                            finalSubmission.id,
                            existingAnswer.id,
                            Uri.parse(newAnswerInput.imageUri)
                        )
                        if (savedFileName != null) {
                            imagePathToUse = savedFileName
                        }
                    } else if (newAnswerInput.imageUri == null && existingAnswer.imagePath != null && newAnswerInput.imagePath == null) {
                        imagePathToUse = null
                    }
                    if (normalizedVal != existingAnswer.value || newAnswerInput.notes != existingAnswer.notes || imagePathToUse != existingAnswer.imagePath || newAnswerInput.pictureSize != existingAnswer.pictureSize) {
                        val modifiedAnswer = existingAnswer.copy(
                            value = normalizedVal,
                            notes = newAnswerInput.notes?.ifBlank { null },
                            imagePath = imagePathToUse,
                            pictureSize = newAnswerInput.pictureSize,
                            timestamp = System.currentTimeMillis(),
                        )
                        answerRepository.updateChecklistAnswer(modifiedAnswer)
                    }
                } else if (normalizedVal.isNotBlank() || !newAnswerInput.notes.isNullOrBlank() || !newAnswerInput.imageUri.isNullOrBlank()) {
                    val newAnswer = ChecklistAnswerEntity(
                        submissionid = finalSubmission.id,
                        questionid = questionId,
                        value = normalizedVal,
                        notes = newAnswerInput.notes?.ifBlank { null },
                        pictureSize = newAnswerInput.pictureSize,
                        timestamp = System.currentTimeMillis(),
                    )
                    answerRepository.insertChecklistAnswer(newAnswer)
                    if (!newAnswerInput.imageUri.isNullOrBlank()) {
                        val savedFileName = SqlExporter.savePictureToMyChecks(
                            context,
                            finalSubmission.id,
                            newAnswer.id,
                            Uri.parse(newAnswerInput.imageUri)
                        )
                        if (savedFileName != null) {
                            val updatedNewAnswer =
                                newAnswer.copy(imagePath = savedFileName)
                            answerRepository.updateChecklistAnswer(updatedNewAnswer)
                        }
                    }
                }
            }

            if (finalSubmission.status.equals("COMPLETED", ignoreCase = true)) {
                val checklist = repository.getAllChecklists().first().find { it.id == finalSubmission.checklistid }
                val allAnswers = answerRepository.getAllChecklistAnswers().first()
                val settings = settingsRepository.userSettings.first()
                PdfExporter.savePdfToMyChecks(context, finalSubmission, checklist, questions, allAnswers, settings)
                SqlExporter.saveSqlToMyChecks(context, finalSubmission, checklist, questions, allAnswers)
                _userMessage.value = context.getString(R.string.msg_submission_completed)
            } else {
                _userMessage.value = context.getString(R.string.msg_submission_updated_in_progress)
            }
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
            _userMessage.value = context.getString(R.string.msg_submission_deleted)
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
        minValue: Double? = null,
        maxValue: Double? = null,
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
                    options = if (type == "combobox" || type == "multiselect") options.trim() else null,
                    required = required,
                    minValue = if (type == "number") minValue else null,
                    maxValue = if (type == "number") maxValue else null,
                    timestamp = System.currentTimeMillis(),
                )
                questionRepository.insertChecklistQuestion(newQuestion)
                _userMessage.value = context.getString(R.string.msg_question_added)
            } else {
                val autoSortNo = if (sortno > 0) sortno else currentEdit.sortno
                val updatedQuestion = currentEdit.copy(
                    sortno = autoSortNo,
                    title = trimmedTitle,
                    description = description.trim(),
                    type = type,
                    options = if (type == "combobox" || type == "multiselect") options.trim() else null,
                    required = required,
                    minValue = if (type == "number") minValue else null,
                    maxValue = if (type == "number") maxValue else null,
                    timestamp = System.currentTimeMillis(),
                )
                questionRepository.updateChecklistQuestion(updatedQuestion)
                _userMessage.value = context.getString(R.string.msg_question_updated)
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
            _userMessage.value = context.getString(R.string.msg_question_deleted)
        }
    }

    fun openSettingsDialog() {
        _isSettingsDialogVisible.value = true
    }

    fun dismissSettingsDialog() {
        _isSettingsDialogVisible.value = false
    }

    fun openPdfSettings() {
        _isPdfSettingsVisible.value = true
    }

    fun closePdfSettings() {
        _isPdfSettingsVisible.value = false
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
            try {
                val devices = deviceRepository.getDeviceId().first()
                val deviceModel = android.os.Build.MODEL ?: "Android Device"
                val effectiveUser = userName.ifBlank { "User" }
                val deviceNotes = "$deviceModel-$effectiveUser"
                devices.firstOrNull()?.let { device ->
                    deviceRepository.insertDevice(device.copy(notes = deviceNotes))
                }
            } catch (_: Exception) {
            }
        }
    }

    fun updateAudioFeedback(audioFeedback: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateAudioFeedback(audioFeedback)
        }
    }

    fun updatePdfHeaderTitle(title: String) {
        viewModelScope.launch { settingsRepository.updatePdfHeaderTitle(title) }
    }

    fun updateSignaturePath(path: String) {
        viewModelScope.launch { settingsRepository.updateSignaturePath(path) }
    }

    fun updatePdfIncludeChecklistTitle(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeChecklistTitle(include) }
    }

    fun updatePdfIncludeChecklistDescription(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeChecklistDescription(include) }
    }

    fun updatePdfIncludeDate(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeDate(include) }
    }

    fun updatePdfIncludeInspector(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeInspector(include) }
    }

    fun updatePdfIncludeStatus(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeStatus(include) }
    }

    fun updatePdfIncludeNotes(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeNotes(include) }
    }

    fun updatePdfIncludeTimestamp(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeTimestamp(include) }
    }

    fun updatePdfIncludePageNumber(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludePageNumber(include) }
    }

    fun updatePdfIncludeUnansweredQuestions(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeUnansweredQuestions(include) }
    }

    fun updatePdfIncludeQuestionTitle(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeQuestionTitle(include) }
    }

    fun updatePdfIncludeQuestionDescription(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeQuestionDescription(include) }
    }

    fun updatePdfIncludeAnswerValue(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeAnswerValue(include) }
    }

    fun updatePdfIncludeAnswerNotes(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeAnswerNotes(include) }
    }

    fun updatePdfIncludeAnswerPicture(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeAnswerPicture(include) }
    }

    fun updatePdfCompactAnswerLayout(compact: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfCompactAnswerLayout(compact) }
    }

    fun updatePdfFooterText(text: String) {
        viewModelScope.launch { settingsRepository.updatePdfFooterText(text) }
    }

    fun updatePdfTemplateVersion(version: String) {
        viewModelScope.launch { settingsRepository.updatePdfTemplateVersion(version) }
    }

    fun updatePdfPictureSize(size: String) {
        viewModelScope.launch { settingsRepository.updatePdfPictureSize(size) }
    }

    fun updatePdfPictureSizeSmall(size: Int) {
        viewModelScope.launch { settingsRepository.updatePdfPictureSizeSmall(size) }
    }

    fun updatePdfPictureSizeMedium(size: Int) {
        viewModelScope.launch { settingsRepository.updatePdfPictureSizeMedium(size) }
    }

    fun updatePdfPictureSizeLarge(size: Int) {
        viewModelScope.launch { settingsRepository.updatePdfPictureSizeLarge(size) }
    }

    fun openLicenseScreen() {
        _isLicenseScreenVisible.value = true
    }

    fun closeLicenseScreen() {
        _isLicenseScreenVisible.value = false
    }

    fun acceptLicense() {
        viewModelScope.launch {
            settingsRepository.acceptLicense()
        }
        closeLicenseScreen()
    }

    fun updateFtpServer(server: String) {
        viewModelScope.launch { settingsRepository.updateFtpServer(server) }
    }

    fun updateFtpPort(port: Int) {
        viewModelScope.launch { settingsRepository.updateFtpPort(port) }
    }

    fun updateFtpUseTls(useTls: Boolean) {
        viewModelScope.launch { settingsRepository.updateFtpUseTls(useTls) }
    }

    fun updateFtpUser(user: String) {
        viewModelScope.launch { settingsRepository.updateFtpUser(user) }
    }

    fun updateFtpPassword(password: String) {
        viewModelScope.launch { settingsRepository.updateFtpPassword(password) }
    }

    fun userMessageShown() {
        _userMessage.value = null
    }

    fun openTextBlocksScreen() {
        _isTextBlocksVisible.value = true
    }

    fun closeTextBlocksScreen() {
        _isTextBlocksVisible.value = false
    }

    fun openAddTextBlockDialog() {
        _textBlockToEdit.value = null
        _isAddEditTextBlockDialogVisible.value = true
    }

    fun openEditTextBlockDialog(textBlock: TextBlockEntity) {
        _textBlockToEdit.value = textBlock
        _isAddEditTextBlockDialogVisible.value = true
    }

    fun dismissAddEditTextBlockDialog() {
        _textBlockToEdit.value = null
        _isAddEditTextBlockDialogVisible.value = false
    }

    fun saveTextBlock(text: String, sortno: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val editing = _textBlockToEdit.value
            if (editing != null) {
                textBlockRepository.updateTextBlock(editing.copy(text = text, sortno = sortno))
            } else {
                val currentList = textBlocks.value
                val nextSortno =
                    if (currentList.isNotEmpty()) (currentList.maxOf { it.sortno } + 1) else 1
                textBlockRepository.insertTextBlock(
                    TextBlockEntity(
                        text = text,
                        sortno = nextSortno
                    )
                )
            }
            withContext(Dispatchers.Main) {
                dismissAddEditTextBlockDialog()
                _userMessage.value = context.getString(R.string.success)
            }
        }
    }

    fun requestDeleteTextBlockConfirmation(textBlock: TextBlockEntity) {
        _textBlockToDelete.value = textBlock
    }

    fun dismissDeleteTextBlockConfirmation() {
        _textBlockToDelete.value = null
    }

    fun confirmDeleteTextBlock() {
        val target = _textBlockToDelete.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            textBlockRepository.deleteTextBlock(target)
            withContext(Dispatchers.Main) {
                dismissDeleteTextBlockConfirmation()
                _userMessage.value = context.getString(R.string.success)
            }
        }
    }

    fun moveTextBlockUp(textBlock: TextBlockEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = textBlocks.value.sortedBy { it.sortno }
            val index = list.indexOfFirst { it.id == textBlock.id }
            if (index > 0) {
                val prev = list[index - 1]
                val tempSort = textBlock.sortno
                textBlockRepository.updateTextBlock(textBlock.copy(sortno = prev.sortno))
                textBlockRepository.updateTextBlock(prev.copy(sortno = tempSort))
            }
        }
    }

    fun moveTextBlockDown(textBlock: TextBlockEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = textBlocks.value.sortedBy { it.sortno }
            val index = list.indexOfFirst { it.id == textBlock.id }
            if (index in 0 until list.size - 1) {
                val next = list[index + 1]
                val tempSort = textBlock.sortno
                textBlockRepository.updateTextBlock(textBlock.copy(sortno = next.sortno))
                textBlockRepository.updateTextBlock(next.copy(sortno = tempSort))
            }
        }
    }

    companion object {
        fun Factory(
            repository: ChecklistRepository,
            questionRepository: ChecklistQuestionRepository,
            deviceRepository: DeviceRepository,
            languageRepository: LanguageRepository,
            settingsRepository: SettingsRepository,
            categoryRepository: CategoryRepository,
            submissionRepository: ChecklistSubmissionRepository,
            answerRepository: ChecklistAnswerRepository,
            textBlockRepository: TextBlockRepository,
            context: Context,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChecklistViewModel(
                        repository,
                        questionRepository,
                        deviceRepository,
                        languageRepository,
                        settingsRepository,
                        categoryRepository,
                        submissionRepository,
                        answerRepository,
                        textBlockRepository,
                        context,
                    ) as T
                }
            }
    }
}
