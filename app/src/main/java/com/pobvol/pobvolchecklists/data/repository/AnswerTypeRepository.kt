package com.pobvol.pobvolchecklists.data.repository

import com.pobvol.pobvolchecklists.data.local.AnswerTypeEntity
import kotlinx.coroutines.flow.Flow

interface AnswerTypeRepository {

    fun getAllAnswerTypes(): Flow<List<AnswerTypeEntity>>
    suspend fun getAnswerType(type: String): AnswerTypeEntity?
    suspend fun insertAnswerType(answertype: AnswerTypeEntity): Long

}
