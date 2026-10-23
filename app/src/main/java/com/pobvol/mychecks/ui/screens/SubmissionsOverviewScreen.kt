package com.pobvol.mychecks.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pobvol.mychecks.R
import com.pobvol.mychecks.data.local.ChecklistSubmissionEntity
import com.pobvol.mychecks.ui.ChecklistUiState
import com.pobvol.mychecks.ui.components.PdfSettingsDialog
import com.pobvol.mychecks.ui.theme.mychecksTheme
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
    onOpenHelpClick: () -> Unit = {},
    onOpenPdfSettingsClick: () -> Unit = {},
    onDismissPdfSettingsDialog: () -> Unit = {},
    onPdfHeaderTitleChanged: (String) -> Unit = {},
    onPdfIncludeChecklistTitleChanged: (Boolean) -> Unit = {},
    onPdfIncludeDateChanged: (Boolean) -> Unit = {},
    onPdfIncludeInspectorChanged: (Boolean) -> Unit = {},
    onPdfIncludeStatusChanged: (Boolean) -> Unit = {},
    onPdfIncludeNotesChanged: (Boolean) -> Unit = {},
    onPdfIncludeTimestampChanged: (Boolean) -> Unit = {},
    onPdfIncludePageNumberChanged: (Boolean) -> Unit = {},
    onPdfIncludeUnansweredQuestionsChanged: (Boolean) -> Unit = {},
    onPdfIncludeQuestionTitleChanged: (Boolean) -> Unit = {},
    onPdfIncludeAnswerValueChanged: (Boolean) -> Unit = {},
    onPdfIncludeAnswerNotesChanged: (Boolean) -> Unit = {},
    onPdfIncludeAnswerPictureChanged: (Boolean) -> Unit = {},
    onPdfCompactAnswerLayoutChanged: (Boolean) -> Unit = {},
    onPdfFooterTextChanged: (String) -> Unit = {},
    onPdfTemplateVersionChanged: (String) -> Unit = {},
    onExportPdfClick: (ChecklistSubmissionEntity) -> Unit = {},
    onExportSqlClick: (ChecklistSubmissionEntity) -> Unit = {},
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

    BackHandler(onBack = onBackClick)

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
                            text = stringResource(R.string.submissions),
                            fontWeight = FontWeight.Bold,
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
                    IconButton(onClick = onOpenHelpClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Help,
                            contentDescription = stringResource(R.string.help),
                        )
                    }
                    IconButton(onClick = onOpenPdfSettingsClick) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.configure_pdf_settings),
                        )
                    }
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
                            contentDescription = if (isSearchActive) stringResource(R.string.clear_search) else stringResource(R.string.search)
                        )
                    }
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
                    placeholder = { Text(stringResource(R.string.search_submissions)) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.clear_search))
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
                        label = { Text(stringResource(R.string.filter_prefix, uiState.selectedChecklistForSubmissions.title)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.clear_checklist_filter),
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }

            //val statuses = listOf("All", "COMPLETED", "IN_PROGRESS", "PENDING")
            val statuses = listOf("All", "COMPLETED", "IN_PROGRESS")

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

                    val labelText = when (status) {
                        "All" -> stringResource(R.string.status_all)
                        "COMPLETED" -> stringResource(R.string.status_completed)
                        "IN_PROGRESS" -> stringResource(R.string.status_in_progress)
                        "PENDING" -> stringResource(R.string.status_pending)
                        else -> status
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onStatusFilterChange(if (status == "All") null else status)
                        },
                        label = { Text(labelText) },
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
                                text = stringResource(R.string.no_submissions_recorded),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.no_submissions_hint),
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
                                onExportPdfClick = { onExportPdfClick(submission) },
                                onExportSqlClick = { onExportSqlClick(submission) },
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

        if (uiState.isPdfSettingsVisible) {
            PdfSettingsDialog(
                userSettings = uiState.userSettings,
                onPdfHeaderTitleChanged = onPdfHeaderTitleChanged,
                onPdfIncludeChecklistTitleChanged = onPdfIncludeChecklistTitleChanged,
                onPdfIncludeDateChanged = onPdfIncludeDateChanged,
                onPdfIncludeInspectorChanged = onPdfIncludeInspectorChanged,
                onPdfIncludeStatusChanged = onPdfIncludeStatusChanged,
                onPdfIncludeNotesChanged = onPdfIncludeNotesChanged,
                onPdfIncludeTimestampChanged = onPdfIncludeTimestampChanged,
                onPdfIncludePageNumberChanged = onPdfIncludePageNumberChanged,
                onPdfIncludeUnansweredQuestionsChanged = onPdfIncludeUnansweredQuestionsChanged,
                onPdfIncludeQuestionTitleChanged = onPdfIncludeQuestionTitleChanged,
                onPdfIncludeAnswerValueChanged = onPdfIncludeAnswerValueChanged,
                onPdfIncludeAnswerNotesChanged = onPdfIncludeAnswerNotesChanged,
                onPdfIncludeAnswerPictureChanged = onPdfIncludeAnswerPictureChanged,
                onPdfCompactAnswerLayoutChanged = onPdfCompactAnswerLayoutChanged,
                onPdfFooterTextChanged = onPdfFooterTextChanged,
                onPdfTemplateVersionChanged = onPdfTemplateVersionChanged,
                onDismissRequest = onDismissPdfSettingsDialog,
            )
        }
    }
}

@Composable
fun SubmissionItem(
    submission: ChecklistSubmissionEntity,
    checklistTitle: String,
    onExportPdfClick: () -> Unit = {},
    onExportSqlClick: () -> Unit = {},
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
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

                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp),
                )

                /*
                val inspectorStr =
                    submission.inspector?.ifBlank { null } ?: stringResource(R.string.anonymous)
                */

                Text(
                    text = " " + submission.date + " / " + checklistTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

            }

            if (!submission.notes.isNullOrBlank()) {
                // Spacer(modifier = Modifier.height(4.dp))
                Text(
                    //text = stringResource(R.string.notes_label, submission.notes),
                    text = submission.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    /* color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f), */
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Spacer(modifier = Modifier.height(12.dp))
            val submissionStatusText = when (submission.status) {
                "All" -> stringResource(R.string.status_all)
                "COMPLETED" -> stringResource(R.string.status_completed)
                "IN_PROGRESS" -> stringResource(R.string.status_in_progress)
                "PENDING" -> stringResource(R.string.status_pending)
                else -> submission.status
            }
            Text(
                //text = stringResource(R.string.notes_label, submission.notes),
                text = submissionStatusText,
                style = MaterialTheme.typography.bodyMedium,
                /* color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f), */
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            // Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left group: Edit and Delete
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.edit_submission),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Right group: PDF report
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onExportPdfClick) {
                        Icon(
                            imageVector = Icons.Rounded.PictureAsPdf,
                            contentDescription = stringResource(R.string.pdf_report),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(28.dp)
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
                text = stringResource(R.string.delete_submission_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.delete_submission_message),
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
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
    mychecksTheme {
        SubmissionsOverviewScreen(
            uiState = ChecklistUiState(
                submissions = listOf(
                    ChecklistSubmissionEntity(
                        id = "1",
                        checklistid = "1",
                        status = "COMPLETED",
                        inspector = "John Doe",
                        notes = "All safety guards passed inspection.",
                        date = "Today()",
                        timestamp = System.currentTimeMillis(),
                    )
                ),
            ),
            onSearchQueryChange = {},
            onUserMessageShown = {},
            onBackClick = {},
            onEditSubmissionClick = {},
            onDeleteSubmissionClick = {},
            onDismissDeleteSubmissionDialog = {},
            onConfirmDeleteSubmission = {},
        )
    }
}
