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
    val pdfIncludeChecklistTitle: Boolean = true,
    val pdfIncludeDate: Boolean = true,
    val pdfIncludeInspector: Boolean = true,
    val pdfIncludeStatus: Boolean = true,
    val pdfIncludeNotes: Boolean = true,
    val includeTimestamp: Boolean = true,
    val pdfIncludePageNumber: Boolean = true,
    val includeUnansweredQuestions: Boolean = true,
    val pdfIncludeQuestionTitle: Boolean = true,
    val pdfIncludeAnswerValue: Boolean = true,
    val pdfIncludeAnswerNotes: Boolean = true,
    val pdfIncludeAnswerPicture: Boolean = true,
    val compactAnswerLayout: Boolean = false,
    val pdfFooterText: String = "",
)

interface SettingsRepository {
    val userSettings: Flow<UserSettings>
    suspend fun updateThemeMode(themeMode: ThemeMode)
    suspend fun updatePreferredLanguage(language: String)
    suspend fun updateUserName(userName: String)
    suspend fun updatePdfHeaderTitle(title: String)
    suspend fun updatePdfIncludeChecklistTitle(include: Boolean)
    suspend fun updatePdfIncludeDate(include: Boolean)
    suspend fun updatePdfIncludeInspector(include: Boolean)
    suspend fun updatePdfIncludeStatus(include: Boolean)
    suspend fun updatePdfIncludeNotes(include: Boolean)
    suspend fun updatePdfIncludeTimestamp(include: Boolean)
    suspend fun updatePdfIncludePageNumber(include: Boolean)
    suspend fun updatePdfIncludeUnansweredQuestions(include: Boolean)
    suspend fun updatePdfIncludeQuestionTitle(include: Boolean)
    suspend fun updatePdfIncludeAnswerValue(include: Boolean)
    suspend fun updatePdfIncludeAnswerNotes(include: Boolean)
    suspend fun updatePdfIncludeAnswerPicture(include: Boolean)
    suspend fun updatePdfCompactAnswerLayout(compact: Boolean)
    suspend fun updatePdfFooterText(text: String)
}
