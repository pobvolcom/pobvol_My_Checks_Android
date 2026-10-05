package com.pobvol.pobvolchecklists.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDAO {

    /* table: records */
    @Query("SELECT * FROM records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getRecordById(id: Int): RecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RecordEntity): Long

    @Update
    suspend fun updateRecord(record: RecordEntity)

    @Delete
    suspend fun deleteRecord(record: RecordEntity)
}

@Dao
interface LanguageDAO {

    @Query("SELECT * FROM languages ORDER BY language ASC")
    fun getAllLanguages(): Flow<List<LanguageEntity>>

    @Query("SELECT * FROM languages WHERE language = :lang")
    suspend fun getLanguageByLang(lang: String): LanguageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguage(language: LanguageEntity): Long

}

@Dao
interface AnswerTypeDAO {

    @Query("SELECT * FROM answer_types ORDER BY type ASC")
    fun getAllAnswerTypes(): Flow<List<AnswerTypeEntity>>

    @Query("SELECT * FROM answer_types WHERE type = :type")
    suspend fun getAnswerType(type: String): AnswerTypeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswerType(answertype: AnswerTypeEntity): Long

}

@Dao
interface CategoryDAO {

    @Query("SELECT * FROM categories ORDER BY category ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE category = :category")
    suspend fun getCategory(category: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

}

@Dao
interface ChecklistDAO {

    @Query("SELECT * FROM checklists ORDER BY title ASC")
    fun getAllChecklists(): Flow<List<ChecklistEntity>>

    @Query("SELECT * FROM checklists WHERE id = :id")
    suspend fun getChecklistById(id: Int): ChecklistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklist(checklist: ChecklistEntity): Long

    @Update
    suspend fun updateChecklist(checklist: ChecklistEntity)

    @Delete
    suspend fun deleteChecklist(checklist: ChecklistEntity)

}

@Dao
interface ChecklistQuestionDAO {

    @Query("SELECT * FROM checklist_questions ORDER BY sortno ASC, id ASC")
    fun getAllChecklistQuestions(): Flow<List<ChecklistQuestionEntity>>

    @Query("SELECT * FROM checklist_questions WHERE id = :id")
    suspend fun getChecklistQuestionById(id: Int): ChecklistQuestionEntity?

    @Query("SELECT * FROM checklist_questions WHERE checklistid = :checklistid ORDER BY sortno ASC, id ASC")
    fun getChecklistQuestionsByChecklistId(checklistid: Int): Flow<List<ChecklistQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistQuestion(question: ChecklistQuestionEntity): Long

    @Update
    suspend fun updateChecklistQuestion(question: ChecklistQuestionEntity)

    @Delete
    suspend fun deleteChecklistQuestion(question: ChecklistQuestionEntity)

}

@Dao
interface ChecklistSubmissionDAO {

    @Query("SELECT * FROM checklist_submissions ORDER BY timestamp DESC")
    fun getAllChecklistSubmissions(): Flow<List<ChecklistSubmissionEntity>>

    @Query("SELECT * FROM checklist_submissions WHERE id = :id")
    suspend fun getChecklistSubmissionById(id: Int): ChecklistSubmissionEntity?

    @Query("SELECT * FROM checklist_submissions WHERE checklistid = :checklistid")
    suspend fun getChecklistSubmissionsByChecklistId(checklistid: Int): ChecklistSubmissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistSubmission(submission: ChecklistSubmissionEntity): Long

    @Update
    suspend fun updateChecklistSubmission(submission: ChecklistSubmissionEntity)

    @Delete
    suspend fun deleteChecklistSubmission(submission: ChecklistSubmissionEntity)

}

@Dao
interface ChecklistAnswerDAO {

    @Query("SELECT * FROM checklist_answers ORDER BY timestamp DESC")
    fun getAllChecklistAnswers(): Flow<List<ChecklistAnswerEntity>>
    @Query("SELECT * FROM checklist_answers WHERE id = :id")
    suspend fun getChecklistAnswerById(id: Int): ChecklistAnswerEntity?

    @Query("SELECT * FROM checklist_answers WHERE questionid = :questionid")
    suspend fun getChecklistAnswerByQuestionId(questionid: Int): ChecklistAnswerEntity?

    @Query("SELECT * FROM checklist_answers WHERE submissionid = :submissionid")
    suspend fun getChecklistAnswersBySubmissionId(submissionid: Int): ChecklistAnswerEntity?

    @Query("SELECT * FROM checklist_answers WHERE submissionid = :submissionid")
    suspend fun getChecklistAnswersListBySubmissionId(submissionid: Int): List<ChecklistAnswerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistAnswer(answer: ChecklistAnswerEntity): Long
    @Update
    suspend fun updateChecklistAnswer(answer: ChecklistAnswerEntity)

    @Delete
    suspend fun deleteChecklistAnswer(answer: ChecklistAnswerEntity)

}
