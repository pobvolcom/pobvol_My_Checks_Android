package com.pobvol.mychecks.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pobvol.mychecks.R
import com.pobvol.mychecks.data.local.ChecklistEntity
import com.pobvol.mychecks.data.repository.ThemeMode
import com.pobvol.mychecks.ui.ChecklistUiState

enum class ChartTimeMode {
    DAILY, MONTHLY, YEARLY
}

private fun formatSubmissionDate(dateStr: String, mode: ChartTimeMode): String {
    if (dateStr.isBlank()) return "Unknown"
    val normalized = if (dateStr.contains(".") && dateStr.length >= 10) {
        val parts = dateStr.split(".")
        if (parts.size >= 3) {
            val day = parts[0].padStart(2, '0')
            val month = parts[1].padStart(2, '0')
            val year = parts[2].take(4)
            "$year-$month-$day"
        } else {
            dateStr
        }
    } else {
        dateStr
    }

    return when (mode) {
        ChartTimeMode.DAILY -> if (normalized.length >= 10) normalized.take(10) else normalized
        ChartTimeMode.MONTHLY -> if (normalized.length >= 7) normalized.take(7) else normalized
        ChartTimeMode.YEARLY -> if (normalized.length >= 4) normalized.take(4) else normalized
    }
}

private fun shortDate(dateStr: String, mode: ChartTimeMode, preferredLang: String): String {
    if (dateStr.length < 4) return dateStr
    try {
        val parts = when {
            dateStr.contains("-") -> dateStr.split(" ")[0].split("-")
            dateStr.contains(".") -> dateStr.split(" ")[0].split(".")
            else -> emptyList()
        }
        if (parts.size >= 3) {
            val (year, month, day) = if (dateStr.contains("-")) {
                Triple(parts[0], parts[1], parts[2])
            } else {
                Triple(parts[2], parts[1], parts[0])
            }
            return when (mode) {
                ChartTimeMode.DAILY -> {
                    if (preferredLang.startsWith("de", ignoreCase = true)) {
                        "$day.$month."
                    } else if (preferredLang.startsWith(
                            "es",
                            ignoreCase = true
                        ) || preferredLang.startsWith("fr", ignoreCase = true)
                    ) {
                        "$day/$month/"
                    } else {
                        "$month/$day"
                    }
                }

                ChartTimeMode.MONTHLY -> "$year-$month"
                ChartTimeMode.YEARLY -> year
            }
        }
    } catch (_: Exception) {
    }
    return dateStr
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistChartScreen(
    checklist: ChecklistEntity,
    uiState: ChecklistUiState,
    onBackClick: () -> Unit,
    onLoadMoreClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBackClick)

    val systemDark = isSystemInDarkTheme()
    val isDarkTheme = when (uiState.userSettings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    var chartTimeMode by remember { mutableStateOf(ChartTimeMode.DAILY) }
    val preferredLang = uiState.userSettings.preferredLanguage

    val totalSubmissions = uiState.allSubmissions.filter { it.checklistid == checklist.id }
        .sortedBy { it.date }
    val submissions = totalSubmissions.takeLast(uiState.submissionLimit)
    val questions = uiState.questionsForSelectedChecklist.filter { it.checklistid == checklist.id }
    val numberQuestions = questions.filter { it.type.lowercase() == "number" }

    val submissionsPerPeriod = submissions.groupBy {
        formatSubmissionDate(it.date, chartTimeMode)
    }.mapValues { (_, subs) -> subs.size }.toList().sortedBy { it.first }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(checklist.title, maxLines = 1, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Time Mode Selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChartTimeMode.entries.forEach { mode ->
                        val label = when (mode) {
                            ChartTimeMode.DAILY -> stringResource(R.string.chart_mode_daily)
                            ChartTimeMode.MONTHLY -> stringResource(R.string.chart_mode_monthly)
                            ChartTimeMode.YEARLY -> stringResource(R.string.chart_mode_yearly)
                        }
                        FilterChip(
                            selected = chartTimeMode == mode,
                            onClick = { chartTimeMode = mode },
                            label = { Text(label) },
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                }
            }

            // 1. Submissions per period chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Analytics,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.submissions_per_day),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (submissionsPerPeriod.isEmpty()) {
                            Text(
                                text = "No submissions recorded yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            SubmissionsLineChart(
                                data = submissionsPerPeriod,
                                isDarkTheme = isDarkTheme,
                                chartTimeMode = chartTimeMode,
                                preferredLang = preferredLang
                            )
                        }
                    }
                }
            }

            // 2. Single-Series Numeric Value Area Chart (Automatically lists all available answer values, fixed width no scrolling)
            if (numberQuestions.isNotEmpty()) {
                val q1 = numberQuestions[0]

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Numeric Value: ${q1.title}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            val singleSeriesData = totalSubmissions.map { sub ->
                                val subAnswers =
                                    uiState.answers.filter { it.submissionid == sub.id }
                                val rawV1 = subAnswers.find { it.questionid == q1.id }?.value ?: ""
                                val v1 = rawV1.replace(',', '.').toDoubleOrNull() ?: 0.0
                                Pair(sub.date.ifBlank { "Sub #${sub.id}" }, v1)
                            }

                            if (singleSeriesData.isEmpty()) {
                                Text(
                                    text = "No submission answers recorded.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                SingleSeriesAreaChart(
                                    data = singleSeriesData,
                                    isDarkTheme = isDarkTheme,
                                    chartTimeMode = chartTimeMode,
                                    preferredLang = preferredLang
                                )
                            }
                        }
                    }
                }
            }

            // 3. Dual-Series Line Chart
            if (numberQuestions.size >= 2) {
                val q1 = numberQuestions[0]
                val q2 = numberQuestions[1]

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Numeric Values (Series 1 & 2)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• Series 1: ${q1.title}\n• Series 2: ${q2.title}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            val seriesData = submissions.groupBy {
                                formatSubmissionDate(it.date, chartTimeMode)
                            }.map { (period, subs) ->
                                val avgV1 = subs.mapNotNull { sub ->
                                    val subAnswers =
                                        uiState.answers.filter { it.submissionid == sub.id }
                                    subAnswers.find { it.questionid == q1.id }?.value?.replace(
                                        ',',
                                        '.'
                                    )?.toDoubleOrNull()
                                }.let { if (it.isNotEmpty()) it.average() else 0.0 }

                                val avgV2 = subs.mapNotNull { sub ->
                                    val subAnswers =
                                        uiState.answers.filter { it.submissionid == sub.id }
                                    subAnswers.find { it.questionid == q2.id }?.value?.replace(
                                        ',',
                                        '.'
                                    )?.toDoubleOrNull()
                                }.let { if (it.isNotEmpty()) it.average() else 0.0 }

                                Triple(period, avgV1, avgV2)
                            }.sortedBy { it.first }

                            if (seriesData.isEmpty()) {
                                Text(
                                    text = "No submission answers recorded.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                DualSeriesLineChart(
                                    data = seriesData,
                                    isDarkTheme = isDarkTheme,
                                    chartTimeMode = chartTimeMode,
                                    preferredLang = preferredLang
                                )
                            }
                        }
                    }
                }
            }

            if (totalSubmissions.size > submissions.size) {
                item {
                    Button(
                        onClick = onLoadMoreClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        Text(
                            text = stringResource(
                                R.string.load_more_submissions,
                                submissions.size,
                                totalSubmissions.size
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun formatValue(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else {
        java.text.DecimalFormat(
            "#.##",
            java.text.DecimalFormatSymbols.getInstance(java.util.Locale.US)
        ).format(value)
    }
}

private fun formatValue(value: Int): String = value.toString()

@Composable
fun SubmissionsLineChart(
    data: List<Pair<String, Int>>,
    isDarkTheme: Boolean,
    chartTimeMode: ChartTimeMode,
    preferredLang: String
) {
    if (data.isEmpty()) return
    val density = LocalDensity.current
    val labelSmallPx = with(density) { MaterialTheme.typography.labelSmall.fontSize.toPx() }

    val maxCount = data.maxOfOrNull { it.second }?.toFloat() ?: 1f
    val primaryColor = MaterialTheme.colorScheme.primary
    val scrollState = rememberScrollState()

    val itemWidth = 55.dp
    val totalWidth = maxOf(320.dp, itemWidth * data.size)
    val skipInterval = if (data.size > 8) data.size / 5 else 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        Box(modifier = Modifier.width(totalWidth)) {
            Column {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(8.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val horizontalPadding = 36f
                    val effectiveWidth = width - (horizontalPadding * 2f)
                    val stepX = if (data.size > 1) effectiveWidth / (data.size - 1) else 0f

                    val points = data.mapIndexed { index, pair ->
                        val x =
                            horizontalPadding + (if (data.size == 1) effectiveWidth / 2f else index * stepX)
                        val y = height - (pair.second.toFloat() / maxOf(maxCount, 1f)) * (height - 30f) - 20f
                        Offset(x, y)
                    }

                    if (points.size > 1) {
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 0 until points.size - 1) {
                                val p1 = points[i]
                                val p2 = points[i + 1]
                                cubicTo((p1.x + p2.x) / 2f, p1.y, (p1.x + p2.x) / 2f, p2.y, p2.x, p2.y)
                            }
                        }
                        drawPath(path, color = primaryColor, style = Stroke(width = 3.dp.toPx()))
                    }

                    val labelColor =
                        if (isDarkTheme) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                    val textPaint = android.graphics.Paint().apply {
                        color = labelColor
                        textSize = labelSmallPx
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }

                    points.forEachIndexed { index, point ->
                        drawCircle(color = primaryColor, radius = 5.dp.toPx(), center = point)
                        drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = point)

                        drawContext.canvas.nativeCanvas.drawText(
                            "${data[index].second}",
                            point.x,
                            point.y - 14f,
                            textPaint
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    data.forEachIndexed { index, pair ->
                        val shouldShowLabel =
                            index == 0 || index == data.size - 1 || index % skipInterval == 0
                        Column(
                            modifier = Modifier.width(55.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (shouldShowLabel) {
                                Text(
                                    text = shortDate(pair.first, chartTimeMode, preferredLang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SingleSeriesAreaChart(
    data: List<Pair<String, Double>>,
    isDarkTheme: Boolean,
    chartTimeMode: ChartTimeMode,
    preferredLang: String
) {
    if (data.isEmpty()) return
    val density = LocalDensity.current
    val labelSmallPx = with(density) { MaterialTheme.typography.labelSmall.fontSize.toPx() }
    val maxVal = maxOf(
        data.maxOfOrNull { it.second } ?: 1.0,
        1.0
    ).toFloat()

    val seriesColor = Color(0xFFD4AF37) // Gold
    val fillColor = seriesColor.copy(alpha = 0.25f)
    val gridColor =
        if (isDarkTheme) Color.DarkGray.copy(alpha = 0.4f) else Color.LightGray.copy(alpha = 0.6f)
    val skipInterval = if (data.size > 8) data.size / 5 else 1

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(8.dp)
        ) {
            val width = size.width
            val height = size.height
            val horizontalPadding = 36f
            val effectiveWidth = width - (horizontalPadding * 2f)
            val stepX = if (data.size > 1) effectiveWidth / (data.size - 1) else 0f
            val bottomBaseline = height - 25f

            val gridSteps = 4
            for (i in 0..gridSteps) {
                val y = 20f + (i * (bottomBaseline - 20f) / gridSteps)
                drawLine(
                    color = gridColor,
                    start = Offset(horizontalPadding, y),
                    end = Offset(width - horizontalPadding, y),
                    strokeWidth = 1f
                )
            }

            val points = data.mapIndexed { index, pair ->
                val x =
                    horizontalPadding + (if (data.size == 1) effectiveWidth / 2f else index * stepX)
                val y = bottomBaseline - (pair.second.toFloat() / maxVal) * (bottomBaseline - 30f)
                Offset(x, y)
            }

            if (points.isNotEmpty()) {
                val fillPath = Path().apply {
                    moveTo(points.first().x, bottomBaseline)
                    lineTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        cubicTo((p1.x + p2.x) / 2f, p1.y, (p1.x + p2.x) / 2f, p2.y, p2.x, p2.y)
                    }
                    lineTo(points.last().x, bottomBaseline)
                    close()
                }
                drawPath(fillPath, color = fillColor)
            }

            if (points.size > 1) {
                val linePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        cubicTo((p1.x + p2.x) / 2f, p1.y, (p1.x + p2.x) / 2f, p2.y, p2.x, p2.y)
                    }
                }
                drawPath(linePath, color = seriesColor, style = Stroke(width = 3.dp.toPx()))
            }

            val labelColor = if (isDarkTheme) android.graphics.Color.rgb(
                212,
                175,
                55
            ) else android.graphics.Color.BLACK
            val textPaint = android.graphics.Paint().apply {
                color = labelColor
                textSize = labelSmallPx
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }

            points.forEachIndexed { index, point ->
                drawCircle(color = seriesColor, radius = 4.dp.toPx(), center = point)
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = point)

                val shouldShowLabel =
                    index == 0 || index == data.size - 1 || index % skipInterval == 0
                if (shouldShowLabel) {
                    drawContext.canvas.nativeCanvas.drawText(
                        formatValue(data[index].second),
                        point.x,
                        point.y - 12f,
                        textPaint
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            data.forEachIndexed { index, pair ->
                val shouldShowLabel =
                    index == 0 || index == data.size - 1 || index % skipInterval == 0
                if (shouldShowLabel) {
                    Text(
                        text = shortDate(pair.first, chartTimeMode, preferredLang),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
fun DualSeriesLineChart(
    data: List<Triple<String, Double, Double>>,
    isDarkTheme: Boolean,
    chartTimeMode: ChartTimeMode,
    preferredLang: String
) {
    if (data.isEmpty()) return
    val density = LocalDensity.current
    val labelSmallPx = with(density) { MaterialTheme.typography.labelSmall.fontSize.toPx() }
    val maxVal = maxOf(
        data.maxOfOrNull { maxOf(it.second, it.third) } ?: 1.0,
        1.0
    ).toFloat()

    val series1Color = Color(0xFFD4AF37) // Gold
    val series2Color = Color(0xFF29B6F6) // Blue
    val scrollState = rememberScrollState()

    val itemWidth = 75.dp
    val totalWidth = maxOf(320.dp, itemWidth * data.size)
    val skipInterval = if (data.size > 8) data.size / 5 else 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        Box(modifier = Modifier.width(totalWidth)) {
            Column {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(8.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val horizontalPadding = 36f
                    val effectiveWidth = width - (horizontalPadding * 2f)
                    val stepX = if (data.size > 1) effectiveWidth / (data.size - 1) else 0f

                    val points1 = data.mapIndexed { index, triple ->
                        val x =
                            horizontalPadding + (if (data.size == 1) effectiveWidth / 2f else index * stepX)
                        val y = height - (triple.second.toFloat() / maxVal) * (height - 40f) - 20f
                        Offset(x, y)
                    }

                    val points2 = data.mapIndexed { index, triple ->
                        val x =
                            horizontalPadding + (if (data.size == 1) effectiveWidth / 2f else index * stepX)
                        val y = height - (triple.third.toFloat() / maxVal) * (height - 40f) - 20f
                        Offset(x, y)
                    }

                    if (points1.size > 1) {
                        val path1 = Path().apply {
                            moveTo(points1.first().x, points1.first().y)
                            for (i in 0 until points1.size - 1) {
                                val p1 = points1[i]
                                val p2 = points1[i + 1]
                                cubicTo((p1.x + p2.x) / 2f, p1.y, (p1.x + p2.x) / 2f, p2.y, p2.x, p2.y)
                            }
                        }
                        drawPath(path1, color = series1Color, style = Stroke(width = 3.dp.toPx()))
                    }

                    if (points2.size > 1) {
                        val path2 = Path().apply {
                            moveTo(points2.first().x, points2.first().y)
                            for (i in 0 until points2.size - 1) {
                                val p1 = points2[i]
                                val p2 = points2[i + 1]
                                cubicTo((p1.x + p2.x) / 2f, p1.y, (p1.x + p2.x) / 2f, p2.y, p2.x, p2.y)
                            }
                        }
                        drawPath(path2, color = series2Color, style = Stroke(width = 3.dp.toPx()))
                    }

                    val labelColor1 = if (isDarkTheme) android.graphics.Color.rgb(
                        212,
                        175,
                        55
                    ) else android.graphics.Color.BLACK
                    val labelColor2 = if (isDarkTheme) android.graphics.Color.rgb(
                        41,
                        182,
                        246
                    ) else android.graphics.Color.BLACK

                    val textPaint1 = android.graphics.Paint().apply {
                        color = labelColor1
                        textSize = labelSmallPx
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    val textPaint2 = android.graphics.Paint().apply {
                        color = labelColor2
                        textSize = labelSmallPx
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }

                    points1.forEachIndexed { index, point ->
                        drawCircle(color = series1Color, radius = 6.dp.toPx(), center = point)
                        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = point)

                        drawContext.canvas.nativeCanvas.drawText(
                            formatValue(data[index].second),
                            point.x,
                            point.y - 14f,
                            textPaint1
                        )
                    }
                    points2.forEachIndexed { index, point ->
                        drawCircle(color = series2Color, radius = 6.dp.toPx(), center = point)
                        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = point)

                        drawContext.canvas.nativeCanvas.drawText(
                            formatValue(data[index].third),
                            point.x,
                            point.y + 21f,
                            textPaint2
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    data.forEachIndexed { index, triple ->
                        val shouldShowLabel =
                            index == 0 || index == data.size - 1 || index % skipInterval == 0
                        Column(
                            modifier = Modifier.width(65.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (shouldShowLabel) {
                                Text(
                                    text = shortDate(triple.first, chartTimeMode, preferredLang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
