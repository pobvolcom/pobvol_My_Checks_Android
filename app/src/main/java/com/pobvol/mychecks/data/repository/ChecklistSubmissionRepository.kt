package com.pobvol.mychecks.data.repository

import com.pobvol.mychecks.data.local.ChecklistSubmissionEntity
import kotlinx.coroutines.flow.Flow

interface ChecklistSubmissionRepository {
    fun getAllChecklistSubmissions(): Flow<List<ChecklistSubmissionEntity>>
    suspend fun getChecklistSubmissionById(id: String): ChecklistSubmissionEntity?
    suspend fun getChecklistSubmissionsByChecklistId(checklistid: String): ChecklistSubmissionEntity?
    suspend fun insertChecklistSubmission(submission: ChecklistSubmissionEntity): Long
    suspend fun updateChecklistSubmission(submission: ChecklistSubmissionEntity)
    suspend fun deleteChecklistSubmission(submission: ChecklistSubmissionEntity)
}
