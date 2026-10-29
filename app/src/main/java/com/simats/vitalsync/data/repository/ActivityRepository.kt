package com.simats.vitalsync.data.repository

import com.google.gson.Gson
import com.simats.vitalsync.data.model.activity.ActivityHistoryItem
import com.simats.vitalsync.data.model.activity.ActivitySession
import com.simats.vitalsync.data.model.activity.ActivitySummary
import com.simats.vitalsync.data.model.activity.RecordSessionRequest
import com.simats.vitalsync.data.model.activity.StepGoalResponse
import com.simats.vitalsync.data.model.activity.UpdateStepGoalRequest
import com.simats.vitalsync.data.model.auth.BaseResponse
import com.simats.vitalsync.data.network.ActivityApiService
import com.simats.vitalsync.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import java.io.IOException

class ActivityRepository(
    private val apiService: ActivityApiService = RetrofitClient.activityApiService
) {

    private val gson = Gson()

    /**
     * Get aggregate activity summary for the day (steps, calories, distance, active minutes)
     */
    fun getActivitySummary(email: String): Flow<Resource<ActivitySummary>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getActivitySummary(email)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.summary != null) {
                    emit(Resource.Success(body.summary))
                } else {
                    emit(Resource.Error("Failed to load activity summary", response.code()))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()) ?: "Error loading activity summary (${response.code()})"
                emit(Resource.Error(errorMsg, response.code()))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please verify backend server connectivity.", throwable = e))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unexpected error occurred", throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Fetch daily step goal and estimated distance & calories
     */
    fun getStepGoal(email: String): Flow<Resource<StepGoalResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getStepGoal(email)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to fetch step goal", response.code()))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()) ?: "Error fetching step goal"
                emit(Resource.Error(errorMsg, response.code()))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error connecting to backend.", throwable = e))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unexpected error", throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Update daily step goal
     */
    fun updateStepGoal(email: String, newGoal: Int): Flow<Resource<StepGoalResponse>> = flow {
        emit(Resource.Loading)
        try {
            val request = UpdateStepGoalRequest(
                email = email,
                dailyStepGoal = newGoal,
                stepGoal = newGoal
            )
            val response = apiService.updateStepGoal(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to update step goal", response.code()))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()) ?: "Error updating step goal"
                emit(Resource.Error(errorMsg, response.code()))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error connecting to backend.", throwable = e))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unexpected error", throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Record a completed live activity session (walking, running, cycling, workout)
     */
    fun recordSession(request: RecordSessionRequest): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.recordSession(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.message.ifBlank { "Activity session saved successfully" }))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to save session", response.code()))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()) ?: "Error saving session (${response.code()})"
                emit(Resource.Error(errorMsg, response.code()))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error connecting to backend.", throwable = e))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unexpected error", throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Fetch activity sessions history list
     */
    fun getSessions(email: String, activityType: String? = null, limit: Int = 30): Flow<Resource<List<ActivitySession>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getSessions(email = email, activityType = activityType, limit = limit)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.sessions))
                } else {
                    emit(Resource.Error("Failed to load sessions", response.code()))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()) ?: "Error loading sessions"
                emit(Resource.Error(errorMsg, response.code()))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error connecting to backend.", throwable = e))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unexpected error", throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Fetch vitals & activity history logs
     */
    fun getHistory(email: String): Flow<Resource<List<ActivityHistoryItem>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getHistory(email)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.history))
                } else {
                    emit(Resource.Error("Failed to load history", response.code()))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()) ?: "Error loading history"
                emit(Resource.Error(errorMsg, response.code()))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error connecting to backend.", throwable = e))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unexpected error", throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    private fun parseErrorMessage(errorBody: ResponseBody?): String? {
        return try {
            val jsonString = errorBody?.string() ?: return null
            val baseResp = gson.fromJson(jsonString, BaseResponse::class.java)
            baseResp?.message?.ifBlank { null }
        } catch (_: Exception) {
            null
        }
    }
}

