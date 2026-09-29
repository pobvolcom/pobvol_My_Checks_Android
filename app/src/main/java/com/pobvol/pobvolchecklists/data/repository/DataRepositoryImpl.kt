package com.pobvol.pobvolchecklists.data.repository

import com.pobvol.pobvolchecklists.data.local.AppDAO
import com.pobvol.pobvolchecklists.data.local.AnswerTypeEntity
import com.pobvol.pobvolchecklists.data.local.CategoryEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistAnswerEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistQuestionEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistSubmissionEntity
import com.pobvol.pobvolchecklists.data.local.LanguageEntity
import com.pobvol.pobvolchecklists.data.local.RecordEntity
import kotlinx.coroutines.flow.Flow

class DataRepositoryImpl(
    private val appDao: AppDAO
) : DataRepository {

    override fun getAllRecords(): Flow<List<RecordEntity>> = appDao.getAllRecords()
    override suspend fun getRecordById(id: Int): RecordEntity? = appDao.getRecordById(id)
    override suspend fun insertRecord(record: RecordEntity): Long = appDao.insertRecord(record)
    override suspend fun updateRecord(record: RecordEntity) = appDao.updateRecord(record)
    override suspend fun deleteRecord(record: RecordEntity) = appDao.deleteRecord(record)



    override fun getAllLanguages(): Flow<List<LanguageEntity>> = appDao.getAllLanguages()
    override suspend fun getLanguageByLang(lang: String): LanguageEntity? = appDao.getLanguageByLang(lang)
    override suspend fun insertLanguage(language: LanguageEntity): Long = appDao.insertLanguage(language)



    override fun getAllCategories(): Flow<List<CategoryEntity>> = appDao.getAllCategories()
    override suspend fun getCategory(category: String): CategoryEntity? = appDao.getCategory(category)
    override suspend fun insertCategory(category: CategoryEntity): Long = appDao.insertCategory(category)



    override fun getAllAnswerTypes(): Flow<List<AnswerTypeEntity>> = appDao.getAllAnswerTypes()
    override suspend fun getAnswerType(type: String): AnswerTypeEntity? = appDao.getAnswerType(type)
    override suspend fun insertAnswerType(answertype: AnswerTypeEntity): Long = appDao.insertAnswerType(answertype)


    override fun getAllChecklists(): Flow<List<ChecklistEntity>> = appDao.getAllChecklists()
    override suspend fun getChecklistById(id: Int): ChecklistEntity? = appDao.getChecklistById(id)
    override suspend fun insertChecklist(checklist: ChecklistEntity): Long = appDao.insertChecklist(checklist)
    override suspend fun updateChecklist(checklist: ChecklistEntity) = appDao.updateChecklist(checklist)
    override suspend fun deleteChecklist(checklist: ChecklistEntity) = appDao.deleteChecklist(checklist)



    override fun getAllChecklistQuestions(): Flow<List<ChecklistQuestionEntity>> = appDao.getAllChecklistQuestions()
    override suspend fun getChecklistQuestionById(id: Int): ChecklistQuestionEntity? = appDao.getChecklistQuestionById(id)
    override suspend fun getChecklistQuestionsByChecklistId(checklistid: Int): ChecklistQuestionEntity? = appDao.getChecklistQuestionsByChecklistId(checklistid)
    override suspend fun insertChecklistQuestion(question: ChecklistQuestionEntity): Long = appDao.insertChecklistQuestion(question)
    override suspend fun updateChecklistQuestion(question: ChecklistQuestionEntity) = appDao.updateChecklistQuestion(question)
    override suspend fun deleteChecklistQuestion(question: ChecklistQuestionEntity) = appDao.deleteChecklistQuestion(question)



    override fun getAllChecklistSubmissions(): Flow<List<ChecklistSubmissionEntity>> = appDao.getAllChecklistSubmissions()
    override suspend fun getChecklistSubmissionById(id: Int): ChecklistSubmissionEntity? = appDao.getChecklistSubmissionById(id)
    override suspend fun getChecklistSubmissionsByChecklistId(checklistid: Int): ChecklistSubmissionEntity? = appDao.getChecklistSubmissionById(checklistid)
    override suspend fun insertChecklistSubmission(submission: ChecklistSubmissionEntity): Long = appDao.insertChecklistSubmission(submission)
    override suspend fun updateChecklistSubmission(submission: ChecklistSubmissionEntity) = appDao.updateChecklistSubmission(submission)
    override suspend fun deleteChecklistSubmission(submission: ChecklistSubmissionEntity) = appDao.deleteChecklistSubmission(submission)



    override fun getAllChecklistAnswers(): Flow<List<ChecklistAnswerEntity>> = appDao.getAllChecklistAnswers()
    override suspend fun getChecklistAnswerById(id: Int): ChecklistAnswerEntity? = appDao.getChecklistAnswerById(id)
    override suspend fun getChecklistAnswerByQuestionId(questionid: Int): ChecklistAnswerEntity? = appDao.getChecklistAnswerByQuestionId(questionid)
    override suspend fun getChecklistAnswersBySubmissionId(submissionid: Int): ChecklistAnswerEntity? = appDao.getChecklistAnswersBySubmissionId(submissionid)
    override suspend fun insertChecklistAnswer(answer: ChecklistAnswerEntity): Long = appDao.insertChecklistAnswer(answer)
    override suspend fun updateChecklistAnswer(answer: ChecklistAnswerEntity) = appDao.updateChecklistAnswer(answer)
    override suspend fun deleteChecklistAnswer(answer: ChecklistAnswerEntity) = appDao.deleteChecklistAnswer(answer)


}
