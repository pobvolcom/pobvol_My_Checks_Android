package com.pobvol.pobvolchecklists.ui

import com.pobvol.pobvolchecklists.data.local.ChecklistEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistQuestionEntity

data class ChecklistUiState(
    val checklists: List<ChecklistEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val isAddEditDialogVisible: Boolean = false,
    val checklistToEdit: ChecklistEntity? = null,
    val checklistToDelete: ChecklistEntity? = null,
    val selectedChecklistForQuestions: ChecklistEntity? = null,
    val questionsForSelectedChecklist: List<ChecklistQuestionEntity> = emptyList(),
    val isAddEditQuestionDialogVisible: Boolean = false,
    val questionToEdit: ChecklistQuestionEntity? = null,
    val questionToDelete: ChecklistQuestionEntity? = null,
    val userMessage: String? = null,
)
