package com.simats.vitalsync.data.repository

import com.simats.vitalsync.data.local.SessionManager
import com.simats.vitalsync.data.model.ai.AiChatListResponse
import com.simats.vitalsync.data.model.ai.AiChatMessageDto
import com.simats.vitalsync.data.model.ai.AiChatRequest
import com.simats.vitalsync.data.model.ai.AiChatResponse
import com.simats.vitalsync.data.model.ai.AiDailyInsightDto
import com.simats.vitalsync.data.model.ai.AiDailyInsightsResponse
import com.simats.vitalsync.data.model.ai.AiHistoryClearResponse
import com.simats.vitalsync.data.network.AiApiService
import com.simats.vitalsync.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiRepository(
    private val apiService: AiApiService = RetrofitClient.aiApiService,
    private val sessionManager: SessionManager? = null
) {

    private fun getCurrentUserId(): Int? {
        val user = sessionManager?.getUser()
        return if (user != null && user.id > 0) user.id else null
    }

    private fun getCurrentUserEmail(): String? {
        return sessionManager?.getUser()?.email
    }

    suspend fun getChatMessages(userId: Int? = null, email: String? = null): Resource<List<AiChatMessageDto>> =
        withContext(Dispatchers.IO) {
            try {
                val resolvedUserId = userId ?: getCurrentUserId()
                val resolvedEmail = email ?: getCurrentUserEmail()
                val response = apiService.getChatMessages(resolvedUserId, resolvedEmail)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        Resource.Success(body.messages)
                    } else {
                        Resource.Error(body?.message ?: "Failed to retrieve conversation history")
                    }
                } else {
                    Resource.Error("HTTP Error: ${response.code()} ${response.message()}", response.code())
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Network error while loading chat", throwable = e)
            }
        }

    suspend fun sendChatMessage(message: String, userId: Int? = null, email: String? = null): Resource<AiChatResponse> =
        withContext(Dispatchers.IO) {
            try {
                val resolvedUserId = userId ?: getCurrentUserId()
                val resolvedEmail = email ?: getCurrentUserEmail()
                val request = AiChatRequest(
                    userId = resolvedUserId,
                    email = resolvedEmail,
                    message = message
                )
                val response = apiService.sendChatMessage(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        Resource.Success(body)
                    } else {
                        Resource.Error(body?.message ?: "Failed to generate AI response")
                    }
                } else {
                    Resource.Error("HTTP Error: ${response.code()} ${response.message()}", response.code())
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Failed to connect to AI engine", throwable = e)
            }
        }

    suspend fun getDailyInsight(userId: Int? = null, refresh: Boolean = false): Resource<AiDailyInsightDto> =
        withContext(Dispatchers.IO) {
            try {
                val resolvedUserId = userId ?: getCurrentUserId()
                val response = apiService.getDailyInsight(resolvedUserId, if (refresh) true else null)
                if (response.isSuccessful) {
                    val body = response.body()
                    val insight = body?.insight
                    if (body != null && body.success && insight != null) {
                        Resource.Success(insight)
                    } else {
                        Resource.Error(body?.message ?: "No daily insight available")
                    }
                } else {
                    Resource.Error("HTTP Error: ${response.code()}", response.code())
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Failed to load daily insight", throwable = e)
            }
        }

    suspend fun clearChatHistory(userId: Int? = null): Resource<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val resolvedUserId = userId ?: getCurrentUserId()
                val response = apiService.clearChatHistory(resolvedUserId)
                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(true)
                } else {
                    Resource.Error(response.body()?.message ?: "Failed to clear history")
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Error clearing chat history", throwable = e)
            }
        }
}
