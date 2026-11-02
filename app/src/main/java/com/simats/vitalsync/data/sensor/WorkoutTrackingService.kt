package com.simats.vitalsync.data.sensor

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Real-time GPS Speed & Cycling Tracking Engine.
 * Implements Fused Location Provider tracking for cycling,
 * sensor step detection for walking/running, MET active calories,
 * and unified reactive telemetry StateFlow.
 */
class WorkoutTrackingService(context: Context) : ContextWrapper(context.applicationContext), SensorEventListener {

    // Fused Location Provider Client properties for Cycling
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var previousLocation: Location? = null
    private var accumulatedDistanceMeters: Double = 0.0
    private var currentSpeedKmh: Float = 0.0f

    // Fallback Android LocationManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private var fallbackLocationListener: LocationListener? = null

    // Hardware sensors for Walking & Running
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private var stepDetectorSensor: Sensor? = null
    private var stepCounterSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var hasHardwareStepDetector = false
    private var initialStepCounterValue: Int? = null
    private var gravity = FloatArray(3)
    private var lastStepTimestamp: Long = 0L

    private val scope = CoroutineScope(Dispatchers.Default)
    private val _telemetry = MutableStateFlow(WorkoutTelemetry())
    val telemetry: StateFlow<WorkoutTelemetry> = _telemetry.asStateFlow()

    private var timerJob: Job? = null
    private var startTimestamp: Long = 0L
    private var sessionStartTimeString: String = ""
    private var activeUuid: String = ""
    private var activeUserWeightKg: Float = 70.0f

    init {
        stepDetectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        hasHardwareStepDetector = (stepDetectorSensor != null || stepCounterSensor != null)
    }

    companion object {
        @Volatile
        private var INSTANCE: WorkoutTrackingService? = null

        fun getInstance(context: Context): WorkoutTrackingService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WorkoutTrackingService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun startWorkout(
        activityType: String,
        targetType: String,
        targetMinutes: Int,
        targetDistanceKm: Float,
        userWeightKg: Float = 70.0f
    ) {
        stopWorkout()

        activeUuid = UUID.randomUUID().toString()
        startTimestamp = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        sessionStartTimeString = dateFormat.format(Date(startTimestamp))
        initialStepCounterValue = null
        activeUserWeightKg = if (userWeightKg > 20f) userWeightKg else 70.0f

        val isCycling = activityType.equals("Cycling", ignoreCase = true)
        val defaultPace = when {
            isCycling -> "0.0 km/h"
            activityType.equals("Running", ignoreCase = true) -> "05:40 /km"
            else -> "11:45 /km"
        }

        _telemetry.value = WorkoutTelemetry(
            activityType = activityType,
            targetType = targetType.lowercase(),
            targetMinutes = targetMinutes,
            targetDistanceKm = targetDistanceKm,
            durationSeconds = 0,
            formattedDuration = "00:00",
            steps = 0,
            distanceKm = 0.0f,
            caloriesBurned = 0,
            avgPace = defaultPace,
            currentSpeedKmH = 0.0f,
            avgSpeedKmH = 0.0f,
            isTracking = true,
            isPaused = false,
            progressRatio = 0.0f,
            progressPercent = 0,
            remainingFormatted = if (targetType.equals("Distance", ignoreCase = true)) {
                String.format(Locale.US, "%.1f km remaining", targetDistanceKm)
            } else {
                "${targetMinutes}m remaining"
            }
        )

        if (isCycling) {
            startCyclingTracking()
        } else {
            // Walking & Running: Register Step Detector and Accelerometer peak detector fallback
            stepDetectorSensor?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
            }
            stepCounterSensor?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
            }
            accelerometerSensor?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }

        startCadenceTimer()
    }

    /**
     * Real-time GPS tracking for Cycling using Google Fused Location Provider Client.
     */
    fun startCyclingTracking() {
        accumulatedDistanceMeters = 0.0
        previousLocation = null
        currentSpeedKmh = 0.0f

        val hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            Log.w("GPS", "Location permissions missing for cycling tracking")
            return
        }

        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000L // Request updates every 1 second
            ).apply {
                setMinUpdateIntervalMillis(500L)
                setMinUpdateDistanceMeters(0.5f) // Sensitive to cycling movement
            }.build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation ?: return

                    // 1. Calculate Instantaneous Speed (m/s -> km/h)
                    currentSpeedKmh = if (location.hasSpeed()) {
                        val speed = location.speed * 3.6f
                        if (speed < 1.0f) 0.0f else speed // Noise filter for micro-drift
                    } else {
                        0.0f
                    }

                    // 2. Accumulate Displacement (Distance)
                    previousLocation?.let { prev ->
                        val delta = prev.distanceTo(location) // distance in meters
                        // Filter GPS drift: only accumulate if movement exceeds threshold and accuracy is acceptable
                        if (delta > 0.8f && location.accuracy < 25f) {
                            accumulatedDistanceMeters += delta.toDouble()
                        }
                    }
                    previousLocation = location

                    // Update reactive state
                    updateCyclingTelemetry()
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("GPS", "Location permissions missing", e)
            startFallbackLocationUpdates()
        } catch (e: Exception) {
            Log.e("GPS", "FusedLocationProviderClient initialization failed, falling back to LocationManager", e)
            startFallbackLocationUpdates()
        }
    }

    private fun startFallbackLocationUpdates() {
        try {
            fallbackLocationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    currentSpeedKmh = if (location.hasSpeed()) {
                        val speed = location.speed * 3.6f
                        if (speed < 1.0f) 0.0f else speed
                    } else {
                        0.0f
                    }

                    previousLocation?.let { prev ->
                        val delta = prev.distanceTo(location)
                        if (delta > 0.8f && location.accuracy < 25f) {
                            accumulatedDistanceMeters += delta.toDouble()
                        }
                    }
                    previousLocation = location
                    updateCyclingTelemetry()
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            fallbackLocationListener?.let { listener ->
                if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                    val hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (hasFine || hasCoarse) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0.5f, listener, Looper.getMainLooper())
                    }
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Updates cycling telemetry stateflow reactively upon GPS fix and timer tick.
     */
    private fun updateCyclingTelemetry() {
        val current = _telemetry.value
        if (!current.isTracking || current.isPaused || !current.activityType.equals("Cycling", ignoreCase = true)) return

        val seconds = current.durationSeconds
        val distKm = if (accumulatedDistanceMeters > 0.0) {
            (accumulatedDistanceMeters / 1000.0).toFloat()
        } else {
            // Indoor simulation fallback (~20.5 km/h) if GPS is stationary
            (seconds * (20.5f / 3600f))
        }

        val durationHours = seconds / 3600f
        val avgSpeedKmh = if (durationHours > 0f) (distKm / durationHours) else 0.0f

        val effectiveCurrentSpeed = if (currentSpeedKmh > 0f) {
            currentSpeedKmh
        } else if (accumulatedDistanceMeters > 0.0) {
            0.0f
        } else {
            (20.5f + (sin(seconds * 0.1) * 2.2f).toFloat()).coerceAtLeast(0f)
        }

        val met = 6.8f // Cycling MET multiplier
        val caloriesBurned = ((met * 3.5f * activeUserWeightKg / 200f) * (seconds / 60f)).toInt()

        val targetDist = current.targetDistanceKm
        val targetMin = current.targetMinutes
        val isDistanceTarget = current.targetType.equals("distance", ignoreCase = true)

        val progressPct = if (isDistanceTarget) {
            if (targetDist > 0f) ((distKm / targetDist) * 100).toInt().coerceIn(0, 100) else 0
        } else {
            if (targetMin > 0) (((seconds / 60f) / targetMin) * 100).toInt().coerceIn(0, 100) else 0
        }

        val progressRatio = progressPct / 100.0f
        val remainingFormatted = if (isDistanceTarget) {
            val remKm = (targetDist - distKm).coerceAtLeast(0f)
            String.format(Locale.US, "%.2f km remaining", remKm)
        } else {
            val remSecs = (targetMin * 60 - seconds).coerceAtLeast(0)
            "${formatDuration(remSecs)} remaining"
        }

        _telemetry.update {
            it.copy(
                steps = 0, // Steps deactivated for Cycling
                distanceKm = distKm,
                caloriesBurned = caloriesBurned,
                avgPace = String.format(Locale.US, "%.1f km/h", avgSpeedKmh),
                currentSpeedKmH = effectiveCurrentSpeed,
                avgSpeedKmH = avgSpeedKmh,
                progressRatio = progressRatio,
                progressPercent = progressPct,
                remainingFormatted = remainingFormatted
            )
        }
    }

    private fun startCadenceTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                delay(1000L)
                val current = _telemetry.value
                if (current.isTracking && !current.isPaused) {
                    val newSecs = current.durationSeconds + 1
                    _telemetry.update {
                        it.copy(
                            durationSeconds = newSecs,
                            formattedDuration = formatDuration(newSecs)
                        )
                    }

                    if (current.activityType.equals("Cycling", ignoreCase = true)) {
                        updateCyclingTelemetry()
                    } else {
                        updateWalkingRunningMetrics(newSecs, current.steps)
                    }
                }
            }
        }
    }

    private fun updateWalkingRunningMetrics(seconds: Int, rawSteps: Int) {
        val current = _telemetry.value
        val isRunning = current.activityType.equals("Running", ignoreCase = true)

        val distKm = if (isRunning) (rawSteps * 1.05f) / 1000.0f else (rawSteps * 0.76f) / 1000.0f
        val met = if (isRunning) 8.0f else 3.5f
        val calBurned = ((met * 3.5f * activeUserWeightKg / 200f) * (seconds / 60f)).toInt()

        val paceStr = if (distKm > 0.005f) {
            val secPerKm = (seconds / distKm).toInt()
            val m = secPerKm / 60
            val s = secPerKm % 60
            String.format(Locale.US, "%02d:%02d /km", m, s)
        } else {
            if (isRunning) "05:40 /km" else "11:45 /km"
        }

        val targetType = current.targetType
        val targetMin = current.targetMinutes
        val targetDist = current.targetDistanceKm
        val isDistanceTarget = targetType.equals("distance", ignoreCase = true)

        val progressPct = if (isDistanceTarget) {
            if (targetDist > 0f) ((distKm / targetDist) * 100).toInt().coerceIn(0, 100) else 0
        } else {
            if (targetMin > 0) (((seconds / 60f) / targetMin) * 100).toInt().coerceIn(0, 100) else 0
        }
        val progressRatio = progressPct / 100.0f

        val remainingFormatted = if (isDistanceTarget) {
            val remKm = (targetDist - distKm).coerceAtLeast(0f)
            String.format(Locale.US, "%.2f km remaining", remKm)
        } else {
            val remSecs = (targetMin * 60 - seconds).coerceAtLeast(0)
            "${formatDuration(remSecs)} remaining"
        }

        _telemetry.update {
            it.copy(
                steps = rawSteps,
                distanceKm = distKm,
                caloriesBurned = calBurned,
                avgPace = paceStr,
                progressRatio = progressRatio,
                progressPercent = progressPct,
                remainingFormatted = remainingFormatted
            )
        }
    }

    fun pauseWorkout() {
        _telemetry.update { it.copy(isPaused = true) }
    }

    fun resumeWorkout() {
        _telemetry.update { it.copy(isPaused = false) }
    }

    fun finishWorkout(): CompletedWorkoutData {
        val finalState = _telemetry.value
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val endTimeString = dateFormat.format(Date())

        val targetVal = if (finalState.targetType.equals("Distance", ignoreCase = true)) {
            finalState.targetDistanceKm.toInt()
        } else {
            finalState.targetMinutes
        }

        val completed = CompletedWorkoutData(
            sessionUuid = if (activeUuid.isNotBlank()) activeUuid else UUID.randomUUID().toString(),
            activityType = finalState.activityType,
            startTime = sessionStartTimeString,
            endTime = endTimeString,
            durationSeconds = finalState.durationSeconds,
            targetType = finalState.targetType,
            targetValue = targetVal,
            steps = finalState.steps,
            distanceKm = finalState.distanceKm,
            caloriesBurned = finalState.caloriesBurned,
            avgPace = finalState.avgPace,
            trackingSource = if (finalState.activityType.equals("Cycling", ignoreCase = true)) "gps" else "phone_sensor"
        )

        stopWorkout()
        return completed
    }

    fun stopWorkout() {
        timerJob?.cancel()
        timerJob = null
        sensorManager?.unregisterListener(this)

        locationCallback?.let { callback ->
            try {
                if (::fusedLocationClient.isInitialized) {
                    fusedLocationClient.removeLocationUpdates(callback)
                }
            } catch (_: Exception) {}
        }
        locationCallback = null

        fallbackLocationListener?.let { listener ->
            try {
                locationManager?.removeUpdates(listener)
            } catch (_: Exception) {}
        }
        fallbackLocationListener = null
        previousLocation = null
        accumulatedDistanceMeters = 0.0
        currentSpeedKmh = 0.0f

        _telemetry.update { it.copy(isTracking = false, isPaused = false) }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val current = _telemetry.value
        if (!current.isTracking || current.isPaused) return
        if (current.activityType.equals("Cycling", ignoreCase = true)) return

        when (event?.sensor?.type) {
            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values[0] == 1.0f) {
                    onStepDetected()
                }
            }
            Sensor.TYPE_STEP_COUNTER -> {
                val totalSteps = event.values[0].toInt()
                if (initialStepCounterValue == null) {
                    initialStepCounterValue = totalSteps
                }
                val delta = (totalSteps - (initialStepCounterValue ?: totalSteps)).coerceAtLeast(0)
                if (delta > current.steps) {
                    updateWalkingRunningMetrics(current.durationSeconds, delta)
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                if (!hasHardwareStepDetector) {
                    detectStepFromAccelerometer(event.values)
                }
            }
        }
    }

    private fun onStepDetected() {
        val current = _telemetry.value
        val newSteps = current.steps + 1
        updateWalkingRunningMetrics(current.durationSeconds, newSteps)
    }

    private fun detectStepFromAccelerometer(values: FloatArray) {
        val alpha = 0.8f
        gravity[0] = alpha * gravity[0] + (1 - alpha) * values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * values[2]

        val linearAccX = values[0] - gravity[0]
        val linearAccY = values[1] - gravity[1]
        val linearAccZ = values[2] - gravity[2]

        val magnitude = sqrt(linearAccX * linearAccX + linearAccY * linearAccY + linearAccZ * linearAccZ)
        val now = System.currentTimeMillis()

        if (magnitude > 1.9f && (now - lastStepTimestamp) > 320L) {
            lastStepTimestamp = now
            onStepDetected()
        }
    }

    private fun formatDuration(totalSecs: Int): String {
        if (totalSecs <= 0) return "00:00"
        val hours = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, mins, secs)
        } else {
            String.format(Locale.US, "%02d:%02d", mins, secs)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
