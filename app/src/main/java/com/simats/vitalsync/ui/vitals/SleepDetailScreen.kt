package com.simats.vitalsync.ui.vitals

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.simats.vitalsync.data.FitnessRepository
import com.simats.vitalsync.data.SleepRecord
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun SleepDetailScreen(
    modifier: Modifier = Modifier,
    targetSleepMinutes: Int = 480, // Default 8 Hours Daily Goal
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { FitnessRepository.getInstance(context) }
    val todayDate = remember { repository.getTodayDateString() }

    // Time Pickers State (Default: Bedtime 10:30 PM [22:30], Wake 06:30 AM [06:30])
    var bedHour by remember { mutableIntStateOf(22) }
    var bedMinute by remember { mutableIntStateOf(30) }

    var wakeHour by remember { mutableIntStateOf(6) }
    var wakeMinute by remember { mutableIntStateOf(30) }

    var isRecordedSaved by remember { mutableStateOf(false) }
    var showSuccessToast by remember { mutableStateOf(false) }

    // Pre-fill fields if previous sleep data exists for today
    LaunchedEffect(todayDate) {
        val existingRecord = repository.getSleepRecord(todayDate)
        if (existingRecord != null) {
            isRecordedSaved = true
            parseFormattedTimeToHourMinute(existingRecord.bedtime)?.let { (h, m) ->
                bedHour = h
                bedMinute = m
            }
            parseFormattedTimeToHourMinute(existingRecord.wakeTime)?.let { (h, m) ->
                wakeHour = h
                wakeMinute = m
            }
        }
    }

    // Automatic Overnight Duration Calculation
    val totalSleepMinutes = remember(bedHour, bedMinute, wakeHour, wakeMinute) {
        val bedTotalMins = bedHour * 60 + bedMinute
        val wakeTotalMins = wakeHour * 60 + wakeMinute
        if (wakeTotalMins >= bedTotalMins) {
            wakeTotalMins - bedTotalMins
        } else {
            (wakeTotalMins + 24 * 60) - bedTotalMins
        }
    }

    val formattedBedtime = remember(bedHour, bedMinute) {
        formatTime12Hour(bedHour, bedMinute)
    }

    val formattedWakeTime = remember(wakeHour, wakeMinute) {
        formatTime12Hour(wakeHour, wakeMinute)
    }

    val durationHours = totalSleepMinutes / 60
    val durationMins = totalSleepMinutes % 60
    val formattedDurationStr = "${durationHours}h ${durationMins}m"

    val progressRatio = (totalSleepMinutes.toFloat() / targetSleepMinutes.toFloat()).coerceIn(0f, 1f)
    val progressPercent = (progressRatio * 100).toInt()

    LaunchedEffect(showSuccessToast) {
        if (showSuccessToast) {
            delay(2500L)
            showSuccessToast = false
        }
    }

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
                    text = "Sleep Tracker",
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

        Spacer(modifier = Modifier.height(16.dp))

        // Confirmation Toast Banner
        AnimatedVisibility(
            visible = showSuccessToast,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFDCFCE7))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌙 Sleep entry saved! $formattedDurationStr recorded ($progressPercent% of 8h goal)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF15803D)
                )
            }
        }

        // Top Sleep Overview & Goal Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🌙 Today's Sleep Summary",
                        fontSize = 13.sp,
                        color = Color(0xFF7C3AED),
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isRecordedSaved) Color(0xFFDCFCE7) else Color(0xFFDDD6FE))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isRecordedSaved) "Logged ✓" else "Calculated",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRecordedSaved) Color(0xFF15803D) else Color(0xFF6D28D9)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = formattedDurationStr,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$progressPercent% of 8h daily target goal",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Daily Sleep Target Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE9D5FF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressRatio)
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Bedtime", fontSize = 11.sp, color = Color(0xFF6B7280))
                        Text(text = formattedBedtime, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    }

                    Column {
                        Text(text = "Wake time", fontSize = 11.sp, color = Color(0xFF6B7280))
                        Text(text = formattedWakeTime, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Manual Entry Time Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "⏱️ Manual Sleep Log & Time Pickers",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Text(
                    text = "Tap to pick Bedtime and Wake time for automatic calculation:",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Bedtime & Wake Time Selector Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bedtime Picker Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF8FAFC))
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        bedHour = h
                                        bedMinute = m
                                    },
                                    bedHour,
                                    bedMinute,
                                    false
                                ).show()
                            }
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🕒", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Bedtime", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = formattedBedtime, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }
                    }

                    // Wake Time Picker Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF8FAFC))
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        wakeHour = h
                                        wakeMinute = m
                                    },
                                    wakeHour,
                                    wakeMinute,
                                    false
                                ).show()
                            }
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "⏰", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Wake-up Time", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = formattedWakeTime, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Calculated Duration Status Line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Calculated Total Sleep:", fontSize = 13.sp, color = Color(0xFF475569))
                        Text(text = "$formattedDurationStr ($totalSleepMinutes mins)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Confirm & Log Sleep Session Button
                Button(
                    onClick = {
                        val record = SleepRecord(
                            bedtime = formattedBedtime,
                            wakeTime = formattedWakeTime,
                            durationMinutes = totalSleepMinutes,
                            sleepScore = 88,
                            dateString = todayDate
                        )
                        repository.saveSleepRecord(record)
                        isRecordedSaved = true
                        showSuccessToast = true
                        Toast.makeText(context, "🌙 Sleep logged: $formattedDurationStr recorded!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VitalTealDark,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "💾 Log Sleep Session", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sleep Stages Bar Chart Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Your sleep stages",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Smoothly Animated Stacked Bar Stages Chart
                var targetProgress by remember { mutableFloatStateOf(0f) }
                LaunchedEffect(Unit) {
                    targetProgress = 1f
                }
                val chartProgress by animateFloatAsState(
                    targetValue = targetProgress,
                    animationSpec = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
                    label = "SleepBarChartAnimation"
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val barWidth = 14.dp.toPx()
                    val totalWidth = size.width
                    val spacing = (totalWidth - (barWidth * 12)) / 13f

                    repeat(12) { i ->
                        val x = spacing + i * (barWidth + spacing)
                        val hDeep = size.height * (0.2f + (i % 3) * 0.1f) * chartProgress
                        val hLight = size.height * (0.35f + (i % 2) * 0.1f) * chartProgress
                        val hRem = size.height * (0.2f) * chartProgress

                        // Deep
                        drawRoundRect(
                            color = Color(0xFF0F9F7A),
                            topLeft = Offset(x, size.height - hDeep),
                            size = Size(barWidth, hDeep),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        // Light
                        drawRoundRect(
                            color = Color(0xFF8B5CF6),
                            topLeft = Offset(x, size.height - hDeep - hLight),
                            size = Size(barWidth, hLight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        // REM
                        drawRoundRect(
                            color = Color(0xFFC084FC),
                            topLeft = Offset(x, size.height - hDeep - hLight - hRem),
                            size = Size(barWidth, hRem),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stage Breakdown List
                val deepStr = "${(totalSleepMinutes * 0.22f).toInt() / 60}h ${(totalSleepMinutes * 0.22f).toInt() % 60}m"
                val lightStr = "${(totalSleepMinutes * 0.52f).toInt() / 60}h ${(totalSleepMinutes * 0.52f).toInt() % 60}m"
                val remStr = "${(totalSleepMinutes * 0.21f).toInt() / 60}h ${(totalSleepMinutes * 0.21f).toInt() % 60}m"
                val awakeStr = "${(totalSleepMinutes * 0.05f).toInt()}m"

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SleepStageRow(label = "Deep sleep", duration = deepStr, color = Color(0xFF0F9F7A))
                    SleepStageRow(label = "Light sleep", duration = lightStr, color = Color(0xFF8B5CF6))
                    SleepStageRow(label = "REM", duration = remStr, color = Color(0xFFC084FC))
                    SleepStageRow(label = "Awake", duration = awakeStr, color = Color(0xFFE5E7EB))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Insight Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "✨ AI insight",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C3AED)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your calculated sleep duration of $formattedDurationStr matches your 8h daily recovery target well. Maintain this sleep schedule for optimal energy.",
                    fontSize = 13.sp,
                    color = Color(0xFF374151)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun formatTime12Hour(hour24: Int, minute: Int): String {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour24)
        set(Calendar.MINUTE, minute)
    }
    return SimpleDateFormat("hh:mm a", Locale.US).format(cal.time)
}

private fun parseFormattedTimeToHourMinute(timeStr: String): Pair<Int, Int>? {
    return try {
        val date = SimpleDateFormat("hh:mm a", Locale.US).parse(timeStr) ?: return null
        val cal = Calendar.getInstance().apply { time = date }
        Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun SleepStageRow(
    label: String,
    duration: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = label, fontSize = 14.sp, color = Color(0xFF374151))
        }

        Text(text = duration, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
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
fun SleepDetailScreenPreview() {
    VitalsyncTheme {
        SleepDetailScreen()
    }
}
