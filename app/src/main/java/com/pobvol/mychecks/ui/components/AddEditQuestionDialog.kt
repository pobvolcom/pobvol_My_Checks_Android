package com.pobvol.mychecks.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pobvol.mychecks.R
import com.pobvol.mychecks.data.local.ChecklistQuestionEntity
import com.pobvol.mychecks.ui.theme.mychecksTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditQuestionDialog(
    questionToEdit: ChecklistQuestionEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, type: String, options: String, required: Boolean, sortno: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by remember { mutableStateOf(questionToEdit?.title ?: "") }
    var description by remember { mutableStateOf(questionToEdit?.description ?: "") }
    var type by remember { mutableStateOf(questionToEdit?.type ?: "text") }
    var options by remember { mutableStateOf(questionToEdit?.options ?: "") }
    var required by remember { mutableStateOf(questionToEdit?.required ?: true) }
    var sortnoText by remember { mutableStateOf(questionToEdit?.sortno?.let { if (it <= 0) "" else it.toString() } ?: "") }

    var titleError by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    val answerTypes = listOf("text", "number", "checkbox", "combobox", "nfc", "qr-code")

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Icon(
                imageVector = Icons.Rounded.Help,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = {
            Text(
                text = if (questionToEdit == null) stringResource(R.string.add_new_question) else stringResource(R.string.edit_question),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text(stringResource(R.string.question_title_required)) },
                    isError = titleError,
                    supportingText = {
                        if (titleError) {
                            Text(stringResource(R.string.title_cannot_be_empty), color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.description_optional)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )

                OutlinedTextField(
                    value = sortnoText,
                    onValueChange = { sortnoText = it.filter { char -> char.isDigit() } },
                    label = { Text(stringResource(R.string.sort_order_optional)) },
                    placeholder = { Text(stringResource(R.string.sort_order_placeholder)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.answer_type)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                    )

                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                    ) {
                        answerTypes.forEach { answerType ->
                            DropdownMenuItem(
                                text = { Text(answerType) },
                                onClick = {
                                    type = answerType
                                    typeExpanded = false
                                },
                            )
                        }
                    }
                }

                if (type == "combobox") {
                    OutlinedTextField(
                        value = options,
                        onValueChange = { options = it },
                        label = { Text(stringResource(R.string.options_comma_separated)) },
                        placeholder = { Text(stringResource(R.string.options_placeholder)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.required_question),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Switch(
                        checked = required,
                        onCheckedChange = { required = it },
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        val sortnoVal = sortnoText.toIntOrNull() ?: 0
                        onSave(title, description, type, options, required, sortnoVal)
                    }
                },
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun AddEditQuestionDialogPreview() {
    mychecksTheme {
        AddEditQuestionDialog(
            questionToEdit = null,
            onDismiss = {},
            onSave = { _, _, _, _, _, _ -> },
        )
    }
}
