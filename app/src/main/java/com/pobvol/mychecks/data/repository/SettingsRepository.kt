package com.pobvol.mychecks.data.repository

import kotlinx.coroutines.flow.Flow

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val preferredLanguage: String = "en",
    val userName: String = "",
    val pdfHeaderTitle: String = "My Checks Inspection Report",
    val includeTimestamp: Boolean = true,
    val includeInspector: Boolean = true,
    val includeUnansweredQuestions: Boolean = true,
    val includeAnswerNotes: Boolean = true,
    val compactAnswerLayout: Boolean = false,
)

interface SettingsRepository {
    val userSettings: Flow<UserSettings>
    suspend fun updateThemeMode(themeMode: ThemeMode)
    suspend fun updatePreferredLanguage(language: String)
    suspend fun updateUserName(userName: String)
    suspend fun updatePdfHeaderTitle(title: String)
    suspend fun updatePdfIncludeTimestamp(include: Boolean)
    suspend fun updatePdfIncludeInspector(include: Boolean)
    suspend fun updatePdfIncludeUnansweredQuestions(include: Boolean)
    suspend fun updatePdfIncludeAnswerNotes(include: Boolean)
    suspend fun updatePdfCompactAnswerLayout(compact: Boolean)
}
