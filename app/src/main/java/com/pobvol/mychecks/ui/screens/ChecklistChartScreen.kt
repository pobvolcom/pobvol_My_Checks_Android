package com.pobvol.mychecks.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pobvol.mychecks.R
import com.pobvol.mychecks.data.local.ChecklistEntity
import com.pobvol.mychecks.ui.ChecklistUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistChartScreen(
    checklist: ChecklistEntity,
    uiState: ChecklistUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBackClick)

    val submissions = uiState.submissions.filter { it.checklistid == checklist.id }
        .sortedBy { it.date }
    val questions = uiState.questionsForSelectedChecklist.filter { it.checklistid == checklist.id }
    val numberQuestions = questions.filter { it.type.lowercase() == "number" }

    val submissionsPerDay = submissions.groupingBy {
        if (it.date.isNotBlank()) it.date.take(10) else "Unknown"
    }.eachCount().toList().sortedBy { it.first }

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
            // 1. Submissions per day chart
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
                        if (submissionsPerDay.isEmpty()) {
                            Text(
                                text = "No submissions recorded yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            SubmissionsLineChart(data = submissionsPerDay)
                        }
                    }
                }
            }

            // 2. If 2 number questions exist, plot Series 1 and Series 2 values per submission
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

                            val seriesData = submissions.map { sub ->
                                val subAnswers =
                                    uiState.answers.filter { it.submissionid == sub.id }
                                val v1 =
                                    subAnswers.find { it.questionid == q1.id }?.value?.toDoubleOrNull()
                                        ?: 0.0
                                val v2 =
                                    subAnswers.find { it.questionid == q2.id }?.value?.toDoubleOrNull()
                                        ?: 0.0
                                Triple(sub.date.ifBlank { "Sub #${sub.id}" }, v1, v2)
                            }

                            if (seriesData.isEmpty()) {
                                Text(
                                    text = "No submission answers recorded.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                DualSeriesLineChart(data = seriesData)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubmissionsLineChart(data: List<Pair<String, Int>>) {
    val maxCount = data.maxOfOrNull { it.second }?.toFloat() ?: 1f
    val primaryColor = MaterialTheme.colorScheme.primary
    val scrollState = rememberScrollState()

    val itemWidth = 60.dp
    val totalWidth = maxOf(300.dp, itemWidth * data.size)

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
                    val stepX = if (data.size > 1) width / (data.size - 1) else width

                    val points = data.mapIndexed { index, pair ->
                        val x = index * stepX + (if (data.size == 1) width / 2f else 0f)
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

                    points.forEach { point ->
                        drawCircle(color = primaryColor, radius = 6.dp.toPx(), center = point)
                        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = point)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    data.forEach { pair ->
                        Column(
                            modifier = Modifier.width(55.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${pair.second}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = pair.first,
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

@Composable
fun DualSeriesLineChart(data: List<Triple<String, Double, Double>>) {
    val maxVal = maxOf(
        data.maxOfOrNull { maxOf(it.second, it.third) } ?: 1.0,
        1.0
    ).toFloat()

    val series1Color = Color(0xFFFFA726) // Orange
    val series2Color = Color(0xFF29B6F6) // Blue
    val scrollState = rememberScrollState()

    val itemWidth = 70.dp
    val totalWidth = maxOf(300.dp, itemWidth * data.size)

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
                    val stepX = if (data.size > 1) width / (data.size - 1) else width

                    val points1 = data.mapIndexed { index, triple ->
                        val x = index * stepX + (if (data.size == 1) width / 2f else 0f)
                        val y = height - (triple.second.toFloat() / maxVal) * (height - 40f) - 20f
                        Offset(x, y)
                    }

                    val points2 = data.mapIndexed { index, triple ->
                        val x = index * stepX + (if (data.size == 1) width / 2f else 0f)
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

                    points1.forEach { point ->
                        drawCircle(color = series1Color, radius = 6.dp.toPx(), center = point)
                        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = point)
                    }
                    points2.forEach { point ->
                        drawCircle(color = series2Color, radius = 6.dp.toPx(), center = point)
                        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = point)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    data.forEach { triple ->
                        Column(
                            modifier = Modifier.width(65.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "S1: ${triple.second}",
                                style = MaterialTheme.typography.labelSmall,
                                color = series1Color,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "S2: ${triple.third}",
                                style = MaterialTheme.typography.labelSmall,
                                color = series2Color,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = triple.first,
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
