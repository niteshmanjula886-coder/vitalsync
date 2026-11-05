package com.simats.vitalsync.data.network

import okhttp3.ConnectionPool
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

    @Volatile
    private var gymApiServiceInstance: GymApiService? = null

    @Volatile
    private var nutritionApiServiceInstance: NutritionApiService? = null

    @Volatile
    private var hydrationApiServiceInstance: HydrationApiService? = null

    @Volatile
    private var profileApiServiceInstance: ProfileApiService? = null

    @Volatile
    private var vitalsApiServiceInstance: VitalsApiService? = null

    @Volatile
    private var aiApiServiceInstance: AiApiService? = null

    @Volatile
    var authHeaderProvider: (() -> String?)? = null

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            // Always set Connection: close and inject Authorization header from active session
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                    .header("Connection", "close")

                val token = authHeaderProvider?.invoke()
                if (!token.isNullOrBlank() && originalRequest.header("Authorization") == null) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }

                val requestWithHeaders = requestBuilder.build()
                val originalUrl = originalRequest.url.toString()

                var response: okhttp3.Response? = null
                var attemptException: Exception? = null

                try {
                    response = chain.proceed(requestWithHeaders)
                } catch (e: Exception) {
                    attemptException = e
                    response = null
                }

                // If primary request was successful or a client error (e.g. 400/401/404), return it immediately
                if (response != null && response.code < 500) {
                    return@addInterceptor response
                }

                // If primary failed (server error 500+ or network exception), attempt fallbacks
                response?.close()

                val candidateUrls = if (ApiConstants.isRunningOnEmulator) {
                    listOf(ApiConstants.EMULATOR_BASE_URL, ApiConstants.LAN_BASE_URL)
                } else {
                    listOf(ApiConstants.LAN_BASE_URL, ApiConstants.EMULATOR_BASE_URL)
                }

                for (fallbackBase in candidateUrls) {
                    if (!originalUrl.startsWith(fallbackBase)) {
                        val currentBasePrefix = when {
                            originalUrl.startsWith(ApiConstants.EMULATOR_BASE_URL) -> ApiConstants.EMULATOR_BASE_URL
                            originalUrl.startsWith(ApiConstants.LAN_BASE_URL) -> ApiConstants.LAN_BASE_URL
                            originalUrl.startsWith(ApiConstants.REMOTE_DEVTUNNEL_URL) -> ApiConstants.REMOTE_DEVTUNNEL_URL
                            else -> currentBaseUrl
                        }
                        val altUrl = originalUrl.replace(currentBasePrefix, fallbackBase)
                        val altRequest = originalRequest.newBuilder()
                            .url(altUrl)
                            .header("Connection", "close")
                            .build()
                        try {
                            val altResponse = chain.proceed(altRequest)
                            if (altResponse.isSuccessful || altResponse.code < 500) {
                                return@addInterceptor altResponse
                            }
                            altResponse.close()
                        } catch (_: Exception) {}
                    }
                }

                // If all fallbacks failed, throw original exception or proceed with original request
                if (attemptException != null) {
                    throw attemptException
                }
                chain.proceed(requestWithHeaders)
            }
            .connectionPool(ConnectionPool(0, 1, TimeUnit.SECONDS))
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
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
                gymApiServiceInstance = null
                nutritionApiServiceInstance = null
                hydrationApiServiceInstance = null
                profileApiServiceInstance = null
                vitalsApiServiceInstance = null
                aiApiServiceInstance = null
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

    val gymApiService: GymApiService
        get() = gymApiServiceInstance ?: synchronized(this) {
            gymApiServiceInstance ?: getRetrofit().create(GymApiService::class.java).also {
                gymApiServiceInstance = it
            }
        }

    val nutritionApiService: NutritionApiService
        get() = nutritionApiServiceInstance ?: synchronized(this) {
            nutritionApiServiceInstance ?: getRetrofit().create(NutritionApiService::class.java).also {
                nutritionApiServiceInstance = it
            }
        }

    val hydrationApiService: HydrationApiService
        get() = hydrationApiServiceInstance ?: synchronized(this) {
            hydrationApiServiceInstance ?: getRetrofit().create(HydrationApiService::class.java).also {
                hydrationApiServiceInstance = it
            }
        }

    val profileApiService: ProfileApiService
        get() = profileApiServiceInstance ?: synchronized(this) {
            profileApiServiceInstance ?: getRetrofit().create(ProfileApiService::class.java).also {
                profileApiServiceInstance = it
            }
        }

    val vitalsApiService: VitalsApiService
        get() = vitalsApiServiceInstance ?: synchronized(this) {
            vitalsApiServiceInstance ?: getRetrofit().create(VitalsApiService::class.java).also {
                vitalsApiServiceInstance = it
            }
        }

    val aiApiService: AiApiService
        get() = aiApiServiceInstance ?: synchronized(this) {
            aiApiServiceInstance ?: getRetrofit().create(AiApiService::class.java).also {
                aiApiServiceInstance = it
            }
        }
}
