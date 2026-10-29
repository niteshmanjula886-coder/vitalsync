package com.simats.vitalsync.data.network

object ApiConstants {
    /**
     * Default base URL for Android Emulator mapping to localhost XAMPP.
     * Note: Android Emulator maps 10.0.2.2 to the host machine's 127.0.0.1.
     * For physical devices on the same Wi-Fi network, replace with the machine's LAN IP
     * (e.g., http://192.168.1.100/vitalsync_background/).
     */
    const val DEFAULT_BASE_URL = "https://z11l1qr6-80.inc1.devtunnels.ms/vitalsync_background/"

    // Network timeout settings
    const val CONNECT_TIMEOUT_SECONDS = 15L
    const val READ_TIMEOUT_SECONDS = 15L
    const val WRITE_TIMEOUT_SECONDS = 15L
}

