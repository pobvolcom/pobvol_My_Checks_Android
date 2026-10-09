package com.pobvol.mychecks.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pobvol.mychecks.data.local.ChecklistEntity
import com.pobvol.mychecks.ui.theme.mychecksTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChecklistItem(
    checklist: ChecklistEntity,
    onSelectChecklist: (ChecklistEntity) -> Unit = {},
    onFillClick: (ChecklistEntity) -> Unit = {},
    onQuestionsClick: (ChecklistEntity) -> Unit,
    onEditClick: (ChecklistEntity) -> Unit,
    onDeleteClick: (ChecklistEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelectChecklist(checklist) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
                    text = checklist.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = checklist.category,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    shape = RoundedCornerShape(12.dp),
                )
            }

            if (checklist.description!!.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = checklist.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = formatTimestamp(checklist.timestamp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                /*horizontalArrangement = Arrangement.spacedBy(4.dp),*/
                horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                IconButton(onClick = { onFillClick(checklist) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Assignment,
                        /* Original:
                        contentDescription = "Fill out checklist",
                        */
                        contentDescription = "List submissions",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }

                IconButton(onClick = { onQuestionsClick(checklist) }) {
                    Icon(
                        imageVector = Icons.Rounded.QuestionAnswer,
                        contentDescription = "Manage questions",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }

                IconButton(onClick = { onEditClick(checklist) }) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit checklist",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }

                IconButton(onClick = { onDeleteClick(checklist) }) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete checklist",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
fun ChecklistItemPreview() {
    mychecksTheme {
        ChecklistItem(
            checklist = ChecklistEntity(
                id = 1,
                title = "Database Migration Notes",
                language = "en",
                description = "Updated Room database to version 2 with indices for title and timestamp.",
                category = "Work",
                icon = "Null",
                timestamp = System.currentTimeMillis(),
            ),
            onSelectChecklist = {},
            onQuestionsClick = {},
            onEditClick = {},
            onDeleteClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
