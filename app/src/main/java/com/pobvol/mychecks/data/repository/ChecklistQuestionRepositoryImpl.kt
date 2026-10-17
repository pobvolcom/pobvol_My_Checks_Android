package com.pobvol.mychecks.data.repository

import com.pobvol.mychecks.data.local.ChecklistQuestionDAO
import com.pobvol.mychecks.data.local.ChecklistQuestionEntity
import kotlinx.coroutines.flow.Flow

class ChecklistQuestionRepositoryImpl(
    private val checklistquestionDao: ChecklistQuestionDAO
) : ChecklistQuestionRepository {
    override fun getAllChecklistQuestions(): Flow<List<ChecklistQuestionEntity>> = checklistquestionDao.getAllChecklistQuestions()
    override suspend fun getChecklistQuestionById(id: Int): ChecklistQuestionEntity? = checklistquestionDao.getChecklistQuestionById(id)
    override fun getChecklistQuestionsByChecklistId(checklistid: Int): Flow<List<ChecklistQuestionEntity>> = checklistquestionDao.getChecklistQuestionsByChecklistId(checklistid)
    override suspend fun insertChecklistQuestion(question: ChecklistQuestionEntity): Long = checklistquestionDao.insertChecklistQuestion(question)
    override suspend fun updateChecklistQuestion(question: ChecklistQuestionEntity) = checklistquestionDao.updateChecklistQuestion(question)
    override suspend fun deleteChecklistQuestion(question: ChecklistQuestionEntity) = checklistquestionDao.deleteChecklistQuestion(question)
}
