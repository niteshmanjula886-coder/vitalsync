package com.simats.vitalsync.ui.vitals.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class StepsUiState(
    val currentSteps: Int = 0,
    val targetSteps: Int = 8000
)

class StepsViewModel : ViewModel() {
    private val _stepsState = MutableStateFlow(StepsUiState())
    val stepsState: StateFlow<StepsUiState> = _stepsState.asStateFlow()

    fun updateStepsCount(newSteps: Int) {
        _stepsState.update { it.copy(currentSteps = newSteps) }
    }

    fun updateTargetSteps(newGoal: Int) {
        _stepsState.update { it.copy(targetSteps = newGoal) }
    }
}
