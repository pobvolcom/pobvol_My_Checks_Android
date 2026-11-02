package com.pobvol.mychecks.ui.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pobvol.mychecks.R

@Composable
fun ManageDatabaseDialog(
    onExportDbClick: () -> Unit,
    onImportDbClick: () -> Unit,
    onLoadDemoClick: () -> Unit,
    onResetDbClick: () -> Unit,
    onClearCacheClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showImportWarning by remember { mutableStateOf(false) }
    var showDemoWarning by remember { mutableStateOf(false) }
    var showResetWarning by remember { mutableStateOf(false) }
    var showCacheWarning by remember { mutableStateOf(false) }

    if (showImportWarning) {
        AlertDialog(
            onDismissRequest = { showImportWarning = false },
            title = { Text(stringResource(R.string.menu_import_db), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.import_db_warning)) },
            confirmButton = {
                Button(
                    onClick = {
                        showImportWarning = false
                        onImportDbClick()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showImportWarning = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showDemoWarning) {
        AlertDialog(
            onDismissRequest = { showDemoWarning = false },
            title = {
                Text(
                    stringResource(R.string.load_demo_data_confirm_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(stringResource(R.string.demo_db_warning)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDemoWarning = false
                        onLoadDemoClick()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDemoWarning = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showResetWarning) {
        AlertDialog(
            onDismissRequest = { showResetWarning = false },
            title = {
                Text(
                    stringResource(R.string.menu_reset_database),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(stringResource(R.string.reset_db_warning)) },
            confirmButton = {
                Button(
                    onClick = {
                        showResetWarning = false
                        onResetDbClick()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetWarning = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showCacheWarning) {
        AlertDialog(
            onDismissRequest = { showCacheWarning = false },
            title = {
                Text(
                    stringResource(R.string.clear_cache_confirm_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(stringResource(R.string.clear_cache_confirm_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showCacheWarning = false
                        onClearCacheClick()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCacheWarning = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.manage_database_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.manage_db_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 1. Create Database Copy
                Button(
                    onClick = {
                        onDismissRequest()
                        onExportDbClick()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.menu_create_db_copy))
                }

                // 2. Import Database
                Button(
                    onClick = {
                        showImportWarning = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(stringResource(R.string.menu_import_db))
                }

                // 3. Load Demo Database
                Button(
                    onClick = {
                        showDemoWarning = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(stringResource(R.string.load_demo_data))
                }

                // 4. Reset Database
                Button(
                    onClick = {
                        showResetWarning = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(stringResource(R.string.menu_reset_database))
                }

                // 5. Clear App Cache
                Button(
                    onClick = {
                        showCacheWarning = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(stringResource(R.string.menu_clear_cache))
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
