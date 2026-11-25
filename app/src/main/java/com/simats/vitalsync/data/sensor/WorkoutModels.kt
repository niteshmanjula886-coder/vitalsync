package com.simats.vitalsync.data.sensor

data class LocationCoordinate(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val speedKmh: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

enum class GpsSignalStatus {
    DISABLED,
    NO_PERMISSION,
    SEARCHING,
    FAIR,
    LOCKED
}

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
    val remainingFormatted: String = "",
    // Live GPS telemetry for cycling map and location-driven tracking
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val currentBearing: Float = 0f,
    val gpsAccuracyMeters: Float = 0f,
    val gpsSignalStatus: GpsSignalStatus = GpsSignalStatus.SEARCHING,
    val routeCoordinates: List<LocationCoordinate> = emptyList(),
    // Target destination / area pin tracking (osmdroid zero-API-key)
    val targetLatitude: Double? = null,
    val targetLongitude: Double? = null,
    val targetRadiusMeters: Float = 100f,
    val initialDistanceToTargetMeters: Float? = null,
    val distanceToTargetMeters: Float? = null,
    val isTargetReached: Boolean = false
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
    val trackingSource: String,
    val routeCoordinates: List<LocationCoordinate> = emptyList()
)
