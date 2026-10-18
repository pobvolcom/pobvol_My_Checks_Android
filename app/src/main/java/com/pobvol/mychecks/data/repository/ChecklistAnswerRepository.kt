package com.pobvol.mychecks.data.repository

import com.pobvol.mychecks.data.local.ChecklistAnswerEntity
import kotlinx.coroutines.flow.Flow

interface ChecklistAnswerRepository {
    fun getAllChecklistAnswers(): Flow<List<ChecklistAnswerEntity>>
    suspend fun getChecklistAnswerById(id: String): ChecklistAnswerEntity?
    suspend fun getChecklistAnswerByQuestionId(questionid: String): ChecklistAnswerEntity?
    suspend fun getChecklistAnswersBySubmissionId(submissionid: String): ChecklistAnswerEntity?
    suspend fun getChecklistAnswersListBySubmissionId(submissionid: String): List<ChecklistAnswerEntity>
    suspend fun insertChecklistAnswer(answer: ChecklistAnswerEntity): Long
    suspend fun updateChecklistAnswer(answer: ChecklistAnswerEntity)
    suspend fun deleteChecklistAnswer(answer: ChecklistAnswerEntity)
}
