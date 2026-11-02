package com.simats.vitalsync.data.sensor

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

/**
 * Unified engine interface delegating to WorkoutTrackingService.
 * Preserves backwards compatibility while utilizing real-time GPS & FusedLocationProviderClient.
 */
class PhoneWorkoutEngine private constructor(context: Context) {

    private val service = WorkoutTrackingService.getInstance(context)
    val telemetry: StateFlow<WorkoutTelemetry> = service.telemetry

    companion object {
        @Volatile
        private var INSTANCE: PhoneWorkoutEngine? = null

        fun getInstance(context: Context): PhoneWorkoutEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PhoneWorkoutEngine(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun startWorkout(
        activityType: String,
        targetType: String,
        targetMinutes: Int,
        targetDistanceKm: Float,
        userWeightKg: Float = 70.0f
    ) {
        service.startWorkout(activityType, targetType, targetMinutes, targetDistanceKm, userWeightKg)
    }

    fun startCyclingTracking() {
        service.startCyclingTracking()
    }

    fun pauseWorkout() {
        service.pauseWorkout()
    }

    fun resumeWorkout() {
        service.resumeWorkout()
    }

    fun finishWorkout(): CompletedWorkoutData {
        return service.finishWorkout()
    }

    fun stopWorkout() {
        service.stopWorkout()
    }
}
