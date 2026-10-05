package com.pobvol.pobvolchecklists.data.repository

import kotlinx.coroutines.flow.Flow

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val preferredLanguage: String = "en",
    val userName: String = ""
)

interface SettingsRepository {
    val userSettings: Flow<UserSettings>
    suspend fun updateThemeMode(themeMode: ThemeMode)
    suspend fun updatePreferredLanguage(language: String)
    suspend fun updateUserName(userName: String)
}
