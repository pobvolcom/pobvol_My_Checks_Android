package com.pobvol.pobvolchecklists.data.repository

import com.pobvol.pobvolchecklists.data.local.ChecklistAnswerEntity
import kotlinx.coroutines.flow.Flow

interface ChecklistAnswerRepository {
    fun getAllChecklistAnswers(): Flow<List<ChecklistAnswerEntity>>
    suspend fun getChecklistAnswerById(id: Int): ChecklistAnswerEntity?
    suspend fun getChecklistAnswerByQuestionId(questionid: Int): ChecklistAnswerEntity?
    suspend fun getChecklistAnswersBySubmissionId(submissionid: Int): ChecklistAnswerEntity?
    suspend fun getChecklistAnswersListBySubmissionId(submissionid: Int): List<ChecklistAnswerEntity>
    suspend fun insertChecklistAnswer(answer: ChecklistAnswerEntity): Long
    suspend fun updateChecklistAnswer(answer: ChecklistAnswerEntity)
    suspend fun deleteChecklistAnswer(answer: ChecklistAnswerEntity)
}
