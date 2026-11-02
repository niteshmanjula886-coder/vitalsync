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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.data.sensor.WorkoutTrackingService
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import java.util.Locale

@Composable
fun CyclingTrackingScreen(
    modifier: Modifier = Modifier,
    targetGoalTab: String = "Distance",
    targetMinutes: Int = 30,
    targetDistanceKm: Float = 10.0f,
    userWeightKg: Float = 70.0f,
    onBackClick: () -> Unit = {},
    onFinishTracking: () -> Unit = {},
    onFinishTrackingWithData: ((durationSeconds: Int, distanceKm: Float, caloriesBurned: Int, avgPace: String, avgHeartRate: Int?, steps: Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val workoutEngine = remember { WorkoutTrackingService.getInstance(context) }
    val telemetry by workoutEngine.telemetry.collectAsState()

    LaunchedEffect(targetGoalTab, targetMinutes, targetDistanceKm, userWeightKg) {
        workoutEngine.startWorkout(
            activityType = "Cycling",
            targetType = targetGoalTab,
            targetMinutes = targetMinutes,
            targetDistanceKm = targetDistanceKm,
            userWeightKg = userWeightKg
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            workoutEngine.stopWorkout()
        }
    }

    val trackingSeconds = telemetry.durationSeconds
    val formattedDynamicTime = telemetry.formattedDuration
    val liveDistanceKm = telemetry.distanceKm
    val liveCalories = telemetry.caloriesBurned
    val liveCurrentSpeedKmH = telemetry.currentSpeedKmH
    val liveAvgSpeedKmH = telemetry.avgSpeedKmH

    val animatedGoalProgress by animateFloatAsState(
        targetValue = telemetry.progressRatio,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "CyclingGoalProgressAnimation"
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
                        text = "Cycling",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                // Interactive Status / Pause Toggle Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!telemetry.isPaused) Color(0xFFE6F4EA) else Color(0xFFFEF3C7))
                        .clickable { 
                            if (telemetry.isPaused) workoutEngine.resumeWorkout() else workoutEngine.pauseWorkout() 
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (!telemetry.isPaused) "• In progress" else "⏸ Paused",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!telemetry.isPaused) Color(0xFF0F9F7A) else Color(0xFFD97706)
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
                    Text(text = "🚴 Outdoor Cycling", fontSize = 13.sp, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE8F6F0))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (targetGoalTab == "Distance") "Target: ${String.format(Locale.US, "%.1f", targetDistanceKm)} km" else "Target: $targetMinutes min",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F9F7A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formattedDynamicTime,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Text(
                    text = "Elapsed Time",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (telemetry.isPaused) Color(0xFFFEF3C7) else Color(0xFFE8F6F0))
                        .clickable {
                            if (telemetry.isPaused) workoutEngine.resumeWorkout() else workoutEngine.pauseWorkout()
                        }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (telemetry.isPaused) "▶ Resume" else "⏸ Pause",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (telemetry.isPaused) Color(0xFFD97706) else Color(0xFF0F9F7A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Live Metrics Grid (2x2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Live Distance", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = String.format(Locale.US, "%.2f km", liveDistanceKm), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Current Speed", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = String.format(Locale.US, "%.1f km/h", liveCurrentSpeedKmH), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Average Speed", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = String.format(Locale.US, "%.1f km/h", liveAvgSpeedKmH), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Active Calories", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "$liveCalories kcal", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Live Target Tracking Progress Column Card
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
                        Text(
                            text = if (targetGoalTab == "Distance") "Live Distance Progress" else "Live Time Progress",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "${(animatedGoalProgress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F9F7A)
                        )
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
                                .fillMaxWidth(animatedGoalProgress)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF0F9F7A))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (targetGoalTab == "Distance") {
                            val remaining = (targetDistanceKm - liveDistanceKm).coerceAtLeast(0f)
                            "${String.format(Locale.US, "%.2f", liveDistanceKm)} of ${String.format(Locale.US, "%.1f", targetDistanceKm)} km · ${String.format(Locale.US, "%.2f", remaining)} km remaining"
                        } else {
                            "$formattedDynamicTime of ${targetMinutes}m · ${telemetry.remainingFormatted}"
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // WORKOUT RESULT STAT CARD UNDER LIVE PROGRESS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F6F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Session Performance Result", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F9F7A))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "Live Result", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "LIVE TIME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A), letterSpacing = 0.8.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = formattedDynamicTime, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }

                        Column {
                            Text(text = "ENERGY BURN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A), letterSpacing = 0.8.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "$liveCalories kcal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "AVG SPEED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A), letterSpacing = 0.8.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = String.format(Locale.US, "%.1f km/h", liveAvgSpeedKmH), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Single Primary Finish Button at Bottom
        Button(
            onClick = {
                val completed = workoutEngine.finishWorkout()
                onFinishTrackingWithData?.invoke(
                    completed.durationSeconds,
                    completed.distanceKm,
                    completed.caloriesBurned,
                    completed.avgPace,
                    138,
                    completed.steps
                )
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
                text = "Finish Cycling Session",
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
fun CyclingTrackingScreenPreview() {
    VitalsyncTheme {
        CyclingTrackingScreen()
    }
}
