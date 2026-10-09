package com.pobvol.mychecks.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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

            UserSettings(
                themeMode = themeMode,
                preferredLanguage = preferredLanguage,
                userName = userName
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
}
