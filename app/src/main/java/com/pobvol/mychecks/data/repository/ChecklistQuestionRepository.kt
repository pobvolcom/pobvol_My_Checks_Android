package com.pobvol.mychecks.data.repository

import com.pobvol.mychecks.data.local.ChecklistQuestionEntity
import kotlinx.coroutines.flow.Flow

interface ChecklistQuestionRepository {

    fun getAllChecklistQuestions(): Flow<List<ChecklistQuestionEntity>>
    suspend fun getChecklistQuestionById(id: String): ChecklistQuestionEntity?
    fun getChecklistQuestionsByChecklistId(checklistid: String): Flow<List<ChecklistQuestionEntity>>
    suspend fun insertChecklistQuestion(question: ChecklistQuestionEntity): Long
    suspend fun updateChecklistQuestion(question: ChecklistQuestionEntity)
    suspend fun deleteChecklistQuestion(question: ChecklistQuestionEntity)

}
