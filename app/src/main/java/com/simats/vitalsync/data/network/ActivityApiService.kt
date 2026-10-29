package com.simats.vitalsync.data.network

import com.simats.vitalsync.data.model.activity.ActivitySessionsResponse
import com.simats.vitalsync.data.model.activity.ActivitySummaryResponse
import com.simats.vitalsync.data.model.activity.HistoryResponse
import com.simats.vitalsync.data.model.activity.RecordSessionRequest
import com.simats.vitalsync.data.model.activity.StepGoalResponse
import com.simats.vitalsync.data.model.activity.UpdateStepGoalRequest
import com.simats.vitalsync.data.model.auth.BaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit interface for Activity Tracking, Step Goals, History & Summaries.
 */
interface ActivityApiService {

    @GET("activity/summary.php")
    suspend fun getActivitySummary(
        @Query("email") email: String
    ): Response<ActivitySummaryResponse>

    @GET("activity/step_goal.php")
    suspend fun getStepGoal(
        @Query("email") email: String
    ): Response<StepGoalResponse>

    @POST("activity/step_goal.php")
    suspend fun updateStepGoal(
        @Body request: UpdateStepGoalRequest
    ): Response<StepGoalResponse>

    @GET("activity/tracking.php")
    suspend fun getSessions(
        @Query("email") email: String,
        @Query("activity_type") activityType: String? = null,
        @Query("limit") limit: Int = 30
    ): Response<ActivitySessionsResponse>

    @POST("activity/tracking.php")
    suspend fun recordSession(
        @Body request: RecordSessionRequest
    ): Response<BaseResponse>

    @GET("activity/history.php")
    suspend fun getHistory(
        @Query("email") email: String
    ): Response<HistoryResponse>
}

