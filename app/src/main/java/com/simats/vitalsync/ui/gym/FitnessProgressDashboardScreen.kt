package com.simats.vitalsync.ui.gym

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
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
import com.simats.vitalsync.ui.gym.viewmodel.GymProgressUiState
import com.simats.vitalsync.ui.gym.viewmodel.GymViewModel
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import java.util.Locale

@Composable
fun FitnessProgressDashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: GymViewModel? = null,
    userEmail: String = "alex.morgan@email.com",
    onBackClick: () -> Unit = {}
) {
    val progressState by viewModel?.progressState?.collectAsState() ?: remember { mutableStateOf(GymProgressUiState()) }

    LaunchedEffect(userEmail) {
        viewModel?.loadGymProgress(userEmail)
    }

    val progress = progressState.progress
    val weeklyWorkouts = progress?.weeklyWorkouts ?: 4
    val weeklyTarget = progress?.weeklyTargetWorkouts ?: 5
    val weeklyVolKg: Float = progress?.weeklyVolumeKg ?: 18500f
    val formattedVol = if (weeklyVolKg >= 1000f) {
        String.format(Locale.US, "%.1fk", weeklyVolKg / 1000f)
    } else {
        weeklyVolKg.toInt().toString()
    }
    val calBurned = progress?.totalCaloriesBurned ?: 1420
    val streakDays = progress?.currentStreakDays ?: 3
    val favoriteSplit = progress?.favoriteSplit ?: "Push"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFCFB))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
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
                text = "Fitness & Nutrition Analytics 📈",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (progressState.isLoading && progress == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF0F9F7A),
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Weekly Summary Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProgressMiniCard("Workouts", "$weeklyWorkouts", "this week", Color(0xFF0F9F7A), Modifier.weight(1f))
            ProgressMiniCard("Volume", formattedVol, "total kg", Color(0xFF38BDF8), Modifier.weight(1f))
            ProgressMiniCard("Streak", "$streakDays d", "current streak", Color(0xFF7C3AED), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Weekly Workout Frequency Chart Card
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
                    Column {
                        Text(text = "Weekly Workout Frequency", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Text(text = "$weeklyWorkouts sessions completed", fontSize = 12.sp, color = Color(0xFF6B7280))
                    }
                    val percent = if (weeklyTarget > 0) (weeklyWorkouts * 100 / weeklyTarget).coerceAtMost(100) else 80
                    Text(text = "$percent% Goal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A))
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Smoothly Animated Bar Chart
                var targetBarProgress by remember { mutableFloatStateOf(0f) }
                LaunchedEffect(Unit) {
                    targetBarProgress = 1f
                }
                val barProgress by animateFloatAsState(
                    targetValue = targetBarProgress,
                    animationSpec = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
                    label = "GymFrequencyBarAnimation"
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    val barWidth = 24.dp.toPx()
                    val heights = listOf(0.80f, 0.0f, 0.90f, 0.0f, 0.85f, 0.95f, 0.0f) // Mon, Tue, Wed, Thu, Fri, Sat, Sun
                    val totalWidth = size.width
                    val spacing = (totalWidth - (barWidth * 7)) / 8f

                    heights.forEachIndexed { i, ratio ->
                        val x = spacing + i * (barWidth + spacing)
                        val barHeight = size.height * ratio * barProgress
                        val y = size.height - barHeight
                        val barColor = if (ratio > 0) Color(0xFF0F9F7A) else Color(0xFFE2E8F0)

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, if (ratio > 0) barHeight else size.height * 0.1f),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                        Text(text = day, fontSize = 11.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Volume Progress Line Chart Card
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
                    Column {
                        Text(text = "Training Volume Trend", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Text(text = "Favorite Routine: $favoriteSplit", fontSize = 12.sp, color = Color(0xFF6B7280))
                    }
                    Text(text = "Burned: $calBurned kcal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Smoothly Animated Volume Trend Line
                var targetLineProgress by remember { mutableFloatStateOf(0f) }
                LaunchedEffect(Unit) {
                    targetLineProgress = 1f
                }
                val lineProgress by animateFloatAsState(
                    targetValue = targetLineProgress,
                    animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                    label = "VolumeTrendLineAnimation"
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    val fullPath = Path().apply {
                        moveTo(0f, h * 0.8f)
                        cubicTo(w * 0.1f, h * 0.7f, w * 0.15f, h * 0.6f, w * 0.2f, h * 0.6f)
                        cubicTo(w * 0.3f, h * 0.5f, w * 0.35f, h * 0.4f, w * 0.4f, h * 0.4f)
                        cubicTo(w * 0.5f, h * 0.45f, w * 0.55f, h * 0.5f, w * 0.6f, h * 0.5f)
                        cubicTo(w * 0.7f, h * 0.35f, w * 0.75f, h * 0.2f, w * 0.8f, h * 0.2f)
                        cubicTo(w * 0.9f, h * 0.22f, w * 0.95f, h * 0.25f, w, h * 0.25f)
                    }

                    val pathMeasure = PathMeasure().apply { setPath(fullPath, false) }
                    val animatedPath = Path()
                    pathMeasure.getSegment(0f, pathMeasure.length * lineProgress, animatedPath, true)

                    if (lineProgress > 0f) {
                        val fillPath = Path().apply {
                            addPath(animatedPath)
                            lineTo(w * lineProgress, h)
                            lineTo(0f, h)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0x3538BDF8), Color(0x0038BDF8))
                            )
                        )

                        drawPath(
                            path = animatedPath,
                            color = Color(0xFF38BDF8),
                            style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun ProgressMiniCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 11.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text(text = subtitle, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
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
fun FitnessProgressDashboardScreenPreview() {
    VitalsyncTheme {
        FitnessProgressDashboardScreen()
    }
}
