/*
 *  My software is <a href="https://en.wikipedia.org/wiki/Free_software" target="_blank" rel="nofollow">Free Software</a>☯ and delivered as open source via <a href="https://github.com/pobvolcom/" target="_blank" rel="nofollow">GitHub</a>☯. You can download, install, operate, modify, and redistribute my software under the terms of the <a href="https://www.gnu.org/licenses/gpl-3.0" target="_blank" rel="nofollow">GNU General Public License v3</a>☯. Copyright © 2026 Volker Pobloth, pobvol Software Services, poppi66292@gmail.com. Developed with Google Android Studio and Gemini 3.5 Flash Lite and Gemini 3.6 Flash
 */

package com.pobvol.mychecks.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pobvol.mychecks.data.local.CategoryEntity
import com.pobvol.mychecks.data.local.DeviceEntity
import com.pobvol.mychecks.data.local.LanguageEntity
import com.pobvol.mychecks.data.repository.CategoryRepository
import com.pobvol.mychecks.data.repository.DeviceRepository
import com.pobvol.mychecks.data.repository.LanguageRepository
import com.pobvol.mychecks.data.repository.SettingsRepository
import com.pobvol.mychecks.data.repository.ThemeMode
import com.pobvol.mychecks.data.repository.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userSettings: UserSettings = UserSettings(),
    val availableDevices: List<DeviceEntity> = emptyList(),
    val availableLanguages: List<LanguageEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val isSettingsDialogVisible: Boolean = false,
    val isPdfSettingsVisible: Boolean = false,
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val deviceRepository: DeviceRepository,
    private val languageRepository: LanguageRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _isSettingsDialogVisible = MutableStateFlow(false)
    private val _isPdfSettingsVisible = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.userSettings,
        deviceRepository.getDeviceId(),
        languageRepository.getAllLanguages(),
        categoryRepository.getAllCategories(),
        _isSettingsDialogVisible,
        _isPdfSettingsVisible,
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        SettingsUiState(
            userSettings = flows[0] as UserSettings,
            availableDevices = flows[1] as List<DeviceEntity>,
            availableLanguages = flows[2] as List<LanguageEntity>,
            categories = flows[3] as List<CategoryEntity>,
            isSettingsDialogVisible = flows[4] as Boolean,
            isPdfSettingsVisible = flows[5] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun openSettingsDialog() {
        _isSettingsDialogVisible.value = true
    }

    fun dismissSettingsDialog() {
        _isSettingsDialogVisible.value = false
    }

    fun openPdfSettingsDialog() {
        _isPdfSettingsVisible.value = true
    }

    fun dismissPdfSettingsDialog() {
        _isPdfSettingsVisible.value = false
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch { settingsRepository.updateThemeMode(themeMode) }
    }

    fun updatePreferredLanguage(language: String) {
        viewModelScope.launch { settingsRepository.updatePreferredLanguage(language) }
    }

    fun updateUserName(userName: String) {
        viewModelScope.launch { settingsRepository.updateUserName(userName) }
    }

    fun updateAudioFeedback(audioFeedback: Boolean) {
        viewModelScope.launch { settingsRepository.updateAudioFeedback(audioFeedback) }
    }

    fun addCategory(categoryName: String) {
        if (categoryName.isBlank()) return
        viewModelScope.launch {
            val title = categoryName.trim()
            categoryRepository.insertCategory(CategoryEntity(category = title, title = title))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch { categoryRepository.deleteCategory(category) }
    }

    fun updatePdfHeaderTitle(title: String) {
        viewModelScope.launch { settingsRepository.updatePdfHeaderTitle(title) }
    }

    fun updatePdfIncludeChecklistTitle(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeChecklistTitle(include) }
    }

    fun updatePdfIncludeChecklistDescription(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeChecklistDescription(include) }
    }

    fun updatePdfIncludeDate(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeDate(include) }
    }

    fun updatePdfIncludeInspector(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeInspector(include) }
    }

    fun updatePdfIncludeStatus(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeStatus(include) }
    }

    fun updatePdfIncludeNotes(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeNotes(include) }
    }

    fun updatePdfIncludeTimestamp(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeTimestamp(include) }
    }

    fun updatePdfIncludePageNumber(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludePageNumber(include) }
    }

    fun updatePdfIncludeUnansweredQuestions(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeUnansweredQuestions(include) }
    }

    fun updatePdfIncludeQuestionTitle(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeQuestionTitle(include) }
    }

    fun updatePdfIncludeQuestionDescription(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeQuestionDescription(include) }
    }

    fun updatePdfIncludeAnswerValue(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeAnswerValue(include) }
    }

    fun updatePdfIncludeAnswerNotes(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeAnswerNotes(include) }
    }

    fun updatePdfIncludeAnswerPicture(include: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfIncludeAnswerPicture(include) }
    }

    fun updatePdfCompactAnswerLayout(compact: Boolean) {
        viewModelScope.launch { settingsRepository.updatePdfCompactAnswerLayout(compact) }
    }

    fun updatePdfFooterText(text: String) {
        viewModelScope.launch { settingsRepository.updatePdfFooterText(text) }
    }

    fun updatePdfTemplateVersion(version: String) {
        viewModelScope.launch { settingsRepository.updatePdfTemplateVersion(version) }
    }

    fun updatePdfPictureSizeSmall(size: Int) {
        viewModelScope.launch { settingsRepository.updatePdfPictureSizeSmall(size) }
    }

    fun updatePdfPictureSizeMedium(size: Int) {
        viewModelScope.launch { settingsRepository.updatePdfPictureSizeMedium(size) }
    }

    fun updatePdfPictureSizeLarge(size: Int) {
        viewModelScope.launch { settingsRepository.updatePdfPictureSizeLarge(size) }
    }

    fun updateSignaturePath(path: String) {
        viewModelScope.launch { settingsRepository.updateSignaturePath(path) }
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val deviceRepository: DeviceRepository,
        private val languageRepository: LanguageRepository,
        private val categoryRepository: CategoryRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                settingsRepository,
                deviceRepository,
                languageRepository,
                categoryRepository,
            ) as T
        }
    }
}
