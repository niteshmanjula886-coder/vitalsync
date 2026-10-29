package com.simats.vitalsync.data.network

object ApiConstants {
    /**
     * Globally accessible Devtunnel base URL connecting to the VitalSync PHP backend on XAMPP.
     * Can be accessed from anywhere: physical Android devices over mobile data, any Wi-Fi, and emulators.
     */
    const val DEFAULT_BASE_URL = "https://z11l1qr6-80.inc1.devtunnels.ms/vitalsync_background/"

    /**
     * Local machine LAN IP fallback (for local subnet Wi-Fi testing without internet).
     */
    const val LAN_BASE_URL = "http://172.23.176.95/vitalsync_background/"

    /**
     * Android emulator loopback fallback.
     */
    const val EMULATOR_BASE_URL = "http://10.0.2.2/vitalsync_background/"

    // Network timeout settings
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 30L
}
