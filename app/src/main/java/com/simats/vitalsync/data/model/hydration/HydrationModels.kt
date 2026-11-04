package com.simats.vitalsync.data.model.hydration

import com.google.gson.annotations.SerializedName

data class HydrationResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("date") val date: String = "",
    @SerializedName("hydration") val hydration: HydrationDataDto? = null
)

data class HydrationDataDto(
    @SerializedName("consumed_ml") val consumedMl: Int = 0,
    @SerializedName("target_ml") val targetMl: Int = 2000,
    @SerializedName("glass_equivalent") val glassEquivalent: Float = 0f,
    @SerializedName("progress_percent") val progressPercent: Int = 0,
    @SerializedName("reminders_enabled") val remindersEnabled: Boolean = true,
    @SerializedName("logs") val logs: List<HydrationLogDto> = emptyList(),
    @SerializedName("weekly_history") val weeklyHistory: List<WeeklyHydrationDayDto> = emptyList()
)

data class HydrationLogDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("amount_ml") val amountMl: Int = 0,
    @SerializedName("logged_at") val loggedAt: String = "",
    @SerializedName("time_formatted") val timeFormatted: String = ""
)

data class WeeklyHydrationDayDto(
    @SerializedName("day") val day: String = "",
    @SerializedName("date") val date: String = "",
    @SerializedName("consumed_ml") val consumedMl: Int? = null,
    @SerializedName("target_ml") val targetMl: Int = 2000,
    @SerializedName("ratio") val ratio: Float? = null,
    @SerializedName("is_today") val isToday: Boolean = false
)

data class LogWaterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("amount_ml") val amountMl: Int,
    @SerializedName("date_string") val dateString: String
)

data class LogWaterResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("logged_entry") val loggedEntry: LoggedWaterEntryDto? = null,
    @SerializedName("total_consumed_ml") val totalConsumedMl: Int = 0,
    @SerializedName("target_ml") val targetMl: Int = 2000,
    @SerializedName("glass_equivalent") val glassEquivalent: Float = 0f,
    @SerializedName("progress_percent") val progressPercent: Int = 0
)

data class LoggedWaterEntryDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("amount_ml") val amountMl: Int = 0,
    @SerializedName("date") val date: String = ""
)

data class UpdateHydrationSettingsRequest(
    @SerializedName("email") val email: String,
    @SerializedName("target_ml") val targetMl: Int,
    @SerializedName("reminders_enabled") val remindersEnabled: Boolean
)

data class UpdateHydrationSettingsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("daily_target_ml") val dailyTargetMl: Int = 2000,
    @SerializedName("reminders_enabled") val remindersEnabled: Boolean = true
)

data class ResetHydrationResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("total_consumed_ml") val totalConsumedMl: Int = 0,
    @SerializedName("glass_equivalent") val glassEquivalent: Float = 0f,
    @SerializedName("progress_percent") val progressPercent: Int = 0
)

data class WeeklyHydrationHistoryResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: WeeklyHydrationHistoryDataDto? = null
)

data class WeeklyHydrationHistoryDataDto(
    @SerializedName("user_id") val userId: Int = 0,
    @SerializedName("daily_target_ml") val dailyTargetMl: Int = 2500,
    @SerializedName("weekly_target_ml") val weeklyTargetMl: Int = 17500,
    @SerializedName("weeks_count") val weeksCount: Int = 0,
    @SerializedName("weekly_history") val weeklyHistory: List<WeeklyHistoryItemDto> = emptyList(),
    @SerializedName("weeks") val weeks: List<WeeklyHistoryItemDto> = emptyList(),
    @SerializedName("summary") val summary: WeeklyHydrationSummaryDto? = null,
    @SerializedName("ai_insight") val aiInsight: HydrationAiInsightDto? = null
)

data class WeeklyHistoryItemDto(
    @SerializedName("week_key") val weekKey: Int = 0,
    @SerializedName("week_number") val weekNumber: Int = 0,
    @SerializedName("year") val year: Int = 0,
    @SerializedName("week_label") val weekLabel: String = "",
    @SerializedName("week_start_date") val weekStartDate: String = "",
    @SerializedName("week_end_date") val weekEndDate: String = "",
    @SerializedName("logged_days") val loggedDays: Int = 0,
    @SerializedName("total_volume_ml") val totalVolumeMl: Int = 0,
    @SerializedName("avg_daily_ml") val avgDailyMl: Int = 0,
    @SerializedName("max_single_day_ml") val maxSingleDayMl: Int = 0,
    @SerializedName("weekly_target_ml") val weeklyTargetMl: Int = 0,
    @SerializedName("daily_target_ml") val dailyTargetMl: Int = 0,
    @SerializedName("completion_rate_percent") val completionRatePercent: Float = 0f,
    @SerializedName("daily_target_achievement_percent") val dailyTargetAchievementPercent: Float = 0f,
    @SerializedName("status") val status: String = "",
    @SerializedName("is_current_week") val isCurrentWeek: Boolean = false
)

data class WeeklyHydrationSummaryDto(
    @SerializedName("total_volume_8_weeks") val totalVolume8Weeks: Int = 0,
    @SerializedName("overall_avg_daily_ml") val overallAvgDailyMl: Int = 0,
    @SerializedName("weekly_average_ml") val weeklyAverageMl: Int = 0,
    @SerializedName("total_logged_days") val totalLoggedDays: Int = 0,
    @SerializedName("weeks_tracked") val weeksTracked: Int = 0,
    @SerializedName("daily_target_ml") val dailyTargetMl: Int = 0,
    @SerializedName("weekly_target_ml") val weeklyTargetMl: Int = 0,
    @SerializedName("trend_direction") val trendDirection: String = "stable",
    @SerializedName("trend_percentage") val trendPercentage: Float = 0f,
    @SerializedName("consistency_rating") val consistencyRating: String = ""
)

data class HydrationAiInsightDto(
    @SerializedName("title") val title: String = "",
    @SerializedName("summary") val summary: String = "",
    @SerializedName("actionable_advice") val actionableAdvice: String = "",
    @SerializedName("hydration_score") val hydrationScore: Int = 0,
    @SerializedName("model_used") val modelUsed: String = ""
)
