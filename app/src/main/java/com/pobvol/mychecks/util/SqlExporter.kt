package com.pobvol.mychecks.util

import android.content.ClipData
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.pobvol.mychecks.data.local.ChecklistAnswerEntity
import com.pobvol.mychecks.data.local.ChecklistEntity
import com.pobvol.mychecks.data.local.ChecklistQuestionEntity
import com.pobvol.mychecks.data.local.ChecklistSubmissionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SqlExporter {

    fun exportSubmissionAsSql(
        context: Context,
        submission: ChecklistSubmissionEntity,
        checklist: ChecklistEntity?,
        questions: List<ChecklistQuestionEntity>,
        answers: List<ChecklistAnswerEntity>,
    ) {
        val submissionAnswers = answers.filter { it.submissionid == submission.id }

        val sqlBuilder = buildSqlString(submission, checklist, questions, submissionAnswers)
        val fileName = "Submission_${submission.id}_Export.sql"
        var fileUri: Uri?

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val relativePath = Environment.DIRECTORY_DOWNLOADS
                val projection = arrayOf(MediaStore.MediaColumns._ID)
                val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
                val selectionArgs = arrayOf(fileName, "$relativePath/")

                resolver.query(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                        val id = cursor.getLong(idColumn)
                        val existingUri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id)
                        resolver.delete(existingUri, null, null)
                    }
                }

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                }
                fileUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (fileUri != null) {
                    resolver.openOutputStream(fileUri)?.use { outputStream ->
                        outputStream.write(sqlBuilder.toString().toByteArray())
                    }
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { fos ->
                    fos.write(sqlBuilder.toString().toByteArray())
                }
                val authority = "${context.packageName}.fileprovider"
                fileUri = FileProvider.getUriForFile(context, authority, file)
            }

            if (fileUri != null) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    clipData = ClipData.newUri(context.contentResolver, "SQL Export", fileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(intent, "Send SQL Export File")
                chooser.clipData = ClipData.newUri(context.contentResolver, "SQL Export", fileUri)
                chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveSqlToMyChecks(
        context: Context,
        submission: ChecklistSubmissionEntity,
        checklist: ChecklistEntity?,
        questions: List<ChecklistQuestionEntity>,
        answers: List<ChecklistAnswerEntity>,
    ) {
        val submissionAnswers = answers.filter { it.submissionid == submission.id }
        val sqlBuilder = buildSqlString(submission, checklist, questions, submissionAnswers)
        val fileName = "Submission_${submission.id}_Export.sql"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/MyChecks"
                val projection = arrayOf(MediaStore.MediaColumns._ID)
                val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
                val selectionArgs = arrayOf(fileName, "$relativePath/")

                resolver.query(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                        val id = cursor.getLong(idColumn)
                        val existingUri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id)
                        resolver.delete(existingUri, null, null)
                    }
                }

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { os ->
                        os.write(sqlBuilder.toString().toByteArray())
                    }
                }
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MyChecks")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, fileName)
                FileOutputStream(file).use { fos ->
                    fos.write(sqlBuilder.toString().toByteArray())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildSqlString(
        submission: ChecklistSubmissionEntity,
        checklist: ChecklistEntity?,
        questions: List<ChecklistQuestionEntity>,
        submissionAnswers: List<ChecklistAnswerEntity>,
    ): StringBuilder {
        return StringBuilder().apply {
            append("-- pobvol Open Checklists SQL Export\n")
            append("-- Submission ID: ${submission.id}\n")
            append("-- Generated at: ${Date()}\n\n")

            if (checklist != null) {
                append("INSERT OR REPLACE INTO `checklists` (`id`, `title`, `language`, `description`, `category`, `icon`, `timestamp`) VALUES (\n")
                append("  ${checklist.id}, '${escapeSql(checklist.title)}', '${escapeSql(checklist.language)}', '${escapeSql(checklist.description ?: "")}', '${escapeSql(checklist.category)}', '${escapeSql(checklist.icon ?: "")}', ${checklist.timestamp}\n);\n\n")
            }

            if (questions.isNotEmpty()) {
                append("-- Questions\n")
                questions.forEach { q ->
                    append("INSERT OR REPLACE INTO `checklist_questions` (`id`, `checklistid`, `title`, `description`, `type`, `options`, `required`, `sortno`, `min_value`, `max_value`) VALUES (\n")
                    append("  ${q.id}, ${q.checklistid}, '${escapeSql(q.title)}', '${escapeSql(q.description ?: "")}', '${escapeSql(q.type)}', '${escapeSql(q.options ?: "")}', ${if (q.required) 1 else 0}, ${q.sortno}, ${q.minValue ?: "NULL"}, ${q.maxValue ?: "NULL"}\n);\n")
                }
                append("\n")
            }

            append("-- Submission\n")
            append("INSERT OR REPLACE INTO `checklist_submissions` (`id`, `checklistid`, `status`, `inspector`, `notes`, `date`, `timestamp`) VALUES (\n")
            append("  ${submission.id}, ${submission.checklistid}, '${escapeSql(submission.status)}', '${escapeSql(submission.inspector ?: "")}', '${escapeSql(submission.notes ?: "")}', '${escapeSql(submission.date)}', ${submission.timestamp}\n);\n\n")

            if (submissionAnswers.isNotEmpty()) {
                append("-- Answers\n")
                submissionAnswers.forEach { a ->
                    append("INSERT OR REPLACE INTO `checklist_answers` (`id`, `submissionid`, `questionid`, `value`, `notes`, `imagePath`, `timestamp`) VALUES (\n")
                    append(
                        "  ${a.id}, ${a.submissionid}, ${a.questionid}, '${escapeSql(a.value)}', '${
                            escapeSql(
                                a.notes ?: ""
                            )
                        }', '${escapeSql(a.imagePath ?: "")}', ${a.timestamp}\n);\n"
                    )
                }
            }
        }
    }

    fun savePictureToMyChecks(
        context: Context,
        submissionId: Int,
        answerId: Int,
        sourceUri: Uri
    ): String? {
        val fileName = "Submission_${submissionId}_Answer_${answerId}_Picture.jpg"
        try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/MyChecks"
                val projection = arrayOf(MediaStore.MediaColumns._ID)
                val selection =
                    "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
                val selectionArgs = arrayOf(fileName, "$relativePath/")

                resolver.query(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                        val id = cursor.getLong(idColumn)
                        val existingUri = ContentUris.withAppendedId(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            id
                        )
                        resolver.delete(existingUri, null, null)
                    }
                }

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } else {
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "MyChecks"
                )
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, fileName)
                FileOutputStream(file).use { fos ->
                    inputStream.copyTo(fos)
                }
            }
            inputStream.close()
            return fileName
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun exportDatabaseCopy(context: Context): Boolean {
        try {
            val dbFile = context.getDatabasePath("mychecksdb.sqlite3")
            if (!dbFile.exists()) return false

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "MyChecks_Database_Backup_$timeStamp.sqlite3"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/MyChecks"
                val projection = arrayOf(MediaStore.MediaColumns._ID)
                val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
                val selectionArgs = arrayOf(fileName, "$relativePath/")

                resolver.query(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                        val id = cursor.getLong(idColumn)
                        val existingUri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id)
                        resolver.delete(existingUri, null, null)
                    }
                }

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { fos ->
                        dbFile.inputStream().use { fis ->
                            fis.copyTo(fos)
                        }
                    }
                    return true
                }
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MyChecks")
                if (!dir.exists()) dir.mkdirs()
                val targetFile = File(dir, fileName)
                dbFile.copyTo(targetFile, overwrite = true)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun escapeSql(input: String): String {
        return input.replace("'", "''")
    }
}
