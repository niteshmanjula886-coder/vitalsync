
package com.simats.vitalsync.ui.activity

import androidx.activity.compose.BackHandler

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.data.FitnessRepository
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun WorkoutTrackingScreen(
    modifier: Modifier = Modifier,
    targetWorkoutMinutes: Int = 45,
    onBackClick: () -> Unit = {},
    onFinishTracking: () -> Unit = {},
    onFinishTrackingWithData: ((durationSeconds: Int, distanceKm: Float, caloriesBurned: Int, avgPace: String, avgHeartRate: Int?, steps: Int) -> Unit)? = null
) {
    // Executable Live Workout Session Timer State
    BackHandler(enabled = true) {
        onBackClick()
    }

    var isWorkoutStarted by remember { mutableStateOf(false) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var workoutSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000L)
            workoutSeconds++
        }
    }

    val formattedDynamicTime = FitnessRepository.formatDurationDynamic(workoutSeconds)
    val totalTargetSecs = targetWorkoutMinutes * 60
    val progressRatio = if (totalTargetSecs > 0) (workoutSeconds.toFloat() / totalTargetSecs.toFloat()).coerceIn(0f, 1f) else 0f

    val animatedProgressAngle by animateFloatAsState(
        targetValue = progressRatio * 360f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "WorkoutCircularProgress"
    )

    // Executable Rest Countdown Timer
    var restCountdownSecs by remember { mutableIntStateOf(30) }
    var isResting by remember { mutableStateOf(false) }

    LaunchedEffect(isResting, restCountdownSecs) {
        if (isResting && restCountdownSecs > 0) {
            delay(1000L)
            restCountdownSecs--
            if (restCountdownSecs == 0) {
                isResting = false
            }
        }
    }

    val estimatedCaloriesBurned = (workoutSeconds * 0.12f).toInt()

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
                        text = "Workout Tracker",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                // Status Badge Indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isTimerRunning -> Color(0xFFE6F4EA)
                                isWorkoutStarted -> Color(0xFFFEF3C7)
                                else -> Color(0xFFF1F5F9)
                            }
                        )
                        .clickable {
                            if (isWorkoutStarted) {
                                isTimerRunning = !isTimerRunning
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            isTimerRunning -> "• Active Workout"
                            isWorkoutStarted -> "⏸ Paused"
                            else -> "Ready to Start"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isTimerRunning -> Color(0xFF0F9F7A)
                            isWorkoutStarted -> Color(0xFFD97706)
                            else -> Color(0xFF64748B)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Live Circular Timer Progress Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🏋️ Full Body Workout", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7280))

                    Spacer(modifier = Modifier.height(16.dp))

                    // Circular Progress Ring & Live Timer Display
                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidthPx = 10.dp.toPx()
                            val radius = (size.minDimension - strokeWidthPx) / 2f
                            val centerOffset = Offset(size.width / 2f, size.height / 2f)

                            // Outer Background Track Ring
                            drawCircle(
                                color = Color(0xFFE2E8F0),
                                radius = radius,
                                center = centerOffset,
                                style = Stroke(width = strokeWidthPx)
                            )

                            // Active Circular Arc Fill Ring
                            if (animatedProgressAngle > 0f) {
                                drawArc(
                                    color = Color(0xFF0F9F7A),
                                    startAngle = -90f,
                                    sweepAngle = animatedProgressAngle,
                                    useCenter = false,
                                    topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f),
                                    size = Size(size.width - strokeWidthPx, size.height - strokeWidthPx),
                                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formattedDynamicTime,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${(progressRatio * 100).toInt()}% of $targetWorkoutMinutes min",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F9F7A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Live Stats Summary inside Timer Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "CALORIES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
                            Text(text = "$estimatedCaloriesBurned kcal", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "ESTIMATED HR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
                            Text(text = if (isTimerRunning) "138 BPM" else "-- BPM", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Exercise Plan Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Workout Exercises", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                Text(text = "4 exercises Â· 12 sets", fontSize = 12.sp, color = Color(0xFF6B7280))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Current Exercise Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Current exercise Â· 2/4", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Dumbbell shoulder press", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))

                    Spacer(modifier = Modifier.height(14.dp))

                    SetRow(setNum = "1", reps = "12", weight = "10kg", isDone = true)
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 8.dp))
                    SetRow(setNum = "2", reps = "10", weight = "12kg", isDone = true)
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 8.dp))
                    SetRow(setNum = "3", reps = "10", weight = "12kg", isDone = false)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rest & Recover Timer Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F6F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F9F7A))
                                .clickable {
                                    if (!isResting && restCountdownSecs == 0) {
                                        restCountdownSecs = 30
                                    }
                                    isResting = !isResting
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = if (isResting) "⏸" else "▶", fontSize = 18.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(text = "Rest & recover", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                            Text(text = "Next set: 3 Â· 10 reps @ 12 kg", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (restCountdownSecs > 0) "0:${String.format(Locale.US, "%02d", restCountdownSecs)}" else "Ready!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F9F7A)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isResting) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                                .clickable {
                                    if (!isResting && restCountdownSecs == 0) {
                                        restCountdownSecs = 30
                                    }
                                    isResting = !isResting
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isResting) "Pause" else "Play",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isResting) Color(0xFF15803D) else Color(0xFFD97706)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Primary Action Controls (Start / Pause / Finish Session)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            if (!isWorkoutStarted) {
                // START WORKOUT Primary Action Button
                Button(
                    onClick = {
                        isWorkoutStarted = true
                        isTimerRunning = true
                    },
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
                        text = "▶ START WORKOUT",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Active Workout Controls: PAUSE / RESUME and FINISH
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { isTimerRunning = !isTimerRunning },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, VitalTealPrimary)
                    ) {
                        Text(
                            text = if (isTimerRunning) "⏸ PAUSE" else "▶ RESUME",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = VitalTealDark
                        )
                    }

                    Button(
                        onClick = {
                            onFinishTrackingWithData?.invoke(workoutSeconds, 0.0f, estimatedCaloriesBurned, "N/A", 138, 0)
                            onFinishTracking()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VitalTealDark,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "⏹ FINISH",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SetRow(setNum: String, reps: String, weight: String, isDone: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = setNum, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "$reps reps", fontSize = 13.sp, color = Color(0xFF374151))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = weight, fontSize = 13.sp, color = Color(0xFF6B7280))
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isDone) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isDone) "✓ Done" else "Pending",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDone) Color(0xFF15803D) else Color(0xFF64748B)
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
fun WorkoutTrackingScreenPreview() {
    VitalsyncTheme {
        WorkoutTrackingScreen()
    }
}

