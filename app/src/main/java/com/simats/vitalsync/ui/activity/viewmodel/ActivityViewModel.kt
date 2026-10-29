package com.simats.vitalsync.ui.activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.vitalsync.data.model.activity.RecordSessionRequest
import com.simats.vitalsync.data.repository.ActivityRepository
import com.simats.vitalsync.data.repository.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing Activity Summaries, Step Goals, Activity Tracking Sessions & History.
 * Follows the Android MVVM architecture pattern.
 */
class ActivityViewModel(
    private val repository: ActivityRepository
) : ViewModel() {

    private val _summaryState = MutableStateFlow(ActivitySummaryUiState())
    val summaryState: StateFlow<ActivitySummaryUiState> = _summaryState.asStateFlow()

    private val _stepGoalState = MutableStateFlow(StepGoalUiState())
    val stepGoalState: StateFlow<StepGoalUiState> = _stepGoalState.asStateFlow()

    private val _sessionsState = MutableStateFlow(ActivitySessionsUiState())
    val sessionsState: StateFlow<ActivitySessionsUiState> = _sessionsState.asStateFlow()

    private val _saveSessionState = MutableStateFlow(SaveSessionUiState())
    val saveSessionState: StateFlow<SaveSessionUiState> = _saveSessionState.asStateFlow()

    private val _historyState = MutableStateFlow(HistoryUiState())
    val historyState: StateFlow<HistoryUiState> = _historyState.asStateFlow()

    fun loadSummary(email: String) {
        viewModelScope.launch {
            repository.getActivitySummary(email).collect { res ->
                when (res) {
                    is Resource.Loading -> _summaryState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _summaryState.update { it.copy(isLoading = false, summary = res.data, errorMessage = null) }
                    is Resource.Error -> _summaryState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
            }
        }
    }

    fun loadStepGoal(email: String) {
        viewModelScope.launch {
            repository.getStepGoal(email).collect { res ->
                when (res) {
                    is Resource.Loading -> _stepGoalState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _stepGoalState.update {
                        it.copy(
                            isLoading = false,
                            dailyStepGoal = res.data.dailyStepGoal,
                            estimatedDistanceKm = res.data.estimatedDistanceKm,
                            estimatedCaloriesKcal = res.data.estimatedCaloriesKcal,
                            errorMessage = null
                        )
                    }
                    is Resource.Error -> _stepGoalState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
            }
        }
    }

    fun updateStepGoal(email: String, newGoal: Int) {
        viewModelScope.launch {
            repository.updateStepGoal(email, newGoal).collect { res ->
                when (res) {
                    is Resource.Loading -> _stepGoalState.update { it.copy(isLoading = true, errorMessage = null, isSavedSuccess = false) }
                    is Resource.Success -> {
                        _stepGoalState.update {
                            it.copy(
                                isLoading = false,
                                isSavedSuccess = true,
                                dailyStepGoal = res.data.dailyStepGoal,
                                estimatedDistanceKm = res.data.estimatedDistanceKm,
                                estimatedCaloriesKcal = res.data.estimatedCaloriesKcal,
                                errorMessage = null
                            )
                        }
                        // Refresh summary after updating goal
                        loadSummary(email)
                    }
                    is Resource.Error -> _stepGoalState.update { it.copy(isLoading = false, errorMessage = res.message, isSavedSuccess = false) }
                }
            }
        }
    }

    fun loadSessions(email: String, activityType: String? = null) {
        viewModelScope.launch {
            repository.getSessions(email, activityType).collect { res ->
                when (res) {
                    is Resource.Loading -> _sessionsState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _sessionsState.update { it.copy(isLoading = false, sessions = res.data, errorMessage = null) }
                    is Resource.Error -> _sessionsState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
            }
        }
    }

    fun recordSession(
        email: String,
        activityType: String,
        durationSeconds: Int,
        distanceKm: Float,
        caloriesBurned: Int,
        avgPace: String,
        avgHeartRate: Int? = null,
        steps: Int = 0
    ) {
        viewModelScope.launch {
            val req = RecordSessionRequest(
                email = email,
                activityType = activityType,
                durationSeconds = durationSeconds,
                distanceKm = distanceKm,
                caloriesBurned = caloriesBurned,
                avgPace = avgPace,
                avgHeartRate = avgHeartRate,
                steps = steps
            )
            repository.recordSession(req).collect { res ->
                when (res) {
                    is Resource.Loading -> _saveSessionState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }
                    is Resource.Success -> {
                        _saveSessionState.update { it.copy(isLoading = false, isSuccess = true, message = res.data, errorMessage = null) }
                        // Refresh summary and history
                        loadSummary(email)
                        loadSessions(email)
                    }
                    is Resource.Error -> _saveSessionState.update { it.copy(isLoading = false, isSuccess = false, errorMessage = res.message) }
                }
            }
        }
    }

    fun loadHistory(email: String) {
        viewModelScope.launch {
            repository.getHistory(email).collect { res ->
                when (res) {
                    is Resource.Loading -> _historyState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _historyState.update { it.copy(isLoading = false, history = res.data, errorMessage = null) }
                    is Resource.Error -> _historyState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
            }
        }
    }

    fun resetSaveSessionState() {
        _saveSessionState.value = SaveSessionUiState()
    }

    fun resetStepGoalSavedState() {
        _stepGoalState.update { it.copy(isSavedSuccess = false) }
    }
}

