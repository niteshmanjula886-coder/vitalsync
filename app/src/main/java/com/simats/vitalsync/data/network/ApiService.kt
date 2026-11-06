package com.simats.vitalsync.data.network

import com.simats.vitalsync.data.model.ai.AiChatRequest
import com.simats.vitalsync.data.model.ai.AiChatResponse
import com.simats.vitalsync.data.model.ai.ChatHistoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("chat_ai.php")
    suspend fun sendAiChatMessage(
        @Body request: AiChatRequest
    ): Response<AiChatResponse>

    @GET("get_chat_history.php")
    suspend fun getChatHistory(
        @Query("user_id") userId: Int
    ): Response<ChatHistoryResponse>
}
