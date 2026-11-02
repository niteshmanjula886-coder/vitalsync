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
import com.simats.vitalsync.data.FitnessRepository
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.sin

@Composable
fun CyclingTrackingScreen(
    modifier: Modifier = Modifier,
    targetGoalTab: String = "Distance",
    targetMinutes: Int = 30,
    targetDistanceKm: Float = 10.0f,
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

    val formattedDynamicTime = FitnessRepository.formatDurationDynamic(trackingSeconds)

    // Live Real-Time Cycling Distance & Metrics Calculations (~20.0 km/h speed)
    val liveDistanceKm = trackingSeconds * 0.00555f
    val liveCalories = (liveDistanceKm * 32f).toInt()
    val liveCurrentSpeedKmH = if (isTimerRunning) {
        (20.0f + sin(trackingSeconds * 0.1f) * 2.2f).coerceAtLeast(0f)
    } else {
        0.0f
    }

    // Live Progress Column Ratio Calculation
    val targetRatio = if (targetGoalTab == "Distance") {
        if (targetDistanceKm > 0f) (liveDistanceKm / targetDistanceKm).coerceIn(0f, 1f) else 0f
    } else {
        if (targetMinutes > 0) ((trackingSeconds / 60f) / targetMinutes.toFloat()).coerceIn(0f, 1f) else 0f
    }

    val animatedGoalProgress by animateFloatAsState(
        targetValue = targetRatio,
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

            // Dual Live Header: Timer & Current Speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = formattedDynamicTime, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Text(text = "Elapsed Time", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = String.format(Locale.US, "%.1f", liveCurrentSpeedKmH), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A))
                    Text(text = "Current Speed km/h", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Live Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Live Distance", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    Text(text = String.format(Locale.US, "%.2f km", liveDistanceKm), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }

                Column {
                    Text(text = "Average Speed", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    Text(text = "20 km/h", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }

                Column {
                    Text(text = "Active Calories", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    Text(text = "$liveCalories kcal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
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
                            val remSecs = (targetMinutes * 60 - trackingSeconds).coerceAtLeast(0)
                            "$formattedDynamicTime of ${targetMinutes}m · ${FitnessRepository.formatDurationDynamic(remSecs)} remaining"
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
                            Text(text = "20.0 km/h", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Single Primary Finish Button at Bottom
        Button(
            onClick = {
                onFinishTrackingWithData?.invoke(trackingSeconds, liveDistanceKm, liveCalories, "2:40", 138, 0)
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
