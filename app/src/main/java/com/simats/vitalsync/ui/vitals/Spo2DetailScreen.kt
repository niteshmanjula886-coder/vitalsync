package com.simats.vitalsync.ui.vitals

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme

// Dark Classification Colors
private val DarkGreenClassification = Color(0xFF047857) // Dark Emerald Green
private val DarkYellowClassification = Color(0xFFB45309) // Dark Amber / Yellow
private val DarkRedClassification = Color(0xFFB91C1C) // Dark Red

@Composable
fun Spo2DetailScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onAddReadingClick: () -> Unit = {}
) {
    var spo2Percentage by remember { mutableIntStateOf(98) }

    // Dynamic SpO2 Status Color Mapping
    val (statusLabel, statusColor, statusBgColor) = when {
        spo2Percentage < 95 -> Triple("Low SpO₂", DarkRedClassification, Color(0xFFFEE2E2))
        spo2Percentage in 95..96 -> Triple("Moderate", DarkYellowClassification, Color(0xFFFEF3C7))
        else -> Triple("Optimal", DarkGreenClassification, Color(0xFFE6F4EA))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFCFB))
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
                            color = Color(0xFF111827)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "SpO₂",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    CalendarIcon(modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Latest Reading Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Latest reading",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )

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

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$spo2Percentage",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "oxygen saturation",
                            fontSize = 13.sp,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Oct 15, 2025 · 9:00 AM · Sample data",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Oxygen Saturation Trend Chart Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Oxygen saturation trend",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )

                        Text(
                            text = "7 days",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Color Classification Legend Row (Dark Green, Dark Yellow, Dark Red)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "● Optimal (≥97%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkGreenClassification)
                        Text(text = "● Moderate (95-96%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkYellowClassification)
                        Text(text = "● Low (<95%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkRedClassification)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated SpO2 Trend Line Chart Canvas with Dark Classification Range Bands
                    var targetProgress by remember { mutableFloatStateOf(0f) }
                    LaunchedEffect(Unit) {
                        targetProgress = 1f
                    }
                    val chartProgress by animateFloatAsState(
                        targetValue = targetProgress,
                        animationSpec = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
                        label = "Spo2TrendLineAnimation"
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        val w = size.width
                        val h = size.height

                        // 1. Dark Color Classification Range Background Bands
                        // Optimal Zone (Top 40%) -> Dark Green
                        drawRect(
                            color = DarkGreenClassification.copy(alpha = 0.18f),
                            topLeft = Offset(0f, 0f),
                            size = Size(w, h * 0.40f)
                        )
                        // Moderate Zone (Middle 30%) -> Dark Yellow
                        drawRect(
                            color = DarkYellowClassification.copy(alpha = 0.15f),
                            topLeft = Offset(0f, h * 0.40f),
                            size = Size(w, h * 0.30f)
                        )
                        // Low Zone (Bottom 30%) -> Dark Red
                        drawRect(
                            color = DarkRedClassification.copy(alpha = 0.15f),
                            topLeft = Offset(0f, h * 0.70f),
                            size = Size(w, h * 0.30f)
                        )

                        // 2. Trend Path
                        val fullPath = Path().apply {
                            moveTo(0f, h * 0.65f)
                            cubicTo(w * 0.08f, h * 0.58f, w * 0.12f, h * 0.50f, w * 0.16f, h * 0.50f)
                            cubicTo(w * 0.24f, h * 0.40f, w * 0.28f, h * 0.30f, w * 0.32f, h * 0.30f)
                            cubicTo(w * 0.40f, h * 0.38f, w * 0.44f, h * 0.45f, w * 0.48f, h * 0.45f)
                            cubicTo(w * 0.56f, h * 0.58f, w * 0.60f, h * 0.70f, w * 0.64f, h * 0.70f)
                            cubicTo(w * 0.72f, h * 0.45f, w * 0.76f, h * 0.20f, w * 0.80f, h * 0.20f)
                            cubicTo(w * 0.90f, h * 0.30f, w * 0.95f, h * 0.40f, w, h * 0.40f)
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
                                    colors = listOf(statusColor.copy(alpha = 0.35f), statusColor.copy(alpha = 0.0f))
                                )
                            )

                            drawPath(
                                path = animatedPath,
                                color = statusColor,
                                style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("9", "10", "11", "12", "13", "14", "15").forEach { date ->
                            Text(text = date, fontSize = 12.sp, color = Color(0xFF9CA3AF))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7-day Stats Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "7-day average", fontSize = 12.sp, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(text = "98", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(text = "%", fontSize = 12.sp, color = Color(0xFF6B7280), modifier = Modifier.padding(bottom = 2.dp))
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Highest", fontSize = 12.sp, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(text = "99", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkGreenClassification)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(text = "%", fontSize = 12.sp, color = Color(0xFF6B7280), modifier = Modifier.padding(bottom = 2.dp))
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Lowest", fontSize = 12.sp, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(text = "94", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkRedClassification)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(text = "%", fontSize = 12.sp, color = Color(0xFF6B7280), modifier = Modifier.padding(bottom = 2.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Readings History Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Recent readings",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Spo2HistoryRow(time = "Oct 15 - 9:00 AM", valStr = "98 %", isLow = false)
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))
                    Spo2HistoryRow(time = "Oct 14 - 8:42 AM", valStr = "99 %", isLow = false)
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))
                    Spo2HistoryRow(time = "Oct 13 - 9:05 AM", valStr = "94 % (Low)", isLow = true)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Button(
                onClick = onAddReadingClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VitalTealDark,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "+ Add Reading",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Sample readings are for wellness awareness, not diagnosis.",
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun Spo2HistoryRow(
    time: String,
    valStr: String,
    isLow: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = time, fontSize = 13.sp, color = Color(0xFF6B7280))
        Text(
            text = valStr,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isLow) DarkRedClassification else Color(0xFF111827)
        )
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
private fun CalendarIcon(modifier: Modifier = Modifier) {
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

        drawPath(path = calPath, color = Color(0xFF6B7280), style = Stroke(width = 1.6.dp.toPx()))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun Spo2DetailScreenPreview() {
    VitalsyncTheme {
        Spo2DetailScreen()
    }
}
