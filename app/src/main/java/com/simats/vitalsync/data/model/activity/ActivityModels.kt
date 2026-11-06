package com.simats.vitalsync.data.model.activity

import com.google.gson.annotations.SerializedName

/**
 * Activity Summary Response from activity/summary.php
 */
data class ActivitySummaryResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("date")
    val date: String = "",

    @SerializedName("summary")
    val summary: ActivitySummary? = null
)

data class ActivitySummary(
    @SerializedName("steps")
    val steps: ProgressMetric = ProgressMetric(current = 0, goal = 8000, progressPercent = 0),

    @SerializedName("distance_km")
    val distanceKm: Float = 0f,

    @SerializedName("calories_burned_kcal")
    val caloriesBurned: ProgressMetric = ProgressMetric(current = 0, goal = 500, progressPercent = 0),

    @SerializedName("active_minutes")
    val activeMinutes: ProgressMetric = ProgressMetric(current = 0, goal = 45, progressPercent = 0),

    @SerializedName("sessions_count")
    val sessionsCount: Int = 0
)

data class ProgressMetric(
    @SerializedName("current")
    val current: Int = 0,

    @SerializedName("goal")
    val goal: Int = 0,

    @SerializedName("progress_percent")
    val progressPercent: Int = 0
)

/**
 * Step Goal Response from activity/step_goal.php
 */
data class StepGoalResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("daily_step_goal")
    val dailyStepGoal: Int = 8000,

    @SerializedName("estimated_distance_km")
    val estimatedDistanceKm: Float = 6.0f,

    @SerializedName("estimated_calories_kcal")
    val estimatedCaloriesKcal: Int = 320
)

/**
 * Update Step Goal Request payload
 */
data class UpdateStepGoalRequest(
    @SerializedName("email")
    val email: String? = null,

    @SerializedName("daily_step_goal")
    val dailyStepGoal: Int = 8000,

    @SerializedName("step_goal")
    val stepGoal: Int = 8000
)

/**
 * Activity Tracking Sessions Response from activity/tracking.php
 */
data class ActivitySessionsResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("count")
    val count: Int = 0,

    @SerializedName("sessions")
    val sessions: List<ActivitySession> = emptyList()
)

data class ActivitySession(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("session_uuid")
    val sessionUuid: String = "",

    @SerializedName("activity_type")
    val activityType: String = "WALKING",

    @SerializedName("duration_seconds")
    val durationSeconds: Int = 0,

    @SerializedName("duration_formatted")
    val durationFormatted: String = "",

    @SerializedName("target_type")
    val targetType: String = "time",

    @SerializedName("target_value")
    val targetValue: Int = 45,

    @SerializedName("distance_km")
    val distanceKm: Float = 0f,

    @SerializedName("calories_burned")
    val caloriesBurned: Int = 0,

    @SerializedName("avg_pace")
    val avgPace: String = "",

    @SerializedName("avg_heart_rate")
    val avgHeartRate: Int? = null,

    @SerializedName("steps")
    val steps: Int = 0,

    @SerializedName("tracking_source")
    val trackingSource: String = "phone_sensor",

    @SerializedName("start_time")
    val startTime: String = "",

    @SerializedName("end_time")
    val endTime: String = ""
)

/**
 * Payload to record an activity session in activity/tracking.php
 */
data class RecordSessionRequest(
    @SerializedName("email")
    val email: String? = null,

    @SerializedName("session_uuid")
    val sessionUuid: String? = null,

    @SerializedName("activity_type")
    val activityType: String,

    @SerializedName("duration_seconds")
    val durationSeconds: Int,

    @SerializedName("target_type")
    val targetType: String? = "time",

    @SerializedName("target_value")
    val targetValue: Int? = 45,

    @SerializedName("distance_km")
    val distanceKm: Float,

    @SerializedName("calories_burned")
    val caloriesBurned: Int,

    @SerializedName("avg_pace")
    val avgPace: String,

    @SerializedName("avg_heart_rate")
    val avgHeartRate: Int? = null,

    @SerializedName("steps")
    val steps: Int = 0,

    @SerializedName("tracking_source")
    val trackingSource: String? = "phone_sensor",

    @SerializedName("start_time")
    val startTime: String? = null,

    @SerializedName("end_time")
    val endTime: String? = null
)

/**
 * Activity History Response from activity/history.php
 */
data class HistoryResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("count")
    val count: Int = 0,

    @SerializedName("history")
    val history: List<ActivityHistoryItem> = emptyList()
)

data class ActivityHistoryItem(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("category")
    val category: String = "",

    @SerializedName("dateGroup")
    val dateGroup: String = "",

    @SerializedName("dayTag")
    val dayTag: String = "",

    @SerializedName("icon")
    val icon: String = "",

    @SerializedName("title")
    val title: String = "",

    @SerializedName("value")
    val value: String = "",

    @SerializedName("unit")
    val unit: String = "",

    @SerializedName("status")
    val status: String = "",

    @SerializedName("time")
    val time: String = ""
)


/**
 * Sync Daily Steps Request payload
 */
data class SyncDailyStepsRequest(
    @SerializedName("email")
    val email: String? = null,

    @SerializedName("step_count")
    val stepCount: Int = 0,

    @SerializedName("target_steps")
    val targetSteps: Int = 8000,

    @SerializedName("distance_km")
    val distanceKm: Float = 0f,

    @SerializedName("calories_kcal")
    val caloriesKcal: Int = 0,

    @SerializedName("active_minutes")
    val activeMinutes: Int = 0,

    @SerializedName("logged_date")
    val loggedDate: String? = null
)

/**
 * Sync Daily Steps Response
 */
data class SyncDailyStepsResponse(
    @SerializedName("status")
    val status: String = "",

    @SerializedName("message")
    val message: String = ""
)
