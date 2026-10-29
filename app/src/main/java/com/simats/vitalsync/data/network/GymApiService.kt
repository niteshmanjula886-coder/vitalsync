package com.simats.vitalsync.data.network

import com.simats.vitalsync.data.model.gym.CreateExerciseRequest
import com.simats.vitalsync.data.model.gym.CreateExerciseResponse
import com.simats.vitalsync.data.model.gym.ExercisesResponse
import com.simats.vitalsync.data.model.gym.GymProgressResponse
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
}
