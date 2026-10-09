package com.pobvol.mychecks.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.HelpCenter
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalContext
import com.pobvol.mychecks.util.PdfExporter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pobvol.mychecks.data.local.ChecklistAnswerEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.pobvol.mychecks.data.local.ChecklistEntity
import com.pobvol.mychecks.data.local.ChecklistQuestionEntity
import com.pobvol.mychecks.data.local.ChecklistSubmissionEntity
import com.pobvol.mychecks.ui.AnswerInput
import com.pobvol.mychecks.ui.ChecklistUiState
import com.pobvol.mychecks.ui.theme.mychecksTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionDetailScreen(
    submission: ChecklistSubmissionEntity,
    checklist: ChecklistEntity?,
    uiState: ChecklistUiState,
    onBackClick: () -> Unit,
    onSaveSubmission: (updatedSubmission: ChecklistSubmissionEntity, updatedAnswers: Map<Int, AnswerInput>) -> Unit,
    onUserMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var inspectorName by remember(submission) { mutableStateOf(submission.inspector ?: "") }
    var status by remember(submission) { mutableStateOf(submission.status) }
    var statusExpanded by remember { mutableStateOf(false) }
    var notes by remember(submission) { mutableStateOf(submission.notes ?: "") }
    var checkDate by remember(submission) {
        mutableStateOf(submission.date.ifBlank { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()) })
    }

    val existingAnswersMap = remember(submission.id, uiState.answers) {
        uiState.answers.filter { it.submissionid == submission.id }.associate { it.questionid to AnswerInput(it.value, it.notes ?: "") }
    }

    val answersMap = remember(submission.id) {
        mutableStateMapOf<Int, AnswerInput>().apply {
            putAll(existingAnswersMap)
        }
    }

    val availableStatuses = listOf("COMPLETED", "IN_PROGRESS", "PENDING")

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
                            text = "Edit Submission",
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = checklist?.title ?: "Submission #${submission.id}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back to submissions",
                        )
                    }
                },
                actions = {
                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            PdfExporter.generateAndOpenSubmissionPdf(
                                context = context,
                                submission = submission,
                                checklist = checklist,
                                questions = uiState.questionsForSelectedChecklist,
                                answers = uiState.answers,
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PictureAsPdf,
                            contentDescription = "Export PDF Report",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Submission Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )

                        OutlinedTextField(
                            value = checkDate,
                            onValueChange = { checkDate = it },
                            label = { Text("Check Date (dd.MM.yyyy)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        ExposedDropdownMenuBox(
                            expanded = statusExpanded,
                            onExpandedChange = { statusExpanded = !statusExpanded },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedTextField(
                                value = status,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Status") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                    .fillMaxWidth(),
                            )

                            ExposedDropdownMenu(
                                expanded = statusExpanded,
                                onDismissRequest = { statusExpanded = false },
                            ) {
                                availableStatuses.forEach { itemStatus ->
                                    DropdownMenuItem(
                                        text = { Text(itemStatus) },
                                        onClick = {
                                            status = itemStatus
                                            statusExpanded = false
                                        },
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes") },
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            if (uiState.questionsForSelectedChecklist.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.HelpCenter,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(64.dp),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No questions found for this submission",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            } else {
                items(
                    items = uiState.questionsForSelectedChecklist,
                    key = { it.id },
                ) { question ->
                    AnswerQuestionItem(
                        question = question,
                        currentAnswer = answersMap[question.id]?.value ?: "",
                        onAnswerChanged = { value ->
                            val current = answersMap[question.id] ?: AnswerInput()
                            answersMap[question.id] = current.copy(value = value)
                        },
                        currentNotes = answersMap[question.id]?.notes ?: "",
                        onNotesChanged = { notesVal ->
                            val current = answersMap[question.id] ?: AnswerInput()
                            answersMap[question.id] = current.copy(notes = notesVal)
                        },
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val context = LocalContext.current
                            OutlinedButton(
                                onClick = {
                                    PdfExporter.generateAndOpenSubmissionPdf(
                                        context = context,
                                        submission = submission,
                                        checklist = checklist,
                                        questions = uiState.questionsForSelectedChecklist,
                                        answers = uiState.answers,
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PictureAsPdf,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 4.dp),
                                )
                                Text("PDF Report")
                            }

                            Button(
                                onClick = {
                                    val updatedSubmission = submission.copy(
                                        inspector = inspectorName.ifBlank { null },
                                        date = checkDate.ifBlank { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()) },
                                        status = status,
                                        notes = notes.ifBlank { null },
                                    )
                                    onSaveSubmission(updatedSubmission, answersMap.toMap())
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Save,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 4.dp),
                                )
                                Text("Save Changes")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubmissionDetailScreenPreview() {
    mychecksTheme {
        SubmissionDetailScreen(
            submission = ChecklistSubmissionEntity(
                id = 1,
                checklistid = 1,
                status = "COMPLETED",
                inspector = "John Doe",
                notes = "Sample notes",
                date = "Today()",
            ),
            checklist = ChecklistEntity(id = 1, title = "UVV Inspection", language = "en", icon = "Null"),
            uiState = ChecklistUiState(
                questionsForSelectedChecklist = listOf(
                    ChecklistQuestionEntity(
                        id = 1,
                        checklistid = 1,
                        title = "Guards checked?",
                        description = null,
                        type = "checkbox",
                        options = null,
                    )
                ),
                answers = listOf(
                    ChecklistAnswerEntity(id = 1, submissionid = 1, questionid = 1, value = "true", notes = "Checked ok")
                )
            ),
            onBackClick = {},
            onSaveSubmission = { _, _ -> },
            onUserMessageShown = {},
        )
    }
}
