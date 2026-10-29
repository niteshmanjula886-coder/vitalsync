package com.simats.vitalsync.ui.activity.viewmodel

import com.simats.vitalsync.data.model.activity.ActivityHistoryItem
import com.simats.vitalsync.data.model.activity.ActivitySession
import com.simats.vitalsync.data.model.activity.ActivitySummary

data class ActivitySummaryUiState(
    val isLoading: Boolean = false,
    val summary: ActivitySummary? = null,
    val errorMessage: String? = null
)

data class StepGoalUiState(
    val isLoading: Boolean = false,
    val dailyStepGoal: Int = 8000,
    val estimatedDistanceKm: Float = 6.0f,
    val estimatedCaloriesKcal: Int = 320,
    val isSavedSuccess: Boolean = false,
    val errorMessage: String? = null
)

data class ActivitySessionsUiState(
    val isLoading: Boolean = false,
    val sessions: List<ActivitySession> = emptyList(),
    val errorMessage: String? = null
)

data class SaveSessionUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)

data class HistoryUiState(
    val isLoading: Boolean = false,
    val history: List<ActivityHistoryItem> = emptyList(),
    val errorMessage: String? = null
)

