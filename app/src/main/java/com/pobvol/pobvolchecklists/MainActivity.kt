package com.pobvol.pobvolchecklists

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pobvol.pobvolchecklists.data.local.AppDatabase
import com.pobvol.pobvolchecklists.data.repository.CategoryRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.ChecklistAnswerRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.ChecklistQuestionRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.ChecklistRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.ChecklistSubmissionRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.LanguageRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.SettingsRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.ThemeMode
import com.pobvol.pobvolchecklists.ui.ChecklistViewModel
import com.pobvol.pobvolchecklists.ui.screens.ChecklistListScreen
import com.pobvol.pobvolchecklists.ui.screens.ChecklistQuestionsScreen
import com.pobvol.pobvolchecklists.ui.screens.FillChecklistScreen
import com.pobvol.pobvolchecklists.ui.screens.SubmissionDetailScreen
import com.pobvol.pobvolchecklists.ui.screens.SubmissionsOverviewScreen
import com.pobvol.pobvolchecklists.ui.theme.pobvolchecklistsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ChecklistRepositoryImpl(database.checklistDao())
        val questionRepository = ChecklistQuestionRepositoryImpl(database.checklistquestionDao())
        val languageRepository = LanguageRepositoryImpl(database.languageDao())
        val settingsRepository = SettingsRepositoryImpl(applicationContext)
        val categoryRepository = CategoryRepositoryImpl(database.categoryDao())
        val submissionRepository = ChecklistSubmissionRepositoryImpl(database.checklistsubmissionDao())
        val answerRepository = ChecklistAnswerRepositoryImpl(database.checklistanswerDao())

        setContent {
            val viewModel: ChecklistViewModel = viewModel(
                factory = ChecklistViewModel.Factory(
                    repository = repository,
                    questionRepository = questionRepository,
                    languageRepository = languageRepository,
                    settingsRepository = settingsRepository,
                    categoryRepository = categoryRepository,
                    submissionRepository = submissionRepository,
                    answerRepository = answerRepository,
                ),
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val darkTheme = when (uiState.userSettings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            pobvolchecklistsTheme(darkTheme = darkTheme) {
                val selectedSubmissionToEdit = uiState.selectedSubmissionToEdit
                val selectedChecklistToAnswer = uiState.selectedChecklistToAnswer
                val selectedChecklistForQuestions = uiState.selectedChecklistForQuestions

                if (selectedSubmissionToEdit != null) {
                    val checklist = uiState.checklists.find { it.id == selectedSubmissionToEdit.checklistid }
                    SubmissionDetailScreen(
                        submission = selectedSubmissionToEdit,
                        checklist = checklist,
                        uiState = uiState,
                        onBackClick = viewModel::closeEditSubmission,
                        onSaveSubmission = viewModel::updateSubmissionAndAnswers,
                        onUserMessageShown = viewModel::userMessageShown,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else if (uiState.isSubmissionsOverviewVisible) {
                    SubmissionsOverviewScreen(
                        uiState = uiState,
                        onBackClick = viewModel::closeSubmissionsOverview,
                        onEditSubmissionClick = viewModel::openEditSubmission,
                        onDeleteSubmissionClick = viewModel::requestDeleteSubmission,
                        onDismissDeleteSubmissionDialog = viewModel::dismissDeleteSubmission,
                        onConfirmDeleteSubmission = viewModel::confirmDeleteSubmission,
                        onUserMessageShown = viewModel::userMessageShown,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else if (selectedChecklistToAnswer != null) {
                    FillChecklistScreen(
                        checklist = selectedChecklistToAnswer,
                        uiState = uiState,
                        onBackClick = viewModel::closeFillChecklist,
                        onSubmit = viewModel::submitChecklistAnswers,
                        onUserMessageShown = viewModel::userMessageShown,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else if (selectedChecklistForQuestions != null) {
                    ChecklistQuestionsScreen(
                        checklist = selectedChecklistForQuestions,
                        uiState = uiState,
                        onBackClick = viewModel::closeQuestionsScreen,
                        onAddQuestionClick = viewModel::openAddQuestionDialog,
                        onEditQuestionClick = viewModel::openEditQuestionDialog,
                        onDeleteQuestionClick = viewModel::requestDeleteQuestionConfirmation,
                        onDismissAddEditQuestionDialog = viewModel::dismissAddEditQuestionDialog,
                        onSaveQuestion = viewModel::saveQuestion,
                        onDismissDeleteQuestionDialog = viewModel::dismissDeleteQuestionConfirmation,
                        onConfirmDeleteQuestion = viewModel::confirmDeleteQuestion,
                        onUserMessageShown = viewModel::userMessageShown,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    ChecklistListScreen(
                        uiState = uiState,
                        onSearchQueryChange = viewModel::onSearchQueryChange,
                        onCategoryFilterChange = viewModel::onCategoryFilterChange,
                        onAddClick = viewModel::openAddDialog,
                        onSelectChecklist = viewModel::openFillChecklist,
                        onQuestionsClick = viewModel::openQuestionsForChecklist,
                        onEditClick = viewModel::openEditDialog,
                        onDeleteClick = viewModel::requestDeleteConfirmation,
                        onDismissAddEditDialog = viewModel::dismissAddEditDialog,
                        onSaveChecklist = viewModel::saveChecklist,
                        onDismissDeleteDialog = viewModel::dismissDeleteConfirmation,
                        onConfirmDelete = viewModel::confirmDelete,
                        onUserMessageShown = viewModel::userMessageShown,
                        onOpenSubmissionsOverviewClick = viewModel::openSubmissionsOverview,
                        onOpenSettingsClick = viewModel::openSettingsDialog,
                        onDismissSettingsDialog = viewModel::dismissSettingsDialog,
                        onThemeModeSelected = viewModel::updateThemeMode,
                        onLanguageSelected = viewModel::updatePreferredLanguage,
                        onUserNameChanged = viewModel::updateUserName,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
