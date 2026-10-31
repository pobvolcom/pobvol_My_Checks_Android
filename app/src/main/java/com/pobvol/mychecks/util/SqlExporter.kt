package com.pobvol.mychecks.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.pobvol.mychecks.data.local.AppDatabase
import com.pobvol.mychecks.data.local.ChecklistAnswerEntity
import com.pobvol.mychecks.data.local.ChecklistEntity
import com.pobvol.mychecks.data.local.ChecklistQuestionEntity
import com.pobvol.mychecks.data.local.ChecklistSubmissionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SqlExporter {

    fun getDeviceFolder(context: Context): String {
        val deviceId = try {
            val db = AppDatabase.getDatabase(context)
            runBlocking {
                db.deviceDao().getDeviceId().first().firstOrNull()?.id ?: "device_default"
            }
        } catch (_: Exception) {
            "device_default"
        }
        return "pobvolMyChecks/$deviceId"
    }

    fun getPdfRootDir(context: Context): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "pobvolMyChecks"
        )
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getDeviceRootDir(context: Context): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            getDeviceFolder(context)
        )
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getSqlDir(context: Context): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "pobvolMyChecks/sql"
        )
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getPdfDir(context: Context): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "pobvolMyChecks/pdf"
        )
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getDbBackupDir(context: Context): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "pobvolMyChecks/dbbackup"
        )
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun createSha256ChecksumFile(
        context: Context,
        targetFileName: String,
        fileBytes: ByteArray,
        targetDir: File
    ) {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(fileBytes)
            val hexString = hashBytes.joinToString("") { "%02x".format(it) }
            val checksumContent = "$hexString  $targetFileName\n"
            val checksumFileName = "$targetFileName.sha256"

            if (!targetDir.exists()) targetDir.mkdirs()
            val checksumFile = File(targetDir, checksumFileName)
            checksumFile.writeText(checksumContent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportSubmissionAsSql(
        context: Context,
        submission: ChecklistSubmissionEntity,
        checklist: ChecklistEntity?,
        questions: List<ChecklistQuestionEntity>,
        answers: List<ChecklistAnswerEntity>,
    ) {
        val submissionAnswers = answers.filter { it.submissionid == submission.id }
        val sqlBuilder =
            buildSqlString(context, submission, checklist, questions, submissionAnswers)
        val fileName = "Submission_${submission.id}_Export.sql"
        var fileUri: Uri?

        try {
            val sqlBytes = sqlBuilder.toString().toByteArray()
            val sqlDir = getSqlDir(context)
            val file = File(sqlDir, fileName)
            file.writeBytes(sqlBytes)
            createSha256ChecksumFile(context, fileName, sqlBytes, sqlDir)

            val authority = "${context.packageName}.fileprovider"
            fileUri = FileProvider.getUriForFile(context, authority, file)

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
        val sqlBuilder =
            buildSqlString(context, submission, checklist, questions, submissionAnswers)
        val fileName = "Submission_${submission.id}_Export.sql"

        try {
            val sqlBytes = sqlBuilder.toString().toByteArray()
            val sqlDir = getSqlDir(context)
            val file = File(sqlDir, fileName)
            file.writeBytes(sqlBytes)
            createSha256ChecksumFile(context, fileName, sqlBytes, sqlDir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildSqlString(
        context: Context,
        submission: ChecklistSubmissionEntity,
        checklist: ChecklistEntity?,
        questions: List<ChecklistQuestionEntity>,
        submissionAnswers: List<ChecklistAnswerEntity>,
    ): StringBuilder {
        val devices = try {
            val db = AppDatabase.getDatabase(context)
            runBlocking {
                db.deviceDao().getDeviceId().first()
            }
        } catch (_: Exception) {
            emptyList()
        }

        return StringBuilder().apply {
            append("-- pobvol Open Checklists SQL Export\n")
            append("-- Submission ID: ${submission.id}\n")
            append("-- Generated at: ${Date()}\n\n")

            // We don't need the device id at the moment
            /*
            if (devices.isNotEmpty()) {
                append("-- Device\n")
                devices.forEach { d ->
                    append("INSERT OR REPLACE INTO `device` (`id`, `notes`) VALUES (\n")
                    append("  '${d.id}', '${escapeSql(d.notes ?: "")}'\n);\n\n")
                }
            }
            */

            if (checklist != null) {
                append("INSERT OR REPLACE INTO `checklists` (`id`, `title`, `language`, `description`, `category`, `icon`, `timestamp`) VALUES (\n")
                append(
                    "  '${checklist.id}', '${escapeSql(checklist.title)}', '${escapeSql(checklist.language)}', '${
                        escapeSql(
                            checklist.description
                        )
                    }', '${escapeSql(checklist.category)}', '${escapeSql(checklist.icon ?: "")}', ${checklist.timestamp}\n);\n\n"
                )
            }

            if (questions.isNotEmpty()) {
                append("-- Questions\n")
                questions.forEach { q ->
                    // minValue, not min_value
                    append("INSERT OR REPLACE INTO `checklist_questions` (`id`, `checklistid`, `title`, `description`, `type`, `options`, `required`, `sortno`, `minValue`, `maxValue`) VALUES (\n")
                    append(
                        "  '${q.id}', '${q.checklistid}', '${escapeSql(q.title)}', '${escapeSql(q.description ?: "")}', '${
                            escapeSql(
                                q.type
                            )
                        }', '${escapeSql(q.options ?: "")}', ${if (q.required) 1 else 0}, ${q.sortno}, ${q.minValue ?: "NULL"}, ${q.maxValue ?: "NULL"}\n);\n"
                    )
                }
                append("\n")
            }

            append("-- Submission\n")
            append("INSERT OR REPLACE INTO `checklist_submissions` (`id`, `checklistid`, `status`, `inspector`, `notes`, `date`, `timestamp`) VALUES (\n")
            append(
                "  '${submission.id}', '${submission.checklistid}', '${escapeSql(submission.status)}', '${
                    escapeSql(
                        submission.inspector ?: ""
                    )
                }', '${escapeSql(submission.notes ?: "")}', '${escapeSql(submission.date)}', ${submission.timestamp}\n);\n\n"
            )

            if (submissionAnswers.isNotEmpty()) {
                append("-- Answers\n")
                submissionAnswers.forEach { a ->
                    append("INSERT OR REPLACE INTO `checklist_answers` (`id`, `submissionid`, `questionid`, `value`, `notes`, `imagePath`, `pictureSize`, `timestamp`) VALUES (\n")
                    append(
                        "  '${a.id}', '${a.submissionid}', '${a.questionid}', '${escapeSql(a.value)}', '${
                            escapeSql(
                                a.notes ?: ""
                            )
                        }', '${escapeSql(a.imagePath ?: "")}', '${escapeSql(a.pictureSize)}', ${a.timestamp}\n);\n"
                    )
                }
            }
        }
    }

    fun savePictureToMyChecks(
        context: Context,
        submissionId: String,
        answerId: String,
        sourceUri: Uri
    ): String? {
        val fileName = "Submission_${submissionId}_Answer_${answerId}_Picture.jpg"
        try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            val picBytes = inputStream.readBytes()

            // 1. Always save internal copy in app internal storage (context.filesDir/pictures)
            val internalDir = File(context.filesDir, "pictures")
            if (!internalDir.exists()) internalDir.mkdirs()
            val internalFile = File(internalDir, fileName)
            internalFile.writeBytes(picBytes)

            // 2. Save export copy in /download/pobvolMyChecks/<deviceid>/sql for cloud sync app
            val sqlDir = getSqlDir(context)
            val targetFile = File(sqlDir, fileName)
            targetFile.writeBytes(picBytes)

            return fileName
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun exportDatabaseCopy(context: Context): Boolean {
        try {
            // Flush SQLite WAL checkpoint before copying db bytes
            try {
                val db = AppDatabase.getDatabase(context).openHelper.writableDatabase
                val cursor = db.query("PRAGMA wal_checkpoint(FULL)")
                cursor.moveToFirst()
                cursor.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val dbFile = context.getDatabasePath("mychecksdb.sqlite3")
            if (!dbFile.exists()) return false

            val timeStamp =
                SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "MyChecks_Database_Backup_$timeStamp.sqlite3"
            val dbBytes = dbFile.readBytes()
            val backupDir = getDbBackupDir(context)

            val targetFile = File(backupDir, fileName)
            targetFile.writeBytes(dbBytes)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun importDatabase(context: Context): Boolean {
        try {
            val dbFile = context.getDatabasePath("mychecksdb.sqlite3")
            val backupFiles = mutableListOf<File>()

            val dbBackupDir = getDbBackupDir(context)
            if (dbBackupDir.exists()) {
                dbBackupDir.listFiles { _, name -> name.endsWith(".sqlite3") }?.let {
                    backupFiles.addAll(it)
                }
            }

            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val pobvolDir = File(downloadsDir, "pobvolMyChecks")
            if (pobvolDir.exists()) {
                pobvolDir.walkTopDown().forEach { file ->
                    if (file.isFile && file.name.endsWith(".sqlite3")) {
                        backupFiles.add(file)
                    }
                }
            }
            val oldDir = File(downloadsDir, "MyChecks")
            if (oldDir.exists()) {
                oldDir.listFiles { _, name -> name.endsWith(".sqlite3") }?.let {
                    backupFiles.addAll(it)
                }
            }

            val latestBackup = backupFiles.maxByOrNull { it.lastModified() } ?: return false

            // Close Room database connections and reset instance
            AppDatabase.resetDatabaseInstance()

            // Delete SQLite WAL and SHM cache files if present
            val walFile = context.getDatabasePath("mychecksdb.sqlite3-wal")
            if (walFile.exists()) walFile.delete()
            val shmFile = context.getDatabasePath("mychecksdb.sqlite3-shm")
            if (shmFile.exists()) shmFile.delete()

            latestBackup.inputStream().use { input ->
                dbFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun generateDemoDbAsset(context: Context): Boolean {
        try {
            val db = AppDatabase.getDatabase(context).openHelper.writableDatabase
            db.query("PRAGMA wal_checkpoint(FULL)").close()

            val dbFile = context.getDatabasePath("mychecksdb.sqlite3")
            if (!dbFile.exists()) return false

            val dbBytes = dbFile.readBytes()
            val targetAssetFile =
                File("C:/Users/Volker/AndroidStudioProjects/MyChecks/app/src/main/assets/demo_db.sqlite3")
            targetAssetFile.parentFile?.mkdirs()
            targetAssetFile.writeBytes(dbBytes)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun escapeSql(input: String): String {
        return input.replace("'", "''")
    }
}
