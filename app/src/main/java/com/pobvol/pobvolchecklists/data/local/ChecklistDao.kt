package com.pobvol.pobvolchecklists.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {

    @Query("SELECT * FROM checklists ORDER BY timestamp DESC")
    fun getAllChecklists(): Flow<List<ChecklistEntity>>

    @Query("SELECT * FROM checklists WHERE id = :id")
    suspend fun getChecklistById(id: Int): ChecklistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklist(checklist: ChecklistEntity): Long

    @Update
    suspend fun updateChecklist(checklist: ChecklistEntity)

    @Delete
    suspend fun deleteChecklist(checklist: ChecklistEntity)

}
