package com.simats.vitalsync.data.network

import com.simats.vitalsync.data.model.ai.AiBaseResponse
import com.simats.vitalsync.data.model.ai.AiChatListResponse
import com.simats.vitalsync.data.model.ai.AiChatRequest
import com.simats.vitalsync.data.model.ai.AiChatResponse
import com.simats.vitalsync.data.model.ai.AiCreateSessionRequest
import com.simats.vitalsync.data.model.ai.AiCreateSessionResponse
import com.simats.vitalsync.data.model.ai.AiDailyInsightsResponse
import com.simats.vitalsync.data.model.ai.AiHistoryClearResponse
import com.simats.vitalsync.data.model.ai.AiInsightResponse
import com.simats.vitalsync.data.model.ai.AiSessionListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AiApiService {

    @GET("ai/sessions.php")
    suspend fun getSessions(
        @Query("user_id") userId: Int? = null
    ): Response<AiSessionListResponse>

    @POST("ai/sessions.php")
    suspend fun createSession(
        @Body request: AiCreateSessionRequest
    ): Response<AiCreateSessionResponse>

    @DELETE("ai/sessions.php")
    suspend fun deleteSession(
        @Query("session_id") sessionId: Int,
        @Query("user_id") userId: Int? = null
    ): Response<AiBaseResponse>

    @GET("ai/chat.php")
    suspend fun getChatMessages(
        @Query("user_id") userId: Int? = null,
        @Query("session_id") sessionId: Int? = null,
        @Query("email") email: String? = null
    ): Response<AiChatListResponse>

    @POST("ai/chat.php")
    suspend fun sendChatMessage(
        @Body request: AiChatRequest
    ): Response<AiChatResponse>

    @GET("ai/daily_insights.php")
    suspend fun getDailyInsight(
        @Query("user_id") userId: Int? = null,
        @Query("refresh") refresh: Boolean? = null
    ): Response<AiDailyInsightsResponse>

    @GET("ai/get_ai_insights.php")
    suspend fun getAiInsight(
        @Query("user_id") userId: Int,
        @Query("is_bt_connected") isBtConnected: Boolean,
        @Query("hydration_ml") hydrationMl: Int? = null,
        @Query("sleep_text") sleepText: String? = null,
        @Query("sleep_score") sleepScore: Int? = null,
        @Query("steps") steps: Int? = null,
        @Query("target_water_ml") targetWaterMl: Int? = null
    ): Response<AiInsightResponse>

    @DELETE("ai/chat.php")
    suspend fun clearChatHistory(
        @Query("user_id") userId: Int? = null,
        @Query("session_id") sessionId: Int? = null
    ): Response<AiHistoryClearResponse>
}

