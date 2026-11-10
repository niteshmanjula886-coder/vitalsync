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

    @Volatile
    private var verifiedBaseUrlPrefix: String? = null

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor { message ->
            android.util.Log.d("API_DEBUG", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            // Always set Connection: close, bypass dev-tunnel anti-abuse page, and inject Authorization header from active session
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val originalUrl = originalRequest.url.toString()

                val activeOverride = verifiedBaseUrlPrefix
                val effectiveUrl = if (!activeOverride.isNullOrBlank() && !originalUrl.startsWith(activeOverride)) {
                    replaceBasePrefix(originalUrl, activeOverride)
                } else {
                    originalUrl
                }

                val requestBuilder = originalRequest.newBuilder()
                    .url(effectiveUrl)
                    .header("Connection", "close")
                    .header("X-Tunnel-Skip-Anti-Abuse", "true")

                val token = authHeaderProvider?.invoke()
                if (!token.isNullOrBlank() && originalRequest.header("Authorization") == null) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }

                val requestWithHeaders = requestBuilder.build()

                var response: okhttp3.Response? = null
                var attemptException: Exception? = null

                try {
                    response = chain.proceed(requestWithHeaders)
                } catch (e: Exception) {
                    android.util.Log.e("API_DEBUG", "Request to $effectiveUrl failed: ${e.message}", e)
                    attemptException = e
                    response = null
                }

                // If any HTTP response was received (success, 4xx, or 5xx backend error), return it immediately without hanging
                if (response != null) {
                    return@addInterceptor response
                }

                // Only attempt fallback URL on actual network failure (timeout / unreachable host)
                val candidateUrls = if (ApiConstants.isRunningOnEmulator) {
                    listOf(ApiConstants.EMULATOR_BASE_URL, ApiConstants.LAN_BASE_URL)
                } else {
                    listOf(ApiConstants.LAN_BASE_URL, ApiConstants.REMOTE_DEVTUNNEL_URL)
                }

                for (fallbackBase in candidateUrls) {
                    if (!effectiveUrl.startsWith(fallbackBase)) {
                        val altUrl = replaceBasePrefix(effectiveUrl, fallbackBase)
                        val altRequest = requestWithHeaders.newBuilder()
                            .url(altUrl)
                            .header("Connection", "close")
                            .header("X-Tunnel-Skip-Anti-Abuse", "true")
                            .build()
                        try {
                            val altResponse = chain.proceed(altRequest)
                            verifiedBaseUrlPrefix = fallbackBase
                            return@addInterceptor altResponse
                        } catch (e: Exception) {
                            android.util.Log.w("API_DEBUG", "Fallback request to $altUrl failed: ${e.message}")
                        }
                    }
                }

                throw (attemptException ?: java.io.IOException(ApiConstants.SERVER_UNREACHABLE_MESSAGE))
            }
            .connectionPool(ConnectionPool(0, 1, TimeUnit.SECONDS))
            .addInterceptor(logging)
            .connectTimeout(ApiConstants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(ApiConstants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(ApiConstants.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
    }

    private fun replaceBasePrefix(url: String, targetBase: String): String {
        val currentBasePrefix = when {
            url.startsWith(ApiConstants.EMULATOR_BASE_URL) -> ApiConstants.EMULATOR_BASE_URL
            url.startsWith(ApiConstants.LAN_BASE_URL) -> ApiConstants.LAN_BASE_URL
            url.startsWith(ApiConstants.REMOTE_DEVTUNNEL_URL) -> ApiConstants.REMOTE_DEVTUNNEL_URL
            else -> currentBaseUrl
        }
        return if (url.startsWith(currentBasePrefix)) {
            url.replace(currentBasePrefix, targetBase)
        } else {
            url
        }
    }

    fun getBaseUrl(): String = currentBaseUrl

    fun updateBaseUrl(newBaseUrl: String) {
        val formattedUrl = if (newBaseUrl.endsWith("/")) newBaseUrl else "$newBaseUrl/"
        if (formattedUrl != currentBaseUrl) {
            currentBaseUrl = formattedUrl
            verifiedBaseUrlPrefix = null
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
