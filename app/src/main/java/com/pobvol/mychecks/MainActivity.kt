package com.pobvol.mychecks

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pobvol.mychecks.data.local.AppDatabase
import com.pobvol.mychecks.data.repository.CategoryRepositoryImpl
import com.pobvol.mychecks.data.repository.ChecklistAnswerRepositoryImpl
import com.pobvol.mychecks.data.repository.ChecklistQuestionRepositoryImpl
import com.pobvol.mychecks.data.repository.ChecklistRepositoryImpl
import com.pobvol.mychecks.data.repository.ChecklistSubmissionRepositoryImpl
import com.pobvol.mychecks.data.repository.LanguageRepositoryImpl
import com.pobvol.mychecks.data.repository.SettingsRepositoryImpl
import com.pobvol.mychecks.data.repository.ThemeMode
import com.pobvol.mychecks.ui.ChecklistViewModel
import com.pobvol.mychecks.ui.screens.ChecklistListScreen
import com.pobvol.mychecks.ui.screens.ChecklistQuestionsScreen
import com.pobvol.mychecks.ui.screens.FillChecklistScreen
import com.pobvol.mychecks.ui.screens.StartScreen
import com.pobvol.mychecks.ui.screens.SubmissionDetailScreen
import com.pobvol.mychecks.ui.screens.SubmissionsOverviewScreen
import com.pobvol.mychecks.ui.theme.mychecksTheme

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var viewModelRef: ChecklistViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
        val questionRepository = ChecklistQuestionRepositoryImpl(database.checklistquestionDao())
        val languageRepository = LanguageRepositoryImpl(database.languageDao())
        val settingsRepository = SettingsRepositoryImpl(applicationContext)
        val categoryRepository = CategoryRepositoryImpl(database.categoryDao())
        val submissionRepository = ChecklistSubmissionRepositoryImpl(database.checklistsubmissionDao())
        val answerRepository = ChecklistAnswerRepositoryImpl(database.checklistanswerDao())

        setContent {
            val viewModel: ChecklistViewModel = viewModel(
                factory = ChecklistViewModel.Factory(
                    repository = repository,
                    questionRepository = questionRepository,
                    languageRepository = languageRepository,
                    settingsRepository = settingsRepository,
                    categoryRepository = categoryRepository,
                    submissionRepository = submissionRepository,
                    answerRepository = answerRepository,
                ),
            )
            viewModelRef = viewModel
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                val selectedSubmissionToEdit = uiState.selectedSubmissionToEdit
                val selectedChecklistToAnswer = uiState.selectedChecklistToAnswer
                val selectedChecklistForQuestions = uiState.selectedChecklistForQuestions

                if (selectedSubmissionToEdit != null) {
                    val checklist = uiState.checklists.find { it.id == selectedSubmissionToEdit.checklistid }
                    SubmissionDetailScreen(
                        submission = selectedSubmissionToEdit,
                        checklist = checklist,
                        uiState = uiState,
                        onBackClick = viewModel::closeEditSubmission,
                        onSaveSubmission = viewModel::updateSubmissionAndAnswers,
                        onUserMessageShown = viewModel::userMessageShown,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else if (uiState.isSubmissionsOverviewVisible) {
                    SubmissionsOverviewScreen(
                        uiState = uiState,
                        onSearchQueryChange = viewModel::onSearchQueryChange,
                        onStatusFilterChange = viewModel::onSubmissionStatusFilterChange,
                        onClearChecklistFilter = viewModel::clearChecklistSubmissionsFilter,
                        onBackClick = viewModel::closeSubmissionsOverview,
                        onEditSubmissionClick = viewModel::openEditSubmission,
                        onDeleteSubmissionClick = viewModel::requestDeleteSubmission,
                        onDismissDeleteSubmissionDialog = viewModel::dismissDeleteSubmission,
                        onConfirmDeleteSubmission = viewModel::confirmDeleteSubmission,
                        onUserMessageShown = viewModel::userMessageShown,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else if (selectedChecklistToAnswer != null) {
                    FillChecklistScreen(
                        checklist = selectedChecklistToAnswer,
                        uiState = uiState,
                        onBackClick = viewModel::closeFillChecklist,
                        onSubmit = { checklistId, inspector, notes, date, answers ->
                            viewModel.submitChecklistAnswers(
                                checklistId = checklistId,
                                inspector = inspector,
                                notes = notes,
                                date = date,
                                answers = answers,
                            )
                        },
                        onUserMessageShown = viewModel::userMessageShown,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else if (selectedChecklistForQuestions != null) {
                    ChecklistQuestionsScreen(
                        checklist = selectedChecklistForQuestions,
                        uiState = uiState,
                        onBackClick = viewModel::closeQuestionsScreen,
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
                        onSearchQueryChange = viewModel::onSearchQueryChange,
                        onCategoryFilterChange = viewModel::onCategoryFilterChange,
                        onAddClick = viewModel::openAddDialog,
                        onSelectChecklist = viewModel::openSubmissionsForChecklist,
                        onFillClick = viewModel::openFillChecklist,
                        onQuestionsClick = viewModel::openQuestionsForChecklist,
                        onEditClick = viewModel::openEditDialog,
                        onDeleteClick = viewModel::requestDeleteConfirmation,
                        onDismissAddEditDialog = viewModel::dismissAddEditDialog,
                        onSaveChecklist = viewModel::saveChecklist,
                        onDismissDeleteDialog = viewModel::dismissDeleteConfirmation,
                        onConfirmDelete = viewModel::confirmDelete,
                        onUserMessageShown = viewModel::userMessageShown,
                        onBackClick = viewModel::closeChecklistsScreen,
                        /*onOpenSubmissionsOverviewClick = viewModel::openSubmissionsOverview,*/
                        /*onOpenSettingsClick = viewModel::openSettingsDialog,*/
                        onDismissSettingsDialog = viewModel::dismissSettingsDialog,
                        onThemeModeSelected = viewModel::updateThemeMode,
                        onLanguageSelected = viewModel::updatePreferredLanguage,
                        onUserNameChanged = viewModel::updateUserName,
                        onAddCategory = viewModel::addCategory,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    StartScreen(
                        uiState = uiState,
                        onOpenChecklistsClick = viewModel::openChecklistsScreen,
                        onOpenSubmissionsClick = viewModel::openSubmissionsOverview,
                        onOpenSettingsClick = viewModel::openSettingsDialog,
                        onDismissSettingsDialog = viewModel::dismissSettingsDialog,
                        onThemeModeSelected = viewModel::updateThemeMode,
                        onLanguageSelected = viewModel::updatePreferredLanguage,
                        onUserNameChanged = viewModel::updateUserName,
                        onAddCategory = viewModel::addCategory,
                        onDeleteCategory = viewModel::deleteCategory,
                        onUserMessageShown = viewModel::userMessageShown,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
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

            // 1. Try reading NDEF message records (text payload)
            val rawMsgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, NdefMessage::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            }

            if (!rawMsgs.isNullOrEmpty()) {
                val msgs = rawMsgs.mapNotNull { it as? NdefMessage }
                val records = msgs.flatMap { it.records.toList() }
                for (record in records) {
                    val payload = record.payload
                    if (payload != null && payload.isNotEmpty()) {
                        try {
                            val textEncoding = if ((payload[0].toInt() and 128) == 0) "UTF-8" else "UTF-16"
                            val languageCodeLength = payload[0].toInt() and 63
                            val parsedText = String(
                                payload,
                                languageCodeLength + 1,
                                payload.size - languageCodeLength - 1,
                                charset(textEncoding)
                            )
                            if (parsedText.isNotBlank()) {
                                tagContent = parsedText
                                break
                            }
                        } catch (_: Exception) {
                            tagContent = String(payload, Charsets.UTF_8)
                        }
                    }
                }
            }

            // 2. Fallback to Tag UID if no NDEF payload
            if (tagContent.isNullOrBlank()) {
                val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                }
                val tagIdBytes = tag?.id
                if (tagIdBytes != null) {
                    tagContent = tagIdBytes.joinToString(":") { "%02X".format(it) }
                }
            }

            if (!tagContent.isNullOrBlank()) {
                viewModelRef?.onNfcTagScanned(tagContent)
            }
        }
    }
}
