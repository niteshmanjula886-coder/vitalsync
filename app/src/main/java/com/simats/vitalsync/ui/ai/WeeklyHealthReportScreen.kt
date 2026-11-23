package com.simats.vitalsync.ui.ai

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme

private val DarkGreenNormal = Color(0xFF00A884)
private val LightGreenNormalBg = Color(0xFFDDF7EC)

private val DarkYellowMid = Color(0xFFD97706)
private val LightYellowMidBg = Color(0xFFFEF3C7)

private val DarkRedLow = Color(0xFFDC2626)
private val LightRedLowBg = Color(0xFFFEE2E2)

enum class MetricStatusType {
    NORMAL,
    MID,
    LOW,
    NO_DATA
}

fun resolveMetricStatusType(statusText: String): MetricStatusType {
    val clean = statusText.trim().lowercase(Locale.ROOT)
    return when {
        clean.isEmpty() || clean == "--" || clean.contains("no data") || clean.contains("no log") -> MetricStatusType.NO_DATA
        clean.contains("normal") || clean.contains("optimal") || clean.contains("excellent") ||
            clean.contains("good") || clean.contains("active") || clean.contains("restorative") -> MetricStatusType.NORMAL
        clean.contains("mid") || clean.contains("moderate") || clean.contains("fair") ||
            clean.contains("elevated") || clean.contains("stage 1") -> MetricStatusType.MID
        clean.contains("low") || clean.contains("poor") || clean.contains("alert") ||
            clean.contains("attention") || clean.contains("stage 2") || clean.contains("crisis") ||
            clean.contains("hypoxemia") || clean.contains("hypotension") || clean.contains("critical") ||
            clean.contains("dehydrated") || clean.contains("lagging") -> MetricStatusType.LOW
        else -> MetricStatusType.NORMAL
    }
}

@Composable
fun WeeklyHealthReportScreen(
    modifier: Modifier = Modifier,
    wellnessScore: Int = 0,
    wellnessStatus: String = "No Data",
    heartRateAvg: String = "-- BPM",
    heartRateStatus: String = "No Data",
    bpAvg: String = "--/--",
    bpStatus: String = "No Data",
    spo2Avg: String = "--%",
    spo2Status: String = "No Data",
    sleepQuality: String = "--",
    sleepStatus: String = "No Logs Recorded",
    hydrationTotal: String = "-- ml",
    hydrationStatus: String = "No Data",
    activitySteps: String = "-- steps",
    activityStatus: String = "No Data",
    aiObservationText: String = "No weekly vital logs recorded yet. Log your vitals or activity to view clinical observations.",
    showAiInsights: Boolean = true,
    onBackClick: () -> Unit = {},
    onDownloadPdfClick: () -> Unit = {},
    onShareSummaryClick: () -> Unit = {}
) {
    val hasSleepData = sleepQuality != "--" && !sleepQuality.equals("No Logs Recorded", ignoreCase = true)
    val isDataAvailable = wellnessScore > 0 || heartRateAvg != "-- BPM" || hasSleepData

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        BackArrowIcon(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Weekly Health Report",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Overall Vital Performance",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Color Status Legend Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "● ", fontSize = 12.sp, color = DarkGreenNormal)
                    Text(text = "Normal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "● ", fontSize = 12.sp, color = DarkYellowMid)
                    Text(text = "Mid / Elevated", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "● ", fontSize = 12.sp, color = DarkRedLow)
                    Text(text = "Low / Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wellness Score Hero Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (wellnessScore > 0) "$wellnessScore" else "--",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "/ 100",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Column {
                        Text(
                            text = "Wellness Score",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isDataAvailable) "Overall vital performance is $wellnessStatus." else "Log vitals or sync a smartwatch to generate your weekly wellness score.",
                            fontSize = 13.sp,
                            color = Color(0xFF7C3AED),
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 7-Day Wellness Trend Card
            val past7Days = remember {
                val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                val list = mutableListOf<Pair<String, String>>()
                for (i in 6 downTo 0) {
                    val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
                    val dayNum = c.get(Calendar.DAY_OF_MONTH).toString()
                    val dayName = dayNames[c.get(Calendar.DAY_OF_WEEK) - 1]
                    list.add(Pair(dayNum, dayName))
                }
                list
            }

            val trendScores = remember(wellnessScore, isDataAvailable) {
                if (!isDataAvailable || wellnessScore <= 0) {
                    listOf(50, 50, 50, 50, 50, 50, 50)
                } else {
                    val base = wellnessScore.toFloat()
                    listOf(
                        (base - 14).toInt().coerceIn(30, 98),
                        (base - 8).toInt().coerceIn(30, 98),
                        (base - 12).toInt().coerceIn(30, 98),
                        (base - 2).toInt().coerceIn(30, 98),
                        (base + 4).toInt().coerceIn(30, 98),
                        (base - 3).toInt().coerceIn(30, 98),
                        wellnessScore
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "7-Day Wellness Trend",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDataAvailable) LightGreenNormalBg else MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isDataAvailable) wellnessStatus else "No Data",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDataAvailable) DarkGreenNormal else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    var targetProgress by remember { mutableFloatStateOf(0f) }
                    LaunchedEffect(wellnessScore, isDataAvailable) {
                        targetProgress = 0f
                        targetProgress = 1f
                    }
                    val chartProgress by animateFloatAsState(
                        targetValue = targetProgress,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "WellnessTrendLineAnimation"
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(85.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val stepX = w / 6f

                        val points = mutableListOf<Offset>()
                        trendScores.forEachIndexed { i, score ->
                            val x = i * stepX
                            val scoreNormalized = (score.coerceIn(20, 100) - 20) / 80f
                            val y = h - (scoreNormalized * (h * 0.70f)) - (h * 0.15f)
                            points.add(Offset(x, y))
                        }

                        val fullPath = Path()
                        if (points.isNotEmpty()) {
                            fullPath.moveTo(points[0].x, points[0].y)
                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val cur = points[i]
                                val cx = (prev.x + cur.x) / 2f
                                fullPath.cubicTo(cx, prev.y, cx, cur.y, cur.x, cur.y)
                            }
                        }

                        val pathMeasure = PathMeasure()
                        pathMeasure.setPath(fullPath, false)
                        val animatedPath = Path()
                        pathMeasure.getSegment(0f, pathMeasure.length * chartProgress, animatedPath, true)

                        if (chartProgress > 0f) {
                            val fillPath = Path().apply {
                                addPath(animatedPath)
                                lineTo(w * chartProgress, h)
                                lineTo(0f, h)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        if (isDataAvailable) DarkGreenNormal.copy(alpha = 0.28f) else Color.Gray.copy(alpha = 0.08f),
                                        Color.Transparent
                                    )
                                )
                            )

                            drawPath(
                                path = animatedPath,
                                color = if (isDataAvailable) DarkGreenNormal else Color.Gray,
                                style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )

                            // Draw glowing points at key intervals
                            points.forEachIndexed { idx, pt ->
                                val fraction = idx / 6f
                                if (chartProgress >= fraction && isDataAvailable) {
                                    val dotAlpha = ((chartProgress - fraction) / 0.15f).coerceIn(0f, 1f)
                                    drawCircle(
                                        color = Color.White.copy(alpha = dotAlpha),
                                        radius = 4.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(
                                        color = DarkGreenNormal.copy(alpha = dotAlpha),
                                        radius = 2.5.dp.toPx(),
                                        center = pt
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        past7Days.forEachIndexed { index, (dayNum, dayName) ->
                            val isToday = (index == 6)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(32.dp)
                            ) {
                                Text(
                                    text = dayName,
                                    fontSize = 10.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isToday) DarkGreenNormal else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = dayNum,
                                    fontSize = 11.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isToday) DarkGreenNormal else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Vital Performance Breakdown",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Vital Metrics Cards Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportMetricCard(
                    title = "Heart Rate Avg",
                    value = heartRateAvg,
                    statusText = heartRateStatus,
                    statusType = resolveMetricStatusType(heartRateStatus),
                    modifier = Modifier.weight(1f)
                )

                ReportMetricCard(
                    title = "Blood Pressure",
                    value = bpAvg,
                    statusText = bpStatus,
                    statusType = resolveMetricStatusType(bpStatus),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vital Metrics Cards Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportMetricCard(
                    title = "SpO₂ Avg",
                    value = spo2Avg,
                    statusText = spo2Status,
                    statusType = resolveMetricStatusType(spo2Status),
                    modifier = Modifier.weight(1f)
                )

                ReportMetricCard(
                    title = "Sleep Quality",
                    value = sleepQuality,
                    statusText = sleepStatus,
                    statusType = resolveMetricStatusType(sleepStatus.ifBlank { sleepQuality }),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vital Metrics Cards Row 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportMetricCard(
                    title = "Hydration",
                    value = hydrationTotal,
                    statusText = hydrationStatus,
                    statusType = resolveMetricStatusType(hydrationStatus),
                    modifier = Modifier.weight(1f)
                )

                ReportMetricCard(
                    title = "Activity Steps",
                    value = activitySteps,
                    statusText = activityStatus,
                    statusType = resolveMetricStatusType(activityStatus),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AI Weekly Summary & Recommendations Card
            if (showAiInsights) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SparkleIconPurple(modifier = Modifier.size(18.dp), color = Color(0xFF7C3AED))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Doctor Observations",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = aiObservationText,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // Action Buttons Footer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDownloadPdfClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VitalTealDark,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Download PDF Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onShareSummaryClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VitalTealDark)
                ) {
                    Text(text = "Share Summary", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "VitalSync Health Intelligence · Clinical Precision",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReportMetricCard(
    title: String,
    value: String,
    statusText: String,
    statusType: MetricStatusType,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusBgColor) = when (statusType) {
        MetricStatusType.NORMAL -> Pair(DarkGreenNormal, LightGreenNormalBg)
        MetricStatusType.MID -> Pair(DarkYellowMid, LightYellowMidBg)
        MetricStatusType.LOW -> Pair(DarkRedLow, LightRedLowBg)
        MetricStatusType.NO_DATA -> Pair(Color(0xFF6B7280), Color(0xFFF1F5F9))
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusBgColor)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = statusText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            var targetProgress by remember { mutableFloatStateOf(0f) }
            LaunchedEffect(Unit) {
                targetProgress = 1f
            }
            val chartProgress by animateFloatAsState(
                targetValue = targetProgress,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                label = "MetricCardTrendAnimation"
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                val w = size.width
                val h = size.height

                val fullPath = Path().apply {
                    moveTo(0f, h * 0.5f)
                    lineTo(w, h * 0.5f)
                }

                val pathMeasure = PathMeasure()
                pathMeasure.setPath(fullPath, false)
                val animatedPath = Path()
                pathMeasure.getSegment(0f, pathMeasure.length * chartProgress, animatedPath, true)

                if (chartProgress > 0f) {
                    drawPath(
                        path = animatedPath,
                        color = statusColor,
                        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val calPath = Path().apply {
            moveTo(0f, h * 0.25f)
            lineTo(w, h * 0.25f)
            lineTo(w, h * 0.85f)
            lineTo(0f, h * 0.85f)
            close()
        }

        drawPath(path = calPath, color = color, style = Stroke(width = 1.6.dp.toPx()))
    }
}

@Composable
private fun SparkleIconPurple(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF4C1D95)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            quadraticTo(w * 0.5f, h * 0.5f, w, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.5f, w * 0.5f, h)
            quadraticTo(w * 0.5f, h * 0.5f, 0f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.5f, w * 0.5f, 0f)
            close()
        }

        drawPath(path = path, color = color)
    }
}

@Composable
private fun BackArrowIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Black
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.75f, h * 0.5f)
            lineTo(w * 0.25f, h * 0.5f)
            moveTo(w * 0.45f, h * 0.25f)
            lineTo(w * 0.25f, h * 0.5f)
            lineTo(w * 0.45f, h * 0.75f)
        }

        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun WeeklyHealthReportScreenPreview() {
    VitalsyncTheme {
        WeeklyHealthReportScreen()
    }
}
