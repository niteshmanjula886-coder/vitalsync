package com.simats.vitalsync.data.network

import android.os.Build
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ApiConstants {

    /**
     * Local machine LAN IP (for physical devices connected to same Wi-Fi).
     */
    const val LAN_BASE_URL = "http://10.18.112.52/vitalsync-backend/"

    /**
     * Android emulator loopback.
     */
    const val EMULATOR_BASE_URL = "http://10.0.2.2/vitalsync-backend/"

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
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")

    /**
     * Active Default Base URL:
     * - Uses 10.0.2.2 if running on an Android Emulator
     * - Uses host LAN IP 10.18.112.52 for physical phones on Wi-Fi
     */
    val DEFAULT_BASE_URL: String
        get() = if (isRunningOnEmulator) EMULATOR_BASE_URL else LAN_BASE_URL

    // Fast-fail network timeout settings so offline/unreachable servers never hang startup
    const val CONNECT_TIMEOUT_SECONDS = 5L
    const val READ_TIMEOUT_SECONDS = 10L
    const val WRITE_TIMEOUT_SECONDS = 10L

    fun resolveNetworkErrorMessage(
        throwable: Throwable?,
        defaultMessage: String = SERVER_UNREACHABLE_MESSAGE
    ): String {
        return when (throwable) {
            is SocketTimeoutException,
            is ConnectException,
            is UnknownHostException,
            is IOException -> SERVER_UNREACHABLE_MESSAGE
            else -> throwable?.localizedMessage?.takeIf { it.isNotBlank() } ?: defaultMessage
        }
    }
}
