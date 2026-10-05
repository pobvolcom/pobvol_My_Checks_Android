package com.pobvol.pobvolchecklists.ui

import com.pobvol.pobvolchecklists.data.local.CategoryEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistAnswerEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistQuestionEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistSubmissionEntity
import com.pobvol.pobvolchecklists.data.local.LanguageEntity
import com.pobvol.pobvolchecklists.data.repository.UserSettings

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
    val userSettings: UserSettings = UserSettings(),
    val availableLanguages: List<LanguageEntity> = emptyList(),
    val isSettingsDialogVisible: Boolean = false,
    val categories: List<CategoryEntity> = emptyList(),
    val selectedChecklistToAnswer: ChecklistEntity? = null,
    val submissions: List<ChecklistSubmissionEntity> = emptyList(),
    val answers: List<ChecklistAnswerEntity> = emptyList(),
    val isSubmissionsOverviewVisible: Boolean = false,
    val selectedSubmissionToEdit: ChecklistSubmissionEntity? = null,
    val submissionToDelete: ChecklistSubmissionEntity? = null,
    val isChecklistListVisible: Boolean = false,
    val selectedSubmissionStatus: String? = null,
    val scannedNfcTag: String? = null,
)
