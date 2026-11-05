/*
 *  My software is <a href="https://en.wikipedia.org/wiki/Free_software" target="_blank" rel="nofollow">Free Software</a>☯ and delivered as open source via <a href="https://github.com/pobvolcom/" target="_blank" rel="nofollow">GitHub</a>☯. You can download, install, operate, modify, and redistribute my software under the terms of the <a href="https://www.gnu.org/licenses/gpl-3.0" target="_blank" rel="nofollow">GNU General Public License v3</a>☯. Copyright © 2026 Volker Pobloth, pobvol Software Services, poppi66292@gmail.com. Developed with Google Android Studio and Gemini 3.5 Flash Lite and Gemini 3.6 Flash
 */

package com.pobvol.mychecks.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pobvol.mychecks.data.local.ChecklistEntity
import com.pobvol.mychecks.data.local.ChecklistQuestionEntity
import com.pobvol.mychecks.data.repository.ChecklistQuestionRepository
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

data class QuestionsUiState(
    val selectedChecklistForQuestions: ChecklistEntity? = null,
    val questions: List<ChecklistQuestionEntity> = emptyList(),
    val isAddEditQuestionDialogVisible: Boolean = false,
    val questionToEdit: ChecklistQuestionEntity? = null,
    val questionToDelete: ChecklistQuestionEntity? = null,
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class QuestionsViewModel(
    private val questionRepository: ChecklistQuestionRepository,
) : ViewModel() {

    private val _selectedChecklistForQuestions = MutableStateFlow<ChecklistEntity?>(null)
    private val _isAddEditQuestionDialogVisible = MutableStateFlow(false)
    private val _questionToEdit = MutableStateFlow<ChecklistQuestionEntity?>(null)
    private val _questionToDelete = MutableStateFlow<ChecklistQuestionEntity?>(null)

    private val _questions = _selectedChecklistForQuestions.flatMapLatest { checklist ->
        if (checklist == null) flowOf(emptyList())
        else questionRepository.getChecklistQuestionsByChecklistId(checklist.id)
    }

    val uiState: StateFlow<QuestionsUiState> = combine(
        _selectedChecklistForQuestions,
        _questions,
        _isAddEditQuestionDialogVisible,
        _questionToEdit,
        _questionToDelete,
    ) { selectedChecklist, questions, isAddEditVisible, toEdit, toDelete ->
        QuestionsUiState(
            selectedChecklistForQuestions = selectedChecklist,
            questions = questions,
            isAddEditQuestionDialogVisible = isAddEditVisible,
            questionToEdit = toEdit,
            questionToDelete = toDelete,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QuestionsUiState()
    )

    fun openQuestionsScreen(checklist: ChecklistEntity) {
        _selectedChecklistForQuestions.value = checklist
    }

    fun closeQuestionsScreen() {
        _selectedChecklistForQuestions.value = null
    }

    fun openAddQuestionDialog() {
        _questionToEdit.value = null; _isAddEditQuestionDialogVisible.value = true
    }

    fun openEditQuestionDialog(question: ChecklistQuestionEntity) {
        _questionToEdit.value = question; _isAddEditQuestionDialogVisible.value = true
    }

    fun dismissAddEditQuestionDialog() {
        _isAddEditQuestionDialogVisible.value = false; _questionToEdit.value = null
    }

    fun requestDeleteQuestion(question: ChecklistQuestionEntity) {
        _questionToDelete.value = question
    }

    fun dismissDeleteQuestionDialog() {
        _questionToDelete.value = null
    }

    fun confirmDeleteQuestion() {
        val target = _questionToDelete.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            questionRepository.deleteChecklistQuestion(target)
            _questionToDelete.value = null
        }
    }

    fun addOrUpdateQuestion(
        title: String,
        description: String?,
        type: String,
        options: String?,
        required: Boolean,
        minValue: Double?,
        maxValue: Double?,
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
                    description = description?.ifBlank { null },
                    type = type,
                    options = options?.ifBlank { null },
                    required = required,
                    sortno = nextSortNo,
                    minValue = minValue,
                    maxValue = maxValue,
                )
                questionRepository.insertChecklistQuestion(newQuestion)
            } else {
                val updated = currentEdit.copy(
                    title = title,
                    description = description?.ifBlank { null },
                    type = type,
                    options = options?.ifBlank { null },
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

    class Factory(
        private val questionRepository: ChecklistQuestionRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QuestionsViewModel(questionRepository) as T
        }
    }
}
