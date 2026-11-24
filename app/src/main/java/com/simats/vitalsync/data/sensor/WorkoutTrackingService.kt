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
    private val activeRoutePoints = mutableListOf<LocationCoordinate>()
    private var currentGpsStatus = GpsSignalStatus.SEARCHING
    private var lastLocationTimestamp: Long = 0L

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
        val defaultPace = if (isCycling) "0.0 km/h" else "--:-- /km"

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
        lastLocationTimestamp = 0L
        synchronized(activeRoutePoints) {
            activeRoutePoints.clear()
        }

        val hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            Log.w("GPS", "Location permissions missing for cycling tracking")
            currentGpsStatus = GpsSignalStatus.NO_PERMISSION
            _telemetry.update { it.copy(gpsSignalStatus = GpsSignalStatus.NO_PERMISSION) }
            return
        }

        val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

        if (!isGpsEnabled) {
            currentGpsStatus = GpsSignalStatus.DISABLED
            _telemetry.update { it.copy(gpsSignalStatus = GpsSignalStatus.DISABLED) }
        } else {
            currentGpsStatus = GpsSignalStatus.SEARCHING
            _telemetry.update { it.copy(gpsSignalStatus = GpsSignalStatus.SEARCHING) }
        }

        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000L // 1-second cadence
            ).apply {
                setMinUpdateIntervalMillis(500L)
                setMinUpdateDistanceMeters(0.5f) // Sensitive to cycling movement
            }.build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation ?: return
                    handleNewLocation(location)
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("GPS", "Location permissions missing", e)
            currentGpsStatus = GpsSignalStatus.NO_PERMISSION
            _telemetry.update { it.copy(gpsSignalStatus = GpsSignalStatus.NO_PERMISSION) }
            startFallbackLocationUpdates()
        } catch (e: Exception) {
            Log.e("GPS", "FusedLocationProviderClient initialization failed, falling back to LocationManager", e)
            startFallbackLocationUpdates()
        }
    }

    private fun handleNewLocation(location: Location) {
        val current = _telemetry.value
        if (!current.isTracking) return

        // 1. Evaluate GPS accuracy & signal status
        val accuracy = if (location.hasAccuracy()) location.accuracy else 50f
        val newStatus = when {
            accuracy <= 15f -> GpsSignalStatus.LOCKED
            accuracy <= 30f -> GpsSignalStatus.FAIR
            else -> GpsSignalStatus.SEARCHING
        }
        currentGpsStatus = newStatus

        // If currently paused, update position marker on map but do not accumulate distance or append to route
        if (current.isPaused) {
            currentSpeedKmh = 0.0f
            _telemetry.update {
                it.copy(
                    currentLatitude = location.latitude,
                    currentLongitude = location.longitude,
                    currentBearing = if (location.hasBearing()) location.bearing else it.currentBearing,
                    gpsAccuracyMeters = accuracy,
                    gpsSignalStatus = newStatus,
                    currentSpeedKmH = 0.0f
                )
            }
            previousLocation = location
            lastLocationTimestamp = location.time
            return
        }

        // 2. Calculate Instantaneous Speed (m/s -> km/h) with fallback to delta distance / delta time
        val prev = previousLocation
        val nowTime = location.time
        val timeDeltaSecs = if (prev != null && lastLocationTimestamp > 0L) {
            ((nowTime - lastLocationTimestamp).coerceAtLeast(100L)) / 1000f
        } else {
            1.0f
        }

        val deltaMeters = prev?.distanceTo(location) ?: 0f

        currentSpeedKmh = if (location.hasSpeed()) {
            val speed = location.speed * 3.6f
            if (speed < 0.8f) 0.0f else speed // Clamp stationary micro-drift
        } else if (prev != null && timeDeltaSecs > 0f) {
            val calculated = (deltaMeters / timeDeltaSecs) * 3.6f
            if (calculated < 0.8f) 0.0f else calculated
        } else {
            0.0f
        }

        // 3. Accumulate Displacement (Distance) & Append to Route with Jitter and Teleport Filter
        var validPointAdded = false
        if (prev != null) {
            val isAccurate = accuracy <= 35f
            val isNotTeleport = (deltaMeters / timeDeltaSecs) < 25f // <= 90 km/h jump threshold
            val isSignificantMove = deltaMeters >= 1.0f // ignore sub-meter noise

            if (isAccurate && isNotTeleport && isSignificantMove) {
                accumulatedDistanceMeters += deltaMeters.toDouble()
                validPointAdded = true
            }
        } else {
            // First fix: add initial starting coordinate
            validPointAdded = true
        }

        if (validPointAdded) {
            val coord = LocationCoordinate(
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = if (location.hasAltitude()) location.altitude else 0.0,
                speedKmh = currentSpeedKmh,
                timestamp = nowTime
            )
            synchronized(activeRoutePoints) {
                activeRoutePoints.add(coord)
            }
        }

        previousLocation = location
        lastLocationTimestamp = nowTime

        // 4. Update reactive state
        updateCyclingTelemetry(location, accuracy, newStatus)
    }

    private fun startFallbackLocationUpdates() {
        try {
            fallbackLocationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    handleNewLocation(location)
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
    private fun updateCyclingTelemetry(
        latestLoc: Location? = null,
        accuracy: Float = 0f,
        gpsStatus: GpsSignalStatus = currentGpsStatus
    ) {
        val current = _telemetry.value
        if (!current.isTracking || !current.activityType.equals("Cycling", ignoreCase = true)) return

        val seconds = current.durationSeconds
        val distKm = if (accumulatedDistanceMeters > 0.0) {
            (accumulatedDistanceMeters / 1000.0).toFloat()
        } else {
            0.0f
        }

        val durationHours = seconds / 3600f
        val avgSpeedKmh = if (durationHours > 0f && distKm > 0.005f) (distKm / durationHours) else 0.0f

        val effectiveCurrentSpeed = if (!current.isPaused && currentSpeedKmh > 0f) currentSpeedKmh else 0.0f

        // Dynamic MET calculation for cycling:
        // Idle/stopped: 1.3 MET, Leisure (<16km/h): 4.5 MET, Moderate (16-20km/h): 6.8 MET, Vigorous (20-25km/h): 8.5 MET, Fast (>25km/h): 10.5 MET
        val met = when {
            effectiveCurrentSpeed <= 0.8f -> 1.3f
            effectiveCurrentSpeed < 16.0f -> 4.5f
            effectiveCurrentSpeed < 20.0f -> 6.8f
            effectiveCurrentSpeed < 25.0f -> 8.5f
            else -> 10.5f
        }

        val caloriesBurned = if (distKm > 0.005f || effectiveCurrentSpeed > 0f || seconds > 60) {
            ((met * 3.5f * activeUserWeightKg / 200f) * (seconds / 60f)).toInt()
        } else {
            0
        }

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

        val lat = latestLoc?.latitude ?: current.currentLatitude
        val lng = latestLoc?.longitude ?: current.currentLongitude
        val bearing = if (latestLoc?.hasBearing() == true) latestLoc.bearing else current.currentBearing

        val routeSnapshot = synchronized(activeRoutePoints) {
            activeRoutePoints.toList()
        }

        _telemetry.update {
            it.copy(
                steps = 0, // Steps deactivated for Cycling
                distanceKm = distKm,
                caloriesBurned = caloriesBurned,
                avgPace = if (avgSpeedKmh > 0.5f) String.format(Locale.US, "%.1f km/h", avgSpeedKmh) else "--.- km/h",
                currentSpeedKmH = effectiveCurrentSpeed,
                avgSpeedKmH = avgSpeedKmh,
                progressRatio = progressRatio,
                progressPercent = progressPct,
                remainingFormatted = remainingFormatted,
                currentLatitude = lat,
                currentLongitude = lng,
                currentBearing = bearing,
                gpsAccuracyMeters = if (accuracy > 0f) accuracy else it.gpsAccuracyMeters,
                gpsSignalStatus = gpsStatus,
                routeCoordinates = routeSnapshot
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

        val distKm = if (rawSteps > 0) {
            if (isRunning) (rawSteps * 1.05f) / 1000.0f else (rawSteps * 0.76f) / 1000.0f
        } else {
            0.0f
        }
        val calBurned = if (rawSteps > 0) {
            val calPerStep = 0.04f * (activeUserWeightKg / 70.0f).coerceIn(0.7f, 1.6f)
            (rawSteps * calPerStep).toInt().coerceAtLeast(1)
        } else {
            0
        }

        val paceStr = if (distKm > 0.005f && seconds > 0) {
            val secPerKm = (seconds / distKm).toInt()
            val m = secPerKm / 60
            val s = secPerKm % 60
            String.format(Locale.US, "%02d:%02d /km", m, s)
        } else {
            "--:-- /km"
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
        currentSpeedKmh = 0.0f
        _telemetry.update { it.copy(isPaused = true, currentSpeedKmH = 0.0f) }
    }

    fun resumeWorkout() {
        previousLocation = null
        lastLocationTimestamp = System.currentTimeMillis()
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

        val routeSnapshot = synchronized(activeRoutePoints) {
            activeRoutePoints.toList()
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
            trackingSource = if (finalState.activityType.equals("Cycling", ignoreCase = true)) "gps" else "phone_sensor",
            routeCoordinates = routeSnapshot
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
        lastLocationTimestamp = 0L
        synchronized(activeRoutePoints) {
            activeRoutePoints.clear()
        }

        _telemetry.update {
            it.copy(
                isTracking = false,
                isPaused = false,
                currentSpeedKmH = 0f,
                routeCoordinates = emptyList()
            )
        }
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
