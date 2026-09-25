package com.pobvol.pobvolchecklists.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {

    /* table: records */
    @Query("SELECT * FROM records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getRecordById(id: Int): RecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RecordEntity): Long

    @Update
    suspend fun updateRecord(record: RecordEntity)

    @Delete
    suspend fun deleteRecord(record: RecordEntity)
}
