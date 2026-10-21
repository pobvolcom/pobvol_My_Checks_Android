package com.pobvol.mychecks.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.FactCheck
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pobvol.mychecks.R
import com.pobvol.mychecks.data.local.CategoryEntity
import com.pobvol.mychecks.data.repository.ThemeMode
import com.pobvol.mychecks.ui.ChecklistUiState
import com.pobvol.mychecks.ui.components.PdfSettingsDialog
import com.pobvol.mychecks.ui.components.SettingsDialog
import com.pobvol.mychecks.ui.theme.mychecksTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(
    uiState: ChecklistUiState,
    onOpenChecklistsClick: () -> Unit,
    onOpenSubmissionsClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    onOpenHelpClick: () -> Unit,
    onDismissSettingsDialog: () -> Unit,
    onOpenPdfSettingsClick: () -> Unit = {},
    onDismissPdfSettingsDialog: () -> Unit = {},
    onOpenSendDataClick: () -> Unit = {},
    onThemeModeSelected: (ThemeMode) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onUserNameChanged: (String) -> Unit,
    onAudioFeedbackChanged: (Boolean) -> Unit = {},
    onPdfHeaderTitleChanged: (String) -> Unit = {},
    onPdfIncludeChecklistTitleChanged: (Boolean) -> Unit = {},
    onPdfIncludeDateChanged: (Boolean) -> Unit = {},
    onPdfIncludeInspectorChanged: (Boolean) -> Unit = {},
    onPdfIncludeStatusChanged: (Boolean) -> Unit = {},
    onPdfIncludeNotesChanged: (Boolean) -> Unit = {},
    onPdfIncludeTimestampChanged: (Boolean) -> Unit = {},
    onPdfIncludePageNumberChanged: (Boolean) -> Unit = {},
    onPdfIncludeUnansweredQuestionsChanged: (Boolean) -> Unit = {},
    onPdfIncludeQuestionTitleChanged: (Boolean) -> Unit = {},
    onPdfIncludeAnswerValueChanged: (Boolean) -> Unit = {},
    onPdfIncludeAnswerNotesChanged: (Boolean) -> Unit = {},
    onPdfIncludeAnswerPictureChanged: (Boolean) -> Unit = {},
    onPdfCompactAnswerLayoutChanged: (Boolean) -> Unit = {},
    onPdfFooterTextChanged: (String) -> Unit = {},
    onPdfTemplateVersionChanged: (String) -> Unit = {},
    onAddCategory: (String) -> Unit = {},
    onDeleteCategory: (CategoryEntity) -> Unit = {},
    onExportDbClick: () -> Unit = {},
    onImportDbClick: () -> Unit = {},
    onCloseAppClick: () -> Unit = {},
    onUserMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showImportWarningDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        // Do nothing to prevent back key on start screen
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onUserMessageShown()
        }
    }

    if (showImportWarningDialog) {
        AlertDialog(
            onDismissRequest = { showImportWarningDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.import_db_dialog_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(stringResource(R.string.import_db_dialog_message))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showImportWarningDialog = false
                        onImportDbClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.import_db_confirm_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showImportWarningDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    var menuExpanded by remember { mutableStateOf(false) }

                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = stringResource(R.string.menu),
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_create_db_copy)) },
                            onClick = {
                                menuExpanded = false
                                onExportDbClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_import_db)) },
                            onClick = {
                                menuExpanded = false
                                showImportWarningDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_close_app)) },
                            onClick = {
                                menuExpanded = false
                                onCloseAppClick()
                            }
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        /*Icon(
                            imageVector = Icons.Rounded.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )*/
                        Text(
                            text = "${stringResource(R.string.app_name)} v${com.pobvol.mychecks.BuildConfig.VERSION_NAME}",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },

                actions = {

                    IconButton(onClick = onOpenHelpClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Help,
                            contentDescription = stringResource(R.string.help),
                        )
                    }

                    IconButton(onClick = onOpenSettingsClick) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.settings_content_description),
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(modifier = Modifier.height(0.1.dp))

            Text(
                text = stringResource(R.string.start_description),
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            // Box 1: Checklists
            StartOptionBox(
                title = stringResource(R.string.checklists),
                subtitle = stringResource(R.string.checklists_subtitle),
                icon = Icons.Rounded.Checklist,
                badgeCount = uiState.checklists.size,
                badgeLabel = stringResource(R.string.records),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = onOpenChecklistsClick,
            )

            // Box 2: Submissions
            StartOptionBox(
                title = stringResource(R.string.submissions),
                subtitle = stringResource(R.string.submissions_subtitle),
                icon = Icons.AutoMirrored.Rounded.FactCheck,
                badgeCount = uiState.submissions.size,
                badgeLabel = stringResource(R.string.records),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = onOpenSubmissionsClick,
            )

            // Box 3: Data transfer
            /*
            StartOptionBox(
                title = stringResource(R.string.data_transfer),
                subtitle = stringResource(R.string.data_transfer_subtitle),
                icon = Icons.AutoMirrored.Rounded.Send,
                badgeCount = 0,
                badgeLabel = "",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onOpenSendDataClick,
            )*/

            Text(
                text = stringResource(R.string.copyright_text),
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

        }

        if (uiState.isSettingsDialogVisible) {
            SettingsDialog(
                userSettings = uiState.userSettings,
                languages = uiState.availableLanguages,
                categories = uiState.categories,
                onThemeModeSelected = onThemeModeSelected,
                onLanguageSelected = onLanguageSelected,
                onUserNameChanged = onUserNameChanged,
                onAudioFeedbackChanged = onAudioFeedbackChanged,
                onOpenPdfSettingsClick = onOpenPdfSettingsClick,
                onAddCategory = onAddCategory,
                onDeleteCategory = onDeleteCategory,
                onDismissRequest = onDismissSettingsDialog,
            )
        }

        if (uiState.isPdfSettingsVisible) {
            PdfSettingsDialog(
                userSettings = uiState.userSettings,
                onPdfHeaderTitleChanged = onPdfHeaderTitleChanged,
                onPdfIncludeChecklistTitleChanged = onPdfIncludeChecklistTitleChanged,
                onPdfIncludeDateChanged = onPdfIncludeDateChanged,
                onPdfIncludeInspectorChanged = onPdfIncludeInspectorChanged,
                onPdfIncludeStatusChanged = onPdfIncludeStatusChanged,
                onPdfIncludeNotesChanged = onPdfIncludeNotesChanged,
                onPdfIncludeTimestampChanged = onPdfIncludeTimestampChanged,
                onPdfIncludePageNumberChanged = onPdfIncludePageNumberChanged,
                onPdfIncludeUnansweredQuestionsChanged = onPdfIncludeUnansweredQuestionsChanged,
                onPdfIncludeQuestionTitleChanged = onPdfIncludeQuestionTitleChanged,
                onPdfIncludeAnswerValueChanged = onPdfIncludeAnswerValueChanged,
                onPdfIncludeAnswerNotesChanged = onPdfIncludeAnswerNotesChanged,
                onPdfIncludeAnswerPictureChanged = onPdfIncludeAnswerPictureChanged,
                onPdfCompactAnswerLayoutChanged = onPdfCompactAnswerLayoutChanged,
                onPdfFooterTextChanged = onPdfFooterTextChanged,
                onPdfTemplateVersionChanged = onPdfTemplateVersionChanged,
                onDismissRequest = onDismissPdfSettingsDialog,
            )
        }
    }
}

@Composable
fun StartOptionBox(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeCount: Int,
    badgeLabel: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = contentColor.copy(alpha = 0.15f),
                ) {
                    Box(
                        modifier = Modifier.padding(12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }

                if (badgeLabel.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = contentColor.copy(alpha = 0.2f),
                    ) {
                        Text(
                            text = "$badgeLabel: $badgeCount ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor.copy(alpha = 0.8f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    mychecksTheme {
        StartScreen(
            uiState = ChecklistUiState(),
            onOpenChecklistsClick = {},
            onOpenSubmissionsClick = {},
            onOpenSettingsClick = {},
            onOpenHelpClick = {},
            onDismissSettingsDialog = {},
            onThemeModeSelected = {},
            onLanguageSelected = {},
            onUserNameChanged = {},
            onUserMessageShown = {},
        )
    }
}
