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
