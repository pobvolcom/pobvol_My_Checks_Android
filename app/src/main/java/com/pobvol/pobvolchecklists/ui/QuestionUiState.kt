package com.pobvol.pobvolchecklists.ui

import com.pobvol.pobvolchecklists.data.local.AnswerTypeEntity
import com.pobvol.pobvolchecklists.data.local.CategoryEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistQuestionEntity

data class QuestionUiState(
    val checklistquestions: List<ChecklistQuestionEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val answertypes: List<AnswerTypeEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedAnswerType: String? = null,
    val isAddEditDialogVisible: Boolean = false,
    val checklistQuestionToEdit: ChecklistQuestionEntity? = null,
    val checklistQuestionToDelete: ChecklistQuestionEntity? = null,
    val selectedChecklistForQuestions: ChecklistEntity? = null,
    val questionsForSelectedChecklist: List<ChecklistQuestionEntity> = emptyList(),
    val isAddEditQuestionDialogVisible: Boolean = false,
    val questionToEdit: ChecklistQuestionEntity? = null,
    val questionToDelete: ChecklistQuestionEntity? = null,
    val userMessage: String? = null,
)
