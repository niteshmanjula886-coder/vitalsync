package com.simats.vitalsync.data.repository

import android.content.Context
import android.util.Log
import com.simats.vitalsync.data.analytics.DailyStatistics
import com.simats.vitalsync.data.analytics.HealthStatisticsEngine
import com.simats.vitalsync.data.analytics.MonthlyStatistics
import com.simats.vitalsync.data.analytics.WeeklyStatistics
import com.simats.vitalsync.data.device.DeviceConnectionStatus
import com.simats.vitalsync.data.device.DiscoveredDevice
import com.simats.vitalsync.data.device.HealthDeviceManager
import com.simats.vitalsync.data.device.LiveHealthMetrics
import com.simats.vitalsync.data.model.vitals.AddVitalReadingRequest
import com.simats.vitalsync.data.model.vitals.VitalReadingItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * HealthDataRepository:
 * Centralized data processing engine managing incoming Bluetooth & Health Connect readings.
 * Handles range validation, deduplication, rolling stats, local StateFlows, and MySQL synchronization.
 */
class HealthDataRepository private constructor(
    private val context: Context,
    private val vitalsRepository: VitalsRepository = VitalsRepository()
) {

    companion object {
        private const val TAG = "HealthDataRepository"

        @Volatile
        private var INSTANCE: HealthDataRepository? = null

        fun getInstance(context: Context): HealthDataRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HealthDataRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val healthDeviceManager: HealthDeviceManager = HealthDeviceManager.getInstance(context)

    // User session info
    private var activeUserId: Int? = null
    private var activeUserEmail: String? = null

    // Reactive StateFlows
    val connectionStatus: StateFlow<DeviceConnectionStatus> = healthDeviceManager.connectionStatus
    val connectedDeviceName: StateFlow<String?> = healthDeviceManager.connectedDeviceName
    val connectedDeviceAddress: StateFlow<String?> = healthDeviceManager.connectedDeviceAddress
    val batteryLevel: StateFlow<Int?> = healthDeviceManager.batteryLevel
    val lastSyncTimestamp: StateFlow<Long?> = healthDeviceManager.lastSyncTimestamp
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = healthDeviceManager.discoveredDevices
    val liveMetrics: StateFlow<LiveHealthMetrics> = healthDeviceManager.liveMetrics

    // Capabilities
    val isHeartRateSupported: StateFlow<Boolean?> = healthDeviceManager.isHeartRateSupported
    val isSpo2Supported: StateFlow<Boolean?> = healthDeviceManager.isSpo2Supported
    val isBloodPressureSupported: StateFlow<Boolean?> = healthDeviceManager.isBloodPressureSupported
    val isStepsSupported: StateFlow<Boolean?> = healthDeviceManager.isStepsSupported

    // Health Statistics
    private val _dailyStats = MutableStateFlow(DailyStatistics())
    val dailyStats: StateFlow<DailyStatistics> = _dailyStats.asStateFlow()

    private val _weeklyStats = MutableStateFlow(WeeklyStatistics())
    val weeklyStats: StateFlow<WeeklyStatistics> = _weeklyStats.asStateFlow()

    private val _monthlyStats = MutableStateFlow(MonthlyStatistics())
    val monthlyStats: StateFlow<MonthlyStatistics> = _monthlyStats.asStateFlow()

    private val _recentReadings = MutableStateFlow<List<VitalReadingItem>>(emptyList())
    val recentReadings: StateFlow<List<VitalReadingItem>> = _recentReadings.asStateFlow()

    // Deduplication trackers: readingType -> (lastValue, lastRecordedTimeMillis)
    private val lastRecordedMap = mutableMapOf<String, Pair<Float, Long>>()
    private val DEDUPLICATION_WINDOW_MS = 15000L // 15 seconds throttle for identical values

    init {
        setupIncomingDataPipeline()
    }

    fun setActiveUser(userId: Int?, email: String?) {
        activeUserId = userId
        activeUserEmail = email
        if (!email.isNullOrBlank()) {
            loadHistoricalReadings()
        }
    }

    private fun setupIncomingDataPipeline() {
        healthDeviceManager.onReadingReceivedListener = { type, val1, val2, text, unit ->
            processAndPersistReading(type, val1, val2, text, unit)
        }

        // Recompute daily stats whenever liveMetrics changes
        scope.launch {
            healthDeviceManager.liveMetrics.collect { metrics ->
                recalculateStatistics(metrics)
            }
        }
    }

    private fun processAndPersistReading(
        type: String,
        val1: Float,
        val2: Float?,
        text: String,
        unit: String
    ) {
        // 1. Range Validation
        if (!validateReadingRange(type, val1, val2)) {
            Log.w(TAG, "Rejected out-of-range reading: $type val1=$val1 val2=$val2")
            return
        }

        // 2. Deduplication check
        val now = System.currentTimeMillis()
        val last = lastRecordedMap[type]
        if (last != null && last.first == val1 && (now - last.second) < DEDUPLICATION_WINDOW_MS) {
            // Skip sending duplicate to remote database, live UI is already updated via liveMetrics
            return
        }
        lastRecordedMap[type] = Pair(val1, now)

        // 3. Determine status label
        val statusLabel = when (type) {
            "heart_rate" -> {
                when {
                    val1 < 60f -> "Low Rate"
                    val1 <= 100f -> "Normal"
                    else -> "Elevated"
                }
            }
            "spo2" -> if (val1 >= 95f) "Optimal" else "Low"
            "blood_pressure" -> {
                val sys = val1
                val dia = val2 ?: 80f
                when {
                    sys <= 120f && dia <= 80f -> "Optimal"
                    sys <= 129f && dia <= 80f -> "Elevated"
                    else -> "Hypertension"
                }
            }
            "steps" -> "Active"
            "sleep" -> "Recorded"
            else -> "Recorded"
        }

        val deviceName = healthDeviceManager.connectedDeviceName.value ?: "Bluetooth Health Device"
        val source = "Bluetooth: $deviceName"
        val timestampStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(now))

        // 4. Update local reading cache
        val localItem = VitalReadingItem(
            id = (now % 100000).toInt(),
            readingType = type,
            valueNumeric = val1,
            valueSecondary = val2,
            valueText = text,
            unit = unit,
            statusLabel = statusLabel,
            source = source,
            recordedAt = timestampStr
        )
        _recentReadings.update { current ->
            listOf(localItem) + current.take(49)
        }

        // 5. Asynchronously persist to backend MySQL database
        val email = activeUserEmail
        val userId = activeUserId
        if (!email.isNullOrBlank() || userId != null) {
            scope.launch {
                val req = AddVitalReadingRequest(
                    userId = userId,
                    email = email,
                    readingType = type,
                    valueNumeric = val1,
                    valueSecondary = val2,
                    valueText = text,
                    unit = unit,
                    statusLabel = statusLabel,
                    source = source,
                    recordedAt = timestampStr
                )
                vitalsRepository.addReading(req).collect { res ->
                    when (res) {
                        is Resource.Success -> {
                            Log.d(TAG, "Successfully persisted $type reading to MySQL: ${res.data.id}")
                        }
                        is Resource.Error -> {
                            Log.w(TAG, "Failed to persist $type to MySQL: ${res.message}")
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun validateReadingRange(type: String, val1: Float, val2: Float?): Boolean {
        return when (type) {
            "heart_rate" -> val1 in 35f..240f
            "spo2" -> val1 in 65f..100f
            "blood_pressure" -> {
                val sys = val1
                val dia = val2 ?: return false
                sys in 50f..260f && dia in 30f..180f && sys > dia
            }
            "steps" -> val1 >= 0f
            "sleep" -> val1 in 30f..1440f
            else -> true
        }
    }

    private fun recalculateStatistics(metrics: LiveHealthMetrics) {
        val readings = _recentReadings.value
        _dailyStats.value = HealthStatisticsEngine.computeDaily(
            readings = readings,
            deviceSteps = metrics.steps,
            deviceCalories = metrics.calories,
            deviceDistanceKm = metrics.distanceKm,
            deviceSleepMinutes = metrics.sleepMinutes
        )
        _weeklyStats.value = HealthStatisticsEngine.computeWeekly(
            readings = readings,
            currentDailySteps = metrics.steps ?: 0
        )
        _monthlyStats.value = HealthStatisticsEngine.computeMonthly(readings)
    }

    fun loadHistoricalReadings() {
        val email = activeUserEmail
        val userId = activeUserId
        if (email.isNullOrBlank() && userId == null) return

        scope.launch {
            vitalsRepository.getReadings(userId = userId, email = email, limit = 50).collect { res ->
                if (res is Resource.Success) {
                    _recentReadings.value = res.data
                    recalculateStatistics(healthDeviceManager.liveMetrics.value)
                }
            }
        }
    }

    fun startScan() {
        healthDeviceManager.startScan()
    }

    fun stopScan() {
        healthDeviceManager.stopScan()
    }

    fun connectDevice(device: DiscoveredDevice) {
        healthDeviceManager.connectDevice(device)
    }

    fun disconnect() {
        healthDeviceManager.disconnect()
    }

    suspend fun syncHealthConnect() {
        healthDeviceManager.syncHealthConnectData()
    }
}

