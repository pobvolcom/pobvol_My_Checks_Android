package com.pobvol.mychecks.data.repository

import com.pobvol.mychecks.data.local.ChecklistAnswerDAO
import com.pobvol.mychecks.data.local.ChecklistAnswerEntity
import kotlinx.coroutines.flow.Flow

class ChecklistAnswerRepositoryImpl(
    private val checklistanswerDao: ChecklistAnswerDAO
) : ChecklistAnswerRepository {

    override fun getAllChecklistAnswers(): Flow<List<ChecklistAnswerEntity>> = checklistanswerDao.getAllChecklistAnswers()
    override suspend fun getChecklistAnswerById(id: String): ChecklistAnswerEntity? =
        checklistanswerDao.getChecklistAnswerById(id)

    override suspend fun getChecklistAnswerByQuestionId(questionid: String): ChecklistAnswerEntity? =
        checklistanswerDao.getChecklistAnswerByQuestionId(questionid)

    override suspend fun getChecklistAnswersBySubmissionId(submissionid: String): ChecklistAnswerEntity? =
        checklistanswerDao.getChecklistAnswersBySubmissionId(submissionid)

    override suspend fun getChecklistAnswersListBySubmissionId(submissionid: String): List<ChecklistAnswerEntity> =
        checklistanswerDao.getChecklistAnswersListBySubmissionId(submissionid)
    override suspend fun insertChecklistAnswer(answer: ChecklistAnswerEntity): Long = checklistanswerDao.insertChecklistAnswer(answer)
    override suspend fun updateChecklistAnswer(answer: ChecklistAnswerEntity) = checklistanswerDao.updateChecklistAnswer(answer)
    override suspend fun deleteChecklistAnswer(answer: ChecklistAnswerEntity) = checklistanswerDao.deleteChecklistAnswer(answer)

}
