package com.simats.vitalsync.ui.vitals.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.simats.vitalsync.data.local.SessionManager
import com.simats.vitalsync.data.sensor.StepSensorManager
import com.simats.vitalsync.util.NotificationReminderEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class StepsUiState(
    val currentSteps: Int = 0,
    val targetSteps: Int = 8000,
    val distanceKm: Float = 0f,
    val caloriesBurned: Int = 0,
    val activeMinutes: Int = 0,
    val isLive: Boolean = false,
    val sensorType: String = "",
    val isReminderEnabled: Boolean = true,
    val reminderMode: String = "HOURLY",
    val reminderTime: String = "07:00 PM"
)

class StepsViewModel : ViewModel() {
    private val _stepsState = MutableStateFlow(StepsUiState())
    val stepsState: StateFlow<StepsUiState> = _stepsState.asStateFlow()

    fun initReminder(context: Context) {
        val sessionManager = SessionManager.getInstance(context)
        val enabled = sessionManager.isStepsReminderEnabled()
        val mode = sessionManager.getStepsReminderMode()
        val time = sessionManager.getStepsReminderTime()
        _stepsState.update {
            it.copy(
                isReminderEnabled = enabled,
                reminderMode = mode,
                reminderTime = time
            )
        }
        // Ensure scheduler matches active mode and time
        NotificationReminderEngine.syncStepsReminder(context, enabled, mode, time)
    }

    fun updateReminderSettings(
        context: Context,
        enabled: Boolean,
        mode: String,
        time: String
    ) {
        val sessionManager = SessionManager.getInstance(context)
        sessionManager.setStepsReminderSettings(enabled, mode, time)

        NotificationReminderEngine.syncStepsReminder(context, enabled, mode, time)

        _stepsState.update {
            it.copy(
                isReminderEnabled = enabled,
                reminderMode = mode,
                reminderTime = time
            )
        }
    }

    fun toggleReminder(context: Context, enabled: Boolean) {
        val current = _stepsState.value
        updateReminderSettings(context, enabled, current.reminderMode, current.reminderTime)
    }

    fun updateReminderSettings(context: Context, enabled: Boolean, time: String = "") {
        val current = _stepsState.value
        val mode = if (time.isNotBlank()) "MANUAL" else current.reminderMode
        val t = if (time.isNotBlank()) time else current.reminderTime
        updateReminderSettings(context, enabled, mode, t)
    }

    fun updateStepsCount(newSteps: Int) {
        _stepsState.update {
            it.copy(
                currentSteps = newSteps,
                distanceKm = newSteps * 0.00076f,
                caloriesBurned = (newSteps * 0.04f).toInt(),
                activeMinutes = (newSteps / 100),
                isLive = true
            )
        }
    }

    fun updateTargetSteps(newGoal: Int) {
        _stepsState.update { it.copy(targetSteps = newGoal) }
    }

    fun syncFromSensor(sensorManager: StepSensorManager) {
        val steps = sensorManager.liveSteps.value
        val dist = sensorManager.liveDistanceKm.value
        val cal = sensorManager.liveCalories.value
        _stepsState.update {
            it.copy(
                currentSteps = steps,
                distanceKm = dist,
                caloriesBurned = cal,
                activeMinutes = steps / 100,
                isLive = sensorManager.isSensorActive.value,
                sensorType = sensorManager.sensorName.value
            )
        }
    }
}
