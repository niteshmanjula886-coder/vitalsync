package com.simats.vitalsync.data.model.gym

import com.google.gson.annotations.SerializedName
import com.simats.vitalsync.data.Exercise
import com.simats.vitalsync.data.WorkoutExercise
import com.simats.vitalsync.data.WorkoutSession
import com.simats.vitalsync.data.WorkoutSet
import com.simats.vitalsync.data.WorkoutSplit

/**
 * Data models for the Gym module (Splits, Exercises, Sessions, Progress).
 */

data class WorkoutSplitsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("splits") val splits: List<WorkoutSplitDto> = emptyList()
)

data class WorkoutSplitDto(
    @SerializedName("split") val split: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("description") val description: String
)

data class ExercisesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("count") val count: Int = 0,
    @SerializedName("exercises") val exercises: List<ExerciseDto> = emptyList()
)

data class ExerciseDto(
    @SerializedName("id") val id: String,
    @SerializedName("db_id") val dbId: Int? = null,
    @SerializedName("name") val name: String,
    @SerializedName("targetMuscle") val targetMuscle: String,
    @SerializedName("defaultWeightKg") val defaultWeightKg: Float = 20f,
    @SerializedName("defaultReps") val defaultReps: Int = 10,
    @SerializedName("notes") val notes: String = "",
    @SerializedName("isCustom") val isCustom: Boolean = false
) {
    fun toExercise(): Exercise = Exercise(
        id = id,
        name = name,
        targetMuscle = targetMuscle,
        defaultWeightKg = defaultWeightKg,
        defaultReps = defaultReps,
        notes = notes
    )
}

data class CreateExerciseRequest(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("target_muscle") val targetMuscle: String,
    @SerializedName("default_weight_kg") val defaultWeightKg: Float,
    @SerializedName("default_reps") val defaultReps: Int,
    @SerializedName("notes") val notes: String = ""
)

data class CreateExerciseResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("exercise") val exercise: ExerciseDto? = null
)

data class WorkoutSessionsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("count") val count: Int = 0,
    @SerializedName("workout_sessions") val workoutSessions: List<WorkoutSessionDto> = emptyList()
)

data class WorkoutSessionDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("split") val split: String = "",
    @SerializedName("dateString") val dateString: String = "",
    @SerializedName("timestampFormatted") val timestampFormatted: String = "",
    @SerializedName("durationMinutes") val durationMinutes: Int = 0,
    @SerializedName("totalVolumeKg") val totalVolumeKg: Float = 0f,
    @SerializedName("estimatedCaloriesBurned") val estimatedCaloriesBurned: Int = 0,
    @SerializedName("exercises") val exercises: List<WorkoutExerciseDto> = emptyList(),
    @SerializedName("total_sets", alternate = ["totalSets"]) val totalSetsVal: Int = 0
) {
    val totalSets: Int
        get() = if (totalSetsVal > 0) totalSetsVal else exercises.sumOf { it.sets.size }

    fun toWorkoutSession(): WorkoutSession {
        val mappedSplit = try {
            WorkoutSplit.valueOf(split.uppercase())
        } catch (_: Exception) {
            WorkoutSplit.CUSTOM
        }

        return WorkoutSession(
            id = id,
            name = name,
            split = mappedSplit,
            dateString = dateString,
            timestampFormatted = timestampFormatted,
            durationMinutes = durationMinutes,
            exercises = exercises.map { it.toWorkoutExercise() },
            totalVolumeKg = totalVolumeKg,
            estimatedCaloriesBurned = estimatedCaloriesBurned
        )
    }
}

data class WorkoutExerciseDto(
    @SerializedName("exercise") val exercise: ExerciseDto,
    @SerializedName("sets") val sets: List<WorkoutSetDto> = emptyList(),
    @SerializedName("notes") val notes: String = ""
) {
    fun toWorkoutExercise(): WorkoutExercise = WorkoutExercise(
        exercise = exercise.toExercise(),
        sets = sets.map { it.toWorkoutSet() },
        notes = notes
    )
}

data class WorkoutSetDto(
    @SerializedName("setNumber") val setNumber: Int,
    @SerializedName("reps") val reps: Int,
    @SerializedName("weightKg") val weightKg: Float,
    @SerializedName("isCompleted") val isCompleted: Boolean = false
) {
    fun toWorkoutSet(): WorkoutSet = WorkoutSet(
        setNumber = setNumber,
        reps = reps,
        weightKg = weightKg,
        isCompleted = isCompleted
    )
}

data class SaveWorkoutSessionRequest(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("split") val split: String,
    @SerializedName("dateString") val dateString: String,
    @SerializedName("timestampFormatted") val timestampFormatted: String,
    @SerializedName("durationMinutes") val durationMinutes: Int,
    @SerializedName("totalVolumeKg") val totalVolumeKg: Float,
    @SerializedName("estimatedCaloriesBurned") val estimatedCaloriesBurned: Int,
    @SerializedName("exercises") val exercises: List<WorkoutExerciseDto>
)

data class SaveWorkoutSessionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("session_id") val sessionId: Int? = null
)

data class GymProgressResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("progress") val progress: GymProgressDto
)

data class GymProgressDto(
    @SerializedName("total_workouts") val totalWorkouts: Int = 0,
    @SerializedName("weekly_workouts") val weeklyWorkouts: Int = 0,
    @SerializedName("weekly_target_workouts") val weeklyTargetWorkouts: Int = 4,
    @SerializedName("lifetime_volume_kg") val lifetimeVolumeKg: Float = 0f,
    @SerializedName("weekly_volume_kg") val weeklyVolumeKg: Float = 0f,
    @SerializedName("total_workout_minutes") val totalWorkoutMinutes: Int = 0,
    @SerializedName("total_calories_burned") val totalCaloriesBurned: Int = 0,
    @SerializedName("favorite_split") val favoriteSplit: String = "PUSH",
    @SerializedName("current_streak_days") val currentStreakDays: Int = 1
)

fun WorkoutSession.toSaveRequest(email: String): SaveWorkoutSessionRequest {
    return SaveWorkoutSessionRequest(
        email = email,
        name = name,
        split = split.name,
        dateString = dateString,
        timestampFormatted = timestampFormatted,
        durationMinutes = durationMinutes,
        totalVolumeKg = totalVolumeKg,
        estimatedCaloriesBurned = estimatedCaloriesBurned,
        exercises = exercises.map { we ->
            WorkoutExerciseDto(
                exercise = ExerciseDto(
                    id = we.exercise.id,
                    name = we.exercise.name,
                    targetMuscle = we.exercise.targetMuscle,
                    defaultWeightKg = we.exercise.defaultWeightKg,
                    defaultReps = we.exercise.defaultReps,
                    notes = we.exercise.notes
                ),
                sets = we.sets.map { st ->
                    WorkoutSetDto(
                        setNumber = st.setNumber,
                        reps = st.reps,
                        weightKg = st.weightKg,
                        isCompleted = st.isCompleted
                    )
                },
                notes = we.notes
            )
        }
    )
}

// =============================================================================
// Dedicated Redesigned Gym Tracking & Nutrition DTOs
// =============================================================================

data class SaveGymWorkoutRequest(
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("split_name") val splitName: String,
    @SerializedName("duration_minutes") val durationMinutes: Int,
    @SerializedName("total_volume_kg") val totalVolumeKg: Float,
    @SerializedName("total_sets") val totalSets: Int,
    @SerializedName("calories_burned") val caloriesBurned: Int,
    @SerializedName("exercises") val exercises: List<WorkoutExerciseDto> = emptyList()
)

data class SaveGymWorkoutResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("workout_id") val workoutId: Int? = null
)

data class GymNutritionLogDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("meal_type") val mealType: String = "",
    @SerializedName("food_description") val foodDescription: String = "",
    @SerializedName("calories") val calories: Int = 0,
    @SerializedName("protein_g") val proteinG: Float = 0f,
    @SerializedName("carbs_g") val carbsG: Float = 0f,
    @SerializedName("fats_g") val fatsG: Float = 0f,
    @SerializedName("ai_feedback") val aiFeedback: String = "",
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("time_formatted") val timeFormatted: String = ""
)

data class GymNutritionResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("date") val date: String = "",
    @SerializedName("total_calories") val totalCalories: Int = 0,
    @SerializedName("total_protein") val totalProtein: Float = 0f,
    @SerializedName("total_carbs") val totalCarbs: Float = 0f,
    @SerializedName("total_fats") val totalFats: Float = 0f,
    @SerializedName("target_calories") val targetCalories: Int = 2200,
    @SerializedName("target_protein") val targetProtein: Float = 140f,
    @SerializedName("target_carbs") val targetCarbs: Float = 240f,
    @SerializedName("target_fats") val targetFats: Float = 65f,
    @SerializedName("ai_coach_tip") val aiCoachTip: String = "",
    @SerializedName("meals") val meals: List<GymNutritionLogDto> = emptyList()
)

data class LogNutritionRequest(
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("meal_type") val mealType: String,
    @SerializedName("food_description") val foodDescription: String,
    @SerializedName("calories") val calories: Int? = null,
    @SerializedName("protein_g") val proteinG: Float? = null,
    @SerializedName("carbs_g") val carbsG: Float? = null,
    @SerializedName("fats_g") val fatsG: Float? = null,
    @SerializedName("save_manually") val saveManually: Boolean = false
)

data class LogNutritionResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: GymNutritionLogDto? = null
)

data class DeleteNutritionRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("email") val email: String? = null
)

data class DeleteNutritionResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = ""
)

data class DeleteWorkoutRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("email") val email: String? = null
)

data class DeleteWorkoutResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = ""
)

data class DayComparisonDto(
    @SerializedName("date") val date: String = "",
    @SerializedName("day_label") val dayLabel: String = "",
    @SerializedName("date_formatted") val dateFormatted: String = "",
    @SerializedName("workouts_count") val workoutsCount: Int = 0,
    @SerializedName("splits_trained") val splitsTrained: String = "Rest",
    @SerializedName("volume_kg") val volumeKg: Float = 0f,
    @SerializedName("calories_burned") val caloriesBurned: Int = 0,
    @SerializedName("calories_in") val caloriesIn: Int = 0,
    @SerializedName("protein_g") val proteinG: Float = 0f,
    @SerializedName("net_calories") val netCalories: Int = 0,
    @SerializedName("is_selected") val isSelected: Boolean = false,
    @SerializedName("is_today") val isToday: Boolean = false
)

data class DailySplitBreakdownDto(
    @SerializedName("split_name") val splitName: String = "",
    @SerializedName("count") val count: Int = 0,
    @SerializedName("volume_kg") val volumeKg: Float = 0f,
    @SerializedName("total_sets") val totalSets: Int = 0,
    @SerializedName("calories_burned") val caloriesBurned: Int = 0
)

data class SplitDistributionItemDto(
    @SerializedName("split_name") val splitName: String = "",
    @SerializedName("count") val count: Int = 0,
    @SerializedName("volume_kg") val volumeKg: Float = 0f,
    @SerializedName("percentage") val percentage: Int = 0
)

data class GymDailyTrendDto(
    @SerializedName("date") val date: String = "",
    @SerializedName("day_label") val dayLabel: String = "",
    @SerializedName("volume_kg") val volumeKg: Float = 0f,
    @SerializedName("calories_burned") val caloriesBurned: Int = 0,
    @SerializedName("calories_in") val caloriesIn: Int = 0,
    @SerializedName("protein_g") val proteinG: Float = 0f
)

data class GymAnalyticsDto(
    // Day-to-Day Analytics Fields (Issue 7)
    @SerializedName("selected_date") val selectedDate: String = "",
    @SerializedName("selected_date_formatted") val selectedDateFormatted: String = "",
    @SerializedName("is_today") val isToday: Boolean = true,
    @SerializedName("daily_workouts_count") val dailyWorkoutsCount: Int = 0,
    @SerializedName("daily_splits_trained") val dailySplitsTrained: String = "Rest Day",
    @SerializedName("daily_volume_kg") val dailyVolumeKg: Float = 0f,
    @SerializedName("daily_sets") val dailySets: Int = 0,
    @SerializedName("daily_duration_mins") val dailyDurationMins: Int = 0,
    @SerializedName("daily_calories_burned") val dailyCaloriesBurned: Int = 0,
    @SerializedName("daily_calories_in") val dailyCaloriesIn: Int = 0,
    @SerializedName("daily_protein_g") val dailyProteinG: Float = 0f,
    @SerializedName("daily_carbs_g") val dailyCarbsG: Float = 0f,
    @SerializedName("daily_fats_g") val dailyFatsG: Float = 0f,
    @SerializedName("daily_net_energy") val dailyNetEnergy: Int = 0,
    @SerializedName("current_streak_days") val currentStreakDays: Int = 0,
    @SerializedName("daily_workouts_list") val dailyWorkoutsList: List<WorkoutSessionDto> = emptyList(),
    @SerializedName("daily_split_breakdown") val dailySplitBreakdown: List<DailySplitBreakdownDto> = emptyList(),
    @SerializedName("last_7_days_comparison") val last7DaysComparison: List<DayComparisonDto> = emptyList(),

    // Backward compatibility fields
    @SerializedName("weekly_workouts_completed") val weeklyWorkoutsCompleted: Int = 0,
    @SerializedName("total_weekly_volume_kg") val totalWeeklyVolumeKg: Float = 0f,
    @SerializedName("avg_session_duration_mins") val avgSessionDurationMins: Int = 0,
    @SerializedName("total_weekly_calories_burned") val totalWeeklyCaloriesBurned: Int = 0,
    @SerializedName("avg_daily_calories") val avgDailyCalories: Int = 0,
    @SerializedName("avg_daily_protein") val avgDailyProtein: Float = 0f,
    @SerializedName("net_energy_balance") val netEnergyBalance: Int = 0,
    @SerializedName("split_distribution") val splitDistribution: List<SplitDistributionItemDto> = emptyList(),
    @SerializedName("daily_trends") val dailyTrends: List<GymDailyTrendDto> = emptyList(),
    @SerializedName("recent_workouts") val recentWorkouts: List<WorkoutSessionDto> = emptyList()
)

data class GymAnalyticsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: GymAnalyticsDto? = null,
    @SerializedName("message") val message: String? = null
)

// =============================================================================
// Curated Pre-Loaded Split Exercise Library
// =============================================================================

val curatedPushExercises = listOf(
    Exercise("push_1", "Barbell Bench Press", "Chest", 60f, 10),
    Exercise("push_2", "Overhead Barbell Press", "Front Delts", 40f, 10),
    Exercise("push_3", "Incline Dumbbell Press", "Upper Chest", 26f, 10),
    Exercise("push_4", "Cable Chest Fly", "Inner Chest", 15f, 15),
    Exercise("push_5", "Cable Triceps Pushdown", "Triceps Lateral Head", 25f, 12),
    Exercise("push_6", "Overhead Triceps Extension", "Triceps Long Head", 20f, 12),
    Exercise("push_7", "Dumbbell Lateral Raise", "Side Delts", 10f, 15)
)

val curatedPullExercises = listOf(
    Exercise("pull_1", "Wide-Grip Lat Pulldown", "Lats", 55f, 12),
    Exercise("pull_2", "Barbell Bent-Over Row", "Middle Back", 60f, 10),
    Exercise("pull_3", "Seated Cable Row", "Mid-Back", 50f, 12),
    Exercise("pull_4", "Face Pull", "Rear Delts & Rotators", 17.5f, 15),
    Exercise("pull_5", "Barbell Biceps Curl", "Biceps Brachii", 30f, 10),
    Exercise("pull_6", "Dumbbell Hammer Curl", "Brachialis & Forearms", 14f, 12),
    Exercise("pull_7", "Incline Dumbbell Curl", "Biceps Long Head", 12f, 12)
)

val curatedLegsExercises = listOf(
    Exercise("leg_1", "Barbell Back Squat", "Quads & Glutes", 80f, 8),
    Exercise("leg_2", "Romanian Deadlift", "Hamstrings & Glutes", 70f, 10),
    Exercise("leg_3", "Leg Press 45°", "Quads & Glutes", 140f, 10),
    Exercise("leg_4", "Leg Extension", "Quads Isolation", 45f, 15),
    Exercise("leg_5", "Lying Leg Curl", "Hamstrings Isolation", 35f, 12),
    Exercise("leg_6", "Standing Calf Raise", "Calves", 55f, 15),
    Exercise("leg_7", "Dumbbell Walking Lunges", "Glutes & Quads", 18f, 12)
)

val curatedArmsExercises = listOf(
    Exercise("arm_1", "Barbell Biceps Curl", "Biceps", 30f, 10),
    Exercise("arm_2", "Preacher Curl Machine", "Biceps Short Head", 25f, 12),
    Exercise("arm_3", "Incline Dumbbell Curl", "Biceps Peak", 14f, 10),
    Exercise("arm_4", "Cable Triceps Pushdown", "Triceps", 27.5f, 12),
    Exercise("arm_5", "EZ-Bar Skull Crushers", "Triceps Long Head", 25f, 10),
    Exercise("arm_6", "Bodyweight / Weighted Dips", "Triceps & Lower Chest", 0f, 12),
    Exercise("arm_7", "Forearm Wrist Curls", "Forearms", 15f, 20)
)

val curatedCardioExercises = listOf(
    Exercise("cardio_1", "Treadmill Steady Run", "Cardio / Aerobic", 0f, 25),
    Exercise("cardio_2", "Stationary Bike Intervals", "Leg Stamina & Heart", 0f, 20),
    Exercise("cardio_3", "HIIT Sprint Intervals", "Anaerobic Power", 0f, 15),
    Exercise("cardio_4", "Concept 2 Rowing Machine", "Full Body Cardio", 0f, 15),
    Exercise("cardio_5", "Stairmaster Climber", "Glutes & Calves", 0f, 15)
)

val curatedAbsExercises = listOf(
    Exercise("abs_1", "Hanging Leg Raises", "Lower Abs", 0f, 15),
    Exercise("abs_2", "Cable Kneeling Crunches", "Upper Abs", 35f, 15),
    Exercise("abs_3", "Plank Hold", "Core Stability", 0f, 60),
    Exercise("abs_4", "Weighted Russian Twists", "Obliques", 10f, 20),
    Exercise("abs_5", "Ab Wheel Rollouts", "Deep Core", 0f, 12)
)

fun getCuratedExercises(split: String): List<Exercise> {
    return when (split.lowercase()) {
        "push" -> curatedPushExercises
        "pull" -> curatedPullExercises
        "legs" -> curatedLegsExercises
        "arms" -> curatedArmsExercises
        "cardio" -> curatedCardioExercises
        "abs" -> curatedAbsExercises
        else -> curatedPushExercises
    }
}

fun createDefaultWorkoutExercises(split: String): List<WorkoutExercise> {
    return getCuratedExercises(split).map { ex ->
        WorkoutExercise(
            exercise = ex,
            sets = listOf(
                WorkoutSet(1, ex.defaultReps, ex.defaultWeightKg, false),
                WorkoutSet(2, ex.defaultReps, ex.defaultWeightKg, false),
                WorkoutSet(3, ex.defaultReps, ex.defaultWeightKg, false)
            )
        )
    }
}

