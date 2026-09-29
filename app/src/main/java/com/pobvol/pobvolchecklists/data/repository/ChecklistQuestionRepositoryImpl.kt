package com.pobvol.pobvolchecklists.data.repository

import com.pobvol.pobvolchecklists.data.local.ChecklistQuestionDao
import com.pobvol.pobvolchecklists.data.local.ChecklistQuestionEntity
import kotlinx.coroutines.flow.Flow

class ChecklistQuestionRepositoryImpl(
    private val checklistquestionDao: ChecklistQuestionDao
) : ChecklistQuestionRepository {
    override fun getAllChecklistQuestions(): Flow<List<ChecklistQuestionEntity>> = checklistquestionDao.getAllChecklistQuestions()
    override suspend fun getChecklistQuestionById(id: Int): ChecklistQuestionEntity? = checklistquestionDao.getChecklistQuestionById(id)
    override suspend fun getChecklistQuestionsByChecklistId(checklistid: Int): ChecklistQuestionEntity? = checklistquestionDao.getChecklistQuestionsByChecklistId(checklistid)
    override suspend fun insertChecklistQuestion(question: ChecklistQuestionEntity): Long = checklistquestionDao.insertChecklistQuestion(question)
    override suspend fun updateChecklistQuestion(question: ChecklistQuestionEntity) = checklistquestionDao.updateChecklistQuestion(question)
    override suspend fun deleteChecklistQuestion(question: ChecklistQuestionEntity) = checklistquestionDao.deleteChecklistQuestion(question)
}
