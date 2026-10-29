package com.simats.vitalsync.ui.activity

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun RunningTrackingScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onFinishTracking: () -> Unit = {},
    onFinishTrackingWithData: ((durationSeconds: Int, distanceKm: Float, caloriesBurned: Int, avgPace: String, avgHeartRate: Int?, steps: Int) -> Unit)? = null
) {
    // Executable Live Timer State
    var trackingSeconds by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000L)
            trackingSeconds++
        }
    }

    val timerMinutes = trackingSeconds / 60
    val timerSecs = trackingSeconds % 60
    val formattedTime = String.format(Locale.US, "%02d:%02d", timerMinutes, timerSecs)

    // Animated Run Goal Fill State
    var targetRunGoalRatio by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        targetRunGoalRatio = 0.85f
    }
    val animatedRunGoalRatio by animateFloatAsState(
        targetValue = targetRunGoalRatio,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "RunGoalAnimation"
    )

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
                        text = "Running",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                // Interactive Status / Pause Toggle Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isTimerRunning) Color(0xFFE6F4EA) else Color(0xFFFEF3C7))
                        .clickable { isTimerRunning = !isTimerRunning }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isTimerRunning) "• In progress" else "⏸ Paused",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTimerRunning) Color(0xFF0F9F7A) else Color(0xFFD97706)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dual Timer Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = formattedTime, fontSize = 38.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Text(text = "Elapsed time", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "6:00", fontSize = 38.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A))
                    Text(text = "Avg pace min/km", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Distance", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    Text(text = "5.0 km", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }

                Column {
                    Text(text = "Calories", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    Text(text = "315 kcal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }

                Column {
                    Text(text = "Heart rate", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    Text(text = "148 bpm", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Pace Trend Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Pace trend", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Text(text = "min/km", fontSize = 12.sp, color = Color(0xFF0F9F7A))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated Pace Trend Line
                    var targetTrendProgress by remember { mutableFloatStateOf(0f) }
                    LaunchedEffect(Unit) {
                        targetTrendProgress = 1f
                    }
                    val trendProgress by animateFloatAsState(
                        targetValue = targetTrendProgress,
                        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                        label = "PaceTrendAnimation"
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                    ) {
                        val w = size.width
                        val h = size.height

                        val fullPath = Path().apply {
                            moveTo(0f, h * 0.6f)
                            cubicTo(w * 0.1f, h * 0.5f, w * 0.15f, h * 0.4f, w * 0.2f, h * 0.4f)
                            cubicTo(w * 0.3f, h * 0.35f, w * 0.35f, h * 0.3f, w * 0.4f, h * 0.3f)
                            cubicTo(w * 0.5f, h * 0.4f, w * 0.55f, h * 0.5f, w * 0.6f, h * 0.5f)
                            cubicTo(w * 0.7f, h * 0.4f, w * 0.75f, h * 0.35f, w * 0.8f, h * 0.35f)
                            cubicTo(w * 0.9f, h * 0.4f, w * 0.95f, h * 0.45f, w, h * 0.45f)
                        }

                        val pathMeasure = PathMeasure().apply { setPath(fullPath, false) }
                        val animatedPath = Path()
                        pathMeasure.getSegment(0f, pathMeasure.length * trendProgress, animatedPath, true)

                        if (trendProgress > 0f) {
                            val fillPath = Path().apply {
                                addPath(animatedPath)
                                lineTo(w * trendProgress, h)
                                lineTo(0f, h)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0x350F9F7A), Color(0x000F9F7A))
                                )
                            )

                            drawPath(
                                path = animatedPath,
                                color = Color(0xFF0F9F7A),
                                style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Run Distance Goal Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Run distance goal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Text(text = "${(animatedRunGoalRatio * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE5E7EB))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedRunGoalRatio)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF0F9F7A))
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "5.0 / 6.0 km · 1.0 km remaining",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Kilometer Splits Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Kilometer splits", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))

                    Spacer(modifier = Modifier.height(12.dp))

                    SplitRow(km = "1", pace = "6:01", bpm = "142")
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 8.dp))
                    SplitRow(km = "2", pace = "5:58", bpm = "148")
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 8.dp))
                    SplitRow(km = "3", pace = "6:02", bpm = "150")
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 8.dp))
                    SplitRow(km = "4", pace = "5:58", bpm = "151")
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Single Primary Finish Button at Bottom
        Button(
            onClick = {
                val duration = if (trackingSeconds > 0) trackingSeconds else 1515
                onFinishTrackingWithData?.invoke(duration, 5.0f, 315, "6:00", 148, 5100)
                onFinishTracking()
            },
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
                text = "Finish Running Session",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SplitRow(km: String, pace: String, bpm: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "KM $km", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Text(text = "$pace /km", fontSize = 13.sp, color = Color(0xFF0F9F7A), fontWeight = FontWeight.Medium)
        Text(text = "$bpm bpm", fontSize = 12.sp, color = Color(0xFF6B7280))
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
fun RunningTrackingScreenPreview() {
    VitalsyncTheme {
        RunningTrackingScreen()
    }
}
