package com.simats.vitalsync.data.sensor

data class WorkoutTelemetry(
    val activityType: String = "",
    val targetType: String = "",
    val targetMinutes: Int = 0,
    val targetDistanceKm: Float = 0f,
    val durationSeconds: Int = 0,
    val formattedDuration: String = "00:00",
    val steps: Int = 0,
    val distanceKm: Float = 0f,
    val caloriesBurned: Int = 0,
    val avgPace: String = "",
    val currentSpeedKmH: Float = 0f,
    val avgSpeedKmH: Float = 0f,
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val progressRatio: Float = 0f,
    val progressPercent: Int = 0,
    val remainingFormatted: String = ""
)

data class CompletedWorkoutData(
    val sessionUuid: String,
    val activityType: String,
    val startTime: String,
    val endTime: String,
    val durationSeconds: Int,
    val targetType: String,
    val targetValue: Int,
    val steps: Int,
    val distanceKm: Float,
    val caloriesBurned: Int,
    val avgPace: String,
    val trackingSource: String
)
