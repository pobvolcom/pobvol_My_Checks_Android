package com.pobvol.pobvolchecklists.data.repository

import com.pobvol.pobvolchecklists.data.local.ChecklistSubmissionEntity
import kotlinx.coroutines.flow.Flow

interface ChecklistSubmissionRepository {
    fun getAllChecklistSubmissions(): Flow<List<ChecklistSubmissionEntity>>
    suspend fun getChecklistSubmissionById(id: Int): ChecklistSubmissionEntity?
    suspend fun getChecklistSubmissionsByChecklistId(checklistid: Int): ChecklistSubmissionEntity?
    suspend fun insertChecklistSubmission(submission: ChecklistSubmissionEntity): Long
    suspend fun updateChecklistSubmission(submission: ChecklistSubmissionEntity)
    suspend fun deleteChecklistSubmission(submission: ChecklistSubmissionEntity)
}
