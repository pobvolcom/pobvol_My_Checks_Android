package com.pobvol.mychecks.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.Note
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pobvol.mychecks.R
import com.pobvol.mychecks.data.local.TextBlockEntity
import com.pobvol.mychecks.ui.ChecklistUiState
import com.pobvol.mychecks.ui.components.AddEditTextBlockDialog
import com.pobvol.mychecks.ui.components.DeleteTextBlockConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextBlocksScreen(
    uiState: ChecklistUiState,
    onBackClick: () -> Unit,
    onOpenHelpClick: () -> Unit = {},
    onAddTextBlockClick: () -> Unit,
    onEditTextBlockClick: (TextBlockEntity) -> Unit,
    onDeleteTextBlockClick: (TextBlockEntity) -> Unit,
    onMoveTextBlockUp: (TextBlockEntity) -> Unit = {},
    onMoveTextBlockDown: (TextBlockEntity) -> Unit = {},
    onDismissAddEditDialog: () -> Unit,
    onSaveTextBlock: (text: String, sortno: Int) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDeleteTextBlock: () -> Unit,
    onUserMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(onBack = onBackClick)

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onUserMessageShown()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.text_blocks),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back),
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
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTextBlockClick,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.add_text_block),
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (uiState.textBlocks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Note,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(80.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_text_blocks_recorded),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(
                        items = uiState.textBlocks,
                        key = { _, item -> item.id },
                    ) { index, textBlock ->
                        val isFirst = index == 0
                        val isLast = index == uiState.textBlocks.size - 1

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
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = textBlock.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 5,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
                                        IconButton(
                                            onClick = { onMoveTextBlockUp(textBlock) },
                                            enabled = !isFirst,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.ArrowUpward,
                                                contentDescription = "Move up",
                                                tint = if (!isFirst) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(
                                                    alpha = 0.38f
                                                ),
                                            )
                                        }
                                        IconButton(
                                            onClick = { onMoveTextBlockDown(textBlock) },
                                            enabled = !isLast,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.ArrowDownward,
                                                contentDescription = "Move down",
                                                tint = if (!isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(
                                                    alpha = 0.38f
                                                ),
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        ) {
                                            Text(
                                                text = "#${textBlock.sortno}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(
                                                    horizontal = 8.dp,
                                                    vertical = 4.dp
                                                ),
                                            )
                                        }
                                        IconButton(onClick = { onEditTextBlockClick(textBlock) }) {
                                            Icon(
                                                imageVector = Icons.Rounded.Edit,
                                                contentDescription = "Edit text block",
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                        IconButton(onClick = { onDeleteTextBlockClick(textBlock) }) {
                                            Icon(
                                                imageVector = Icons.Rounded.Delete,
                                                contentDescription = "Delete text block",
                                                tint = MaterialTheme.colorScheme.error,
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.isAddEditTextBlockDialogVisible) {
            AddEditTextBlockDialog(
                textBlock = uiState.textBlockToEdit,
                onDismiss = onDismissAddEditDialog,
                onSave = onSaveTextBlock,
            )
        }

        if (uiState.textBlockToDelete != null) {
            DeleteTextBlockConfirmationDialog(
                textBlock = uiState.textBlockToDelete,
                onDismiss = onDismissDeleteDialog,
                onConfirm = onConfirmDeleteTextBlock,
            )
        }
    }
}
