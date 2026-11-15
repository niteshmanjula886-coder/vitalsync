package com.simats.vitalsync.data.network

import com.simats.vitalsync.data.model.gym.CreateExerciseRequest
import com.simats.vitalsync.data.model.gym.CreateExerciseResponse
import com.simats.vitalsync.data.model.gym.DeleteNutritionRequest
import com.simats.vitalsync.data.model.gym.DeleteNutritionResponse
import com.simats.vitalsync.data.model.gym.DeleteWorkoutRequest
import com.simats.vitalsync.data.model.gym.DeleteWorkoutResponse
import com.simats.vitalsync.data.model.gym.ExercisesResponse
import com.simats.vitalsync.data.model.gym.GymAnalyticsResponse
import com.simats.vitalsync.data.model.gym.GymNutritionResponse
import com.simats.vitalsync.data.model.gym.GymProgressResponse
import com.simats.vitalsync.data.model.gym.LogNutritionRequest
import com.simats.vitalsync.data.model.gym.LogNutritionResponse
import com.simats.vitalsync.data.model.gym.SaveGymWorkoutRequest
import com.simats.vitalsync.data.model.gym.SaveGymWorkoutResponse
import com.simats.vitalsync.data.model.gym.SaveWorkoutSessionRequest
import com.simats.vitalsync.data.model.gym.SaveWorkoutSessionResponse
import com.simats.vitalsync.data.model.gym.WorkoutSessionsResponse
import com.simats.vitalsync.data.model.gym.WorkoutSplitsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GymApiService {

    @GET("gym/splits.php")
    suspend fun getSplits(): Response<WorkoutSplitsResponse>

    @GET("gym/exercises.php")
    suspend fun getExercises(
        @Query("email") email: String
    ): Response<ExercisesResponse>

    @POST("gym/exercises.php")
    suspend fun addCustomExercise(
        @Body request: CreateExerciseRequest
    ): Response<CreateExerciseResponse>

    @GET("gym/sessions.php")
    suspend fun getWorkoutSessions(
        @Query("email") email: String
    ): Response<WorkoutSessionsResponse>

    @POST("gym/sessions.php")
    suspend fun saveWorkoutSession(
        @Body request: SaveWorkoutSessionRequest
    ): Response<SaveWorkoutSessionResponse>

    @GET("gym/progress.php")
    suspend fun getGymProgress(
        @Query("email") email: String
    ): Response<GymProgressResponse>

    // Dedicated Modern Gym Tracking & Nutrition Endpoints
    @POST("gym/save_gym_workout.php")
    suspend fun saveGymWorkout(
        @Body request: SaveGymWorkoutRequest
    ): Response<SaveGymWorkoutResponse>

    @GET("gym/get_gym_workouts.php")
    suspend fun getGymWorkouts(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null,
        @Query("date") date: String? = null
    ): Response<WorkoutSessionsResponse>

    @POST("gym/delete_gym_workout.php")
    suspend fun deleteGymWorkout(
        @Body request: DeleteWorkoutRequest
    ): Response<DeleteWorkoutResponse>

    @POST("gym/analyze_and_log_nutrition.php")
    suspend fun analyzeAndLogNutrition(
        @Body request: LogNutritionRequest
    ): Response<LogNutritionResponse>

    @GET("gym/get_gym_nutrition.php")
    suspend fun getGymNutrition(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null,
        @Query("date") date: String? = null
    ): Response<GymNutritionResponse>

    @POST("gym/delete_gym_nutrition.php")
    suspend fun deleteGymNutrition(
        @Body request: DeleteNutritionRequest
    ): Response<DeleteNutritionResponse>

    @GET("gym/get_gym_analytics.php")
    suspend fun getGymAnalytics(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null,
        @Query("date") date: String? = null
    ): Response<GymAnalyticsResponse>
}
