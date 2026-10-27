package com.simats.vitalsync.ui.ai

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme

// Dynamic Health Classification Colors
private val DarkGreenNormal = Color(0xFF00A884) // Green (Normal / Optimal)
private val LightGreenNormalBg = Color(0xFFDDF7EC)

private val DarkYellowMid = Color(0xFFD97706) // Yellow / Amber (Mid / Moderate)
private val LightYellowMidBg = Color(0xFFFEF3C7)

private val DarkRedLow = Color(0xFFDC2626) // Red (Low / Critical Alert)
private val LightRedLowBg = Color(0xFFFEE2E2)

enum class MetricStatusType {
    NORMAL, // Green
    MID,    // Yellow
    LOW     // Red
}

@Composable
fun WeeklyHealthReportScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
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

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "📅", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Overall Vital Performance · October 9–15, 2025",
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
                                text = "82",
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
                            text = "Overall vital performance is Good (+6% increase from last week).",
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
                                .background(LightGreenNormalBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "↑ 6% Normal",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGreenNormal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Smoothly Animated Trend Line
                    var targetProgress by remember { mutableFloatStateOf(0f) }
                    LaunchedEffect(Unit) {
                        targetProgress = 1f
                    }
                    val chartProgress by animateFloatAsState(
                        targetValue = targetProgress,
                        animationSpec = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
                        label = "WellnessTrendLineAnimation"
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(85.dp)
                    ) {
                        val w = size.width
                        val h = size.height

                        val fullPath = Path().apply {
                            moveTo(0f, h * 0.7f)
                            lineTo(w * 0.15f, h * 0.4f)
                            lineTo(w * 0.30f, h * 0.5f)
                            lineTo(w * 0.45f, h * 0.35f)
                            lineTo(w * 0.60f, h * 0.40f)
                            lineTo(w * 0.75f, h * 0.20f)
                            lineTo(w, h * 0.30f)
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
                                    colors = listOf(DarkGreenNormal.copy(alpha = 0.30f), DarkGreenNormal.copy(alpha = 0.0f))
                                )
                            )

                            drawPath(
                                path = animatedPath,
                                color = DarkGreenNormal,
                                style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
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

            // Vital Metrics Cards Row 1 (Green Normal vs Yellow Mid)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportMetricCard(
                    title = "Heart Rate Avg",
                    value = "76 BPM",
                    statusText = "Normal",
                    statusType = MetricStatusType.NORMAL,
                    modifier = Modifier.weight(1f)
                )

                ReportMetricCard(
                    title = "Blood Pressure",
                    value = "132/86",
                    statusText = "Mid / Elevated",
                    statusType = MetricStatusType.MID,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vital Metrics Cards Row 2 (Red Low vs Green Normal)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportMetricCard(
                    title = "SpO₂ Avg",
                    value = "92%",
                    statusText = "Low Rate Alert",
                    statusType = MetricStatusType.LOW,
                    modifier = Modifier.weight(1f)
                )

                ReportMetricCard(
                    title = "Sleep Quality",
                    value = "7h 32m",
                    statusText = "Score 88 Normal",
                    statusType = MetricStatusType.NORMAL,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vital Metrics Cards Row 3 (Yellow Mid vs Red Low)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportMetricCard(
                    title = "Hydration",
                    value = "1,250 ml",
                    statusText = "5/8 Mid Goal",
                    statusType = MetricStatusType.MID,
                    modifier = Modifier.weight(1f)
                )

                ReportMetricCard(
                    title = "Activity Steps",
                    value = "3,120 steps",
                    statusText = "Low Goal Alert",
                    statusType = MetricStatusType.LOW,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AI Weekly Summary & Recommendations Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "✨ AI Doctor Observations",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C3AED)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Heart Rate & Sleep remain in the optimal Green Normal range.\n" +
                            "• Blood Pressure & Hydration are in the Yellow Mid range.\n" +
                            "• SpO₂ & Daily Step Count dipped into the Red Low Alert range this week.\n" +
                            "• Recommendation: Take brief walk breaks and maintain continuous hydration.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
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
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VitalTealDark,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Download PDF Report", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VitalTealDark)
                ) {
                    Text(text = "Share Summary", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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

            // Classification Color Status Badge
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

            // Animated Metric Line in Classification Color (Green / Yellow / Red)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                val w = size.width
                val h = size.height

                val fullPath = Path().apply {
                    when (statusType) {
                        MetricStatusType.NORMAL -> {
                            moveTo(0f, h * 0.7f)
                            quadraticTo(w * 0.25f, h * 0.2f, w * 0.5f, h * 0.5f)
                            quadraticTo(w * 0.75f, h * 0.8f, w, h * 0.2f)
                        }
                        MetricStatusType.MID -> {
                            moveTo(0f, h * 0.4f)
                            quadraticTo(w * 0.25f, h * 0.6f, w * 0.5f, h * 0.3f)
                            quadraticTo(w * 0.75f, h * 0.7f, w, h * 0.5f)
                        }
                        MetricStatusType.LOW -> {
                            moveTo(0f, h * 0.2f)
                            quadraticTo(w * 0.25f, h * 0.5f, w * 0.5f, h * 0.7f)
                            quadraticTo(w * 0.75f, h * 0.4f, w, h * 0.85f)
                        }
                    }
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
