package com.simats.vitalsync.data.ai

import android.content.Context
import com.simats.vitalsync.data.FitnessRepository
import com.simats.vitalsync.data.local.SessionManager
import com.simats.vitalsync.data.model.ai.AiInsightResponse
import com.simats.vitalsync.data.model.ai.OllamaChatMessageDto
import com.simats.vitalsync.data.model.profile.WeeklyReportDto
import com.simats.vitalsync.data.repository.HealthDataRepository
import com.simats.vitalsync.data.sensor.StepSensorManager
import com.simats.vitalsync.ui.ai.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Builds a compact real-time user profile, vitals, wellness, and nutrition snapshot
 * right before each VitalSync AI request, alongside multi-turn /api/chat message arrays.
 */
object LiveUserContextBuilder {

    const val MAX_HISTORY_MESSAGES = 10

    const val NUTRITION_MACRO_REFERENCE = """
[NUTRITION REFERENCE BASELINES]:
- 1 large whole boiled egg (50g): 75 kcal | 6.3g P | 0.6g C | 5.0g F
- 1 egg white (33g): 17 kcal | 3.6g P | 0.2g C | 0.1g F
- 1 scoop whey protein (30g): 120 kcal | 24g P | 2g C | 1.5g F
- 1 plate beef biryani (280g): 500 kcal | 24g P | 58g C | 18g F
- 100g cooked white rice: 130 kcal | 2.7g P | 28g C | 0.3g F

[MACRO CALCULATION RULES]:
- Multiply EVERY food item by the EXACT quantity requested (e.g., 10 boiled eggs = 10x single egg; 2 scoops whey = 2x single scoop). Never show only 1-unit macros for multi-unit queries.
- List each item's multiplied Calories, Protein, Carbs, and Fat, then finish with a **Combined Meal Total** summing all items (Calories ~= Protein*4 + Carbs*4 + Fat*9).
"""

    const val SYSTEM_BEHAVIOR_RULES = """
You are VitalSync AI, a precise health, fitness, and nutrition coach.
- RESPONSE LENGTH: Match answer length to the question. For short/direct questions (single food check, vitals check, personal info lookup, yes/no, or follow-up), reply in 2-4 concise sentences or max 3 short bullet points with no filler. For detailed breakdown requests (multi-item meals, workout routines, full daily summary), output a clean structured breakdown with exact numbers and totals.
- BRANDING: Refer to yourself only as VitalSync AI. Never output internal model names or system tags.
- VITALS & WELLNESS ASSESSMENTS: For HR, SpO2, Hydration, and Sleep queries, always report: (1) exact reading + timestamp from snapshot (or state clearly if no reading is logged yet), (2) status rating (**Good**, **Moderate / Fair**, or **Poor / Low**), and (3) a brief 1-2 sentence actionable tip.
  Thresholds: Resting HR 60-100 bpm = Good, 50-59 or 101-110 = Moderate/Fair, <50 or >110 = Poor/Low; SpO2 95-100% = Good, 90-94% = Moderate/Fair, <90% = Poor/Low; Hydration >=75% target = Good, 40-74% = Moderate/Fair, <40% = Poor/Low; Sleep 7-9h = Good, 6-6.9h = Moderate/Fair, <6h = Poor/Low.
"""

    fun buildLiveContextSnapshot(
        context: Context,
        weeklyReport: WeeklyReportDto? = null,
        aiInsight: AiInsightResponse? = null
    ): String {
        val appCtx = context.applicationContext
        val sessionManager = SessionManager.getInstance(appCtx)
        val fitnessRepo = FitnessRepository.getInstance(appCtx)
        val healthRepo = HealthDataRepository.getInstance(appCtx)
        val stepManager = StepSensorManager.getInstance(appCtx)
        val gymPrefs = appCtx.getSharedPreferences("vitalsync_gym_preferences", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        val user = sessionManager.getUser()
        val profileWeight = user?.weight?.takeIf { it > 0f } ?: gymPrefs.getFloat("key_body_weight", 0f)
        val heightCm = user?.height?.takeIf { it > 0f } ?: 0f
        val bmiVal = if (heightCm > 0f && profileWeight > 0f) {
            val hM = heightCm / 100f
            ((profileWeight / (hM * hM)) * 10f).roundToInt() / 10f
        } else null

        val proteinMode = gymPrefs.getString("key_protein_mode", "BODY_WEIGHT") ?: "BODY_WEIGHT"
        val customCal = gymPrefs.getInt("key_custom_calories", 2200)
        val customProt = gymPrefs.getFloat("key_custom_protein", 140f)
        val mult = gymPrefs.getFloat("key_protein_multiplier", 1.8f)
        val targetCalories = if (proteinMode == "CUSTOM") customCal else ((if (profileWeight > 0f) profileWeight else 70f) * 32f).toInt().coerceAtLeast(1600)
        val targetProteinG = if (proteinMode == "CUSTOM") customProt.toInt() else ((if (profileWeight > 0f) profileWeight else 70f) * mult).toInt()
        val stepGoal = sessionManager.getSavedStepGoal() ?: fitnessRepo.getSavedStepGoal() ?: 10000

        // Vitals from HealthDataRepository + FitnessRepository
        val liveMetrics = healthRepo.liveMetrics.value
        val recentReadings = healthRepo.recentReadings.value
        val latestHrItem = recentReadings.firstOrNull { it.readingType == "heart_rate" && (it.valueNumeric ?: 0f) > 0f }
        val latestSpo2Item = recentReadings.firstOrNull { it.readingType == "spo2" && (it.valueNumeric ?: 0f) > 0f }
        val latestBpItem = recentReadings.firstOrNull { it.readingType == "blood_pressure" && (it.valueNumeric ?: 0f) > 0f }
        val cachedTodaySpo2 = fitnessRepo.getTodaySpo2Reading()

        val hrBpm = latestHrItem?.valueNumeric?.roundToInt() ?: liveMetrics.heartRate
        val hrTime = latestHrItem?.recordedAt
        val hrText = if (hrBpm != null && hrBpm > 0) {
            if (!hrTime.isNullOrBlank()) "$hrBpm bpm (logged $hrTime)" else "$hrBpm bpm"
        } else "No reading logged yet"

        val spo2Pct = cachedTodaySpo2?.percentage ?: latestSpo2Item?.valueNumeric?.roundToInt() ?: liveMetrics.spo2
        val spo2Time = cachedTodaySpo2?.recordedAt ?: latestSpo2Item?.recordedAt
        val spo2Text = if (spo2Pct != null && spo2Pct > 0) {
            if (!spo2Time.isNullOrBlank()) "$spo2Pct% (logged $spo2Time)" else "$spo2Pct%"
        } else "No reading logged yet"

        val bpSys = latestBpItem?.valueNumeric?.roundToInt() ?: liveMetrics.bloodPressure?.first
        val bpDia = latestBpItem?.valueSecondary?.roundToInt() ?: liveMetrics.bloodPressure?.second
        val bpText = if (bpSys != null && bpDia != null && bpSys > 0 && bpDia > 0) {
            val bpTime = latestBpItem?.recordedAt
            if (!bpTime.isNullOrBlank()) "$bpSys/$bpDia mmHg (logged $bpTime)" else "$bpSys/$bpDia mmHg"
        } else "No reading logged yet"

        // Steps, Activity, Cycling, Sleep, Nutrition
        val activitySessionsToday = fitnessRepo.getActivitySessionRecords().filter { it.dateString == today }
        val activityStepsToday = activitySessionsToday.sumOf { it.steps }
        val todaySteps = maxOf(stepManager.liveSteps.value, liveMetrics.steps ?: 0, activityStepsToday)

        val latestActivity = fitnessRepo.getActivitySessionRecords().firstOrNull()
        val latestActivityText = if (latestActivity != null) {
            "${latestActivity.activityType}: ${String.format(Locale.US, "%.2f km", latestActivity.distanceKm)}, ${latestActivity.durationSeconds / 60}m ${latestActivity.durationSeconds % 60}s, ${latestActivity.caloriesBurned} kcal (${latestActivity.dateString})"
        } else {
            val latestGym = fitnessRepo.getAllWorkoutSessions().firstOrNull()
            if (latestGym != null) "${latestGym.name}: ${latestGym.durationMinutes}m, ${latestGym.totalVolumeKg.toInt()}kg volume, ${latestGym.estimatedCaloriesBurned} kcal (${latestGym.dateString})"
            else "No reading logged yet"
        }

        val lastSleep = fitnessRepo.getRecentSleepRecords(1).firstOrNull()
        val sleepText = when {
            lastSleep != null -> "${lastSleep.durationMinutes / 60}h ${lastSleep.durationMinutes % 60}m (Quality: ${lastSleep.qualityLabel}, Score: ${lastSleep.sleepScore}/100, Date: ${lastSleep.dateString})"
            (liveMetrics.sleepMinutes ?: 0) > 0 -> "${(liveMetrics.sleepMinutes ?: 0) / 60}h ${(liveMetrics.sleepMinutes ?: 0) % 60}m"
            else -> "No reading logged yet"
        }

        val todayMeals = fitnessRepo.getMealEntriesForDate(today)
        val nutritionText = if (todayMeals.isNotEmpty()) {
            val calSum = todayMeals.sumOf { it.calculatedCalories }
            val pSum = todayMeals.sumOf { it.calculatedProtein.toDouble() }.roundToInt()
            val cSum = todayMeals.sumOf { it.calculatedCarbs.toDouble() }.roundToInt()
            val fSum = todayMeals.sumOf { it.calculatedFat.toDouble() }.roundToInt()
            "$calSum kcal logged ($pSum g P, $cSum g C, $fSum g F)"
        } else "0 kcal logged on device"

        val hydrationEval = aiInsight?.metricEvaluations?.firstOrNull {
            it.metric.contains("Hydration", ignoreCase = true) || it.metric.contains("Water", ignoreCase = true)
        }?.value ?: weeklyReport?.vitalPerformanceBreakdown?.hydration?.value
        val hydrationText = hydrationEval?.takeIf { it.isNotBlank() && it != "--" } ?: "Check synced daily hydration log"

        return buildString {
            appendLine("[LIVE_USER_SNAPSHOT]:")
            appendLine("- Name: ${user?.displayName ?: "VitalSync User"}, Age: ${user?.age ?: "Not set"}, Gender: ${user?.gender ?: "Not set"}")
            appendLine("- Height: ${if (heightCm > 0f) "${heightCm.toInt()} cm" else "Not set"}, Weight: ${if (profileWeight > 0f) "$profileWeight kg" else "Not set"}, BMI: ${bmiVal ?: "Not set"}")
            appendLine("- Fitness Goal: ${user?.primaryGoal ?: "General Wellness"}, Daily Targets: $targetCalories kcal | ${targetProteinG}g Protein | $stepGoal steps")
            appendLine("- Live Heart Rate: $hrText")
            appendLine("- Live SpO2: $spo2Text")
            appendLine("- Live Blood Pressure: $bpText")
            appendLine("- Today Steps: $todaySteps / $stepGoal")
            appendLine("- Hydration: $hydrationText")
            appendLine("- Latest Sleep: $sleepText")
            appendLine("- Today Nutrition: $nutritionText")
            appendLine("- Latest Activity/Workout: $latestActivityText")
        }.trim()
    }

    fun buildOllamaChatMessages(
        context: Context,
        userPrompt: String,
        recentHistory: List<ChatMessage>,
        weeklyReport: WeeklyReportDto? = null,
        aiInsight: AiInsightResponse? = null
    ): List<OllamaChatMessageDto> {
        val systemContent = buildString {
            appendLine(SYSTEM_BEHAVIOR_RULES.trim())
            appendLine()
            appendLine(NUTRITION_MACRO_REFERENCE.trim())
            appendLine()
            appendLine(buildLiveContextSnapshot(context, weeklyReport, aiInsight))
        }

        val messages = mutableListOf(
            OllamaChatMessageDto(role = "system", content = systemContent)
        )

        recentHistory
            .filter {
                it.text.isNotBlank() &&
                    !it.text.startsWith("Unable to reach VitalSync AI", ignoreCase = true) &&
                    !it.text.equals("Generation stopped.", ignoreCase = true)
            }
            .takeLast(MAX_HISTORY_MESSAGES)
            .forEach { msg ->
                val role = if (msg.sender.equals("USER", ignoreCase = true)) "user" else "assistant"
                messages.add(OllamaChatMessageDto(role = role, content = msg.text.trim()))
            }

        messages.add(OllamaChatMessageDto(role = "user", content = userPrompt.trim()))
        return messages
    }
}
