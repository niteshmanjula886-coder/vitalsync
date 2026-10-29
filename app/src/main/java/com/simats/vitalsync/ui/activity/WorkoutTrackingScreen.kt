package com.simats.vitalsync.ui.activity

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
fun WorkoutTrackingScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onFinishTracking: () -> Unit = {},
    onFinishTrackingWithData: ((durationSeconds: Int, distanceKm: Float, caloriesBurned: Int, avgPace: String, avgHeartRate: Int?, steps: Int) -> Unit)? = null
) {
    // Executable Live Workout Timer State
    var workoutSeconds by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000L)
            workoutSeconds++
        }
    }

    val timerMinutes = workoutSeconds / 60
    val timerSecs = workoutSeconds % 60
    val formattedTime = String.format(Locale.US, "%02d:%02d", timerMinutes, timerSecs)

    // Executable Rest Countdown Timer
    var restCountdownSecs by remember { mutableIntStateOf(30) }
    var isResting by remember { mutableStateOf(false) } // Strictly manual Play/Pause control

    LaunchedEffect(isResting, restCountdownSecs) {
        if (isResting && restCountdownSecs > 0) {
            delay(1000L)
            restCountdownSecs--
            if (restCountdownSecs == 0) {
                isResting = false
            }
        }
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
                        text = "Workout",
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

            // Timer Banner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "🏋️ Strength training", fontSize = 13.sp, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = formattedTime, fontSize = 52.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                Text(text = "Elapsed time", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Exercise Plan Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Full body", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                Text(text = "4 exercises · 12 sets", fontSize = 12.sp, color = Color(0xFF6B7280))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current Exercise Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Current exercise · 2/4", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A))
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
                            Text(text = "Next set: 3 · 10 reps @ 12 kg", fontSize = 12.sp, color = Color(0xFF6B7280))
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

        // Single Primary Finish Button at Bottom
        Button(
            onClick = {
                val duration = if (workoutSeconds > 0) workoutSeconds else 2700
                onFinishTrackingWithData?.invoke(duration, 0.0f, 285, "N/A", 138, 0)
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
                text = "Finish Workout Session",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
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
