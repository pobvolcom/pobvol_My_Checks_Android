package com.pobvol.mychecks.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.rounded.HelpCenter
import androidx.compose.material.icons.automirrored.rounded.Note
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.pobvol.mychecks.R
import com.pobvol.mychecks.data.local.ChecklistEntity
import com.pobvol.mychecks.data.local.ChecklistQuestionEntity
import com.pobvol.mychecks.data.local.TextBlockEntity
import com.pobvol.mychecks.ui.AnswerInput
import com.pobvol.mychecks.ui.ChecklistUiState
import com.pobvol.mychecks.ui.components.QrCodeScannerDialog
import com.pobvol.mychecks.ui.theme.mychecksTheme
import com.pobvol.mychecks.util.PdfExporter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillChecklistScreen(
    checklist: ChecklistEntity,
    uiState: ChecklistUiState,
    onBackClick: () -> Unit,
    onOpenHelpClick: () -> Unit = {},
    onSubmit: (checklistId: String, inspector: String?, notes: String?, date: String, status: String, answers: Map<String, AnswerInput>) -> Unit,
    onUserMessageShown: () -> Unit,
    onNfcTagConsumed: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var inspectorName by remember(uiState.userSettings.userName) {
        mutableStateOf(uiState.userSettings.userName)
    }
    var notes by remember { mutableStateOf("") }
    var checkDate by remember { mutableStateOf(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())) }
    var status by remember { mutableStateOf("IN_PROGRESS") }

    val answersMap = remember(uiState.questionsForSelectedChecklist) {
        mutableStateMapOf<String, AnswerInput>().apply {
            uiState.questionsForSelectedChecklist.forEach { q ->
                if (q.type.lowercase() == "combobox") {
                    val options = q.options?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
                    if (options.isNotEmpty()) {
                        put(q.id, AnswerInput(value = options.first()))
                    }
                }
            }
        }
    }
    var validationError by remember { mutableStateOf<String?>(null) }
    var activeNfcQuestionId by remember { mutableStateOf<String?>(null) }

    BackHandler(onBack = onBackClick)

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
                ?: uiState.questionsForSelectedChecklist.firstOrNull()?.id

                if (targetId != null) {
                    val current = answersMap[targetId] ?: AnswerInput()
                    answersMap[targetId] = current.copy(value = tag)
                }
                onNfcTagConsumed()
            }
        }
    }

    val localizedStatus = when (status) {
        "COMPLETED" -> stringResource(R.string.status_completed)
        "IN_PROGRESS" -> stringResource(R.string.status_in_progress)
        "PENDING" -> stringResource(R.string.status_pending)
        else -> status
    }
    val titleWithStatus = "${checklist.title} ($localizedStatus)"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        //text = titleWithStatus,
                        text = "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Rounded.Cancel,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            var rangeError: String? = null
                            for (question in uiState.questionsForSelectedChecklist) {
                                val ans = answersMap[question.id]?.value ?: ""
                                if (question.type.lowercase() == "number" && ans.isNotBlank()) {
                                    val num = ans.toDoubleOrNull()
                                    if (num == null) {
                                        rangeError =
                                            "Question \"${question.title}\" requires a valid number"
                                        break
                                    }
                                    if (question.minValue != null && num < question.minValue) {
                                        rangeError =
                                            "Question \"${question.title}\" must be >= ${question.minValue}"
                                        break
                                    }
                                    if (question.maxValue != null && num > question.maxValue) {
                                        rangeError =
                                            "Question \"${question.title}\" must be <= ${question.maxValue}"
                                        break
                                    }
                                }
                            }

                            if (rangeError != null) {
                                validationError = rangeError
                            } else {
                                validationError = null
                                onSubmit(
                                    checklist.id,
                                    inspectorName,
                                    notes,
                                    checkDate,
                                    status,
                                    answersMap.toMap(),
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        Text(stringResource(R.string.save))
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
            // Header item: Title and subtitle in scroll area
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.fill_out_checklist),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = titleWithStatus,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (validationError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = validationError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            // Header item: Inspector & Notes Card
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
                            text = stringResource(R.string.submission_details_header),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )

                        OutlinedTextField(
                            value = inspectorName,
                            onValueChange = { inspectorName = it },
                            label = { Text(stringResource(R.string.inspector_user_name)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        var showDatePicker by remember { mutableStateOf(false) }

                        if (showDatePicker) {
                            val datePickerState = rememberDatePickerState(
                                initialSelectedDateMillis = remember(checkDate) {
                                    try {
                                        val sdf =
                                            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                        sdf.parse(checkDate)?.time ?: System.currentTimeMillis()
                                    } catch (_: Exception) {
                                        System.currentTimeMillis()
                                    }
                                }
                            )

                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            datePickerState.selectedDateMillis?.let { millis ->
                                                val calendar =
                                                    Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                                calendar.timeInMillis = millis
                                                val formattedDate = SimpleDateFormat(
                                                    "dd.MM.yyyy",
                                                    Locale.getDefault()
                                                ).format(calendar.time)
                                                checkDate = formattedDate
                                            }
                                            showDatePicker = false
                                        }
                                    ) {
                                        Text(stringResource(R.string.add))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDatePicker = false }) {
                                        Text(stringResource(R.string.cancel))
                                    }
                                }
                            ) {
                                DatePicker(state = datePickerState)
                            }
                        }

                        OutlinedTextField(
                            value = checkDate,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text(stringResource(R.string.check_date)) },
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(
                                        imageVector = Icons.Rounded.CalendarToday,
                                        contentDescription = stringResource(R.string.check_date),
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusProperties { canFocus = false }
                                .clickable { showDatePicker = true },
                        )

                        /*
                        ExposedDropdownMenuBox(
                            expanded = statusExpanded,
                            onExpandedChange = { statusExpanded = !statusExpanded },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedTextField(
                                value = status,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.status)) },
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
                        }*/

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(stringResource(R.string.notes_optional)) },
                            minLines = 3,
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
                                text = stringResource(R.string.no_questions_in_checklist),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = stringResource(R.string.questions_need_to_be_added),
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
                        currentImageUri = answersMap[question.id]?.imageUri
                            ?: answersMap[question.id]?.imagePath,
                        onImageChanged = { uri ->
                            val current = answersMap[question.id] ?: AnswerInput()
                            answersMap[question.id] = current.copy(
                                imageUri = uri,
                                imagePath = if (uri == null) null else current.imagePath
                            )
                        },
                        isActiveNfcTarget = (activeNfcQuestionId == question.id),
                        onActivateNfcScan = { activeNfcQuestionId = question.id },
                        textBlocks = uiState.textBlocks,
                        currentPictureSize = answersMap[question.id]?.pictureSize ?: "medium",
                        onPictureSizeChanged = { size ->
                            val current = answersMap[question.id] ?: AnswerInput()
                            answersMap[question.id] = current.copy(pictureSize = size)
                        },
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
                                    var rangeError: String? = null
                                    for (question in uiState.questionsForSelectedChecklist) {
                                        val ans = answersMap[question.id]?.value ?: ""
                                        if (question.type.lowercase() == "number" && ans.isNotBlank()) {
                                            val num = ans.toDoubleOrNull()
                                            if (num == null) {
                                                rangeError = "Question \"${question.title}\" requires a valid number"
                                                break
                                            }
                                            if (question.minValue != null && num < question.minValue) {
                                                rangeError = "Question \"${question.title}\" must be >= ${question.minValue}"
                                                break
                                            }
                                            if (question.maxValue != null && num > question.maxValue) {
                                                rangeError = "Question \"${question.title}\" must be <= ${question.maxValue}"
                                                break
                                            }
                                        }
                                    }

                                    if (rangeError != null) {
                                        validationError = rangeError
                                    } else {
                                        validationError = null
                                        onSubmit(
                                            checklist.id,
                                            inspectorName,
                                            notes,
                                            checkDate,
                                            status,
                                            answersMap.toMap(),
                                        )
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
                                Text(stringResource(R.string.save))
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
    currentImageUri: String? = null,
    onImageChanged: (String?) -> Unit = {},
    isActiveNfcTarget: Boolean = false,
    onActivateNfcScan: () -> Unit = {},
    textBlocks: List<TextBlockEntity> = emptyList(),
    currentPictureSize: String = "medium",
    onPictureSizeChanged: (String) -> Unit = {},
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
                    label = { Text(if (question.required) stringResource(R.string.required) else stringResource(R.string.optional)) },
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
                        Text(text = if (isChecked) stringResource(R.string.yes_checked) else stringResource(R.string.no_unchecked))
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

                "multiselect" -> {
                    val optionsList = question.options?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
                    val selectedSet = currentAnswer.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        optionsList.forEach { option ->
                            val isSelected = selectedSet.contains(option)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        val newSet = if (checked) {
                                            selectedSet + option
                                        } else {
                                            selectedSet - option
                                        }
                                        val orderedResult =
                                            optionsList.filter { newSet.contains(it) } + newSet.filter {
                                                !optionsList.contains(it)
                                            }
                                        onAnswerChanged(orderedResult.joinToString(", "))
                                    },
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
                    val labelText = buildString {
                        append(stringResource(R.string.enter_number))
                        if (question.minValue != null && question.maxValue != null) {
                            append(" (${question.minValue} - ${question.maxValue})")
                        } else if (question.minValue != null) {
                            append(" (Min: ${question.minValue})")
                        } else if (question.maxValue != null) {
                            append(" (Max: ${question.maxValue})")
                        }
                    }

                    OutlinedTextField(
                        value = currentAnswer,
                        onValueChange = onAnswerChanged,
                        label = { Text(labelText) },
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
                            label = { Text(stringResource(R.string.nfc_tag_content)) },
                            placeholder = { Text(stringResource(R.string.scan_or_enter_nfc)) },
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
                            Text(if (isActiveNfcTarget) stringResource(R.string.nfc_ready_tap) else stringResource(R.string.scan_nfc_button))
                        }
                    }
                }

                "qr-code", "qrcode" -> {
                    var isQrScannerOpen by remember { mutableStateOf(false) }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = currentAnswer,
                            onValueChange = onAnswerChanged,
                            label = { Text(stringResource(R.string.qr_code_content)) },
                            placeholder = { Text(stringResource(R.string.scan_or_enter_qr)) },
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
                            Text(stringResource(R.string.scan_qr_button))
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
                    var answerTextBlockMenuExpanded by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = currentAnswer,
                        onValueChange = onAnswerChanged,
                        label = { Text(stringResource(R.string.enter_response)) },
                        trailingIcon = {
                            if (textBlocks.isNotEmpty()) {
                                Box {
                                    IconButton(onClick = { answerTextBlockMenuExpanded = true }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Note,
                                            contentDescription = stringResource(R.string.text_blocks),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = answerTextBlockMenuExpanded,
                                        onDismissRequest = { answerTextBlockMenuExpanded = false },
                                    ) {
                                        textBlocks.forEachIndexed { index, block ->
                                            if (index > 0) {
                                                HorizontalDivider(
                                                    modifier = Modifier.padding(
                                                        horizontal = 8.dp,
                                                        vertical = 4.dp
                                                    ),
                                                    color = MaterialTheme.colorScheme.outlineVariant,
                                                )
                                            }
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = block.text,
                                                        maxLines = 3,
                                                        overflow = TextOverflow.Ellipsis,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                    )
                                                },
                                                onClick = {
                                                    answerTextBlockMenuExpanded = false
                                                    val updatedAnswer =
                                                        if (currentAnswer.isBlank()) block.text else "$currentAnswer\n${block.text}"
                                                    onAnswerChanged(updatedAnswer)
                                                },
                                                modifier = Modifier.padding(vertical = 4.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            var textBlockMenuExpanded by remember { mutableStateOf(false) }

            OutlinedTextField(
                value = currentNotes,
                onValueChange = onNotesChanged,
                label = { Text(stringResource(R.string.answer_notes_optional)) },
                trailingIcon = {
                    if (textBlocks.isNotEmpty()) {
                        Box {
                            IconButton(onClick = { textBlockMenuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Note,
                                    contentDescription = stringResource(R.string.text_blocks),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                            DropdownMenu(
                                expanded = textBlockMenuExpanded,
                                onDismissRequest = { textBlockMenuExpanded = false },
                            ) {
                                textBlocks.forEachIndexed { index, block ->
                                    if (index > 0) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 4.dp
                                            ),
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = block.text,
                                                maxLines = 3,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        },
                                        onClick = {
                                            textBlockMenuExpanded = false
                                            val updatedNotes =
                                                if (currentNotes.isBlank()) block.text else "$currentNotes\n${block.text}"
                                            onNotesChanged(updatedNotes)
                                        },
                                        modifier = Modifier.padding(vertical = 4.dp),
                                    )
                                }
                            }
                        }
                    }
                },
                minLines = 1,
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            val context = LocalContext.current
            var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

            val cameraLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                    if (success && tempCameraUri != null) {
                        onImageChanged(tempCameraUri.toString())
                    }
                }

            val galleryLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    if (uri != null) {
                        onImageChanged(uri.toString())
                    }
                }

            val permissionLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (isGranted) {
                        val photoFile =
                            File(context.cacheDir, "camera_pic_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            photoFile
                        )
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    }
                }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                if (!currentImageUri.isNullOrBlank()) {

                    //Picture (optionally)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.picture_optional),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    //Picture, Camera, Upload, Delete, Size S/M/L
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val imageModel: Any = remember(currentImageUri) {
                                if (currentImageUri.startsWith("content://") || currentImageUri.startsWith(
                                        "file://"
                                    )
                                ) {
                                    currentImageUri
                                } else {
                                    PdfExporter.getPictureFile(context, currentImageUri)
                                        ?: currentImageUri
                                }
                            }

                            AsyncImage(
                                model = imageModel,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = "PDF Size:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    val sizes =
                                        listOf("small" to "S", "medium" to "M", "large" to "L")
                                    sizes.forEach { (sizeCode, sizeLabel) ->
                                        val isSelected =
                                            currentPictureSize.equals(sizeCode, ignoreCase = true)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { onPictureSizeChanged(sizeCode) },
                                            label = {
                                                Text(
                                                    sizeLabel,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        //Column (verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {

                            //CAMERA
                            IconButton(
                                onClick =
                                    {
                                        if (ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.CAMERA
                                            ) == PackageManager.PERMISSION_GRANTED
                                        ) {
                                            val photoFile = File(
                                                context.cacheDir,
                                                "camera_pic_${System.currentTimeMillis()}.jpg"
                                            )
                                            val uri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                photoFile
                                            )
                                            tempCameraUri = uri
                                            cameraLauncher.launch(uri)
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CameraAlt,
                                    contentDescription = stringResource(R.string.camera),
                                    modifier = Modifier.weight(1f),
                                    //modifier = Modifier.padding(end = 4.dp),
                                )
                            }

                            //UPLOAD PICTURE
                            IconButton(
                                onClick =
                                    {
                                        galleryLauncher.launch("image/*")
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Upload,
                                    contentDescription = stringResource(R.string.upload),
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            //DELETE PICTURE
                            IconButton(
                                onClick =
                                    {
                                        onImageChanged(null)
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = stringResource(R.string.remove),
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            /*
                            Button(
                                onClick = {
                                    if (ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.CAMERA
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        val photoFile = File(
                                            context.cacheDir,
                                            "camera_pic_${System.currentTimeMillis()}.jpg"
                                        )
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            photoFile
                                        )
                                        tempCameraUri = uri
                                        cameraLauncher.launch(uri)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(R.string.take_photo))
                            }

                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(R.string.upload_image))
                            }

                             */
                             */
                        }

                        // Spacer(modifier = Modifier.weight(1f))

                        /*
                        OutlinedButton(
                            onClick = { onImageChanged(null) },
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(stringResource(R.string.remove))
                        }
                        */
                    }

                } else {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {

                        //Picture (Optional)
                        Text(
                            text = stringResource(R.string.picture_optional),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )

                        //CAMERA
                        IconButton(
                            onClick =
                                {
                                    if (ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.CAMERA
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        val photoFile = File(
                                            context.cacheDir,
                                            "camera_pic_${System.currentTimeMillis()}.jpg"
                                        )
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            photoFile
                                        )
                                        tempCameraUri = uri
                                        cameraLauncher.launch(uri)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CameraAlt,
                                contentDescription = stringResource(R.string.camera),
                                modifier = Modifier.weight(1f),
                                //modifier = Modifier.padding(end = 4.dp),
                            )
                        }

                        //UPLOAD PICTURE
                        IconButton(
                            onClick =
                                {
                                    galleryLauncher.launch("image/*")
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Upload,
                                contentDescription = stringResource(R.string.upload),
                                modifier = Modifier.weight(1f),
                            )
                        }

                        //CAMERA
                        /*

                        Button(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    val photoFile = File(
                                        context.cacheDir,
                                        "camera_pic_${System.currentTimeMillis()}.jpg"
                                    )
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        photoFile
                                    )
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp),
                            )
                            //Text(stringResource(R.string.camera))
                        }

                        //UPLOAD PICTURE
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Upload,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp),
                            )
                            //Text(stringResource(R.string.upload))
                        }

                         */
                         */
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FillChecklistScreenPreview() {
    mychecksTheme {
        FillChecklistScreen(
            checklist = ChecklistEntity(
                id = "1",
                title = "UVV Safety Inspection",
                language = "en",
                icon = "Null"
            ),
            uiState = ChecklistUiState(
                questionsForSelectedChecklist = listOf(
                    ChecklistQuestionEntity(
                        id = "1",
                        checklistid = "1",
                        title = "Are all guards in place?",
                        description = "Check machine covers.",
                        type = "checkbox",
                        options = null,
                        required = true,
                    ),
                    ChecklistQuestionEntity(
                        id = "2",
                        checklistid = "1",
                        title = "Select shift",
                        description = null,
                        type = "combobox",
                        options = "Morning, Evening, Night",
                        required = false,
                    ),
                ),
            ),
            onBackClick = {},
            onOpenHelpClick = {},
            onSubmit = { _, _, _, _, _, _ -> },
            onUserMessageShown = {},
        )
    }
}
