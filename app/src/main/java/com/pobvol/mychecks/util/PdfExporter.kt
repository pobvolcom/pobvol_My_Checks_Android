package com.pobvol.mychecks.util

import android.content.ClipData
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.pobvol.mychecks.R
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

    enum class HighlightColor {
        GREEN, YELLOW, RED, NONE
    }

    fun getPictureFile(context: Context, imagePath: String?): File? {
        if (imagePath.isNullOrBlank()) return null
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "MyChecks"
        )
        val file = File(dir, imagePath)
        if (file.exists()) return file

        val cacheFile = File(context.cacheDir, imagePath)
        if (cacheFile.exists()) return cacheFile

        return null
    }

    private fun truncateText(paint: Paint, text: String, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var truncated = text
        while (truncated.isNotEmpty() && paint.measureText("$truncated...") > maxWidth) {
            truncated = truncated.dropLast(1)
        }
        return "$truncated..."
    }

    private fun getComboboxHighlightColor(questionOptions: String?, answerValue: String): HighlightColor {
        if (questionOptions.isNullOrBlank()) {
            val lower = answerValue.lowercase()
            return when {
                lower.contains("ohne") || lower.contains("ok") || lower.contains("bestanden") -> HighlightColor.GREEN
                lower.contains("erforderlich") || lower.contains("danger") || lower.contains("schwer") -> HighlightColor.RED
                lower.contains("beseitigt") || lower.contains("minor") || lower.contains("gering") -> HighlightColor.YELLOW
                else -> HighlightColor.NONE
            }
        }

        val optionsList = questionOptions.split(",").map { it.trim() }
        val matchingOption = optionsList.find { 
            cleanOptionText(it).equals(answerValue, ignoreCase = true) || it.equals(answerValue, ignoreCase = true) 
        } ?: optionsList.firstOrNull() ?: ""

        val lowerOpt = matchingOption.lowercase()
        return when {
            lowerOpt.contains("[green]") || lowerOpt.contains("[grün]") || lowerOpt.contains("[gruen]") -> HighlightColor.GREEN
            lowerOpt.contains("[yellow]") || lowerOpt.contains("[gelb]") -> HighlightColor.YELLOW
            lowerOpt.contains("[red]") || lowerOpt.contains("[rot]") -> HighlightColor.RED
            else -> {
                val lowerVal = answerValue.lowercase()
                when {
                    lowerVal.contains("ohne") || lowerVal.contains("ok") || lowerVal.contains("bestanden") -> HighlightColor.GREEN
                    lowerVal.contains("erforderlich") || lowerVal.contains("danger") || lowerVal.contains("schwer") -> HighlightColor.RED
                    lowerVal.contains("beseitigt") || lowerVal.contains("minor") || lowerVal.contains("gering") -> HighlightColor.YELLOW
                    else -> HighlightColor.NONE
                }
            }
        }
    }

    private fun cleanOptionText(optionText: String): String {
        return optionText
            .replace(Regex("(?i)\\[green]"), "")
            .replace(Regex("(?i)\\[grün]"), "")
            .replace(Regex("(?i)\\[gruen]"), "")
            .replace(Regex("(?i)\\[yellow]"), "")
            .replace(Regex("(?i)\\[gelb]"), "")
            .replace(Regex("(?i)\\[red]"), "")
            .replace(Regex("(?i)\\[rot]"), "")
            .trim()
    }

    private fun calculateMultiLineHeight(
        text: String,
        paint: Paint,
        maxWidth: Float,
        lineHeight: Float
    ): Float {
        var lineCount = 0
        val paragraphs = text.replace("\r\n", "\n").split("\n")
        paragraphs.forEach { paragraph ->
            if (paragraph.isBlank()) {
                lineCount++
                return@forEach
            }
            val words = paragraph.split(" ")
            var currentLine = StringBuilder()
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) <= maxWidth) {
                    currentLine.append(if (currentLine.isEmpty()) word else " $word")
                } else {
                    lineCount++
                    currentLine = StringBuilder(word)
                }
            }
            if (currentLine.isNotEmpty()) {
                lineCount++
            }
        }
        return lineCount * lineHeight
    }

    private fun drawMultiLineText(
        canvas: Canvas,
        text: String,
        startX: Float,
        startY: Float,
        paint: Paint,
        maxWidth: Float,
        lineHeight: Float = 16f,
        bottomLimit: Float = 760f,
        onPageOverflow: () -> Unit = {}
    ): Float {
        var currentY = startY
        val paragraphs = text.replace("\r\n", "\n").split("\n")

        paragraphs.forEach { paragraph ->
            if (paragraph.isBlank()) {
                currentY += lineHeight
                return@forEach
            }

            val words = paragraph.split(" ")
            var currentLine = StringBuilder()

            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) <= maxWidth) {
                    currentLine.append(if (currentLine.isEmpty()) word else " $word")
                } else {
                    if (currentY > bottomLimit) {
                        onPageOverflow()
                        currentY = 50f
                    }
                    canvas.drawText(currentLine.toString(), startX, currentY, paint)
                    currentY += lineHeight
                    currentLine = StringBuilder(word)
                }
            }

            if (currentLine.isNotEmpty()) {
                if (currentY > bottomLimit) {
                    onPageOverflow()
                    currentY = 50f
                }
                canvas.drawText(currentLine.toString(), startX, currentY, paint)
                currentY += lineHeight
            }
        }

        return currentY
    }

    private fun createSubmissionPdfDocument(
        context: Context,
        submission: ChecklistSubmissionEntity,
        checklist: ChecklistEntity?,
        questions: List<ChecklistQuestionEntity>,
        answers: List<ChecklistAnswerEntity>,
        settings: UserSettings = UserSettings(),
    ): PdfDocument {
        val answersMap = answers.filter { it.submissionid == submission.id }.associateBy { it.questionid }
        val pdfDocument = PdfDocument()

        val pageWidth = 595
        val pageHeight = 842
        val startX = 40f
        val endX = 555f
        val bottomLimit = 760f

        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
        }

        val dividerPaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 0.5f
        }

        var y = 50f

        fun drawFooter() {
            paint.textSize = 9f
            paint.color = Color.GRAY
            var footerLine = ""
            if (settings.includeTimestamp) {
                val formattedGenerated = SimpleDateFormat("dd.MM.yyyy • HH:mm", Locale.getDefault()).format(Date())
                footerLine += context.getString(R.string.pdf_generated_on, formattedGenerated)
            }
            if (settings.pdfIncludePageNumber) {
                if (footerLine.isNotEmpty()) footerLine += "  •  "
                footerLine += context.getString(R.string.pdf_page_number, pageNum)
            }
            if (footerLine.isNotEmpty()) {
                canvas.drawText(footerLine, startX, 810f, paint)
            }
            if (settings.pdfFooterText.isNotBlank()) {
                canvas.drawText(settings.pdfFooterText, startX, 796f, paint)
            }
        }

        fun startNewPage() {
            drawFooter()
            pdfDocument.finishPage(page)
            pageNum++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            y = 50f
        }

        // Inspection Header Details (Page 1)
        var hasHeaderContent = false
        if (settings.pdfHeaderTitle.isNotBlank()) {
            hasHeaderContent = true
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 16f
            paint.color = Color.BLACK
            canvas.drawText(settings.pdfHeaderTitle, startX, y, paint)
            y += 24f
        }

        if (settings.pdfIncludeChecklistTitle && checklist != null) {
            hasHeaderContent = true
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 14f
            paint.color = Color.BLACK
            canvas.drawText(checklist.title, startX, y, paint)
            y += 20f
        }

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        paint.color = Color.DKGRAY

        val headerParts = mutableListOf<String>()
        if (settings.pdfIncludeDate) {
            headerParts.add(context.getString(R.string.pdf_date_label, submission.date))
        }
        if (settings.pdfIncludeInspector && !submission.inspector.isNullOrBlank()) {
            headerParts.add(context.getString(R.string.pdf_inspector_label, submission.inspector))
        }
        if (settings.pdfIncludeStatus) {
            val translatedStatus = when (submission.status.uppercase()) {
                "COMPLETED" -> context.getString(R.string.status_completed)
                "IN_PROGRESS" -> context.getString(R.string.status_in_progress)
                "PENDING" -> context.getString(R.string.status_pending)
                else -> submission.status
            }
            headerParts.add(context.getString(R.string.pdf_status_label, translatedStatus))
        }

        if (headerParts.isNotEmpty()) {
            hasHeaderContent = true
            canvas.drawText(headerParts.joinToString("  •  "), startX, y, paint)
            y += 18f
        }

        if (settings.pdfIncludeNotes && !submission.notes.isNullOrBlank()) {
            hasHeaderContent = true
            y = drawMultiLineText(
                canvas = canvas,
                text = submission.notes,
                startX = startX,
                startY = y,
                paint = paint,
                maxWidth = endX - startX,
                lineHeight = 16f,
                bottomLimit = bottomLimit,
                onPageOverflow = { startNewPage() }
            )
        }

        if (hasHeaderContent) {
            y += 10f
            paint.color = Color.LTGRAY
            canvas.drawLine(startX, y, endX, y, paint)
            y += 20f
        }

        // Questions Section
        val sortedQuestions = questions.sortedWith(compareBy({ it.sortno }, { it.id })).filter { q ->
            val answerEntity = answersMap[q.id]
            val hasAnswer =
                !answerEntity?.value.isNullOrBlank() || !answerEntity?.imagePath.isNullOrBlank()
            settings.includeUnansweredQuestions || hasAnswer
        }

        sortedQuestions.forEach { q ->
            val answerEntity = answersMap[q.id]
            val answerValueRaw = answerEntity?.value ?: context.getString(R.string.pdf_no_answer)
            val answerValue = if (q.type.lowercase() == "checkbox") {
                if (answerValueRaw.equals("true", ignoreCase = true) || answerValueRaw.equals("yes", ignoreCase = true) || answerValueRaw.equals("ja", ignoreCase = true)) {
                    context.getString(R.string.pdf_yes)
                } else if (answerValueRaw.equals("false", ignoreCase = true) || answerValueRaw.equals("no", ignoreCase = true) || answerValueRaw.equals("nein", ignoreCase = true)) {
                    context.getString(R.string.pdf_no)
                } else {
                    answerValueRaw
                }
            } else {
                answerValueRaw
            }

            val qType = q.type.lowercase()
            val isRightAlignedType = qType in listOf("text", "number", "combobox", "multiselect", "checkbox")

            var blockHeight = 0f
            val hasTitle = settings.pdfIncludeQuestionTitle
            val hasValue = settings.pdfIncludeAnswerValue
            val hasNotes = settings.pdfIncludeAnswerNotes && !answerEntity?.notes.isNullOrBlank()

            if (hasTitle || hasValue) {
                if (isRightAlignedType) {
                    if (qType == "multiselect") {
                        val optionsSelected = answerValue.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        val linesCount = maxOf(1, optionsSelected.size)
                        blockHeight += 14f + (linesCount * 16f)
                    } else {
                        blockHeight += 18f
                    }
                } else {
                    if (hasTitle) blockHeight += 14f
                    if (hasValue) {
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                        paint.textSize = 11f
                        val answerContent = if (qType in listOf("nfc", "qr-code", "qrcode")) {
                            "    $answerValue"
                        } else {
                            val answerLabel = context.getString(R.string.pdf_answer_label, "").trimEnd()
                            "    $answerLabel $answerValue"
                        }
                        blockHeight += calculateMultiLineHeight(answerContent, paint, endX - startX, 16f)
                    }
                }
            }
            if (hasNotes && answerEntity.notes != null) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                paint.textSize = 10f
                blockHeight += calculateMultiLineHeight("    ${answerEntity.notes}", paint, endX - startX, 14f)
            }

            var picBitmap: Bitmap? = null
            var picHeight = 0f
            if (settings.pdfIncludeAnswerPicture && !answerEntity?.imagePath.isNullOrBlank()) {
                val picFile = getPictureFile(context, answerEntity.imagePath)
                if (picFile != null && picFile.exists()) {
                    try {
                        picBitmap = BitmapFactory.decodeFile(picFile.absolutePath)
                        if (picBitmap != null) {
                            val targetWidth = 100f
                            val targetHeight =
                                (picBitmap.height.toFloat() / picBitmap.width.toFloat()) * targetWidth
                            picHeight = targetHeight + 8f
                            blockHeight += picHeight
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            if (blockHeight > 0f) {
                if (y + blockHeight > bottomLimit) {
                    startNewPage()
                }

                // Draw thin divider line between answers/questions
                canvas.drawLine(startX, y - 4f, endX, y - 4f, dividerPaint)
                y += 2f

                paint.textSize = 11f

                if (isRightAlignedType) {
                    if (hasTitle) {
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        paint.color = Color.BLACK
                        val questionTitle = truncateText(paint, q.title, 260f)
                        canvas.drawText(questionTitle, startX, y, paint)
                    }

                    if (hasValue) {
                        val valX = startX + 280f
                        if (qType == "multiselect") {
                            val optionsSelected = answerValue.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            if (optionsSelected.isEmpty()) {
                                canvas.drawText(answerValue, valX, y, paint)
                            } else {
                                optionsSelected.forEachIndexed { optIndex, optVal ->
                                    val optY = y + (optIndex * 16f)
                                    val cleanedOpt = cleanOptionText(optVal)
                                    val highlightColor = getComboboxHighlightColor(q.options, optVal)

                                    paint.typeface = Typeface.create(
                                        Typeface.DEFAULT,
                                        if (hasTitle && optIndex == 0) Typeface.NORMAL else Typeface.BOLD
                                    )
                                    paint.color = Color.BLACK

                                    if (highlightColor != HighlightColor.NONE) {
                                        val bgPaint = Paint().apply {
                                            color = when (highlightColor) {
                                                HighlightColor.GREEN -> Color.parseColor("#D4EDDA")
                                                HighlightColor.YELLOW -> Color.parseColor("#FFF3CD")
                                                HighlightColor.RED -> Color.parseColor("#F8D7DA")
                                                else -> Color.TRANSPARENT
                                            }
                                            style = Paint.Style.FILL
                                        }
                                        val textWidth = paint.measureText(cleanedOpt)
                                        canvas.drawRect(valX - 4f, optY - 11f, valX + textWidth + 4f, optY + 4f, bgPaint)
                                    }

                                    canvas.drawText(cleanedOpt, valX, optY, paint)
                                }
                                y += (optionsSelected.size - 1) * 16f
                            }
                        } else {
                            val cleanedVal = if (qType == "combobox") cleanOptionText(answerValue) else answerValue
                            val highlightColor = if (qType == "combobox") getComboboxHighlightColor(q.options, answerValue) else HighlightColor.NONE

                            paint.typeface = Typeface.create(
                                Typeface.DEFAULT,
                                if (hasTitle) Typeface.NORMAL else Typeface.BOLD
                            )
                            paint.color = Color.BLACK

                            if (highlightColor != HighlightColor.NONE) {
                                val bgPaint = Paint().apply {
                                    color = when (highlightColor) {
                                        HighlightColor.GREEN -> Color.parseColor("#D4EDDA")
                                        HighlightColor.YELLOW -> Color.parseColor("#FFF3CD")
                                        HighlightColor.RED -> Color.parseColor("#F8D7DA")
                                        else -> Color.TRANSPARENT
                                    }
                                    style = Paint.Style.FILL
                                }
                                val textWidth = paint.measureText(cleanedVal)
                                canvas.drawRect(valX - 4f, y - 11f, valX + textWidth + 4f, y + 4f, bgPaint)
                            }

                            canvas.drawText(cleanedVal, valX, y, paint)
                        }
                    }
                    y += 18f
                } else {
                    if (hasTitle) {
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        paint.color = Color.BLACK
                        canvas.drawText(q.title, startX, y, paint)
                        y += 14f
                    }

                    if (hasValue) {
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                        paint.color = Color.BLACK
                        val answerContent = if (qType in listOf("nfc", "qr-code", "qrcode")) {
                            "    $answerValue"
                        } else {
                            val answerLabel = context.getString(R.string.pdf_answer_label, "").trimEnd()
                            "    $answerLabel $answerValue"
                        }
                        y = drawMultiLineText(
                            canvas = canvas,
                            text = answerContent,
                            startX = startX,
                            startY = y,
                            paint = paint,
                            maxWidth = endX - startX,
                            lineHeight = 16f,
                            bottomLimit = bottomLimit,
                            onPageOverflow = { startNewPage() }
                        )
                    }
                }

                if (hasNotes && answerEntity.notes != null) {
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    paint.textSize = 10f
                    paint.color = Color.DKGRAY
                    val notesContent = "    ${answerEntity.notes}"
                    y = drawMultiLineText(
                        canvas = canvas,
                        text = notesContent,
                        startX = startX,
                        startY = y,
                        paint = paint,
                        maxWidth = endX - startX,
                        lineHeight = 14f,
                        bottomLimit = bottomLimit,
                        onPageOverflow = { startNewPage() }
                    )
                }

                if (picBitmap != null && picHeight > 0f) {
                    try {
                        val targetWidth = 100f
                        val targetHeight =
                            (picBitmap.height.toFloat() / picBitmap.width.toFloat()) * targetWidth
                        val scaledPicBitmap = Bitmap.createScaledBitmap(
                            picBitmap,
                            targetWidth.toInt(),
                            targetHeight.toInt(),
                            true
                        )
                        canvas.drawBitmap(scaledPicBitmap, startX + 20f, y, paint)
                        y += targetHeight + 6f
                        scaledPicBitmap.recycle()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        picBitmap.recycle()
                    }
                }

                y += 8f
            }
        }

        drawFooter()
        pdfDocument.finishPage(page)
        return pdfDocument
    }

    fun generateAndOpenSubmissionPdf(
        context: Context,
        submission: ChecklistSubmissionEntity,
        checklist: ChecklistEntity?,
        questions: List<ChecklistQuestionEntity>,
        answers: List<ChecklistAnswerEntity>,
        settings: UserSettings = UserSettings(),
    ) {
        val pdfDocument = createSubmissionPdfDocument(
            context,
            submission,
            checklist,
            questions,
            answers,
            settings
        )

        val pdfFile = File(context.cacheDir, "Submission_${submission.id}_Report.pdf")
        val fos = FileOutputStream(pdfFile)
        pdfDocument.writeTo(fos)
        fos.close()
        pdfDocument.close()

        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, pdfFile)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/pdf")
            clipData = ClipData.newUri(context.contentResolver, "PDF Report", contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                clipData = ClipData.newUri(context.contentResolver, "PDF Report", contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Share Inspection PDF Report")
            chooser.clipData = ClipData.newUri(context.contentResolver, "PDF Report", contentUri)
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
        val pdfDocument = createSubmissionPdfDocument(
            context,
            submission,
            checklist,
            questions,
            answers,
            settings
        )
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
                )?.use { queryCursor ->
                    if (queryCursor.moveToFirst()) {
                        val idColumn = queryCursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                        val id = queryCursor.getLong(idColumn)
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
        } catch (_: Exception) {
        }
        pdfDocument.close()
    }
}
