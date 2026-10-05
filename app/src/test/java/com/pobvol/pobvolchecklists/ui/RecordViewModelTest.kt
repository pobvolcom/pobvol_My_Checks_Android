package com.pobvol.pobvolchecklists.ui

import com.pobvol.pobvolchecklists.data.local.CategoryDAO
import com.pobvol.pobvolchecklists.data.local.CategoryEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistAnswerDAO
import com.pobvol.pobvolchecklists.data.local.ChecklistAnswerEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistDAO
import com.pobvol.pobvolchecklists.data.local.ChecklistEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistQuestionDAO
import com.pobvol.pobvolchecklists.data.local.ChecklistQuestionEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistSubmissionDAO
import com.pobvol.pobvolchecklists.data.local.ChecklistSubmissionEntity
import com.pobvol.pobvolchecklists.data.local.LanguageDAO
import com.pobvol.pobvolchecklists.data.local.LanguageEntity
import com.pobvol.pobvolchecklists.data.repository.CategoryRepository
import com.pobvol.pobvolchecklists.data.repository.CategoryRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.ChecklistAnswerRepository
import com.pobvol.pobvolchecklists.data.repository.ChecklistAnswerRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.ChecklistQuestionRepository
import com.pobvol.pobvolchecklists.data.repository.ChecklistQuestionRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.ChecklistRepository
import com.pobvol.pobvolchecklists.data.repository.ChecklistRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.ChecklistSubmissionRepository
import com.pobvol.pobvolchecklists.data.repository.ChecklistSubmissionRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.LanguageRepository
import com.pobvol.pobvolchecklists.data.repository.LanguageRepositoryImpl
import com.pobvol.pobvolchecklists.data.repository.SettingsRepository
import com.pobvol.pobvolchecklists.data.repository.ThemeMode
import com.pobvol.pobvolchecklists.data.repository.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeChecklistDao: FakeChecklistDao
    private lateinit var fakeQuestionDao: FakeChecklistQuestionDao
    private lateinit var fakeLanguageDao: FakeLanguageDao
    private lateinit var fakeCategoryDao: FakeCategoryDao
    private lateinit var fakeSubmissionDao: FakeSubmissionDao
    private lateinit var fakeAnswerDao: FakeAnswerDao
    private lateinit var repository: ChecklistRepository
    private lateinit var questionRepository: ChecklistQuestionRepository
    private lateinit var languageRepository: LanguageRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var submissionRepository: ChecklistSubmissionRepository
    private lateinit var answerRepository: ChecklistAnswerRepository
    private lateinit var viewModel: ChecklistViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeChecklistDao = FakeChecklistDao()
        fakeQuestionDao = FakeChecklistQuestionDao()
        fakeLanguageDao = FakeLanguageDao()
        fakeCategoryDao = FakeCategoryDao()
        fakeSubmissionDao = FakeSubmissionDao()
        fakeAnswerDao = FakeAnswerDao()
        repository = ChecklistRepositoryImpl(fakeChecklistDao)
        questionRepository = ChecklistQuestionRepositoryImpl(fakeQuestionDao)
        languageRepository = LanguageRepositoryImpl(fakeLanguageDao)
        settingsRepository = FakeSettingsRepository()
        categoryRepository = CategoryRepositoryImpl(fakeCategoryDao)
        submissionRepository = ChecklistSubmissionRepositoryImpl(fakeSubmissionDao)
        answerRepository = ChecklistAnswerRepositoryImpl(fakeAnswerDao)
        viewModel = ChecklistViewModel(
            repository,
            questionRepository,
            languageRepository,
            settingsRepository,
            categoryRepository,
            submissionRepository,
            answerRepository,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addChecklistInsertsToRepositoryAndUpdatesState() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.openAddDialog()
        viewModel.saveChecklist("Title 1", "en", "Desc 1", "Work", "")

        val checklists = repository.getAllChecklists().first()
        assertEquals(1, checklists.size)
        assertEquals("Title 1", checklists[0].title)
        assertEquals("Desc 1", checklists[0].description)
        assertEquals("Work", checklists[0].category)

        val uiState = viewModel.uiState.value
        assertEquals("Checklist added successfully", uiState.userMessage)
        assertFalse(uiState.isAddEditDialogVisible)
    }

    @Test
    fun editChecklistUpdatesRepository() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val original = ChecklistEntity(id = 1, title = "Original", language = "en", description = "Old Desc", category = "Personal", icon = "")
        repository.insertChecklist(original)

        viewModel.openEditDialog(original)
        viewModel.saveChecklist("Updated Title", "en", "New Desc", "Personal", "")

        val fetched = repository.getChecklistById(1)
        assertNotNull(fetched)
        assertEquals("Updated Title", fetched?.title)
        assertEquals("New Desc", fetched?.description)

        val uiState = viewModel.uiState.value
        assertEquals("Checklist updated successfully", uiState.userMessage)
    }

    @Test
    fun deleteChecklistRemovesFromRepository() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val checklist = ChecklistEntity(id = 1, title = "To Delete", language = "en", description = "Desc", category = "General", icon = "")
        repository.insertChecklist(checklist)

        viewModel.requestDeleteConfirmation(checklist)
        assertEquals(checklist, viewModel.uiState.value.checklistToDelete)

        viewModel.confirmDelete()

        val fetched = repository.getChecklistById(1)
        assertNull(fetched)
        assertNull(viewModel.uiState.value.checklistToDelete)
        assertEquals("Checklist deleted successfully", viewModel.uiState.value.userMessage)
    }

    @Test
    fun deleteChecklistRemovesChecklistAndRelatedQuestions() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val checklist = ChecklistEntity(id = 1, title = "To Delete", language = "en", description = "Desc", category = "General", icon = "")
        repository.insertChecklist(checklist)

        val question = ChecklistQuestionEntity(id = 1, checklistid = 1, title = "Q1", description = null, type = "text", options = null, required = true)
        questionRepository.insertChecklistQuestion(question)

        viewModel.requestDeleteConfirmation(checklist)
        viewModel.confirmDelete()

        val fetchedChecklist = repository.getChecklistById(1)
        assertNull(fetchedChecklist)

        val questions = questionRepository.getChecklistQuestionsByChecklistId(1).first()
        assertEquals(true, questions.isEmpty())
    }

    @Test
    fun searchQueryFiltersChecklists() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        repository.insertChecklist(ChecklistEntity(id = 1, title = "Apple", language = "en", description = "Fruit", category = "Food", icon = ""))
        repository.insertChecklist(ChecklistEntity(id = 2, title = "Banana", language = "en", description = "Yellow Fruit", category = "Food", icon = ""))
        repository.insertChecklist(ChecklistEntity(id = 3, title = "Carrot", language = "en", description = "Vegetable", category = "Food", icon = ""))

        viewModel.onSearchQueryChange("Banana")

        val filtered = viewModel.uiState.value.checklists
        assertEquals(1, filtered.size)
        assertEquals("Banana", filtered[0].title)
    }

    @Test
    fun categoryFilterFiltersChecklists() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        repository.insertChecklist(ChecklistEntity(id = 1, title = "Task 1", language = "en", description = "Work task", category = "Work", icon = ""))
        repository.insertChecklist(ChecklistEntity(id = 2, title = "Task 2", language = "en", description = "Personal task", category = "Personal", icon = ""))

        viewModel.onCategoryFilterChange("Work")

        val filtered = viewModel.uiState.value.checklists
        assertEquals(1, filtered.size)
        assertEquals("Task 1", filtered[0].title)
    }

    @Test
    fun settingsUpdateState() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.updateThemeMode(ThemeMode.DARK)
        viewModel.updatePreferredLanguage("de")
        viewModel.updateUserName("Jane Doe")

        val uiState = viewModel.uiState.value
        assertEquals(ThemeMode.DARK, uiState.userSettings.themeMode)
        assertEquals("de", uiState.userSettings.preferredLanguage)
        assertEquals("Jane Doe", uiState.userSettings.userName)
    }

    @Test
    fun submitChecklistAnswersSavesSubmissionAndAnswers() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val checklist = ChecklistEntity(id = 10, title = "Safety Inspection", language = "en", icon = "")
        repository.insertChecklist(checklist)

        val answers = mapOf(1 to "true", 2 to "Morning")
        viewModel.submitChecklistAnswers(10, "John Inspector", "All clear", date = "", answers = answers)

        val submission = submissionRepository.getChecklistSubmissionsByChecklistId(10)
        assertNotNull(submission)
        assertEquals("John Inspector", submission?.inspector)
        assertEquals("All clear", submission?.notes)
        assertNotNull(submission?.date)

        val uiState = viewModel.uiState.value
        assertEquals("Checklist submitted successfully", uiState.userMessage)
    }

    private class FakeChecklistDao : ChecklistDAO {
        private val checklistsMap = mutableMapOf<Int, ChecklistEntity>()
        private val checklistsFlow = MutableStateFlow<List<ChecklistEntity>>(emptyList())

        private fun updateFlow() {
            checklistsFlow.value = checklistsMap.values.toList().sortedByDescending { it.timestamp }
        }

        override fun getAllChecklists(): Flow<List<ChecklistEntity>> = checklistsFlow

        override suspend fun getChecklistById(id: Int): ChecklistEntity? {
            return checklistsMap[id]
        }

        override suspend fun insertChecklist(checklist: ChecklistEntity): Long {
            val id = if (checklist.id == 0) (checklistsMap.keys.maxOrNull() ?: 0) + 1 else checklist.id
            val newChecklist = checklist.copy(id = id)
            checklistsMap[id] = newChecklist
            updateFlow()
            return id.toLong()
        }

        override suspend fun updateChecklist(checklist: ChecklistEntity) {
            checklistsMap[checklist.id] = checklist
            updateFlow()
        }

        override suspend fun deleteChecklist(checklist: ChecklistEntity) {
            checklistsMap.remove(checklist.id)
            updateFlow()
        }
    }

    private class FakeChecklistQuestionDao : ChecklistQuestionDAO {
        private val questionsMap = mutableMapOf<Int, ChecklistQuestionEntity>()
        private val questionsFlow = MutableStateFlow<List<ChecklistQuestionEntity>>(emptyList())

        private fun updateFlow() {
            questionsFlow.value = questionsMap.values.toList().sortedWith(compareBy({ it.sortno }, { it.id }))
        }

        override fun getAllChecklistQuestions(): Flow<List<ChecklistQuestionEntity>> = questionsFlow

        override suspend fun getChecklistQuestionById(id: Int): ChecklistQuestionEntity? = questionsMap[id]

        override fun getChecklistQuestionsByChecklistId(checklistid: Int): Flow<List<ChecklistQuestionEntity>> {
            return MutableStateFlow(questionsMap.values.filter { it.checklistid == checklistid }.sortedWith(compareBy({ it.sortno }, { it.id })))
        }

        override suspend fun insertChecklistQuestion(question: ChecklistQuestionEntity): Long {
            val id = if (question.id == 0) (questionsMap.keys.maxOrNull() ?: 0) + 1 else question.id
            val newQuestion = question.copy(id = id)
            questionsMap[id] = newQuestion
            updateFlow()
            return id.toLong()
        }

        override suspend fun updateChecklistQuestion(question: ChecklistQuestionEntity) {
            questionsMap[question.id] = question
            updateFlow()
        }

        override suspend fun deleteChecklistQuestion(question: ChecklistQuestionEntity) {
            questionsMap.remove(question.id)
            updateFlow()
        }
    }

    private class FakeLanguageDao : LanguageDAO {
        private val languagesMap = mutableMapOf<String, LanguageEntity>()
        private val languagesFlow = MutableStateFlow<List<LanguageEntity>>(emptyList())

        override fun getAllLanguages(): Flow<List<LanguageEntity>> = languagesFlow

        override suspend fun getLanguageByLang(lang: String): LanguageEntity? = languagesMap[lang]

        override suspend fun insertLanguage(language: LanguageEntity): Long {
            languagesMap[language.language] = language
            languagesFlow.value = languagesMap.values.toList()
            return 1L
        }
    }

    private class FakeCategoryDao : CategoryDAO {
        private val categoriesMap = mutableMapOf<Int, CategoryEntity>()
        private val categoriesFlow = MutableStateFlow<List<CategoryEntity>>(emptyList())

        override fun getAllCategories(): Flow<List<CategoryEntity>> = categoriesFlow

        override suspend fun getCategory(category: String): CategoryEntity? = categoriesMap.values.find { it.category == category }

        override suspend fun insertCategory(category: CategoryEntity): Long {
            val id = if (category.id == 0) (categoriesMap.keys.maxOrNull() ?: 0) + 1 else category.id
            val newCat = category.copy(id = id)
            categoriesMap[id] = newCat
            categoriesFlow.value = categoriesMap.values.toList()
            return id.toLong()
        }

        override suspend fun deleteCategory(category: CategoryEntity) {
            categoriesMap.entries.removeAll { it.value.category.equals(category.category, ignoreCase = true) }
            categoriesFlow.value = categoriesMap.values.toList()
        }
    }

    private class FakeSubmissionDao : ChecklistSubmissionDAO {
        private val submissionsMap = mutableMapOf<Int, ChecklistSubmissionEntity>()
        private val submissionsFlow = MutableStateFlow<List<ChecklistSubmissionEntity>>(emptyList())

        override fun getAllChecklistSubmissions(): Flow<List<ChecklistSubmissionEntity>> = submissionsFlow

        override suspend fun getChecklistSubmissionById(id: Int): ChecklistSubmissionEntity? = submissionsMap[id]

        override suspend fun getChecklistSubmissionsByChecklistId(checklistid: Int): ChecklistSubmissionEntity? =
            submissionsMap.values.find { it.checklistid == checklistid }

        override suspend fun insertChecklistSubmission(submission: ChecklistSubmissionEntity): Long {
            val id = if (submission.id == 0) (submissionsMap.keys.maxOrNull() ?: 0) + 1 else submission.id
            val newSub = submission.copy(id = id)
            submissionsMap[id] = newSub
            submissionsFlow.value = submissionsMap.values.toList()
            return id.toLong()
        }

        override suspend fun updateChecklistSubmission(submission: ChecklistSubmissionEntity) {
            submissionsMap[submission.id] = submission
            submissionsFlow.value = submissionsMap.values.toList()
        }

        override suspend fun deleteChecklistSubmission(submission: ChecklistSubmissionEntity) {
            submissionsMap.remove(submission.id)
            submissionsFlow.value = submissionsMap.values.toList()
        }
    }

    private class FakeAnswerDao : ChecklistAnswerDAO {
        private val answersMap = mutableMapOf<Int, ChecklistAnswerEntity>()
        private val answersFlow = MutableStateFlow<List<ChecklistAnswerEntity>>(emptyList())

        override fun getAllChecklistAnswers(): Flow<List<ChecklistAnswerEntity>> = answersFlow

        override suspend fun getChecklistAnswerById(id: Int): ChecklistAnswerEntity? = answersMap[id]

        override suspend fun getChecklistAnswerByQuestionId(questionid: Int): ChecklistAnswerEntity? =
            answersMap.values.find { it.questionid == questionid }

        override suspend fun getChecklistAnswersBySubmissionId(submissionid: Int): ChecklistAnswerEntity? =
            answersMap.values.find { it.submissionid == submissionid }

        override suspend fun getChecklistAnswersListBySubmissionId(submissionid: Int): List<ChecklistAnswerEntity> =
            answersMap.values.filter { it.submissionid == submissionid }

        override suspend fun insertChecklistAnswer(answer: ChecklistAnswerEntity): Long {
            val id = if (answer.id == 0) (answersMap.keys.maxOrNull() ?: 0) + 1 else answer.id
            val newAns = answer.copy(id = id)
            answersMap[id] = newAns
            answersFlow.value = answersMap.values.toList()
            return id.toLong()
        }

        override suspend fun updateChecklistAnswer(answer: ChecklistAnswerEntity) {
            answersMap[answer.id] = answer
            answersFlow.value = answersMap.values.toList()
        }

        override suspend fun deleteChecklistAnswer(answer: ChecklistAnswerEntity) {
            answersMap.remove(answer.id)
            answersFlow.value = answersMap.values.toList()
        }
    }

    private class FakeSettingsRepository : SettingsRepository {
        private val _settings = MutableStateFlow(UserSettings())
        override val userSettings: Flow<UserSettings> = _settings

        override suspend fun updateThemeMode(themeMode: ThemeMode) {
            _settings.value = _settings.value.copy(themeMode = themeMode)
        }

        override suspend fun updatePreferredLanguage(language: String) {
            _settings.value = _settings.value.copy(preferredLanguage = language)
        }

        override suspend fun updateUserName(userName: String) {
            _settings.value = _settings.value.copy(userName = userName)
        }
    }
}
