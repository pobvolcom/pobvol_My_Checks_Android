package com.pobvol.mychecks.data.repository

import com.pobvol.mychecks.data.local.RecordDAO
import com.pobvol.mychecks.data.local.RecordEntity
import kotlinx.coroutines.flow.Flow

class RecordRepositoryImpl(
    private val recordDao: RecordDAO
) : RecordRepository {

    /* table: records */
    override fun getAllRecords(): Flow<List<RecordEntity>> = recordDao.getAllRecords()

    override suspend fun getRecordById(id: Int): RecordEntity? = recordDao.getRecordById(id)

    override suspend fun insertRecord(record: RecordEntity): Long = recordDao.insertRecord(record)

    override suspend fun updateRecord(record: RecordEntity) = recordDao.updateRecord(record)

    override suspend fun deleteRecord(record: RecordEntity) = recordDao.deleteRecord(record)
}
