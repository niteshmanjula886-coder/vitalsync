package com.simats.vitalsync.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    @Volatile
    private var currentBaseUrl: String = ApiConstants.DEFAULT_BASE_URL

    @Volatile
    private var retrofitInstance: Retrofit? = null

    @Volatile
    private var authApiServiceInstance: AuthApiService? = null

    @Volatile
    private var activityApiServiceInstance: ActivityApiService? = null

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(ApiConstants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(ApiConstants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(ApiConstants.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    fun getBaseUrl(): String = currentBaseUrl

    fun updateBaseUrl(newBaseUrl: String) {
        val formattedUrl = if (newBaseUrl.endsWith("/")) newBaseUrl else "$newBaseUrl/"
        if (formattedUrl != currentBaseUrl) {
            currentBaseUrl = formattedUrl
            synchronized(this) {
                retrofitInstance = null
                authApiServiceInstance = null
                activityApiServiceInstance = null
            }
        }
    }

    private fun getRetrofit(): Retrofit {
        return retrofitInstance ?: synchronized(this) {
            retrofitInstance ?: Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build().also { retrofitInstance = it }
        }
    }

    val authApiService: AuthApiService
        get() = authApiServiceInstance ?: synchronized(this) {
            authApiServiceInstance ?: getRetrofit().create(AuthApiService::class.java).also {
                authApiServiceInstance = it
            }
        }

    val activityApiService: ActivityApiService
        get() = activityApiServiceInstance ?: synchronized(this) {
            activityApiServiceInstance ?: getRetrofit().create(ActivityApiService::class.java).also {
                activityApiServiceInstance = it
            }
        }
}
