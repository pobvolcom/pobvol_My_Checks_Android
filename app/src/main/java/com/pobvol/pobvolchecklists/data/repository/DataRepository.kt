package com.pobvol.pobvolchecklists.data.repository

import com.pobvol.pobvolchecklists.data.local.AnswerTypeEntity
import com.pobvol.pobvolchecklists.data.local.CategoryEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistAnswerEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistQuestionEntity
import com.pobvol.pobvolchecklists.data.local.ChecklistSubmissionEntity
import com.pobvol.pobvolchecklists.data.local.LanguageEntity
import com.pobvol.pobvolchecklists.data.local.RecordEntity
import kotlinx.coroutines.flow.Flow

interface DataRepository {


    fun getAllRecords(): Flow<List<RecordEntity>>
    suspend fun getRecordById(id: Int): RecordEntity?
    suspend fun insertRecord(record: RecordEntity): Long
    suspend fun updateRecord(record: RecordEntity)
    suspend fun deleteRecord(record: RecordEntity)


    
    fun getAllLanguages(): Flow<List<LanguageEntity>>
    suspend fun getLanguageByLang(lang: String): LanguageEntity?
    suspend fun insertLanguage(language: LanguageEntity): Long



    fun getAllAnswerTypes(): Flow<List<AnswerTypeEntity>>
    suspend fun getAnswerType(type: String): AnswerTypeEntity?
    suspend fun insertAnswerType(answertype: AnswerTypeEntity): Long


    fun getAllCategories(): Flow<List<CategoryEntity>>
    suspend fun getCategory(category: String): CategoryEntity?
    suspend fun insertCategory(category: CategoryEntity): Long



    fun getAllChecklists(): Flow<List<ChecklistEntity>>
    suspend fun getChecklistById(id: Int): ChecklistEntity?
    suspend fun insertChecklist(checklist: ChecklistEntity): Long
    suspend fun updateChecklist(checklist: ChecklistEntity)
    suspend fun deleteChecklist(checklist: ChecklistEntity)



    fun getAllChecklistQuestions(): Flow<List<ChecklistQuestionEntity>>
    suspend fun getChecklistQuestionById(id: Int): ChecklistQuestionEntity?
    suspend fun getChecklistQuestionsByChecklistId(checklistid: Int): ChecklistQuestionEntity?
    suspend fun insertChecklistQuestion(question: ChecklistQuestionEntity): Long
    suspend fun updateChecklistQuestion(question: ChecklistQuestionEntity)
    suspend fun deleteChecklistQuestion(question: ChecklistQuestionEntity)



    fun getAllChecklistSubmissions(): Flow<List<ChecklistSubmissionEntity>>
    suspend fun getChecklistSubmissionById(id: Int): ChecklistSubmissionEntity?
    suspend fun getChecklistSubmissionsByChecklistId(checklistid: Int): ChecklistSubmissionEntity?
    suspend fun insertChecklistSubmission(submission: ChecklistSubmissionEntity): Long
    suspend fun updateChecklistSubmission(submission: ChecklistSubmissionEntity)
    suspend fun deleteChecklistSubmission(submission: ChecklistSubmissionEntity)



    fun getAllChecklistAnswers(): Flow<List<ChecklistAnswerEntity>>
    suspend fun getChecklistAnswerById(id: Int): ChecklistAnswerEntity?
    suspend fun getChecklistAnswerByQuestionId(questionid: Int): ChecklistAnswerEntity?
    suspend fun getChecklistAnswersBySubmissionId(submissionid: Int): ChecklistAnswerEntity?
    suspend fun insertChecklistAnswer(answer: ChecklistAnswerEntity): Long
    suspend fun updateChecklistAnswer(answer: ChecklistAnswerEntity)
    suspend fun deleteChecklistAnswer(answer: ChecklistAnswerEntity)


}
