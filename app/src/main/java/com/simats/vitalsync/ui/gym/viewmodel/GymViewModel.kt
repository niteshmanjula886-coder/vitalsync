package com.simats.vitalsync.ui.gym.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.vitalsync.data.Exercise
import com.simats.vitalsync.data.WorkoutSession
import com.simats.vitalsync.data.model.gym.CreateExerciseRequest
import com.simats.vitalsync.data.model.gym.toSaveRequest
import com.simats.vitalsync.data.repository.GymRepository
import com.simats.vitalsync.data.repository.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GymViewModel(
    private val repository: GymRepository
) : ViewModel() {

    private val _splitsState = MutableStateFlow(SplitsUiState())
    val splitsState: StateFlow<SplitsUiState> = _splitsState.asStateFlow()

    private val _exercisesState = MutableStateFlow(ExercisesUiState())
    val exercisesState: StateFlow<ExercisesUiState> = _exercisesState.asStateFlow()

    private val _addExerciseState = MutableStateFlow(AddExerciseUiState())
    val addExerciseState: StateFlow<AddExerciseUiState> = _addExerciseState.asStateFlow()

    private val _sessionsState = MutableStateFlow(WorkoutSessionsUiState())
    val sessionsState: StateFlow<WorkoutSessionsUiState> = _sessionsState.asStateFlow()

    private val _saveSessionState = MutableStateFlow(SaveWorkoutSessionUiState())
    val saveSessionState: StateFlow<SaveWorkoutSessionUiState> = _saveSessionState.asStateFlow()

    private val _progressState = MutableStateFlow(GymProgressUiState())
    val progressState: StateFlow<GymProgressUiState> = _progressState.asStateFlow()

    fun loadSplits() {
        viewModelScope.launch {
            repository.getSplits().collect { res ->
                when (res) {
                    is Resource.Loading -> _splitsState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _splitsState.update { it.copy(isLoading = false, splits = res.data, errorMessage = null) }
                    is Resource.Error -> _splitsState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
            }
        }
    }

    fun loadExercises(email: String) {
        viewModelScope.launch {
            repository.getExercises(email).collect { res ->
                when (res) {
                    is Resource.Loading -> _exercisesState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _exercisesState.update { it.copy(isLoading = false, exercises = res.data, errorMessage = null) }
                    is Resource.Error -> _exercisesState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
            }
        }
    }

    fun addCustomExercise(
        email: String,
        name: String,
        targetMuscle: String,
        defaultWeightKg: Float,
        defaultReps: Int,
        notes: String = "",
        onSuccess: ((Exercise) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val req = CreateExerciseRequest(
                email = email,
                name = name,
                targetMuscle = targetMuscle,
                defaultWeightKg = defaultWeightKg,
                defaultReps = defaultReps,
                notes = notes
            )
            repository.addCustomExercise(req).collect { res ->
                when (res) {
                    is Resource.Loading -> _addExerciseState.update { it.copy(isLoading = true, isSuccess = false, errorMessage = null) }
                    is Resource.Success -> {
                        val created = res.data
                        _addExerciseState.update { it.copy(isLoading = false, isSuccess = true, createdExercise = created, errorMessage = null) }
                        loadExercises(email)
                        onSuccess?.invoke(created.toExercise())
                    }
                    is Resource.Error -> _addExerciseState.update { it.copy(isLoading = false, isSuccess = false, errorMessage = res.message) }
                }
            }
        }
    }

    fun loadWorkoutSessions(email: String) {
        viewModelScope.launch {
            repository.getWorkoutSessions(email).collect { res ->
                when (res) {
                    is Resource.Loading -> _sessionsState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _sessionsState.update { it.copy(isLoading = false, sessions = res.data, errorMessage = null) }
                    is Resource.Error -> _sessionsState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
            }
        }
    }

    fun saveWorkoutSession(
        email: String,
        session: WorkoutSession,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val req = session.toSaveRequest(email)
            repository.saveWorkoutSession(req).collect { res ->
                when (res) {
                    is Resource.Loading -> _saveSessionState.update { it.copy(isLoading = true, isSuccess = false, errorMessage = null) }
                    is Resource.Success -> {
                        _saveSessionState.update { it.copy(isLoading = false, isSuccess = true, sessionId = res.data.sessionId, errorMessage = null) }
                        loadWorkoutSessions(email)
                        loadGymProgress(email)
                        onSuccess?.invoke()
                    }
                    is Resource.Error -> _saveSessionState.update { it.copy(isLoading = false, isSuccess = false, errorMessage = res.message) }
                }
            }
        }
    }

    fun loadGymProgress(email: String) {
        viewModelScope.launch {
            repository.getGymProgress(email).collect { res ->
                when (res) {
                    is Resource.Loading -> _progressState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _progressState.update { it.copy(isLoading = false, progress = res.data, errorMessage = null) }
                    is Resource.Error -> _progressState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
            }
        }
    }

    fun resetAddExerciseState() {
        _addExerciseState.value = AddExerciseUiState()
    }

    fun resetSaveSessionState() {
        _saveSessionState.value = SaveWorkoutSessionUiState()
    }
}
