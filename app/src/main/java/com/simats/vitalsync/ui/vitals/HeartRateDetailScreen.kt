package com.simats.vitalsync.ui.vitals

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.VitalsyncTheme

// Color classification constants
private val HighAmberColor = Color(0xFFD97706) // Amber / High (>90)
private val NormalTealColor = Color(0xFF00A884) // Teal / Normal (60-90)
private val LowRedColor = Color(0xFFDC2626) // Coral / Red / Low (<60)

@Composable
fun HeartRateDetailScreen(
    modifier: Modifier = Modifier,
    uiState: HeartRateUiState = HeartRateUiState(),
    initialBpm: Int = 0,
    restingBpm: Int = 0,
    minBpm: Int = 0,
    maxBpm: Int = 0,
    avgBpm: Int = 0,
    showAiInsights: Boolean = true,
    onBackClick: () -> Unit = {},
    onReadingReviewClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {}
) {
    val currentBpm = uiState.currentBpm ?: if (initialBpm > 0) initialBpm else null
    val avgBpmVal = uiState.averageBpm ?: if (avgBpm > 0) avgBpm else currentBpm
    val maxBpmVal = uiState.highestBpm ?: if (maxBpm > 0) maxBpm else currentBpm
    val minBpmVal = uiState.lowestBpm ?: if (minBpm > 0) minBpm else currentBpm
    val restingBpmVal = uiState.restingBpm ?: if (restingBpm > 0) restingBpm else null

    val isElevated = currentBpm != null && currentBpm > 100
    val isLive = uiState.isLiveStreaming

    // Dynamic Status Badge & Subtitle
    val (statusLabel, statusColor, statusBgColor) = when {
        currentBpm == null || currentBpm <= 0 -> Triple("No data", Color(0xFF6B7280), Color(0xFFF3F4F6))
        currentBpm < 60 -> Triple("Low Rate", LowRedColor, Color(0xFFFEE2E2))
        currentBpm in 60..90 -> Triple("Normal", NormalTealColor, Color(0xFFDDF7EC))
        currentBpm in 91..100 -> Triple("Moderate", HighAmberColor, Color(0xFFFEF3C7))
        else -> Triple("Elevated", LowRedColor, Color(0xFFFEE2E2))
    }

    val subtitleStatus = if (currentBpm != null && currentBpm > 0) {
        if (uiState.bpmStatus.isNotBlank() && uiState.bpmStatus != "No data") {
            "Status: ${uiState.bpmStatus}"
        } else {
            "Status: $statusLabel resting rhythm"
        }
    } else {
        "Status: No data"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Top Bar
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
                    text = "Heart Rate",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = onHistoryClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                HistoryDataLogIcon(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Latest Reading Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isLive) {
                            val heartPulse = rememberInfiniteTransition(label = "pulseHeart")
                            val hScale by heartPulse.animateFloat(
                                initialValue = 1.0f,
                                targetValue = 1.30f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(550, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "heartScale"
                            )
                            HeartIconSmall(
                                modifier = Modifier.size(18.dp).scale(hScale),
                                color = statusColor
                            )
                        } else {
                            HeartIconSmall(modifier = Modifier.size(18.dp), color = statusColor)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        val formattedTimeText = if (isLive) {
                            "Latest Reading - Just Now"
                        } else if (uiState.formattedTime.isNotBlank() && uiState.formattedTime != "--") {
                            "Latest Reading - ${uiState.formattedTime}"
                        } else {
                            "Latest Reading - Just Now"
                        }

                        Text(
                            text = formattedTimeText,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Yellow Review Badge if Elevated / Review needed
                        if (isElevated) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .clickable { onReadingReviewClick() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Review ⚠️",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HighAmberColor
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusBgColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isLive) "● Live" else statusLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (currentBpm != null && currentBpm > 0) "$currentBpm" else "--",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BPM",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 7.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subtitleStatus,
                    fontSize = 13.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Heart Rate Trend Card (7 Days)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
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
                        text = "Heart rate trend",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "7 days",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Color Legend Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(HighAmberColor))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "High (>90)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HighAmberColor)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(NormalTealColor))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Normal (60-90)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NormalTealColor)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(LowRedColor))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Low (<60)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LowRedColor)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Smooth Curved Chart with 3 Colored Background Range Bands
                var targetProgress by remember { mutableFloatStateOf(0f) }
                LaunchedEffect(Unit) {
                    targetProgress = 1f
                }
                val chartProgress by animateFloatAsState(
                    targetValue = targetProgress,
                    animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                    label = "HeartRateTrendLineAnimation"
                )

                val fullPath = remember { Path() }
                val animatedPath = remember { Path() }
                val fillPath = remember { Path() }
                val pathMeasure = remember { PathMeasure() }

                val hasTrendData = uiState.hasTrendData || (currentBpm != null && currentBpm > 0)
                val trendDaysList = uiState.trendDays

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Top Band (High > 90): Light amber tint
                        drawRect(
                            color = HighAmberColor.copy(alpha = 0.12f),
                            topLeft = Offset(0f, 0f),
                            size = Size(w, h * 0.33f)
                        )

                        // Middle Band (Normal 60-90): Soft teal tint
                        drawRect(
                            color = NormalTealColor.copy(alpha = 0.15f),
                            topLeft = Offset(0f, h * 0.33f),
                            size = Size(w, h * 0.34f)
                        )

                        // Bottom Band (Low < 60): Soft red/pink tint
                        drawRect(
                            color = LowRedColor.copy(alpha = 0.12f),
                            topLeft = Offset(0f, h * 0.67f),
                            size = Size(w, h * 0.33f)
                        )

                        if (!hasTrendData) {
                            // Baseline horizontal dashed line when no data exists
                            val dashPath = Path().apply {
                                moveTo(0f, h * 0.5f)
                                lineTo(w, h * 0.5f)
                            }
                            drawPath(
                                path = dashPath,
                                color = Color(0xFF9CA3AF),
                                style = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                                )
                            )
                        } else {
                            // Dynamic curve based on real days or smooth generated spline
                            fullPath.reset()
                            val points = mutableListOf<Offset>()

                            if (trendDaysList.isNotEmpty() && trendDaysList.any { it.hasData && it.bpm != null }) {
                                val validDays = trendDaysList.take(7)
                                val stepX = w / (validDays.size - 1).coerceAtLeast(1)
                                validDays.forEachIndexed { idx, d ->
                                    val bpmVal = d.bpm ?: currentBpm ?: 72
                                    val clampedBpm = bpmVal.coerceIn(40, 140)
                                    // Map 40..140 BPM to h..0f
                                    val y = h - ((clampedBpm - 40f) / 100f * h)
                                    points.add(Offset(idx * stepX, y))
                                }
                            } else {
                                // Baseline natural resting rhythm spline
                                val baseBpm = currentBpm ?: 74
                                val baseRatio = (baseBpm.coerceIn(50, 120) - 50f) / 70f
                                val midY = h * (0.65f - baseRatio * 0.35f)

                                points.add(Offset(0f, midY + h * 0.10f))
                                points.add(Offset(w * 0.16f, midY - h * 0.05f))
                                points.add(Offset(w * 0.32f, midY - h * 0.15f))
                                points.add(Offset(w * 0.48f, midY + h * 0.05f))
                                points.add(Offset(w * 0.64f, midY - h * 0.18f))
                                points.add(Offset(w * 0.80f, midY - h * 0.05f))
                                points.add(Offset(w, midY))
                            }

                            if (points.isNotEmpty()) {
                                fullPath.moveTo(points[0].x, points[0].y)
                                for (k in 0 until points.size - 1) {
                                    val p0 = points[k]
                                    val p1 = points[k + 1]
                                    val ctrlX1 = (p0.x + p1.x) / 2f
                                    fullPath.cubicTo(ctrlX1, p0.y, ctrlX1, p1.y, p1.x, p1.y)
                                }

                                pathMeasure.setPath(fullPath, false)
                                animatedPath.reset()
                                pathMeasure.getSegment(0f, pathMeasure.length * chartProgress, animatedPath, true)

                                if (chartProgress > 0f) {
                                    fillPath.reset()
                                    fillPath.addPath(animatedPath)
                                    fillPath.lineTo(w * chartProgress, h)
                                    fillPath.lineTo(0f, h)
                                    fillPath.close()

                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(statusColor.copy(alpha = 0.35f), statusColor.copy(alpha = 0.0f))
                                        )
                                    )

                                    drawPath(
                                        path = animatedPath,
                                        color = statusColor,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                }
                            }
                        }
                    }

                    if (!hasTrendData) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sync data from watch to generate trend",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // X-Axis Labels (M, T, W, T, F, S, S)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                        Text(
                            text = day,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Vitals Statistics 2x2 Grid Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Average",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (avgBpmVal != null && avgBpmVal > 0) "$avgBpmVal BPM" else "--",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Highest",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (maxBpmVal != null && maxBpmVal > 0) "$maxBpmVal BPM" else "--",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = LowRedColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lowest",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (minBpmVal != null && minBpmVal > 0) "$minBpmVal BPM" else "--",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighAmberColor
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Resting max",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (restingBpmVal != null && restingBpmVal > 0) "$restingBpmVal BPM" else "--",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. AI Insight Card
        if (showAiInsights) {
            Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "✨ AI insight",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C3AED)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = uiState.aiInsight,
                    fontSize = 14.sp,
                    color = Color(0xFF374151),
                    lineHeight = 20.sp
                )
            }
        }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
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

@Composable
private fun HistoryDataLogIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF6B7280)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Document / list history icon
        val docPath = Path().apply {
            moveTo(w * 0.20f, h * 0.15f)
            lineTo(w * 0.80f, h * 0.15f)
            lineTo(w * 0.80f, h * 0.85f)
            lineTo(w * 0.20f, h * 0.85f)
            close()
            moveTo(w * 0.35f, h * 0.35f)
            lineTo(w * 0.65f, h * 0.35f)
            moveTo(w * 0.35f, h * 0.50f)
            lineTo(w * 0.65f, h * 0.50f)
            moveTo(w * 0.35f, h * 0.65f)
            lineTo(w * 0.55f, h * 0.65f)
        }

        drawPath(path = docPath, color = color, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun HeartIconSmall(
    modifier: Modifier = Modifier,
    color: Color = NormalTealColor
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.8f)
            cubicTo(w * 0.1f, h * 0.5f, w * 0.1f, h * 0.2f, w * 0.5f, h * 0.35f)
            cubicTo(w * 0.9f, h * 0.2f, w * 0.9f, h * 0.5f, w * 0.5f, h * 0.8f)
            close()
        }
        drawPath(path = path, color = color, style = Stroke(width = 1.6.dp.toPx()))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun HeartRateDetailScreenPreview() {
    VitalsyncTheme {
        HeartRateDetailScreen()
    }
}
