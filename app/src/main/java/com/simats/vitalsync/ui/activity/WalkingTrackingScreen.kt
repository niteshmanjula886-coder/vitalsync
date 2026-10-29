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
import androidx.compose.ui.geometry.Offset
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
fun WalkingTrackingScreen(
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

    // Animated Session Step Goal Fill State
    var targetStepGoalRatio by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        targetStepGoalRatio = 0.85f
    }
    val animatedStepGoalRatio by animateFloatAsState(
        targetValue = targetStepGoalRatio,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "WalkingStepGoalAnimation"
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
                        text = "Walking",
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

            // Live Timer Banner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🏃 Outdoor walk", fontSize = 13.sp, color = Color(0xFF6B7280))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formattedTime,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Text(
                    text = "Elapsed time",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Metrics Grid (2x2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Distance", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "3.2 km", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }

                Column {
                    Text(text = "Steps", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "4,280", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Average pace", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "12:30 /km", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }

                Column {
                    Text(text = "Active calories", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "168 kcal", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Route Map Card
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
                        Text(text = "Your route", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Text(text = "3.2 km GPS route", fontSize = 12.sp, color = Color(0xFF0F9F7A))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Smooth Animated Route GPS Path
                    var targetRouteProgress by remember { mutableFloatStateOf(0f) }
                    LaunchedEffect(Unit) {
                        targetRouteProgress = 1f
                    }
                    val routeProgress by animateFloatAsState(
                        targetValue = targetRouteProgress,
                        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                        label = "WalkingRouteAnimation"
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9))
                    ) {
                        val w = size.width
                        val h = size.height

                        val fullRoutePath = Path().apply {
                            moveTo(w * 0.2f, h * 0.8f)
                            lineTo(w * 0.8f, h * 0.8f)
                            lineTo(w * 0.8f, h * 0.3f)
                            lineTo(w * 0.3f, h * 0.3f)
                            lineTo(w * 0.3f, h * 0.6f)
                        }

                        val pathMeasure = PathMeasure().apply { setPath(fullRoutePath, false) }
                        val animatedRoutePath = Path()
                        pathMeasure.getSegment(0f, pathMeasure.length * routeProgress, animatedRoutePath, true)

                        if (routeProgress > 0f) {
                            drawPath(
                                path = animatedRoutePath,
                                color = Color(0xFF0F9F7A),
                                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                            drawCircle(color = Color(0xFF0F9F7A), radius = 6.dp.toPx(), center = Offset(w * 0.3f, h * 0.6f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Step Goal Progress Bar Card
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
                        Text(text = "Session step-goal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Text(text = "${(animatedStepGoalRatio * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A))
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
                                .fillMaxWidth(animatedStepGoalRatio)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF0F9F7A))
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "4,280 of 5,000 steps · 720 steps remaining",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Single Primary Finish Button at Bottom
        Button(
            onClick = {
                val duration = if (trackingSeconds > 0) trackingSeconds else 1845
                onFinishTrackingWithData?.invoke(duration, 3.2f, 168, "12:30", 115, 4280)
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
                text = "Finish Walking Session",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun WalkingTrackingScreenPreview() {
    VitalsyncTheme {
        WalkingTrackingScreen()
    }
}
