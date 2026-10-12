package com.pobvol.mychecks.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class SettingsRepositoryImpl(
    private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
        val USER_NAME = stringPreferencesKey("user_name")
        val PDF_HEADER_TITLE = stringPreferencesKey("pdf_header_title")
        val INCLUDE_TIMESTAMP = booleanPreferencesKey("include_timestamp")
        val INCLUDE_INSPECTOR = booleanPreferencesKey("include_inspector")
        val INCLUDE_UNANSWERED_QUESTIONS = booleanPreferencesKey("include_unanswered_questions")
        val INCLUDE_ANSWER_NOTES = booleanPreferencesKey("include_answer_notes")
        val COMPACT_ANSWER_LAYOUT = booleanPreferencesKey("compact_answer_layout")
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
            val userName = preferences[PreferencesKeys.USER_NAME] ?: ""
            val pdfHeaderTitle = preferences[PreferencesKeys.PDF_HEADER_TITLE] ?: "My Checks Inspection Report"
            val includeTimestamp = preferences[PreferencesKeys.INCLUDE_TIMESTAMP] ?: true
            val includeInspector = preferences[PreferencesKeys.INCLUDE_INSPECTOR] ?: true
            val includeUnansweredQuestions = preferences[PreferencesKeys.INCLUDE_UNANSWERED_QUESTIONS] ?: true
            val includeAnswerNotes = preferences[PreferencesKeys.INCLUDE_ANSWER_NOTES] ?: true
            val compactAnswerLayout = preferences[PreferencesKeys.COMPACT_ANSWER_LAYOUT] ?: false

            UserSettings(
                themeMode = themeMode,
                preferredLanguage = preferredLanguage,
                userName = userName,
                pdfHeaderTitle = pdfHeaderTitle,
                includeTimestamp = includeTimestamp,
                includeInspector = includeInspector,
                includeUnansweredQuestions = includeUnansweredQuestions,
                includeAnswerNotes = includeAnswerNotes,
                compactAnswerLayout = compactAnswerLayout,
            )
        }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    override suspend fun updatePreferredLanguage(language: String) {
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

    override suspend fun updatePdfIncludeTimestamp(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INCLUDE_TIMESTAMP] = include
        }
    }

    override suspend fun updatePdfIncludeInspector(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INCLUDE_INSPECTOR] = include
        }
    }

    override suspend fun updatePdfIncludeUnansweredQuestions(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INCLUDE_UNANSWERED_QUESTIONS] = include
        }
    }

    override suspend fun updatePdfIncludeAnswerNotes(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INCLUDE_ANSWER_NOTES] = include
        }
    }

    override suspend fun updatePdfCompactAnswerLayout(compact: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COMPACT_ANSWER_LAYOUT] = compact
        }
    }
}
