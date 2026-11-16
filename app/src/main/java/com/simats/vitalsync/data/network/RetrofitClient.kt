package com.simats.vitalsync.data.network

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val TAG = "RetrofitClient"

    @Volatile
    private var currentBaseUrl: String = ApiConstants.DEFAULT_BASE_URL

    @Volatile
    var isDebugLoggingEnabled: Boolean = true

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

    fun configureDebugLogging(context: Context) {
        isDebugLoggingEnabled =
            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = if (isDebugLoggingEnabled) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
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
                    if (!response.isSuccessful) {
                        Log.e(TAG, "HTTP ${response.code} ${response.message} from $effectiveUrl")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Request to $effectiveUrl failed (${e.javaClass.simpleName}): ${e.message}", e)
                    attemptException = e
                    verifiedBaseUrlPrefix = null
                    response = null
                }

                // Return any HTTP response (2xx/4xx/5xx) immediately without probing alternate hosts
                if (response != null) {
                    return@addInterceptor response
                }

                // Do not replay requests onto alternate hosts when the backend received the request but read timed out
                if (attemptException != null && !isHostUnreachableError(attemptException)) {
                    throw attemptException
                }

                // Probe local/emulator/ADB-reverse/tunnel host candidates on connection/DNS failure
                val candidateUrls = if (ApiConstants.isRunningOnEmulator) {
                    listOf(
                        ApiConstants.EMULATOR_BASE_URL,
                        ApiConstants.ADB_LOCALHOST_BASE_URL,
                        ApiConstants.LOCALHOST_BASE_URL,
                        ApiConstants.LAN_BASE_URL,
                        ApiConstants.LAN_HOTSPOT_BASE_URL,
                        ApiConstants.LAN_WIFI_BASE_URL,
                        ApiConstants.REMOTE_DEVTUNNEL_URL
                    )
                } else {
                    listOf(
                        ApiConstants.LAN_BASE_URL,
                        ApiConstants.LAN_HOTSPOT_BASE_URL,
                        ApiConstants.LAN_WIFI_BASE_URL,
                        ApiConstants.ADB_LOCALHOST_BASE_URL,
                        ApiConstants.LOCALHOST_BASE_URL,
                        ApiConstants.REMOTE_DEVTUNNEL_URL
                    )
                }.distinct()

                for (fallbackBase in candidateUrls) {
                    if (!effectiveUrl.startsWith(fallbackBase)) {
                        val altUrl = replaceBasePrefix(effectiveUrl, fallbackBase)
                        val altRequest = requestWithHeaders.newBuilder()
                            .url(altUrl)
                            .header("Connection", "close")
                            .header("X-Tunnel-Skip-Anti-Abuse", "true")
                            .build()
                        try {
                            val altResponse = chain
                                .withConnectTimeout(ApiConstants.CONNECT_TIMEOUT_SECONDS.toInt(), TimeUnit.SECONDS)
                                .withReadTimeout(ApiConstants.READ_TIMEOUT_SECONDS.toInt(), TimeUnit.SECONDS)
                                .proceed(altRequest)
                            verifiedBaseUrlPrefix = fallbackBase
                            Log.i(TAG, "Connected via fallback host: $fallbackBase")
                            if (!altResponse.isSuccessful) {
                                Log.e(TAG, "HTTP ${altResponse.code} ${altResponse.message} from $altUrl")
                            }
                            return@addInterceptor altResponse
                        } catch (e: Exception) {
                            Log.w(TAG, "Fallback request to $altUrl failed: ${e.message}")
                        }
                    }
                }

                throw (attemptException ?: IOException(ApiConstants.SERVER_UNREACHABLE_MESSAGE))
            }
            .connectionPool(ConnectionPool(0, 1, TimeUnit.SECONDS))
            .addInterceptor(logging)
            .connectTimeout(ApiConstants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(ApiConstants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(ApiConstants.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun isHostUnreachableError(e: Exception): Boolean {
        return when (e) {
            is ConnectException,
            is UnknownHostException,
            is NoRouteToHostException -> true
            is SocketTimeoutException ->
                e.message?.contains("connect", ignoreCase = true) == true
            else -> false
        }
    }

    private fun replaceBasePrefix(url: String, targetBase: String): String {
        val regex = Regex("^https?://[^/]+/vitalsync-backend/")
        return if (regex.containsMatchIn(url)) {
            url.replaceFirst(regex, targetBase)
        } else {
            val currentBasePrefix = when {
                url.startsWith(ApiConstants.EMULATOR_BASE_URL) -> ApiConstants.EMULATOR_BASE_URL
                url.startsWith(ApiConstants.LAN_HOTSPOT_BASE_URL) -> ApiConstants.LAN_HOTSPOT_BASE_URL
                url.startsWith(ApiConstants.LAN_WIFI_BASE_URL) -> ApiConstants.LAN_WIFI_BASE_URL
                url.startsWith(ApiConstants.LAN_BASE_URL) -> ApiConstants.LAN_BASE_URL
                url.startsWith(ApiConstants.ADB_LOCALHOST_BASE_URL) -> ApiConstants.ADB_LOCALHOST_BASE_URL
                url.startsWith(ApiConstants.LOCALHOST_BASE_URL) -> ApiConstants.LOCALHOST_BASE_URL
                url.startsWith(ApiConstants.REMOTE_DEVTUNNEL_URL) -> ApiConstants.REMOTE_DEVTUNNEL_URL
                else -> currentBaseUrl
            }
            if (url.startsWith(currentBasePrefix)) {
                url.replaceFirst(currentBasePrefix, targetBase)
            } else {
                url
            }
        }
    }

    fun getBaseUrl(): String = currentBaseUrl

    /**
     * Resolves an avatar URL or relative path against the active backend host and downloads its bytes.
     */
    suspend fun downloadImageBytes(urlOrPath: String): ByteArray? = withContext(Dispatchers.IO) {
        val raw = urlOrPath.trim()
        if (raw.isBlank()) return@withContext null

        val targetUrl = if (raw.startsWith("http://", ignoreCase = true) || raw.startsWith("https://", ignoreCase = true)) {
            val idx = raw.indexOf("/vitalsync-backend/", ignoreCase = true)
            if (idx >= 0) {
                val relative = raw.substring(idx + "/vitalsync-backend/".length).trimStart('/')
                currentBaseUrl + relative
            } else {
                raw
            }
        } else {
            currentBaseUrl + raw.trimStart('/')
        }

        try {
            val request = Request.Builder()
                .url(targetUrl)
                .get()
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.bytes()
                } else {
                    Log.w(TAG, "Failed to download avatar ($targetUrl): HTTP ${response.code}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error downloading avatar ($targetUrl): ${e.message}")
            null
        }
    }

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
