package com.pobvol.mychecks.util

import android.content.ClipData
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.pobvol.mychecks.data.local.ChecklistAnswerEntity
import com.pobvol.mychecks.data.local.ChecklistEntity
import com.pobvol.mychecks.data.local.ChecklistQuestionEntity
import com.pobvol.mychecks.data.local.ChecklistSubmissionEntity
import com.pobvol.mychecks.data.repository.UserSettings
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun generateAndOpenSubmissionPdf(
        context: Context,
        submission: ChecklistSubmissionEntity,
        checklist: ChecklistEntity?,
        questions: List<ChecklistQuestionEntity>,
        answers: List<ChecklistAnswerEntity>,
        settings: UserSettings = UserSettings(),
    ) {
        val answersMap = answers.filter { it.submissionid == submission.id }.associateBy { it.questionid }

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
        }

        var y = 50f
        val startX = 40f
        val endX = 555f

        // Inspection Header Details
        y = 50f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        paint.color = Color.BLACK
        val titleText = checklist?.title ?: "Checklist #${submission.checklistid}"
        canvas.drawText(titleText, startX, y, paint)

        if (settings.pdfHeaderTitle.isNotBlank()) {
            y += 20f
            paint.textSize = 12f
            paint.color = Color.DKGRAY
            canvas.drawText(settings.pdfHeaderTitle, startX, y, paint)
        }

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        paint.color = Color.DKGRAY

        canvas.drawText("Check Date: ${submission.date}", startX, y, paint)
        if (settings.includeInspector) {
            y += 16f
            canvas.drawText("Inspector: ${submission.inspector ?: "Anonymous"}", startX, y, paint)
        }
        y += 16f
        canvas.drawText("Status: ${submission.status}", startX, y, paint)
        if (!submission.notes.isNullOrBlank()) {
            y += 16f
            canvas.drawText("Notes: ${submission.notes}", startX, y, paint)
        }

        y += 20f
        paint.color = Color.LTGRAY
        canvas.drawLine(startX, y, endX, y, paint)

        // Questions Section
        val sortedQuestions = questions.sortedWith(compareBy({ it.sortno }, { it.id })).filter { q ->
            val answerEntity = answersMap[q.id]
            val hasAnswer = !answerEntity?.value.isNullOrBlank()
            settings.includeUnansweredQuestions || hasAnswer
        }

        sortedQuestions.forEach { q ->
            if (y > 770f) {
                return@forEach
            }

            y += 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11f
            paint.color = Color.BLACK
            val answerEntity = answersMap[q.id]
            val answerValue = answerEntity?.value ?: "(No answer)"

            if (settings.compactAnswerLayout) {
                canvas.drawText("${q.title}: $answerValue", startX, y, paint)
            } else {
                canvas.drawText(q.title, startX, y, paint)
                y += 14f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("    Answer: $answerValue", startX, y, paint)
            }

            if (settings.includeAnswerNotes && !answerEntity?.notes.isNullOrBlank()) {
                y += 14f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                paint.textSize = 10f
                paint.color = Color.DKGRAY
                canvas.drawText("    Notes: ${answerEntity.notes}", startX, y, paint)
            }

            y += 6f
        }

        // Footer Timestamp
        if (settings.includeTimestamp) {
            y = 810f
            paint.textSize = 9f
            paint.color = Color.GRAY
            val formattedGenerated = SimpleDateFormat("dd.MM.yyyy • HH:mm", Locale.getDefault()).format(Date())
            canvas.drawText("Generated on $formattedGenerated", startX, y, paint)
        }

        pdfDocument.finishPage(page)

        val pdfFile = File(context.cacheDir, "Submission_${submission.id}_Report.pdf")
        val fos = FileOutputStream(pdfFile)
        pdfDocument.writeTo(fos)
        fos.close()
        pdfDocument.close()

        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, pdfFile)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/pdf")
            setClipData(ClipData.newUri(context.contentResolver, "PDF Report", contentUri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                setClipData(ClipData.newUri(context.contentResolver, "PDF Report", contentUri))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Share Inspection PDF Report")
            chooser.setClipData(ClipData.newUri(context.contentResolver, "PDF Report", contentUri))
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }

    fun savePdfToMyChecks(
        context: Context,
        submission: ChecklistSubmissionEntity,
        checklist: ChecklistEntity?,
        questions: List<ChecklistQuestionEntity>,
        answers: List<ChecklistAnswerEntity>,
        settings: UserSettings = UserSettings(),
    ) {
        val answersMap = answers.filter { it.submissionid == submission.id }.associateBy { it.questionid }

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
        }

        var y = 50f
        val startX = 40f
        val endX = 555f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        paint.color = Color.BLACK
        val titleText = checklist?.title ?: "Checklist #${submission.checklistid}"
        canvas.drawText(titleText, startX, y, paint)

        if (settings.pdfHeaderTitle.isNotBlank()) {
            y += 20f
            paint.textSize = 12f
            paint.color = Color.DKGRAY
            canvas.drawText(settings.pdfHeaderTitle, startX, y, paint)
        }

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        paint.color = Color.DKGRAY

        canvas.drawText("Check Date: ${submission.date}", startX, y, paint)
        if (settings.includeInspector) {
            y += 16f
            canvas.drawText("Inspector: ${submission.inspector ?: "Anonymous"}", startX, y, paint)
        }
        y += 16f
        canvas.drawText("Status: ${submission.status}", startX, y, paint)
        if (!submission.notes.isNullOrBlank()) {
            y += 16f
            canvas.drawText("Notes: ${submission.notes}", startX, y, paint)
        }

        y += 20f
        paint.color = Color.LTGRAY
        canvas.drawLine(startX, y, endX, y, paint)

        val sortedQuestions = questions.sortedWith(compareBy({ it.sortno }, { it.id })).filter { q ->
            val answerEntity = answersMap[q.id]
            val hasAnswer = !answerEntity?.value.isNullOrBlank()
            settings.includeUnansweredQuestions || hasAnswer
        }

        sortedQuestions.forEach { q ->
            if (y > 770f) {
                return@forEach
            }

            y += 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11f
            paint.color = Color.BLACK
            val answerEntity = answersMap[q.id]
            val answerValue = answerEntity?.value ?: "(No answer)"

            if (settings.compactAnswerLayout) {
                canvas.drawText("${q.title}: $answerValue", startX, y, paint)
            } else {
                canvas.drawText(q.title, startX, y, paint)
                y += 14f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("    Answer: $answerValue", startX, y, paint)
            }

            if (settings.includeAnswerNotes && !answerEntity?.notes.isNullOrBlank()) {
                y += 14f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                paint.textSize = 10f
                paint.color = Color.DKGRAY
                canvas.drawText("    Notes: ${answerEntity.notes}", startX, y, paint)
            }

            y += 6f
        }

        if (settings.includeTimestamp) {
            y = 810f
            paint.textSize = 9f
            paint.color = Color.GRAY
            val formattedGenerated = SimpleDateFormat("dd.MM.yyyy • HH:mm", Locale.getDefault()).format(Date())
            canvas.drawText("Generated on $formattedGenerated", startX, y, paint)
        }

        pdfDocument.finishPage(page)

        val fileName = "Submission_${submission.id}_Report.pdf"
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
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { fos ->
                        pdfDocument.writeTo(fos)
                    }
                }
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MyChecks")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, fileName)
                FileOutputStream(file).use { fos ->
                    pdfDocument.writeTo(fos)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pdfDocument.close()
    }
}
