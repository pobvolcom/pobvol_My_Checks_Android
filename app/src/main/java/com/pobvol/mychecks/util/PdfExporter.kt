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
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
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

        // 1. Always check internal app storage first!
        val internalDir = File(context.filesDir, "pictures")
        val internalFile = File(internalDir, imagePath)
        if (internalFile.exists()) return internalFile

        val cacheFile = File(context.cacheDir, imagePath)
        if (cacheFile.exists()) return cacheFile

        // 2. Fallback to SQL export directory or root
        val sqlDir = SqlExporter.getSqlDir(context)
        val file = File(sqlDir, imagePath)
        if (file.exists()) return file

        val sendDir = SqlExporter.getSendDir(context)
        val sendFile = File(sendDir, imagePath)
        if (sendFile.exists()) return sendFile

        val rootDir = SqlExporter.getPdfDir(context)
        val rootFile = File(rootDir, imagePath)
        if (rootFile.exists()) return rootFile

        val oldDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "MyChecks"
        )
        val oldFile = File(oldDir, imagePath)
        if (oldFile.exists()) return oldFile

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

    private fun getOrderedSelectedOptions(
        questionOptions: String?,
        answerValue: String
    ): List<String> {
        val selectedSet =
            answerValue.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
        val definedOptions =
            questionOptions?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
                ?: emptyList()
        if (definedOptions.isNotEmpty()) {
            val ordered = definedOptions.filter { selectedSet.contains(it) }
            val remaining = selectedSet.filter { !definedOptions.contains(it) }
            return ordered + remaining
        }
        return selectedSet.toList()
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

    fun createSubmissionPdfDocument(
        context: Context,
        submission: ChecklistSubmissionEntity,
        checklist: ChecklistEntity?,
        questions: List<ChecklistQuestionEntity>,
        answers: List<ChecklistAnswerEntity>,
        settings: UserSettings = UserSettings(),
    ): PdfDocument {
        return createSubmissionPdfDocumentV1(
            context,
            submission,
            checklist,
            questions,
            answers,
            settings
        )
    }

    private fun createSubmissionPdfDocumentV1(
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

            if (settings.pdfHeaderTitle.isNotBlank()) {
                //hasHeaderContent = true
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 16f
                paint.color = Color.BLACK
                canvas.drawText(settings.pdfHeaderTitle, startX, y, paint)
                y += 24f
            }

            // print Checklist title & Status
            val headerTitleParts = mutableListOf<String>()
            if (checklist != null) {
                headerTitleParts.add(checklist.title)
            }
            if (settings.pdfIncludeStatus) {
                val translatedStatus = when (submission.status.uppercase()) {
                    "COMPLETED" -> context.getString(R.string.status_completed)
                    "IN_PROGRESS" -> context.getString(R.string.status_in_progress)
                    "PENDING" -> context.getString(R.string.status_pending)
                    else -> submission.status
                }
                headerTitleParts.add("(" + translatedStatus + ")")
            }

            if (settings.pdfIncludeChecklistTitle && headerTitleParts.isNotEmpty()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 14f
                paint.color = Color.BLACK
                canvas.drawText(headerTitleParts.joinToString(" "), startX, y, paint)
                y += 20f
            }

            // Questions section
            y += 10f
            if (y + 100f > bottomLimit) {
                startNewPage()
                y += 10f
            }

            val blueBgPaint = Paint().apply {
                color = "#82CFFD".toColorInt()
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            val greenLinePaint = Paint().apply {
                color = Color.parseColor("#008000")
                strokeWidth = 3f
                isAntiAlias = true
            }

            val tableHeaderRect = RectF(startX, y, endX, y + 20f)
            canvas.drawRect(tableHeaderRect, blueBgPaint)

            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.BLACK
            canvas.drawText(context.getString(R.string.pdf_pos), startX + 6f, y + 14f, paint)
            canvas.drawText(
                context.getString(R.string.pdf_checkpoint),
                startX + 40f,
                y + 14f,
                paint
            )
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(context.getString(R.string.pdf_status), endX - 12f, y + 14f, paint)
            paint.textAlign = Paint.Align.LEFT

            y += 24f

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

        // print Checklist title & Status
        val headerTitleParts = mutableListOf<String>()
        if (checklist != null) {
            headerTitleParts.add(checklist.title)
        }
        if (settings.pdfIncludeStatus) {
            val translatedStatus = when (submission.status.uppercase()) {
                "COMPLETED" -> context.getString(R.string.status_completed)
                "IN_PROGRESS" -> context.getString(R.string.status_in_progress)
                "PENDING" -> context.getString(R.string.status_pending)
                else -> submission.status
            }
            headerTitleParts.add("(" + translatedStatus + ")")
        }

        if (settings.pdfIncludeChecklistTitle && headerTitleParts.isNotEmpty()) {
            hasHeaderContent = true
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 14f
            paint.color = Color.BLACK
            canvas.drawText(headerTitleParts.joinToString(" "), startX, y, paint)
            y += 20f
        }

        // print Notes
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        paint.color = Color.DKGRAY

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

        //----------------------------------------------------------------------
        // Questions section
        y += 10f
        if (y + 100f > bottomLimit) {
            startNewPage()
            y += 10f
        }

        val blueBgPaint = Paint().apply {
            color = "#82CFFD".toColorInt()
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val greenLinePaint = Paint().apply {
            color = Color.parseColor("#008000")
            strokeWidth = 3f
            isAntiAlias = true
        }

        val tableHeaderRect = RectF(startX, y, endX, y + 20f)
        canvas.drawRect(tableHeaderRect, blueBgPaint)

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.BLACK
        canvas.drawText(context.getString(R.string.pdf_pos), startX + 6f, y + 14f, paint)
        canvas.drawText(context.getString(R.string.pdf_checkpoint), startX + 40f, y + 14f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(context.getString(R.string.pdf_status), endX - 12f, y + 14f, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 24f

        val sortedQuestions =
            questions.sortedWith(compareBy({ it.sortno }, { it.id })).filter { q ->
                val answerEntity = answersMap[q.id]
                val hasAnswer =
                    !answerEntity?.value.isNullOrBlank() || !answerEntity?.imagePath.isNullOrBlank()
                settings.includeUnansweredQuestions || hasAnswer
            }

        sortedQuestions.forEachIndexed { qIndex, q ->

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

            val linePadding = if (qIndex > 0) 6f else 0f
            val hasTitle = settings.pdfIncludeQuestionTitle
            val hasValue = settings.pdfIncludeAnswerValue
            val hasNotes = settings.pdfIncludeAnswerNotes && !answerEntity?.notes.isNullOrBlank()
            val qType = q.type.lowercase()
            val isRightAlignedType =
                qType in listOf("number", "combobox", "multiselect", "checkbox")


            // Calculate Left Block Height (Title + Left Answer + Notes + Picture)
            var leftBlockHeight = 0f
            if (hasTitle) leftBlockHeight += 14f
            if (hasValue && !isRightAlignedType) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 11f
                val answerContent = "    $answerValue"
                leftBlockHeight += calculateMultiLineHeight(
                    answerContent,
                    paint,
                    endX - startX,
                    16f
                )
            }
            if (hasNotes) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                paint.textSize = 10f
                leftBlockHeight += calculateMultiLineHeight(
                    "    ${answerEntity.notes}",
                    paint,
                    endX - startX,
                    14f
                )
            }

            var picBitmap: Bitmap? = null
            var picHeight = 0f
            if (settings.pdfIncludeAnswerPicture && !answerEntity?.imagePath.isNullOrBlank()) {
                val picFile = getPictureFile(context, answerEntity.imagePath)
                if (picFile != null && picFile.exists()) {
                    try {
                        picBitmap = BitmapFactory.decodeFile(picFile.absolutePath)
                        if (picBitmap != null) {
                            val maxAllowedWidth = when (answerEntity.pictureSize?.lowercase()) {
                                "small", "s" -> settings.pdfPictureSizeSmall.toFloat()
                                "large", "l" -> settings.pdfPictureSizeLarge.toFloat()
                                else -> settings.pdfPictureSizeMedium.toFloat()
                            }
                            val targetWidth = minOf(picBitmap.width.toFloat(), maxAllowedWidth)
                            val targetHeight =
                                (picBitmap.height.toFloat() / picBitmap.width.toFloat()) * targetWidth
                            picHeight = targetHeight + 8f
                            leftBlockHeight += picHeight
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // Calculate Right Block Height
            var rightBlockHeight = 0f
            if (hasValue && isRightAlignedType) {
                if (qType == "multiselect") {
                    val optionsSelected = getOrderedSelectedOptions(q.options, answerValue)
                    rightBlockHeight += maxOf(1, optionsSelected.size) * 16f
                } else {
                    rightBlockHeight += 16f
                }
            }

            val questionRowContentHeight = maxOf(leftBlockHeight, rightBlockHeight)
            val blockHeight = questionRowContentHeight + linePadding

            if (blockHeight > linePadding) {
                if (y + blockHeight > bottomLimit) {
                    startNewPage()
                }

                val startY = y
                var leftY = startY
                var rightY = startY


                //canvas.drawLine(endX - 2f, y - 4f, endX - 2f, y + blockHeight - 6f, greenLinePaint)

                // print POS
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText("${qIndex + 1}", 46f, y + 10f, paint)


                // LEFT BLOCK RENDERING (Title -> Left Answer -> Notes -> Picture)

                // TITLE
                if (hasTitle) {
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textAlign = Paint.Align.LEFT
                    paint.color = Color.BLACK
                    val titleStr = truncateText(paint, q.title, 320f)
                    canvas.drawText(titleStr, startX + 40f, y + 10f, paint)
                    leftY = y + 24f
                }

                // TEXT VALUE must be printed below of the title
                if (hasValue && !isRightAlignedType) {
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.BLACK
                    val answerContent = "    $answerValue"
                    leftY = drawMultiLineText(
                        canvas = canvas,
                        text = answerContent,
                        startX = startX + 40f,
                        startY = leftY,
                        paint = paint,
                        maxWidth = 320f,
                        lineHeight = 16f,
                        bottomLimit = bottomLimit,
                        onPageOverflow = { startNewPage() }
                    )
                }

                // NOTES must be printed below of question & text answers
                if (hasNotes) {
                    paint.textSize = 10f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    paint.color = Color.DKGRAY
                    val notesContent = "    ${answerEntity.notes}"
                    leftY = drawMultiLineText(
                        canvas = canvas,
                        text = notesContent,
                        startX = startX + 40f,
                        startY = leftY,
                        paint = paint,
                        maxWidth = 320f,
                        lineHeight = 14f,
                        bottomLimit = bottomLimit,
                        onPageOverflow = { startNewPage() }
                    )
                }

                // PICTURES must be printed below of notes
                if (picBitmap != null && picHeight > 0f) {
                    try {
                        val maxAllowedWidth = when (answerEntity?.pictureSize?.lowercase()) {
                            "small", "s" -> settings.pdfPictureSizeSmall.toFloat()
                            "large", "l" -> settings.pdfPictureSizeLarge.toFloat()
                            else -> settings.pdfPictureSizeMedium.toFloat()
                        }
                        val targetWidth = minOf(picBitmap.width.toFloat(), maxAllowedWidth)
                        val targetHeight =
                            (picBitmap.height.toFloat() / picBitmap.width.toFloat()) * targetWidth
                        val destRect = RectF(
                            startX + 40f,
                            leftY,
                            startX + 40f + targetWidth,
                            leftY + targetHeight
                        )
                        val bitmapPaint = Paint().apply {
                            isAntiAlias = true
                            isFilterBitmap = true
                            isDither = true
                        }
                        canvas.drawBitmap(picBitmap, null, destRect, bitmapPaint)
                        leftY += targetHeight + 10f
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        picBitmap.recycle()
                    }
                }

                // RIGHT BLOCK RENDERING (Right-aligned values / multiselect options)

                // print "✓" instead of true, yes, ja, ok, completed,...
                val statusDisplay = if (answerValue.lowercase() in listOf(
                        "true",
                        "yes",
                        "ja",
                        "ok",
                        "completed"
                    )
                ) "✓" else answerValue


                if (hasValue && isRightAlignedType) {
                    val valX = startX + 280f
                    if (qType == "multiselect") {
                        val optionsSelected = getOrderedSelectedOptions(q.options, answerValue)
                        if (optionsSelected.isEmpty()) {
                            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            paint.color = Color.BLACK
                            canvas.drawText(answerValue, valX, rightY, paint)
                            rightY += 16f
                        } else {
                            rightY += 10f
                            optionsSelected.forEachIndexed { optIndex, optVal ->
                                val optY = rightY + (optIndex * 16f)
                                val cleanedOpt = cleanOptionText(optVal)
                                val highlightColor = getComboboxHighlightColor(q.options, optVal)

                                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
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
                                    paint.textAlign = Paint.Align.RIGHT
                                    canvas.drawRect(
                                        endX - 12f - textWidth - 4f,
                                        optY - 11f,
                                        endX - 12f + 4f,
                                        optY + 4f,
                                        bgPaint
                                    )
                                    paint.textAlign = Paint.Align.LEFT

                                }

                                paint.textAlign = Paint.Align.RIGHT
                                canvas.drawText(cleanedOpt, endX - 12f, optY, paint)
                                paint.textAlign = Paint.Align.LEFT

                            }
                            rightY += optionsSelected.size * 16f
                        }
                    } else {
                        val cleanedVal = cleanOptionText(statusDisplay)
                        val highlightColor = getComboboxHighlightColor(q.options, answerValue)
                        //) else HighlightColor.NONE

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
                            paint.textAlign = Paint.Align.RIGHT
                            canvas.drawRect(
                                endX - 12f - textWidth - 4f,
                                rightY + 10 - 11f,
                                endX - 12f + 4f,
                                rightY + 10 + 4f,
                                bgPaint
                            )
                            paint.textAlign = Paint.Align.LEFT

                        }

                        //canvas.drawText(cleanedVal, valX, rightY, paint)
                        paint.textAlign = Paint.Align.RIGHT
                        canvas.drawText(cleanedVal, endX - 12f, y + 10f, paint)
                        paint.textAlign = Paint.Align.LEFT
                        rightY += 16f
                    }
                }

                // print a line after position is printed
                y = maxOf(leftY, rightY) - 6f
                canvas.drawLine(startX, y, endX, y, dividerPaint)
                y += 6f
            }
        }
        //------------------------------------------------------------------

        //Print Date and Inspector Signature
        if (settings.pdfIncludeDate || settings.pdfIncludeInspector) {
            y += 40f
            if (y + 40 > bottomLimit) {
                startNewPage()
                y += 40f
            }
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textAlign = Paint.Align.LEFT
            paint.color = Color.DKGRAY
            //----------------------------------------------
            //print Date
            paint.textSize = 11f
            val footerX = 40f
            canvas.drawText(submission.date, footerX, y + 10f, paint)
            //print line
            canvas.drawLine(footerX, y + 16, footerX + 160f, y + 16, dividerPaint)
            //print label Date
            paint.textSize = 8f
            canvas.drawText(context.getString(R.string.date), footerX, y + 26f, paint)
            //----------------------------------------------
            //print Inspector
            paint.textSize = 11f
            canvas.drawLine(footerX + 220f, y + 16, footerX + 380f, y + 16, dividerPaint)
            paint.textSize = 8f
            canvas.drawText(
                context.getString(R.string.inspector) + ": " + submission.inspector,
                footerX + 220f,
                y + 26f,
                paint
            )
            y += 40f
        }
        //----------------------------------------------
        // print Footer
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
        val pdfFileName = "Submission_${submission.id}_Report.pdf"

        // 1. Ensure copy is saved in /download/pobvolMyChecks/<deviceid>/pdf
        savePdfToMyChecks(context, submission, checklist, questions, answers, settings)

        // 2. Generate/render into context.cacheDir so external PDF viewer apps can read it via FileProvider without URI permission crashes
        val pdfFile = File(context.cacheDir, pdfFileName)
        val pdfDocument = createSubmissionPdfDocument(
            context,
            submission,
            checklist,
            questions,
            answers,
            settings
        )
        try {
            val fos = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fos)
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }

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
        val subFolderPath = "pobvolMyChecks/pdf"

        try {
            val baos = java.io.ByteArrayOutputStream()
            pdfDocument.writeTo(baos)
            val pdfBytes = baos.toByteArray()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/$subFolderPath"

                val projection = arrayOf(MediaStore.MediaColumns._ID)
                val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
                val selectionArgs = arrayOf(fileName)

                resolver.query(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )?.use { queryCursor ->
                    while (queryCursor.moveToNext()) {
                        val idColumn =
                            queryCursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                        val id = queryCursor.getLong(idColumn)
                        val existingUri = ContentUris.withAppendedId(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            id
                        )
                        try {
                            resolver.delete(existingUri, null, null)
                        } catch (_: Exception) {
                        }
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
                        fos.write(pdfBytes)
                    }
                }
            } else {
                val dir = SqlExporter.getPdfDir(context)
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, fileName)
                file.writeBytes(pdfBytes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pdfDocument.close()
    }
}
