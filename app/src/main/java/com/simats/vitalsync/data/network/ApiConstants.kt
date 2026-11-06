package com.simats.vitalsync.data.network

import android.os.Build

object ApiConstants {

    /**
     * Local machine LAN IP (for physical devices connected to same Wi-Fi).
     */
    const val LAN_BASE_URL = "http://10.154.129.52/vitalsync-backend/"

    /**
     * Android emulator loopback.
     */
    const val EMULATOR_BASE_URL = "http://10.0.2.2/vitalsync-backend/"

    /**
     * Optional remote DevTunnel URL (if running).
     */
    const val REMOTE_DEVTUNNEL_URL = "https://z11l1qr6-80.inc1.devtunnels.ms/vitalsync-backend/"

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
     * - Uses host LAN IP 172.23.176.95 for physical phones on Wi-Fi
     */
    val DEFAULT_BASE_URL: String
        get() = if (isRunningOnEmulator) EMULATOR_BASE_URL else LAN_BASE_URL

    // Network timeout settings - accommodates local CPU inference for AI
    const val CONNECT_TIMEOUT_SECONDS = 60L
    const val READ_TIMEOUT_SECONDS = 120L
    const val WRITE_TIMEOUT_SECONDS = 60L
}
