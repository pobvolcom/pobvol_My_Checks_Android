package com.pobvol.pobvolchecklists.data.repository

import com.pobvol.pobvolchecklists.data.local.AnswerTypeDAO
import com.pobvol.pobvolchecklists.data.local.AnswerTypeEntity
import kotlinx.coroutines.flow.Flow

class AnswerTypeRepositoryImpl(
    private val answertypeDao: AnswerTypeDAO
) : AnswerTypeRepository {
    override fun getAllAnswerTypes(): Flow<List<AnswerTypeEntity>> = answertypeDao.getAllAnswerTypes()
    override suspend fun getAnswerType(type: String): AnswerTypeEntity? = answertypeDao.getAnswerType(type)
    override suspend fun insertAnswerType(answertype: AnswerTypeEntity): Long = answertypeDao.insertAnswerType(answertype)

}
