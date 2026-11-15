package com.simats.vitalsync.ui.ai

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.data.model.ai.AiInsightResponse
import com.simats.vitalsync.data.model.profile.WeeklyReportDto
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import java.util.Locale

@Composable
fun AiScreen(
    modifier: Modifier = Modifier,
    wellnessScore: Int = 0,
    aiInsightTitle: String? = null,
    aiInsightSummary: String? = null,
    aiInsight: AiInsightResponse? = null,
    isInsightLoading: Boolean = false,
    weeklyReport: WeeklyReportDto? = null,
    fallbackSteps: String = "-- steps",
    fallbackWorkouts: Int = 0,
    fallbackSleepDuration: String = "--",
    fallbackSleepScore: String = "--",
    fallbackHydration: String = "-- ml",
    fallbackHeartRate: String = "-- BPM",
    onRefreshWeekly: () -> Unit = {},
    onAskAiClick: () -> Unit = {}
) {
    // Refresh weekly aggregated trends whenever the AI screen is viewed (skip in Compose Previews)
    val inspectionMode = LocalInspectionMode.current
    LaunchedEffect(Unit) {
        if (!inspectionMode) {
            onRefreshWeekly()
        }
    }

    val trends = weeklyReport?.weeklyTrends
    val metrics = weeklyReport?.metrics
    val breakdown = weeklyReport?.vitalPerformanceBreakdown

    // -------------------------------------------------------------------------
    // 1. Activity / Steps (Strict 7-Day Weekly Metrics)
    // -------------------------------------------------------------------------
    val fallbackStepsNum = fallbackSteps.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
    val totalSteps = trends?.activity?.totalSteps
        ?: metrics?.weeklyTotalSteps
        ?: metrics?.totalSteps
        ?: fallbackStepsNum

    val avgSteps = trends?.activity?.avgSteps
        ?: metrics?.weeklyAvgSteps
        ?: (if (totalSteps > 0) totalSteps / 7 else 0)

    val stepsPercent = trends?.activity?.completionPercent
        ?: metrics?.activityStatus?.replace(Regex("[^0-9]"), "")?.toIntOrNull()
        ?: 0

    val workoutCount = trends?.activity?.workoutCount
        ?: metrics?.workoutCount
        ?: fallbackWorkouts

    val activityTrendValue = when {
        !trends?.activity?.titleValue.isNullOrBlank() -> trends!!.activity!!.titleValue
        totalSteps > 0 -> String.format(Locale.US, "%,d steps", totalSteps)
        !breakdown?.activity?.value.isNullOrBlank() && breakdown!!.activity!!.value != "-- steps" && breakdown.activity.value != "0 steps" -> breakdown.activity.value
        fallbackSteps != "-- steps" && fallbackSteps.isNotBlank() && fallbackSteps != "0 steps" -> fallbackSteps
        else -> "0 steps"
    }

    val activityTrendSub = when {
        !trends?.activity?.subtitle.isNullOrBlank() -> trends!!.activity!!.subtitle
        totalSteps > 0 -> String.format(Locale.US, "%,d total (%,d/day avg) • %d%% of wk goal", totalSteps, avgSteps, stepsPercent)
        else -> "$workoutCount workout${if (workoutCount == 1) "" else "s"} (7d)"
    }

    // -------------------------------------------------------------------------
    // 2. Sleep Quality & Duration (Strict 7-Day Weekly Metrics)
    // -------------------------------------------------------------------------
    val parsedFallbackSleepMins = run {
        val m = Regex("(\\d+)\\s*h(?:ours?)?(?:\\s*(\\d+)\\s*m)?", RegexOption.IGNORE_CASE).find(fallbackSleepDuration)
        if (m != null) {
            val h = m.groupValues[1].toIntOrNull() ?: 0
            val min = m.groupValues.getOrNull(2)?.toIntOrNull() ?: 0
            (h * 60) + min
        } else {
            0
        }
    }

    val avgSleepMinutes = trends?.sleep?.avgMinutes
        ?: weeklyReport?.resolvedAvgSleepMinutes()?.takeIf { it > 0 }
        ?: metrics?.avgSleepMinutes?.takeIf { it > 0 }
        ?: parsedFallbackSleepMins

    val totalSleepHours = trends?.sleep?.totalHours
        ?: metrics?.weeklyTotalSleepHours
        ?: (avgSleepMinutes * 7.0 / 60.0)

    val avgSleepHours = trends?.sleep?.avgHours
        ?: metrics?.weeklyAvgSleepHours
        ?: (avgSleepMinutes / 60.0)

    val sleepQuality = trends?.sleep?.status?.takeIf { it.isNotBlank() && it != "No Logs Recorded" && it != "No logs yet" }
        ?: weeklyReport?.resolvedSleepTrendTier()?.takeIf { it != "No logs yet" }
        ?: "No logs yet"

    val sleepTrendValue = when {
        !trends?.sleep?.titleValue.isNullOrBlank() && trends!!.sleep!!.titleValue != "--" -> trends.sleep.titleValue
        avgSleepHours > 0.0 -> String.format(Locale.US, "%.1f hrs/night", avgSleepHours)
        avgSleepMinutes > 0 -> String.format(Locale.US, "%.1f hrs/night", avgSleepMinutes / 60f)
        !breakdown?.sleep?.value.isNullOrBlank() && breakdown!!.sleep!!.value != "--" -> breakdown.sleep.value
        fallbackSleepDuration != "--" && fallbackSleepDuration.isNotBlank() -> fallbackSleepDuration
        else -> "--"
    }

    val sleepTrendSub = when {
        !trends?.sleep?.subtitle.isNullOrBlank() -> trends!!.sleep!!.subtitle
        avgSleepHours > 0.0 -> String.format(Locale.US, "(%.1fh wk total) • %s", totalSleepHours, sleepQuality)
        else -> "No logs yet"
    }

    val sleepTrendColor = when {
        sleepQuality.contains("Optimal", ignoreCase = true) || sleepQuality.contains("Good", ignoreCase = true) -> MaterialTheme.colorScheme.primary
        sleepQuality.contains("Fair", ignoreCase = true) || sleepQuality.contains("Moderate", ignoreCase = true) -> Color(0xFFD97706)
        sleepQuality.contains("Needs", ignoreCase = true) || sleepQuality.contains("Poor", ignoreCase = true) -> Color(0xFFDC2626)
        else -> Color(0xFF6B7280)
    }

    // -------------------------------------------------------------------------
    // 3. Hydration Intake (Strict 7-Day Weekly Metrics)
    // -------------------------------------------------------------------------
    val fallbackHydrationNum = fallbackHydration.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
    val totalWaterMl = trends?.hydration?.totalMl
        ?: metrics?.weeklyTotalHydrationMl
        ?: metrics?.totalHydrationMl
        ?: fallbackHydrationNum

    val avgWaterMl = trends?.hydration?.avgMl
        ?: metrics?.weeklyAvgHydrationMl
        ?: (if (totalWaterMl > 0) totalWaterMl / 7 else 0)

    val hydrationPercent = trends?.hydration?.completionPercent
        ?: metrics?.hydrationStatus?.replace(Regex("[^0-9]"), "")?.toIntOrNull()
        ?: 0

    val hydrationTrendValue = when {
        !trends?.hydration?.titleValue.isNullOrBlank() -> trends!!.hydration!!.titleValue
        totalWaterMl >= 1000 -> String.format(Locale.US, "%.1f L", totalWaterMl / 1000f)
        totalWaterMl > 0 -> "$totalWaterMl ml"
        !breakdown?.hydration?.value.isNullOrBlank() && breakdown!!.hydration!!.value != "-- ml" && breakdown.hydration.value != "0 ml" -> breakdown.hydration.value
        fallbackHydration != "-- ml" && fallbackHydration.isNotBlank() && fallbackHydration != "0 ml" -> fallbackHydration
        else -> "0 ml"
    }

    val hydrationTrendSub = when {
        !trends?.hydration?.subtitle.isNullOrBlank() -> trends!!.hydration!!.subtitle
        totalWaterMl > 0 -> String.format(Locale.US, "(%,d ml/day) • %d%% of goal", avgWaterMl, hydrationPercent)
        else -> "Rolling 7d total"
    }

    // -------------------------------------------------------------------------
    // 4. Heart Rate (Strict 7-Day Weekly Metrics)
    // -------------------------------------------------------------------------
    val fallbackHrNum = fallbackHeartRate.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
    val avgHr = trends?.heartRate?.avgBpm?.takeIf { it > 0 }
        ?: metrics?.avgHrBpm?.takeIf { it > 0 }
        ?: fallbackHrNum

    val minHr = trends?.heartRate?.minBpm ?: metrics?.minHrBpm
    val maxHr = trends?.heartRate?.maxBpm ?: metrics?.maxHrBpm

    val hrStatus = trends?.heartRate?.status?.takeIf { it.isNotBlank() && it != "No Data" }
        ?: metrics?.heartRateStatus?.takeIf { it.isNotBlank() && it != "No Data" }
        ?: "Normal Resting"

    val hrTrendValue = when {
        !trends?.heartRate?.titleValue.isNullOrBlank() && trends!!.heartRate!!.titleValue != "-- bpm" -> trends.heartRate.titleValue
        avgHr > 0 -> "$avgHr BPM"
        !breakdown?.heartRate?.value.isNullOrBlank() && breakdown!!.heartRate!!.value != "-- BPM" -> breakdown.heartRate.value
        fallbackHeartRate != "-- BPM" && fallbackHeartRate.isNotBlank() && fallbackHeartRate != "--" -> {
            if (fallbackHeartRate.contains("BPM", ignoreCase = true)) fallbackHeartRate else "$fallbackHeartRate BPM"
        }
        else -> "-- BPM"
    }

    val hrTrendSub = when {
        !trends?.heartRate?.subtitle.isNullOrBlank() && trends!!.heartRate!!.subtitle != "No readings yet" -> trends.heartRate.subtitle
        avgHr > 0 && minHr != null && maxHr != null -> "wk avg ($minHr–$maxHr bpm) • $hrStatus"
        avgHr > 0 -> "7d weekly avg • $hrStatus"
        fallbackHeartRate != "-- BPM" && fallbackHeartRate.isNotBlank() && fallbackHeartRate != "--" -> "Latest reading"
        else -> "No readings yet"
    }

    // -------------------------------------------------------------------------
    // 5. SpO2 & Blood Pressure Weekly Overview
    // -------------------------------------------------------------------------
    val spo2WeeklyTrendText = trends?.spo2?.formattedValue?.takeIf { it.isNotBlank() && it != "--% wk avg" }
        ?: if (metrics?.avgSpo2 != null) "${metrics.avgSpo2}% wk avg • ${metrics.spo2Status}"
        else if (!metrics?.spo2Avg.isNullOrBlank() && metrics.spo2Avg != "--%") {
            val formatted = if (metrics.spo2Avg.endsWith("%")) metrics.spo2Avg else "${metrics.spo2Avg}%"
            "$formatted wk avg • ${metrics.spo2Status}"
        }
        else if (!breakdown?.spo2?.value.isNullOrBlank() && breakdown!!.spo2!!.value != "--%") "${breakdown.spo2.value} • ${breakdown.spo2.status}"
        else "--% wk avg"

    val bpWeeklyTrendText = trends?.bloodPressure?.formattedValue?.takeIf { it.isNotBlank() && it != "--/-- wk avg" }
        ?: if (metrics?.avgSys != null && metrics?.avgDia != null) "${metrics.avgSys}/${metrics.avgDia} mmHg wk avg • ${metrics.bpStatus}"
        else if (!breakdown?.bloodPressure?.value.isNullOrBlank() && breakdown!!.bloodPressure!!.value != "--/--") "${breakdown.bloodPressure.value} • ${breakdown.bloodPressure.status}"
        else "--/-- wk avg"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VitalSync AI ✨",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = onAskAiClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    ChatBubbleIcon(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onBackground)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Your weekly trends
            Text(
                text = "Your weekly trends",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2x2 Trend Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TrendCard(
                    title = "Activity",
                    value = activityTrendValue,
                    subtitle = activityTrendSub,
                    color = MaterialTheme.colorScheme.primary,
                    iconType = TrendIcon.STEPS,
                    modifier = Modifier.weight(1f)
                )

                TrendCard(
                    title = "Sleep",
                    value = sleepTrendValue,
                    subtitle = sleepTrendSub,
                    color = sleepTrendColor,
                    iconType = TrendIcon.SLEEP,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TrendCard(
                    title = "Hydration",
                    value = hydrationTrendValue,
                    subtitle = hydrationTrendSub,
                    color = if (totalWaterMl > 0) MaterialTheme.colorScheme.primary else Color(0xFFD97706),
                    iconType = TrendIcon.WATER,
                    modifier = Modifier.weight(1f)
                )

                TrendCard(
                    title = "Heart Rate",
                    value = hrTrendValue,
                    subtitle = hrTrendSub,
                    color = if (avgHr > 0) MaterialTheme.colorScheme.primary else Color(0xFF6B7280),
                    iconType = TrendIcon.HEART,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 7-Day Cardiovascular Telemetry (SpO2 & Blood Pressure)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Blood Oxygen (7d)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = spo2WeeklyTrendText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Blood Pressure (7d)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = bpWeeklyTrendText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Small steps for today
            Text(
                text = "Small steps for today",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    StepActionRow(icon = "👟", text = "Take a 15-minute walk today.")
                    Spacer(modifier = Modifier.height(14.dp))
                    StepActionRow(icon = "🌙", text = "Maintain a consistent bedtime.")
                    Spacer(modifier = Modifier.height(14.dp))
                    StepActionRow(icon = "💧", text = "Drink another glass of water.")
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Ask VitalSync AI Button Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Button(
                onClick = onAskAiClick,
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
                    text = "Ask VitalSync AI",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Wellness guidance, not a medical diagnosis.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

enum class TrendIcon {
    STEPS,
    SLEEP,
    WATER,
    HEART
}

@Composable
private fun TrendCard(
    title: String,
    value: String,
    subtitle: String? = null,
    color: Color,
    iconType: TrendIcon,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                    when (iconType) {
                        TrendIcon.STEPS -> StepsIconSmall(modifier = Modifier.size(14.dp))
                        TrendIcon.SLEEP -> MoonIconSmall(modifier = Modifier.size(14.dp))
                        TrendIcon.WATER -> WaterIconSmall(modifier = Modifier.size(14.dp))
                        TrendIcon.HEART -> HeartIconSmall(modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun StepActionRow(
    icon: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3E8FF)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ChatBubbleIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF374151)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.15f, h * 0.2f)
            lineTo(w * 0.85f, h * 0.2f)
            lineTo(w * 0.85f, h * 0.65f)
            lineTo(w * 0.4f, h * 0.65f)
            lineTo(w * 0.2f, h * 0.85f)
            lineTo(w * 0.2f, h * 0.65f)
            lineTo(w * 0.15f, h * 0.65f)
            close()
        }

        drawPath(path = path, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun StepsIconSmall(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color = primary, radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.5f))
    }
}

@Composable
private fun MoonIconSmall(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color = primary, radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.5f))
    }
}

@Composable
private fun WaterIconSmall(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color = Color(0xFFD97706), radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.5f))
    }
}

@Composable
private fun HeartIconSmall(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color = primary, radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.5f))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun AiScreenPreview() {
    VitalsyncTheme {
        AiScreen()
    }
}
