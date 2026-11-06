package com.simats.vitalsync.data.repository

import com.simats.vitalsync.data.local.SessionManager
import com.simats.vitalsync.data.model.ai.AiChatListResponse
import com.simats.vitalsync.data.model.ai.AiChatMessageDto
import com.simats.vitalsync.data.model.ai.AiChatRequest
import com.simats.vitalsync.data.model.ai.AiChatResponse
import com.simats.vitalsync.data.model.ai.AiChatSessionDto
import com.simats.vitalsync.data.model.ai.AiCreateSessionRequest
import com.simats.vitalsync.data.model.ai.AiCreateSessionResponse
import com.simats.vitalsync.data.model.ai.AiDailyInsightDto
import com.simats.vitalsync.data.model.ai.AiDailyInsightsResponse
import com.simats.vitalsync.data.model.ai.AiHistoryClearResponse
import com.simats.vitalsync.data.model.ai.AiInsightResponse
import com.simats.vitalsync.data.network.AiApiService
import com.simats.vitalsync.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiRepository(
    private val apiService: AiApiService = RetrofitClient.aiApiService,
    private val sessionManager: SessionManager? = null
) {

    fun getCurrentUserId(): Int? {
        val user = sessionManager?.getUser()
        return if (user != null && user.id > 0) user.id else null
    }

    private fun getCurrentUserEmail(): String? {
        return sessionManager?.getUser()?.email
    }

    suspend fun getSessions(userId: Int? = null): Resource<List<AiChatSessionDto>> =
        withContext(Dispatchers.IO) {
            try {
                val resolvedUserId = userId ?: getCurrentUserId()
                val response = apiService.getSessions(resolvedUserId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        Resource.Success(body.sessions)
                    } else {
                        Resource.Error(body?.message ?: "Failed to retrieve chat sessions")
                    }
                } else {
                    Resource.Error("HTTP Error: ${response.code()}", response.code())
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Failed to load chat sessions", throwable = e)
            }
        }

    suspend fun createSession(title: String = "New Chat", userId: Int? = null): Resource<AiCreateSessionResponse> =
        withContext(Dispatchers.IO) {
            try {
                val resolvedUserId = userId ?: getCurrentUserId()
                val request = AiCreateSessionRequest(userId = resolvedUserId, title = title)
                val response = apiService.createSession(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        Resource.Success(body)
                    } else {
                        Resource.Error(body?.message ?: "Failed to create new chat session")
                    }
                } else {
                    Resource.Error("HTTP Error: ${response.code()}", response.code())
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Failed to create chat session", throwable = e)
            }
        }

    suspend fun deleteSession(sessionId: Int, userId: Int? = null): Resource<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val resolvedUserId = userId ?: getCurrentUserId()
                val response = apiService.deleteSession(sessionId, resolvedUserId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        Resource.Success(true)
                    } else {
                        Resource.Error(body?.message ?: "Failed to delete session")
                    }
                } else {
                    Resource.Error("HTTP Error: ${response.code()}", response.code())
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Failed to delete chat session", throwable = e)
            }
        }

    suspend fun getChatMessages(sessionId: Int? = null, userId: Int? = null, email: String? = null): Resource<AiChatListResponse> =
        withContext(Dispatchers.IO) {
            try {
                val resolvedUserId = userId ?: getCurrentUserId()
                val resolvedEmail = email ?: getCurrentUserEmail()
                val response = apiService.getChatMessages(resolvedUserId, sessionId, resolvedEmail)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        Resource.Success(body)
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

    suspend fun sendAiChatMessage(userId: Int, message: String): Resource<AiChatResponse> =
        withContext(Dispatchers.IO) {
            try {
                val request = AiChatRequest(user_id = userId, message = message)
                val response = apiService.sendAiChatMessage(request)
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
                Resource.Error(e.localizedMessage ?: "Failed to connect to local AI engine", throwable = e)
            }
        }

    suspend fun getChatHistory(userId: Int? = null): Resource<List<com.simats.vitalsync.data.model.ai.ChatHistoryMessage>> =
        withContext(Dispatchers.IO) {
            try {
                val resolvedUserId = userId ?: getCurrentUserId() ?: 1
                val response = apiService.getChatHistory(resolvedUserId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        Resource.Success(body.messages)
                    } else {
                        Resource.Error(body?.message ?: "Failed to retrieve chat history")
                    }
                } else {
                    Resource.Error("HTTP Error: ${response.code()}", response.code())
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Failed to load chat history", throwable = e)
            }
        }

    suspend fun sendChatMessage(message: String, sessionId: Int? = null, userId: Int? = null, email: String? = null): Resource<AiChatResponse> =
        withContext(Dispatchers.IO) {
            val resolvedUserId = userId ?: getCurrentUserId() ?: 1
            sendAiChatMessage(resolvedUserId, message)
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

    suspend fun getAiInsight(
        userId: Int? = null,
        isBtConnected: Boolean = false,
        hydrationMl: Int? = null,
        sleepText: String? = null,
        sleepScore: Int? = null,
        steps: Int? = null,
        targetWaterMl: Int? = null
    ): Resource<AiInsightResponse> =
        withContext(Dispatchers.IO) {
            try {
                val resolvedUserId = userId ?: getCurrentUserId() ?: 1
                val response = apiService.getAiInsight(
                    userId = resolvedUserId,
                    isBtConnected = isBtConnected,
                    hydrationMl = hydrationMl,
                    sleepText = sleepText,
                    sleepScore = sleepScore,
                    steps = steps,
                    targetWaterMl = targetWaterMl
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        Resource.Success(body)
                    } else {
                        Resource.Error(body?.message ?: "Failed to retrieve AI health insight")
                    }
                } else {
                    Resource.Error("HTTP Error: ${response.code()}", response.code())
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Failed to generate AI health insight", throwable = e)
            }
        }

    suspend fun clearChatHistory(sessionId: Int? = null, userId: Int? = null): Resource<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val resolvedUserId = userId ?: getCurrentUserId()
                val response = apiService.clearChatHistory(resolvedUserId, sessionId)
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

