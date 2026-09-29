package com.pobvol.pobvolchecklists.data.repository

import com.pobvol.pobvolchecklists.data.local.ChecklistDao
import com.pobvol.pobvolchecklists.data.local.ChecklistEntity
import kotlinx.coroutines.flow.Flow

class ChecklistRepositoryImpl(
    private val checklistDao: ChecklistDao
) : ChecklistRepository {

    override fun getAllChecklists(): Flow<List<ChecklistEntity>> = checklistDao.getAllChecklists()

    override suspend fun getChecklistById(id: Int): ChecklistEntity? = checklistDao.getChecklistById(id)

    override suspend fun insertChecklist(checklist: ChecklistEntity): Long = checklistDao.insertChecklist(checklist)

    override suspend fun updateChecklist(checklist: ChecklistEntity) = checklistDao.updateChecklist(checklist)

    override suspend fun deleteChecklist(checklist: ChecklistEntity) = checklistDao.deleteChecklist(checklist)

}
