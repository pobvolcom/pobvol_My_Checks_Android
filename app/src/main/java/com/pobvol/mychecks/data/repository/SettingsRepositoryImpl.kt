package com.pobvol.mychecks.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class SettingsRepositoryImpl(
    private val context: Context,
) : SettingsRepository {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
        val USER_NAME = stringPreferencesKey("user_name")
        val PDF_HEADER_TITLE = stringPreferencesKey("pdf_header_title")
        val PDF_INCLUDE_CHECKLIST_TITLE = booleanPreferencesKey("pdf_include_checklist_title")
        val PDF_INCLUDE_DATE = booleanPreferencesKey("pdf_include_date")
        val PDF_INCLUDE_INSPECTOR = booleanPreferencesKey("pdf_include_inspector")
        val PDF_INCLUDE_STATUS = booleanPreferencesKey("pdf_include_status")
        val PDF_INCLUDE_NOTES = booleanPreferencesKey("pdf_include_notes")
        val INCLUDE_TIMESTAMP = booleanPreferencesKey("include_timestamp")
        val PDF_INCLUDE_PAGE_NUMBER = booleanPreferencesKey("pdf_include_page_number")
        val INCLUDE_UNANSWERED_QUESTIONS = booleanPreferencesKey("include_unanswered_questions")
        val PDF_INCLUDE_QUESTION_TITLE = booleanPreferencesKey("pdf_include_question_title")
        val PDF_INCLUDE_QUESTION_DESCRIPTION =
            booleanPreferencesKey("pdf_include_question_description")
        val PDF_INCLUDE_ANSWER_VALUE = booleanPreferencesKey("pdf_include_answer_value")
        val INCLUDE_ANSWER_NOTES = booleanPreferencesKey("include_answer_notes")
        val PDF_INCLUDE_ANSWER_PICTURE = booleanPreferencesKey("pdf_include_answer_picture")
        val COMPACT_ANSWER_LAYOUT = booleanPreferencesKey("compact_answer_layout")
        val PDF_FOOTER_TEXT = stringPreferencesKey("pdf_footer_text")
        val PDF_TEMPLATE_VERSION = stringPreferencesKey("pdf_template_version")
        val AUDIO_FEEDBACK = booleanPreferencesKey("audio_feedback")
        val FTP_SERVER = stringPreferencesKey("ftp_server")
        val FTP_PORT = intPreferencesKey("ftp_port")
        val FTP_USE_TLS = booleanPreferencesKey("ftp_use_tls")
        val FTP_USER = stringPreferencesKey("ftp_user")
        val FTP_PASSWORD = stringPreferencesKey("ftp_password")
        val IS_LICENSE_ACCEPTED = booleanPreferencesKey("is_license_accepted")
        val PICTURE_SIZE = stringPreferencesKey("picture_size")
        val PDF_PICTURE_SIZE = stringPreferencesKey("pdf_picture_size")
        val PDF_PICTURE_SIZE_SMALL = intPreferencesKey("pdf_picture_size_small")
        val PDF_PICTURE_SIZE_MEDIUM = intPreferencesKey("pdf_picture_size_medium")
        val PDF_PICTURE_SIZE_LARGE = intPreferencesKey("pdf_picture_size_large")
        val SIGNATURE_PATH = stringPreferencesKey("signature_path")
    }

    override val userSettings: Flow<UserSettings> = context.dataStore.data
        .map { preferences ->
            val themeModeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            val themeMode = try {
                ThemeMode.valueOf(themeModeStr)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
            val preferredLanguage = preferences[PreferencesKeys.PREFERRED_LANGUAGE] ?: "en"

            try {
                val prefs = context.getSharedPreferences("my_checks_prefs", Context.MODE_PRIVATE)
                if (prefs.getString("saved_language", null) == null) {
                    prefs.edit().putString("saved_language", preferredLanguage).apply()
                }
            } catch (_: Exception) {
            }

            val userName = preferences[PreferencesKeys.USER_NAME] ?: ""
            val pdfHeaderTitle = preferences[PreferencesKeys.PDF_HEADER_TITLE] ?: "My Checks Inspection Report"
            val pdfIncludeChecklistTitle =
                preferences[PreferencesKeys.PDF_INCLUDE_CHECKLIST_TITLE] ?: true
            val pdfIncludeDate = preferences[PreferencesKeys.PDF_INCLUDE_DATE] ?: true
            val pdfIncludeInspector = preferences[PreferencesKeys.PDF_INCLUDE_INSPECTOR] ?: true
            val pdfIncludeStatus = preferences[PreferencesKeys.PDF_INCLUDE_STATUS] ?: true
            val pdfIncludeNotes = preferences[PreferencesKeys.PDF_INCLUDE_NOTES] ?: true
            val includeTimestamp = preferences[PreferencesKeys.INCLUDE_TIMESTAMP] ?: true
            val pdfIncludePageNumber = preferences[PreferencesKeys.PDF_INCLUDE_PAGE_NUMBER] ?: true
            val includeUnansweredQuestions = preferences[PreferencesKeys.INCLUDE_UNANSWERED_QUESTIONS] ?: true
            val pdfIncludeQuestionTitle =
                preferences[PreferencesKeys.PDF_INCLUDE_QUESTION_TITLE] ?: true
            val pdfIncludeQuestionDescription =
                preferences[PreferencesKeys.PDF_INCLUDE_QUESTION_DESCRIPTION] ?: false
            val pdfIncludeAnswerValue =
                preferences[PreferencesKeys.PDF_INCLUDE_ANSWER_VALUE] ?: true
            val includeAnswerNotes = preferences[PreferencesKeys.INCLUDE_ANSWER_NOTES] ?: true
            val pdfIncludeAnswerPicture =
                preferences[PreferencesKeys.PDF_INCLUDE_ANSWER_PICTURE] ?: true
            val compactAnswerLayout = preferences[PreferencesKeys.COMPACT_ANSWER_LAYOUT] ?: false
            val pdfFooterText = preferences[PreferencesKeys.PDF_FOOTER_TEXT] ?: ""
            val pdfTemplateVersion = preferences[PreferencesKeys.PDF_TEMPLATE_VERSION] ?: "V1"
            val audioFeedback = preferences[PreferencesKeys.AUDIO_FEEDBACK] ?: false
            val ftpServer = preferences[PreferencesKeys.FTP_SERVER] ?: ""
            val ftpPort = preferences[PreferencesKeys.FTP_PORT] ?: 21
            val ftpUseTls = preferences[PreferencesKeys.FTP_USE_TLS] ?: false
            val ftpUser = preferences[PreferencesKeys.FTP_USER] ?: ""
            val ftpPassword = preferences[PreferencesKeys.FTP_PASSWORD] ?: ""
            val isLicenseAccepted = preferences[PreferencesKeys.IS_LICENSE_ACCEPTED] ?: false
            val pdfPictureSize = preferences[PreferencesKeys.PDF_PICTURE_SIZE] ?: "medium"
            val pdfPictureSizeSmall = preferences[PreferencesKeys.PDF_PICTURE_SIZE_SMALL] ?: 100
            val pdfPictureSizeMedium = preferences[PreferencesKeys.PDF_PICTURE_SIZE_MEDIUM] ?: 200
            val pdfPictureSizeLarge = preferences[PreferencesKeys.PDF_PICTURE_SIZE_LARGE] ?: 300
            val signaturePath = preferences[PreferencesKeys.SIGNATURE_PATH] ?: ""

            UserSettings(
                themeMode = themeMode,
                preferredLanguage = preferredLanguage,
                userName = userName,
                pdfHeaderTitle = pdfHeaderTitle,
                pdfIncludeChecklistTitle = pdfIncludeChecklistTitle,
                pdfIncludeDate = pdfIncludeDate,
                pdfIncludeInspector = pdfIncludeInspector,
                pdfIncludeStatus = pdfIncludeStatus,
                pdfIncludeNotes = pdfIncludeNotes,
                includeTimestamp = includeTimestamp,
                pdfIncludePageNumber = pdfIncludePageNumber,
                includeUnansweredQuestions = includeUnansweredQuestions,
                pdfIncludeQuestionTitle = pdfIncludeQuestionTitle,
                pdfIncludeQuestionDescription = pdfIncludeQuestionDescription,
                pdfIncludeAnswerValue = pdfIncludeAnswerValue,
                pdfIncludeAnswerNotes = includeAnswerNotes,
                pdfIncludeAnswerPicture = pdfIncludeAnswerPicture,
                compactAnswerLayout = compactAnswerLayout,
                pdfFooterText = pdfFooterText,
                pdfTemplateVersion = pdfTemplateVersion,
                audioFeedback = audioFeedback,
                ftpServer = ftpServer,
                ftpPort = ftpPort,
                ftpUseTls = ftpUseTls,
                ftpUser = ftpUser,
                ftpPassword = ftpPassword,
                isLicenseAccepted = isLicenseAccepted,
                pdfPictureSize = pdfPictureSize,
                pdfPictureSizeSmall = pdfPictureSizeSmall,
                pdfPictureSizeMedium = pdfPictureSizeMedium,
                pdfPictureSizeLarge = pdfPictureSizeLarge,
                signaturePath = signaturePath,
            )
        }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    override suspend fun updatePreferredLanguage(language: String) {
        try {
            context.getSharedPreferences("my_checks_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("saved_language", language)
                .apply()
        } catch (_: Exception) {
        }

        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREFERRED_LANGUAGE] = language
        }
    }

    override suspend fun updateUserName(userName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = userName
        }
    }

    override suspend fun updatePdfHeaderTitle(title: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PDF_HEADER_TITLE] = title
        }
    }

    override suspend fun updatePdfIncludeChecklistTitle(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PDF_INCLUDE_CHECKLIST_TITLE] = include
        }
    }

    override suspend fun updatePdfIncludeDate(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PDF_INCLUDE_DATE] = include
        }
    }

    override suspend fun updatePdfIncludeInspector(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PDF_INCLUDE_INSPECTOR] = include
        }
    }

    override suspend fun updatePdfIncludeStatus(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PDF_INCLUDE_STATUS] = include
        }
    }

    override suspend fun updatePdfIncludeNotes(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PDF_INCLUDE_NOTES] = include
        }
    }

    override suspend fun updatePdfIncludeTimestamp(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INCLUDE_TIMESTAMP] = include
        }
    }

    override suspend fun updatePdfIncludePageNumber(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PDF_INCLUDE_PAGE_NUMBER] = include
        }
    }

    override suspend fun updatePdfIncludeUnansweredQuestions(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INCLUDE_UNANSWERED_QUESTIONS] = include
        }
    }

    override suspend fun updatePdfIncludeQuestionTitle(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PDF_INCLUDE_QUESTION_TITLE] = include
        }
    }

    override suspend fun updatePdfIncludeQuestionDescription(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PDF_INCLUDE_QUESTION_DESCRIPTION] = include
        }
    }

    override suspend fun updatePdfIncludeAnswerValue(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PDF_INCLUDE_ANSWER_VALUE] = include
        }
    }

    override suspend fun updatePdfIncludeAnswerNotes(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INCLUDE_ANSWER_NOTES] = include
        }
    }

    override suspend fun updatePdfIncludeAnswerPicture(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PDF_INCLUDE_ANSWER_PICTURE] = include
        }
    }

    override suspend fun updatePdfCompactAnswerLayout(compact: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COMPACT_ANSWER_LAYOUT] = compact
        }
    }

    override suspend fun updatePdfFooterText(text: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PDF_FOOTER_TEXT] = text
        }
    }

    override suspend fun updatePdfTemplateVersion(version: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PDF_TEMPLATE_VERSION] = version
        }
    }

    override suspend fun updateAudioFeedback(audioFeedback: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUDIO_FEEDBACK] = audioFeedback
        }
    }

    override suspend fun updateFtpServer(server: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FTP_SERVER] = server
        }
    }

    override suspend fun updateFtpPort(port: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FTP_PORT] = port
        }
    }

    override suspend fun updateFtpUseTls(useTls: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FTP_USE_TLS] = useTls
        }
    }

    override suspend fun updateFtpUser(user: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FTP_USER] = user
        }
    }

    override suspend fun updateFtpPassword(password: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FTP_PASSWORD] = password
        }
    }

    override suspend fun acceptLicense() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LICENSE_ACCEPTED] = true
        }
    }

    override suspend fun updatePdfPictureSize(size: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PDF_PICTURE_SIZE] = size
        }
    }

    override suspend fun updatePdfPictureSizeSmall(size: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PDF_PICTURE_SIZE_SMALL] = size
        }
    }

    override suspend fun updatePdfPictureSizeMedium(size: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PDF_PICTURE_SIZE_MEDIUM] = size
        }
    }

    override suspend fun updatePdfPictureSizeLarge(size: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PDF_PICTURE_SIZE_LARGE] = size
        }
    }

    override suspend fun updateSignaturePath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SIGNATURE_PATH] = path
        }
    }

    override suspend fun updatePictureSize(size: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PICTURE_SIZE] = size
        }
    }
}
