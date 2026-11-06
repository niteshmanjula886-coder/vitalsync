package com.simats.vitalsync.ui.vitals.viewmodel

import androidx.lifecycle.ViewModel
import com.simats.vitalsync.data.sensor.StepSensorManager
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
    val sensorType: String = ""
)

class StepsViewModel : ViewModel() {
    private val _stepsState = MutableStateFlow(StepsUiState())
    val stepsState: StateFlow<StepsUiState> = _stepsState.asStateFlow()

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
