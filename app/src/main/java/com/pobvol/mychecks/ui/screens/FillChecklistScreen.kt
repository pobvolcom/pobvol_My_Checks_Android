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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.HelpCenter
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.QrCodeScanner
import com.pobvol.mychecks.ui.components.QrCodeScannerDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.pobvol.mychecks.data.local.ChecklistEntity
import com.pobvol.mychecks.data.local.ChecklistQuestionEntity
import com.pobvol.mychecks.ui.AnswerInput
import com.pobvol.mychecks.ui.ChecklistUiState
import com.pobvol.mychecks.ui.theme.mychecksTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillChecklistScreen(
    checklist: ChecklistEntity,
    uiState: ChecklistUiState,
    onBackClick: () -> Unit,
    onSubmit: (checklistId: Int, inspector: String?, notes: String?, date: String, answers: Map<Int, AnswerInput>) -> Unit,
    onUserMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var inspectorName by remember(uiState.userSettings.userName) {
        mutableStateOf(uiState.userSettings.userName)
    }
    var notes by remember { mutableStateOf("") }
    var checkDate by remember { mutableStateOf(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())) }
    val answersMap = remember { mutableStateMapOf<Int, AnswerInput>() }
    var validationError by remember { mutableStateOf<String?>(null) }
    var activeNfcQuestionId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onUserMessageShown()
        }
    }

    LaunchedEffect(uiState.scannedNfcTag) {
        uiState.scannedNfcTag?.let { tag ->
            if (tag.isNotBlank()) {
                val targetId = activeNfcQuestionId ?: uiState.questionsForSelectedChecklist
                    .find { it.type.lowercase() == "nfc" && (answersMap[it.id]?.value.isNullOrBlank()) }?.id
                    ?: uiState.questionsForSelectedChecklist.find { it.type.lowercase() == "nfc" }?.id

                if (targetId != null) {
                    val current = answersMap[targetId] ?: AnswerInput()
                    answersMap[targetId] = current.copy(value = tag)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Fill Out Checklist",
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = checklist.title,
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
                            contentDescription = "Back to checklists",
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
            // Header item: Inspector & Notes
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
                            value = inspectorName,
                            onValueChange = { inspectorName = it },
                            label = { Text("Inspector / User Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        OutlinedTextField(
                            value = checkDate,
                            onValueChange = { checkDate = it },
                            label = { Text("Check Date (dd.MM.yyyy)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes (Optional)") },
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.HelpCenter,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(64.dp),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No questions in this checklist",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Questions need to be added to this checklist before submitting answers.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
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
                        isActiveNfcTarget = (activeNfcQuestionId == question.id),
                        onActivateNfcScan = { activeNfcQuestionId = question.id },
                    )
                }
            }

            if (uiState.questionsForSelectedChecklist.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        ) {
                            if (validationError != null) {
                                Text(
                                    text = validationError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )
                            }
                            Button(
                                onClick = {
                                    val missingRequired = uiState.questionsForSelectedChecklist.filter { question ->
                                        question.required && (answersMap[question.id]?.value.isNullOrBlank())
                                    }
                                    if (missingRequired.isNotEmpty()) {
                                        validationError = "Please answer all required questions (${missingRequired.first().title})"
                                    } else {
                                        validationError = null
                                        onSubmit(checklist.id, inspectorName, notes, checkDate, answersMap.toMap())
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp),
                                )
                                Text("Save changes")
                            }
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun AnswerQuestionItem(
    question: ChecklistQuestionEntity,
    currentAnswer: String,
    onAnswerChanged: (String) -> Unit,
    currentNotes: String,
    onNotesChanged: (String) -> Unit,
    isActiveNfcTarget: Boolean = false,
    onActivateNfcScan: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = question.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                AssistChip(
                    onClick = {},
                    label = { Text(if (question.required) "Required" else "Optional") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (question.required) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer,
                        labelColor = if (question.required) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                    ),
                    shape = RoundedCornerShape(12.dp),
                )
            }

            if (!question.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = question.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (question.type.lowercase()) {
                "checkbox" -> {
                    val isChecked = currentAnswer.equals("true", ignoreCase = true) || currentAnswer.equals("yes", ignoreCase = true)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                onAnswerChanged(if (checked) "true" else "false")
                            },
                        )
                        Text(text = if (isChecked) "Yes / Checked" else "No / Unchecked")
                    }
                }

                "combobox" -> {
                    val optionsList = question.options?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        optionsList.forEach { option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                RadioButton(
                                    selected = currentAnswer.equals(option, ignoreCase = true),
                                    onClick = { onAnswerChanged(option) },
                                )
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }

                "number" -> {
                    OutlinedTextField(
                        value = currentAnswer,
                        onValueChange = onAnswerChanged,
                        label = { Text("Enter number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                "nfc" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = currentAnswer,
                            onValueChange = onAnswerChanged,
                            label = { Text("NFC Tag Content") },
                            placeholder = { Text("Scan or enter NFC tag data") },
                            singleLine = false,
                            minLines = 3,
                            maxLines = 6,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(
                            onClick = onActivateNfcScan,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(if (isActiveNfcTarget) "Ready: Tap NFC tag against back of device..." else "Scan NFC Tag for this Question")
                        }
                    }
                }

                "qr-code", "qrcode" -> {
                    var isQrScannerOpen by remember { mutableStateOf(false) }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = currentAnswer,
                            onValueChange = onAnswerChanged,
                            label = { Text("QR Code Content") },
                            placeholder = { Text("Scan or enter QR code data") },
                            singleLine = false,
                            minLines = 3,
                            maxLines = 6,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(
                            onClick = { isQrScannerOpen = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Text("Scan QR Code")
                        }
                    }

                    if (isQrScannerOpen) {
                        QrCodeScannerDialog(
                            onDismiss = { isQrScannerOpen = false },
                            onQrCodeScanned = { qrResult ->
                                onAnswerChanged(qrResult)
                                isQrScannerOpen = false
                            },
                        )
                    }
                }

                else -> { // text
                    OutlinedTextField(
                        value = currentAnswer,
                        onValueChange = onAnswerChanged,
                        label = { Text("Enter response") },
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = currentNotes,
                onValueChange = onNotesChanged,
                label = { Text("Answer Notes (Optional)") },
                minLines = 1,
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FillChecklistScreenPreview() {
    mychecksTheme {
        FillChecklistScreen(
            checklist = ChecklistEntity(id = 1, title = "UVV Safety Inspection", language = "en", icon = "Null"),
            uiState = ChecklistUiState(
                questionsForSelectedChecklist = listOf(
                    ChecklistQuestionEntity(
                        id = 1,
                        checklistid = 1,
                        title = "Are all guards in place?",
                        description = "Check machine covers.",
                        type = "checkbox",
                        options = null,
                        required = true,
                    ),
                    ChecklistQuestionEntity(
                        id = 2,
                        checklistid = 1,
                        title = "Select shift",
                        description = null,
                        type = "combobox",
                        options = "Morning, Evening, Night",
                        required = false,
                    ),
                ),
            ),
            onBackClick = {},
            onSubmit = { _, _, _, _, _ -> },
            onUserMessageShown = {},
        )
    }
}
