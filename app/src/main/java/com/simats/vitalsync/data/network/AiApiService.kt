package com.simats.vitalsync.data.network

import com.simats.vitalsync.data.model.ai.AiChatListResponse
import com.simats.vitalsync.data.model.ai.AiChatRequest
import com.simats.vitalsync.data.model.ai.AiChatResponse
import com.simats.vitalsync.data.model.ai.AiDailyInsightsResponse
import com.simats.vitalsync.data.model.ai.AiHistoryClearResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AiApiService {

    @GET("ai/chat.php")
    suspend fun getChatMessages(
        @Query("user_id") userId: Int? = null,
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

    @DELETE("ai/chat.php")
    suspend fun clearChatHistory(
        @Query("user_id") userId: Int? = null
    ): Response<AiHistoryClearResponse>
}

