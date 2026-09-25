package com.pobvol.pobvolchecklists.ui

import com.pobvol.pobvolchecklists.data.local.ChecklistEntity
data class ChecklistUiState(
    val checklists: List<ChecklistEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val isAddEditDialogVisible: Boolean = false,
    val checklistToEdit: ChecklistEntity? = null,
    val checklistToDelete: ChecklistEntity? = null,
    val userMessage: String? = null,
)
