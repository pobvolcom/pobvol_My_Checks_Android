package com.pobvol.pobvolchecklists

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pobvol.pobvolchecklists.data.local.AppDatabase
import com.pobvol.pobvolchecklists.data.repository.ChecklistQuestionRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.ChecklistRepositoryImpl
import com.pobvol.pobvolchecklists.ui.ChecklistViewModel
import com.pobvol.pobvolchecklists.ui.screens.ChecklistListScreen
import com.pobvol.pobvolchecklists.ui.screens.ChecklistQuestionsScreen
import com.pobvol.pobvolchecklists.ui.theme.pobvolchecklistsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ChecklistRepositoryImpl(database.checklistDao())
        val questionRepository = ChecklistQuestionRepositoryImpl(database.checklistquestionDao())

        setContent {
            pobvolchecklistsTheme {
                val viewModel: ChecklistViewModel = viewModel(
                    factory = ChecklistViewModel.Factory(repository, questionRepository),
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                val selectedChecklist = uiState.selectedChecklistForQuestions
                if (selectedChecklist != null) {
                    ChecklistQuestionsScreen(
                        checklist = selectedChecklist,
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
                        onQuestionsClick = viewModel::openQuestionsForChecklist,
                        onEditClick = viewModel::openEditDialog,
                        onDeleteClick = viewModel::requestDeleteConfirmation,
                        onDismissAddEditDialog = viewModel::dismissAddEditDialog,
                        onSaveChecklist = viewModel::saveChecklist,
                        onDismissDeleteDialog = viewModel::dismissDeleteConfirmation,
                        onConfirmDelete = viewModel::confirmDelete,
                        onUserMessageShown = viewModel::userMessageShown,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
