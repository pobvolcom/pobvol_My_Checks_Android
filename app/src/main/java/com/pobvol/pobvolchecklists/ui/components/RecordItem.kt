package com.pobvol.pobvolchecklists.ui.components

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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
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
import com.pobvol.pobvolchecklists.data.local.RecordEntity
import com.pobvol.pobvolchecklists.ui.theme.pobvolchecklistsTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordItem(
    record: RecordEntity,
    onEditClick: (RecordEntity) -> Unit,
    onDeleteClick: (RecordEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = record.category,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (record.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = record.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formatTimestamp(record.timestamp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Row {
                    IconButton(onClick = { onEditClick(record) }) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit record",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = { onDeleteClick(record) }) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete record",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
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
fun RecordItemPreview() {
    pobvolchecklistsTheme {
        RecordItem(
            record = RecordEntity(
                id = 1,
                title = "Database Migration Notes",
                description = "Updated Room database to version 2 with indices for title and timestamp.",
                category = "Work",
                timestamp = System.currentTimeMillis()
            ),
            onEditClick = {},
            onDeleteClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
