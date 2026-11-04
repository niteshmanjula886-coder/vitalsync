package com.simats.vitalsync.data.repository

import com.simats.vitalsync.data.model.hydration.HydrationDataDto
import com.simats.vitalsync.data.model.hydration.LogWaterRequest
import com.simats.vitalsync.data.model.hydration.LogWaterResponse
import com.simats.vitalsync.data.model.hydration.ResetHydrationResponse
import com.simats.vitalsync.data.model.hydration.UpdateHydrationSettingsRequest
import com.simats.vitalsync.data.model.hydration.UpdateHydrationSettingsResponse
import com.simats.vitalsync.data.model.hydration.WeeklyHydrationHistoryDataDto
import com.simats.vitalsync.data.network.HydrationApiService
import com.simats.vitalsync.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject

class HydrationRepository(
    private val apiService: HydrationApiService = RetrofitClient.hydrationApiService
) {

    fun getHydration(
        email: String,
        date: String
    ): Flow<Resource<HydrationDataDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getHydration(email, date)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.hydration != null) {
                    emit(Resource.Success(body.hydration))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to load hydration details" }))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error loading hydration details"))
        }
    }.flowOn(Dispatchers.IO)

    fun logWater(
        email: String,
        amountMl: Int,
        date: String
    ): Flow<Resource<LogWaterResponse>> = flow {
        emit(Resource.Loading)
        try {
            val req = LogWaterRequest(email = email, amountMl = amountMl, dateString = date)
            val response = apiService.logWater(req)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to log water consumption" }))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error logging water consumption"))
        }
    }.flowOn(Dispatchers.IO)

    fun updateSettings(
        email: String,
        targetMl: Int,
        remindersEnabled: Boolean
    ): Flow<Resource<UpdateHydrationSettingsResponse>> = flow {
        emit(Resource.Loading)
        try {
            val req = UpdateHydrationSettingsRequest(
                email = email,
                targetMl = targetMl,
                remindersEnabled = remindersEnabled
            )
            val response = apiService.updateSettings(req)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to update hydration settings" }))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error updating hydration settings"))
        }
    }.flowOn(Dispatchers.IO)

    fun resetHydration(
        email: String,
        date: String
    ): Flow<Resource<ResetHydrationResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.resetHydration(email, date)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to reset hydration" }))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error resetting hydration"))
        }
    }.flowOn(Dispatchers.IO)

    fun deleteLog(
        email: String,
        logId: Int
    ): Flow<Resource<ResetHydrationResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.deleteLog(email, logId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to delete log entry" }))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error deleting log entry"))
        }
    }.flowOn(Dispatchers.IO)

    fun getWeeklyHistory(
        userId: Int? = null,
        email: String? = null
    ): Flow<Resource<WeeklyHydrationHistoryDataDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getWeeklyHistory(userId = userId, email = email)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to load weekly hydration history" }))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error loading weekly hydration history"))
        }
    }.flowOn(Dispatchers.IO)

    private fun parseErrorMessage(errorJson: String?): String {
        if (errorJson.isNullOrBlank()) return "Server returned an error response"
        return try {
            val json = JSONObject(errorJson)
            json.optString("message", "An unexpected server error occurred")
        } catch (_: Exception) {
            "An unexpected server error occurred"
        }
    }
}

