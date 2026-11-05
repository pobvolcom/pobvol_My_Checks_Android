/*
 *  My software is <a href="https://en.wikipedia.org/wiki/Free_software" target="_blank" rel="nofollow">Free Software</a>☯ and delivered as open source via <a href="https://github.com/pobvolcom/" target="_blank" rel="nofollow">GitHub</a>☯. You can download, install, operate, modify, and redistribute my software under the terms of the <a href="https://www.gnu.org/licenses/gpl-3.0" target="_blank" rel="nofollow">GNU General Public License v3</a>☯. Copyright © 2026 Volker Pobloth, pobvol Software Services, poppi66292@gmail.com. Developed with Google Android Studio and Gemini 3.5 Flash Lite and Gemini 3.6 Flash
 */

package com.pobvol.mychecks.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pobvol.mychecks.data.local.ChecklistAnswerEntity
import com.pobvol.mychecks.data.local.ChecklistEntity
import com.pobvol.mychecks.data.local.ChecklistSubmissionEntity
import com.pobvol.mychecks.data.repository.ChecklistAnswerRepository
import com.pobvol.mychecks.data.repository.ChecklistQuestionRepository
import com.pobvol.mychecks.data.repository.ChecklistRepository
import com.pobvol.mychecks.data.repository.ChecklistSubmissionRepository
import com.pobvol.mychecks.data.repository.SettingsRepository
import com.pobvol.mychecks.util.PdfExporter
import com.pobvol.mychecks.util.SqlExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SubmissionsUiState(
    val submissions: List<ChecklistSubmissionEntity> = emptyList(),
    val allSubmissions: List<ChecklistSubmissionEntity> = emptyList(),
    val answers: List<ChecklistAnswerEntity> = emptyList(),
    val submissionSearchQuery: String = "",
    val selectedSubmissionStatus: String? = null,
    val selectedChecklistForSubmissions: ChecklistEntity? = null,
    val totalMatchingSubmissionsCount: Int = 0,
    val submissionLimit: Int = 10,
    val submissionToDelete: ChecklistSubmissionEntity? = null,
    val selectedSubmissionToEdit: ChecklistSubmissionEntity? = null,
)

class SubmissionsViewModel(
    private val submissionRepository: ChecklistSubmissionRepository,
    private val answerRepository: ChecklistAnswerRepository,
    private val checklistRepository: ChecklistRepository,
    private val questionRepository: ChecklistQuestionRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _submissionSearchQuery = MutableStateFlow("")
    private val _selectedSubmissionStatus = MutableStateFlow<String?>(null)
    private val _selectedChecklistForSubmissions = MutableStateFlow<ChecklistEntity?>(null)
    private val _submissionLimit = MutableStateFlow(10)
    private val _submissionToDelete = MutableStateFlow<ChecklistSubmissionEntity?>(null)
    private val _selectedSubmissionToEdit = MutableStateFlow<ChecklistSubmissionEntity?>(null)

    val uiState: StateFlow<SubmissionsUiState> = combine(
        submissionRepository.getAllChecklistSubmissions(),
        answerRepository.getAllChecklistAnswers(),
        _submissionSearchQuery,
        _selectedSubmissionStatus,
        _selectedChecklistForSubmissions,
        _submissionLimit,
        _submissionToDelete,
        _selectedSubmissionToEdit,
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val rawSubmissions = flows[0] as List<ChecklistSubmissionEntity>

        @Suppress("UNCHECKED_CAST")
        val answers = flows[1] as List<ChecklistAnswerEntity>
        val query = flows[2] as String
        val status = flows[3] as String?
        val selectedChecklist = flows[4] as ChecklistEntity?
        val limit = flows[5] as Int
        val toDelete = flows[6] as ChecklistSubmissionEntity?
        val toEdit = flows[7] as ChecklistSubmissionEntity?

        val filtered = rawSubmissions.filter { submission ->
            val matchesChecklist =
                selectedChecklist == null || submission.checklistid == selectedChecklist.id
            val matchesSearch = query.isBlank() ||
                    submission.inspector?.contains(query, ignoreCase = true) == true ||
                    submission.notes?.contains(query, ignoreCase = true) == true ||
                    submission.date.contains(query, ignoreCase = true) == true
            val matchesStatus = status == null || status.equals("All", ignoreCase = true) ||
                    submission.status.equals(status, ignoreCase = true)

            matchesChecklist && matchesSearch && matchesStatus
        }

        SubmissionsUiState(
            submissions = filtered.take(limit),
            allSubmissions = rawSubmissions,
            answers = answers,
            submissionSearchQuery = query,
            selectedSubmissionStatus = status,
            selectedChecklistForSubmissions = selectedChecklist,
            totalMatchingSubmissionsCount = filtered.size,
            submissionLimit = limit,
            submissionToDelete = toDelete,
            selectedSubmissionToEdit = toEdit,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SubmissionsUiState()
    )

    fun setSubmissionSearchQuery(query: String) {
        _submissionSearchQuery.value = query
    }

    fun setSubmissionStatusFilter(status: String?) {
        _selectedSubmissionStatus.value = status
    }

    fun selectChecklistForSubmissions(checklist: ChecklistEntity?) {
        _selectedChecklistForSubmissions.value = checklist
    }

    fun loadMoreSubmissions() {
        _submissionLimit.value += 10
    }

    fun requestDeleteSubmission(submission: ChecklistSubmissionEntity) {
        _submissionToDelete.value = submission
    }

    fun dismissDeleteSubmissionDialog() {
        _submissionToDelete.value = null
    }

    fun openEditSubmission(submission: ChecklistSubmissionEntity) {
        _selectedSubmissionToEdit.value = submission
    }

    fun closeEditSubmission() {
        _selectedSubmissionToEdit.value = null
    }

    fun confirmDeleteSubmission() {
        val target = _submissionToDelete.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            submissionRepository.deleteChecklistSubmission(target)
            _submissionToDelete.value = null
        }
    }

    fun exportPdf(context: Context, submission: ChecklistSubmissionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val checklist = checklistRepository.getAllChecklists().first()
                .find { it.id == submission.checklistid }
            val questions =
                questionRepository.getChecklistQuestionsByChecklistId(submission.checklistid)
                    .first()
            val answers = answerRepository.getChecklistAnswersListBySubmissionId(submission.id)
            PdfExporter.generateAndOpenSubmissionPdf(
                context,
                submission,
                checklist,
                questions,
                answers
            )
        }
    }

    fun exportSql(context: Context, submission: ChecklistSubmissionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val checklist = checklistRepository.getAllChecklists().first()
                .find { it.id == submission.checklistid }
            val questions =
                questionRepository.getChecklistQuestionsByChecklistId(submission.checklistid)
                    .first()
            val answers = answerRepository.getChecklistAnswersListBySubmissionId(submission.id)
            SqlExporter.exportSubmissionAsSql(context, submission, checklist, questions, answers)
        }
    }

    class Factory(
        private val submissionRepository: ChecklistSubmissionRepository,
        private val answerRepository: ChecklistAnswerRepository,
        private val checklistRepository: ChecklistRepository,
        private val questionRepository: ChecklistQuestionRepository,
        private val settingsRepository: SettingsRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SubmissionsViewModel(
                submissionRepository,
                answerRepository,
                checklistRepository,
                questionRepository,
                settingsRepository,
            ) as T
        }
    }
}
