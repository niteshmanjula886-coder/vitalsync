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

enum class NutritionTrendMetric(val label: String, val unit: String, val icon: String) {
    CALORIES("Calories", "kcal", "🔥"),
    PROTEIN("Protein", "g", "🥩"),
    CARBS("Carbs", "g", "🍞"),
    FATS("Fats", "g", "🥑")
}

// =============================================================================
// Unified Modern 3-Tab Gym UI State
// =============================================================================

data class GymUiState(
    val selectedTab: Int = 0, // 0: Workouts, 1: Nutrition, 2: Analytics

    // 1. Workouts State
    val selectedSplit: String = "Push",
    val customSplits: List<String> = emptyList(),
    val workoutTimerSeconds: Long = 0L,
    val isWorkoutTimerRunning: Boolean = false,
    val restTimerSeconds: Int = 0,
    val initialRestTimerPreset: Int = 60,
    val isRestTimerRunning: Boolean = false,
    val totalVolumeKg: Float = 0f,
    val totalSetsCompleted: Int = 0,
    val estimatedCaloriesBurned: Int = 0,
    val isSavingWorkout: Boolean = false,
    val saveWorkoutSuccessMessage: String? = null,
    val workoutErrorMessage: String? = null,
    val todayWorkouts: List<WorkoutSessionDto> = emptyList(),
    val isWorkoutsLoading: Boolean = false,

    // 2. Nutrition State
    val selectedMealType: String = "Breakfast",
    val foodDescriptionInput: String = "",
    val manualCaloriesInput: String = "",
    val manualProteinInput: String = "",
    val manualCarbsInput: String = "",
    val manualFatsInput: String = "",
    val isAnalyzingNutrition: Boolean = false,
    val isNutritionLoading: Boolean = false,
    val nutritionSuccessMessage: String? = null,
    val nutritionErrorMessage: String? = null,
    val todayNutrition: com.simats.vitalsync.data.model.gym.GymNutritionResponse? = null,

    // Weekly Nutrition Meal Trend State
    val isWeeklyNutritionLoading: Boolean = false,
    val weeklyNutritionTrend: com.simats.vitalsync.data.model.gym.WeeklyNutritionTrendResponse? = null,
    val selectedWeeklyNutritionMetric: NutritionTrendMetric = NutritionTrendMetric.CALORIES,
    val selectedWeeklyTrendDate: String? = null,
    val weeklyNutritionErrorMessage: String? = null,

    // Dual-Mode Protein & Calorie Targets (Issue 5)
    val proteinTargetMode: String = "BODY_WEIGHT", // "BODY_WEIGHT" or "CUSTOM"
    val bodyWeightKg: Float = 70f,
    val bodyWeightMultiplier: Float = 1.8f, // 1.6f Build, 1.8f Optimal, 2.0f Cut
    val customProteinTargetG: Float = 140f,
    val customCaloriesTarget: Int = 2200,

    // 3. Analytics State (Issue 7 Day-to-Day)
    val selectedAnalyticsDate: String = "",
    val isAnalyticsLoading: Boolean = false,
    val analyticsData: com.simats.vitalsync.data.model.gym.GymAnalyticsDto? = null,
    val analyticsErrorMessage: String? = null,

    // General feedback
    val isLoading: Boolean = false,
    val globalMessage: String? = null
) {
    val activeProteinTargetG: Float
        get() = if (proteinTargetMode == "CUSTOM") customProteinTargetG
                else (bodyWeightKg * bodyWeightMultiplier)

    val activeCaloriesTarget: Int
        get() = if (proteinTargetMode == "CUSTOM") customCaloriesTarget
                else ((bodyWeightKg * 32f).toInt().coerceAtLeast(1600))
}
