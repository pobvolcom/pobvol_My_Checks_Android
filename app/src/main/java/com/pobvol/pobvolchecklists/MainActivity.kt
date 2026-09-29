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
import com.pobvol.pobvolchecklists.data.local.LanguageEntity
import com.pobvol.pobvolchecklists.data.repository.ChecklistRepositoryImpl
import com.pobvol.pobvolchecklists.ui.ChecklistViewModel
import com.pobvol.pobvolchecklists.ui.screens.ChecklistListScreen
import com.pobvol.pobvolchecklists.ui.theme.pobvolchecklistsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ChecklistRepositoryImpl(database.checklistDao())

        setContent {
            pobvolchecklistsTheme {
                val viewModel: ChecklistViewModel = viewModel(factory = ChecklistViewModel.Factory(repository))
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LanguageEntity(language = "de", title = "Deutsch")
                LanguageEntity(language = "es", title = "Español")
                LanguageEntity(language = "fr", title = "French")
                LanguageEntity(language = "en", title = "English")

                ChecklistListScreen(
                    uiState = uiState,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onCategoryFilterChange = viewModel::onCategoryFilterChange,
                    onAddClick = viewModel::openAddDialog,
                    onEditClick = viewModel::openEditDialog,
                    onDeleteClick = viewModel::requestDeleteConfirmation,
                    onDismissAddEditDialog = viewModel::dismissAddEditDialog,
                    onSaveChecklist = viewModel::saveChecklist,
                    onDismissDeleteDialog = viewModel::dismissDeleteConfirmation,
                    onConfirmDelete = viewModel::confirmDelete,
                    onUserMessageShown = viewModel::userMessageShown,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
