package com.pobvol.mychecks.ui.screens

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
import androidx.compose.material.icons.automirrored.rounded.NoteAdd
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pobvol.mychecks.R
import com.pobvol.mychecks.data.local.CategoryEntity
import com.pobvol.mychecks.data.local.ChecklistEntity
import com.pobvol.mychecks.data.repository.ThemeMode
import com.pobvol.mychecks.ui.ChecklistUiState
import com.pobvol.mychecks.ui.components.AddEditChecklistDialog
import com.pobvol.mychecks.ui.components.ChecklistItem
import com.pobvol.mychecks.ui.components.DeleteChecklistConfirmationDialog
import com.pobvol.mychecks.ui.components.SettingsDialog
import com.pobvol.mychecks.ui.theme.mychecksTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistListScreen(
    uiState: ChecklistUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onAddClick: () -> Unit,
    onSelectChecklist: (ChecklistEntity) -> Unit = {},
    onFillClick: (ChecklistEntity) -> Unit = {},
    onQuestionsClick: (ChecklistEntity) -> Unit,
    onEditClick: (ChecklistEntity) -> Unit,
    onDeleteClick: (ChecklistEntity) -> Unit,
    onDismissAddEditDialog: () -> Unit,
    onSaveChecklist: (title: String, language: String, description: String, category: String, icon: String) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDelete: () -> Unit,
    onUserMessageShown: () -> Unit,
    onBackClick: () -> Unit = {},
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
                        Text(
                            text = stringResource(R.string.checklists),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back_to_dashboard),
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
                            contentDescription = stringResource(R.string.search)
                        )
                    }
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
                    contentDescription = stringResource(R.string.add_new_checklist)
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
                    placeholder = { Text(stringResource(R.string.search_checklists_placeholder)) },
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
                                    contentDescription = stringResource(R.string.clear_search)
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

                /* Button to add new categories has been disabled by author. Creates errors!
                AssistChip(
                    onClick = { isAddCategoryDialogVisible = true },
                    label = { Text(stringResource(R.string.add_category)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                )
                 */

            }

            if (isAddCategoryDialogVisible) {
                var newCategoryName by remember { mutableStateOf("") }
                var isError by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { isAddCategoryDialogVisible = false },
                    title = { Text(stringResource(R.string.add_new_category), fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newCategoryName,
                                onValueChange = {
                                    newCategoryName = it
                                    if (it.isNotBlank()) isError = false
                                },
                                label = { Text(stringResource(R.string.category_name)) },
                                isError = isError,
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (isError) {
                                Text(stringResource(R.string.category_name_empty), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
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
                            Text(stringResource(R.string.add))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { isAddCategoryDialogVisible = false },
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(stringResource(R.string.cancel))
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
                            onSelectChecklist = onFillClick,
                            onFillClick = onSelectChecklist,
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
                text = if (isFiltering) stringResource(R.string.no_matching_checklists) else stringResource(R.string.no_checklists_found),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isFiltering) {
                    "Try searching for something else or clear active filters."
                } else {
                    stringResource(R.string.empty_checklists_hint)
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
                    Text(stringResource(R.string.clear_filters))
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
                    Text(stringResource(R.string.add_checklist))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChecklistListScreenPreview() {
    mychecksTheme {
        ChecklistListScreen(
            uiState = ChecklistUiState(
                checklists = listOf(
                    ChecklistEntity(
                        id = 1,
                        title = "UVV Safety Inspection",
                        language = "en",
                        description = "Monthly safety check for equipment.",
                        category = "UVV",
                        icon = null
                    ),
                    ChecklistEntity(
                        id = 2,
                        title = "Health & Hygiene Check",
                        language = "en",
                        description = "Daily hygiene protocol.",
                        category = "Health",
                        icon = null
                    )
                ),
                categories = listOf(
                    CategoryEntity(id = 1, category = "UVV", title = "UVV"),
                    CategoryEntity(id = 2, category = "Health", title = "Health")
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
            onUserMessageShown = {},
        )
    }
}
