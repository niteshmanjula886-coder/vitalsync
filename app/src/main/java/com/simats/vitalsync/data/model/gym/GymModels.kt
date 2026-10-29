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
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("split") val split: String,
    @SerializedName("dateString") val dateString: String,
    @SerializedName("timestampFormatted") val timestampFormatted: String,
    @SerializedName("durationMinutes") val durationMinutes: Int,
    @SerializedName("totalVolumeKg") val totalVolumeKg: Float,
    @SerializedName("estimatedCaloriesBurned") val estimatedCaloriesBurned: Int,
    @SerializedName("exercises") val exercises: List<WorkoutExerciseDto> = emptyList()
) {
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
