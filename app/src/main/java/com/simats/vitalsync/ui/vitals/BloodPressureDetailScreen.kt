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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme

@Composable
fun BloodPressureDetailScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onAddReadingClick: () -> Unit = {}
) {
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
                        text = "Blood Pressure",
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
                            text = "Latest Reading",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE6F4EA))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Normal",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F9F7A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "120 / 80",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "mmHg",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Oct 15, 2025 · 9:00 AM · Rest status",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Blood Pressure Trend Chart Card
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
                            text = "Blood pressure trend",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )

                        Text(
                            text = "7 days",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F9F7A)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(text = "● Systolic (High/Med)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A))
                        Text(text = "● Diastolic", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Animated Dual Trend Lines Chart
                    var targetProgress by remember { mutableFloatStateOf(0f) }
                    LaunchedEffect(Unit) {
                        targetProgress = 1f
                    }
                    val chartProgress by animateFloatAsState(
                        targetValue = targetProgress,
                        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                        label = "BpTrendLinesAnimation"
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        val w = size.width
                        val h = size.height

                        // Systolic Full Path
                        val sysFullPath = Path().apply {
                            moveTo(0f, h * 0.25f)
                            cubicTo(w * 0.12f, h * 0.22f, w * 0.20f, h * 0.22f, w * 0.25f, h * 0.22f)
                            cubicTo(w * 0.38f, h * 0.30f, w * 0.42f, h * 0.30f, w * 0.50f, h * 0.30f)
                            cubicTo(w * 0.62f, h * 0.20f, w * 0.68f, h * 0.20f, w * 0.75f, h * 0.20f)
                            cubicTo(w * 0.88f, h * 0.25f, w * 0.95f, h * 0.25f, w, h * 0.25f)
                        }

                        // Diastolic Full Path
                        val diaFullPath = Path().apply {
                            moveTo(0f, h * 0.70f)
                            cubicTo(w * 0.12f, h * 0.68f, w * 0.20f, h * 0.68f, w * 0.25f, h * 0.68f)
                            cubicTo(w * 0.38f, h * 0.75f, w * 0.42f, h * 0.75f, w * 0.50f, h * 0.75f)
                            cubicTo(w * 0.62f, h * 0.65f, w * 0.68f, h * 0.65f, w * 0.75f, h * 0.65f)
                            cubicTo(w * 0.88f, h * 0.70f, w * 0.95f, h * 0.70f, w, h * 0.70f)
                        }

                        val sysMeasure = PathMeasure().apply { setPath(sysFullPath, false) }
                        val diaMeasure = PathMeasure().apply { setPath(diaFullPath, false) }

                        val sysAnimatedPath = Path()
                        val diaAnimatedPath = Path()

                        sysMeasure.getSegment(0f, sysMeasure.length * chartProgress, sysAnimatedPath, true)
                        diaMeasure.getSegment(0f, diaMeasure.length * chartProgress, diaAnimatedPath, true)

                        if (chartProgress > 0f) {
                            // Systolic Area Fill
                            val sysFill = Path().apply {
                                addPath(sysAnimatedPath)
                                lineTo(w * chartProgress, h)
                                lineTo(0f, h)
                                close()
                            }
                            drawPath(
                                path = sysFill,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0x300F9F7A), Color(0x000F9F7A))
                                )
                            )

                            // Animated Systolic Line (Normal - Green)
                            drawPath(
                                path = sysAnimatedPath,
                                color = Color(0xFF0F9F7A),
                                style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )

                            // Animated Diastolic Line (Optimal - Cyan)
                            drawPath(
                                path = diaAnimatedPath,
                                color = Color(0xFF38BDF8),
                                style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                            Text(text = day, fontSize = 12.sp, color = Color(0xFF9CA3AF))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Readings History (With Color-Coded Normal - Green, Med - Yellow, High - Red)
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

                    BpHistoryRow("120/80 mmHg", "Oct 15 - 9:00 AM", "Normal", Color(0xFF0F9F7A), Color(0xFFE6F4EA))
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))
                    BpHistoryRow("128/83 mmHg", "Oct 14 - 9:15 AM", "Elevated (Med)", Color(0xFFF59E0B), Color(0xFFFEF3C7))
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))
                    BpHistoryRow("142/92 mmHg", "Oct 12 - 8:10 AM", "High (Alert)", Color(0xFFEF4444), Color(0xFFFEE2E2))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Add Reading Button
        Button(
            onClick = onAddReadingClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
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
    }
}

@Composable
private fun BpHistoryRow(
    reading: String,
    time: String,
    status: String,
    textColor: Color,
    bgColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = reading, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text(text = time, fontSize = 12.sp, color = Color(0xFF9CA3AF), modifier = Modifier.padding(top = 2.dp))
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(bgColor)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = status,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
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
fun BloodPressureDetailScreenPreview() {
    VitalsyncTheme {
        BloodPressureDetailScreen()
    }
}
