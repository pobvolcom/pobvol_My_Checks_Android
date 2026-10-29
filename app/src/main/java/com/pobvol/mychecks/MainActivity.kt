package com.pobvol.mychecks

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pobvol.mychecks.data.local.AppDatabase
import com.pobvol.mychecks.data.repository.CategoryRepositoryImpl
import com.pobvol.mychecks.data.repository.ChecklistAnswerRepositoryImpl
import com.pobvol.mychecks.data.repository.ChecklistQuestionRepositoryImpl
import com.pobvol.mychecks.data.repository.ChecklistRepositoryImpl
import com.pobvol.mychecks.data.repository.ChecklistSubmissionRepositoryImpl
import com.pobvol.mychecks.data.repository.DeviceRepositoryImpl
import com.pobvol.mychecks.data.repository.LanguageRepositoryImpl
import com.pobvol.mychecks.data.repository.SettingsRepositoryImpl
import com.pobvol.mychecks.data.repository.TextBlockRepositoryImpl
import com.pobvol.mychecks.data.repository.ThemeMode
import com.pobvol.mychecks.ui.ChecklistViewModel
import com.pobvol.mychecks.ui.screens.ChecklistChartScreen
import com.pobvol.mychecks.ui.screens.ChecklistListScreen
import com.pobvol.mychecks.ui.screens.ChecklistQuestionsScreen
import com.pobvol.mychecks.ui.screens.FillChecklistScreen
import com.pobvol.mychecks.ui.screens.HelpScreen
import com.pobvol.mychecks.ui.screens.LicenseScreen
import com.pobvol.mychecks.ui.screens.SplashScreen
import com.pobvol.mychecks.ui.screens.StartScreen
import com.pobvol.mychecks.ui.screens.SubmissionDetailScreen
import com.pobvol.mychecks.ui.screens.SubmissionsOverviewScreen
import com.pobvol.mychecks.ui.screens.TextBlocksScreen
import com.pobvol.mychecks.ui.theme.mychecksTheme
import com.pobvol.mychecks.util.SqlExporter
import com.pobvol.mychecks.util.TtsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var viewModelRef: ChecklistViewModel? = null
    private var ttsHelper: TtsHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("my_checks_prefs", MODE_PRIVATE)
        val savedLang = prefs.getString("saved_language", null)
        if (!savedLang.isNullOrBlank()) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(savedLang))
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ttsHelper = TtsHelper(applicationContext)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter != null) {
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            pendingIntent = PendingIntent.getActivity(
                this, 0,
                Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                flags
            )
        }

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ChecklistRepositoryImpl(database.checklistDao())
        val deviceRepository = DeviceRepositoryImpl(database.deviceDao())
        val questionRepository = ChecklistQuestionRepositoryImpl(database.checklistquestionDao())
        val languageRepository = LanguageRepositoryImpl(database.languageDao())
        val settingsRepository = SettingsRepositoryImpl(applicationContext)
        val categoryRepository = CategoryRepositoryImpl(database.categoryDao())
        val submissionRepository = ChecklistSubmissionRepositoryImpl(database.checklistsubmissionDao())
        val answerRepository = ChecklistAnswerRepositoryImpl(database.checklistanswerDao())
        val textBlockRepository = TextBlockRepositoryImpl(database.textblockDao())

        setContent {
            val viewModel: ChecklistViewModel = viewModel(
                factory = ChecklistViewModel.Factory(
                    repository = repository,
                    questionRepository = questionRepository,
                    deviceRepository = deviceRepository,
                    languageRepository = languageRepository,
                    settingsRepository = settingsRepository,
                    categoryRepository = categoryRepository,
                    submissionRepository = submissionRepository,
                    answerRepository = answerRepository,
                    textBlockRepository = textBlockRepository,
                    context = applicationContext,
                ),
            )
            viewModelRef = viewModel
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            var isSplashVisible by remember { mutableStateOf(true) }

            LaunchedEffect(uiState.userMessage, uiState.userSettings.audioFeedback) {
                val message = uiState.userMessage
                if (!message.isNullOrBlank() && uiState.userSettings.audioFeedback) {
                    ttsHelper?.speak(message, uiState.userSettings.preferredLanguage)
                }
            }

            LaunchedEffect(uiState.userSettings.preferredLanguage) {
                val appLocale = LocaleListCompat.forLanguageTags(uiState.userSettings.preferredLanguage)
                AppCompatDelegate.setApplicationLocales(appLocale)
            }

            val darkTheme = when (uiState.userSettings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            mychecksTheme(darkTheme = darkTheme) {
                if (isSplashVisible) {
                    SplashScreen(onTimeout = { isSplashVisible = false })
                } else if (!uiState.userSettings.isLicenseAccepted || uiState.isLicenseScreenVisible) {
                    LicenseScreen(
                        uiState = uiState,
                        onAcceptClick = viewModel::acceptLicense,
                        onLanguageSelected = viewModel::updatePreferredLanguage,
                        onBackClick = if (uiState.userSettings.isLicenseAccepted) viewModel::closeLicenseScreen else null,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    val selectedSubmissionToEdit = uiState.selectedSubmissionToEdit
                    val selectedChecklistToAnswer = uiState.selectedChecklistToAnswer
                    val selectedChecklistForQuestions = uiState.selectedChecklistForQuestions

                    val chartChecklist = uiState.selectedChecklistForChart
                    if (chartChecklist != null) {
                        ChecklistChartScreen(
                            checklist = chartChecklist,
                            uiState = uiState,
                            onBackClick = viewModel::closeChartScreen,
                            onLoadMoreClick = viewModel::loadMoreSubmissions,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else if (uiState.isHelpVisible) {
                        HelpScreen(
                            uiState = uiState,
                            onBackClick = viewModel::closeHelpScreen,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else if (uiState.isTextBlocksVisible) {
                        TextBlocksScreen(
                            uiState = uiState,
                            onBackClick = viewModel::closeTextBlocksScreen,
                            onOpenHelpClick = viewModel::openHelpScreen,
                            onAddTextBlockClick = viewModel::openAddTextBlockDialog,
                            onEditTextBlockClick = viewModel::openEditTextBlockDialog,
                            onDeleteTextBlockClick = viewModel::requestDeleteTextBlockConfirmation,
                            onMoveTextBlockUp = viewModel::moveTextBlockUp,
                            onMoveTextBlockDown = viewModel::moveTextBlockDown,
                            onDismissAddEditDialog = viewModel::dismissAddEditTextBlockDialog,
                            onSaveTextBlock = viewModel::saveTextBlock,
                            onDismissDeleteDialog = viewModel::dismissDeleteTextBlockConfirmation,
                            onConfirmDeleteTextBlock = viewModel::confirmDeleteTextBlock,
                            onUserMessageShown = viewModel::userMessageShown,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else if (selectedSubmissionToEdit != null) {
                        val checklist =
                            uiState.checklists.find { it.id == selectedSubmissionToEdit.checklistid }
                        SubmissionDetailScreen(
                            submission = selectedSubmissionToEdit,
                            checklist = checklist,
                            uiState = uiState,
                            onBackClick = viewModel::closeEditSubmission,
                            onOpenHelpClick = viewModel::openHelpScreen,
                            onSaveSubmission = viewModel::updateSubmissionAndAnswers,
                            onUserMessageShown = viewModel::userMessageShown,
                            onNfcTagConsumed = viewModel::clearNfcTag,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else if (selectedChecklistToAnswer != null) {
                        FillChecklistScreen(
                            checklist = selectedChecklistToAnswer,
                            uiState = uiState,
                            onBackClick = viewModel::closeFillChecklist,
                            onOpenHelpClick = viewModel::openHelpScreen,
                            onSubmit = { checklistId, inspector, notes, date, status, answers ->
                                viewModel.submitChecklistAnswers(
                                    checklistId = checklistId,
                                    inspector = inspector,
                                    notes = notes,
                                    date = date,
                                    status = status,
                                    answers = answers,
                                )
                            },
                            onUserMessageShown = viewModel::userMessageShown,
                            onNfcTagConsumed = viewModel::clearNfcTag,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else if (uiState.isSubmissionsOverviewVisible) {
                        SubmissionsOverviewScreen(
                            uiState = uiState,
                            onSearchQueryChange = viewModel::onSubmissionSearchQueryChange,
                            onStatusFilterChange = viewModel::onSubmissionStatusFilterChange,
                            onClearChecklistFilter = viewModel::clearChecklistSubmissionsFilter,
                            onBackClick = viewModel::closeSubmissionsOverview,
                            onOpenHelpClick = viewModel::openHelpScreen,
                            onOpenSettingsClick = viewModel::openSettingsDialog,
                            onDismissSettingsDialog = viewModel::dismissSettingsDialog,
                            onThemeModeSelected = viewModel::updateThemeMode,
                            onLanguageSelected = viewModel::updatePreferredLanguage,
                            onUserNameChanged = viewModel::updateUserName,
                            onAudioFeedbackChanged = viewModel::updateAudioFeedback,
                            onAddCategory = viewModel::addCategory,
                            onDeleteCategory = viewModel::deleteCategory,
                            onOpenPdfSettingsClick = viewModel::openPdfSettings,
                            onDismissPdfSettingsDialog = viewModel::closePdfSettings,
                            onPdfHeaderTitleChanged = viewModel::updatePdfHeaderTitle,
                            onPdfIncludeChecklistTitleChanged = viewModel::updatePdfIncludeChecklistTitle,
                            onPdfIncludeChecklistDescriptionChanged = viewModel::updatePdfIncludeChecklistDescription,
                            onPdfIncludeDateChanged = viewModel::updatePdfIncludeDate,
                            onPdfIncludeInspectorChanged = viewModel::updatePdfIncludeInspector,
                            onPdfIncludeStatusChanged = viewModel::updatePdfIncludeStatus,
                            onPdfIncludeNotesChanged = viewModel::updatePdfIncludeNotes,
                            onPdfIncludeTimestampChanged = viewModel::updatePdfIncludeTimestamp,
                            onPdfIncludePageNumberChanged = viewModel::updatePdfIncludePageNumber,
                            onPdfIncludeUnansweredQuestionsChanged = viewModel::updatePdfIncludeUnansweredQuestions,
                            onPdfIncludeQuestionTitleChanged = viewModel::updatePdfIncludeQuestionTitle,
                            onPdfIncludeQuestionDescriptionChanged = viewModel::updatePdfIncludeQuestionDescription,
                            onPdfIncludeAnswerValueChanged = viewModel::updatePdfIncludeAnswerValue,
                            onPdfIncludeAnswerNotesChanged = viewModel::updatePdfIncludeAnswerNotes,
                            onPdfIncludeAnswerPictureChanged = viewModel::updatePdfIncludeAnswerPicture,
                            onPdfCompactAnswerLayoutChanged = viewModel::updatePdfCompactAnswerLayout,
                            onPdfFooterTextChanged = viewModel::updatePdfFooterText,
                            onPdfTemplateVersionChanged = viewModel::updatePdfTemplateVersion,
                            onPdfPictureSizeSmallChanged = viewModel::updatePdfPictureSizeSmall,
                            onPdfPictureSizeMediumChanged = viewModel::updatePdfPictureSizeMedium,
                            onPdfPictureSizeLargeChanged = viewModel::updatePdfPictureSizeLarge,
                            onSignatureCaptured = viewModel::updateSignaturePath,
                            onAddSubmissionClick = viewModel::openFillChecklist,
                            onLoadMoreClick = viewModel::loadMoreSubmissions,
                            onExportPdfClick = { submission ->
                                viewModel.exportSubmissionPdf(this, submission)
                            },
                            onExportSqlClick = { submission ->
                                viewModel.exportSubmissionSql(this, submission)
                            },
                            onEditSubmissionClick = viewModel::openEditSubmission,
                            onDeleteSubmissionClick = viewModel::requestDeleteSubmission,
                            onDismissDeleteSubmissionDialog = viewModel::dismissDeleteSubmission,
                            onConfirmDeleteSubmission = viewModel::confirmDeleteSubmission,
                            onUserMessageShown = viewModel::userMessageShown,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else if (selectedChecklistForQuestions != null) {
                        ChecklistQuestionsScreen(
                            checklist = selectedChecklistForQuestions,
                            uiState = uiState,
                            onBackClick = viewModel::closeQuestionsScreen,
                            onOpenHelpClick = viewModel::openHelpScreen,
                            onAddQuestionClick = viewModel::openAddQuestionDialog,
                            onEditQuestionClick = viewModel::openEditQuestionDialog,
                            onDeleteQuestionClick = viewModel::requestDeleteQuestionConfirmation,
                            onMoveQuestionUp = viewModel::moveQuestionUp,
                            onMoveQuestionDown = viewModel::moveQuestionDown,
                            onDismissAddEditQuestionDialog = viewModel::dismissAddEditQuestionDialog,
                            onSaveQuestion = viewModel::saveQuestion,
                            onDismissDeleteQuestionDialog = viewModel::dismissDeleteQuestionConfirmation,
                            onConfirmDeleteQuestion = viewModel::confirmDeleteQuestion,
                            onUserMessageShown = viewModel::userMessageShown,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else if (uiState.isChecklistListVisible) {
                        ChecklistListScreen(
                            uiState = uiState,
                            onSearchQueryChange = viewModel::onChecklistSearchQueryChange,
                            onCategoryFilterChange = viewModel::onCategoryFilterChange,
                            onAddClick = viewModel::openAddDialog,
                            onSelectChecklist = viewModel::openSubmissionsForChecklist,
                            onQuestionsClick = viewModel::openQuestionsForChecklist,
                            onEditClick = viewModel::openEditDialog,
                            onDeleteClick = viewModel::requestDeleteConfirmation,
                            onChartClick = viewModel::openChartForChecklist,
                            onDismissAddEditDialog = viewModel::dismissAddEditDialog,
                            onSaveChecklist = viewModel::saveChecklist,
                            onDismissDeleteDialog = viewModel::dismissDeleteConfirmation,
                            onConfirmDelete = viewModel::confirmDelete,
                            onCopyClick = viewModel::openCopyChecklistDialog,
                            onDismissCopyDialog = viewModel::dismissCopyChecklistDialog,
                            onConfirmCopyChecklist = viewModel::copyChecklist,
                            onUserMessageShown = viewModel::userMessageShown,
                            onBackClick = viewModel::closeChecklistsScreen,
                            onOpenHelpClick = viewModel::openHelpScreen,
                            onOpenSettingsClick = viewModel::openSettingsDialog,
                            onDismissSettingsDialog = viewModel::dismissSettingsDialog,
                            onSignatureCaptured = viewModel::updateSignaturePath,
                            onOpenPdfSettingsClick = viewModel::openPdfSettings,
                            onDismissPdfSettingsDialog = viewModel::closePdfSettings,
                            onThemeModeSelected = viewModel::updateThemeMode,
                            onLanguageSelected = viewModel::updatePreferredLanguage,
                            onUserNameChanged = viewModel::updateUserName,
                            onAudioFeedbackChanged = viewModel::updateAudioFeedback,
                            onPdfHeaderTitleChanged = viewModel::updatePdfHeaderTitle,
                            onPdfIncludeChecklistTitleChanged = viewModel::updatePdfIncludeChecklistTitle,
                            onPdfIncludeChecklistDescriptionChanged = viewModel::updatePdfIncludeChecklistDescription,
                            onPdfIncludeDateChanged = viewModel::updatePdfIncludeDate,
                            onPdfIncludeInspectorChanged = viewModel::updatePdfIncludeInspector,
                            onPdfIncludeStatusChanged = viewModel::updatePdfIncludeStatus,
                            onPdfIncludeNotesChanged = viewModel::updatePdfIncludeNotes,
                            onPdfIncludeTimestampChanged = viewModel::updatePdfIncludeTimestamp,
                            onPdfIncludePageNumberChanged = viewModel::updatePdfIncludePageNumber,
                            onPdfIncludeUnansweredQuestionsChanged = viewModel::updatePdfIncludeUnansweredQuestions,
                            onPdfIncludeQuestionTitleChanged = viewModel::updatePdfIncludeQuestionTitle,
                            onPdfIncludeQuestionDescriptionChanged = viewModel::updatePdfIncludeQuestionDescription,
                            onPdfIncludeAnswerValueChanged = viewModel::updatePdfIncludeAnswerValue,
                            onPdfIncludeAnswerNotesChanged = viewModel::updatePdfIncludeAnswerNotes,
                            onPdfIncludeAnswerPictureChanged = viewModel::updatePdfIncludeAnswerPicture,
                            onPdfCompactAnswerLayoutChanged = viewModel::updatePdfCompactAnswerLayout,
                            onPdfFooterTextChanged = viewModel::updatePdfFooterText,
                            onPdfTemplateVersionChanged = viewModel::updatePdfTemplateVersion,
                            onPdfPictureSizeSmallChanged = viewModel::updatePdfPictureSizeSmall,
                            onPdfPictureSizeMediumChanged = viewModel::updatePdfPictureSizeMedium,
                            onPdfPictureSizeLargeChanged = viewModel::updatePdfPictureSizeLarge,
                            onAddCategory = viewModel::addCategory,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        StartScreen(
                            uiState = uiState,
                            onOpenChecklistsClick = viewModel::openChecklistsScreen,
                            onOpenSubmissionsClick = viewModel::openSubmissionsOverview,
                            onOpenTextBlocksClick = viewModel::openTextBlocksScreen,
                            onOpenLicenseClick = viewModel::openLicenseScreen,
                            onOpenSettingsClick = viewModel::openSettingsDialog,
                            onOpenHelpClick = viewModel::openHelpScreen,
                            onDismissSettingsDialog = viewModel::dismissSettingsDialog,
                            onSignatureCaptured = viewModel::updateSignaturePath,
                            onOpenPdfSettingsClick = viewModel::openPdfSettings,
                            onDismissPdfSettingsDialog = viewModel::closePdfSettings,
                            onThemeModeSelected = viewModel::updateThemeMode,
                            onLanguageSelected = viewModel::updatePreferredLanguage,
                            onUserNameChanged = viewModel::updateUserName,
                            onAudioFeedbackChanged = viewModel::updateAudioFeedback,
                            onPdfHeaderTitleChanged = viewModel::updatePdfHeaderTitle,
                            onPdfIncludeChecklistTitleChanged = viewModel::updatePdfIncludeChecklistTitle,
                            onPdfIncludeChecklistDescriptionChanged = viewModel::updatePdfIncludeChecklistDescription,
                            onPdfIncludeDateChanged = viewModel::updatePdfIncludeDate,
                            onPdfIncludeInspectorChanged = viewModel::updatePdfIncludeInspector,
                            onPdfIncludeStatusChanged = viewModel::updatePdfIncludeStatus,
                            onPdfIncludeNotesChanged = viewModel::updatePdfIncludeNotes,
                            onPdfIncludeTimestampChanged = viewModel::updatePdfIncludeTimestamp,
                            onPdfIncludePageNumberChanged = viewModel::updatePdfIncludePageNumber,
                            onPdfIncludeUnansweredQuestionsChanged = viewModel::updatePdfIncludeUnansweredQuestions,
                            onPdfIncludeQuestionTitleChanged = viewModel::updatePdfIncludeQuestionTitle,
                            onPdfIncludeQuestionDescriptionChanged = viewModel::updatePdfIncludeQuestionDescription,
                            onPdfIncludeAnswerValueChanged = viewModel::updatePdfIncludeAnswerValue,
                            onPdfIncludeAnswerNotesChanged = viewModel::updatePdfIncludeAnswerNotes,
                            onPdfIncludeAnswerPictureChanged = viewModel::updatePdfIncludeAnswerPicture,
                            onPdfCompactAnswerLayoutChanged = viewModel::updatePdfCompactAnswerLayout,
                            onPdfFooterTextChanged = viewModel::updatePdfFooterText,
                            onPdfTemplateVersionChanged = viewModel::updatePdfTemplateVersion,
                            onPdfPictureSizeSmallChanged = viewModel::updatePdfPictureSizeSmall,
                            onPdfPictureSizeMediumChanged = viewModel::updatePdfPictureSizeMedium,
                            onPdfPictureSizeLargeChanged = viewModel::updatePdfPictureSizeLarge,
                            onAddCategory = viewModel::addCategory,
                            onDeleteCategory = viewModel::deleteCategory,
                            onExportDbClick = {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    val success = SqlExporter.exportDatabaseCopy(this@MainActivity)
                                    withContext(Dispatchers.Main) {
                                        val msg =
                                            if (success) getString(R.string.msg_db_backup_saved) else "Failed to export database"
                                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG)
                                            .show()
                                    }
                                }
                            },
                            onImportDbClick = {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    val success = SqlExporter.importDatabase(this@MainActivity)
                                    withContext(Dispatchers.Main) {
                                        if (success) {
                                            Toast.makeText(
                                                this@MainActivity,
                                                R.string.msg_db_imported,
                                                Toast.LENGTH_LONG
                                            ).show()
                                            val intent =
                                                packageManager.getLaunchIntentForPackage(packageName)
                                            if (intent != null) {
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                startActivity(intent)
                                                Runtime.getRuntime().exit(0)
                                            }
                                        } else {
                                            Toast.makeText(
                                                this@MainActivity,
                                                R.string.msg_db_import_failed,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            },
                            onCloseAppClick = {
                                finishAffinity()
                            },
                            onUserMessageShown = viewModel::userMessageShown,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper?.shutdown()
    }

    override fun onResume() {
        super.onResume()
        if (nfcAdapter != null && pendingIntent != null) {
            try {
                nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (nfcAdapter != null) {
            try {
                nfcAdapter?.disableForegroundDispatch(this)
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    private fun handleNfcIntent(intent: Intent) {
        val action = intent.action
        if (NfcAdapter.ACTION_TAG_DISCOVERED == action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == action) {

            var tagContent: String? = null

            val rawMsgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, NdefMessage::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            }

            if (!rawMsgs.isNullOrEmpty()) {
                val msgs = rawMsgs.mapNotNull { it as? NdefMessage }
                for (msg in msgs) {
                    for (record in msg.records) {
                        if (record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type.contentEquals(
                                NdefRecord.RTD_TEXT
                            )
                        ) {
                            try {
                                val payload = record.payload
                                val textEncoding = if ((payload[0].toInt() and 128) == 0) "UTF-8" else "UTF-16"
                                val languageCodeLength = payload[0].toInt() and 63
                                tagContent = String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, Charset.forName(textEncoding))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }

            if (tagContent == null) {
                val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                }
                if (tag != null) {
                    val id = tag.id
                    tagContent = id.joinToString("") { "%02X".format(it) }
                }
            }


            if (!tagContent.isNullOrBlank()) {
                viewModelRef?.onNfcTagScanned(tagContent)
            }
        }
    }
}
