package com.simats.vitalsync.data.network

import com.simats.vitalsync.data.model.hydration.HydrationResponse
import com.simats.vitalsync.data.model.hydration.LogWaterRequest
import com.simats.vitalsync.data.model.hydration.LogWaterResponse
import com.simats.vitalsync.data.model.hydration.ResetHydrationResponse
import com.simats.vitalsync.data.model.hydration.UpdateHydrationSettingsRequest
import com.simats.vitalsync.data.model.hydration.UpdateHydrationSettingsResponse
import com.simats.vitalsync.data.model.hydration.WeeklyHydrationHistoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface HydrationApiService {

    @GET("hydration/hydration.php")
    suspend fun getHydration(
        @Query("email") email: String,
        @Query("date") date: String
    ): Response<HydrationResponse>

    @POST("hydration/hydration.php")
    suspend fun logWater(
        @Body request: LogWaterRequest
    ): Response<LogWaterResponse>

    @POST("hydration/hydration.php")
    suspend fun updateSettings(
        @Body request: UpdateHydrationSettingsRequest
    ): Response<UpdateHydrationSettingsResponse>

    @DELETE("hydration/hydration.php")
    suspend fun resetHydration(
        @Query("email") email: String,
        @Query("date") date: String
    ): Response<ResetHydrationResponse>

    @DELETE("hydration/hydration.php")
    suspend fun deleteLog(
        @Query("email") email: String,
        @Query("id") logId: Int
    ): Response<ResetHydrationResponse>

    @GET("api/hydration/get_weekly_history.php")
    suspend fun getWeeklyHistory(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null
    ): Response<WeeklyHydrationHistoryResponse>
}

