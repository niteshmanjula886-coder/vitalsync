package com.simats.vitalsync.data.repository

import com.simats.vitalsync.data.local.SessionManager
import com.simats.vitalsync.data.model.vitals.AddVitalReadingRequest
import com.simats.vitalsync.data.model.vitals.BloodPressureResponse
import com.simats.vitalsync.data.model.vitals.BmiDetailData
import com.simats.vitalsync.data.model.vitals.HeartRateDetailData
import com.simats.vitalsync.data.model.vitals.ManualVitalsLogRequest
import com.simats.vitalsync.data.model.vitals.ManualVitalsLogResponse
import com.simats.vitalsync.data.model.vitals.Spo2Response
import com.simats.vitalsync.data.model.vitals.VitalReadingItem
import com.simats.vitalsync.data.model.vitals.VitalsSummaryResponse
import com.simats.vitalsync.data.network.ApiConstants
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
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e, "Network error fetching vitals summary"), throwable = e))
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
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
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
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
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
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
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
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
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
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
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
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
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
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun logManualVitals(request: ManualVitalsLogRequest): Flow<Resource<ManualVitalsLogResponse>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = request.userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = request.email ?: sessionManager?.getUserEmail()
            val finalReq = request.copy(userId = resolvedUid, email = resolvedEmail)
            val response = apiService.logManualVitals(finalReq)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to save vitals"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun getHrDetails(
        userId: Int? = null,
        email: String? = null
    ): Flow<Resource<com.simats.vitalsync.data.model.vitals.VitalDetailGuidanceResponse>> =
        fetchVitalGuidance("heart rate guidance", userId, email) { uid, mail -> apiService.getHrDetails(uid, mail) }

    fun getSpo2Details(
        userId: Int? = null,
        email: String? = null
    ): Flow<Resource<com.simats.vitalsync.data.model.vitals.VitalDetailGuidanceResponse>> =
        fetchVitalGuidance("blood oxygen guidance", userId, email) { uid, mail -> apiService.getSpo2Details(uid, mail) }

    fun getBpDetails(
        userId: Int? = null,
        email: String? = null
    ): Flow<Resource<com.simats.vitalsync.data.model.vitals.VitalDetailGuidanceResponse>> =
        fetchVitalGuidance("blood pressure guidance", userId, email) { uid, mail -> apiService.getBpDetails(uid, mail) }

    private fun fetchVitalGuidance(
        label: String,
        userId: Int? = null,
        email: String? = null,
        call: suspend (Int?, String?) -> retrofit2.Response<com.simats.vitalsync.data.model.vitals.VitalDetailGuidanceResponse>
    ): Flow<Resource<com.simats.vitalsync.data.model.vitals.VitalDetailGuidanceResponse>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val response = call(resolvedUid, resolvedEmail)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e, "Network error fetching $label"), throwable = e))
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

