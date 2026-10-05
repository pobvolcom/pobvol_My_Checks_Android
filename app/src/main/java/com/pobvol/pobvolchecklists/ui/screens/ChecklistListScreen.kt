package com.pobvol.pobvolchecklists.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.FactCheck
import androidx.compose.material.icons.automirrored.rounded.NoteAdd
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pobvol.pobvolchecklists.data.local.CategoryEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistEntity
import com.pobvol.pobvolchecklists.data.repository.ThemeMode
import com.pobvol.pobvolchecklists.ui.ChecklistUiState
import com.pobvol.pobvolchecklists.ui.components.AddEditChecklistDialog
import com.pobvol.pobvolchecklists.ui.components.ChecklistItem
import com.pobvol.pobvolchecklists.ui.components.DeleteChecklistConfirmationDialog
import com.pobvol.pobvolchecklists.ui.components.SettingsDialog
import com.pobvol.pobvolchecklists.ui.theme.pobvolchecklistsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistListScreen(
    uiState: ChecklistUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onAddClick: () -> Unit,
    onSelectChecklist: (ChecklistEntity) -> Unit = {},
    onQuestionsClick: (ChecklistEntity) -> Unit,
    onEditClick: (ChecklistEntity) -> Unit,
    onDeleteClick: (ChecklistEntity) -> Unit,
    onDismissAddEditDialog: () -> Unit,
    onSaveChecklist: (title: String, language: String, description: String, category: String, icon: String) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDelete: () -> Unit,
    onUserMessageShown: () -> Unit,
    onBackClick: () -> Unit = {},
    /*onOpenSubmissionsOverviewClick: () -> Unit = {},*/
    /*onOpenSettingsClick: () -> Unit = {},*/
    onDismissSettingsDialog: () -> Unit = {},
    onThemeModeSelected: (ThemeMode) -> Unit = {},
    onLanguageSelected: (String) -> Unit = {},
    onUserNameChanged: (String) -> Unit = {},
    onAddCategory: (String) -> Unit = {},
    onDeleteCategory: (CategoryEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isSearchActive by remember { mutableStateOf(false) }
    var isAddCategoryDialogVisible by remember { mutableStateOf(false) }

    val categories = remember(uiState.categories) {
        val dbCategoryTitles = uiState.categories.map { it.title.ifBlank { it.category } }
        if (dbCategoryTitles.isEmpty()) {
            listOf("All", "UVV", "Health")
        } else {
            listOf("All") + dbCategoryTitles
        }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onUserMessageShown()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text =
                                /*if (uiState.userSettings.userName.isNotBlank()) {
                                "Checklists (${uiState.userSettings.userName})"
                            } else {
                                "Checklists Overview"
                            },*/
                                "Checklists Overview",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back to dashboard",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) {
                                onSearchQueryChange("")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Rounded.Close else Icons.Rounded.Search,
                            contentDescription = if (isSearchActive) "Close search" else "Search checklists"
                        )
                    }
                    /*IconButton(onClick = onOpenSubmissionsOverviewClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.FactCheck,
                            contentDescription = "Submissions Overview"
                        )
                    }*/
                    /*IconButton(onClick = onOpenSettingsClick) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings"
                        )
                    }*/
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add new checklist"
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search by title, description, or category...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Clear search query"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AssistChip(
                    onClick = { isAddCategoryDialogVisible = true },
                    label = { Text("+ Category") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                )

                categories.forEach { category ->
                    val isSelected = if (category == "All") {
                        uiState.selectedCategory == null || uiState.selectedCategory == "All"
                    } else {
                        uiState.selectedCategory.equals(category, ignoreCase = true)
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (category == "All") {
                                onCategoryFilterChange(null)
                            } else {
                                onCategoryFilterChange(category)
                            }
                        },
                        label = { Text(category) },
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }

            if (isAddCategoryDialogVisible) {
                var newCategoryName by remember { mutableStateOf("") }
                var isError by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { isAddCategoryDialogVisible = false },
                    title = { Text("Add New Category", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newCategoryName,
                                onValueChange = {
                                    newCategoryName = it
                                    if (it.isNotBlank()) isError = false
                                },
                                label = { Text("Category Name *") },
                                isError = isError,
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (isError) {
                                Text("Category name cannot be empty", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newCategoryName.isBlank()) {
                                    isError = true
                                } else {
                                    onAddCategory(newCategoryName.trim())
                                    isAddCategoryDialogVisible = false
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { isAddCategoryDialogVisible = false },
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text("Cancel")
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                )
            }

            if (uiState.checklists.isEmpty()) {
                EmptyStateView(
                    isFiltering = uiState.searchQuery.isNotEmpty() || (uiState.selectedCategory != null && uiState.selectedCategory != "All"),
                    onAddClick = onAddClick,
                    onClearFilter = {
                        onSearchQueryChange("")
                        onCategoryFilterChange(null)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.checklists,
                        key = { it.id }
                    ) { checklist ->
                        ChecklistItem(
                            checklist = checklist,
                            onSelectChecklist = onSelectChecklist,
                            onQuestionsClick = onQuestionsClick,
                            onEditClick = onEditClick,
                            onDeleteClick = onDeleteClick
                        )
                    }
                }
            }
        }

        if (uiState.isAddEditDialogVisible) {
            AddEditChecklistDialog(
                checklistToEdit = uiState.checklistToEdit,
                availableCategories = uiState.categories,
                onDismiss = onDismissAddEditDialog,
                onSave = onSaveChecklist
            )
        }

        if (uiState.checklistToDelete != null) {
            DeleteChecklistConfirmationDialog(
                checklist = uiState.checklistToDelete,
                onDismiss = onDismissDeleteDialog,
                onConfirmDelete = onConfirmDelete
            )
        }

        if (uiState.isSettingsDialogVisible) {
            SettingsDialog(
                userSettings = uiState.userSettings,
                languages = uiState.availableLanguages,
                categories = uiState.categories,
                onThemeModeSelected = onThemeModeSelected,
                onLanguageSelected = onLanguageSelected,
                onUserNameChanged = onUserNameChanged,
                onAddCategory = onAddCategory,
                onDeleteCategory = onDeleteCategory,
                onDismissRequest = onDismissSettingsDialog,
            )
        }
    }
}

@Composable
private fun EmptyStateView(
    isFiltering: Boolean,
    onAddClick: () -> Unit,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isFiltering) Icons.Rounded.SearchOff else Icons.AutoMirrored.Rounded.NoteAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isFiltering) "No matching checklists found" else "No checklists stored yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isFiltering) {
                    "Try searching for something else or clear active filters."
                } else {
                    "Your SQLite database is currently empty. Tap below to create your first checklist!"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isFiltering) {
                Button(
                    onClick = onClearFilter,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear Filters")
                }
            } else {
                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Add Checklist")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChecklistListScreenPreview() {
    pobvolchecklistsTheme {
        ChecklistListScreen(
            uiState = ChecklistUiState(
                checklists = listOf(
                    ChecklistEntity(
                        id = 1,
                        title = "Project Launch Checklist",
                        language = "en",
                        description = "Review unit tests, perform UI audit, and verify SQLite migrations.",
                        category = "Work",
                        icon = "Null",
                        timestamp = System.currentTimeMillis()
                    ),
                    ChecklistEntity(
                        id = 2,
                        title = "Grocery Shopping",
                        language = "en",
                        description = "Apples, Bananas, Milk, Coffee beans",
                        category = "Personal",
                        icon = "Null",
                        timestamp = System.currentTimeMillis() - 3600000
                    )
                )
            ),
            onSearchQueryChange = {},
            onCategoryFilterChange = {},
            onAddClick = {},
            onQuestionsClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onDismissAddEditDialog = {},
            onSaveChecklist = { _, _, _, _, _ -> },
            onDismissDeleteDialog = {},
            onConfirmDelete = {},
            onUserMessageShown = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChecklistListScreenEmptyPreview() {
    pobvolchecklistsTheme {
        ChecklistListScreen(
            uiState = ChecklistUiState(checklists = emptyList()),
            onSearchQueryChange = {},
            onCategoryFilterChange = {},
            onAddClick = {},
            onQuestionsClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onDismissAddEditDialog = {},
            onSaveChecklist = { _, _, _, _, _ -> },
            onDismissDeleteDialog = {},
            onConfirmDelete = {},
            onUserMessageShown = {}
        )
    }
}
