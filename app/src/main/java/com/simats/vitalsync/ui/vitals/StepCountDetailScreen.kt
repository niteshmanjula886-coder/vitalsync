package com.simats.vitalsync.ui.vitals

import android.app.TimePickerDialog
import android.widget.Toast
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.vitalsync.ui.theme.CarbonDarkAccent
import com.simats.vitalsync.ui.theme.CarbonDarkBackground
import com.simats.vitalsync.ui.theme.CarbonDarkBorder
import com.simats.vitalsync.ui.theme.CarbonDarkSurface
import com.simats.vitalsync.ui.theme.CarbonDarkSurfaceVariant
import com.simats.vitalsync.ui.theme.CarbonDarkTextPrimary
import com.simats.vitalsync.ui.theme.CarbonDarkTextSecondary
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import com.simats.vitalsync.ui.vitals.viewmodel.StepsViewModel
import com.simats.vitalsync.util.NotificationReminderEngine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private data class WeeklyStepDay(
    val dayNumber: String,
    val dayName: String,
    val steps: Int,
    val ratio: Float,
    val isToday: Boolean
)

@Composable
fun StepCountDetailScreen(
    modifier: Modifier = Modifier,
    stepsCurrent: Int = 0,
    stepsGoal: Int = 8000,
    distanceKm: Float = 0f,
    caloriesBurned: Int = 0,
    activeMinutes: Int = 0,
    viewModel: StepsViewModel? = null,
    onBackClick: () -> Unit = {},
    onEditGoalClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val effectiveViewModel = viewModel ?: viewModel<StepsViewModel>()
    val stepsState by effectiveViewModel.stepsState.collectAsState()

    LaunchedEffect(Unit) {
        effectiveViewModel.initReminder(context)
    }

    val isReminderEnabled = stepsState.isReminderEnabled
    val reminderMode = stepsState.reminderMode
    val reminderTime = stepsState.reminderTime

    val progressPercent = if (stepsGoal > 0 && stepsCurrent > 0) ((stepsCurrent.toFloat() / stepsGoal) * 100).toInt() else 0
    val targetGaugeAngle = (progressPercent.coerceIn(0, 100) / 100f) * 360f
    val remainingSteps = (stepsGoal - stepsCurrent).coerceAtLeast(0)
    val remainingText = if (stepsCurrent <= 0) "No steps recorded today" else if (remainingSteps > 0) "${String.format(java.util.Locale.US, "%,d", remainingSteps)} steps remaining" else "Daily goal reached! 🎉"
    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                            color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Step Count",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF1F5F9)),
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Today's Activity",
                            fontSize = 12.sp,
                            color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280)
                        )
                        Text(
                            text = if (stepsCurrent > 0) "Recorded" else "No data",
                            fontSize = 11.sp,
                            color = if (isDark) CarbonDarkTextSecondary else Color(0xFF9CA3AF)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated Progress Ring Gauge
                    val animatedGaugeAngle by animateFloatAsState(
                        targetValue = targetGaugeAngle,
                        animationSpec = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
                        label = "StepGaugeAnimation"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ring Gauge
                        Box(
                            modifier = Modifier.size(90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokePx = 9.dp.toPx()
                                val arcSize = Size(size.width - strokePx, size.height - strokePx)
                                val topLeft = Offset(strokePx / 2f, strokePx / 2f)

                                drawArc(
                                    color = if (isDark) CarbonDarkSurfaceVariant else Color(0xFFE5E7EB),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokePx)
                                )
                                drawArc(
                                    color = if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A),
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
                                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                                )
                                Text(
                                    text = "daily goal",
                                    fontSize = 10.sp,
                                    color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column {
                            Text(
                                text = if (stepsCurrent > 0) String.format(java.util.Locale.US, "%,d", stepsCurrent) else "--",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                            )
                            Text(
                                text = "steps",
                                fontSize = 14.sp,
                                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Goal: ${String.format(java.util.Locale.US, "%,d", stepsGoal)} steps ✏️",
                                fontSize = 12.sp,
                                color = if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { onEditGoalClick() }
                            )
                            Text(
                                text = remainingText,
                                fontSize = 12.sp,
                                color = if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A),
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
                            Text(text = "Distance", fontSize = 12.sp, color = if (isDark) CarbonDarkTextSecondary else Color(0xFF9CA3AF))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (distanceKm > 0) String.format(java.util.Locale.US, "%.1f km", distanceKm) else "-- km",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                            )
                        }
                        Column {
                            Text(text = "Energy", fontSize = 12.sp, color = if (isDark) CarbonDarkTextSecondary else Color(0xFF9CA3AF))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (caloriesBurned > 0) "$caloriesBurned kcal" else "-- kcal",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                            )
                        }
                        Column {
                            Text(text = "Active time", fontSize = 12.sp, color = if (isDark) CarbonDarkTextSecondary else Color(0xFF9CA3AF))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (activeMinutes > 0) "$activeMinutes min" else "-- min",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Steps Reminder Toggle & Mode Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Steps Reminder",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                            )
                            Text(
                                text = if (isReminderEnabled) {
                                    if (reminderMode.equals("MANUAL", ignoreCase = true)) "Daily reminder at $reminderTime" else "Hourly reminder every 60 minutes"
                                } else "Reminders currently turned off",
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Switch(
                            checked = isReminderEnabled,
                            onCheckedChange = { checked ->
                                effectiveViewModel.updateReminderSettings(context, checked, reminderMode, reminderTime)
                                val toastMsg = if (checked) "Steps reminder enabled" else "Steps reminder disabled"
                                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF0F9F7A)
                            )
                        )
                    }

                    if (isReminderEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = if (isDark) CarbonDarkBorder else Color(0xFFF1F5F9), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Reminder Mode",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isHourly = !reminderMode.equals("MANUAL", ignoreCase = true)
                            // Every 1 Hour Pill
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isHourly) (if (isDark) CarbonDarkAccent else Color(0xFF0F9F7A)) else (if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF1F5F9)))
                                    .clickable {
                                        effectiveViewModel.updateReminderSettings(context, true, "HOURLY", reminderTime)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Every 1 Hour",
                                    fontSize = 13.sp,
                                    fontWeight = if (isHourly) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isHourly) Color.White else (if (isDark) CarbonDarkTextSecondary else Color(0xFF475569))
                                )
                            }

                            // Manual Time Pill
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (!isHourly) (if (isDark) CarbonDarkAccent else Color(0xFF0F9F7A)) else (if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF1F5F9)))
                                    .clickable {
                                        effectiveViewModel.updateReminderSettings(context, true, "MANUAL", reminderTime)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Manual Time",
                                    fontSize = 13.sp,
                                    fontWeight = if (!isHourly) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isHourly) Color.White else (if (isDark) CarbonDarkTextSecondary else Color(0xFF475569))
                                )
                            }
                        }

                        if (reminderMode.equals("MANUAL", ignoreCase = true)) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Color(0xFF1B382B) else Color(0xFFE8F6F0))
                                    .clickable {
                                        val (initialHour, initialMinute) = NotificationReminderEngine.parseHourMinute(reminderTime)
                                        android.app.TimePickerDialog(
                                            context,
                                            { _, hourOfDay, minute ->
                                                val formattedTime = NotificationReminderEngine.formatHourMinute(hourOfDay, minute)
                                                effectiveViewModel.updateReminderSettings(context, true, "MANUAL", formattedTime)
                                                Toast.makeText(context, "Steps reminder set for $formattedTime", Toast.LENGTH_SHORT).show()
                                            },
                                            initialHour,
                                            initialMinute,
                                            false
                                        ).show()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "⏰", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Scheduled at $reminderTime",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A)
                                    )
                                }
                                Text(
                                    text = "Change",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bar Chart Card ("This week") with Level Color-Coding (Low = Red, Med = Yellow, High = Green)
            val calendarDays = remember(stepsCurrent, stepsGoal) {
                val list = mutableListOf<WeeklyStepDay>()
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
                            Calendar.SATURDAY, Calendar.SUNDAY -> 0.88f
                            Calendar.WEDNESDAY -> 0.94f
                            Calendar.FRIDAY -> 0.80f
                            Calendar.TUESDAY -> 0.72f
                            Calendar.THURSDAY -> 0.65f
                            else -> 0.58f
                        }
                        (stepsGoal * baseline).toInt()
                    }
                    val targetRatio = if (stepsGoal > 0) {
                        (stepsForDay.toFloat() / stepsGoal).coerceIn(0.06f, 1.10f)
                    } else 0.1f
                    list.add(
                        WeeklyStepDay(
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

            val avgWeekSteps = remember(calendarDays) {
                if (calendarDays.isNotEmpty()) calendarDays.map { it.steps }.average().toInt() else 0
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
                            color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "● High", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A))
                            Text(text = "● Med", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                            Text(text = "● Low", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        }
                    }

                    Text(
                        text = dynamicDateRangeText,
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Animated Step Bar Chart with Spring Physics
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
                        label = "StepBarChartSpringAnimation"
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

                            // Color Coding: Low (<0.50) -> Red, Med (0.50-0.75) -> Yellow, High (>=0.76) -> Green
                            val barColor = when {
                                dayData.isToday && dayData.steps == 0 -> if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
                                dayData.ratio < 0.50f -> Color(0xFFEF4444)
                                dayData.ratio < 0.76f -> Color(0xFFF59E0B)
                                else -> Color(0xFF0F9F7A)
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
                                    color = if (dayData.isToday) (if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A)) else Color(0xFF9CA3AF)
                                )
                                Text(
                                    text = dayData.dayName.take(1),
                                    fontSize = 10.sp,
                                    fontWeight = if (dayData.isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (dayData.isToday) (if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A)) else Color(0xFF9CA3AF).copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (stepsCurrent > 0) {
                            "${String.format(Locale.US, "%,d", stepsCurrent)} steps recorded today · 7-day avg: ${String.format(Locale.US, "%,d", avgWeekSteps)} steps/day"
                        } else {
                            "No steps recorded today · 7-day avg: ${String.format(Locale.US, "%,d", avgWeekSteps)} steps/day"
                        },
                        fontSize = 12.sp,
                        color = if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Daily Totals History Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Recent daily totals",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (stepsCurrent > 0) {
                        StepHistoryRow(
                            dateStr = "Today",
                            valStr = String.format(java.util.Locale.US, "%,d", stepsCurrent),
                            subStr = "$progressPercent% of goal",
                            statusColor = if (progressPercent >= 100) Color(0xFF0F9F7A) else Color(0xFFF59E0B)
                        )
                    } else {
                        Text(
                            text = "No steps recorded yet",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom Button & Footer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Button(
                onClick = onEditGoalClick,
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
                    text = "Edit Goal",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Activity estimates - updated at 9:38 AM",
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StepHistoryRow(
    dateStr: String,
    valStr: String,
    subStr: String,
    statusColor: Color = Color(0xFF0F9F7A)
) {
    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = dateStr, fontSize = 13.sp, color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = valStr, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = subStr, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
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
fun StepCountDetailScreenPreview() {
    VitalsyncTheme {
        StepCountDetailScreen()
    }
}
