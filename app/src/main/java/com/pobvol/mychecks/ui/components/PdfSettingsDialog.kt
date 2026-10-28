package com.pobvol.mychecks.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pobvol.mychecks.R
import com.pobvol.mychecks.data.repository.UserSettings
import com.pobvol.mychecks.util.PdfExporter
import java.io.File

@Composable
fun PdfSettingsDialog(
    userSettings: UserSettings,
    onPdfHeaderTitleChanged: (String) -> Unit,
    onPdfIncludeChecklistTitleChanged: (Boolean) -> Unit,
    onPdfIncludeDateChanged: (Boolean) -> Unit,
    onPdfIncludeInspectorChanged: (Boolean) -> Unit,
    onPdfIncludeStatusChanged: (Boolean) -> Unit,
    onPdfIncludeNotesChanged: (Boolean) -> Unit,
    onPdfIncludeTimestampChanged: (Boolean) -> Unit,
    onPdfIncludePageNumberChanged: (Boolean) -> Unit,
    onPdfIncludeUnansweredQuestionsChanged: (Boolean) -> Unit,
    onPdfIncludeQuestionTitleChanged: (Boolean) -> Unit,
    onPdfIncludeQuestionDescriptionChanged: (Boolean) -> Unit = {},
    onPdfIncludeAnswerValueChanged: (Boolean) -> Unit,
    onPdfIncludeAnswerNotesChanged: (Boolean) -> Unit,
    onPdfIncludeAnswerPictureChanged: (Boolean) -> Unit,
    onPdfCompactAnswerLayoutChanged: (Boolean) -> Unit,
    onPdfFooterTextChanged: (String) -> Unit,
    onPdfTemplateVersionChanged: (String) -> Unit = {},
    onPdfPictureSizeSmallChanged: (Int) -> Unit = {},
    onPdfPictureSizeMediumChanged: (Int) -> Unit = {},
    onPdfPictureSizeLargeChanged: (Int) -> Unit = {},
    onSignatureCaptured: (String) -> Unit = {},
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.pdf_report_designer),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                }

                // Template Version Selector
                /*Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.pdf_template_version_label),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val isV1 = userSettings.pdfTemplateVersion != "V2"
                        FilterChip(
                            selected = isV1,
                            onClick = { onPdfTemplateVersionChanged("V1") },
                            label = { Text(stringResource(R.string.pdf_template_v1)) },
                            modifier = Modifier.weight(1f),
                        )
                        FilterChip(
                            selected = !isV1,
                            onClick = { onPdfTemplateVersionChanged("V2") },
                            label = { Text(stringResource(R.string.pdf_template_v2)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }*/

                // Picture Size Selector (<Number input> for S, M, L)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.pdf_picture_size_label),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        var smallText by remember(userSettings.pdfPictureSizeSmall) {
                            mutableStateOf(
                                userSettings.pdfPictureSizeSmall.toString()
                            )
                        }
                        var mediumText by remember(userSettings.pdfPictureSizeMedium) {
                            mutableStateOf(
                                userSettings.pdfPictureSizeMedium.toString()
                            )
                        }
                        var largeText by remember(userSettings.pdfPictureSizeLarge) {
                            mutableStateOf(
                                userSettings.pdfPictureSizeLarge.toString()
                            )
                        }

                        OutlinedTextField(
                            value = smallText,
                            onValueChange = { newVal ->
                                smallText = newVal
                                newVal.toIntOrNull()?.let { onPdfPictureSizeSmallChanged(it) }
                            },
                            label = { Text(stringResource(R.string.pdf_picture_size_s_label)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = mediumText,
                            onValueChange = { newVal ->
                                mediumText = newVal
                                newVal.toIntOrNull()?.let { onPdfPictureSizeMediumChanged(it) }
                            },
                            label = { Text(stringResource(R.string.pdf_picture_size_m_label)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = largeText,
                            onValueChange = { newVal ->
                                largeText = newVal
                                newVal.toIntOrNull()?.let { onPdfPictureSizeLargeChanged(it) }
                            },
                            label = { Text(stringResource(R.string.pdf_picture_size_l_label)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Header Design
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.pdf_header_section),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    var headerTitleText by remember(userSettings.pdfHeaderTitle) {
                        mutableStateOf(
                            userSettings.pdfHeaderTitle
                        )
                    }
                    OutlinedTextField(
                        value = headerTitleText,
                        onValueChange = { newText ->
                            headerTitleText = newText
                            onPdfHeaderTitleChanged(newText)
                        },
                        label = { Text(stringResource(R.string.pdf_header_title_label)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    PdfSwitchRow(
                        stringResource(R.string.pdf_include_checklist_title),
                        userSettings.pdfIncludeChecklistTitle,
                        onPdfIncludeChecklistTitleChanged
                    )
                    PdfSwitchRow(
                        stringResource(R.string.pdf_include_status),
                        userSettings.pdfIncludeStatus,
                        onPdfIncludeStatusChanged
                    )
                    PdfSwitchRow(
                        stringResource(R.string.pdf_include_notes),
                        userSettings.pdfIncludeNotes,
                        onPdfIncludeNotesChanged
                    )
                }

                // Answer Design
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.pdf_answer_section),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    PdfSwitchRow(
                        stringResource(R.string.include_unanswered_questions),
                        userSettings.includeUnansweredQuestions,
                        onPdfIncludeUnansweredQuestionsChanged
                    )
                    PdfSwitchRow(
                        stringResource(R.string.pdf_include_question_title),
                        userSettings.pdfIncludeQuestionTitle,
                        onPdfIncludeQuestionTitleChanged
                    )
                    PdfSwitchRow(
                        stringResource(R.string.pdf_include_question_description),
                        userSettings.pdfIncludeQuestionDescription,
                        onPdfIncludeQuestionDescriptionChanged
                    )
                    PdfSwitchRow(
                        stringResource(R.string.pdf_include_answer_value),
                        userSettings.pdfIncludeAnswerValue,
                        onPdfIncludeAnswerValueChanged
                    )
                    PdfSwitchRow(
                        stringResource(R.string.include_answer_notes),
                        userSettings.pdfIncludeAnswerNotes,
                        onPdfIncludeAnswerNotesChanged
                    )
                    PdfSwitchRow(
                        stringResource(R.string.pdf_include_answer_picture),
                        userSettings.pdfIncludeAnswerPicture,
                        onPdfIncludeAnswerPictureChanged
                    )
                    PdfSwitchRow(
                        stringResource(R.string.compact_answer_layout),
                        userSettings.compactAnswerLayout,
                        onPdfCompactAnswerLayoutChanged
                    )
                }

                // Footer Design
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.pdf_footer_section),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    var footerText by remember(userSettings.pdfFooterText) {
                        mutableStateOf(
                            userSettings.pdfFooterText
                        )
                    }
                    OutlinedTextField(
                        value = footerText,
                        onValueChange = { newText ->
                            footerText = newText
                            onPdfFooterTextChanged(newText)
                        },
                        label = { Text(stringResource(R.string.pdf_footer_text_label)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    PdfSwitchRow(
                        stringResource(R.string.pdf_include_date),
                        userSettings.pdfIncludeDate,
                        onPdfIncludeDateChanged
                    )
                    PdfSwitchRow(
                        stringResource(R.string.include_inspector),
                        userSettings.pdfIncludeInspector,
                        onPdfIncludeInspectorChanged
                    )

                    // Signature Section below Inspector Name
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        var showSignatureDialog by remember { mutableStateOf(false) }

                        Button(
                            onClick = { showSignatureDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.capture_signature))
                        }

                        //Show signature
                        if (!userSettings.signaturePath.isBlank()) {
                            val context = LocalContext.current
                            val sigFile = PdfExporter.getPictureFile(
                                context,
                                File(userSettings.signaturePath).name
                            ) ?: File(userSettings.signaturePath)
                            if (sigFile.exists()) {
                                val sigLastModified =
                                    remember(userSettings.signaturePath, sigFile.lastModified()) {
                                        if (sigFile.exists()) sigFile.lastModified() else System.currentTimeMillis()
                                    }
                                val bitmap = remember(userSettings.signaturePath, sigLastModified) {
                                    BitmapFactory.decodeFile(sigFile.absolutePath)
                                }
                                if (bitmap != null) {
                                    Text(
                                        text = "Saved Signature Preview:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White),
                                        contentScale = ContentScale.Fit,
                                    )
                                }
                            }
                        }

                        if (showSignatureDialog) {
                            SignatureCaptureDialog(
                                onDismiss = { showSignatureDialog = false },
                                onSaveSignature = { path ->
                                    onSignatureCaptured(path)
                                    showSignatureDialog = false
                                }
                            )
                        }
                    }
                    PdfSwitchRow(
                        stringResource(R.string.include_timestamp),
                        userSettings.includeTimestamp,
                        onPdfIncludeTimestampChanged
                    )
                    PdfSwitchRow(
                        stringResource(R.string.pdf_include_page_number),
                        userSettings.pdfIncludePageNumber,
                        onPdfIncludePageNumberChanged
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.close))
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
