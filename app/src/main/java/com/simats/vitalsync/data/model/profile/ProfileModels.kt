package com.simats.vitalsync.data.model.profile

import com.google.gson.annotations.SerializedName
import com.simats.vitalsync.data.model.auth.User
import kotlin.math.roundToInt

data class ProfileResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("profile")
    val profile: User? = null
)

data class UpdateProfileRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("full_name")
    val fullName: String? = null,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("dob")
    val dob: String? = null,
    @SerializedName("age")
    val age: Int? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("height")
    val height: Float? = null,
    @SerializedName("weight")
    val weight: Float? = null,
    @SerializedName("activity_level")
    val activityLevel: String? = null,
    @SerializedName("primary_goal")
    val primaryGoal: String? = null,
    @SerializedName("step_goal")
    val stepGoal: Int? = null,
    @SerializedName("clear_profile_image")
    val clearProfileImage: Boolean? = null
)

data class UploadProfileResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("profile_image")
    val profileImage: String? = null,
    @SerializedName("image_url", alternate = ["profile_image_url", "avatar_url"])
    val imageUrl: String? = null
)

data class HealthGoalItem(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("title")
    val title: String = "",
    @SerializedName("badge")
    val badge: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("icon_type")
    val iconType: String = "FITNESS"
)

data class HealthGoalsDetailDto(
    @SerializedName("target_steps", alternate = ["step_goal", "daily_step_goal"])
    val targetSteps: Int? = null,
    @SerializedName("target_sleep_hours")
    val targetSleepHours: Double? = null,
    @SerializedName("target_water_ml")
    val targetWaterMl: Int? = null,
    @SerializedName("target_weight_kg")
    val targetWeightKg: Double? = null,
    @SerializedName("target_calories")
    val targetCalories: Int? = null
)

data class HealthGoalsResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("selected_goal")
    val selectedGoal: String? = null,
    @SerializedName("primary_goal")
    val primaryGoal: String? = null,
    @SerializedName("step_goal", alternate = ["target_steps", "daily_step_goal"])
    val stepGoal: Int? = null,
    @SerializedName("goals")
    val goals: HealthGoalsDetailDto? = null,
    @SerializedName("available_goals")
    val availableGoals: List<HealthGoalItem> = emptyList()
)

data class SaveHealthGoalRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("goal_id")
    val goalId: String? = null,
    @SerializedName("goal_title")
    val goalTitle: String? = null,
    @SerializedName("primary_goal")
    val primaryGoal: String? = null,
    @SerializedName("step_goal")
    val stepGoal: Int? = null,
    @SerializedName("target_steps")
    val targetSteps: Int? = null
)

data class ReminderItem(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("user_id")
    val userId: Int = 0,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("reminder_type")
    val reminderType: String? = null,
    @SerializedName("time")
    val time: String = "08:00 AM",
    @SerializedName("frequency")
    val frequency: String = "Every day",
    @SerializedName("repeat_days")
    val repeatDays: String? = null,
    @SerializedName("sound_vibration")
    val soundVibration: Boolean = true,
    @SerializedName("is_enabled")
    val isEnabled: Boolean = true,
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class RemindersResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("count")
    val count: Int = 0,
    @SerializedName("reminders")
    val reminders: List<ReminderItem> = emptyList()
)

data class AddReminderRequest(
    @SerializedName("user_id")
    val userId: Int = 0,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("title")
    val title: String,
    @SerializedName("reminder_type")
    val reminderType: String = "General",
    @SerializedName("time")
    val time: String = "08:00 AM",
    @SerializedName("frequency")
    val frequency: String = "Every day",
    @SerializedName("repeat_days")
    val repeatDays: String = "0,1,2,3,4,5,6",
    @SerializedName("sound_vibration")
    val soundVibration: Boolean = true,
    @SerializedName("is_enabled")
    val isEnabled: Boolean = true,
    @SerializedName("notes")
    val notes: String? = null
)

data class UpdateReminderRequest(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("time")
    val time: String? = null,
    @SerializedName("frequency")
    val frequency: String? = null,
    @SerializedName("is_enabled")
    val isEnabled: Boolean? = null
)

data class AppSettingsDto(
    @SerializedName("user_id")
    val userId: Int = 0,
    @SerializedName("push_notifications")
    val pushNotifications: Boolean = true,
    @SerializedName("vital_alerts")
    val vitalAlerts: Boolean = true,
    @SerializedName("daily_reminders")
    val dailyReminders: Boolean = true,
    @SerializedName("metric_units")
    val metricUnits: String = "metric",
    @SerializedName("dark_mode")
    val darkMode: Boolean = false,
    @SerializedName("data_sync_interval")
    val dataSyncInterval: Int = 15,
    @SerializedName("ai_insights")
    val aiInsights: Boolean = true,
    @SerializedName("usage_analytics")
    val usageAnalytics: Boolean = false,
    @SerializedName("ai_insights_enabled")
    val aiInsightsEnabled: Boolean = true,
    @SerializedName("user_analytics_enabled")
    val userAnalyticsEnabled: Boolean = false,
    @SerializedName("reminder_water_interval")
    val reminderWaterInterval: Int = 120,
    @SerializedName("reminder_workout_enabled")
    val reminderWorkoutEnabled: Boolean = true,
    @SerializedName("reminder_sleep_enabled")
    val reminderSleepEnabled: Boolean = true,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class AppSettingsResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("settings")
    val settings: AppSettingsDto? = null
)

data class UpdateAppSettingsRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("push_notifications")
    val pushNotifications: Boolean? = null,
    @SerializedName("vital_alerts")
    val vitalAlerts: Boolean? = null,
    @SerializedName("daily_reminders")
    val dailyReminders: Boolean? = null,
    @SerializedName("metric_units")
    val metricUnits: String? = null,
    @SerializedName("dark_mode")
    val darkMode: Boolean? = null,
    @SerializedName("data_sync_interval")
    val dataSyncInterval: Int? = null,
    @SerializedName("ai_insights")
    val aiInsights: Boolean? = null,
    @SerializedName("usage_analytics")
    val usageAnalytics: Boolean? = null,
    @SerializedName("ai_insights_enabled")
    val aiInsightsEnabled: Boolean? = null,
    @SerializedName("user_analytics_enabled")
    val userAnalyticsEnabled: Boolean? = null,
    @SerializedName("reminder_water_interval")
    val reminderWaterInterval: Int? = null,
    @SerializedName("reminder_workout_enabled")
    val reminderWorkoutEnabled: Boolean? = null,
    @SerializedName("reminder_sleep_enabled")
    val reminderSleepEnabled: Boolean? = null
)

data class HealthPermissionsDto(
    @SerializedName("vital_signs")
    val vitalSigns: Boolean = true,
    @SerializedName("activity")
    val activity: Boolean = true,
    @SerializedName("sleep")
    val sleep: Boolean = true,
    @SerializedName("hydration")
    val hydration: Boolean = false,
    @SerializedName("write_health_data")
    val writeHealthData: Boolean = true
)

data class HealthPermissionsResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("permissions")
    val permissions: HealthPermissionsDto? = null
)

data class UpdateHealthPermissionsRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("vital_signs")
    val vitalSigns: Boolean? = null,
    @SerializedName("activity")
    val activity: Boolean? = null,
    @SerializedName("sleep")
    val sleep: Boolean? = null,
    @SerializedName("hydration")
    val hydration: Boolean? = null,
    @SerializedName("write_health_data")
    val writeHealthData: Boolean? = null
)

data class LoginSessionItem(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("device_name")
    val deviceName: String = "",
    @SerializedName("device_os")
    val deviceOs: String? = null,
    @SerializedName("ip_address")
    val ipAddress: String? = null,
    @SerializedName("location")
    val location: String? = null,
    @SerializedName("is_current")
    val isCurrent: Boolean = false,
    @SerializedName("last_active")
    val lastActive: String? = null
)

data class LoginSessionsResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("count")
    val count: Int = 0,
    @SerializedName("sessions")
    val sessions: List<LoginSessionItem> = emptyList()
)

data class LogoutOthersRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("action")
    val action: String = "logout_others"
)

data class NotificationItem(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("icon")
    val icon: String? = null,
    @SerializedName("is_read")
    val isRead: Boolean = false,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("time_ago")
    val timeAgo: String? = null
)

data class NotificationsResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("unread_count")
    val unreadCount: Int = 0,
    @SerializedName("notifications")
    val notifications: List<NotificationItem> = emptyList()
)

data class NotificationActionRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("action")
    val action: String? = null,
    @SerializedName("id")
    val id: Int? = null
)

data class AppVersionResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("version")
    val version: String = "1.0",
    @SerializedName("version_name")
    val versionName: String = "1.0 - Stable",
    @SerializedName("build_type")
    val buildType: String = "Release",
    @SerializedName("is_up_to_date")
    val isUpToDate: Boolean = true,
    @SerializedName("checked_at")
    val checkedAt: String? = null,
    @SerializedName("features")
    val features: List<String> = emptyList()
)

data class SupportTicketItem(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("subject")
    val subject: String = "",
    @SerializedName("message")
    val message: String = "",
    @SerializedName("status")
    val status: String = "open",
    @SerializedName("created_at")
    val createdAt: String? = null
)

data class SupportTicketsResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("email_support")
    val emailSupport: String? = null,
    @SerializedName("hours")
    val hours: String? = null,
    @SerializedName("tickets")
    val tickets: List<SupportTicketItem> = emptyList()
)

data class CreateSupportTicketRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("subject")
    val subject: String = "General In-App Inquiry",
    @SerializedName("message")
    val message: String
)

data class BaseProfileResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null
)

data class WeeklySleepDayDto(
    @SerializedName("date")
    val date: String = "",
    @SerializedName("day_label")
    val dayLabel: String = "",
    @SerializedName("duration_minutes", alternate = ["duration"])
    val durationMinutes: Int = 0,
    @SerializedName("quality_score", alternate = ["sleep_score"])
    val qualityScore: Int = 0,
    @SerializedName("quality_label", alternate = ["quality"])
    val qualityLabel: String = "No Log"
)

data class WeeklyReportScoresDto(
    @SerializedName("heart_rate")
    val heartRate: Int = 0,
    @SerializedName("blood_pressure")
    val bloodPressure: Int = 0,
    @SerializedName("spo2")
    val spo2: Int = 0,
    @SerializedName("sleep")
    val sleep: Int = 0,
    @SerializedName("hydration")
    val hydration: Int = 0,
    @SerializedName("activity")
    val activity: Int = 0
)

data class MetricBreakdownItemDto(
    @SerializedName("value")
    val value: String = "--",
    @SerializedName("status")
    val status: String = "No Data",
    @SerializedName("score")
    val score: Int = 0,
    @SerializedName("total_ml")
    val totalMl: Int? = null,
    @SerializedName("goal_ml")
    val goalMl: Int? = null,
    @SerializedName("total_steps")
    val totalSteps: Int? = null,
    @SerializedName("goal_steps")
    val goalSteps: Int? = null,
    @SerializedName("completion_percent")
    val completionPercent: Int? = null,
    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,
    @SerializedName("distance_km")
    val distanceKm: Double? = null,
    @SerializedName("calories_kcal")
    val caloriesKcal: Int? = null
)

data class ExecutiveSummaryDto(
    @SerializedName("summary")
    val summary: String = "",
    @SerializedName("key_findings")
    val keyFindings: List<String> = emptyList(),
    @SerializedName("recommendations")
    val recommendations: List<String> = emptyList()
)

data class VitalPerformanceBreakdownDto(
    @SerializedName("overall_wellness_score")
    val overallWellnessScore: Int = 0,
    @SerializedName("overall_status")
    val overallStatus: String = "No Data",
    @SerializedName("heart_rate")
    val heartRate: MetricBreakdownItemDto? = null,
    @SerializedName("blood_pressure")
    val bloodPressure: MetricBreakdownItemDto? = null,
    @SerializedName("spo2")
    val spo2: MetricBreakdownItemDto? = null,
    @SerializedName("sleep")
    val sleep: MetricBreakdownItemDto? = null,
    @SerializedName("hydration")
    val hydration: MetricBreakdownItemDto? = null,
    @SerializedName("activity")
    val activity: MetricBreakdownItemDto? = null
)

data class VitalPerformanceBreakdown(
    val overallWellnessScore: Int,
    val overallStatus: String,
    val heartRateValue: String,
    val heartRateStatus: String,
    val bpValue: String,
    val bpStatus: String,
    val spo2Value: String,
    val spo2Status: String,
    val sleepValue: String,
    val sleepStatus: String,
    val hydrationValue: String,
    val hydrationStatus: String,
    val activityValue: String,
    val activityStatus: String,
    val observationText: String,
    val keyFindings: List<String> = emptyList(),
    val recommendations: List<String> = emptyList()
)

data class WeeklyReportDto(
    @SerializedName("week_start_date")
    val weekStartDate: String = "",
    @SerializedName("week_end_date")
    val weekEndDate: String = "",
    @SerializedName("wellness_score")
    val wellnessScore: Int = 0,
    @SerializedName("performance_category")
    val performanceCategory: String = "low",
    @SerializedName("category_label")
    val categoryLabel: String = "Low",
    @SerializedName("average_duration_minutes")
    val averageDurationMinutes: Int = 0,
    @SerializedName("average_sleep_quality", alternate = ["average_sleep_score"])
    val averageSleepQuality: Int = 0,
    @SerializedName("logged_sleep_days")
    val loggedSleepDays: Int = 0,
    @SerializedName("scores")
    val scores: WeeklyReportScoresDto? = null,
    @SerializedName("summary_text")
    val summaryText: String = "",
    @SerializedName("sleep_days")
    val sleepDays: List<WeeklySleepDayDto> = emptyList(),
    @SerializedName("metrics")
    val metrics: WeeklyReportMetricsDto? = null,
    @SerializedName("vital_performance_breakdown")
    val vitalPerformanceBreakdown: VitalPerformanceBreakdownDto? = null,
    @SerializedName("executive_summary")
    val executiveSummary: ExecutiveSummaryDto? = null
) {
    /**
     * Constructs a unified, validated VitalPerformanceBreakdown domain object, preferring
     * calculated breakdown fields from the backend while falling back safely to metrics and client state.
     */
    fun toVitalPerformanceBreakdown(
        fallbackScore: Int = 0,
        fallbackStatus: String = "No Data",
        fallbackHr: String = "-- BPM",
        fallbackHrStatus: String = "No Data",
        fallbackBp: String = "--/--",
        fallbackBpStatus: String = "No Data",
        fallbackSpo2: String = "--%",
        fallbackSpo2Status: String = "No Data",
        fallbackSleepVal: String = "--",
        fallbackSleepStatus: String = "No Logs Recorded",
        fallbackHydrationVal: String = "-- ml",
        fallbackHydrationStatus: String = "No Data",
        fallbackStepsVal: String = "-- steps",
        fallbackStepsStatus: String = "No Data",
        fallbackObs: String = ""
    ): VitalPerformanceBreakdown {
        val b = vitalPerformanceBreakdown
        val exec = executiveSummary

        val score = b?.overallWellnessScore?.takeIf { it > 0 }
            ?: wellnessScore.takeIf { it > 0 }
            ?: fallbackScore

        val status = b?.overallStatus?.takeIf { it.isNotBlank() && !it.equals("No Data", ignoreCase = true) }
            ?: categoryLabel.takeIf { it.isNotBlank() && !it.equals("Low", ignoreCase = true) }
            ?: fallbackStatus

        val hrVal = b?.heartRate?.value?.takeIf { it != "-- BPM" && it.isNotBlank() }
            ?: metrics?.heartRateAvg?.takeIf { it != "-- BPM" && it.isNotBlank() }
            ?: fallbackHr

        val hrStat = b?.heartRate?.status?.takeIf { it.isNotBlank() && !it.equals("No Data", ignoreCase = true) }
            ?: metrics?.heartRateStatus?.takeIf { it.isNotBlank() && !it.equals("No Data", ignoreCase = true) }
            ?: fallbackHrStatus

        val bpVal = b?.bloodPressure?.value?.takeIf { it != "--/--" && it.isNotBlank() }
            ?: metrics?.bpAvg?.takeIf { it != "--/--" && it.isNotBlank() }
            ?: fallbackBp

        val bpStat = b?.bloodPressure?.status?.takeIf { it.isNotBlank() && !it.equals("No Data", ignoreCase = true) }
            ?: metrics?.bpStatus?.takeIf { it.isNotBlank() && !it.equals("No Data", ignoreCase = true) }
            ?: fallbackBpStatus

        val spo2Val = b?.spo2?.value?.takeIf { it != "--%" && it.isNotBlank() }
            ?: metrics?.spo2Avg?.takeIf { it != "--%" && it.isNotBlank() }
            ?: fallbackSpo2

        val spo2Stat = b?.spo2?.status?.takeIf { it.isNotBlank() && !it.equals("No Data", ignoreCase = true) }
            ?: metrics?.spo2Status?.takeIf { it.isNotBlank() && !it.equals("No Data", ignoreCase = true) }
            ?: fallbackSpo2Status

        val sleepVal = b?.sleep?.value?.takeIf { it != "--" && it.isNotBlank() }
            ?: resolvedBreakdownSleepValue().takeIf { it != "--" }
            ?: fallbackSleepVal

        val sleepStat = b?.sleep?.status?.takeIf { it.isNotBlank() && !it.equals("No Logs Recorded", ignoreCase = true) }
            ?: resolvedBreakdownSleepStatus().takeIf { it != "No Logs Recorded" }
            ?: fallbackSleepStatus

        val hydVal = b?.hydration?.value?.takeIf { it != "0 ml" && it != "-- ml" && it.isNotBlank() }
            ?: metrics?.hydrationTotal?.takeIf { it != "0 ml" && it != "-- ml" && it.isNotBlank() }
            ?: fallbackHydrationVal

        val hydStat = b?.hydration?.status?.takeIf { it.isNotBlank() && !it.equals("No Data", ignoreCase = true) }
            ?: metrics?.hydrationStatus?.takeIf { it.isNotBlank() && !it.equals("No Data", ignoreCase = true) }
            ?: fallbackHydrationStatus

        val actVal = b?.activity?.value?.takeIf { it != "0 steps" && it != "-- steps" && it.isNotBlank() }
            ?: metrics?.activitySteps?.takeIf { it != "0 steps" && it != "-- steps" && it.isNotBlank() }
            ?: fallbackStepsVal

        val actStat = b?.activity?.status?.takeIf { it.isNotBlank() && !it.equals("No Data", ignoreCase = true) }
            ?: metrics?.activityStatus?.takeIf { it.isNotBlank() && !it.equals("No Data", ignoreCase = true) }
            ?: fallbackStepsStatus

        val obs = exec?.summary?.takeIf { it.isNotBlank() }
            ?: summaryText.takeIf { it.isNotBlank() }
            ?: fallbackObs

        return VitalPerformanceBreakdown(
            overallWellnessScore = score,
            overallStatus = status,
            heartRateValue = hrVal,
            heartRateStatus = hrStat,
            bpValue = bpVal,
            bpStatus = bpStat,
            spo2Value = spo2Val,
            spo2Status = spo2Stat,
            sleepValue = sleepVal,
            sleepStatus = sleepStat,
            hydrationValue = hydVal,
            hydrationStatus = hydStat,
            activityValue = actVal,
            activityStatus = actStat,
            observationText = obs,
            keyFindings = exec?.keyFindings ?: emptyList(),
            recommendations = exec?.recommendations ?: emptyList()
        )
    }
    /**
     * Resolve average nightly sleep minutes across explicit backend fields and logged sleep_days entries.
     */
    fun resolvedAvgSleepMinutes(): Int {
        if (averageDurationMinutes > 0) return averageDurationMinutes
        val metricDur = metrics?.averageDurationMinutes ?: 0
        if (metricDur > 0) return metricDur
        val metricAvg = metrics?.avgSleepMinutes ?: 0
        if (metricAvg > 0) return metricAvg
        val logged = sleepDays.filter { it.durationMinutes > 0 }
        return if (logged.isNotEmpty()) logged.map { it.durationMinutes }.average().roundToInt() else 0
    }

    /**
     * Resolve average sleep quality percentage/score across backend aggregates or derived per-day scores.
     */
    fun resolvedAvgSleepQuality(): Int {
        val avgMins = resolvedAvgSleepMinutes()
        if (avgMins <= 0) return 0
        if (averageSleepQuality > 0) return averageSleepQuality
        val metricQual = metrics?.averageSleepQuality ?: 0
        if (metricQual > 0) return metricQual
        val scoreQual = scores?.sleep ?: 0
        if (scoreQual > 0) return scoreQual
        val logged = sleepDays.filter { it.durationMinutes > 0 }
        if (logged.isNotEmpty()) {
            val avg = logged.map { day ->
                if (day.qualityScore > 0) day.qualityScore else deriveSleepQualityScore(day.durationMinutes)
            }.average().roundToInt()
            if (avg > 0) return avg
        }
        return deriveSleepQualityScore(avgMins)
    }

    fun resolvedSleepQualityTier(): String {
        val avgMins = resolvedAvgSleepMinutes()
        if (avgMins <= 0) return "No Logs Recorded"
        val backendStatus = metrics?.sleepStatus?.trim().orEmpty()
        return when {
            backendStatus.equals("Restorative", ignoreCase = true) ||
                backendStatus.equals("Deep / Restorative", ignoreCase = true) -> "Excellent"
            backendStatus.isNotEmpty() &&
                !backendStatus.equals("No Data", ignoreCase = true) &&
                !backendStatus.equals("No Log", ignoreCase = true) -> backendStatus
            else -> deriveSleepQualityTier(avgMins)
        }
    }

    /**
     * Formatted sleep quality string for the Weekly Health Report Vital Performance Breakdown card.
     */
    fun resolvedBreakdownSleepValue(): String {
        val avgMins = resolvedAvgSleepMinutes()
        val avgQuality = resolvedAvgSleepQuality()
        if (avgMins <= 0 || avgQuality <= 0) return "--"
        return "$avgQuality% - ${resolvedSleepQualityTier()}"
    }

    fun resolvedBreakdownSleepStatus(): String {
        val avgMins = resolvedAvgSleepMinutes()
        if (avgMins <= 0) return "No Logs Recorded"
        return "${avgMins / 60}h ${avgMins % 60}m avg"
    }

    /**
     * Quality tier label for the AI Screen "Your weekly trends" Sleep card.
     */
    fun resolvedSleepTrendTier(): String {
        val avgMins = resolvedAvgSleepMinutes()
        if (avgMins <= 0) return "No logs yet"
        val avgQuality = resolvedAvgSleepQuality()
        val tier = resolvedSleepQualityTier()
        return when {
            tier.equals("Excellent", ignoreCase = true) ||
                tier.equals("Optimal", ignoreCase = true) ||
                tier.equals("Restorative", ignoreCase = true) ||
                tier.equals("Deep / Restorative", ignoreCase = true) ||
                avgQuality >= 75 -> "Optimal"
            tier.equals("Fair", ignoreCase = true) || avgQuality >= 60 -> "Fair"
            else -> "Needs Attention"
        }
    }

    companion object {
        fun deriveSleepQualityScore(durationMinutes: Int): Int = when {
            durationMinutes <= 0 -> 0
            durationMinutes < 360 -> 50
            durationMinutes < 420 -> 68
            durationMinutes <= 510 -> 88
            else -> 92
        }

        fun deriveSleepQualityTier(durationMinutes: Int): String = when {
            durationMinutes <= 0 -> "No Log"
            durationMinutes < 360 -> "Poor"
            durationMinutes < 420 -> "Fair"
            durationMinutes <= 510 -> "Optimal"
            else -> "Excellent"
        }
    }
}

data class WeeklyReportMetricsDto(
    @SerializedName("heart_rate_avg")
    val heartRateAvg: String = "-- BPM",
    @SerializedName("heart_rate_status")
    val heartRateStatus: String = "No Data",
    @SerializedName("avg_hr_bpm")
    val avgHrBpm: Int? = null,
    @SerializedName("bp_avg")
    val bpAvg: String = "--/--",
    @SerializedName("bp_status")
    val bpStatus: String = "No Data",
    @SerializedName("spo2_avg")
    val spo2Avg: String = "--%",
    @SerializedName("spo2_status")
    val spo2Status: String = "No Data",
    @SerializedName("sleep_quality")
    val sleepQuality: String = "--",
    @SerializedName("sleep_status")
    val sleepStatus: String = "No Data",
    @SerializedName("avg_sleep_minutes")
    val avgSleepMinutes: Int = 0,
    @SerializedName("average_duration_minutes")
    val averageDurationMinutes: Int? = null,
    @SerializedName("average_sleep_quality", alternate = ["sleep_score"])
    val averageSleepQuality: Int? = null,
    @SerializedName("hydration_total")
    val hydrationTotal: String = "0 ml",
    @SerializedName("hydration_status")
    val hydrationStatus: String = "No Data",
    @SerializedName("total_hydration_ml")
    val totalHydrationMl: Int = 0,
    @SerializedName("activity_steps")
    val activitySteps: String = "0 steps",
    @SerializedName("activity_status")
    val activityStatus: String = "No Data",
    @SerializedName("total_steps")
    val totalSteps: Int = 0,
    @SerializedName("workout_count")
    val workoutCount: Int = 0
)

data class WeeklyReportResponse(
    @SerializedName("status")
    val status: String = "success",
    @SerializedName("success")
    val success: Boolean = true,
    @SerializedName("message")
    val message: String = "",
    @SerializedName("data")
    val data: WeeklyReportDto? = null
)