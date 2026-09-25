package com.pobvol.pobvolchecklists.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pobvol.pobvolchecklists.data.local.RecordEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistEntity
import com.pobvol.pobvolchecklists.ui.theme.pobvolchecklistsTheme

@Composable
fun DeleteRecordConfirmationDialog(
    record: RecordEntity,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = "Delete Record?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"${record.title}\"? This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Delete")
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
fun DeleteRecordConfirmationDialogPreview() {
    pobvolchecklistsTheme {
        DeleteChecklistConfirmationDialog(
            checklist = ChecklistEntity(
                id = 1,
                language = "English",
                title = "Important Document",
                description = "Scan of passport",
                category = "Important",
                icon = "Null"

            ),
            onDismiss = {},
            onConfirmDelete = {}
        )
    }
}
