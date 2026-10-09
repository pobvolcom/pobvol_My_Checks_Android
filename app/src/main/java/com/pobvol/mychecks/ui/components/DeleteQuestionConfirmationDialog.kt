package com.pobvol.mychecks.ui.components

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pobvol.mychecks.data.local.ChecklistQuestionEntity
import com.pobvol.mychecks.ui.theme.mychecksTheme

@Composable
fun DeleteQuestionConfirmationDialog(
    question: ChecklistQuestionEntity,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        title = {
            Text(
                text = "Delete Question?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"${question.title}\"? This action cannot be undone.",
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp),
    )
}

@Preview(showBackground = true)
@Composable
fun DeleteQuestionConfirmationDialogPreview() {
    mychecksTheme {
        DeleteQuestionConfirmationDialog(
            question = ChecklistQuestionEntity(
                id = 1,
                title = "Is safety equipment worn?",
                description = "Check compliance",
                type = "checkbox",
                options = null,
                required = true,
            ),
            onDismiss = {},
            onConfirmDelete = {},
        )
    }
}
