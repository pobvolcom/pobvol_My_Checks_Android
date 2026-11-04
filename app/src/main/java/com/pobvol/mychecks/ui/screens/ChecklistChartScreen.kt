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
import com.pobvol.mychecks.data.local.ChecklistSubmissionEntity
import com.pobvol.mychecks.data.repository.ThemeMode
import com.pobvol.mychecks.ui.ChecklistUiState
import com.pobvol.mychecks.util.DateUtil
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

enum class ChartTimeMode {
    ALL, ONE_MONTH, SIX_MONTHS
}

private fun filterSubmissions(
    submissions: List<ChecklistSubmissionEntity>,
    mode: ChartTimeMode,
    preferredLang: String = "en"
): List<ChecklistSubmissionEntity> {
    if (submissions.isEmpty() || mode == ChartTimeMode.ALL) return submissions
    val maxVal = submissions.maxOfOrNull { parseSubmissionDate(it.date, preferredLang) } ?: 0L
    if (maxVal == 0L) return submissions

    val year = (maxVal / 10000L).toInt()
    val month = ((maxVal / 100L) % 100L).toInt() - 1
    val day = (maxVal % 100L).toInt()

    val cal = java.util.Calendar.getInstance().apply {
        set(year, month, day, 0, 0, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    val maxDaysSinceEpoch = cal.timeInMillis / (24L * 60L * 60L * 1000L)

    val daysBack = when (mode) {
        ChartTimeMode.ALL -> 0L
        ChartTimeMode.ONE_MONTH -> 30L
        ChartTimeMode.SIX_MONTHS -> 180L
    }
    val minDaysSinceEpoch = maxDaysSinceEpoch - daysBack

    return submissions.filter { sub ->
        val subVal = parseSubmissionDate(sub.date, preferredLang)
        if (subVal == 0L) return@filter false
        val subYear = (subVal / 10000L).toInt()
        val subMonth = ((subVal / 100L) % 100L).toInt() - 1
        val subDay = (subVal % 100L).toInt()
        val subCal = java.util.Calendar.getInstance().apply {
            set(subYear, subMonth, subDay, 0, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val subDays = subCal.timeInMillis / (24L * 60L * 60L * 1000L)
        subDays in minDaysSinceEpoch..maxDaysSinceEpoch
    }
}

private fun formatSubmissionDate(dateStr: String, preferredLang: String = "en"): String {
    if (dateStr.isBlank()) return "Unknown"
    return DateUtil.toIsoDate(dateStr, preferredLang)
}

private fun shortDate(dateStr: String, preferredLang: String): String {
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
            return if (preferredLang.startsWith("de", ignoreCase = true)) {
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
    } catch (_: Exception) {
    }
    return dateStr
}

private fun parseSubmissionDate(dateStr: String, preferredLang: String = "en"): Long {
    if (dateStr.isBlank()) return 0L
    val iso = DateUtil.toIsoDate(dateStr, preferredLang)
    try {
        val parts = iso.split("-")
        if (parts.size >= 3) {
            val year = parts[0].toLongOrNull() ?: 0L
            val month = parts[1].toLongOrNull() ?: 0L
            val day = parts[2].toLongOrNull() ?: 0L
            return year * 10000L + month * 100L + day
        }
    } catch (_: Exception) {
    }
    return 0L
}

private fun parseDateTriple(
    dateStr: String,
    preferredLang: String = "en"
): Triple<String, String, String> {
    if (dateStr.length < 4) return Triple(dateStr, "", "")
    val iso = DateUtil.toIsoDate(dateStr, preferredLang)
    try {
        val parts = iso.split("-")
        if (parts.size >= 3) {
            val year = parts[0]
            val month = parts[1]
            val day = parts[2]
            return Triple(day, month, year)
        }
    } catch (_: Exception) {
    }
    return Triple(dateStr, "", "")
}

private fun calculateNiceScale(maxDataVal: Float, gridSteps: Int = 4): Pair<Float, Float> {
    val maxVal = maxOf(maxDataVal, 1f)
    val roughStep = maxVal / gridSteps.toFloat()
    val magnitude = 10.0.pow(floor(log10(roughStep.toDouble()))).toFloat()
    val residual = roughStep / magnitude
    val niceResidual = when {
        residual <= 1f -> 1f
        residual <= 2f -> 2f
        residual <= 2.5f -> 2.5f
        residual <= 5f -> 5f
        else -> 10f
    }
    val step = niceResidual * magnitude
    val niceMax = ceil(maxVal / step) * step
    return Pair(step, if (niceMax > 0f) niceMax else step * gridSteps)
}

private fun formatShortDateRange(dateStr: String, preferredLang: String): String {
    val iso = DateUtil.toIsoDate(dateStr, preferredLang)
    if (iso.length < 10) return dateStr
    try {
        val parts = iso.split("-")
        if (parts.size >= 3) {
            val year = parts[0]
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            val shortYear = year.takeLast(2)
            return if (preferredLang.startsWith("de", ignoreCase = true)) {
                "$day.$month.$shortYear"
            } else if (preferredLang.startsWith(
                    "es",
                    ignoreCase = true
                ) || preferredLang.startsWith("fr", ignoreCase = true)
            ) {
                "$day/$month/$shortYear"
            } else {
                "$month/$day/$shortYear"
            }
        }
    } catch (_: Exception) {
    }
    return dateStr
}

private fun formatMillisDateRange(millis: Long, preferredLang: String): String {
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
    val year = cal.get(java.util.Calendar.YEAR)
    val month = cal.get(java.util.Calendar.MONTH) + 1
    val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
    val shortYear = year.toString().takeLast(2)
    return when {
        preferredLang.startsWith("de", ignoreCase = true) -> "$day.$month.$shortYear"
        preferredLang.startsWith("es", ignoreCase = true) || preferredLang.startsWith(
            "fr",
            ignoreCase = true
        ) -> "$day/$month/$shortYear"

        else -> "$month/$day/$shortYear"
    }
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

    var chartTimeMode by remember { mutableStateOf(ChartTimeMode.ALL) }
    val preferredLang = uiState.userSettings.preferredLanguage

    val totalSubmissions = uiState.allSubmissions.filter { it.checklistid == checklist.id }
        .sortedWith(compareBy({ parseSubmissionDate(it.date, preferredLang) }, { it.timestamp }))
    val filteredSubmissions = remember(totalSubmissions, chartTimeMode, preferredLang) {
        filterSubmissions(totalSubmissions, chartTimeMode, preferredLang)
    }
    val submissions = filteredSubmissions.takeLast(uiState.submissionLimit)
    val questions = uiState.questionsForSelectedChecklist.filter { it.checklistid == checklist.id }
    val numberQuestions = questions.filter { it.type.lowercase() == "number" }

    val submissionsPerPeriod = submissions.groupBy {
        formatSubmissionDate(it.date, preferredLang)
    }.mapValues { (_, subs) -> subs.size }.toList().sortedBy { it.first }

    val dateRangeStr = if (filteredSubmissions.isNotEmpty() && chartTimeMode != ChartTimeMode.ALL) {
        val maxVal =
            totalSubmissions.maxOfOrNull { parseSubmissionDate(it.date, preferredLang) } ?: 0L
        val year = (maxVal / 10000L).toInt()
        val month = ((maxVal / 100L) % 100L).toInt() - 1
        val day = (maxVal % 100L).toInt()
        val cal = java.util.Calendar.getInstance().apply { set(year, month, day, 23, 59, 59) }
        val maxMillis = cal.timeInMillis

        val startDateMillis = when (chartTimeMode) {
            ChartTimeMode.ALL -> 0L
            ChartTimeMode.ONE_MONTH -> maxMillis - (30L * 24L * 60L * 60L * 1000L)
            ChartTimeMode.SIX_MONTHS -> maxMillis - (180L * 24L * 60L * 60L * 1000L)
        }
        val startDate = formatMillisDateRange(startDateMillis, preferredLang)
        val endDate = formatMillisDateRange(maxMillis, preferredLang)
        " ($startDate - $endDate)"
    } else {
        ""
    }

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
                            ChartTimeMode.ALL -> stringResource(R.string.chart_filter_all)
                            ChartTimeMode.ONE_MONTH -> stringResource(R.string.chart_filter_1m)
                            ChartTimeMode.SIX_MONTHS -> stringResource(R.string.chart_filter_6m)
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
                                text = stringResource(R.string.submissions_per_day) + dateRangeStr,
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

            // 2. Single-Series Numeric Value Area Chart (Automatically lists all available answer values, fixed width no scrolling, with Y-axis)
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
                                text = stringResource(R.string.numeric_value) + ": ${q1.title}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            val singleSeriesData = filteredSubmissions.map { sub ->
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

            // 3. Line Chart (Single-series if 1 numeric question, dual-series if 2+ numeric questions)
            if (numberQuestions.size == 1) {
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
                                text = stringResource(R.string.numeric_value) + ": ${q1.title}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            val singleSeriesLineData = submissions.groupBy {
                                formatSubmissionDate(it.date, preferredLang)
                            }.map { (period, subs) ->
                                val avgV1 = subs.mapNotNull { sub ->
                                    val subAnswers =
                                        uiState.answers.filter { it.submissionid == sub.id }
                                    subAnswers.find { it.questionid == q1.id }?.value?.replace(
                                        ',',
                                        '.'
                                    )?.toDoubleOrNull()
                                }.let { if (it.isNotEmpty()) it.average() else 0.0 }
                                Pair(period, avgV1)
                            }.sortedBy { it.first }

                            if (singleSeriesLineData.isEmpty()) {
                                Text(
                                    text = "No submission answers recorded.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                SingleSeriesLineChart(
                                    data = singleSeriesLineData,
                                    isDarkTheme = isDarkTheme,
                                    chartTimeMode = chartTimeMode,
                                    preferredLang = preferredLang
                                )
                            }
                        }
                    }
                }
            } else if (numberQuestions.size >= 2) {
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
                                text = stringResource(R.string.numeric_values_dual),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.series_1) + q1.title + "\n" + stringResource(
                                    R.string.series_2
                                ) + q2.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            val seriesData = submissions.groupBy {
                                formatSubmissionDate(it.date, preferredLang)
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
                        val y =
                            height - (pair.second.toFloat() / maxOf(
                                maxCount,
                                1f
                            )) * (height - 30f) - 20f
                        Offset(x, y)
                    }

                    if (points.size > 1) {
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 0 until points.size - 1) {
                                val p1 = points[i]
                                val p2 = points[i + 1]
                                cubicTo(
                                    (p1.x + p2.x) / 2f,
                                    p1.y,
                                    (p1.x + p2.x) / 2f,
                                    p2.y,
                                    p2.x,
                                    p2.y
                                )
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
                        val (day, month, year) = parseDateTriple(pair.first, preferredLang)
                        Column(
                            modifier = Modifier.width(55.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                            )
                            Text(
                                text = month,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                            Text(
                                text = year,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                            )
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
    val maxValData = maxOf(
        data.maxOfOrNull { it.second } ?: 1.0,
        1.0
    ).toFloat()

    val gridSteps = 4
    val (step, niceMax) = calculateNiceScale(maxValData, gridSteps)

    val seriesColor = Color(0xFFD4AF37) // Gold
    val fillColor = seriesColor.copy(alpha = 0.25f)
    val gridColor =
        if (isDarkTheme) Color.DarkGray.copy(alpha = 0.4f) else Color.LightGray.copy(alpha = 0.6f)

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
            val leftPadding = 15f
            val rightPadding = 65f
            val effectiveWidth = width - (leftPadding + rightPadding)
            val stepX = if (data.size > 1) effectiveWidth / (data.size - 1) else 0f
            val bottomBaseline = height - 25f

            val yLabelPaint = android.graphics.Paint().apply {
                color =
                    if (isDarkTheme) android.graphics.Color.LTGRAY else android.graphics.Color.DKGRAY
                textSize = labelSmallPx * 0.85f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.LEFT
            }

            // Draw Y-axis grid lines & labels on the right
            for (i in 0..gridSteps) {
                val ratio = i.toFloat() / gridSteps
                val valAtStep = (i * step).toLong()
                val y = bottomBaseline - (ratio * (bottomBaseline - 30f))

                // Grid line
                drawLine(
                    color = gridColor,
                    start = Offset(leftPadding, y),
                    end = Offset(width - rightPadding, y),
                    strokeWidth = 1f
                )

                // Y-axis label on the right
                drawContext.canvas.nativeCanvas.drawText(
                    valAtStep.toString(),
                    width - rightPadding + 8f,
                    y + 4f,
                    yLabelPaint
                )
            }

            val points = data.mapIndexed { index, pair ->
                val x = leftPadding + (if (data.size == 1) effectiveWidth / 2f else index * stepX)
                val y = bottomBaseline - (pair.second.toFloat() / niceMax) * (bottomBaseline - 30f)
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

            points.forEach { point ->
                drawCircle(color = seriesColor, radius = 4.dp.toPx(), center = point)
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = point)
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
                                cubicTo(
                                    (p1.x + p2.x) / 2f,
                                    p1.y,
                                    (p1.x + p2.x) / 2f,
                                    p2.y,
                                    p2.x,
                                    p2.y
                                )
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
                                cubicTo(
                                    (p1.x + p2.x) / 2f,
                                    p1.y,
                                    (p1.x + p2.x) / 2f,
                                    p2.y,
                                    p2.x,
                                    p2.y
                                )
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
                        drawCircle(color = Color.White, radius = 2.dp.toPx(), center = point)

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
                        val shouldShowLabel = true
                        Column(
                            modifier = Modifier.width(65.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (shouldShowLabel) {
                                Text(
                                    text = shortDate(triple.first, preferredLang),
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
fun SingleSeriesLineChart(
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
    val scrollState = rememberScrollState()

    val itemWidth = 75.dp
    val totalWidth = maxOf(320.dp, itemWidth * data.size)

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

                    val points = data.mapIndexed { index, pair ->
                        val x =
                            horizontalPadding + (if (data.size == 1) effectiveWidth / 2f else index * stepX)
                        val y = height - (pair.second.toFloat() / maxVal) * (height - 40f) - 20f
                        Offset(x, y)
                    }

                    if (points.size > 1) {
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 0 until points.size - 1) {
                                val p1 = points[i]
                                val p2 = points[i + 1]
                                cubicTo(
                                    (p1.x + p2.x) / 2f,
                                    p1.y,
                                    (p1.x + p2.x) / 2f,
                                    p2.y,
                                    p2.x,
                                    p2.y
                                )
                            }
                        }
                        drawPath(path, color = seriesColor, style = Stroke(width = 3.dp.toPx()))
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
                        drawCircle(color = seriesColor, radius = 6.dp.toPx(), center = point)
                        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = point)

                        drawContext.canvas.nativeCanvas.drawText(
                            formatValue(data[index].second),
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
                        val shouldShowLabel = true
                        Column(
                            modifier = Modifier.width(65.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (shouldShowLabel) {
                                Text(
                                    text = shortDate(pair.first, preferredLang),
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
