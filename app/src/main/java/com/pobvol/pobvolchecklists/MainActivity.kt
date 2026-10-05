package com.pobvol.pobvolchecklists

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pobvol.pobvolchecklists.data.local.AppDatabase
import com.pobvol.pobvolchecklists.data.repository.CategoryRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.ChecklistAnswerRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.ChecklistQuestionRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.ChecklistRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.ChecklistSubmissionRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.LanguageRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.SettingsRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.ThemeMode
import com.pobvol.pobvolchecklists.ui.ChecklistViewModel
import com.pobvol.pobvolchecklists.ui.screens.ChecklistListScreen
import com.pobvol.pobvolchecklists.ui.screens.ChecklistQuestionsScreen
import com.pobvol.pobvolchecklists.ui.screens.FillChecklistScreen
import com.pobvol.pobvolchecklists.ui.screens.StartScreen
import com.pobvol.pobvolchecklists.ui.screens.SubmissionDetailScreen
import com.pobvol.pobvolchecklists.ui.screens.SubmissionsOverviewScreen
import com.pobvol.pobvolchecklists.ui.theme.pobvolchecklistsTheme

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var viewModelRef: ChecklistViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

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

            val darkTheme = when (uiState.userSettings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            pobvolchecklistsTheme(darkTheme = darkTheme) {
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
                        onSelectChecklist = viewModel::openFillChecklist,
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
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
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
            val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
            val tagIdBytes = tag?.id
            if (tagIdBytes != null) {
                val tagIdHex = tagIdBytes.joinToString(":") { "%02X".format(it) }
                viewModelRef?.onNfcTagScanned(tagIdHex)
            }
        }
    }
}
