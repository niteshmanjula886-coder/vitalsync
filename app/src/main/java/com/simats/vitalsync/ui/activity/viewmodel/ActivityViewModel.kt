package com.simats.vitalsync.ui.activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.vitalsync.data.model.activity.RecordSessionRequest
import com.simats.vitalsync.data.network.ApiConstants
import com.simats.vitalsync.data.repository.ActivityRepository
import com.simats.vitalsync.data.repository.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException

/**
 * ViewModel managing Activity Summaries, Step Goals, Activity Tracking Sessions & History.
 * Follows the Android MVVM architecture pattern.
 */
class ActivityViewModel(
    private val repository: ActivityRepository
) : ViewModel() {

    private val _summaryState = MutableStateFlow(ActivitySummaryUiState())
    val summaryState: StateFlow<ActivitySummaryUiState> = _summaryState.asStateFlow()

    private val initialCachedGoal = repository.getCachedStepGoal()
    private val _stepGoalState = MutableStateFlow(
        StepGoalUiState(
            dailyStepGoal = initialCachedGoal ?: 8000,
            isGoalLoaded = initialCachedGoal != null
        )
    )
    val stepGoalState: StateFlow<StepGoalUiState> = _stepGoalState.asStateFlow()

    private val _sessionsState = MutableStateFlow(ActivitySessionsUiState())
    val sessionsState: StateFlow<ActivitySessionsUiState> = _sessionsState.asStateFlow()

    private val _saveSessionState = MutableStateFlow(SaveSessionUiState())
    val saveSessionState: StateFlow<SaveSessionUiState> = _saveSessionState.asStateFlow()

    private val _historyState = MutableStateFlow(HistoryUiState())
    val historyState: StateFlow<HistoryUiState> = _historyState.asStateFlow()

    fun loadSummary(email: String) {
        viewModelScope.launch {
            try {
                repository.getActivitySummary(email).collect { res ->
                    when (res) {
                        is Resource.Loading -> _summaryState.update { it.copy(isLoading = true, errorMessage = null) }
                        is Resource.Success -> _summaryState.update { it.copy(isLoading = false, summary = res.data, errorMessage = null) }
                        is Resource.Error -> _summaryState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    }
                }
            } catch (_: SocketTimeoutException) {
                _summaryState.update { it.copy(isLoading = false, errorMessage = ApiConstants.SERVER_UNREACHABLE_MESSAGE) }
            } catch (_: ConnectException) {
                _summaryState.update { it.copy(isLoading = false, errorMessage = ApiConstants.SERVER_UNREACHABLE_MESSAGE) }
            } catch (e: Exception) {
                _summaryState.update { it.copy(isLoading = false, errorMessage = ApiConstants.resolveNetworkErrorMessage(e)) }
            } finally {
                _summaryState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearSummaryError() {
        _summaryState.update { it.copy(errorMessage = null) }
    }

    fun loadStepGoal(email: String) {
        viewModelScope.launch {
            try {
                repository.getStepGoal(email).collect { res ->
                    when (res) {
                        is Resource.Loading -> _stepGoalState.update { it.copy(isLoading = true, errorMessage = null) }
                        is Resource.Success -> _stepGoalState.update {
                            it.copy(
                                isLoading = false,
                                isGoalLoaded = true,
                                dailyStepGoal = res.data.dailyStepGoal,
                                estimatedDistanceKm = res.data.estimatedDistanceKm,
                                estimatedCaloriesKcal = res.data.estimatedCaloriesKcal,
                                errorMessage = null
                            )
                        }
                        is Resource.Error -> _stepGoalState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    }
                }
            } catch (e: Exception) {
                _stepGoalState.update { it.copy(isLoading = false, errorMessage = ApiConstants.resolveNetworkErrorMessage(e)) }
            } finally {
                _stepGoalState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateStepGoal(email: String, newGoal: Int) {
        repository.saveStepGoalLocally(newGoal)
        _stepGoalState.update {
            it.copy(
                dailyStepGoal = newGoal,
                isGoalLoaded = true
            )
        }
        viewModelScope.launch {
            try {
                repository.updateStepGoal(email, newGoal).collect { res ->
                    when (res) {
                        is Resource.Loading -> _stepGoalState.update { it.copy(isLoading = true, errorMessage = null, isSavedSuccess = false) }
                        is Resource.Success -> {
                            val savedGoal = res.data.dailyStepGoal.takeIf { it >= 1000 } ?: newGoal
                            repository.saveStepGoalLocally(savedGoal)
                            _stepGoalState.update {
                                it.copy(
                                    isLoading = false,
                                    isGoalLoaded = true,
                                    isSavedSuccess = true,
                                    dailyStepGoal = savedGoal,
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
            } catch (e: Exception) {
                _stepGoalState.update { it.copy(isLoading = false, errorMessage = ApiConstants.resolveNetworkErrorMessage(e)) }
            } finally {
                _stepGoalState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadSessions(email: String, activityType: String? = null) {
        viewModelScope.launch {
            try {
                repository.getSessions(email, activityType).collect { res ->
                    when (res) {
                        is Resource.Loading -> _sessionsState.update { it.copy(isLoading = true, errorMessage = null) }
                        is Resource.Success -> _sessionsState.update { it.copy(isLoading = false, sessions = res.data, errorMessage = null) }
                        is Resource.Error -> _sessionsState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    }
                }
            } catch (e: Exception) {
                _sessionsState.update { it.copy(isLoading = false, errorMessage = ApiConstants.resolveNetworkErrorMessage(e)) }
            } finally {
                _sessionsState.update { it.copy(isLoading = false) }
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
        steps: Int = 0,
        sessionUuid: String = java.util.UUID.randomUUID().toString(),
        targetType: String = "time",
        targetValue: Int = 45,
        trackingSource: String = "phone_sensor",
        startTime: String? = null,
        endTime: String? = null,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val req = RecordSessionRequest(
                email = email,
                sessionUuid = sessionUuid,
                activityType = activityType,
                durationSeconds = durationSeconds,
                targetType = targetType,
                targetValue = targetValue,
                distanceKm = distanceKm,
                caloriesBurned = caloriesBurned,
                avgPace = avgPace,
                avgHeartRate = avgHeartRate,
                steps = steps,
                trackingSource = trackingSource,
                startTime = startTime,
                endTime = endTime
            )
            try {
                repository.recordSession(req).collect { res ->
                    when (res) {
                        is Resource.Loading -> _saveSessionState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }
                        is Resource.Success -> {
                            _saveSessionState.update { it.copy(isLoading = false, isSuccess = true, message = res.data, errorMessage = null) }
                            // Refresh summary and history
                            loadSummary(email)
                            loadSessions(email)
                            onComplete?.invoke()
                        }
                        is Resource.Error -> {
                            _saveSessionState.update { it.copy(isLoading = false, isSuccess = false, errorMessage = res.message) }
                            onComplete?.invoke()
                        }
                    }
                }
            } catch (e: Exception) {
                _saveSessionState.update { it.copy(isLoading = false, isSuccess = false, errorMessage = ApiConstants.resolveNetworkErrorMessage(e)) }
                onComplete?.invoke()
            } finally {
                _saveSessionState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadHistory(email: String) {
        viewModelScope.launch {
            try {
                repository.getHistory(email).collect { res ->
                    when (res) {
                        is Resource.Loading -> _historyState.update { it.copy(isLoading = true, errorMessage = null) }
                        is Resource.Success -> _historyState.update { it.copy(isLoading = false, history = res.data, errorMessage = null) }
                        is Resource.Error -> _historyState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    }
                }
            } catch (e: Exception) {
                _historyState.update { it.copy(isLoading = false, errorMessage = ApiConstants.resolveNetworkErrorMessage(e)) }
            } finally {
                _historyState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun syncDailySteps(
        email: String,
        stepCount: Int,
        targetSteps: Int = 8000,
        distanceKm: Float = 0f,
        caloriesKcal: Int = 0,
        activeMinutes: Int = 0
    ) {
        viewModelScope.launch {
            repository.syncDailySteps(
                email = email,
                stepCount = stepCount,
                targetSteps = targetSteps,
                distanceKm = distanceKm,
                caloriesKcal = caloriesKcal,
                activeMinutes = activeMinutes
            ).collect { res ->
                if (res is Resource.Success) {
                    loadSummary(email)
                }
            }
        }
    }

    fun clearActivityHistory(email: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearActivityHistory(email).collect { res ->
                if (res is Resource.Success) {
                    _sessionsState.update { it.copy(sessions = emptyList()) }
                    loadSummary(email)
                    onComplete()
                }
            }
        }
    }

    fun deleteActivitySession(email: String, sessionUuid: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _sessionsState.update { current ->
                current.copy(sessions = current.sessions.filterNot { it.sessionUuid == sessionUuid })
            }
            if (email.isNotBlank() && sessionUuid.isNotBlank()) {
                repository.deleteActivitySession(email, sessionUuid).collect { res ->
                    if (res is Resource.Success) {
                        loadSummary(email)
                        onComplete()
                    }
                }
            } else {
                onComplete()
            }
        }
    }

    fun resetSaveSessionState() {
        _saveSessionState.value = SaveSessionUiState()
    }

    fun resetStepGoalSavedState() {
        _stepGoalState.update { it.copy(isSavedSuccess = false) }
    }

    fun resetState() {
        _summaryState.value = ActivitySummaryUiState()
        _stepGoalState.value = StepGoalUiState(dailyStepGoal = 8000, isGoalLoaded = false)
        _sessionsState.value = ActivitySessionsUiState()
        _saveSessionState.value = SaveSessionUiState()
        _historyState.value = HistoryUiState()
    }
}


