package com.simats.vitalsync.data.network

import android.os.Build
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ApiConstants {

    /**
     * Active Host Machine Wi-Fi IPv4 Base URL (detected from ipconfig):
     */
    const val BASE_URL = "http://10.205.196.52/vitalsync-backend/"

    /**
     * Local machine LAN IP (for physical devices connected to same Wi-Fi / Hotspot).
     */
    const val LAN_BASE_URL = BASE_URL
    const val LAN_HOTSPOT_BASE_URL = "http://10.205.196.52/vitalsync-backend/"
    const val LAN_WIFI_BASE_URL = "http://192.168.1.5/vitalsync-backend/"

    /**
     * Android emulator loopback.
     */
    const val EMULATOR_BASE_URL = "http://10.0.2.2/vitalsync-backend/"

    /**
     * Physical device over USB with ADB reverse (`adb reverse tcp:80 tcp:80`).
     */
    const val ADB_LOCALHOST_BASE_URL = "http://127.0.0.1/vitalsync-backend/"
    const val LOCALHOST_BASE_URL = "http://localhost/vitalsync-backend/"

    /**
     * Optional remote DevTunnel URL (if running).
     */
    const val REMOTE_DEVTUNNEL_URL = "https://z11l1qr6-80.inc1.devtunnels.ms/vitalsync-backend/"

    const val SERVER_UNREACHABLE_MESSAGE =
        "Unable to reach server. Please check your network connection and server IP."

    /**
     * Determines whether current device is an Android Emulator.
     */
    val isRunningOnEmulator: Boolean
        get() = Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_gphone")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("vbox86p")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")

    /**
     * Active Default Base URL:
     * - Uses 10.0.2.2 if running on an Android Emulator
     * - Uses host LAN IP 10.205.196.52 for physical phones on Wi-Fi/Hotspot
     */
    val DEFAULT_BASE_URL: String
        get() = if (isRunningOnEmulator) EMULATOR_BASE_URL else BASE_URL

    // Resilient timeouts supporting fast connect failover and up to 60s for local Ollama / AI inference
    const val CONNECT_TIMEOUT_SECONDS = 6L
    const val READ_TIMEOUT_SECONDS = 60L
    const val WRITE_TIMEOUT_SECONDS = 30L

    fun resolveNetworkErrorMessage(
        throwable: Throwable?,
        defaultMessage: String = SERVER_UNREACHABLE_MESSAGE
    ): String {
        return when (throwable) {
            is SocketTimeoutException -> {
                if (throwable.message?.contains("connect", ignoreCase = true) == true) {
                    SERVER_UNREACHABLE_MESSAGE
                } else {
                    "Request timed out while analyzing. Please try again."
                }
            }
            is ConnectException,
            is UnknownHostException,
            is IOException -> SERVER_UNREACHABLE_MESSAGE
            else -> throwable?.localizedMessage?.takeIf { it.isNotBlank() } ?: defaultMessage
        }
    }
}
