package com.pobvol.pobvolchecklists.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.FactCheck
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pobvol.pobvolchecklists.data.local.ChecklistSubmissionEntity
import com.pobvol.pobvolchecklists.ui.ChecklistUiState
import com.pobvol.pobvolchecklists.ui.theme.pobvolchecklistsTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionsOverviewScreen(
    uiState: ChecklistUiState,
    onSearchQueryChange: (String) -> Unit,
    onStatusFilterChange: (String?) -> Unit = {},
    onClearChecklistFilter: () -> Unit = {},
    onBackClick: () -> Unit,
    onEditSubmissionClick: (ChecklistSubmissionEntity) -> Unit,
    onDeleteSubmissionClick: (ChecklistSubmissionEntity) -> Unit,
    onDismissDeleteSubmissionDialog: () -> Unit,
    onConfirmDeleteSubmission: () -> Unit,
    onUserMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val checklistMap = remember(uiState.checklists) {
        uiState.checklists.associateBy { it.id }
    }

    var isSearchActive by remember { mutableStateOf(false) }

    /*val categories = remember(uiState.categories) {
        val dbCategoryTitles = uiState.categories.map { it.title.ifBlank { it.category } }
        if (dbCategoryTitles.isEmpty()) {
            listOf("All", "UVV", "Health")
        } else {
            listOf("All") + dbCategoryTitles
        }
    }*/

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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            /*text = uiState.selectedChecklistForSubmissions?.title ?: "Submissions Overview",*/
                            text = "Submissions Overview",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (uiState.selectedChecklistForSubmissions != null) {
                            Text(
                                text = "Filtered by selected checklist",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
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
                    containerColor = MaterialTheme.colorScheme.surface,
                ),

            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (isSearchActive) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search submissions...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            if (uiState.selectedChecklistForSubmissions != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AssistChip(
                        onClick = onClearChecklistFilter,
                        label = { Text("Filter: ${uiState.selectedChecklistForSubmissions.title}") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear checklist filter",
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }

            val statuses = listOf("All", "COMPLETED", "IN_PROGRESS", "PENDING")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                statuses.forEach { status ->
                    val isSelected = if (status == "All") {
                        uiState.selectedSubmissionStatus == null || uiState.selectedSubmissionStatus.equals("All", ignoreCase = true)
                    } else {
                        uiState.selectedSubmissionStatus.equals(status, ignoreCase = true)
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onStatusFilterChange(if (status == "All") null else status)
                        },
                        label = { Text(status) },
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            ) {
                if (uiState.submissions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.FactCheck,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(80.dp),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No submissions recorded yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Select a checklist from the list to answer questions and create submissions.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = uiState.submissions,
                            key = { it.id },
                        ) { submission ->
                            val checklistTitle = checklistMap[submission.checklistid]?.title ?: "Checklist #${submission.checklistid}"
                            SubmissionItem(
                                submission = submission,
                                checklistTitle = checklistTitle,
                                onEditClick = { onEditSubmissionClick(submission) },
                                onDeleteClick = { onDeleteSubmissionClick(submission) },
                            )
                        }
                    }
                }
            }
        }

        if (uiState.submissionToDelete != null) {
            DeleteSubmissionConfirmationDialog(
                submission = uiState.submissionToDelete,
                onDismiss = onDismissDeleteSubmissionDialog,
                onConfirmDelete = onConfirmDeleteSubmission,
            )
        }
    }
}

@Composable
fun SubmissionItem(
    submission: ChecklistSubmissionEntity,
    checklistTitle: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = checklistTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                AssistChip(
                    onClick = onEditClick,
                    label = { Text(submission.status) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f),
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                    shape = RoundedCornerShape(12.dp),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Inspector: ${submission.inspector ?: "Anonymous"}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
            )

            if (!submission.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notes: ${submission.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Check Date: ${submission.date}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = formatSubmissionTimestamp(submission.timestamp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    )
                }

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit submission",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete submission",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteSubmissionConfirmationDialog(
    submission: ChecklistSubmissionEntity,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Submission?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete this submission? All associated answers will also be removed.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp),
    )
}

private fun formatSubmissionTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
fun SubmissionsOverviewScreenPreview() {
    pobvolchecklistsTheme {
        SubmissionsOverviewScreen(
            uiState = ChecklistUiState(
                submissions = listOf(
                    ChecklistSubmissionEntity(
                        id = 1,
                        checklistid = 1,
                        status = "COMPLETED",
                        inspector = "John Doe",
                        notes = "All safety guards passed inspection.",
                        date = "Today()",
                        timestamp = System.currentTimeMillis(),
                    )
                ),
            ),
            onSearchQueryChange = {},
            /*onCategoryFilterChange = {},*/
            onUserMessageShown = {},
            onBackClick = {},
            onEditSubmissionClick = {},
            onDeleteSubmissionClick = {},
            onDismissDeleteSubmissionDialog = {},
            onConfirmDeleteSubmission = {},
        )
    }
}
