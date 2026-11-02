package com.simats.vitalsync.data.repository

import com.simats.vitalsync.data.local.SessionManager
import com.simats.vitalsync.data.model.vitals.AddVitalReadingRequest
import com.simats.vitalsync.data.model.vitals.BloodPressureResponse
import com.simats.vitalsync.data.model.vitals.BmiDetailData
import com.simats.vitalsync.data.model.vitals.HeartRateDetailData
import com.simats.vitalsync.data.model.vitals.Spo2Response
import com.simats.vitalsync.data.model.vitals.VitalReadingItem
import com.simats.vitalsync.data.model.vitals.VitalsSummaryResponse
import com.simats.vitalsync.data.network.RetrofitClient
import com.simats.vitalsync.data.network.VitalsApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject

class VitalsRepository(
    private val apiService: VitalsApiService = RetrofitClient.vitalsApiService,
    private val sessionManager: SessionManager? = null
) {

    fun getSummary(userId: Int? = null, email: String? = null): Flow<Resource<VitalsSummaryResponse>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val response = apiService.getSummary(userId = resolvedUid, email = resolvedEmail)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error("Failed to fetch vitals summary"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error fetching vitals summary"))
        }
    }.flowOn(Dispatchers.IO)

    fun getHeartRate(userId: Int? = null, email: String? = null): Flow<Resource<HeartRateDetailData>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val response = apiService.getHeartRate(userId = resolvedUid, email = resolvedEmail)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.heartRate != null) {
                    emit(Resource.Success(body.heartRate))
                } else {
                    emit(Resource.Error("Failed to fetch heart rate details"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error fetching heart rate"))
        }
    }.flowOn(Dispatchers.IO)

    fun getBloodPressure(userId: Int? = null, email: String? = null): Flow<Resource<BloodPressureResponse>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val response = apiService.getBloodPressure(userId = resolvedUid, email = resolvedEmail)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error("Failed to fetch blood pressure details"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error fetching blood pressure"))
        }
    }.flowOn(Dispatchers.IO)

    fun getSpo2(userId: Int? = null, email: String? = null): Flow<Resource<Spo2Response>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val response = apiService.getSpo2(userId = resolvedUid, email = resolvedEmail)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error("Failed to fetch SpO2 details"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error fetching SpO2"))
        }
    }.flowOn(Dispatchers.IO)

    fun getBmi(userId: Int? = null, email: String? = null): Flow<Resource<BmiDetailData>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val response = apiService.getBmi(userId = resolvedUid, email = resolvedEmail)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.bmi != null) {
                    emit(Resource.Success(body.bmi))
                } else {
                    emit(Resource.Error("Failed to fetch BMI details"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error fetching BMI"))
        }
    }.flowOn(Dispatchers.IO)

    fun getReadings(userId: Int? = null, email: String? = null, type: String? = null, limit: Int = 30): Flow<Resource<List<VitalReadingItem>>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val response = apiService.getReadings(userId = resolvedUid, email = resolvedEmail, type = type, limit = limit)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body.readings))
                } else {
                    emit(Resource.Error("Failed to fetch readings"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error fetching readings"))
        }
    }.flowOn(Dispatchers.IO)

    fun addReading(request: AddVitalReadingRequest): Flow<Resource<VitalReadingItem>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = request.userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = request.email ?: sessionManager?.getUserEmail()
            val finalReq = request.copy(userId = resolvedUid, email = resolvedEmail)
            val response = apiService.addReading(finalReq)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.reading != null) {
                    emit(Resource.Success(body.reading))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to record vital reading"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error recording reading"))
        }
    }.flowOn(Dispatchers.IO)

    fun deleteReading(id: Int, userId: Int? = null, email: String? = null): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val response = apiService.deleteReading(id = id, userId = resolvedUid, email = resolvedEmail)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(true))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to delete reading"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error deleting reading"))
        }
    }.flowOn(Dispatchers.IO)

    private fun parseErrorMessage(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "Unknown server response"
        return try {
            val json = JSONObject(errorBody)
            json.optString("message", "Request failed with server error")
        } catch (_: Exception) {
            "An error occurred while connecting to the server"
        }
    }
}

