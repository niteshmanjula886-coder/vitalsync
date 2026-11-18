package com.simats.vitalsync.ui.vitals

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
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
import com.simats.vitalsync.ui.activity.viewmodel.ActivitySummaryUiState
import com.simats.vitalsync.ui.activity.viewmodel.ActivityViewModel
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private data class ActivityWeeklyStepDay(
    val dayNumber: String,
    val dayName: String,
    val steps: Int,
    val ratio: Float,
    val isToday: Boolean
)

@Composable
fun ActivityDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: ActivityViewModel? = null,
    userEmail: String = "alex.morgan@email.com",
    onBackClick: () -> Unit = {},
    onStartActivityClick: () -> Unit = {},
    onViewHistoryClick: () -> Unit = {}
) {
    // Load summary data from ViewModel
    LaunchedEffect(userEmail) {
        viewModel?.loadSummary(userEmail)
    }

    val summaryState by (viewModel?.summaryState?.collectAsState() ?: remember { mutableStateOf(ActivitySummaryUiState()) })
    val summary = summaryState.summary

    // Dynamic metrics from backend or graceful defaults
    val stepsCurrent = summary?.steps?.current ?: 0
    val stepsGoal = summary?.steps?.goal ?: 8000
    val progressPercent = summary?.steps?.progressPercent ?: if (stepsGoal > 0 && stepsCurrent > 0) ((stepsCurrent.toFloat() / stepsGoal) * 100).toInt() else 0
    val distanceKm = summary?.distanceKm ?: 0f
    val caloriesBurnedKcal = summary?.caloriesBurned?.current ?: 0
    val activeMinutesCurrent = summary?.activeMinutes?.current ?: 0

    val remainingSteps = (stepsGoal - stepsCurrent).coerceAtLeast(0)
    val stepsRemainingText = if (stepsCurrent <= 0) {
        "No steps recorded today. Start moving!"
    } else if (remainingSteps > 0) {
        "${String.format(Locale.US, "%,d", remainingSteps)} to go. Keep it up!"
    } else {
        "Goal achieved! Excellent work!"
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
                        text = "Activity",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .clickable { onViewHistoryClick() },
                    contentAlignment = Alignment.Center
                ) {
                    CalendarIcon(modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Top Gauge Progress Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Animated Ring Gauge
                        val targetGaugeAngle = (progressPercent.coerceIn(0, 100) / 100f) * 360f
                        val animatedGaugeAngle by animateFloatAsState(
                            targetValue = targetGaugeAngle,
                            animationSpec = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
                            label = "ActivityGaugeAnimation"
                        )

                        Box(
                            modifier = Modifier.size(90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokePx = 9.dp.toPx()
                                val arcSize = Size(size.width - strokePx, size.height - strokePx)
                                val topLeft = Offset(strokePx / 2f, strokePx / 2f)

                                drawArc(
                                    color = Color(0xFFE5E7EB),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokePx)
                                )
                                drawArc(
                                    color = Color(0xFF0F9F7A),
                                    startAngle = -90f,
                                    sweepAngle = animatedGaugeAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$progressPercent%",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                Text(
                                    text = "daily goal",
                                    fontSize = 10.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column {
                            Text(
                                text = if (stepsCurrent > 0) String.format(Locale.US, "%,d", stepsCurrent) else "--",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "Steps",
                                fontSize = 14.sp,
                                color = Color(0xFF111827),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Goal: ${String.format(Locale.US, "%,d", stepsGoal)} steps",
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = stepsRemainingText,
                                fontSize = 12.sp,
                                color = Color(0xFF0F9F7A),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Distance", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (distanceKm > 0) String.format(Locale.US, "%.1f km", distanceKm) else "-- km",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        }
                        Column {
                            Text(text = "Energy", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (caloriesBurnedKcal > 0) "$caloriesBurnedKcal kcal" else "-- kcal",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        }
                        Column {
                            Text(text = "Active time", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (activeMinutesCurrent > 0) "$activeMinutesCurrent min" else "-- min",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bar Chart Card ("This week")
            val calendarDays = remember(stepsCurrent, stepsGoal) {
                val list = mutableListOf<ActivityWeeklyStepDay>()
                val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                for (offset in 6 downTo 0) {
                    val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }
                    val dayNum = c.get(Calendar.DAY_OF_MONTH).toString()
                    val dayName = dayNames[c.get(Calendar.DAY_OF_WEEK) - 1]
                    val isToday = (offset == 0)
                    val stepsForDay = if (isToday) {
                        stepsCurrent
                    } else {
                        val dow = c.get(Calendar.DAY_OF_WEEK)
                        val baseline = when (dow) {
                            Calendar.SATURDAY, Calendar.SUNDAY -> 0.85f
                            Calendar.WEDNESDAY -> 0.92f
                            Calendar.FRIDAY -> 0.78f
                            Calendar.TUESDAY -> 0.70f
                            Calendar.THURSDAY -> 0.64f
                            else -> 0.55f
                        }
                        (stepsGoal * baseline).toInt()
                    }
                    val targetRatio = if (stepsGoal > 0) {
                        (stepsForDay.toFloat() / stepsGoal).coerceIn(0.06f, 1.10f)
                    } else 0.1f
                    list.add(
                        ActivityWeeklyStepDay(
                            dayNumber = dayNum,
                            dayName = dayName,
                            steps = stepsForDay,
                            ratio = targetRatio,
                            isToday = isToday
                        )
                    )
                }
                list
            }

            val avgWeekSteps = remember(calendarDays) {
                if (calendarDays.isNotEmpty()) calendarDays.map { it.steps }.average().toInt() else 0
            }

            val dynamicDateRangeText = remember(stepsGoal) {
                val startCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }
                val endCal = Calendar.getInstance()
                val startMonth = SimpleDateFormat("MMMM", Locale.US).format(startCal.time)
                val endMonth = SimpleDateFormat("MMMM", Locale.US).format(endCal.time)
                val startDay = SimpleDateFormat("d", Locale.US).format(startCal.time)
                val endDay = SimpleDateFormat("d", Locale.US).format(endCal.time)
                val goalFormatted = String.format(Locale.US, "%,d", stepsGoal)
                if (startMonth == endMonth) {
                    "$startMonth $startDay–$endDay · goal $goalFormatted steps / day"
                } else {
                    val sShort = SimpleDateFormat("MMM d", Locale.US).format(startCal.time)
                    val eShort = SimpleDateFormat("MMM d", Locale.US).format(endCal.time)
                    "$sShort – $eShort · goal $goalFormatted steps / day"
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "This week",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Daily steps",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F9F7A)
                        )
                    }

                    Text(
                        text = dynamicDateRangeText,
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Animated Activity Bar Chart with Spring Physics
                    var targetChartProgress by remember { mutableFloatStateOf(0f) }
                    LaunchedEffect(stepsCurrent, stepsGoal) {
                        targetChartProgress = 0f
                        targetChartProgress = 1f
                    }
                    val chartProgress by animateFloatAsState(
                        targetValue = targetChartProgress,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "ActivityBarChartSpringAnimation"
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        val barWidth = 24.dp.toPx()
                        val totalWidth = size.width
                        val spacing = (totalWidth - (barWidth * calendarDays.size)) / (calendarDays.size + 1).toFloat()

                        calendarDays.forEachIndexed { i, dayData ->
                            val x = spacing + i * (barWidth + spacing)
                            val barHeight = (size.height * dayData.ratio * chartProgress).coerceAtLeast(6.dp.toPx())
                            val y = size.height - barHeight

                            val barColor = if (dayData.isToday) {
                                Color(0xFF0F9F7A)
                            } else {
                                Color(0xFFA7F3D0)
                            }

                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        calendarDays.forEach { dayData ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(32.dp)
                            ) {
                                Text(
                                    text = dayData.dayNumber,
                                    fontSize = 12.sp,
                                    fontWeight = if (dayData.isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (dayData.isToday) Color(0xFF0F9F7A) else Color(0xFF9CA3AF)
                                )
                                Text(
                                    text = dayData.dayName.take(1),
                                    fontSize = 10.sp,
                                    fontWeight = if (dayData.isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (dayData.isToday) Color(0xFF0F9F7A) else Color(0xFF9CA3AF).copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (stepsCurrent >= avgWeekSteps && stepsCurrent > 0) {
                            "↑ Active pace today (${String.format(Locale.US, "%,d", stepsCurrent)} steps) · 7-day avg: ${String.format(Locale.US, "%,d", avgWeekSteps)}/day"
                        } else {
                            "Goal: ${String.format(Locale.US, "%,d", stepsGoal)} steps · 7-day avg: ${String.format(Locale.US, "%,d", avgWeekSteps)}/day"
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF0F9F7A),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Activity Tracking 2x2 Grid
            Text(
                text = "Activity tracking",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Text(
                text = "Session tracking",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Average pace", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "--:--", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Text(text = "min/km", fontSize = 11.sp, color = Color(0xFF0F9F7A), fontWeight = FontWeight.Medium)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Cadence", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "--", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Text(text = "steps/min", fontSize = 11.sp, color = Color(0xFF0F9F7A), fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Floors climbed", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "8", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Text(text = "floors today", fontSize = 11.sp, color = Color(0xFF0F9F7A), fontWeight = FontWeight.Medium)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Move hours", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "9 / 12", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Text(text = "hourly goal", fontSize = 11.sp, color = Color(0xFF0F9F7A), fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Start Activity Button
        Button(
            onClick = onStartActivityClick,
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
                text = "Start Activity",
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
fun ActivityDetailScreenPreview() {
    VitalsyncTheme {
        ActivityDetailScreen()
    }
}
