package com.simats.vitalsync.ui.vitals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.data.model.hydration.WeeklyHydrationDayDto
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import com.simats.vitalsync.ui.vitals.viewmodel.HydrationUiState
import com.simats.vitalsync.ui.vitals.viewmodel.HydrationViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HydrationDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: HydrationViewModel? = null,
    userEmail: String = "alex.morgan@email.com",
    dateString: String = "2026-10-30",
    onBackClick: () -> Unit = {}
) {
    val rawDate = remember(dateString) {
        val trimmed = if (dateString.contains("·")) dateString.substringAfter("·").trim() else dateString.trim()
        if (trimmed.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) trimmed else SimpleDateFormat("yyyy-MM-dd", Locale.US).format(
            Date()
        )
    }

    val state by viewModel?.hydrationState?.collectAsState() ?: remember { mutableStateOf(HydrationUiState()) }

    LaunchedEffect(userEmail, rawDate) {
        viewModel?.loadHydration(userEmail, rawDate)
    }

    var localConsumedMl by remember { mutableIntStateOf(1250) }
    var localDailyTargetMl by remember { mutableIntStateOf(2000) }
    var localIsReminderEnabled by remember { mutableStateOf(true) }
    var localLastLoggedAmountMl by remember { mutableIntStateOf(0) }
    var localShowToast by remember { mutableStateOf(false) }

    val consumedWaterMl = if (viewModel != null) state.consumedWaterMl else localConsumedMl
    val dailyTargetMl = if (viewModel != null) state.dailyTargetMl else localDailyTargetMl
    val isReminderEnabled = if (viewModel != null) state.isReminderEnabled else localIsReminderEnabled
    val lastLoggedAmountMl = if (viewModel != null) state.lastLoggedAmountMl else localLastLoggedAmountMl
    val showLoggedToast = if (viewModel != null) state.showToast else localShowToast

    var showCustomMlDialog by remember { mutableStateOf(false) }
    var customInputMlText by remember { mutableStateOf("150") }

    var showCustomTargetDialog by remember { mutableStateOf(false) }
    var customTargetInputText by remember { mutableStateOf("$dailyTargetMl") }

    var selectedChartDayIndex by remember { mutableStateOf<Int?>(null) }

    fun addWaterMl(amountMl: Int) {
        if (amountMl <= 0) return
        if (viewModel != null) {
            viewModel.addWater(userEmail, amountMl, rawDate)
        } else {
            localConsumedMl = (localConsumedMl + amountMl).coerceAtMost(5000)
            localLastLoggedAmountMl = amountMl
            localShowToast = true
        }
    }

    fun setCustomTargetGoal(newTargetMl: Int) {
        if (newTargetMl <= 0) return
        if (viewModel != null) {
            viewModel.updateDailyTarget(userEmail, newTargetMl)
        } else {
            localDailyTargetMl = newTargetMl
        }
    }

    LaunchedEffect(showLoggedToast) {
        if (showLoggedToast) {
            delay(2200L)
            if (viewModel != null) {
                viewModel.dismissToast()
            } else {
                localShowToast = false
            }
        }
    }

    // Smoothly Animated Water Number Counter
    val animatedConsumedMl by animateIntAsState(
        targetValue = consumedWaterMl,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "AnimatedWaterCounter"
    )

    val fillProgressRatio = if (dailyTargetMl > 0) {
        (animatedConsumedMl.toFloat() / dailyTargetMl.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val glassEquivalent = animatedConsumedMl / 250f
    val progressPercentage = (fillProgressRatio * 100).toInt()

    // Strict Log-Driven Weekly Data Construction (Only days with actual logs have values, unlogged days are null)
    val weeklyDaysList = remember(state.weeklyHistory, consumedWaterMl, dailyTargetMl) {
        if (viewModel != null && state.weeklyHistory.size == 7) {
            state.weeklyHistory
        } else {
            // Monday to Sunday strict log-driven layout
            val days = listOf("M", "T", "W", "T", "F", "S", "S")
            days.mapIndexed { index, dayLabel ->
                val isToday = (index == 4) // Friday is current today index in mock setup
                if (isToday && consumedWaterMl > 0) {
                    val ratio = (consumedWaterMl.toFloat() / dailyTargetMl.toFloat()).coerceIn(0f, 1f)
                    WeeklyHydrationDayDto(day = dayLabel, date = "Oct ${11 + index}", consumedMl = consumedWaterMl, targetMl = dailyTargetMl, ratio = ratio, isToday = true)
                } else {
                    // Strict rule: unlogged days are NULL (no fake fallback values)
                    WeeklyHydrationDayDto(day = dayLabel, date = "Oct ${11 + index}", consumedMl = null, targetMl = dailyTargetMl, ratio = null, isToday = isToday)
                }
            }
        }
    }

    // Calculate Weekly Average strictly from days with actual logs (excluding null unlogged days)
    val loggedDays = remember(weeklyDaysList) { weeklyDaysList.filter { it.consumedMl != null } }
    val weeklyAverageFormatted = remember(loggedDays) {
        if (loggedDays.isNotEmpty()) {
            val totalMl = loggedDays.sumOf { it.consumedMl ?: 0 }
            val avgMl = totalMl / loggedDays.size
            "${String.format(Locale.US, "%,d", avgMl)} ml/day (${loggedDays.size} ${if (loggedDays.size == 1) "day" else "days"} logged)"
        } else {
            "No data"
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
                    text = "Hydration Tracker",
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

        Spacer(modifier = Modifier.height(20.dp))

        // Logged Water Toast Banner Popup
        AnimatedVisibility(
            visible = showLoggedToast,
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
                    text = "💧 +$lastLoggedAmountMl ml added to your hydration log!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF15803D)
                )
            }
        }

        // Top Water Glass & Configurable Target Goal Card
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
                // Configurable Daily Goal Badge (Clickable to Edit Goal)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8F6F0))
                        .clickable {
                            customTargetInputText = "$dailyTargetMl"
                            showCustomTargetDialog = true
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🎯 Target: ${String.format(Locale.US, "%,d", dailyTargetMl)} ml ✏️",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F9F7A)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Large Animated Floating Water Glass Illustration
                WaterGlassLarge(
                    fillRatio = fillProgressRatio,
                    modifier = Modifier.size(90.dp, 110.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Smooth Animated Text Values
                Text(
                    text = "${String.format(Locale.US, "%.1f", glassEquivalent)} / ${(dailyTargetMl / 250)} glasses",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Text(
                    text = "${String.format(Locale.US, "%,d", animatedConsumedMl)} ml of ${String.format(Locale.US, "%,d", dailyTargetMl)} ml goal",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Smooth Animated Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5E7EB))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fillProgressRatio)
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(VitalTealPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$progressPercentage% complete",
                        fontSize = 12.sp,
                        color = Color(0xFF0F9F7A),
                        fontWeight = FontWeight.Bold
                    )

                    if (consumedWaterMl > 0) {
                        Text(
                            text = "Reset ↺",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                if (viewModel != null) {
                                    viewModel.resetHydration(userEmail, rawDate)
                                } else {
                                    localConsumedMl = 0
                                    localShowToast = false
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Log & Custom ml Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Log a drink",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Text(
                text = "+ Custom ml ✏️",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F9F7A),
                modifier = Modifier.clickable {
                    customInputMlText = "150"
                    showCustomMlDialog = true
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Preset Quick Action Buttons Grid
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { addWaterMl(250) },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VitalTealDark)
                ) {
                    Text(text = "💧 +250 ml", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { addWaterMl(500) },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6F4EA), contentColor = VitalTealDark)
                ) {
                    Text(text = "🥛 +500 ml", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { addWaterMl(750) },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6F4EA), contentColor = VitalTealDark)
                ) {
                    Text(text = "🍾 +750 ml", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        customInputMlText = "350"
                        showCustomMlDialog = true
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6F4EA), contentColor = VitalTealDark)
                ) {
                    Text(text = "✨ Custom ml", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Daily Water Bar Chart Card with Strict Log-Driven Rules & Interactive Tooltips
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weekly hydration history",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    // Strict Log-Calculated Average Summary
                    Text(
                        text = "Avg: $weeklyAverageFormatted",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F9F7A)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Smoothly Animated Water Bar Chart
                var targetProgress by remember { mutableFloatStateOf(0f) }
                LaunchedEffect(Unit) {
                    targetProgress = 1f
                }
                val chartProgress by animateFloatAsState(
                    targetValue = targetProgress,
                    animationSpec = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
                    label = "HydrationBarChartAnimation"
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    val barWidth = 24.dp.toPx()
                    val totalWidth = size.width
                    val spacing = (totalWidth - (barWidth * 7)) / 8f

                    weeklyDaysList.forEachIndexed { i, dayData ->
                        val x = spacing + i * (barWidth + spacing)
                        val ratio = dayData.ratio

                        if (ratio != null && ratio > 0f) {
                            // Solid Logged Water Bar
                            val barHeight = size.height * ratio * chartProgress
                            val y = size.height - barHeight

                            drawRoundRect(
                                color = if (dayData.isToday) Color(0xFF0F9F7A) else Color(0xFFA7F3D0),
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        } else {
                            // Strict Log-Driven Rule: Unlogged days render as empty ghost placeholder outlines
                            drawRoundRect(
                                color = Color(0xFFF1F5F9),
                                topLeft = Offset(x, size.height * 0.85f),
                                size = Size(barWidth, size.height * 0.15f),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )

                            drawRoundRect(
                                color = Color(0xFFCBD5E1),
                                topLeft = Offset(x, 0f),
                                size = Size(barWidth, size.height),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                                style = Stroke(
                                    width = 1.2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Days Labels Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weeklyDaysList.forEachIndexed { index, dayData ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { selectedChartDayIndex = if (selectedChartDayIndex == index) null else index }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = dayData.day,
                                fontSize = 12.sp,
                                fontWeight = if (dayData.isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (dayData.isToday) Color(0xFF0F9F7A) else Color(0xFF9CA3AF)
                            )
                        }
                    }
                }

                // Interactive Day Tooltip Output
                selectedChartDayIndex?.let { idx ->
                    if (idx in weeklyDaysList.indices) {
                        val dayItem = weeklyDaysList[idx]
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Day: ${dayItem.day} (${dayItem.date})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155)
                                )

                                Text(
                                    text = if (dayItem.consumedMl != null) "${String.format(Locale.US, "%,d", dayItem.consumedMl)} ml logged" else "No log",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dayItem.consumedMl != null) Color(0xFF0F9F7A) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Smart Reminder Toggle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Smart reminder",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "A gentle nudge every 2 hours",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Switch(
                    checked = isReminderEnabled,
                    onCheckedChange = { checked ->
                        if (viewModel != null) {
                            viewModel.updateReminder(userEmail, checked)
                        } else {
                            localIsReminderEnabled = checked
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = VitalTealPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Custom ML Input Dialog
    if (showCustomMlDialog) {
        AlertDialog(
            onDismissRequest = { showCustomMlDialog = false },
            title = {
                Text(
                    text = "Add Custom Water (ml)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter the amount of water you drank in milliliters:",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customInputMlText,
                        onValueChange = { customInputMlText = it.filter { char -> char.isDigit() } },
                        label = { Text("Amount (ml)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VitalTealPrimary,
                            focusedLabelColor = VitalTealPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Quick Selection Chips Inside Dialog
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(100, 150, 300, 500).forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE6F4EA))
                                    .clickable { customInputMlText = preset.toString() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "+$preset ml",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VitalTealDark
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = customInputMlText.toIntOrNull() ?: 0
                        if (amount > 0) {
                            addWaterMl(amount)
                        }
                        showCustomMlDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VitalTealDark)
                ) {
                    Text("Add Water")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomMlDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            }
        )
    }

    // Configurable Daily Target Goal Dialog
    if (showCustomTargetDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTargetDialog = false },
            title = {
                Text(
                    text = "Set Daily Hydration Goal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Set your custom daily target water intake in milliliters:",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customTargetInputText,
                        onValueChange = { customTargetInputText = it.filter { char -> char.isDigit() } },
                        label = { Text("Daily Target Goal (ml)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VitalTealPrimary,
                            focusedLabelColor = VitalTealPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Target Goal Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1500, 2000, 2500, 3000).forEach { targetPreset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE6F4EA))
                                    .clickable { customTargetInputText = targetPreset.toString() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$targetPreset ml",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VitalTealDark
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = customTargetInputText.toIntOrNull() ?: 2000
                        if (target > 0) {
                            setCustomTargetGoal(target)
                        }
                        showCustomTargetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VitalTealDark)
                ) {
                    Text("Save Goal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTargetDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            }
        )
    }
}

@Composable
private fun WaterGlassLarge(
    fillRatio: Float,
    modifier: Modifier = Modifier
) {
    // Continuous Wave Floating Animation
    val infiniteTransition = rememberInfiniteTransition(label = "WaterWaveTransition")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    // Smooth Water Rising/Falling Level Animation
    val animatedFillRatio by animateFloatAsState(
        targetValue = fillRatio.coerceIn(0.05f, 0.95f),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "AnimatedWaterFill"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Outer Glass Container Outline
        val glassPath = Path().apply {
            moveTo(w * 0.15f, 0f)
            lineTo(w * 0.85f, 0f)
            lineTo(w * 0.75f, h)
            lineTo(w * 0.25f, h)
            close()
        }

        drawPath(
            path = glassPath,
            color = Color(0xFF0F9F7A),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val baseWaterY = h * (1f - animatedFillRatio)
        val waveAmplitude = 3.5.dp.toPx()

        // Primary Floating Wave Layer
        val waterPath1 = Path().apply {
            val startX = w * 0.20f
            val endX = w * 0.80f
            val steps = 24

            moveTo(startX, baseWaterY + sin(wavePhase) * waveAmplitude)

            for (i in 1..steps) {
                val progress = i.toFloat() / steps
                val x = startX + (endX - startX) * progress
                val y = baseWaterY + sin(wavePhase + progress * 2f * Math.PI.toFloat()) * waveAmplitude
                lineTo(x, y)
            }

            lineTo(w * 0.74f, h - 2.dp.toPx())
            lineTo(w * 0.26f, h - 2.dp.toPx())
            close()
        }

        // Secondary Subsurface Wave Layer for 3D Water Fluid Depth
        val waterPath2 = Path().apply {
            val startX = w * 0.20f
            val endX = w * 0.80f
            val steps = 24

            moveTo(startX, baseWaterY + cos(wavePhase) * waveAmplitude)

            for (i in 1..steps) {
                val progress = i.toFloat() / steps
                val x = startX + (endX - startX) * progress
                val y = baseWaterY + cos(wavePhase + progress * 2f * Math.PI.toFloat()) * waveAmplitude
                lineTo(x, y)
            }

            lineTo(w * 0.74f, h - 2.dp.toPx())
            lineTo(w * 0.26f, h - 2.dp.toPx())
            close()
        }

        // Draw Subsurface Water Wave
        drawPath(
            path = waterPath2,
            color = Color(0xFF0284C7).copy(alpha = 0.35f)
        )

        // Draw Main Floating Water Wave Surface
        drawPath(
            path = waterPath1,
            color = Color(0xFF38BDF8).copy(alpha = 0.65f)
        )
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
fun HydrationDetailScreenPreview() {
    VitalsyncTheme {
        HydrationDetailScreen()
    }
}
