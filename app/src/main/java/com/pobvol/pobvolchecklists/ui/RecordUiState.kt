package com.pobvol.pobvolchecklists.ui

import com.pobvol.pobvolchecklists.data.local.RecordEntity

data class RecordUiState(
    val records: List<RecordEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val isAddEditDialogVisible: Boolean = false,
    val recordToEdit: RecordEntity? = null,
    val recordToDelete: RecordEntity? = null,
    val userMessage: String? = null,
)
