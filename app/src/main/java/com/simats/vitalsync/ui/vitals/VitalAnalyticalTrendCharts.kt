package com.simats.vitalsync.ui.vitals

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.data.model.vitals.VitalReadingItem
import com.simats.vitalsync.ui.theme.CarbonDarkBackground
import com.simats.vitalsync.ui.theme.CarbonDarkBorder
import com.simats.vitalsync.ui.theme.CarbonDarkSurface
import com.simats.vitalsync.ui.theme.CarbonDarkSurfaceVariant
import com.simats.vitalsync.ui.theme.CarbonDarkTextPrimary
import com.simats.vitalsync.ui.theme.CarbonDarkTextSecondary
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.max

/**
 * Clinical wellness range thresholds & color palettes.
 * - Optimal / Normal: Emerald Green (#10B981)
 * - Elevated / Borderline: Amber (#F59E0B)
 * - Critical / High: Red (#EF4444)
 */
object VitalClinicalRanges {
    val COLOR_OPTIMAL = Color(0xFF10B981)
    val COLOR_ELEVATED = Color(0xFFF59E0B)
    val COLOR_CRITICAL = Color(0xFFEF4444)

    // Heart Rate (BPM)
    // Optimal: 60-100 bpm
    // Elevated / Borderline: 50-59 bpm or 101-120 bpm
    // Critical: < 50 bpm or > 120 bpm
    fun getHrColor(bpm: Float): Color = when {
        bpm in 60f..100f -> COLOR_OPTIMAL
        (bpm in 50f..59.99f) || (bpm in 100.01f..120f) -> COLOR_ELEVATED
        else -> COLOR_CRITICAL
    }

    fun getHrLabel(bpm: Float): String = when {
        bpm in 60f..100f -> "Optimal"
        bpm in 50f..59.99f -> "Low Borderline"
        bpm in 100.01f..120f -> "Elevated"
        bpm < 50f -> "Bradycardia"
        else -> "High"
    }

    // SpO2 (%)
    // Optimal: 95-100%
    // Moderate / Mildly Low: 91-94%
    // Critical: <= 90%
    fun getSpo2Color(pct: Float): Color = when {
        pct >= 95f -> COLOR_OPTIMAL
        pct >= 91f -> COLOR_ELEVATED
        else -> COLOR_CRITICAL
    }

    fun getSpo2Label(pct: Float): String = when {
        pct >= 95f -> "Optimal"
        pct >= 91f -> "Mildly Low"
        else -> "Critical"
    }

    // Blood Pressure (mmHg)
    // Normal: Sys < 120 and Dia < 80
    // Elevated / Stage 1: Sys 120-139 or Dia 80-89
    // High / Stage 2+: Sys >= 140 or Dia >= 90
    fun getBpColor(sys: Float, dia: Float): Color = when {
        sys < 120f && dia < 80f -> COLOR_OPTIMAL
        sys <= 139f && dia <= 89f -> COLOR_ELEVATED
        else -> COLOR_CRITICAL
    }

    fun getBpLabel(sys: Float, dia: Float): String = when {
        sys < 120f && dia < 80f -> "Normal"
        sys < 130f && dia < 80f -> "Elevated"
        sys <= 139f || dia <= 89f -> "Stage 1"
        else -> "Stage 2+"
    }
}

data class TrendPoint(
    val id: Int,
    val timeLabel: String,
    val fullDate: String,
    val value1: Float,
    val value2: Float? = null,
    val color: Color,
    val status: String,
    val source: String
)

data class LegendItem(
    val label: String,
    val color: Color
)

/**
 * Extracts chronological trend points for a given reading type from raw vital items.
 */
fun extractTrendPoints(
    readings: List<VitalReadingItem>,
    type: String,
    limit: Int = 10
): List<TrendPoint> {
    val filtered = readings
        .filter { it.readingType.equals(type, ignoreCase = true) && (it.valueNumeric ?: 0f) > 0f }
        .take(limit)
        .reversed() // Oldest to newest for left-to-right chronological trend

    val parseFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    val timeFmt = SimpleDateFormat("h:mm a", Locale.US)
    val dateFmt = SimpleDateFormat("MMM d", Locale.US)

    return filtered.mapIndexed { index, item ->
        val v1 = item.valueNumeric ?: 0f
        val v2 = item.valueSecondary
        val parsedDate = try {
            item.recordedAt?.let { parseFmt.parse(it) }
        } catch (_: Exception) {
            null
        }
        val tLabel = if (parsedDate != null) timeFmt.format(parsedDate) else "Pt ${index + 1}"
        val fDate = if (parsedDate != null) dateFmt.format(parsedDate) else ""

        val (c, s) = when (type.lowercase(Locale.US)) {
            "heart_rate" -> Pair(VitalClinicalRanges.getHrColor(v1), VitalClinicalRanges.getHrLabel(v1))
            "spo2" -> Pair(VitalClinicalRanges.getSpo2Color(v1), VitalClinicalRanges.getSpo2Label(v1))
            "blood_pressure" -> {
                val dia = v2 ?: 80f
                Pair(VitalClinicalRanges.getBpColor(v1, dia), VitalClinicalRanges.getBpLabel(v1, dia))
            }
            else -> Pair(VitalClinicalRanges.COLOR_OPTIMAL, "Normal")
        }

        TrendPoint(
            id = item.id,
            timeLabel = tLabel,
            fullDate = fDate,
            value1 = v1,
            value2 = v2,
            color = c,
            status = s,
            source = item.source ?: "Device"
        )
    }
}

/**
 * Computes at most 4 evenly spaced timestamps for the X-axis:
 * first, ~1/3, ~2/3, and latest reading.
 */
fun computeDisplayTimestamps(points: List<TrendPoint>): List<String> {
    if (points.isEmpty()) return emptyList()
    if (points.size <= 4) return points.map { it.timeLabel }
    val first = points.first().timeLabel
    val oneThird = points[points.size / 3].timeLabel
    val twoThirds = points[(points.size * 2) / 3].timeLabel
    val last = points.last().timeLabel
    return listOf(first, oneThird, twoThirds, last)
}

/**
 * Shared FlowRow clinical range legend.
 * Uses FlowRow with spacedBy(12.dp) and spacedBy(6.dp) where each dot + label is in a Row with softWrap = false.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClinicalRangeFlowLegend(
    items: List<LegendItem>,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF8FAFC))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(item.color)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = item.color,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

/**
 * Heart Rate Trend Chart:
 * Clean dark-mode Canvas line/area chart with dynamic clinical status colors:
 * - Optimal (60-100 bpm): #10B981
 * - Elevated / Low-Borderline (50-59 bpm or 101-120 bpm): #F59E0B
 * - Critical (<50 or >120 bpm): #EF4444
 */
@Composable
fun HeartRateTrendChart(
    readings: List<VitalReadingItem>,
    latestBpm: Int? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground
    val points = remember(readings) {
        val extracted = extractTrendPoints(readings, "heart_rate", limit = 10)
        if (extracted.isEmpty() && latestBpm != null && latestBpm > 0) {
            listOf(
                TrendPoint(
                    id = 1,
                    timeLabel = "Now",
                    fullDate = "Today",
                    value1 = latestBpm.toFloat(),
                    color = VitalClinicalRanges.getHrColor(latestBpm.toFloat()),
                    status = VitalClinicalRanges.getHrLabel(latestBpm.toFloat()),
                    source = "Latest"
                )
            )
        } else extracted
    }

    if (points.isEmpty()) {
        VitalChartEmptyCard(
            title = "Heart Rate Trend",
            unit = "BPM",
            message = "No heart rate trend logged yet. Measure or sync your pulse to view baseline analytics.",
            icon = Icons.Rounded.Favorite,
            isDark = isDark,
            modifier = modifier
        )
        return
    }

    var selectedIndex by remember(points.size) { mutableIntStateOf(points.lastIndex) }
    val safeIndex = selectedIndex.coerceIn(0, points.lastIndex)
    val activePoint = points[safeIndex]
    val displayTimestamps = remember(points) { computeDisplayTimestamps(points) }

    // Smooth horizontal line and area reveal animation
    val revealProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        revealProgress.snapTo(0f)
        revealProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    // Subtle pulsing halo animation for latest reading node
    val infiniteTransition = rememberInfiniteTransition(label = "hrPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hrPulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hrPulseAlpha"
    )

    // Smooth value & status transitions
    val animatedColor by animateColorAsState(
        targetValue = activePoint.color,
        animationSpec = tween(durationMillis = 350),
        label = "hrColor"
    )
    val animatedValue by animateIntAsState(
        targetValue = activePoint.value1.toInt(),
        animationSpec = tween(durationMillis = 350),
        label = "hrValue"
    )

    // Reusable Path and Paint instances for allocation-free Canvas animations
    val linePath = remember { Path() }
    val fillPath = remember { Path() }
    val axisTextPaint = remember(isDark) {
        android.graphics.Paint().apply {
            color = if (isDark) android.graphics.Color.parseColor("#8B949E") else android.graphics.Color.parseColor("#94A3B8")
            isAntiAlias = true
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) CarbonDarkSurface else Color.White
        ),
        border = BorderStroke(1.dp, if (isDark) CarbonDarkBorder else Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: title/timestamp on left with weight(1f), reading & status on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(animatedColor.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = animatedColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Heart Rate Trend",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${activePoint.timeLabel} ${activePoint.fullDate}".trim(),
                            fontSize = 11.sp,
                            color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Compact reading value + status pill
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$animatedValue",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827),
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "BPM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B),
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(animatedColor.copy(alpha = 0.18f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = activePoint.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = animatedColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Chart
            val minVal = max(40f, (points.minOf { it.value1 } - 15f))
            val maxVal = max(minVal + 40f, (points.maxOf { it.value1 } + 15f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(points) {
                            detectTapGestures { tapOffset ->
                                val count = points.size
                                if (count > 1) {
                                    val stepX = (size.width - 56.dp.toPx()) / (count - 1)
                                    val leftPad = 40.dp.toPx()
                                    val clicked = ((tapOffset.x - leftPad + stepX / 2) / stepX)
                                        .toInt()
                                        .coerceIn(0, count - 1)
                                    selectedIndex = clicked
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val leftPad = 40.dp.toPx()
                    val rightPad = 16.dp.toPx()
                    val topPad = 12.dp.toPx()
                    val bottomPad = 10.dp.toPx()
                    val plotW = w - leftPad - rightPad
                    val plotH = h - topPad - bottomPad

                    val gridColor = if (isDark) Color(0xFF262E38) else Color(0xFFE2E8F0)

                    val yLevels = listOf(
                        minVal + (maxVal - minVal) * 0.9f,
                        minVal + (maxVal - minVal) * 0.5f,
                        minVal + (maxVal - minVal) * 0.1f
                    )
                    axisTextPaint.textSize = 10.sp.toPx()

                    yLevels.forEach { yLvl ->
                        val yPos = topPad + plotH * (1f - (yLvl - minVal) / (maxVal - minVal))
                        drawLine(
                            color = gridColor,
                            start = Offset(leftPad, yPos),
                            end = Offset(w - rightPad, yPos),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            "${yLvl.toInt()}",
                            6.dp.toPx(),
                            yPos + 4.dp.toPx(),
                            axisTextPaint
                        )
                    }

                    // Baseline
                    drawLine(
                        color = gridColor,
                        start = Offset(leftPad, topPad + plotH),
                        end = Offset(w - rightPad, topPad + plotH),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    val count = points.size
                    val coords = points.mapIndexed { idx, pt ->
                        val x = if (count == 1) leftPad + plotW / 2 else leftPad + (plotW / (count - 1)) * idx
                        val normY = ((pt.value1 - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                        val y = topPad + plotH * (1f - normY)
                        Offset(x, y)
                    }

                    val revealX = leftPad + plotW * revealProgress.value

                    if (coords.size > 1) {
                        linePath.reset()
                        linePath.moveTo(coords.first().x, coords.first().y)
                        for (i in 1 until coords.size) {
                            val prev = coords[i - 1]
                            val cur = coords[i]
                            val midX = (prev.x + cur.x) / 2f
                            linePath.cubicTo(midX, prev.y, midX, cur.y, cur.x, cur.y)
                        }

                        fillPath.reset()
                        fillPath.addPath(linePath)
                        fillPath.lineTo(coords.last().x, topPad + plotH)
                        fillPath.lineTo(coords.first().x, topPad + plotH)
                        fillPath.close()

                        clipRect(right = revealX) {
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        animatedColor.copy(alpha = 0.28f),
                                        Color.Transparent
                                    ),
                                    startY = topPad,
                                    endY = topPad + plotH
                                )
                            )

                            drawPath(
                                path = linePath,
                                color = animatedColor,
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    coords.forEachIndexed { idx, c ->
                        val pt = points[idx]
                        val isSelected = idx == safeIndex
                        val isLatest = idx == points.lastIndex

                        val pointProgress = if (plotW > 0f) ((c.x - leftPad) / plotW).coerceIn(0f, 1f) else 0f
                        val scaleFraction = if (count <= 1) revealProgress.value else ((revealProgress.value - pointProgress) / 0.12f).coerceIn(0f, 1f)
                        val pointScale = FastOutSlowInEasing.transform(scaleFraction)

                        if (pointScale > 0.01f) {
                            // Pulsing halo for latest reading node
                            if (isLatest && pointScale > 0.6f) {
                                drawCircle(
                                    color = pt.color.copy(alpha = pulseAlpha * pointScale),
                                    radius = 8.dp.toPx() * pulseScale,
                                    center = c
                                )
                            }

                            if (isSelected) {
                                drawCircle(
                                    color = pt.color.copy(alpha = 0.25f),
                                    radius = 10.dp.toPx() * pointScale,
                                    center = c
                                )
                                drawCircle(
                                    color = if (isDark) CarbonDarkSurface else Color.White,
                                    radius = 6.dp.toPx() * pointScale,
                                    center = c
                                )
                            }

                            drawCircle(
                                color = pt.color,
                                radius = if (isSelected) 5.dp.toPx() * pointScale else 3.8.dp.toPx() * pointScale,
                                center = c
                            )
                        }
                    }
                }
            }

            // Evenly spaced X-axis timestamps below graph (at most 4 timestamps)
            if (displayTimestamps.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 40.dp, end = 16.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    displayTimestamps.forEach { timeLabel ->
                        Text(
                            text = timeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clinical Range FlowRow Legend
            ClinicalRangeFlowLegend(
                items = listOf(
                    LegendItem("60–100 Optimal", VitalClinicalRanges.COLOR_OPTIMAL),
                    LegendItem("50–59 / 101–120 Borderline", VitalClinicalRanges.COLOR_ELEVATED),
                    LegendItem("<50 / >120 Critical", VitalClinicalRanges.COLOR_CRITICAL)
                ),
                isDark = isDark
            )
        }
    }
}

/**
 * Blood Oxygen (SpO2) Trend Chart:
 * Clean dark-mode Canvas line/area chart with dynamic clinical status colors:
 * - Optimal (95-100%): #10B981
 * - Moderate / Mildly Low (91-94%): #F59E0B
 * - Critical (<=90%): #EF4444
 */
@Composable
fun Spo2TrendChart(
    readings: List<VitalReadingItem>,
    latestSpo2: Int? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground
    val points = remember(readings) {
        val extracted = extractTrendPoints(readings, "spo2", limit = 10)
        if (extracted.isEmpty() && latestSpo2 != null && latestSpo2 > 0) {
            listOf(
                TrendPoint(
                    id = 1,
                    timeLabel = "Now",
                    fullDate = "Today",
                    value1 = latestSpo2.toFloat(),
                    color = VitalClinicalRanges.getSpo2Color(latestSpo2.toFloat()),
                    status = VitalClinicalRanges.getSpo2Label(latestSpo2.toFloat()),
                    source = "Latest"
                )
            )
        } else extracted
    }

    if (points.isEmpty()) {
        VitalChartEmptyCard(
            title = "Blood Oxygen (SpO₂) Trend",
            unit = "%",
            message = "No blood oxygen readings recorded yet. Measure with a pulse oximeter or sync to view trend analytics.",
            icon = Icons.Rounded.Opacity,
            isDark = isDark,
            modifier = modifier
        )
        return
    }

    var selectedIndex by remember(points.size) { mutableIntStateOf(points.lastIndex) }
    val safeIndex = selectedIndex.coerceIn(0, points.lastIndex)
    val activePoint = points[safeIndex]
    val displayTimestamps = remember(points) { computeDisplayTimestamps(points) }

    // Smooth horizontal line and area reveal animation
    val revealProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        revealProgress.snapTo(0f)
        revealProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    // Subtle pulsing halo animation for latest reading node
    val infiniteTransition = rememberInfiniteTransition(label = "spo2Pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spo2PulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spo2PulseAlpha"
    )

    // Smooth value & status transitions
    val animatedColor by animateColorAsState(
        targetValue = activePoint.color,
        animationSpec = tween(durationMillis = 350),
        label = "spo2Color"
    )
    val animatedValue by animateIntAsState(
        targetValue = activePoint.value1.toInt(),
        animationSpec = tween(durationMillis = 350),
        label = "spo2Value"
    )

    // Reusable Path and Paint instances for allocation-free Canvas animations
    val linePath = remember { Path() }
    val fillPath = remember { Path() }
    val axisTextPaint = remember(isDark) {
        android.graphics.Paint().apply {
            color = if (isDark) android.graphics.Color.parseColor("#8B949E") else android.graphics.Color.parseColor("#94A3B8")
            isAntiAlias = true
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) CarbonDarkSurface else Color.White
        ),
        border = BorderStroke(1.dp, if (isDark) CarbonDarkBorder else Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(animatedColor.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Opacity,
                            contentDescription = null,
                            tint = animatedColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Blood Oxygen (SpO₂) Trend",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${activePoint.timeLabel} ${activePoint.fullDate}".trim(),
                            fontSize = 11.sp,
                            color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$animatedValue%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827),
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(animatedColor.copy(alpha = 0.18f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = activePoint.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = animatedColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fixed Y scale for SpO2: 85% to 100%
            val minVal = 85f
            val maxVal = 100f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(points) {
                            detectTapGestures { tapOffset ->
                                val count = points.size
                                if (count > 1) {
                                    val stepX = (size.width - 56.dp.toPx()) / (count - 1)
                                    val leftPad = 40.dp.toPx()
                                    val clicked = ((tapOffset.x - leftPad + stepX / 2) / stepX)
                                        .toInt()
                                        .coerceIn(0, count - 1)
                                    selectedIndex = clicked
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val leftPad = 40.dp.toPx()
                    val rightPad = 16.dp.toPx()
                    val topPad = 12.dp.toPx()
                    val bottomPad = 10.dp.toPx()
                    val plotW = w - leftPad - rightPad
                    val plotH = h - topPad - bottomPad

                    val gridColor = if (isDark) Color(0xFF262E38) else Color(0xFFE2E8F0)

                    val yLevels = listOf(100f, 95f, 90f)
                    axisTextPaint.textSize = 10.sp.toPx()

                    yLevels.forEach { yLvl ->
                        val yPos = topPad + plotH * (1f - (yLvl - minVal) / (maxVal - minVal))
                        drawLine(
                            color = if (yLvl == 95f) VitalClinicalRanges.COLOR_OPTIMAL.copy(alpha = 0.35f)
                            else if (yLvl == 90f) VitalClinicalRanges.COLOR_CRITICAL.copy(alpha = 0.35f)
                            else gridColor,
                            start = Offset(leftPad, yPos),
                            end = Offset(w - rightPad, yPos),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            "${yLvl.toInt()}%",
                            6.dp.toPx(),
                            yPos + 4.dp.toPx(),
                            axisTextPaint
                        )
                    }

                    // Baseline
                    drawLine(
                        color = gridColor,
                        start = Offset(leftPad, topPad + plotH),
                        end = Offset(w - rightPad, topPad + plotH),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    val count = points.size
                    val coords = points.mapIndexed { idx, pt ->
                        val x = if (count == 1) leftPad + plotW / 2 else leftPad + (plotW / (count - 1)) * idx
                        val normY = ((pt.value1 - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                        val y = topPad + plotH * (1f - normY)
                        Offset(x, y)
                    }

                    val revealX = leftPad + plotW * revealProgress.value

                    if (coords.size > 1) {
                        linePath.reset()
                        linePath.moveTo(coords.first().x, coords.first().y)
                        for (i in 1 until coords.size) {
                            val prev = coords[i - 1]
                            val cur = coords[i]
                            val midX = (prev.x + cur.x) / 2f
                            linePath.cubicTo(midX, prev.y, midX, cur.y, cur.x, cur.y)
                        }

                        fillPath.reset()
                        fillPath.addPath(linePath)
                        fillPath.lineTo(coords.last().x, topPad + plotH)
                        fillPath.lineTo(coords.first().x, topPad + plotH)
                        fillPath.close()

                        clipRect(right = revealX) {
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        animatedColor.copy(alpha = 0.28f),
                                        Color.Transparent
                                    ),
                                    startY = topPad,
                                    endY = topPad + plotH
                                )
                            )

                            drawPath(
                                path = linePath,
                                color = animatedColor,
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    coords.forEachIndexed { idx, c ->
                        val pt = points[idx]
                        val isSelected = idx == safeIndex
                        val isLatest = idx == points.lastIndex

                        val pointProgress = if (plotW > 0f) ((c.x - leftPad) / plotW).coerceIn(0f, 1f) else 0f
                        val scaleFraction = if (count <= 1) revealProgress.value else ((revealProgress.value - pointProgress) / 0.12f).coerceIn(0f, 1f)
                        val pointScale = FastOutSlowInEasing.transform(scaleFraction)

                        if (pointScale > 0.01f) {
                            // Pulsing halo for latest reading node
                            if (isLatest && pointScale > 0.6f) {
                                drawCircle(
                                    color = pt.color.copy(alpha = pulseAlpha * pointScale),
                                    radius = 8.dp.toPx() * pulseScale,
                                    center = c
                                )
                            }

                            if (isSelected) {
                                drawCircle(
                                    color = pt.color.copy(alpha = 0.25f),
                                    radius = 10.dp.toPx() * pointScale,
                                    center = c
                                )
                                drawCircle(
                                    color = if (isDark) CarbonDarkSurface else Color.White,
                                    radius = 6.dp.toPx() * pointScale,
                                    center = c
                                )
                            }

                            drawCircle(
                                color = pt.color,
                                radius = if (isSelected) 5.dp.toPx() * pointScale else 3.8.dp.toPx() * pointScale,
                                center = c
                            )
                        }
                    }
                }
            }

            // Evenly spaced X-axis timestamps below graph (at most 4 timestamps)
            if (displayTimestamps.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 40.dp, end = 16.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    displayTimestamps.forEach { timeLabel ->
                        Text(
                            text = timeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            ClinicalRangeFlowLegend(
                items = listOf(
                    LegendItem(">=95% Optimal", VitalClinicalRanges.COLOR_OPTIMAL),
                    LegendItem("91–94% Mild Hypoxia", VitalClinicalRanges.COLOR_ELEVATED),
                    LegendItem("<=90% Critical Alert", VitalClinicalRanges.COLOR_CRITICAL)
                ),
                isDark = isDark
            )
        }
    }
}

/**
 * Blood Pressure Trend Chart:
 * Dual-line trend (Systolic curve on top + Diastolic curve below with connecting pulse pressure band)
 * Dynamic clinical status colors:
 * - Normal (Sys < 120 and Dia < 80): #10B981
 * - Elevated / Stage 1 (Sys 120-139 or Dia 80-89): #F59E0B
 * - High / Stage 2+ (Sys >= 140 or Dia >= 90): #EF4444
 */
@Composable
fun BloodPressureTrendChart(
    readings: List<VitalReadingItem>,
    latestSys: Int? = null,
    latestDia: Int? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground
    val points = remember(readings) {
        val extracted = extractTrendPoints(readings, "blood_pressure", limit = 10)
        if (extracted.isEmpty() && latestSys != null && latestSys > 0 && latestDia != null && latestDia > 0) {
            listOf(
                TrendPoint(
                    id = 1,
                    timeLabel = "Now",
                    fullDate = "Today",
                    value1 = latestSys.toFloat(),
                    value2 = latestDia.toFloat(),
                    color = VitalClinicalRanges.getBpColor(latestSys.toFloat(), latestDia.toFloat()),
                    status = VitalClinicalRanges.getBpLabel(latestSys.toFloat(), latestDia.toFloat()),
                    source = "Latest"
                )
            )
        } else extracted
    }

    if (points.isEmpty()) {
        VitalChartEmptyCard(
            title = "Blood Pressure Trend",
            unit = "mmHg",
            message = "No blood pressure trend logged yet. Record cuff measurements to visualize systolic & diastolic trends.",
            icon = Icons.Rounded.Speed,
            isDark = isDark,
            modifier = modifier
        )
        return
    }

    var selectedIndex by remember(points.size) { mutableIntStateOf(points.lastIndex) }
    val safeIndex = selectedIndex.coerceIn(0, points.lastIndex)
    val activePoint = points[safeIndex]
    val activeDia = activePoint.value2 ?: 80f
    val displayTimestamps = remember(points) { computeDisplayTimestamps(points) }

    // Smooth horizontal line and area reveal animation
    val revealProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        revealProgress.snapTo(0f)
        revealProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    // Subtle pulsing halo animation for latest reading node
    val infiniteTransition = rememberInfiniteTransition(label = "bpPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bpPulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bpPulseAlpha"
    )

    // Smooth value & status transitions
    val animatedColor by animateColorAsState(
        targetValue = activePoint.color,
        animationSpec = tween(durationMillis = 350),
        label = "bpColor"
    )
    val animatedSys by animateIntAsState(
        targetValue = activePoint.value1.toInt(),
        animationSpec = tween(durationMillis = 350),
        label = "bpSys"
    )
    val animatedDia by animateIntAsState(
        targetValue = activeDia.toInt(),
        animationSpec = tween(durationMillis = 350),
        label = "bpDia"
    )

    // Reusable Path and Paint instances for allocation-free Canvas animations
    val bandPath = remember { Path() }
    val sysPath = remember { Path() }
    val diaPath = remember { Path() }
    val axisTextPaint = remember(isDark) {
        android.graphics.Paint().apply {
            color = if (isDark) android.graphics.Color.parseColor("#8B949E") else android.graphics.Color.parseColor("#94A3B8")
            isAntiAlias = true
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) CarbonDarkSurface else Color.White
        ),
        border = BorderStroke(1.dp, if (isDark) CarbonDarkBorder else Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(animatedColor.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Speed,
                            contentDescription = null,
                            tint = animatedColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Blood Pressure Trend",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${activePoint.timeLabel} ${activePoint.fullDate}".trim(),
                            fontSize = 11.sp,
                            color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$animatedSys/$animatedDia",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827),
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "mmHg",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B),
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(animatedColor.copy(alpha = 0.18f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = activePoint.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = animatedColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Y scale: 50 to 180 mmHg
            val minVal = 50f
            val maxVal = max(160f, (points.maxOf { it.value1 } + 15f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(points) {
                            detectTapGestures { tapOffset ->
                                val count = points.size
                                if (count > 1) {
                                    val stepX = (size.width - 56.dp.toPx()) / (count - 1)
                                    val leftPad = 40.dp.toPx()
                                    val clicked = ((tapOffset.x - leftPad + stepX / 2) / stepX)
                                        .toInt()
                                        .coerceIn(0, count - 1)
                                    selectedIndex = clicked
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val leftPad = 40.dp.toPx()
                    val rightPad = 16.dp.toPx()
                    val topPad = 12.dp.toPx()
                    val bottomPad = 10.dp.toPx()
                    val plotW = w - leftPad - rightPad
                    val plotH = h - topPad - bottomPad

                    val gridColor = if (isDark) Color(0xFF262E38) else Color(0xFFE2E8F0)

                    val yLevels = listOf(140f, 120f, 80f, 60f)
                    axisTextPaint.textSize = 10.sp.toPx()

                    yLevels.forEach { yLvl ->
                        val yPos = topPad + plotH * (1f - (yLvl - minVal) / (maxVal - minVal))
                        drawLine(
                            color = if (yLvl == 140f) VitalClinicalRanges.COLOR_CRITICAL.copy(alpha = 0.25f)
                            else if (yLvl == 120f) VitalClinicalRanges.COLOR_OPTIMAL.copy(alpha = 0.25f)
                            else gridColor,
                            start = Offset(leftPad, yPos),
                            end = Offset(w - rightPad, yPos),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            "${yLvl.toInt()}",
                            6.dp.toPx(),
                            yPos + 4.dp.toPx(),
                            axisTextPaint
                        )
                    }

                    // Baseline
                    drawLine(
                        color = gridColor,
                        start = Offset(leftPad, topPad + plotH),
                        end = Offset(w - rightPad, topPad + plotH),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    val count = points.size
                    val sysCoords = points.mapIndexed { idx, pt ->
                        val x = if (count == 1) leftPad + plotW / 2 else leftPad + (plotW / (count - 1)) * idx
                        val normY = ((pt.value1 - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                        val y = topPad + plotH * (1f - normY)
                        Offset(x, y)
                    }

                    val diaCoords = points.mapIndexed { idx, pt ->
                        val x = if (count == 1) leftPad + plotW / 2 else leftPad + (plotW / (count - 1)) * idx
                        val dia = pt.value2 ?: 80f
                        val normY = ((dia - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                        val y = topPad + plotH * (1f - normY)
                        Offset(x, y)
                    }

                    val revealX = leftPad + plotW * revealProgress.value

                    // Shaded pulse pressure band between Systolic and Diastolic
                    if (sysCoords.size > 1) {
                        bandPath.reset()
                        bandPath.moveTo(sysCoords.first().x, sysCoords.first().y)
                        for (i in 1 until sysCoords.size) {
                            bandPath.lineTo(sysCoords[i].x, sysCoords[i].y)
                        }
                        for (i in diaCoords.indices.reversed()) {
                            bandPath.lineTo(diaCoords[i].x, diaCoords[i].y)
                        }
                        bandPath.close()

                        sysPath.reset()
                        sysPath.moveTo(sysCoords.first().x, sysCoords.first().y)
                        for (i in 1 until sysCoords.size) {
                            sysPath.lineTo(sysCoords[i].x, sysCoords[i].y)
                        }

                        diaPath.reset()
                        diaPath.moveTo(diaCoords.first().x, diaCoords.first().y)
                        for (i in 1 until diaCoords.size) {
                            diaPath.lineTo(diaCoords[i].x, diaCoords[i].y)
                        }

                        clipRect(right = revealX) {
                            drawPath(
                                path = bandPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        animatedColor.copy(alpha = 0.22f),
                                        Color(0xFF38BDF8).copy(alpha = 0.12f)
                                    ),
                                    startY = topPad,
                                    endY = topPad + plotH
                                )
                            )

                            // Systolic Line (top)
                            drawPath(
                                path = sysPath,
                                color = animatedColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )

                            // Diastolic Line (bottom)
                            drawPath(
                                path = diaPath,
                                color = Color(0xFF38BDF8),
                                style = Stroke(
                                    width = 2.5.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                                )
                            )
                        }
                    }

                    // Draw vertical connectors & dots
                    sysCoords.forEachIndexed { idx, sCoord ->
                        val dCoord = diaCoords[idx]
                        val pt = points[idx]
                        val isSelected = idx == safeIndex
                        val isLatest = idx == points.lastIndex

                        val pointProgress = if (plotW > 0f) ((sCoord.x - leftPad) / plotW).coerceIn(0f, 1f) else 0f
                        val scaleFraction = if (count <= 1) revealProgress.value else ((revealProgress.value - pointProgress) / 0.12f).coerceIn(0f, 1f)
                        val pointScale = FastOutSlowInEasing.transform(scaleFraction)

                        if (pointScale > 0.01f) {
                            // Pulsing halo for latest reading node (both systolic and diastolic)
                            if (isLatest && pointScale > 0.6f) {
                                drawCircle(
                                    color = pt.color.copy(alpha = pulseAlpha * pointScale),
                                    radius = 8.dp.toPx() * pulseScale,
                                    center = sCoord
                                )
                                drawCircle(
                                    color = Color(0xFF38BDF8).copy(alpha = pulseAlpha * pointScale),
                                    radius = 7.dp.toPx() * pulseScale,
                                    center = dCoord
                                )
                            }

                            drawLine(
                                color = pt.color.copy(alpha = (if (isSelected) 0.6f else 0.25f) * pointScale),
                                start = sCoord,
                                end = dCoord,
                                strokeWidth = (if (isSelected) 2.5.dp.toPx() else 1.5.dp.toPx()) * pointScale
                            )

                            if (isSelected) {
                                drawCircle(color = pt.color.copy(alpha = 0.25f), radius = 10.dp.toPx() * pointScale, center = sCoord)
                            }
                            drawCircle(color = pt.color, radius = (if (isSelected) 5.dp.toPx() else 3.8.dp.toPx()) * pointScale, center = sCoord)

                            if (isSelected) {
                                drawCircle(color = Color(0xFF38BDF8).copy(alpha = 0.25f), radius = 10.dp.toPx() * pointScale, center = dCoord)
                            }
                            drawCircle(color = Color(0xFF38BDF8), radius = (if (isSelected) 4.5.dp.toPx() else 3.5.dp.toPx()) * pointScale, center = dCoord)
                        }
                    }
                }
            }

            // Evenly spaced X-axis timestamps below graph (at most 4 timestamps)
            if (displayTimestamps.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 40.dp, end = 16.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    displayTimestamps.forEach { timeLabel ->
                        Text(
                            text = timeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clinical Range FlowRow Legend (includes curves + clinical bounds)
            ClinicalRangeFlowLegend(
                items = listOf(
                    LegendItem("Systolic", animatedColor),
                    LegendItem("Diastolic", Color(0xFF38BDF8)),
                    LegendItem("<120/<80 Normal", VitalClinicalRanges.COLOR_OPTIMAL),
                    LegendItem("120–139 Stage 1", VitalClinicalRanges.COLOR_ELEVATED),
                    LegendItem(">=140 High", VitalClinicalRanges.COLOR_CRITICAL)
                ),
                isDark = isDark
            )
        }
    }
}

/**
 * Empty state card for vitals trend charts when no readings exist yet.
 */
@Composable
fun VitalChartEmptyCard(
    title: String,
    unit: String,
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) CarbonDarkSurface else Color.White
        ),
        border = BorderStroke(1.dp, if (isDark) CarbonDarkBorder else Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF1F5F9))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "No Data",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF8FAFC)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Timeline,
                    contentDescription = null,
                    tint = if (isDark) CarbonDarkTextSecondary else Color(0xFF94A3B8),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No Trend Readings Recorded",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = message,
                fontSize = 12.sp,
                color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

/**
 * Interactive Analytical Trends Dashboard for the Vitals / Analytics screen:
 * Displays selectable tabs for Heart Rate, SpO2, and Blood Pressure with
 * live reactively-updating trend charts.
 */
@Composable
fun VitalAnalyticalTrendsDashboard(
    readings: List<VitalReadingItem>,
    latestHr: Int? = null,
    latestSpo2: Int? = null,
    latestSys: Int? = null,
    latestDia: Int? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = HR, 1 = SpO2, 2 = BP

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Analytical Trends",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
            )
            Text(
                text = "Clinical Analytics",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Full-width Segmented Metric Selector Pills (Equal width, zero text wrapping)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF1F5F9))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Heart Rate", "SpO₂", "Blood Pressure").forEachIndexed { idx, tabLabel ->
                val isTabSelected = selectedTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            if (isTabSelected) {
                                if (isDark) Color(0xFF10B981) else Color(0xFF0F9F7A)
                            } else Color.Transparent
                        )
                        .clickable { selectedTab = idx }
                        .padding(vertical = 7.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabLabel,
                        fontSize = 12.sp,
                        fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isTabSelected) Color.White else (if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B)),
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(280)) togetherWith fadeOut(animationSpec = tween(180))
            },
            label = "trendDashboardTab"
        ) { tab ->
            when (tab) {
                0 -> HeartRateTrendChart(readings = readings, latestBpm = latestHr)
                1 -> Spo2TrendChart(readings = readings, latestSpo2 = latestSpo2)
                2 -> BloodPressureTrendChart(readings = readings, latestSys = latestSys, latestDia = latestDia)
            }
        }
    }
}
