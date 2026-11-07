/*
 *  My software is <a href="https://en.wikipedia.org/wiki/Free_software" target="_blank" rel="nofollow">Free Software</a>☯ and delivered as open source via <a href="https://github.com/pobvolcom/" target="_blank" rel="nofollow">GitHub</a>☯. You can download, install, operate, modify, and redistribute my software under the terms of the <a href="https://www.gnu.org/licenses/gpl-3.0" target="_blank" rel="nofollow">GNU General Public License v3</a>☯. Copyright © 2026 Volker Pobloth, pobvol Software Services, poppi66292@gmail.com. Developed with Google Android Studio and Gemini 3.5 Flash Lite and Gemini 3.6 Flash
 */

package com.pobvol.mychecks.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pobvol.mychecks.data.local.AppDatabase
import com.pobvol.mychecks.data.local.ChecklistAnswerEntity
import com.pobvol.mychecks.data.local.ChecklistEntity
import com.pobvol.mychecks.data.local.ChecklistQuestionEntity
import com.pobvol.mychecks.data.local.ChecklistSubmissionEntity
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
import com.pobvol.mychecks.ui.AnswerInput
import com.pobvol.mychecks.ui.ChecklistUiState
import com.pobvol.mychecks.util.DateUtil
import com.pobvol.mychecks.util.PdfExporter
import com.pobvol.mychecks.util.SqlExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
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

    private val _checklistSearchQuery = MutableStateFlow("")
    private val _submissionSearchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _userMessage = MutableStateFlow<String?>(null)

    private val _isAddEditChecklistDialogVisible = MutableStateFlow(false)
    private val _checklistToEdit = MutableStateFlow<ChecklistEntity?>(null)
    private val _checklistToDelete = MutableStateFlow<ChecklistEntity?>(null)
    private val _checklistToCopy = MutableStateFlow<ChecklistEntity?>(null)

    private val _selectedChecklistForQuestions = MutableStateFlow<ChecklistEntity?>(null)
    private val _selectedChecklistToAnswer = MutableStateFlow<ChecklistEntity?>(null)
    private val _selectedChecklistForChart = MutableStateFlow<ChecklistEntity?>(null)
    private val _selectedSubmissionToEdit = MutableStateFlow<ChecklistSubmissionEntity?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val _activeChecklistId = combine(
        _selectedChecklistForQuestions,
        _selectedChecklistToAnswer,
        _selectedSubmissionToEdit,
        _selectedChecklistForChart,
    ) { qChecklist, fillChecklist, editSubmission, chartChecklist ->
        qChecklist?.id ?: fillChecklist?.id ?: editSubmission?.checklistid ?: chartChecklist?.id
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val _questions = _activeChecklistId.flatMapLatest { checklistId ->
        if (checklistId.isNullOrBlank()) flowOf(emptyList())
        else questionRepository.getChecklistQuestionsByChecklistId(checklistId)
    }

    private val _isAddEditQuestionDialogVisible = MutableStateFlow(false)
    private val _questionToEdit = MutableStateFlow<ChecklistQuestionEntity?>(null)
    private val _questionToDelete = MutableStateFlow<ChecklistQuestionEntity?>(null)

    private val _selectedChecklistForSubmissions = MutableStateFlow<ChecklistEntity?>(null)
    private val _selectedSubmissionStatus = MutableStateFlow<String?>(null)

    private val _isSettingsDialogVisible = MutableStateFlow(false)
    private val _isPdfSettingsVisible = MutableStateFlow(false)
    private val _isSubmissionsOverviewVisible = MutableStateFlow(false)
    private val _isChecklistListVisible = MutableStateFlow(false)
    private val _isTextBlocksVisible = MutableStateFlow(false)
    private val _isHelpVisible = MutableStateFlow(false)
    private val _isChecklistHelpVisible = MutableStateFlow(false)
    private val _isLicenseScreenVisible = MutableStateFlow(false)
    private val _isManageDatabaseDialogVisible = MutableStateFlow(false)

    private val _submissionToDelete = MutableStateFlow<ChecklistSubmissionEntity?>(null)
    private val _submissionLimit = MutableStateFlow(30)
    private val _scannedNfcTag = MutableStateFlow<String?>(null)

    private val _isAddEditTextBlockDialogVisible = MutableStateFlow(false)
    private val _textBlockToEdit = MutableStateFlow<TextBlockEntity?>(null)
    private val _textBlockToDelete = MutableStateFlow<TextBlockEntity?>(null)

    val uiState: StateFlow<ChecklistUiState> = combine(
        repository.getAllChecklists(),
        _checklistSearchQuery,
        _submissionSearchQuery,
        _selectedCategory,
        settingsRepository.userSettings,
        submissionRepository.getAllChecklistSubmissions(),
        answerRepository.getAllChecklistAnswers(),
        categoryRepository.getAllCategories(),
        textBlockRepository.getAllTextBlocks(),
        languageRepository.getAllLanguages(),
        deviceRepository.getDeviceId(),
        _isChecklistListVisible,
        _isSubmissionsOverviewVisible,
        _isTextBlocksVisible,
        _isHelpVisible,
        _isChecklistHelpVisible,
        _isLicenseScreenVisible,
        _isManageDatabaseDialogVisible,
        _selectedChecklistForQuestions,
        _selectedChecklistToAnswer,
        _selectedChecklistForChart,
        _selectedChecklistForSubmissions,
        _selectedSubmissionToEdit,
        _submissionToDelete,
        _isSettingsDialogVisible,
        _isPdfSettingsVisible,
        _isAddEditChecklistDialogVisible,
        _checklistToEdit,
        _checklistToDelete,
        _checklistToCopy,
        _questions,
        _isAddEditQuestionDialogVisible,
        _questionToEdit,
        _questionToDelete,
        _selectedSubmissionStatus,
        _submissionLimit,
        _userMessage,
        _isAddEditTextBlockDialogVisible,
        _textBlockToEdit,
        _textBlockToDelete,
        _scannedNfcTag,
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val checklists = flows[0] as List<ChecklistEntity>
        val checklistQuery = flows[1] as String
        val submissionQuery = flows[2] as String
        val category = flows[3] as String?
        val userSettings = flows[4] as com.pobvol.mychecks.data.repository.UserSettings

        @Suppress("UNCHECKED_CAST")
        val rawSubmissions = flows[5] as List<ChecklistSubmissionEntity>

        @Suppress("UNCHECKED_CAST")
        val answers = flows[6] as List<ChecklistAnswerEntity>

        @Suppress("UNCHECKED_CAST")
        val categories = flows[7] as List<com.pobvol.mychecks.data.local.CategoryEntity>

        @Suppress("UNCHECKED_CAST")
        val textBlocks = flows[8] as List<TextBlockEntity>

        @Suppress("UNCHECKED_CAST")
        val languages = flows[9] as List<com.pobvol.mychecks.data.local.LanguageEntity>

        @Suppress("UNCHECKED_CAST")
        val devices = flows[10] as List<com.pobvol.mychecks.data.local.DeviceEntity>

        val isChecklistListVisible = flows[11] as Boolean
        val isSubmissionsOverviewVisible = flows[12] as Boolean
        val isTextBlocksVisible = flows[13] as Boolean
        val isHelpVisible = flows[14] as Boolean
        val isChecklistHelpVisible = flows[15] as Boolean
        val isLicenseScreenVisible = flows[16] as Boolean
        val isManageDbVisible = flows[17] as Boolean
        val selectedChecklistForQuestions = flows[18] as ChecklistEntity?
        val selectedChecklistToAnswer = flows[19] as ChecklistEntity?
        val selectedChecklistForChart = flows[20] as ChecklistEntity?
        val selectedChecklistSub = flows[21] as ChecklistEntity?
        val selectedSubmissionToEdit = flows[22] as ChecklistSubmissionEntity?
        val submissionToDelete = flows[23] as ChecklistSubmissionEntity?
        val isSettingsDialogVisible = flows[24] as Boolean
        val isPdfSettingsVisible = flows[25] as Boolean
        val isAddEditChecklistDialogVisible = flows[26] as Boolean
        val checklistToEdit = flows[27] as ChecklistEntity?
        val checklistToDelete = flows[28] as ChecklistEntity?
        val checklistToCopy = flows[29] as ChecklistEntity?

        @Suppress("UNCHECKED_CAST")
        val questions = flows[30] as List<ChecklistQuestionEntity>
        val isAddEditQuestionDialogVisible = flows[31] as Boolean
        val questionToEdit = flows[32] as ChecklistQuestionEntity?
        val questionToDelete = flows[33] as ChecklistQuestionEntity?
        val selectedStatus = flows[34] as String?
        val limit = flows[35] as Int
        val userMessage = flows[36] as String?
        val isAddEditTextBlockDialogVisible = flows[37] as Boolean
        val textBlockToEdit = flows[38] as TextBlockEntity?
        val textBlockToDelete = flows[39] as TextBlockEntity?
        val scannedNfcTag = flows[40] as String?

        val targetLangCode = normalizeLanguage(userSettings.preferredLanguage)
        val effectiveChecklists =
            checklists.filter { normalizeLanguage(it.language) == targetLangCode }
        val languageChecklistIds = effectiveChecklists.map { it.id }.toSet()

        val filteredChecklists = effectiveChecklists.filter { checklist ->
            val matchesQuery = checklistQuery.isBlank() ||
                    checklist.title.contains(checklistQuery, ignoreCase = true) ||
                    checklist.description.contains(checklistQuery, ignoreCase = true) ||
                    checklist.category.contains(checklistQuery, ignoreCase = true)

            val matchesCategory = category == null ||
                    checklist.category.equals(category, ignoreCase = true) ||
                    categories.any { cat ->
                        (cat.title.equals(category, ignoreCase = true) || cat.category.equals(
                            category,
                            ignoreCase = true
                        )) &&
                                (checklist.category.equals(
                                    cat.category,
                                    ignoreCase = true
                                ) || checklist.category.equals(cat.title, ignoreCase = true))
                    }

            matchesQuery && matchesCategory
        }

        val checklistMap = checklists.associateBy { it.id }
        val sortedSubmissions = rawSubmissions.sortedWith(
            compareByDescending<ChecklistSubmissionEntity> {
                parseSubmissionDate(
                    it.date,
                    userSettings.preferredLanguage
                )
            }
                .thenByDescending { it.timestamp }
        )

        val filteredSubmissions = sortedSubmissions.filter { submission ->
            val matchesChecklist = if (selectedChecklistSub != null) {
                submission.checklistid == selectedChecklistSub.id
            } else {
                submission.checklistid in languageChecklistIds
            }

            val matchesSearch = submissionQuery.isBlank() ||
                    submission.inspector?.contains(submissionQuery, ignoreCase = true) == true ||
                    submission.notes?.contains(submissionQuery, ignoreCase = true) == true ||
                    submission.date.contains(submissionQuery, ignoreCase = true) == true ||
                    (checklistMap[submission.checklistid]?.title?.contains(
                        submissionQuery,
                        ignoreCase = true
                    ) == true)

            val matchesStatus =
                selectedStatus == null || selectedStatus.equals("All", ignoreCase = true) ||
                        submission.status.equals(selectedStatus, ignoreCase = true)

            matchesChecklist && matchesSearch && matchesStatus
        }

        val effectiveTextBlocks = textBlocks.filter {
            normalizeLanguage(it.language) == targetLangCode
        }.sortedBy { it.sortno }

        ChecklistUiState(
            checklists = filteredChecklists,
            allChecklists = checklists,
            checklistSearchQuery = checklistQuery,
            submissionSearchQuery = submissionQuery,
            selectedCategory = category,
            isAddEditDialogVisible = isAddEditChecklistDialogVisible,
            checklistToEdit = checklistToEdit,
            checklistToDelete = checklistToDelete,
            checklistToCopy = checklistToCopy,
            selectedChecklistForQuestions = selectedChecklistForQuestions,
            questionsForSelectedChecklist = questions,
            isAddEditQuestionDialogVisible = isAddEditQuestionDialogVisible,
            questionToEdit = questionToEdit,
            questionToDelete = questionToDelete,
            userMessage = userMessage,
            userSettings = userSettings,
            categories = categories,
            selectedChecklistToAnswer = selectedChecklistToAnswer,
            submissions = filteredSubmissions.take(limit),
            totalMatchingSubmissionsCount = filteredSubmissions.size,
            submissionLimit = limit,
            allSubmissions = sortedSubmissions,
            answers = answers,
            isSubmissionsOverviewVisible = isSubmissionsOverviewVisible,
            selectedSubmissionToEdit = selectedSubmissionToEdit,
            submissionToDelete = submissionToDelete,
            isChecklistListVisible = isChecklistListVisible,
            selectedSubmissionStatus = selectedStatus,
            scannedNfcTag = scannedNfcTag,
            selectedChecklistForSubmissions = selectedChecklistSub,
            selectedChecklistForChart = selectedChecklistForChart,
            textBlocks = effectiveTextBlocks,
            isTextBlocksVisible = isTextBlocksVisible,
            isAddEditTextBlockDialogVisible = isAddEditTextBlockDialogVisible,
            textBlockToEdit = textBlockToEdit,
            textBlockToDelete = textBlockToDelete,
            isHelpVisible = isHelpVisible,
            isChecklistHelpVisible = isChecklistHelpVisible,
            isLicenseScreenVisible = isLicenseScreenVisible,
            isManageDatabaseDialogVisible = isManageDbVisible,
            isSettingsDialogVisible = isSettingsDialogVisible,
            isPdfSettingsVisible = isPdfSettingsVisible,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChecklistUiState()
    )

    private fun normalizeLanguage(lang: String): String {
        val trimmed = lang.trim()
        return when {
            trimmed.startsWith("de", ignoreCase = true) -> "de"
            trimmed.startsWith("en", ignoreCase = true) -> "en"
            trimmed.startsWith("es", ignoreCase = true) -> "es"
            trimmed.startsWith("fr", ignoreCase = true) -> "fr"
            else -> trimmed
        }
    }

    private fun parseSubmissionDate(dateStr: String, preferredLang: String = "de"): Long {
        val iso = DateUtil.toIsoDate(dateStr, preferredLang)
        try {
            val parts = iso.split("-")
            if (parts.size >= 3) {
                val year = parts[0].toLongOrNull() ?: 0L
                val month = parts[1].toLongOrNull() ?: 0L
                val day = parts[2].toLongOrNull() ?: 0L
                return year * 10000L + month * 100L + day
            }
        } catch (_: Exception) {
        }
        return 0L
    }

    fun userMessageShown() {
        _userMessage.value = null
    }

    fun onChecklistSearchQueryChange(q: String) {
        _checklistSearchQuery.value = q
    }

    fun onSubmissionSearchQueryChange(q: String) {
        _submissionSearchQuery.value = q
    }

    fun onCategoryFilterChange(c: String?) {
        _selectedCategory.value = c
    }

    fun onSubmissionStatusFilterChange(s: String?) {
        _selectedSubmissionStatus.value = s
    }

    fun openAddDialog() {
        _checklistToEdit.value = null; _isAddEditChecklistDialogVisible.value = true
    }

    fun openEditDialog(c: ChecklistEntity) {
        _checklistToEdit.value = c; _isAddEditChecklistDialogVisible.value = true
    }

    fun dismissAddEditDialog() {
        _isAddEditChecklistDialogVisible.value = false; _checklistToEdit.value = null
    }

    fun requestDeleteConfirmation(c: ChecklistEntity) {
        _checklistToDelete.value = c
    }

    fun dismissDeleteConfirmation() {
        _checklistToDelete.value = null
    }

    fun openCopyChecklistDialog(c: ChecklistEntity) {
        _checklistToCopy.value = c
    }

    fun dismissCopyChecklistDialog() {
        _checklistToCopy.value = null
    }

    fun openChecklistsScreen() {
        _isChecklistListVisible.value = true
    }

    fun closeChecklistsScreen() {
        _isChecklistListVisible.value = false
    }

    fun openSubmissionsOverview() {
        _isSubmissionsOverviewVisible.value = true
    }

    fun closeSubmissionsOverview() {
        _isSubmissionsOverviewVisible.value = false; _selectedChecklistForSubmissions.value = null
    }

    fun clearChecklistSubmissionsFilter() {
        _selectedChecklistForSubmissions.value = null
    }

    fun openSubmissionsForChecklist(c: ChecklistEntity) {
        _selectedChecklistForSubmissions.value = c; _isSubmissionsOverviewVisible.value = true
    }

    fun openChartForChecklist(c: ChecklistEntity) {
        _selectedChecklistForChart.value = c
    }

    fun closeChartScreen() {
        _selectedChecklistForChart.value = null
    }

    fun openHelpScreen() {
        _isHelpVisible.value = true
    }

    fun closeHelpScreen() {
        _isHelpVisible.value = false
    }

    fun openLicenseScreen() {
        _isLicenseScreenVisible.value = true
    }

    fun closeLicenseScreen() {
        _isLicenseScreenVisible.value = false
    }

    fun openTextBlocksScreen() {
        _isTextBlocksVisible.value = true
    }

    fun closeTextBlocksScreen() {
        _isTextBlocksVisible.value = false
    }

    fun openManageDatabaseDialog() {
        _isManageDatabaseDialogVisible.value = true
    }

    fun closeManageDatabaseDialog() {
        _isManageDatabaseDialogVisible.value = false
    }

    fun openQuestionsForChecklist(c: ChecklistEntity) {
        _selectedChecklistForQuestions.value = c
    }

    fun closeQuestionsScreen() {
        _selectedChecklistForQuestions.value = null
    }

    fun openAddQuestionDialog() {
        _questionToEdit.value = null; _isAddEditQuestionDialogVisible.value = true
    }

    fun openEditQuestionDialog(q: ChecklistQuestionEntity) {
        _questionToEdit.value = q; _isAddEditQuestionDialogVisible.value = true
    }

    fun dismissAddEditQuestionDialog() {
        _isAddEditQuestionDialogVisible.value = false; _questionToEdit.value = null
    }

    fun requestDeleteQuestionConfirmation(q: ChecklistQuestionEntity) {
        _questionToDelete.value = q
    }

    fun dismissDeleteQuestionConfirmation() {
        _questionToDelete.value = null
    }

    fun openFillChecklist(c: ChecklistEntity) {
        _selectedChecklistToAnswer.value = c
    }

    fun closeFillChecklist() {
        _selectedChecklistToAnswer.value = null
    }

    fun openEditSubmission(s: ChecklistSubmissionEntity) {
        _selectedSubmissionToEdit.value = s
    }

    fun closeEditSubmission() {
        _selectedSubmissionToEdit.value = null
    }

    fun requestDeleteSubmission(s: ChecklistSubmissionEntity) {
        _submissionToDelete.value = s
    }

    fun dismissDeleteSubmission() {
        _submissionToDelete.value = null
    }

    fun loadMoreSubmissions() {
        _submissionLimit.value += 30
    }

    fun clearNfcTag() {
        _scannedNfcTag.value = null
    }

    fun onNfcTagScanned(tag: String) {
        _scannedNfcTag.value = tag
    }

    fun acceptLicense() {
        viewModelScope.launch { settingsRepository.acceptLicense() }
    }

    fun updatePreferredLanguage(lang: String) {
        viewModelScope.launch { settingsRepository.updatePreferredLanguage(lang) }
    }

    fun updateThemeMode(theme: com.pobvol.mychecks.data.repository.ThemeMode) {
        viewModelScope.launch { settingsRepository.updateThemeMode(theme) }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch { settingsRepository.updateUserName(name) }
    }

    fun updateAudioFeedback(audio: Boolean) {
        viewModelScope.launch { settingsRepository.updateAudioFeedback(audio) }
    }

    fun addCategory(cat: String) {
        if (cat.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val t = cat.trim()
            categoryRepository.insertCategory(
                com.pobvol.mychecks.data.local.CategoryEntity(
                    category = t,
                    title = t
                )
            )
        }
    }

    fun deleteCategory(cat: com.pobvol.mychecks.data.local.CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) { categoryRepository.deleteCategory(cat) }
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

    fun updatePdfHeaderTitle(t: String) {
        viewModelScope.launch { settingsRepository.updatePdfHeaderTitle(t) }
    }

    fun updatePdfIncludeChecklistTitle(i: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeChecklistTitle(i) }
    }

    fun updatePdfIncludeChecklistDescription(i: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeChecklistDescription(i) }
    }

    fun updatePdfIncludeDate(i: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeDate(i) }
    }

    fun updatePdfIncludeInspector(i: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeInspector(i) }
    }

    fun updatePdfIncludeStatus(i: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeStatus(i) }
    }

    fun updatePdfIncludeNotes(i: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeNotes(i) }
    }

    fun updatePdfIncludeTimestamp(i: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeTimestamp(i) }
    }

    fun updatePdfIncludePageNumber(i: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludePageNumber(i) }
    }

    fun updatePdfIncludeUnansweredQuestions(i: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeUnansweredQuestions(i) }
    }

    fun updatePdfIncludeQuestionTitle(i: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeQuestionTitle(i) }
    }

    fun updatePdfIncludeQuestionDescription(i: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeQuestionDescription(i) }
    }

    fun updatePdfIncludeAnswerValue(i: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeAnswerValue(i) }
    }

    fun updatePdfIncludeAnswerNotes(i: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeAnswerNotes(i) }
    }

    fun updatePdfIncludeAnswerPicture(i: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeAnswerPicture(i) }
    }

    fun updatePdfCompactAnswerLayout(c: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfCompactAnswerLayout(c) }
    }

    fun updatePdfFooterText(t: String) {
        viewModelScope.launch { settingsRepository.updatePdfFooterText(t) }
    }

    fun updatePdfTemplateVersion(v: String) {
        viewModelScope.launch { settingsRepository.updatePdfTemplateVersion(v) }
    }

    fun updatePdfPictureSizeSmall(s: Int) {
        viewModelScope.launch { settingsRepository.updatePdfPictureSizeSmall(s) }
    }

    fun updatePdfPictureSizeMedium(s: Int) {
        viewModelScope.launch { settingsRepository.updatePdfPictureSizeMedium(s) }
    }

    fun updatePdfPictureSizeLarge(s: Int) {
        viewModelScope.launch { settingsRepository.updatePdfPictureSizeLarge(s) }
    }

    fun updateSignaturePath(p: String) {
        viewModelScope.launch { settingsRepository.updateSignaturePath(p) }
    }

    fun confirmDelete() {
        val target = _checklistToDelete.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteChecklist(target)
            _checklistToDelete.value = null
        }
    }

    fun confirmDeleteSubmission() {
        val target = _submissionToDelete.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            submissionRepository.deleteChecklistSubmission(target)
            _submissionToDelete.value = null
        }
    }

    fun confirmDeleteQuestion() {
        val target = _questionToDelete.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            questionRepository.deleteChecklistQuestion(target)
            _questionToDelete.value = null
        }
    }

    fun saveChecklist(
        title: String,
        language: String,
        description: String,
        category: String,
        icon: String?,
        iconColor: String? = null
    ) {
        val currentEdit = _checklistToEdit.value
        viewModelScope.launch(Dispatchers.IO) {
            if (currentEdit == null) {
                val newChecklist = ChecklistEntity(
                    title = title,
                    language = language,
                    description = description,
                    category = category,
                    icon = icon,
                )
                repository.insertChecklist(newChecklist)
            } else {
                val updated = currentEdit.copy(
                    title = title,
                    language = language,
                    description = description,
                    category = category,
                    icon = icon,
                    timestamp = System.currentTimeMillis(),
                )
                repository.updateChecklist(updated)
            }
            _isAddEditChecklistDialogVisible.value = false
            _checklistToEdit.value = null
        }
    }

    fun copyChecklist(sourceChecklist: ChecklistEntity, newTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newChecklist = ChecklistEntity(
                title = newTitle,
                language = sourceChecklist.language,
                description = sourceChecklist.description,
                category = sourceChecklist.category,
                icon = sourceChecklist.icon,
            )
            val newId = repository.insertChecklist(newChecklist)
            val sourceQuestions =
                questionRepository.getChecklistQuestionsByChecklistId(sourceChecklist.id).first()
            sourceQuestions.forEach { q ->
                questionRepository.insertChecklistQuestion(
                    ChecklistQuestionEntity(
                        checklistid = newId.toString(),
                        title = q.title,
                        description = q.description,
                        type = q.type,
                        options = q.options,
                        required = q.required,
                        sortno = q.sortno,
                        minValue = q.minValue,
                        maxValue = q.maxValue,
                    )
                )
            }
            _checklistToCopy.value = null
        }
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
        val checklist = _selectedChecklistForQuestions.value ?: return
        val currentEdit = _questionToEdit.value
        viewModelScope.launch(Dispatchers.IO) {
            if (currentEdit == null) {
                val currentQuestions =
                    questionRepository.getChecklistQuestionsByChecklistId(checklist.id).first()
                val nextSortNo = (currentQuestions.maxOfOrNull { it.sortno } ?: 0) + 1
                val newQuestion = ChecklistQuestionEntity(
                    checklistid = checklist.id,
                    title = title,
                    description = description.ifBlank { null },
                    type = type,
                    options = options.ifBlank { null },
                    required = required,
                    sortno = nextSortNo,
                    minValue = minValue,
                    maxValue = maxValue,
                )
                questionRepository.insertChecklistQuestion(newQuestion)
            } else {
                val updated = currentEdit.copy(
                    title = title,
                    description = description.ifBlank { null },
                    type = type,
                    options = options.ifBlank { null },
                    required = required,
                    minValue = minValue,
                    maxValue = maxValue,
                    timestamp = System.currentTimeMillis(),
                )
                questionRepository.updateChecklistQuestion(updated)
            }
            _isAddEditQuestionDialogVisible.value = false
            _questionToEdit.value = null
        }
    }

    fun moveQuestionUp(question: ChecklistQuestionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val questionsList =
                questionRepository.getChecklistQuestionsByChecklistId(question.checklistid).first()
                    .sortedWith(compareBy({ it.sortno }, { it.id }))
            val index = questionsList.indexOfFirst { it.id == question.id }
            if (index > 0) {
                val prev = questionsList[index - 1]
                val newQuestionsList = questionsList.toMutableList()
                val tempSortno = prev.sortno
                newQuestionsList[index - 1] = prev.copy(sortno = question.sortno)
                newQuestionsList[index] = question.copy(sortno = tempSortno)
                questionRepository.updateChecklistQuestion(newQuestionsList[index - 1])
                questionRepository.updateChecklistQuestion(newQuestionsList[index])
            }
        }
    }

    fun moveQuestionDown(question: ChecklistQuestionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val questionsList =
                questionRepository.getChecklistQuestionsByChecklistId(question.checklistid).first()
                    .sortedWith(compareBy({ it.sortno }, { it.id }))
            val index = questionsList.indexOfFirst { it.id == question.id }
            if (index in 0 until questionsList.size - 1) {
                val next = questionsList[index + 1]
                val newQuestionsList = questionsList.toMutableList()
                val tempSortno = next.sortno
                newQuestionsList[index + 1] = next.copy(sortno = question.sortno)
                newQuestionsList[index] = question.copy(sortno = tempSortno)
                questionRepository.updateChecklistQuestion(newQuestionsList[index + 1])
                questionRepository.updateChecklistQuestion(newQuestionsList[index])
            }
        }
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
            val currentPreferredLang = settingsRepository.userSettings.first().preferredLanguage
            val isoDate = DateUtil.toIsoDate(date, currentPreferredLang)
            val formattedDate = isoDate.ifBlank {
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    .format(java.util.Date())
            }

            val questions =
                questionRepository.getChecklistQuestionsByChecklistId(checklistId).first()
            val allRequiredAnswered = questions.all { q ->
                !q.required || answers[q.id]?.value?.isNotBlank() == true
            }
            val finalStatus = if (status.equals(
                    "COMPLETED",
                    ignoreCase = true
                ) || allRequiredAnswered
            ) "COMPLETED" else status

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
                            val updatedAnswer = answer.copy(imagePath = savedFileName)
                            answerRepository.updateChecklistAnswer(updatedAnswer)
                        }
                    }
                }
            }

            if (submission.status.equals("COMPLETED", ignoreCase = true)) {
                val checklist =
                    repository.getAllChecklists().first().find { it.id == submission.checklistid }
                val allAnswers = answerRepository.getAllChecklistAnswers().first()
                val settings = settingsRepository.userSettings.first()
                try {
                    PdfExporter.savePdfToMyChecks(
                        context,
                        submission,
                        checklist,
                        questions,
                        allAnswers,
                        settings
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    SqlExporter.saveSqlToMyChecks(
                        context,
                        submission,
                        checklist,
                        questions,
                        allAnswers
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                _userMessage.value =
                    context.getString(com.pobvol.mychecks.R.string.msg_submission_completed)
            } else {
                _userMessage.value =
                    context.getString(com.pobvol.mychecks.R.string.msg_submission_updated_in_progress)
            }
            closeFillChecklist()
        }
    }

    fun updateSubmissionAndAnswers(
        updatedSubmission: ChecklistSubmissionEntity,
        updatedAnswersMap: Map<String, AnswerInput>,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentPreferredLang = settingsRepository.userSettings.first().preferredLanguage
            val isoDate = DateUtil.toIsoDate(updatedSubmission.date, currentPreferredLang)
            val normalizedSubmission = updatedSubmission.copy(date = isoDate)

            val questions =
                questionRepository.getChecklistQuestionsByChecklistId(normalizedSubmission.checklistid)
                    .first()
            val allRequiredAnswered = questions.all { q ->
                !q.required || updatedAnswersMap[q.id]?.value?.isNotBlank() == true
            }
            val finalStatus = if (normalizedSubmission.status.equals(
                    "COMPLETED",
                    ignoreCase = true
                ) || allRequiredAnswered
            ) "COMPLETED" else normalizedSubmission.status

            val finalSubmission = normalizedSubmission.copy(status = finalStatus)
            submissionRepository.updateChecklistSubmission(finalSubmission)

            val existingAnswers =
                answerRepository.getChecklistAnswersListBySubmissionId(finalSubmission.id)
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
                val checklist = repository.getAllChecklists().first()
                    .find { it.id == finalSubmission.checklistid }
                val allAnswers = answerRepository.getAllChecklistAnswers().first()
                val settings = settingsRepository.userSettings.first()
                try {
                    PdfExporter.savePdfToMyChecks(
                        context,
                        finalSubmission,
                        checklist,
                        questions,
                        allAnswers,
                        settings
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    SqlExporter.saveSqlToMyChecks(
                        context,
                        finalSubmission,
                        checklist,
                        questions,
                        allAnswers
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                _userMessage.value =
                    context.getString(com.pobvol.mychecks.R.string.msg_submission_completed)
            } else {
                _userMessage.value =
                    context.getString(com.pobvol.mychecks.R.string.msg_submission_updated_in_progress)
            }
            closeEditSubmission()
        }
    }

    fun exportSubmissionPdf(context: Context, submission: ChecklistSubmissionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val checklist =
                repository.getAllChecklists().first().find { it.id == submission.checklistid }
            val questions =
                questionRepository.getChecklistQuestionsByChecklistId(submission.checklistid)
                    .first()
            val answers = answerRepository.getChecklistAnswersListBySubmissionId(submission.id)
            val settings = settingsRepository.userSettings.first()
            PdfExporter.generateAndOpenSubmissionPdf(
                context,
                submission,
                checklist,
                questions,
                answers,
                settings
            )
        }
    }

    fun exportSubmissionSql(context: Context, submission: ChecklistSubmissionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val checklist =
                repository.getAllChecklists().first().find { it.id == submission.checklistid }
            val questions =
                questionRepository.getChecklistQuestionsByChecklistId(submission.checklistid)
                    .first()
            val answers = answerRepository.getChecklistAnswersListBySubmissionId(submission.id)
            SqlExporter.exportSubmissionAsSql(context, submission, checklist, questions, answers)
        }
    }

    fun openAddTextBlockDialog() {
        _textBlockToEdit.value = null; _isAddEditTextBlockDialogVisible.value = true
    }

    fun openEditTextBlockDialog(tb: TextBlockEntity) {
        _textBlockToEdit.value = tb; _isAddEditTextBlockDialogVisible.value = true
    }

    fun dismissAddEditTextBlockDialog() {
        _isAddEditTextBlockDialogVisible.value = false; _textBlockToEdit.value = null
    }

    fun requestDeleteTextBlockConfirmation(tb: TextBlockEntity) {
        _textBlockToDelete.value = tb
    }

    fun dismissDeleteTextBlockConfirmation() {
        _textBlockToDelete.value = null
    }

    fun confirmDeleteTextBlock() {
        val target = _textBlockToDelete.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            textBlockRepository.deleteTextBlock(target)
            _textBlockToDelete.value = null
        }
    }

    fun saveTextBlock(text: String, sortno: Int = 0) {
        val currentEdit = _textBlockToEdit.value
        viewModelScope.launch(Dispatchers.IO) {
            val currentPreferredLang = settingsRepository.userSettings.first().preferredLanguage
            if (currentEdit == null) {
                val currentBlocks = textBlockRepository.getAllTextBlocks().first()
                val nextSortNo =
                    if (sortno > 0) sortno else (currentBlocks.maxOfOrNull { it.sortno } ?: 0) + 1
                val newBlock = TextBlockEntity(
                    text = text,
                    sortno = nextSortNo,
                    language = currentPreferredLang,
                )
                textBlockRepository.insertTextBlock(newBlock)
            } else {
                val updated = currentEdit.copy(
                    text = text,
                    sortno = if (sortno > 0) sortno else currentEdit.sortno,
                    language = currentPreferredLang,
                )
                textBlockRepository.updateTextBlock(updated)
            }
            _isAddEditTextBlockDialogVisible.value = false
            _textBlockToEdit.value = null
        }
    }

    fun moveTextBlockUp(tb: TextBlockEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = textBlockRepository.getAllTextBlocks().first().sortedBy { it.sortno }
            val index = list.indexOfFirst { it.id == tb.id }
            if (index > 0) {
                val prev = list[index - 1]
                val newList = list.toMutableList()
                val tempSortno = prev.sortno
                newList[index - 1] = prev.copy(sortno = tb.sortno)
                newList[index] = tb.copy(sortno = tempSortno)
                textBlockRepository.updateTextBlock(newList[index - 1])
                textBlockRepository.updateTextBlock(newList[index])
            }
        }
    }

    fun moveTextBlockDown(tb: TextBlockEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = textBlockRepository.getAllTextBlocks().first().sortedBy { it.sortno }
            val index = list.indexOfFirst { it.id == tb.id }
            if (index in 0 until list.size - 1) {
                val next = list[index + 1]
                val newList = list.toMutableList()
                val tempSortno = next.sortno
                newList[index + 1] = next.copy(sortno = tb.sortno)
                newList[index] = tb.copy(sortno = tempSortno)
                textBlockRepository.updateTextBlock(newList[index + 1])
                textBlockRepository.updateTextBlock(newList[index])
            }
        }
    }

    fun loadDemoDatabase(c: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dbFile = c.getDatabasePath("mychecksdb.sqlite3")
                AppDatabase.resetDatabaseInstance()
                if (dbFile.exists()) dbFile.delete()

                val assetManager = c.assets
                assetManager.open("demo_db.sqlite3").use { inputStream ->
                    java.io.FileOutputStream(dbFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                _userMessage.value = "Demo database loaded"
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.value = "Failed to load demo database: ${e.message}"
            }
        }
    }

    fun resetDatabase(c: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dbFile = c.getDatabasePath("mychecksdb.sqlite3")
                AppDatabase.resetDatabaseInstance()
                if (dbFile.exists()) dbFile.delete()
                _userMessage.value = c.getString(com.pobvol.mychecks.R.string.msg_db_reset)
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.value = "Failed to reset database"
            }
        }
    }

    fun clearAppCache(c: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val downloadDir = SqlExporter.getPdfRootDir(c)
                if (downloadDir.exists() && downloadDir.isDirectory) {
                    val pdfDir = SqlExporter.getPdfDir(c)
                    val sqlDir = SqlExporter.getSqlDir(c)

                    if (pdfDir.exists() && pdfDir.isDirectory) {
                        pdfDir.listFiles()?.forEach { file -> if (file.isFile) file.delete() }
                    }
                    if (sqlDir.exists() && sqlDir.isDirectory) {
                        sqlDir.listFiles()?.forEach { file -> if (file.isFile) file.delete() }
                    }
                }
                _userMessage.value = c.getString(com.pobvol.mychecks.R.string.msg_cache_cleared)
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.value = "Failed to clear cache"
            }
        }
    }

    suspend fun importDatabaseFromUri(c: Context, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val dbFile = c.getDatabasePath("mychecksdb.sqlite3")
                AppDatabase.resetDatabaseInstance()
                if (dbFile.exists()) dbFile.delete()
                val walFile = c.getDatabasePath("mychecksdb.sqlite3-wal")
                if (walFile.exists()) walFile.delete()
                val shmFile = c.getDatabasePath("mychecksdb.sqlite3-shm")
                if (shmFile.exists()) shmFile.delete()

                c.contentResolver.openInputStream(uri)?.use { inputStream ->
                    java.io.FileOutputStream(dbFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: return@withContext false

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    class Factory(
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
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(
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
