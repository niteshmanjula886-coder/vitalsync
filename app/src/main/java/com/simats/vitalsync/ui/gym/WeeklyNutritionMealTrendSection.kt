package com.simats.vitalsync.ui.gym

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.data.model.gym.WeeklyNutritionTrendDayDto
import com.simats.vitalsync.ui.gym.viewmodel.GymViewModel
import com.simats.vitalsync.ui.gym.viewmodel.NutritionTrendMetric
import com.simats.vitalsync.ui.theme.CarbonDarkAccent
import com.simats.vitalsync.ui.theme.VitalPrimaryMint
import com.simats.vitalsync.ui.theme.VitalTealDark
import java.text.SimpleDateFormat
import java.util.*

/**
 * 7-Day Weekly Nutrition Meal Trend vs. Target Section
 * Supports switching across Calories, Protein, Carbs, and Fats,
 * interactive day selection, target threshold lines, and weekly adherence stats.
 */
@Composable
fun WeeklyNutritionMealTrendSection(
    viewModel: GymViewModel,
    userId: Int?,
    userEmail: String?,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val trendData = uiState.weeklyNutritionTrend
    val selectedMetric = uiState.selectedWeeklyNutritionMetric

    // Dynamic targets from user's active settings (Issue 5 sync)
    val activeCalTarget = uiState.activeCaloriesTarget
    val activeProtTarget = uiState.activeProteinTargetG
    val activeCarbTarget = (activeCalTarget * 0.45f / 4f)
    val activeFatTarget = (activeCalTarget * 0.25f / 9f)

    // Build 7-day calendar window (defaults to past 7 days ending today if empty)
    val calendarDays = remember(trendData, activeCalTarget, activeProtTarget) {
        if (!trendData?.days.isNullOrEmpty()) {
            trendData!!.days
        } else {
            val list = mutableListOf<WeeklyNutritionTrendDayDto>()
            val cal = Calendar.getInstance()
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)

            for (i in 6 downTo 0) {
                val dayCal = Calendar.getInstance()
                dayCal.add(Calendar.DAY_OF_YEAR, -i)
                val dStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(dayCal.time)
                val dayName = SimpleDateFormat("EEE", Locale.US).format(dayCal.time)
                val dateFmt = SimpleDateFormat("MMM d", Locale.US).format(dayCal.time)
                val isToday = (dStr == todayStr)

                // If today, use todayNutrition if available
                val todayNut = if (isToday) uiState.todayNutrition else null
                val calVal = todayNut?.totalCalories ?: 0
                val protVal = todayNut?.totalProtein ?: 0f
                val carbVal = todayNut?.totalCarbs ?: 0f
                val fatVal = todayNut?.totalFats ?: 0f
                val count = todayNut?.meals?.size ?: 0

                val status = if (calVal <= 0) "No logs"
                else if (kotlin.math.abs(calVal - activeCalTarget) <= (activeCalTarget * 0.12f)) "On Target"
                else if (calVal < activeCalTarget) "Under Target"
                else "Over Target"

                list.add(
                    WeeklyNutritionTrendDayDto(
                        date = dStr,
                        dayOfWeek = dayName,
                        dayShort = dayName,
                        dateFormatted = dateFmt,
                        isToday = isToday,
                        calories = calVal,
                        targetCalories = activeCalTarget,
                        proteinG = protVal,
                        targetProteinG = activeProtTarget,
                        carbsG = carbVal,
                        targetCarbsG = activeCarbTarget,
                        fatsG = fatVal,
                        targetFatsG = activeFatTarget,
                        mealsCount = count,
                        status = status
                    )
                )
            }
            list
        }
    }

    // Determine currently selected day
    val selectedDay = remember(calendarDays, uiState.selectedWeeklyTrendDate) {
        calendarDays.find { it.date == uiState.selectedWeeklyTrendDate }
            ?: calendarDays.find { it.isToday }
            ?: calendarDays.lastOrNull()
            ?: WeeklyNutritionTrendDayDto()
    }

    // Active metric target and values
    val currentTarget = when (selectedMetric) {
        NutritionTrendMetric.CALORIES -> activeCalTarget.toFloat()
        NutritionTrendMetric.PROTEIN -> activeProtTarget
        NutritionTrendMetric.CARBS -> activeCarbTarget
        NutritionTrendMetric.FATS -> activeFatTarget
    }

    // Calculate maximum bar value for chart scaling (target * 1.3f or highest logged value)
    val maxChartValue = remember(calendarDays, selectedMetric, currentTarget) {
        val highestLogged = calendarDays.maxOfOrNull { day ->
            when (selectedMetric) {
                NutritionTrendMetric.CALORIES -> day.calories.toFloat()
                NutritionTrendMetric.PROTEIN -> day.proteinG
                NutritionTrendMetric.CARBS -> day.carbsG
                NutritionTrendMetric.FATS -> day.fatsG
            }
        } ?: 0f
        maxOf(currentTarget * 1.25f, highestLogged * 1.1f, 10f)
    }

    // Weekly calculations
    val loggedDaysCount = remember(calendarDays) { calendarDays.count { it.calories > 0 } }
    val onTargetDaysCount = remember(calendarDays, selectedMetric, currentTarget) {
        calendarDays.count { day ->
            val v = when (selectedMetric) {
                NutritionTrendMetric.CALORIES -> day.calories.toFloat()
                NutritionTrendMetric.PROTEIN -> day.proteinG
                NutritionTrendMetric.CARBS -> day.carbsG
                NutritionTrendMetric.FATS -> day.fatsG
            }
            if (v <= 0f) false
            else kotlin.math.abs(v - currentTarget) <= (currentTarget * 0.15f) || (selectedMetric == NutritionTrendMetric.PROTEIN && v >= currentTarget * 0.9f)
        }
    }
    val adherencePercent = if (calendarDays.isNotEmpty()) ((onTargetDaysCount.toFloat() / calendarDays.size.toFloat()) * 100).toInt() else 0

    val weeklyTotalForMetric = remember(calendarDays, selectedMetric) {
        calendarDays.sumOf { day ->
            when (selectedMetric) {
                NutritionTrendMetric.CALORIES -> day.calories.toDouble()
                NutritionTrendMetric.PROTEIN -> day.proteinG.toDouble()
                NutritionTrendMetric.CARBS -> day.carbsG.toDouble()
                NutritionTrendMetric.FATS -> day.fatsG.toDouble()
            }
        }.toFloat()
    }
    val weeklyAvgForMetric = if (loggedDaysCount > 0) weeklyTotalForMetric / loggedDaysCount else 0f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // -----------------------------------------------------------------
            // 1. Header: Title, Subtitle & Weekly Adherence Badge
            // -----------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.BarChart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Weekly Meal Trends",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "7-Day logged intake vs. nutrition goals",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Adherence Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (adherencePercent >= 60) Color(0xFFE8F6F0) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$adherencePercent% Adherence",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (adherencePercent >= 60) VitalPrimaryMint else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // -----------------------------------------------------------------
            // 2. Metric Selector Tabs (Calories, Protein, Carbs, Fats)
            // -----------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                NutritionTrendMetric.values().forEach { metric ->
                    val isSelected = (metric == selectedMetric)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) VitalTealDark else Color.Transparent)
                            .clickable { viewModel.selectNutritionTrendMetric(metric) }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val metricIcon = when (metric) {
                                NutritionTrendMetric.CALORIES -> Icons.Rounded.LocalFireDepartment
                                NutritionTrendMetric.PROTEIN -> Icons.Rounded.FitnessCenter
                                NutritionTrendMetric.CARBS -> Icons.Rounded.BakeryDining
                                NutritionTrendMetric.FATS -> Icons.Rounded.Opacity
                            }
                            Icon(
                                imageVector = metricIcon,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = metric.label,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // -----------------------------------------------------------------
            // 3. Metric Goal Target Banner
            // -----------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Daily Target: ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when (selectedMetric) {
                            NutritionTrendMetric.CALORIES -> "${currentTarget.toInt()} kcal"
                            else -> "${currentTarget.toInt()}g"
                        },
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = VitalPrimaryMint
                    )
                }

                Text(
                    text = "Weekly Avg: ${if (selectedMetric == NutritionTrendMetric.CALORIES) weeklyAvgForMetric.toInt().toString() + " kcal" else String.format(Locale.US, "%.1fg", weeklyAvgForMetric)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -----------------------------------------------------------------
            // 4. Interactive 7-Day Bar Chart with Target Reference Line
            // -----------------------------------------------------------------
            val targetLineRatio = (currentTarget / maxChartValue).coerceIn(0.1f, 0.95f)
            val chartHeight = 150.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
            ) {
                // Background Target Reference Dashed Line Canvas
                val outlineColor = MaterialTheme.colorScheme.outlineVariant
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 28.dp) // Leave space for day labels
                ) {
                    val targetY = size.height * (1f - targetLineRatio)
                    drawLine(
                        color = outlineColor,
                        start = Offset(0f, targetY),
                        end = Offset(size.width, targetY),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                }

                // 7 Days Columns
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    calendarDays.forEach { day ->
                        val isDaySelected = (day.date == selectedDay.date)
                        val dayVal = when (selectedMetric) {
                            NutritionTrendMetric.CALORIES -> day.calories.toFloat()
                            NutritionTrendMetric.PROTEIN -> day.proteinG
                            NutritionTrendMetric.CARBS -> day.carbsG
                            NutritionTrendMetric.FATS -> day.fatsG
                        }

                        // Bar height ratio relative to max
                        val rawRatio = if (maxChartValue > 0f) (dayVal / maxChartValue).coerceIn(0f, 1f) else 0f
                        val animatedRatio by animateFloatAsState(
                            targetValue = rawRatio,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "barHeightRatio"
                        )

                        // Status Color
                        val barColor = when {
                            dayVal <= 0f -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            kotlin.math.abs(dayVal - currentTarget) <= (currentTarget * 0.15f) -> VitalPrimaryMint
                            dayVal > currentTarget -> Color(0xFFE11D48) // Over target (Rose red)
                            else -> Color(0xFFD97706) // Under target (Amber orange)
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { viewModel.selectWeeklyTrendDate(day.date) }
                                .padding(horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            // The Bar Pill (Clean, without floating dots/icons above)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.68f)
                                        .fillMaxHeight(fraction = animatedRatio.coerceAtLeast(0.04f))
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                        .background(barColor)
                                        .then(
                                            if (isDaySelected) Modifier.border(
                                                width = 2.dp,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                                            ) else Modifier
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Day Label (Mon, Tue, etc.)
                            Text(
                                text = day.dayShort.take(3),
                                fontSize = 11.sp,
                                fontWeight = if (isDaySelected || day.isToday) FontWeight.Bold else FontWeight.Medium,
                                color = if (isDaySelected) MaterialTheme.colorScheme.onBackground
                                        else if (day.isToday) VitalPrimaryMint
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Date Day Number (Clean, without stray dots)
                            val dayNum = day.date.takeLast(2)
                            Text(
                                text = dayNum,
                                fontSize = 9.5.sp,
                                fontWeight = if (isDaySelected || day.isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDaySelected) MaterialTheme.colorScheme.onBackground
                                        else if (day.isToday) VitalPrimaryMint
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -----------------------------------------------------------------
            // 5. Interactive Selected Day Detail Card
            // -----------------------------------------------------------------
            val selectedVal = when (selectedMetric) {
                NutritionTrendMetric.CALORIES -> selectedDay.calories.toFloat()
                NutritionTrendMetric.PROTEIN -> selectedDay.proteinG
                NutritionTrendMetric.CARBS -> selectedDay.carbsG
                NutritionTrendMetric.FATS -> selectedDay.fatsG
            }

            val dayPct = if (currentTarget > 0f) ((selectedVal / currentTarget) * 100).toInt() else 0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header with Date and Status Tag
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${selectedDay.dayOfWeek}, ${selectedDay.dateFormatted}",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (selectedDay.isToday) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(VitalPrimaryMint.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "TODAY",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VitalPrimaryMint
                                    )
                                }
                            }
                        }

                        // Day status pill
                        val (statusText, statusBg, statusColor) = when {
                            selectedVal <= 0f -> Triple("No Meals Logged", MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), MaterialTheme.colorScheme.onSurfaceVariant)
                            kotlin.math.abs(selectedVal - currentTarget) <= (currentTarget * 0.15f) -> Triple("On Target ($dayPct%)", Color(0xFFE8F6F0), VitalPrimaryMint)
                            selectedVal > currentTarget -> Triple("Over Target ($dayPct%)", Color(0xFFFFECEE), Color(0xFFE11D48))
                            else -> Triple("Under Target ($dayPct%)", Color(0xFFFEF3C7), Color(0xFFD97706))
                        }

                        val statusIcon = when {
                            selectedVal <= 0f -> null
                            kotlin.math.abs(selectedVal - currentTarget) <= (currentTarget * 0.15f) -> Icons.Rounded.CheckCircle
                            selectedVal > currentTarget -> Icons.Rounded.ArrowUpward
                            else -> Icons.Rounded.Warning
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusBg)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (statusIcon != null) {
                                    Icon(
                                        imageVector = statusIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(11.dp),
                                        tint = statusColor
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = statusText,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Prominent Metric Comparison
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = when (selectedMetric) {
                                    NutritionTrendMetric.CALORIES -> "${selectedDay.calories} / ${activeCalTarget} kcal"
                                    NutritionTrendMetric.PROTEIN -> "${selectedDay.proteinG.toInt()} / ${activeProtTarget.toInt()}g protein"
                                    NutritionTrendMetric.CARBS -> "${selectedDay.carbsG.toInt()} / ${activeCarbTarget.toInt()}g carbs"
                                    NutritionTrendMetric.FATS -> "${selectedDay.fatsG.toInt()} / ${activeFatTarget.toInt()}g fats"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (selectedDay.mealsCount > 0) "${selectedDay.mealsCount} meals logged" else "Tap 'Log Meal' below to add",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Mini Macro Summary Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MacroChip(label = "P", value = "${selectedDay.proteinG.toInt()}g", color = Color(0xFF026C54))
                            MacroChip(label = "C", value = "${selectedDay.carbsG.toInt()}g", color = Color(0xFF0284C7))
                            MacroChip(label = "F", value = "${selectedDay.fatsG.toInt()}g", color = Color(0xFFD97706))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // -----------------------------------------------------------------
            // 6. 3-Stat Weekly Summary Row (Avg Intake, Days Logged, Adherence)
            // -----------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeeklyStatMiniCard(
                    modifier = Modifier.weight(1f),
                    title = "7D Average",
                    value = if (selectedMetric == NutritionTrendMetric.CALORIES) "${weeklyAvgForMetric.toInt()} kcal" else "${weeklyAvgForMetric.toInt()}g",
                    subtitle = "per logged day"
                )
                WeeklyStatMiniCard(
                    modifier = Modifier.weight(1f),
                    title = "Logged Days",
                    value = "$loggedDaysCount / 7",
                    subtitle = "days active"
                )
                WeeklyStatMiniCard(
                    modifier = Modifier.weight(1f),
                    title = "Target Met",
                    value = "$onTargetDaysCount / 7",
                    subtitle = "on-target days"
                )
            }

            // -----------------------------------------------------------------
            // 7. AI Nutrition Trend Insight
            // -----------------------------------------------------------------
            val aiTrendInsight = trendData?.summary?.aiTrendInsight ?: when {
                loggedDaysCount == 0 -> "Log meals regularly to unlock weekly macro analytics and personalized AI nutrition tips!"
                onTargetDaysCount >= 5 -> "Phenomenal discipline! You met your nutrition target on $onTargetDaysCount of 7 days with optimal macro consistency."
                onTargetDaysCount >= 3 -> "Great effort! You're on track for $onTargetDaysCount days this week. Adjust portion sizes on rest days for peak recovery."
                else -> "Logged $loggedDaysCount days this week. Focus on balanced pre- and post-workout meals to hit your daily $activeCalTarget kcal goal consistently."
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(VitalPrimaryMint.copy(alpha = 0.08f))
                    .border(BorderStroke(1.dp, VitalPrimaryMint.copy(alpha = 0.2f)), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = VitalPrimaryMint
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Weekly Nutrition Insight",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = VitalPrimaryMint
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = aiTrendInsight.replace(",", ""),
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroChip(
    label: String,
    value: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = value,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun WeeklyStatMiniCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                fontSize = 9.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )
        }
    }
}
