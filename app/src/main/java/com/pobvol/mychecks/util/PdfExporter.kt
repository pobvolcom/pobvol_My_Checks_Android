package com.pobvol.mychecks.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
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

object PdfExporter {

    fun generateAndOpenSubmissionPdf(
        context: Context,
        submission: ChecklistSubmissionEntity,
        checklist: ChecklistEntity?,
        questions: List<ChecklistQuestionEntity>,
        answers: List<ChecklistAnswerEntity>,
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

        // App Title
        /*
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 20f
        paint.color = Color.rgb(33, 150, 243)
        canvas.drawText("pobvol Open Checklists", startX, y, paint)

        y += 24f
        paint.textSize = 13f
        paint.color = Color.DKGRAY
        canvas.drawText("Inspection & Checklist Submission Report", startX, y, paint)

        y += 16f
        paint.color = Color.LTGRAY
        paint.strokeWidth = 1f
        canvas.drawLine(startX, y, endX, y, paint)
        */

        // Inspection Header Details
        /*y += 28f*/
        y = 50f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        paint.color = Color.BLACK
        val titleText = checklist?.title ?: "Checklist #${submission.checklistid}"
        canvas.drawText(titleText, startX, y, paint)

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        paint.color = Color.DKGRAY

        canvas.drawText("Check Date: ${submission.date}", startX, y, paint)
        y += 16f
        canvas.drawText("Inspector: ${submission.inspector ?: "Anonymous"}", startX, y, paint)
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

        /*
        y += 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 13f
        paint.color = Color.BLACK
        canvas.drawText("Inspection Questions & Answers:", startX, y, paint)
        y += 12f
         */

        val sortedQuestions = questions.sortedWith(compareBy({ it.sortno }, { it.id }))

        sortedQuestions.forEachIndexed { index, q ->
            if (y > 770f) {
                return@forEachIndexed
            }

            y += 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11f
            paint.color = Color.BLACK
            val sortLabel = if (q.sortno > 0) "#${q.sortno}" else "#${index + 1}"
            val answerEntity = answersMap[q.id]
            val answerValue = answerEntity?.value ?: "(No answer)"
            /* y += 16f */
            /* canvas.drawText("$sortLabel. ${q.title}", startX, y, paint) */
            canvas.drawText("${q.title}: $answerValue", startX, y, paint)

            /* paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) */
            /* paint.textSize = 10f */
            /* paint.color = Color.rgb(0, 100, 0) */
            /* canvas.drawText("    Answer: $answerValue", startX, y, paint) */
            /* canvas.drawText("$answerValue", endX, y, paint) */

            if (answerEntity != null && !answerEntity.notes.isNullOrBlank()) {
                y += 14f
                paint.color = Color.DKGRAY
                /* canvas.drawText("    Answer Notes: ${answerEntity.notes}", startX, y, paint) */
                canvas.drawText("    ${answerEntity.notes}", startX, y, paint)
            }

            y += 6f
        }

        // Footer
        y = 810f
        paint.textSize = 9f
        paint.color = Color.GRAY
        val formattedGenerated = SimpleDateFormat("dd.MM.yyyy • HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generated on $formattedGenerated", startX, y, paint)

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
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Share Inspection PDF Report")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }
}
