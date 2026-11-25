package com.simats.vitalsync.data.device

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * Represents a single heart rate measurement retrieved from Health Connect.
 */
data class WatchBpmReading(
    val bpm: Long,
    val time: Instant,
    val sourcePackageName: String? = null,
    val sourceTag: String = WatchBpmSync.SOURCE_HEALTH_CONNECT
)

/**
 * Represents a single blood oxygen (SpO2) saturation measurement retrieved from Health Connect.
 */
data class WatchSpo2Reading(
    val percentage: Int,
    val time: Instant,
    val sourcePackageName: String? = null,
    val sourceTag: String = WatchBpmSync.SOURCE_HEALTH_CONNECT
)

/**
 * Represents a single blood pressure measurement retrieved from Health Connect.
 */
data class WatchBpReading(
    val systolic: Int,
    val diastolic: Int,
    val time: Instant,
    val sourcePackageName: String? = null,
    val sourceTag: String = WatchBpmSync.SOURCE_HEALTH_CONNECT
)

enum class BluetoothReadiness {
    ENABLED,
    DISABLED,
    ADAPTER_UNAVAILABLE,
    PERMISSION_REQUIRED
}

enum class HealthConnectAvailability {
    AVAILABLE,
    UNAVAILABLE,
    UPDATE_REQUIRED
}

sealed class WatchBpmSyncResult {
    data class Success(val reading: WatchBpmReading) : WatchBpmSyncResult()
    data object NoRecentReading : WatchBpmSyncResult()
    data object BluetoothDisabled : WatchBpmSyncResult()
    data object BluetoothAdapterUnavailable : WatchBpmSyncResult()
    data object BluetoothPermissionRequired : WatchBpmSyncResult()
    data object HealthConnectUnavailable : WatchBpmSyncResult()
    data class HealthConnectUpdateRequired(val playStoreIntent: Intent) : WatchBpmSyncResult()
    data object PermissionRequired : WatchBpmSyncResult()
    data class Error(val message: String) : WatchBpmSyncResult()
}

sealed class HealthConnectVitalResult<out T> {
    data class Success<T>(val reading: T) : HealthConnectVitalResult<T>()
    data object NoRecentReading : HealthConnectVitalResult<Nothing>()
    data object HealthConnectUnavailable : HealthConnectVitalResult<Nothing>()
    data class HealthConnectUpdateRequired(val playStoreIntent: Intent) : HealthConnectVitalResult<Nothing>()
    data object PermissionRequired : HealthConnectVitalResult<Nothing>()
    data class Error(val message: String) : HealthConnectVitalResult<Nothing>()
}

/**
 * Local-first helper for checking Bluetooth & Health Connect readiness and reading
 * Heart Rate, SpO2, and Blood Pressure records from Health Connect without paid APIs.
 */
object WatchBpmSync {

    const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"
    const val SOURCE_HEALTH_CONNECT = "health_connect"
    const val SOURCE_MANUAL = "manual"

    val HEART_RATE_READ_PERMISSION: String =
        HealthPermission.getReadPermission(HeartRateRecord::class)

    val OXYGEN_SATURATION_READ_PERMISSION: String =
        HealthPermission.getReadPermission(OxygenSaturationRecord::class)

    val BLOOD_PRESSURE_READ_PERMISSION: String =
        HealthPermission.getReadPermission(BloodPressureRecord::class)

    val REQUIRED_PERMISSIONS: Set<String> = setOf(HEART_RATE_READ_PERMISSION)

    val ALL_VITAL_READ_PERMISSIONS: Set<String> = setOf(
        HEART_RATE_READ_PERMISSION,
        OXYGEN_SATURATION_READ_PERMISSION,
        BLOOD_PRESSURE_READ_PERMISSION
    )

    /**
     * Checks whether Android 12+ BLUETOOTH_CONNECT permission is granted (always true on API < 31).
     */
    fun hasBluetoothConnectPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Safely checks Bluetooth availability and enabled state using BluetoothManager & BluetoothAdapter.
     */
    fun checkBluetoothReadiness(context: Context): BluetoothReadiness {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter: BluetoothAdapter = bluetoothManager?.adapter
            ?: return BluetoothReadiness.ADAPTER_UNAVAILABLE

        if (!hasBluetoothConnectPermission(context)) {
            return BluetoothReadiness.PERMISSION_REQUIRED
        }

        return try {
            if (adapter.isEnabled) {
                BluetoothReadiness.ENABLED
            } else {
                BluetoothReadiness.DISABLED
            }
        } catch (_: SecurityException) {
            BluetoothReadiness.PERMISSION_REQUIRED
        } catch (_: Exception) {
            BluetoothReadiness.DISABLED
        }
    }

    fun isBluetoothEnabled(context: Context): Boolean =
        checkBluetoothReadiness(context) == BluetoothReadiness.ENABLED

    /**
     * Checks Health Connect SDK status cleanly via HealthConnectClient.getSdkStatus(context).
     */
    fun getHealthConnectAvailability(context: Context): HealthConnectAvailability {
        return when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                HealthConnectAvailability.UPDATE_REQUIRED
            else -> HealthConnectAvailability.UNAVAILABLE
        }
    }

    /**
     * Builds an Intent to install or update Health Connect in the Google Play Store.
     */
    fun buildHealthConnectPlayStoreIntent(context: Context): Intent {
        val uriString =
            "market://details?id=$HEALTH_CONNECT_PACKAGE&url=healthconnect%3A%2F%2Fonboarding"
        return Intent(Intent.ACTION_VIEW).apply {
            setPackage("com.android.vending")
            data = Uri.parse(uriString)
            putExtra("overlay", true)
            putExtra("callerId", context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Opens Health Connect in Google Play Store, falling back to a browser URL if Play Store is missing.
     */
    fun openHealthConnectPlayStore(context: Context): Boolean {
        return try {
            context.startActivity(buildHealthConnectPlayStoreIntent(context))
            true
        } catch (_: ActivityNotFoundException) {
            try {
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$HEALTH_CONNECT_PACKAGE")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                true
            } catch (_: Exception) {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Checks whether a specific Health Connect permission is currently granted.
     */
    suspend fun hasPermission(context: Context, permission: String): Boolean =
        withContext(Dispatchers.IO) {
            if (getHealthConnectAvailability(context) != HealthConnectAvailability.AVAILABLE) {
                return@withContext false
            }
            try {
                val client = HealthConnectClient.getOrCreate(context)
                hasPermission(client, permission)
            } catch (_: Exception) {
                false
            }
        }

    suspend fun hasPermission(client: HealthConnectClient, permission: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val granted = client.permissionController.getGrantedPermissions()
                granted.contains(permission)
            } catch (_: Exception) {
                false
            }
        }

    /**
     * Checks whether READ_HEART_RATE permission is already granted in Health Connect.
     */
    suspend fun hasHeartRatePermission(context: Context): Boolean =
        hasPermission(context, HEART_RATE_READ_PERMISSION)

    suspend fun hasHeartRatePermission(client: HealthConnectClient): Boolean =
        hasPermission(client, HEART_RATE_READ_PERMISSION)

    suspend fun hasSpo2Permission(client: HealthConnectClient): Boolean =
        hasPermission(client, OXYGEN_SATURATION_READ_PERMISSION)

    suspend fun hasBloodPressurePermission(client: HealthConnectClient): Boolean =
        hasPermission(client, BLOOD_PRESSURE_READ_PERMISSION)

    /**
     * Queries Health Connect for HeartRateRecord over the recent [windowHours] window (default 24h),
     * flattens all HeartRateRecord.samples, and returns the latest BPM sample with timestamp and origin.
     */
    suspend fun fetchLatestHeartRate(
        context: Context,
        windowHours: Long = 24L
    ): WatchBpmSyncResult = withContext(Dispatchers.IO) {
        when (checkBluetoothReadiness(context)) {
            BluetoothReadiness.ADAPTER_UNAVAILABLE -> return@withContext WatchBpmSyncResult.BluetoothAdapterUnavailable
            BluetoothReadiness.PERMISSION_REQUIRED -> return@withContext WatchBpmSyncResult.BluetoothPermissionRequired
            BluetoothReadiness.DISABLED -> return@withContext WatchBpmSyncResult.BluetoothDisabled
            BluetoothReadiness.ENABLED -> Unit
        }

        when (getHealthConnectAvailability(context)) {
            HealthConnectAvailability.UNAVAILABLE -> return@withContext WatchBpmSyncResult.HealthConnectUnavailable
            HealthConnectAvailability.UPDATE_REQUIRED -> {
                return@withContext WatchBpmSyncResult.HealthConnectUpdateRequired(
                    buildHealthConnectPlayStoreIntent(context)
                )
            }
            HealthConnectAvailability.AVAILABLE -> Unit
        }

        val client = try {
            HealthConnectClient.getOrCreate(context)
        } catch (e: Exception) {
            return@withContext WatchBpmSyncResult.Error(
                e.localizedMessage ?: "Failed to initialize Health Connect client."
            )
        }

        if (!hasHeartRatePermission(client)) {
            return@withContext WatchBpmSyncResult.PermissionRequired
        }

        return@withContext queryLatestHeartRateSample(client, windowHours)
    }

    /**
     * Performs the Health Connect HeartRateRecord query directly assuming readiness & permissions passed.
     */
    suspend fun queryLatestHeartRateSample(
        context: Context,
        windowHours: Long = 24L
    ): WatchBpmSyncResult = withContext(Dispatchers.IO) {
        if (getHealthConnectAvailability(context) != HealthConnectAvailability.AVAILABLE) {
            return@withContext WatchBpmSyncResult.HealthConnectUnavailable
        }
        val client = try {
            HealthConnectClient.getOrCreate(context)
        } catch (e: Exception) {
            return@withContext WatchBpmSyncResult.Error(
                e.localizedMessage ?: "Failed to initialize Health Connect client."
            )
        }
        queryLatestHeartRateSample(client, windowHours)
    }

    private suspend fun queryLatestHeartRateSample(
        client: HealthConnectClient,
        windowHours: Long
    ): WatchBpmSyncResult {
        return try {
            val end = Instant.now()
            val start = end.minus(windowHours.coerceAtLeast(1L), ChronoUnit.HOURS)
            val timeRangeFilter = TimeRangeFilter.between(start, end)

            var latestSample: WatchBpmReading? = null
            var pageToken: String? = null

            do {
                val request = ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = timeRangeFilter,
                    pageToken = pageToken
                )
                val response = client.readRecords(request)

                for (record in response.records) {
                    val sourcePkg = record.metadata.dataOrigin.packageName.takeIf { it.isNotBlank() }
                    for (sample in record.samples) {
                        if (sample.beatsPerMinute > 0) {
                            val currentLatest = latestSample
                            if (currentLatest == null || sample.time.isAfter(currentLatest.time)) {
                                latestSample = WatchBpmReading(
                                    bpm = sample.beatsPerMinute,
                                    time = sample.time,
                                    sourcePackageName = sourcePkg
                                )
                            }
                        }
                    }
                }

                pageToken = response.pageToken
            } while (pageToken != null)

            if (latestSample != null) {
                WatchBpmSyncResult.Success(latestSample)
            } else {
                WatchBpmSyncResult.NoRecentReading
            }
        } catch (_: SecurityException) {
            WatchBpmSyncResult.PermissionRequired
        } catch (e: Exception) {
            WatchBpmSyncResult.Error(
                e.localizedMessage ?: "Unable to read heart rate from Health Connect."
            )
        }
    }

    /**
     * Checks availability + READ_OXYGEN_SATURATION permission and queries the latest SpO2 record.
     */
    suspend fun fetchLatestSpo2(
        context: Context,
        windowHours: Long = 24L
    ): HealthConnectVitalResult<WatchSpo2Reading> = withContext(Dispatchers.IO) {
        readVitalWithPermissionCheck(context, OXYGEN_SATURATION_READ_PERMISSION) { client ->
            queryLatestSpo2(client, windowHours)
        }
    }

    /**
     * Directly queries OxygenSaturationRecord assuming READ_OXYGEN_SATURATION permission was just granted.
     */
    suspend fun queryLatestSpo2(
        context: Context,
        windowHours: Long = 24L
    ): HealthConnectVitalResult<WatchSpo2Reading> = withContext(Dispatchers.IO) {
        if (getHealthConnectAvailability(context) != HealthConnectAvailability.AVAILABLE) {
            return@withContext HealthConnectVitalResult.HealthConnectUnavailable
        }
        val client = try {
            HealthConnectClient.getOrCreate(context)
        } catch (e: Exception) {
            return@withContext HealthConnectVitalResult.Error(
                e.localizedMessage ?: "Failed to initialize Health Connect client."
            )
        }
        queryLatestSpo2(client, windowHours)
    }

    private suspend fun queryLatestSpo2(
        client: HealthConnectClient,
        windowHours: Long
    ): HealthConnectVitalResult<WatchSpo2Reading> {
        return try {
            val end = Instant.now()
            val start = end.minus(windowHours.coerceAtLeast(1L), ChronoUnit.HOURS)
            val timeRangeFilter = TimeRangeFilter.between(start, end)

            var latest: WatchSpo2Reading? = null
            var pageToken: String? = null

            do {
                val request = ReadRecordsRequest(
                    recordType = OxygenSaturationRecord::class,
                    timeRangeFilter = timeRangeFilter,
                    pageToken = pageToken
                )
                val response = client.readRecords(request)

                for (record in response.records) {
                    val pct = record.percentage.value.roundToInt()
                    if (pct in 50..100) {
                        val sourcePkg = record.metadata.dataOrigin.packageName.takeIf { it.isNotBlank() }
                        val currentLatest = latest
                        if (currentLatest == null || record.time.isAfter(currentLatest.time)) {
                            latest = WatchSpo2Reading(
                                percentage = pct,
                                time = record.time,
                                sourcePackageName = sourcePkg
                            )
                        }
                    }
                }

                pageToken = response.pageToken
            } while (pageToken != null)

            if (latest != null) {
                HealthConnectVitalResult.Success(latest)
            } else {
                HealthConnectVitalResult.NoRecentReading
            }
        } catch (_: SecurityException) {
            HealthConnectVitalResult.PermissionRequired
        } catch (e: Exception) {
            HealthConnectVitalResult.Error(
                e.localizedMessage ?: "Unable to read SpO2 from Health Connect."
            )
        }
    }

    /**
     * Checks availability + READ_BLOOD_PRESSURE permission and queries the latest BloodPressureRecord.
     */
    suspend fun fetchLatestBloodPressure(
        context: Context,
        windowHours: Long = 24L
    ): HealthConnectVitalResult<WatchBpReading> = withContext(Dispatchers.IO) {
        readVitalWithPermissionCheck(context, BLOOD_PRESSURE_READ_PERMISSION) { client ->
            queryLatestBloodPressure(client, windowHours)
        }
    }

    /**
     * Directly queries BloodPressureRecord assuming READ_BLOOD_PRESSURE permission was just granted.
     */
    suspend fun queryLatestBloodPressure(
        context: Context,
        windowHours: Long = 24L
    ): HealthConnectVitalResult<WatchBpReading> = withContext(Dispatchers.IO) {
        if (getHealthConnectAvailability(context) != HealthConnectAvailability.AVAILABLE) {
            return@withContext HealthConnectVitalResult.HealthConnectUnavailable
        }
        val client = try {
            HealthConnectClient.getOrCreate(context)
        } catch (e: Exception) {
            return@withContext HealthConnectVitalResult.Error(
                e.localizedMessage ?: "Failed to initialize Health Connect client."
            )
        }
        queryLatestBloodPressure(client, windowHours)
    }

    private suspend fun queryLatestBloodPressure(
        client: HealthConnectClient,
        windowHours: Long
    ): HealthConnectVitalResult<WatchBpReading> {
        return try {
            val end = Instant.now()
            val start = end.minus(windowHours.coerceAtLeast(1L), ChronoUnit.HOURS)
            val timeRangeFilter = TimeRangeFilter.between(start, end)

            var latest: WatchBpReading? = null
            var pageToken: String? = null

            do {
                val request = ReadRecordsRequest(
                    recordType = BloodPressureRecord::class,
                    timeRangeFilter = timeRangeFilter,
                    pageToken = pageToken
                )
                val response = client.readRecords(request)

                for (record in response.records) {
                    val sys = record.systolic.inMillimetersOfMercury.roundToInt()
                    val dia = record.diastolic.inMillimetersOfMercury.roundToInt()
                    if (sys > 0 && dia > 0 && sys > dia) {
                        val sourcePkg = record.metadata.dataOrigin.packageName.takeIf { it.isNotBlank() }
                        val currentLatest = latest
                        if (currentLatest == null || record.time.isAfter(currentLatest.time)) {
                            latest = WatchBpReading(
                                systolic = sys,
                                diastolic = dia,
                                time = record.time,
                                sourcePackageName = sourcePkg
                            )
                        }
                    }
                }

                pageToken = response.pageToken
            } while (pageToken != null)

            if (latest != null) {
                HealthConnectVitalResult.Success(latest)
            } else {
                HealthConnectVitalResult.NoRecentReading
            }
        } catch (_: SecurityException) {
            HealthConnectVitalResult.PermissionRequired
        } catch (e: Exception) {
            HealthConnectVitalResult.Error(
                e.localizedMessage ?: "Unable to read blood pressure from Health Connect."
            )
        }
    }

    private suspend inline fun <T> readVitalWithPermissionCheck(
        context: Context,
        permission: String,
        queryBlock: (HealthConnectClient) -> HealthConnectVitalResult<T>
    ): HealthConnectVitalResult<T> {
        when (getHealthConnectAvailability(context)) {
            HealthConnectAvailability.UNAVAILABLE -> return HealthConnectVitalResult.HealthConnectUnavailable
            HealthConnectAvailability.UPDATE_REQUIRED -> {
                return HealthConnectVitalResult.HealthConnectUpdateRequired(
                    buildHealthConnectPlayStoreIntent(context)
                )
            }
            HealthConnectAvailability.AVAILABLE -> Unit
        }

        val client = try {
            HealthConnectClient.getOrCreate(context)
        } catch (e: Exception) {
            return HealthConnectVitalResult.Error(
                e.localizedMessage ?: "Failed to initialize Health Connect client."
            )
        }

        if (!hasPermission(client, permission)) {
            return HealthConnectVitalResult.PermissionRequired
        }

        return queryBlock(client)
    }
}
