package com.pobvol.pobvolchecklists.data.repository

import com.pobvol.pobvolchecklists.data.local.RecordEntity
import kotlinx.coroutines.flow.Flow

interface RecordRepository {
    fun getAllRecords(): Flow<List<RecordEntity>>
    suspend fun getRecordById(id: Int): RecordEntity?
    suspend fun insertRecord(record: RecordEntity): Long
    suspend fun updateRecord(record: RecordEntity)
    suspend fun deleteRecord(record: RecordEntity)
}
