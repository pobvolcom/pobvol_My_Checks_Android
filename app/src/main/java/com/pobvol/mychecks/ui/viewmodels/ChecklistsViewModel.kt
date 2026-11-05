/*
 *  My software is <a href="https://en.wikipedia.org/wiki/Free_software" target="_blank" rel="nofollow">Free Software</a>☯ and delivered as open source via <a href="https://github.com/pobvolcom/" target="_blank" rel="nofollow">GitHub</a>☯. You can download, install, operate, modify, and redistribute my software under the terms of the <a href="https://www.gnu.org/licenses/gpl-3.0" target="_blank" rel="nofollow">GNU General Public License v3</a>☯. Copyright © 2026 Volker Pobloth, pobvol Software Services, poppi66292@gmail.com. Developed with Google Android Studio and Gemini 3.5 Flash Lite and Gemini 3.6 Flash
 */

package com.pobvol.mychecks.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pobvol.mychecks.data.local.ChecklistEntity
import com.pobvol.mychecks.data.local.ChecklistQuestionEntity
import com.pobvol.mychecks.data.repository.CategoryRepository
import com.pobvol.mychecks.data.repository.ChecklistQuestionRepository
import com.pobvol.mychecks.data.repository.ChecklistRepository
import com.pobvol.mychecks.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChecklistsUiState(
    val checklists: List<ChecklistEntity> = emptyList(),
    val allChecklists: List<ChecklistEntity> = emptyList(),
    val checklistSearchQuery: String = "",
    val selectedCategory: String? = null,
    val isAddEditChecklistDialogVisible: Boolean = false,
    val checklistToEdit: ChecklistEntity? = null,
    val checklistToDelete: ChecklistEntity? = null,
    val checklistToCopy: ChecklistEntity? = null,
)

class ChecklistsViewModel(
    private val checklistRepository: ChecklistRepository,
    private val questionRepository: ChecklistQuestionRepository,
    private val settingsRepository: SettingsRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _checklistSearchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _isAddEditChecklistDialogVisible = MutableStateFlow(false)
    private val _checklistToEdit = MutableStateFlow<ChecklistEntity?>(null)
    private val _checklistToDelete = MutableStateFlow<ChecklistEntity?>(null)
    private val _checklistToCopy = MutableStateFlow<ChecklistEntity?>(null)

    val uiState: StateFlow<ChecklistsUiState> = combine(
        checklistRepository.getAllChecklists(),
        settingsRepository.userSettings,
        categoryRepository.getAllCategories(),
        _checklistSearchQuery,
        _selectedCategory,
        _isAddEditChecklistDialogVisible,
        _checklistToEdit,
        _checklistToDelete,
        _checklistToCopy,
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val rawChecklists = flows[0] as List<ChecklistEntity>
        val settings = flows[1] as com.pobvol.mychecks.data.repository.UserSettings

        @Suppress("UNCHECKED_CAST")
        val categories = flows[2] as List<com.pobvol.mychecks.data.local.CategoryEntity>
        val query = flows[3] as String
        val category = flows[4] as String?
        val isAddEditVisible = flows[5] as Boolean
        val toEdit = flows[6] as ChecklistEntity?
        val toDelete = flows[7] as ChecklistEntity?
        val toCopy = flows[8] as ChecklistEntity?

        val targetLang = normalizeLanguage(settings.preferredLanguage)
        val effective = rawChecklists.filter { normalizeLanguage(it.language) == targetLang }

        val filtered = effective.filter { checklist ->
            val matchesQuery = query.isBlank() ||
                    checklist.title.contains(query, ignoreCase = true) ||
                    checklist.description.contains(query, ignoreCase = true) ||
                    checklist.category.contains(query, ignoreCase = true)

            val matchesCategory = category == null ||
                    checklist.category.equals(category, ignoreCase = true) ||
                    categories.any { cat ->
                        (cat.title.equals(category, ignoreCase = true) || cat.category.equals(
                            category,
                            ignoreCase = true
                        )) &&
                                (checklist.category.equals(
                                    cat.category,
                                    ignoreCase = true
                                ) || checklist.category.equals(cat.title, ignoreCase = true))
                    }

            matchesQuery && matchesCategory
        }

        ChecklistsUiState(
            checklists = filtered,
            allChecklists = rawChecklists,
            checklistSearchQuery = query,
            selectedCategory = category,
            isAddEditChecklistDialogVisible = isAddEditVisible,
            checklistToEdit = toEdit,
            checklistToDelete = toDelete,
            checklistToCopy = toCopy,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChecklistsUiState()
    )

    private fun normalizeLanguage(lang: String): String {
        val trimmed = lang.trim()
        return when {
            trimmed.startsWith("de", ignoreCase = true) -> "de"
            trimmed.startsWith("en", ignoreCase = true) -> "en"
            trimmed.startsWith("es", ignoreCase = true) -> "es"
            trimmed.startsWith("fr", ignoreCase = true) -> "fr"
            else -> trimmed
        }
    }

    fun setChecklistSearchQuery(query: String) {
        _checklistSearchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun openAddChecklistDialog() {
        _checklistToEdit.value = null; _isAddEditChecklistDialogVisible.value = true
    }

    fun openEditChecklistDialog(checklist: ChecklistEntity) {
        _checklistToEdit.value = checklist; _isAddEditChecklistDialogVisible.value = true
    }

    fun dismissAddEditChecklistDialog() {
        _isAddEditChecklistDialogVisible.value = false; _checklistToEdit.value = null
    }

    fun requestDeleteChecklist(checklist: ChecklistEntity) {
        _checklistToDelete.value = checklist
    }

    fun dismissDeleteChecklistDialog() {
        _checklistToDelete.value = null
    }

    fun requestCopyChecklist(checklist: ChecklistEntity) {
        _checklistToCopy.value = checklist
    }

    fun dismissCopyChecklistDialog() {
        _checklistToCopy.value = null
    }

    fun confirmDeleteChecklist() {
        val target = _checklistToDelete.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            checklistRepository.deleteChecklist(target)
            _checklistToDelete.value = null
        }
    }

    fun confirmCopyChecklist(newTitle: String, newLanguage: String) {
        val source = _checklistToCopy.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val newChecklist = ChecklistEntity(
                title = newTitle,
                language = newLanguage,
                description = source.description,
                category = source.category,
                icon = source.icon,
            )
            val newId = checklistRepository.insertChecklist(newChecklist)
            val sourceQuestions =
                questionRepository.getChecklistQuestionsByChecklistId(source.id).first()
            sourceQuestions.forEach { q ->
                questionRepository.insertChecklistQuestion(
                    ChecklistQuestionEntity(
                        checklistid = newId.toString(),
                        title = q.title,
                        description = q.description,
                        type = q.type,
                        options = q.options,
                        required = q.required,
                        sortno = q.sortno,
                        minValue = q.minValue,
                        maxValue = q.maxValue,
                    )
                )
            }
            _checklistToCopy.value = null
        }
    }

    fun addOrUpdateChecklist(
        title: String,
        language: String,
        description: String,
        category: String,
        icon: String?
    ) {
        val currentEdit = _checklistToEdit.value
        viewModelScope.launch(Dispatchers.IO) {
            if (currentEdit == null) {
                val newChecklist = ChecklistEntity(
                    title = title,
                    language = language,
                    description = description,
                    category = category,
                    icon = icon,
                )
                checklistRepository.insertChecklist(newChecklist)
            } else {
                val updated = currentEdit.copy(
                    title = title,
                    language = language,
                    description = description,
                    category = category,
                    icon = icon,
                    timestamp = System.currentTimeMillis(),
                )
                checklistRepository.updateChecklist(updated)
            }
            _isAddEditChecklistDialogVisible.value = false
            _checklistToEdit.value = null
        }
    }

    class Factory(
        private val checklistRepository: ChecklistRepository,
        private val questionRepository: ChecklistQuestionRepository,
        private val settingsRepository: SettingsRepository,
        private val categoryRepository: CategoryRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChecklistsViewModel(
                checklistRepository,
                questionRepository,
                settingsRepository,
                categoryRepository,
            ) as T
        }
    }
}
