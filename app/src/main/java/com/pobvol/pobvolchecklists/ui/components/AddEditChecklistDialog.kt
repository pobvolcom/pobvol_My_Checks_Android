package com.pobvol.pobvolchecklists.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pobvol.pobvolchecklists.data.local.ChecklistEntity
import com.pobvol.pobvolchecklists.ui.theme.pobvolchecklistsTheme

@Composable
fun AddEditChecklistDialog(
    checklistToEdit: ChecklistEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, language: String, description: String, category: String, icon: String) -> Unit
) {
    var title by remember(checklistToEdit) { mutableStateOf(checklistToEdit?.title ?: "") }
    var language by remember(checklistToEdit) { mutableStateOf(checklistToEdit?.language ?: "English") }
    var description by remember(checklistToEdit) { mutableStateOf(checklistToEdit?.description ?: "") }
    var category by remember(checklistToEdit) { mutableStateOf(checklistToEdit?.category ?: "General") }
    var icon by remember(checklistToEdit) { mutableStateOf(checklistToEdit?.icon ?: "") }
    var isTitleError by remember { mutableStateOf(false) }

    val predefinedCategories = listOf("General", "Work", "Personal", "UVV", "Ideas")
    val predefinedLanguages = listOf("English", "Deutsch")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (checklistToEdit == null) "Add Checklist" else "Edit Checklist",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) isTitleError = false
                    },
                    label = { Text("Title *") },
                    isError = isTitleError,
                    supportingText = if (isTitleError) {
                        { Text("Title is required") }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    predefinedCategories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category.equals(cat, ignoreCase = true),
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    predefinedCategories.drop(3).forEach { cat ->
                        FilterChip(
                            selected = category.equals(cat, ignoreCase = true),
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Custom Category") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        isTitleError = true
                    } else {
                        onSave(title, language, description, category, icon)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (checklistToEdit == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Preview
@Composable
fun AddChecklistDialogPreview() {
    pobvolchecklistsTheme {
        AddEditChecklistDialog(
            checklistToEdit = null,
            onDismiss = {},
            onSave = { _, _, _, _, _ -> }
        )
    }
}

@Preview
@Composable
fun EditChecklistDialogPreview() {
    pobvolchecklistsTheme {
        AddEditChecklistDialog(
            checklistToEdit = ChecklistEntity(
                id = 1,
                title = "Shopping List",
                language = "en",
                description = "Milk, Eggs, Bread",
                category = "Personal",
                icon = "Null"
            ),
            onDismiss = {},
            onSave = { _, _, _, _, _ -> }
        )
    }
}
