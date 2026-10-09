package com.pobvol.mychecks.data.repository

import com.pobvol.mychecks.data.local.ChecklistEntity
import kotlinx.coroutines.flow.Flow

interface ChecklistRepository {

    fun getAllChecklists(): Flow<List<ChecklistEntity>>
    suspend fun getChecklistById(id: Int): ChecklistEntity?
    suspend fun insertChecklist(checklist: ChecklistEntity): Long
    suspend fun updateChecklist(checklist: ChecklistEntity)
    suspend fun deleteChecklist(checklist: ChecklistEntity)

}
