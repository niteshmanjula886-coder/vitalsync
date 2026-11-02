package com.simats.vitalsync.data.network

import com.simats.vitalsync.data.model.vitals.AddVitalReadingRequest
import com.simats.vitalsync.data.model.vitals.AddVitalReadingResponse
import com.simats.vitalsync.data.model.vitals.BloodPressureResponse
import com.simats.vitalsync.data.model.vitals.BmiResponse
import com.simats.vitalsync.data.model.vitals.DeleteVitalReadingResponse
import com.simats.vitalsync.data.model.vitals.HeartRateResponse
import com.simats.vitalsync.data.model.vitals.Spo2Response
import com.simats.vitalsync.data.model.vitals.VitalsReadingsResponse
import com.simats.vitalsync.data.model.vitals.VitalsSummaryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VitalsApiService {

    @GET("vitals/summary.php")
    suspend fun getSummary(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null
    ): Response<VitalsSummaryResponse>

    @GET("vitals/heart_rate.php")
    suspend fun getHeartRate(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null
    ): Response<HeartRateResponse>

    @GET("vitals/blood_pressure.php")
    suspend fun getBloodPressure(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null
    ): Response<BloodPressureResponse>

    @GET("vitals/spo2.php")
    suspend fun getSpo2(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null
    ): Response<Spo2Response>

    @GET("vitals/bmi.php")
    suspend fun getBmi(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null
    ): Response<BmiResponse>

    @GET("vitals/readings.php")
    suspend fun getReadings(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null,
        @Query("reading_type") type: String? = null,
        @Query("limit") limit: Int = 30
    ): Response<VitalsReadingsResponse>

    @POST("vitals/readings.php")
    suspend fun addReading(
        @Body body: AddVitalReadingRequest
    ): Response<AddVitalReadingResponse>

    @DELETE("vitals/readings.php")
    suspend fun deleteReading(
        @Query("id") id: Int,
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null
    ): Response<DeleteVitalReadingResponse>

    @GET("api/bluetooth/get_vital_analytics.php")
    suspend fun getVitalAnalytics(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null,
        @Query("timeframe") timeframe: String = "week"
    ): Response<com.simats.vitalsync.data.model.vitals.VitalAnalyticsResponse>

    @POST("api/bluetooth/sync_vitals.php")
    suspend fun syncVitals(
        @Body body: com.simats.vitalsync.data.model.vitals.SyncVitalsRequest
    ): Response<com.simats.vitalsync.data.model.vitals.SyncVitalsResponse>
}

