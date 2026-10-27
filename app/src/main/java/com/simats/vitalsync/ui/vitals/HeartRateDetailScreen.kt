package com.simats.vitalsync.ui.vitals

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.VitalsyncTheme

// Classification Colors
private val DarkGreenClassification = Color(0xFF00A884) // Emerald Green
private val DarkYellowClassification = Color(0xFFD97706) // Amber / Yellow
private val DarkRedClassification = Color(0xFFDC2626) // Red

@Composable
fun HeartRateDetailScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onReadingReviewClick: () -> Unit = {}
) {
    var heartRateBpm by remember { mutableIntStateOf(76) }

    // Dynamic Level Color Mapping
    val (statusLabel, statusColor, statusBgColor) = when {
        heartRateBpm < 60 -> Triple("Low Rate", DarkRedClassification, Color(0xFFFEE2E2))
        heartRateBpm in 60..70 -> Triple("Moderate", DarkYellowClassification, Color(0xFFFEF3C7))
        heartRateBpm in 71..90 -> Triple("Normal", DarkGreenClassification, Color(0xFFDDF7EC))
        else -> Triple("High Rate", DarkRedClassification, Color(0xFFFEE2E2))
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
                    text = "Heart Rate",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                CalendarIcon(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Latest Reading Card
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
                        HeartIconSmall(modifier = Modifier.size(18.dp), color = statusColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Latest Reading - 9:00 AM",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                color = DarkYellowClassification
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusBgColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = statusLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$heartRateBpm",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BPM",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Status: $statusLabel",
                    fontSize = 13.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Heart Rate Trend Chart Card
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

                Spacer(modifier = Modifier.height(6.dp))

                // Color Classification Legend Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "● High (>90)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkYellowClassification)
                    Text(text = "● Normal (60-90)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkGreenClassification)
                    Text(text = "● Low (<60)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkRedClassification)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Smooth Animated Heart Rate Trend Line Chart Canvas with Zero Allocations
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

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    val w = size.width
                    val h = size.height

                    // 1. Draw 3 Dark Color Classification Background Range Bands
                    drawRect(
                        color = DarkYellowClassification.copy(alpha = 0.15f),
                        topLeft = Offset(0f, 0f),
                        size = Size(w, h * 0.33f)
                    )
                    drawRect(
                        color = DarkGreenClassification.copy(alpha = 0.18f),
                        topLeft = Offset(0f, h * 0.33f),
                        size = Size(w, h * 0.34f)
                    )
                    drawRect(
                        color = DarkRedClassification.copy(alpha = 0.15f),
                        topLeft = Offset(0f, h * 0.67f),
                        size = Size(w, h * 0.33f)
                    )

                    // 2. Trend Line Path
                    fullPath.reset()
                    fullPath.moveTo(0f, h * 0.70f)
                    fullPath.cubicTo(w * 0.08f, h * 0.60f, w * 0.12f, h * 0.50f, w * 0.16f, h * 0.50f)
                    fullPath.cubicTo(w * 0.24f, h * 0.30f, w * 0.28f, h * 0.20f, w * 0.32f, h * 0.20f)
                    fullPath.cubicTo(w * 0.40f, h * 0.30f, w * 0.44f, h * 0.40f, w * 0.48f, h * 0.40f)
                    fullPath.cubicTo(w * 0.56f, h * 0.20f, w * 0.60f, h * 0.10f, w * 0.64f, h * 0.10f)
                    fullPath.cubicTo(w * 0.72f, h * 0.20f, w * 0.76f, h * 0.30f, w * 0.80f, h * 0.30f)
                    fullPath.cubicTo(w * 0.90f, h * 0.40f, w * 0.95f, h * 0.50f, w, h * 0.50f)

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

                Spacer(modifier = Modifier.height(8.dp))

                // Days Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                        Text(
                            text = day,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4 Stat Metrics Grid Card
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
                        Text(text = "Average", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "74 BPM", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Highest", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "92 BPM", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkRedClassification)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Lowest", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "61 BPM", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkYellowClassification)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Resting max", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "64 BPM", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Insight Banner
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
                    text = "Your readings have remained relatively stable this week.",
                    fontSize = 14.sp,
                    color = Color(0xFF374151)
                )
            }
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
private fun CalendarIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF6B7280)
) {
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
private fun HeartIconSmall(
    modifier: Modifier = Modifier,
    color: Color = DarkGreenClassification
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
