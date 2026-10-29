package com.simats.vitalsync.ui.gym.viewmodel

import com.simats.vitalsync.data.model.gym.ExerciseDto
import com.simats.vitalsync.data.model.gym.GymProgressDto
import com.simats.vitalsync.data.model.gym.WorkoutSessionDto
import com.simats.vitalsync.data.model.gym.WorkoutSplitDto

data class SplitsUiState(
    val isLoading: Boolean = false,
    val splits: List<WorkoutSplitDto> = emptyList(),
    val errorMessage: String? = null
)

data class ExercisesUiState(
    val isLoading: Boolean = false,
    val exercises: List<ExerciseDto> = emptyList(),
    val errorMessage: String? = null
)

data class AddExerciseUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val createdExercise: ExerciseDto? = null,
    val errorMessage: String? = null
)

data class WorkoutSessionsUiState(
    val isLoading: Boolean = false,
    val sessions: List<WorkoutSessionDto> = emptyList(),
    val errorMessage: String? = null
)

data class SaveWorkoutSessionUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val sessionId: Int? = null,
    val errorMessage: String? = null
)

data class GymProgressUiState(
    val isLoading: Boolean = false,
    val progress: GymProgressDto? = null,
    val errorMessage: String? = null
)
