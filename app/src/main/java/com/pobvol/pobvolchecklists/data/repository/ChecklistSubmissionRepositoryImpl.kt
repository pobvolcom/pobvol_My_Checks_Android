package com.pobvol.pobvolchecklists.data.repository

import com.pobvol.pobvolchecklists.data.local.ChecklistSubmissionDAO
import com.pobvol.pobvolchecklists.data.local.ChecklistSubmissionEntity
import kotlinx.coroutines.flow.Flow

class ChecklistSubmissionRepositoryImpl(
    private val checklistsubmissionDao: ChecklistSubmissionDAO
) : ChecklistSubmissionRepository {
    override fun getAllChecklistSubmissions(): Flow<List<ChecklistSubmissionEntity>> = checklistsubmissionDao.getAllChecklistSubmissions()
    override suspend fun getChecklistSubmissionById(id: Int): ChecklistSubmissionEntity? = checklistsubmissionDao.getChecklistSubmissionById(id)
    override suspend fun getChecklistSubmissionsByChecklistId(checklistid: Int): ChecklistSubmissionEntity? = checklistsubmissionDao.getChecklistSubmissionById(checklistid)
    override suspend fun insertChecklistSubmission(submission: ChecklistSubmissionEntity): Long = checklistsubmissionDao.insertChecklistSubmission(submission)
    override suspend fun updateChecklistSubmission(submission: ChecklistSubmissionEntity) = checklistsubmissionDao.updateChecklistSubmission(submission)
    override suspend fun deleteChecklistSubmission(submission: ChecklistSubmissionEntity) = checklistsubmissionDao.deleteChecklistSubmission(submission)
}
