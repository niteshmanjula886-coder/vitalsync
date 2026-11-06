package com.simats.vitalsync.data.sensor

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

/**
 * Universal Hardware & Motion Step Sensor Manager.
 * Operates across three sensor tiers:
 *   Tier 1: Sensor.TYPE_STEP_COUNTER (Hardware continuous since boot)
 *   Tier 2: Sensor.TYPE_STEP_DETECTOR (Hardware per-step interrupt)
 *   Tier 3: Sensor.TYPE_ACCELEROMETER (Universal dynamic acceleration peak detection)
 *
 * Automatically handles:
 *   - Midnight date rollover & daily baseline resets
 *   - Device reboot recovery (step counter reset)
 *   - SharedPreferences offline caching
 *   - Real-time reactive StateFlows for UI
 */
class StepSensorManager private constructor(context: Context) : SensorEventListener {

    private val appContext = context.applicationContext
    private val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val prefs: SharedPreferences = appContext.getSharedPreferences("vitalsync_step_tracker", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _liveSteps = MutableStateFlow(0)
    val liveSteps: StateFlow<Int> = _liveSteps.asStateFlow()

    private val _liveDistanceKm = MutableStateFlow(0f)
    val liveDistanceKm: StateFlow<Float> = _liveDistanceKm.asStateFlow()

    private val _liveCalories = MutableStateFlow(0)
    val liveCalories: StateFlow<Int> = _liveCalories.asStateFlow()

    private val _isSensorActive = MutableStateFlow(false)
    val isSensorActive: StateFlow<Boolean> = _isSensorActive.asStateFlow()

    private val _sensorName = MutableStateFlow("Initializing")
    val sensorName: StateFlow<String> = _sensorName.asStateFlow()

    // Active registered sensor
    private var registeredSensor: Sensor? = null
    private var currentTier: Int = 0 // 1: Counter, 2: Detector, 3: Accelerometer

    // Accelerometer fallback state
    private val gravity = FloatArray(3)
    private var lastAccStepTimestamp: Long = 0L

    // Server sync callback
    var onStepThresholdReached: ((steps: Int, distanceKm: Float, calories: Int) -> Unit)? = null
    private var lastSyncedSteps = 0

    // External offset to prevent step count from dropping below database records
    private var baselineManualOffset = 0

    companion object {
        private const val TAG = "StepSensorManager"

        @Volatile
        private var INSTANCE: StepSensorManager? = null

        fun getInstance(context: Context): StepSensorManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StepSensorManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    init {
        // Load stored steps on startup
        checkAndResetDailyWindow()
        val savedSteps = prefs.getInt("today_steps_${getTodayDateString()}", 0)
        _liveSteps.value = savedSteps
        updateMetrics(savedSteps)
    }

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun checkAndResetDailyWindow(): String {
        val today = getTodayDateString()
        val lastDate = prefs.getString("last_tracked_date", null)
        if (lastDate != today) {
            // New day detected: persist today's initial record
            prefs.edit()
                .putString("last_tracked_date", today)
                .putFloat("boot_baseline_$today", -1f)
                .apply()
        }
        return today
    }

    /**
     * Seeds the step counter with existing database / workout steps for today
     * so that live tracking never displays fewer steps than already verified.
     */
    fun seedBaseSteps(existingVerifiedSteps: Int) {
        if (existingVerifiedSteps > _liveSteps.value) {
            val delta = existingVerifiedSteps - _liveSteps.value
            baselineManualOffset += delta
            val newTotal = _liveSteps.value + delta
            _liveSteps.value = newTotal
            updateMetrics(newTotal)
            persistCurrentSteps(newTotal)
        }
    }

    /**
     * Start live step tracking. Selects the most accurate hardware sensor available.
     */
    fun startTracking() {
        if (_isSensorActive.value && registeredSensor != null) return

        if (sensorManager == null) {
            Log.e(TAG, "SensorManager unavailable on this device")
            _sensorName.value = "Sensor Unavailable"
            return
        }

        checkAndResetDailyWindow()

        val hasActivityPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        var registered = false

        if (hasActivityPermission && stepCounter != null) {
            registered = sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI)
            if (registered) {
                registeredSensor = stepCounter
                currentTier = 1
                _sensorName.value = "Hardware Step Counter"
                Log.d(TAG, "Registered TYPE_STEP_COUNTER")
            }
        }

        if (!registered && hasActivityPermission && stepDetector != null) {
            registered = sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_UI)
            if (registered) {
                registeredSensor = stepDetector
                currentTier = 2
                _sensorName.value = "Hardware Step Detector"
                Log.d(TAG, "Registered TYPE_STEP_DETECTOR")
            }
        }

        if (!registered && accelerometer != null) {
            registered = sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            if (registered) {
                registeredSensor = accelerometer
                currentTier = 3
                _sensorName.value = "Motion Accelerometer"
                Log.d(TAG, "Registered TYPE_ACCELEROMETER (Fallback)")
            }
        }

        _isSensorActive.value = registered
        if (!registered) {
            _sensorName.value = "No Sensor Available"
            Log.w(TAG, "Could not register any step or motion sensors")
        }
    }

    fun stopTracking() {
        if (_isSensorActive.value) {
            sensorManager?.unregisterListener(this)
            registeredSensor = null
            _isSensorActive.value = false
            Log.d(TAG, "Step sensor unregistered")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val today = checkAndResetDailyWindow()

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalBootSteps = event.values[0]
                var baseline = prefs.getFloat("boot_baseline_$today", -1f)

                // First reading today, or device reboot occurred (boot steps went below baseline)
                if (baseline < 0f || totalBootSteps < baseline) {
                    baseline = totalBootSteps
                    prefs.edit().putFloat("boot_baseline_$today", baseline).apply()
                }

                val deltaSinceBaseline = (totalBootSteps - baseline).toInt().coerceAtLeast(0)
                val totalTodaySteps = deltaSinceBaseline + baselineManualOffset
                val currentMax = prefs.getInt("today_steps_$today", 0)
                val resolvedSteps = maxOf(totalTodaySteps, currentMax)

                if (resolvedSteps != _liveSteps.value) {
                    _liveSteps.value = resolvedSteps
                    updateMetrics(resolvedSteps)
                    persistCurrentSteps(resolvedSteps)
                    checkSyncThreshold(resolvedSteps)
                }
            }

            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values[0] == 1.0f) {
                    incrementStep(today)
                }
            }

            Sensor.TYPE_ACCELEROMETER -> {
                detectStepFromAccelerometer(event.values, today)
            }
        }
    }

    private fun incrementStep(today: String) {
        val current = _liveSteps.value + 1
        _liveSteps.value = current
        updateMetrics(current)
        persistCurrentSteps(current)
        checkSyncThreshold(current)
    }

    private fun detectStepFromAccelerometer(values: FloatArray, today: String) {
        val alpha = 0.8f
        gravity[0] = alpha * gravity[0] + (1 - alpha) * values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * values[2]

        val linearAccX = values[0] - gravity[0]
        val linearAccY = values[1] - gravity[1]
        val linearAccZ = values[2] - gravity[2]

        val magnitude = sqrt(linearAccX * linearAccX + linearAccY * linearAccY + linearAccZ * linearAccZ)
        val now = System.currentTimeMillis()

        // Human walking cadence: 1.85 magnitude threshold with >= 300ms debounce
        if (magnitude > 1.85f && (now - lastAccStepTimestamp) > 300L) {
            lastAccStepTimestamp = now
            incrementStep(today)
        }
    }

    private fun updateMetrics(steps: Int) {
        _liveDistanceKm.value = (steps * 0.00076f)
        _liveCalories.value = (steps * 0.04f).toInt()
    }

    private fun persistCurrentSteps(steps: Int) {
        val today = getTodayDateString()
        prefs.edit().putInt("today_steps_$today", steps).apply()
    }

    private fun checkSyncThreshold(steps: Int) {
        if (steps - lastSyncedSteps >= 25) {
            lastSyncedSteps = steps
            scope.launch {
                try {
                    onStepThresholdReached?.invoke(steps, _liveDistanceKm.value, _liveCalories.value)
                } catch (e: Exception) {
                    Log.w(TAG, "Step threshold sync error: ${e.message}")
                }
            }
        }
    }

    fun forceFlushSync() {
        val current = _liveSteps.value
        if (current > 0) {
            lastSyncedSteps = current
            onStepThresholdReached?.invoke(current, _liveDistanceKm.value, _liveCalories.value)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
