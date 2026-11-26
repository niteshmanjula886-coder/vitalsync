package com.simats.vitalsync.ui.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.automirrored.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.GpsNotFixed
import androidx.compose.material.icons.rounded.GpsOff
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Color as AndroidColor
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.MotionEvent
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.modules.SqlTileWriter
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay
import com.simats.vitalsync.R
import com.simats.vitalsync.data.ActivitySessionRecord
import com.simats.vitalsync.data.CyclingDayMetric
import com.simats.vitalsync.data.CyclingOverallStats
import com.simats.vitalsync.data.FitnessRepository
import com.simats.vitalsync.data.sensor.GpsSignalStatus
import com.simats.vitalsync.data.sensor.WorkoutTrackingService
import com.simats.vitalsync.data.sensor.WorkoutTelemetry
import com.simats.vitalsync.data.sensor.NominatimPlace
import com.simats.vitalsync.data.sensor.OsmRoutingService
import com.simats.vitalsync.data.sensor.OsmRouteResult
import com.simats.vitalsync.ui.theme.CarbonDarkAccent
import com.simats.vitalsync.ui.theme.CarbonDarkBackground
import com.simats.vitalsync.ui.theme.CarbonDarkBorder
import com.simats.vitalsync.ui.theme.CarbonDarkSurface
import com.simats.vitalsync.ui.theme.CarbonDarkSurfaceVariant
import com.simats.vitalsync.ui.theme.CarbonDarkTextPrimary
import com.simats.vitalsync.ui.theme.CarbonDarkTextSecondary
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalPrimaryMint
import com.simats.vitalsync.data.sensor.isDeviceLocationEnabled
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun CyclingTrackingScreen(
    modifier: Modifier = Modifier,
    targetGoalTab: String = "Distance",
    targetMinutes: Int = 30,
    targetDistanceKm: Float = 10.0f,
    userWeightKg: Float = 70.0f,
    onBackClick: () -> Unit = {},
    onFinishTracking: () -> Unit = {},
    onFinishTrackingWithData: ((durationSeconds: Int, distanceKm: Float, caloriesBurned: Int, avgPace: String, avgHeartRate: Int?, steps: Int) -> Unit)? = null
) {
    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val workoutEngine = remember { WorkoutTrackingService.getInstance(context) }
    val repository = remember { FitnessRepository.getInstance(context) }
    val telemetry by workoutEngine.telemetry.collectAsState()

    // Screen View Tabs: 0 = Live Tracking & Map, 1 = Overall Statistics
    var selectedScreenTab by remember { mutableIntStateOf(0) }

    // Initialize osmdroid configuration once
    remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Purge stale tile cache on startup so old Carto or warning tiles are refreshed
    LaunchedEffect(Unit) {
        try {
            SqlTileWriter().purgeCache()
            val cacheDir = Configuration.getInstance().osmdroidTileCache
            if (cacheDir != null && cacheDir.exists()) {
                cacheDir.listFiles()?.forEach { file ->
                    try { file.deleteRecursively() } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}
    }

    // Follow camera mode state & MapView reference
    var isCameraFollowing by remember { mutableStateOf(true) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Single overlay references to prevent duplicate instances and memory leaks
    val routePolylineRef = remember { mutableStateOf<Polyline?>(null) }
    val targetGuidePolylineRef = remember { mutableStateOf<Polyline?>(null) }
    val startMarkerRef = remember { mutableStateOf<Marker?>(null) }
    val riderMarkerRef = remember { mutableStateOf<Marker?>(null) }
    val targetMarkerRef = remember { mutableStateOf<Marker?>(null) }

    var isMapFullscreen by remember { mutableStateOf(false) }
    var currentMapZoom by remember { mutableDoubleStateOf(17.0) }

    // Compass and Rotation Sensor Listener for Directional Heading Marker
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    var deviceCompassHeading by remember { mutableFloatStateOf(0f) }
    var smoothedMarkerHeading by remember { mutableFloatStateOf(0f) }

    DisposableEffect(sensorManager) {
        if (sensorManager == null) return@DisposableEffect onDispose {}

        val rotSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)

        val listener = object : SensorEventListener {
            private val rotationMatrix = FloatArray(9)
            private val orientationValues = FloatArray(3)

            override fun onSensorChanged(event: SensorEvent) {
                val rawAzimuth = if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationValues)
                    var deg = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
                    if (deg < 0f) deg += 360f
                    deg
                } else {
                    event.values[0]
                }
                deviceCompassHeading = rawAzimuth
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        rotSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Combine GPS bearing (when riding at speed >= 3 km/h) with compass azimuth when slow/stationary
    val effectiveHeading = if (telemetry.currentSpeedKmH >= 3.0f) {
        telemetry.currentBearing
    } else {
        deviceCompassHeading
    }

    // Smooth shortest-angular interpolation to eliminate jitter
    LaunchedEffect(effectiveHeading) {
        var diff = (effectiveHeading - smoothedMarkerHeading) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        if (kotlin.math.abs(diff) > 0.4f) {
            smoothedMarkerHeading = (smoothedMarkerHeading + diff * 0.22f + 360f) % 360f
        }
    }

    // Update rider marker rotation dynamically
    LaunchedEffect(smoothedMarkerHeading) {
        riderMarkerRef.value?.let { riderM ->
            riderM.rotation = smoothedMarkerHeading
            mapViewRef?.invalidate()
        }
    }

    // Destination Search & OSRM Route State (Zero Paid API Keys)
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<NominatimPlace>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val targetRoutePoints = remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    val routeEstimatedDistanceMeters = remember { mutableStateOf<Float?>(null) }
    val routeEstimatedDurationSeconds = remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.length < 2) {
            searchResults = emptyList()
            isSearching = false
            return@LaunchedEffect
        }
        delay(400)
        isSearching = true
        val places = OsmRoutingService.searchPlaces(q)
        searchResults = places
        isSearching = false
    }

    fun selectAndRouteToTarget(targetLat: Double, targetLng: Double) {
        workoutEngine.setTargetDestination(targetLat, targetLng)
        searchQuery = ""
        searchResults = emptyList()

        val curLat = telemetry.currentLatitude ?: 13.0827
        val curLng = telemetry.currentLongitude ?: 80.2707

        coroutineScope.launch {
            val result = OsmRoutingService.fetchCyclingRoute(curLat, curLng, targetLat, targetLng)
            targetRoutePoints.value = result.points
            routeEstimatedDistanceMeters.value = result.distanceMeters
            routeEstimatedDurationSeconds.value = result.durationSeconds

            mapViewRef?.let { mv ->
                val p1 = GeoPoint(curLat, curLng)
                val p2 = GeoPoint(targetLat, targetLng)
                fitMapToPoints(mv, p1, p2)
                mv.invalidate()
            }
        }
    }

    fun clearTarget() {
        workoutEngine.clearTargetDestination()
        targetRoutePoints.value = emptyList()
        routeEstimatedDistanceMeters.value = null
        routeEstimatedDurationSeconds.value = null
        searchQuery = ""
        searchResults = emptyList()
        mapViewRef?.invalidate()
    }

    // High-contrast, sharp dark mode color filter (desaturates colors and inverts with high contrast)
    val darkMapColorFilter = remember {
        val darkMatrix = ColorMatrix().apply {
            setSaturation(0.12f)
        }
        val invertMatrix = ColorMatrix(
            floatArrayOf(
                -0.85f, 0f, 0f, 0f, 235f,
                0f, -0.85f, 0f, 0f, 235f,
                0f, 0f, -0.85f, 0f, 235f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        darkMatrix.postConcat(invertMatrix)
        ColorMatrixColorFilter(darkMatrix)
    }

    // Device Location / GPS state and session feedback banners
    var isGpsEnabled by remember { mutableStateOf(isDeviceLocationEnabled(context)) }
    var showGpsPromptDialog by remember { mutableStateOf(false) }
    var finishBannerMessage by remember { mutableStateOf<String?>(null) }

    // Location Permission Handler
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val isLocationOff = !isGpsEnabled || !hasLocationPermission
    val effectiveGpsStatus = when {
        !hasLocationPermission -> GpsSignalStatus.NO_PERMISSION
        !isGpsEnabled -> GpsSignalStatus.DISABLED
        telemetry.gpsSignalStatus == GpsSignalStatus.DISABLED -> GpsSignalStatus.SEARCHING
        else -> telemetry.gpsSignalStatus
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        val gpsNow = isDeviceLocationEnabled(context)
        isGpsEnabled = gpsNow
        if (granted && gpsNow) {
            workoutEngine.startCyclingTracking()
        }
    }

    // Intercept back navigation: exits fullscreen first if open, else exits tracking screen
    BackHandler(enabled = isMapFullscreen) {
        mapViewRef?.let { currentMapZoom = it.zoomLevelDouble }
        isMapFullscreen = false
    }

    BackHandler(enabled = !isMapFullscreen) {
        if (telemetry.isTracking) {
            workoutEngine.stopWorkout()
        } else {
            workoutEngine.stopLocationUpdates()
        }
        onBackClick()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapViewRef?.onResume()
                    val gpsNow = isDeviceLocationEnabled(context)
                    isGpsEnabled = gpsNow
                    val permNow = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                  ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    hasLocationPermission = permNow
                    if (gpsNow && permNow) {
                        workoutEngine.startCyclingTracking()
                    } else if (!gpsNow && telemetry.isTracking) {
                        workoutEngine.stopLocationUpdates()
                    }
                }
                Lifecycle.Event.ON_PAUSE -> mapViewRef?.onPause()
                Lifecycle.Event.ON_DESTROY -> mapViewRef?.onDetach()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapViewRef?.onPause()
            mapViewRef?.onDetach()
            if (!workoutEngine.telemetry.value.isTracking) {
                workoutEngine.stopLocationUpdates()
            }
        }
    }

    // Statistics weekly breakdown selected metric: 0 = Distance, 1 = Calories, 2 = Duration
    var statsMetricIndex by remember { mutableIntStateOf(0) }
    var overallStats by remember { mutableStateOf(repository.getDetailedCyclingStats()) }

    // Periodic & Event-driven location state check (every 1 second)
    LaunchedEffect(Unit) {
        overallStats = repository.getDetailedCyclingStats()
        val initGps = isDeviceLocationEnabled(context)
        isGpsEnabled = initGps
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else if (initGps) {
            workoutEngine.startCyclingTracking()
        }
        while (isActive) {
            val enabled = isDeviceLocationEnabled(context)
            if (isGpsEnabled != enabled) {
                isGpsEnabled = enabled
                if (!enabled) {
                    workoutEngine.stopLocationUpdates()
                } else if (hasLocationPermission) {
                    workoutEngine.startCyclingTracking()
                }
            }
            delay(1000L)
        }
    }

    // Auto-dismiss confirmation banner after 8 seconds
    LaunchedEffect(finishBannerMessage) {
        if (finishBannerMessage != null) {
            kotlinx.coroutines.delay(8000L)
            finishBannerMessage = null
        }
    }

    // Finish session handler: saves ride, refreshes stats, clears map, and stays on screen
    val finishSession: () -> Unit = {
        val completed = workoutEngine.finishWorkout()
        val dur = completed.durationSeconds
        val dist = completed.distanceKm
        val cal = completed.caloriesBurned
        val pace = completed.avgPace
        val steps = completed.steps
        val sessionUuid = completed.sessionUuid.ifBlank { java.util.UUID.randomUUID().toString() }
        val targetType = targetGoalTab.lowercase()
        val targetVal = if (targetGoalTab == "Distance") targetDistanceKm.toInt() else targetMinutes
        val todayDate = repository.getTodayDateString()

        // 1. Notify callback to record in MainActivity / backend
        onFinishTrackingWithData?.invoke(
            dur,
            dist,
            cal,
            pace,
            138,
            steps
        )

        // 2. Ensure saved in local repository
        if (onFinishTrackingWithData == null) {
            val record = ActivitySessionRecord(
                activityType = "Cycling",
                durationSeconds = dur,
                distanceKm = dist,
                caloriesBurned = cal,
                dateString = todayDate,
                sessionUuid = sessionUuid,
                targetType = targetType,
                targetValue = targetVal,
                steps = steps,
                avgPace = pace,
                trackingSource = "gps"
            )
            repository.saveActivitySessionRecord(record)
        }

        // 3. Immediately refresh overall cycling statistics so the completed ride is visible
        overallStats = repository.getDetailedCyclingStats()

        // 4. Show confirmation banner
        val formattedDist = String.format(Locale.US, "%.2f km", dist)
        val mins = dur / 60
        val secs = dur % 60
        val durStr = if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
        finishBannerMessage = "Ride saved: $formattedDist in $durStr ($cal kcal)"

        // 5. Clear live route overlays on map
        routePolylineRef.value?.setPoints(emptyList())
        startMarkerRef.value?.setVisible(false)
        mapViewRef?.invalidate()
    }

    DisposableEffect(Unit) {
        onDispose {
            if (workoutEngine.telemetry.value.isTracking) {
                workoutEngine.stopWorkout()
            } else {
                workoutEngine.stopLocationUpdates()
            }
        }
    }

    val formattedDynamicTime = telemetry.formattedDuration
    val liveDistanceKm = telemetry.distanceKm
    val liveCalories = telemetry.caloriesBurned
    val liveCurrentSpeedKmH = telemetry.currentSpeedKmH
    val liveAvgSpeedKmH = telemetry.avgSpeedKmH
    val routeGeoPoints = remember(telemetry.routeCoordinates) {
        telemetry.routeCoordinates.map { GeoPoint(it.latitude, it.longitude) }
    }
    val currentGeoPoint = remember(telemetry.currentLatitude, telemetry.currentLongitude) {
        val lat = telemetry.currentLatitude
        val lng = telemetry.currentLongitude
        if (lat != null && lng != null) GeoPoint(lat, lng) else null
    }

    val animatedGoalProgress by animateFloatAsState(
        targetValue = telemetry.progressRatio,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "CyclingGoalProgressAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isMapFullscreen) {
            // Fullscreen Map View (like Google Maps)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDark) Color(0xFF0F1215) else Color(0xFFF8FAFC))
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                OsmMapViewContent(
                    modifier = Modifier.fillMaxSize(),
                    isDark = isDark,
                    isFullscreen = true,
                    currentGeoPoint = currentGeoPoint,
                    telemetry = telemetry,
                    isLocationOff = isLocationOff,
                    effectiveGpsStatus = effectiveGpsStatus,
                    hasLocationPermission = hasLocationPermission,
                    isGpsEnabled = isGpsEnabled,
                    isCameraFollowing = isCameraFollowing,
                    onCameraFollowingChange = { isCameraFollowing = it },
                    routeEstimatedDistanceMeters = routeEstimatedDistanceMeters.value,
                    targetRoutePoints = targetRoutePoints.value,
                    smoothedMarkerHeading = smoothedMarkerHeading,
                    currentMapZoom = currentMapZoom,
                    onZoomChange = { currentMapZoom = it },
                    darkMapColorFilter = darkMapColorFilter,
                    routeGeoPoints = routeGeoPoints,
                    routePolylineRef = routePolylineRef,
                    targetGuidePolylineRef = targetGuidePolylineRef,
                    startMarkerRef = startMarkerRef,
                    riderMarkerRef = riderMarkerRef,
                    targetMarkerRef = targetMarkerRef,
                    onSelectAndRouteToTarget = { lat, lng -> selectAndRouteToTarget(lat, lng) },
                    onClearTarget = { clearTarget() },
                    onExitFullscreen = {
                        mapViewRef?.let { currentMapZoom = it.zoomLevelDouble }
                        isMapFullscreen = false
                    },
                    onPromptLocation = {
                        if (!hasLocationPermission) {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else if (!isGpsEnabled) {
                            try {
                                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            } catch (_: Exception) {}
                        }
                    },
                    onMapViewReady = { mv -> mapViewRef = mv }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (!telemetry.isTracking) {
                                workoutEngine.stopLocationUpdates()
                            }
                            onBackClick()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        BackArrowIcon(
                            modifier = Modifier.size(20.dp),
                            color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Cycling Tracker",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                    )
                }

                // Interactive GPS Signal Status Pill
                GpsStatusBadge(
                    status = effectiveGpsStatus,
                    hasPermission = hasLocationPermission,
                    isDark = isDark,
                    onPromptClick = {
                        if (!hasLocationPermission) {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else if (!isGpsEnabled) {
                            try {
                                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            } catch (_: Exception) {}
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Tabs: Live Tracking & Map vs Overall Statistics
            TabRow(
                selectedTabIndex = selectedScreenTab,
                containerColor = if (isDark) CarbonDarkSurface else Color(0xFFF1F5F9),
                contentColor = if (isDark) Color(0xFF34D399) else VitalTealDark,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedScreenTab]),
                        color = if (isDark) Color(0xFF34D399) else VitalTealDark
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedScreenTab == 0,
                    onClick = { selectedScreenTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Map,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Live Map & Ride",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedScreenTab == 1,
                    onClick = {
                        overallStats = repository.getDetailedCyclingStats()
                        selectedScreenTab = 1
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.BarChart,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Overall Statistics",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // VIEW CONTENT SWITCHER
            if (selectedScreenTab == 0) {
                // ==========================================
                // TAB 0: LIVE TRACKING & INTERACTIVE MAP VIEW
                // ==========================================

                // Confirmation Banner (after session is saved)
                if (finishBannerMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF1B382B) else Color(0xFFE8F6F0)
                        ),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF10B981) else Color(0xFF34D399))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ride Completed & Saved",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = finishBannerMessage ?: "",
                                    fontSize = 12.sp,
                                    color = if (isDark) CarbonDarkTextSecondary else Color(0xFF475569)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            TextButton(
                                onClick = {
                                    overallStats = repository.getDetailedCyclingStats()
                                    selectedScreenTab = 1
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "View Stats",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A)
                                )
                            }
                            IconButton(
                                onClick = { finishBannerMessage = null },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Dismiss",
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isDark) CarbonDarkTextSecondary else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                // Device GPS Off Reminder Card: Only show when device location is truly off or permission missing
                if (isLocationOff) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF3B1D1D) else Color(0xFFFEF2F2)
                        ),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFFEF4444) else Color(0xFFFCA5A5))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF5F2120) else Color(0xFFFEE2E2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.GpsOff,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (!hasLocationPermission) "Location Permission Required" else "Location / GPS is Turned Off",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFFFCA5A5) else Color(0xFF991B1B)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (!hasLocationPermission) {
                                        "Grant location permission to record your route, live speed, and distance."
                                    } else if (telemetry.isTracking) {
                                        "GPS tracking paused. Turn on Location to resume tracking your route and speed."
                                    } else {
                                        "Turn on device location to record your route, live speed, and distance."
                                    },
                                    fontSize = 11.5.sp,
                                    color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF475569),
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = {
                                        if (!hasLocationPermission) {
                                            permissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        } else {
                                            try {
                                                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                                context.startActivity(intent)
                                            } catch (_: Exception) {}
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDark) Color(0xFFDC2626) else Color(0xFFEF4444),
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(
                                        text = if (!hasLocationPermission) "Grant Permission" else "Open Location Settings",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // Destination Search & Target Distance Card (Zero Paid API Keys)
                // -------------------------------------------------------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF2A323D) else Color(0xFFE2E8F0)),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF14191E) else Color(0xFFFFFFFF)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        // Search Bar Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) Color(0xFF1F262E) else Color(0xFFF1F5F9))
                                .padding(horizontal = 10.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search",
                                modifier = Modifier.size(18.dp),
                                tint = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp),
                                textStyle = TextStyle(
                                    fontSize = 12.5.sp,
                                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF1E293B)
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(VitalPrimaryMint),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {
                                    val q = searchQuery.trim()
                                    if (q.length >= 2) {
                                        isSearching = true
                                        coroutineScope.launch {
                                            searchResults = OsmRoutingService.searchPlaces(q)
                                            isSearching = false
                                        }
                                    }
                                }),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search destination (e.g. Central Park, Beach)...",
                                            fontSize = 11.5.sp,
                                            color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(15.dp),
                                    strokeWidth = 2.dp,
                                    color = VitalPrimaryMint
                                )
                            } else if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear Search",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            searchQuery = ""
                                            searchResults = emptyList()
                                        },
                                    tint = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B)
                                )
                            }
                        }

                        // Search Results Dropdown List
                        if (searchResults.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF1B222A) else Color(0xFFF8FAFC)
                                ),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF2E3844) else Color(0xFFE2E8F0))
                            ) {
                                Column {
                                    searchResults.forEachIndexed { index, place ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectAndRouteToTarget(place.latitude, place.longitude)
                                                }
                                                .padding(horizontal = 10.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Place,
                                                contentDescription = null,
                                                modifier = Modifier.size(15.dp),
                                                tint = VitalPrimaryMint
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = place.shortTitle,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF1E293B),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = place.displayName,
                                                    fontSize = 10.sp,
                                                    color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        if (index < searchResults.size - 1) {
                                            HorizontalDivider(
                                                color = if (isDark) Color(0xFF252D37) else Color(0xFFE2E8F0),
                                                thickness = 0.5.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // If target destination is set, show a clean active target chip with clear action
                        if (telemetry.targetLatitude != null && telemetry.targetLongitude != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Place,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFFF97316)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Destination Active",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                        .clickable { clearTarget() }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Clear Target",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive OpenStreetMap (osmdroid) Card with Polyline & Camera Controls
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF2A323D) else Color(0xFFE2E8F0)),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF121619) else Color(0xFFF8FAFC)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    OsmMapViewContent(
                        modifier = Modifier.fillMaxSize(),
                        isDark = isDark,
                        isFullscreen = false,
                        currentGeoPoint = currentGeoPoint,
                        telemetry = telemetry,
                        isLocationOff = isLocationOff,
                        effectiveGpsStatus = effectiveGpsStatus,
                        hasLocationPermission = hasLocationPermission,
                        isGpsEnabled = isGpsEnabled,
                        isCameraFollowing = isCameraFollowing,
                        onCameraFollowingChange = { isCameraFollowing = it },
                        routeEstimatedDistanceMeters = routeEstimatedDistanceMeters.value,
                        targetRoutePoints = targetRoutePoints.value,
                        smoothedMarkerHeading = smoothedMarkerHeading,
                        currentMapZoom = currentMapZoom,
                        onZoomChange = { currentMapZoom = it },
                        darkMapColorFilter = darkMapColorFilter,
                        routeGeoPoints = routeGeoPoints,
                        routePolylineRef = routePolylineRef,
                        targetGuidePolylineRef = targetGuidePolylineRef,
                        startMarkerRef = startMarkerRef,
                        riderMarkerRef = riderMarkerRef,
                        targetMarkerRef = targetMarkerRef,
                        onSelectAndRouteToTarget = { lat, lng -> selectAndRouteToTarget(lat, lng) },
                        onClearTarget = { clearTarget() },
                        onExitFullscreen = null,
                        onPromptLocation = {
                            if (!hasLocationPermission) {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            } else if (!isGpsEnabled) {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                } catch (_: Exception) {}
                            }
                        },
                        onMapViewReady = { mv -> mapViewRef = mv }
                    )
                }

                // Fullscreen Expand Button below map card aligned cleanly with right edge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDark) Color(0xFF192026) else Color(0xFFF1F5F9))
                            .border(1.dp, if (isDark) Color(0xFF2A323D) else Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                            .clickable {
                                mapViewRef?.let { currentMapZoom = it.zoomLevelDouble }
                                isMapFullscreen = true
                            }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            FullscreenIcon(
                                modifier = Modifier.size(14.dp),
                                color = VitalPrimaryMint
                            )
                            Text(
                                text = "Fullscreen Map",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF1E293B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Live Timer & Target Banner
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.DirectionsBike,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GPS Outdoor Cycle",
                            fontSize = 12.sp,
                            color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isDark) Color(0xFF1B382B) else Color(0xFFE8F6F0))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (targetGoalTab == "Distance") "Target: ${String.format(Locale.US, "%.1f", targetDistanceKm)} km" else "Target: $targetMinutes min",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formattedDynamicTime,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                    )

                    Text(
                        text = "Elapsed Time",
                        fontSize = 11.sp,
                        color = if (isDark) CarbonDarkTextSecondary else Color(0xFF9CA3AF)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pause / Resume Toggle or Ready Status
                    if (telemetry.isTracking) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (telemetry.isPaused) {
                                        if (isDark) Color(0xFF3B2D14) else Color(0xFFFEF3C7)
                                    } else {
                                        if (isDark) Color(0xFF1B382B) else Color(0xFFE8F6F0)
                                    }
                                )
                                .clickable {
                                    if (telemetry.isPaused) workoutEngine.resumeWorkout() else workoutEngine.pauseWorkout()
                                }
                                .padding(horizontal = 14.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (telemetry.isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = if (telemetry.isPaused) {
                                        if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
                                    } else {
                                        if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A)
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (telemetry.isPaused) "Resume Ride" else "Pause Ride",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (telemetry.isPaused) {
                                        if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
                                    } else {
                                        if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A)
                                    }
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isDark) Color(0xFF1E2630) else Color(0xFFF1F5F9))
                                .padding(horizontal = 14.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "Ready to Ride · Tap Start below",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Metrics Card (2x2 Grid with Zero Clipping)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) CarbonDarkSurface else Color(0xFFF8FAFC)
                    ),
                    border = BorderStroke(1.dp, if (isDark) CarbonDarkBorder else Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Live Distance", fontSize = 11.5.sp, color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = String.format(Locale.US, "%.2f km", liveDistanceKm),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Current Speed", fontSize = 11.5.sp, color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = String.format(Locale.US, "%.1f km/h", if (!isLocationOff) liveCurrentSpeedKmH else 0.0f),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Average Speed", fontSize = 11.5.sp, color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = String.format(Locale.US, "%.1f km/h", liveAvgSpeedKmH),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Active Calories", fontSize = 11.5.sp, color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$liveCalories kcal",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Live Target Tracking Progress Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (targetGoalTab == "Distance") "Live Distance Progress" else "Live Time Progress",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                            )
                            Text(
                                text = "${(animatedGoalProgress * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFE5E7EB))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedGoalProgress)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isDark) CarbonDarkAccent else Color(0xFF0F9F7A))
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (targetGoalTab == "Distance") {
                                val remaining = (targetDistanceKm - liveDistanceKm).coerceAtLeast(0f)
                                "${String.format(Locale.US, "%.2f", liveDistanceKm)} of ${String.format(Locale.US, "%.1f", targetDistanceKm)} km · ${String.format(Locale.US, "%.2f", remaining)} km remaining"
                            } else {
                                "$formattedDynamicTime of ${targetMinutes}m · ${telemetry.remainingFormatted}"
                            },
                            fontSize = 12.sp,
                            color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Workout Result Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1B382B) else Color(0xFFE8F6F0)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Live Ride Performance",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isLocationOff) (if (isDark) Color(0xFFEF4444) else Color(0xFFDC2626))
                                        else if (isDark) CarbonDarkAccent else Color(0xFF0F9F7A)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (!isGpsEnabled) "Location Off"
                                           else if (!hasLocationPermission) "Permission Required"
                                           else if (!telemetry.isTracking) "Ready"
                                           else if (!telemetry.isPaused) "GPS Active"
                                           else "Ride Paused",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "RIDE TIME",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A),
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formattedDynamicTime,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                                )
                            }

                            Column {
                                Text(
                                    text = "ENERGY BURN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A),
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$liveCalories kcal",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "AVG SPEED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A),
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = String.format(Locale.US, "%.1f km/h", liveAvgSpeedKmH),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                                )
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // TAB 1: OVERALL CYCLING STATISTICS VIEW
                // ==========================================
                CyclingStatisticsView(
                    stats = overallStats,
                    selectedMetricIndex = statsMetricIndex,
                    onMetricIndexChange = { statsMetricIndex = it },
                    isDark = isDark,
                    onRefresh = {
                        overallStats = repository.getDetailedCyclingStats()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Primary Action Controls at Bottom
        if (selectedScreenTab == 0) {
            if (!telemetry.isTracking) {
                // IDLE STATE: Explicit Start Ride Button
                Button(
                    onClick = {
                        val gpsNow = isDeviceLocationEnabled(context)
                        isGpsEnabled = gpsNow
                        if (!gpsNow) {
                            showGpsPromptDialog = true
                        } else if (!hasLocationPermission) {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else {
                            finishBannerMessage = null
                            workoutEngine.startWorkout(
                                activityType = "Cycling",
                                targetType = targetGoalTab,
                                targetMinutes = targetMinutes,
                                targetDistanceKm = targetDistanceKm,
                                userWeightKg = userWeightKg
                            )
                            isCameraFollowing = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 12.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) CarbonDarkAccent else VitalTealDark,
                        contentColor = Color.White
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start Ride",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // ACTIVE STATE: Explicit Pause/Resume and Finish Session Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pause / Resume Button
                    Button(
                        onClick = {
                            if (telemetry.isPaused) workoutEngine.resumeWorkout() else workoutEngine.pauseWorkout()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (telemetry.isPaused) {
                                if (isDark) Color(0xFFD97706) else Color(0xFFF59E0B)
                            } else {
                                if (isDark) Color(0xFF263238) else Color(0xFFE2E8F0)
                            },
                            contentColor = if (telemetry.isPaused) Color.White else (if (isDark) CarbonDarkTextPrimary else Color(0xFF1E293B))
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (telemetry.isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (telemetry.isPaused) "Resume" else "Pause",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Finish / Stop Cycling Session Button
                    Button(
                        onClick = {
                            finishSession()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) CarbonDarkAccent else VitalTealDark,
                            contentColor = Color.White
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Finish Session",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            Button(
                onClick = { selectedScreenTab = 0 },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) CarbonDarkAccent else VitalTealDark,
                    contentColor = Color.White
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.DirectionsBike,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Return to Live Ride",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
    }

    // GPS Turn-On Prompt Alert Dialog
    if (showGpsPromptDialog) {
        AlertDialog(
            onDismissRequest = { showGpsPromptDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.GpsOff,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626),
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Turn On Location / GPS",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                )
            },
            text = {
                Text(
                    text = "Live cycling tracking requires device GPS to record your route, calculate live speed, and track distance accurately. Please turn on Location in system settings.",
                    fontSize = 13.sp,
                    color = if (isDark) CarbonDarkTextSecondary else Color(0xFF4B5563),
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGpsPromptDialog = false
                        try {
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) CarbonDarkAccent else VitalTealDark,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Open Settings", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showGpsPromptDialog = false }
                ) {
                    Text(
                        text = "Cancel",
                        color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280),
                        fontSize = 13.sp
                    )
                }
            },
            containerColor = if (isDark) CarbonDarkSurface else Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }
    }
}

@Composable
private fun GpsStatusBadge(
    status: GpsSignalStatus,
    hasPermission: Boolean,
    isDark: Boolean,
    onPromptClick: () -> Unit
) {
    val (label, icon, dotColor, bgColor) = when {
        !hasPermission -> Quadruple(
            "Permission Required",
            Icons.Rounded.GpsOff,
            Color(0xFFEF4444),
            if (isDark) Color(0xFF3B1D1D) else Color(0xFFFEE2E2)
        )
        status == GpsSignalStatus.DISABLED -> Quadruple(
            "GPS Disabled",
            Icons.Rounded.GpsOff,
            Color(0xFFEF4444),
            if (isDark) Color(0xFF3B1D1D) else Color(0xFFFEE2E2)
        )
        status == GpsSignalStatus.SEARCHING -> Quadruple(
            "Acquiring GPS...",
            Icons.Rounded.GpsNotFixed,
            Color(0xFFF59E0B),
            if (isDark) Color(0xFF382D14) else Color(0xFFFEF3C7)
        )
        status == GpsSignalStatus.FAIR -> Quadruple(
            "GPS Lock (Fair)",
            Icons.Rounded.GpsFixed,
            Color(0xFF10B981),
            if (isDark) Color(0xFF1B382B) else Color(0xFFE6F4EA)
        )
        else -> Quadruple(
            "GPS Locked",
            Icons.Rounded.GpsFixed,
            Color(0xFF10B981),
            if (isDark) Color(0xFF1B382B) else Color(0xFFE6F4EA)
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(onClick = onPromptClick)
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = dotColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = dotColor
            )
        }
    }
}

@Composable
private fun GpsAcquiringMapOverlay(isDark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF13171B) else Color(0xFFF8FAFC)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF1E2830) else Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (isDark) Color(0xFF34D399) else VitalTealDark
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Connecting to Satellite GPS",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Streaming live position & drawing route polyline as you ride...",
                fontSize = 12.sp,
                color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CyclingStatisticsView(
    stats: CyclingOverallStats,
    selectedMetricIndex: Int,
    onMetricIndexChange: (Int) -> Unit,
    isDark: Boolean,
    onRefresh: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Overall Cycling Analytics",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
            )

            IconButton(onClick = onRefresh, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Refresh Analytics",
                    modifier = Modifier.size(16.dp),
                    tint = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2x2 Aggregated Metric Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricStatCard(
                title = "Total Distance",
                value = String.format(Locale.US, "%.1f km", stats.totalDistanceKm),
                subtitle = "${stats.totalRides} rides completed",
                icon = Icons.AutoMirrored.Rounded.DirectionsBike,
                modifier = Modifier.weight(1f),
                isDark = isDark
            )

            MetricStatCard(
                title = "Total Calories",
                value = "${stats.totalCalories} kcal",
                subtitle = "Active energy burn",
                icon = Icons.Rounded.Timeline,
                modifier = Modifier.weight(1f),
                isDark = isDark
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricStatCard(
                title = "Total Ride Time",
                value = stats.totalDurationFormatted,
                subtitle = "Active moving time",
                icon = Icons.Rounded.Timer,
                modifier = Modifier.weight(1f),
                isDark = isDark
            )

            MetricStatCard(
                title = "Average Speed",
                value = if (stats.avgSpeedKmh > 0.1f) String.format(Locale.US, "%.1f km/h", stats.avgSpeedKmh) else "--.- km/h",
                subtitle = "${String.format(Locale.US, "%.1f km", stats.avgDistanceKm)} avg / ride",
                icon = Icons.Rounded.Speed,
                modifier = Modifier.weight(1f),
                isDark = isDark
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Personal Best Session Highlight
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF1B382B) else Color(0xFFE8F6F0)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "PERSONAL BEST RECORD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A),
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${String.format(Locale.US, "%.1f km", stats.bestDistanceKm)} • ${stats.bestDurationFormatted}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0xFF10B981) else Color(0xFF0F9F7A))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (stats.bestSpeedKmh > 0.1f) "${String.format(Locale.US, "%.1f", stats.bestSpeedKmh)} km/h max" else "New Record",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Weekly Breakdown Section with Metric Switcher Chips
        Text(
            text = "Weekly Activity Trends",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Metric Filter Chips: Distance, Calories, Duration
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Distance (km)", "Calories (kcal)", "Duration (min)").forEachIndexed { index, title ->
                val isSelected = (selectedMetricIndex == index)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) {
                                if (isDark) Color(0xFF10B981) else Color(0xFF0F9F7A)
                            } else {
                                if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF1F5F9)
                            }
                        )
                        .clickable { onMetricIndexChange(index) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else (if (isDark) CarbonDarkTextSecondary else Color(0xFF475569))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bar Chart Component across Mon-Sun
        WeeklyCyclingBarChart(
            breakdown = stats.weeklyBreakdown,
            metricIndex = selectedMetricIndex,
            isDark = isDark
        )
    }
}

@Composable
private fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) CarbonDarkSurface else Color(0xFFF8FAFC)
        ),
        border = BorderStroke(1.dp, if (isDark) CarbonDarkBorder else Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (isDark) Color(0xFF34D399) else VitalTealDark
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = if (isDark) CarbonDarkTextSecondary else Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun WeeklyCyclingBarChart(
    breakdown: List<CyclingDayMetric>,
    metricIndex: Int,
    isDark: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) CarbonDarkSurface else Color(0xFFF8FAFC)
        ),
        border = BorderStroke(1.dp, if (isDark) CarbonDarkBorder else Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val maxVal = remember(breakdown, metricIndex) {
                val vals = breakdown.map {
                    when (metricIndex) {
                        0 -> it.distanceKm
                        1 -> it.calories.toFloat()
                        else -> it.durationMinutes.toFloat()
                    }
                }
                (vals.maxOrNull() ?: 10f).coerceAtLeast(1f)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                breakdown.forEach { item ->
                    val rawVal = when (metricIndex) {
                        0 -> item.distanceKm
                        1 -> item.calories.toFloat()
                        else -> item.durationMinutes.toFloat()
                    }
                    val heightRatio = (rawVal / maxVal).coerceIn(0.08f, 1.0f)
                    val activeColor = if (item.isToday) {
                        if (isDark) Color(0xFF10B981) else Color(0xFF0F9F7A)
                    } else {
                        if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (rawVal > 0f) {
                            Text(
                                text = when (metricIndex) {
                                    0 -> String.format(Locale.US, "%.1f", rawVal)
                                    1 -> "${rawVal.toInt()}"
                                    else -> "${rawVal.toInt()}m"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isToday) (if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A)) else (if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height((80 * heightRatio).dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(activeColor)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.dayLabel,
                            fontSize = 11.sp,
                            fontWeight = if (item.isToday) FontWeight.Bold else FontWeight.Medium,
                            color = if (item.isToday) (if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A)) else (if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B))
                        )
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)


@Composable
private fun BackArrowIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Black
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.75f, h * 0.5f)
            lineTo(w * 0.25f, h * 0.5f)
            moveTo(w * 0.45f, h * 0.25f)
            lineTo(w * 0.25f, h * 0.5f)
            lineTo(w * 0.45f, h * 0.75f)
        }

        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

private fun fitMapToPoints(mapView: MapView?, p1: GeoPoint, p2: GeoPoint) {
    if (mapView == null) return
    try {
        val minLat = minOf(p1.latitude, p2.latitude)
        val maxLat = maxOf(p1.latitude, p2.latitude)
        val minLng = minOf(p1.longitude, p2.longitude)
        val maxLng = maxOf(p1.longitude, p2.longitude)
        val deltaLat = (maxLat - minLat).coerceAtLeast(0.005)
        val deltaLng = (maxLng - minLng).coerceAtLeast(0.005)
        val box = org.osmdroid.util.BoundingBox(
            maxLat + deltaLat * 0.25,
            maxLng + deltaLng * 0.25,
            minLat - deltaLat * 0.25,
            minLng - deltaLng * 0.25
        )
        mapView.zoomToBoundingBox(box, true, 80)
    } catch (_: Exception) {
        mapView.controller.animateTo(p2)
    }
}

private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val phi1 = Math.toRadians(lat1)
    val phi2 = Math.toRadians(lat2)
    val deltaLambda = Math.toRadians(lon2 - lon1)
    val y = Math.sin(deltaLambda) * Math.cos(phi2)
    val x = Math.cos(phi1) * Math.sin(phi2) - Math.sin(phi1) * Math.cos(phi2) * Math.cos(deltaLambda)
    val theta = Math.atan2(y, x)
    return (Math.toDegrees(theta) + 360.0) % 360.0
}

@Composable
private fun OsmMapViewContent(
    modifier: Modifier = Modifier,
    isDark: Boolean,
    isFullscreen: Boolean,
    currentGeoPoint: GeoPoint?,
    telemetry: WorkoutTelemetry,
    isLocationOff: Boolean,
    effectiveGpsStatus: GpsSignalStatus,
    hasLocationPermission: Boolean,
    isGpsEnabled: Boolean,
    isCameraFollowing: Boolean,
    onCameraFollowingChange: (Boolean) -> Unit,
    routeEstimatedDistanceMeters: Float?,
    targetRoutePoints: List<GeoPoint>,
    smoothedMarkerHeading: Float,
    currentMapZoom: Double,
    onZoomChange: (Double) -> Unit,
    darkMapColorFilter: ColorMatrixColorFilter,
    routeGeoPoints: List<GeoPoint>,
    routePolylineRef: androidx.compose.runtime.MutableState<Polyline?>,
    targetGuidePolylineRef: androidx.compose.runtime.MutableState<Polyline?>,
    startMarkerRef: androidx.compose.runtime.MutableState<Marker?>,
    riderMarkerRef: androidx.compose.runtime.MutableState<Marker?>,
    targetMarkerRef: androidx.compose.runtime.MutableState<Marker?>,
    onSelectAndRouteToTarget: (Double, Double) -> Unit,
    onClearTarget: () -> Unit,
    onExitFullscreen: (() -> Unit)? = null,
    onPromptLocation: () -> Unit,
    onMapViewReady: (MapView) -> Unit
) {
    var localMapView by remember { mutableStateOf<MapView?>(null) }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    localMapView = this
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    setTilesScaledToDpi(true)
                    setUseDataConnection(true)
                    setFlingEnabled(true)
                    minZoomLevel = 12.0
                    maxZoomLevel = 20.0
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    controller.setZoom(currentMapZoom)
                    val initialCenter = currentGeoPoint ?: GeoPoint(13.0827, 80.2707)
                    controller.setCenter(initialCenter)

                    setOnTouchListener { v, event ->
                        when (event.actionMasked) {
                            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                                v.parent?.requestDisallowInterceptTouchEvent(true)
                                onCameraFollowingChange(false)
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                v.parent?.requestDisallowInterceptTouchEvent(false)
                            }
                        }
                        false
                    }

                    val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            if (p == null) return false
                            val hasTarget = telemetry.targetLatitude != null && telemetry.targetLongitude != null
                            if (hasTarget) {
                                onClearTarget()
                            } else {
                                onSelectAndRouteToTarget(p.latitude, p.longitude)
                            }
                            postInvalidate()
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint?): Boolean {
                            if (p != null) {
                                onSelectAndRouteToTarget(p.latitude, p.longitude)
                                postInvalidate()
                                return true
                            }
                            return false
                        }
                    })

                    val guidePolyline = Polyline(this).apply {
                        outlinePaint.color = AndroidColor.parseColor("#F97316")
                        outlinePaint.strokeWidth = 7f
                        outlinePaint.strokeCap = Paint.Cap.ROUND
                        outlinePaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(24f, 16f), 0f)
                        isVisible = false
                    }
                    val polyline = Polyline(this).apply {
                        outlinePaint.color = AndroidColor.parseColor("#10B981")
                        outlinePaint.strokeWidth = 10f
                        outlinePaint.strokeCap = Paint.Cap.ROUND
                        outlinePaint.strokeJoin = Paint.Join.ROUND
                    }
                    val startM = Marker(this).apply {
                        icon = ContextCompat.getDrawable(ctx, R.drawable.ic_ride_start_marker)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        setOnMarkerClickListener { _, _ -> true }
                    }
                    val targetM = Marker(this).apply {
                        icon = ContextCompat.getDrawable(ctx, R.drawable.ic_target_destination_marker)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { _, mv ->
                            onClearTarget()
                            mv.invalidate()
                            true
                        }
                    }
                    val riderM = Marker(this).apply {
                        icon = ContextCompat.getDrawable(ctx, R.drawable.ic_rider_marker)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        rotation = smoothedMarkerHeading
                        setOnMarkerClickListener { _, _ -> true }
                    }

                    overlays.add(0, eventsOverlay)
                    overlays.add(guidePolyline)
                    overlays.add(polyline)
                    overlays.add(startM)
                    overlays.add(targetM)
                    overlays.add(riderM)

                    targetGuidePolylineRef.value = guidePolyline
                    routePolylineRef.value = polyline
                    startMarkerRef.value = startM
                    targetMarkerRef.value = targetM
                    riderMarkerRef.value = riderM

                    onMapViewReady(this)
                }
            },
            update = { mapView ->
                mapView.overlayManager.tilesOverlay.setColorFilter(
                    if (isDark) darkMapColorFilter else null
                )

                val targetLat = telemetry.targetLatitude
                val targetLng = telemetry.targetLongitude
                val targetGeoPoint = if (targetLat != null && targetLng != null) GeoPoint(targetLat, targetLng) else null

                targetGuidePolylineRef.value?.let { guide ->
                    if (targetGeoPoint != null && currentGeoPoint != null) {
                        val pts = if (targetRoutePoints.isNotEmpty()) {
                            targetRoutePoints
                        } else {
                            listOf(currentGeoPoint, targetGeoPoint)
                        }
                        guide.setPoints(pts)
                        guide.isVisible = true
                    } else {
                        guide.isVisible = false
                    }
                }

                targetMarkerRef.value?.let { targetM ->
                    if (targetGeoPoint != null) {
                        targetM.position = targetGeoPoint
                        targetM.icon = ContextCompat.getDrawable(
                            mapView.context,
                            R.drawable.ic_target_destination_marker
                        )
                        targetM.setVisible(true)
                        targetM.closeInfoWindow()
                    } else {
                        targetM.setVisible(false)
                    }
                }

                routePolylineRef.value?.let { polyline ->
                    if (routeGeoPoints.size > 1) {
                        polyline.setPoints(routeGeoPoints)
                        polyline.setVisible(true)
                    } else {
                        polyline.setVisible(false)
                    }
                }

                startMarkerRef.value?.let { startM ->
                    if (routeGeoPoints.isNotEmpty()) {
                        startM.position = routeGeoPoints.first()
                        startM.setVisible(true)
                        startM.closeInfoWindow()
                    } else {
                        startM.setVisible(false)
                    }
                }

                riderMarkerRef.value?.let { riderM ->
                    if (currentGeoPoint != null) {
                        riderM.position = currentGeoPoint
                        riderM.rotation = smoothedMarkerHeading
                        riderM.setVisible(true)
                        riderM.closeInfoWindow()

                        if (isCameraFollowing) {
                            mapView.controller.animateTo(currentGeoPoint)
                        }
                    } else {
                        riderM.setVisible(false)
                    }
                }

                mapView.invalidate()
            }
        )

        // Radar acquiring overlay if GPS not acquired yet
        if (currentGeoPoint == null) {
            GpsAcquiringMapOverlay(isDark = isDark)
        }

        // Top-Left: In Fullscreen, show Exit Fullscreen button alongside Speed HUD
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isFullscreen && onExitFullscreen != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) Color(0xDD121619) else Color(0xEEFFFFFF))
                        .border(1.dp, if (isDark) Color(0xFF2A323D) else Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                        .clickable { onExitFullscreen() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BackArrowIcon(
                            modifier = Modifier.size(16.dp),
                            color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                        )
                        Text(
                            text = "Exit",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                        )
                    }
                }
            }

            // Live Speed HUD Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDark) Color(0xDD121619) else Color(0xEEFFFFFF))
                    .border(1.dp, if (isDark) Color(0xFF10B981) else Color(0xFF0F9F7A), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f km/h", if (!isLocationOff) telemetry.currentSpeedKmH else 0.0f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                    )
                }
            }
        }

        // Top-Right: Floating GPS Status Badge
        val gpsStatusIsLocked = !isLocationOff &&
            (telemetry.gpsSignalStatus == GpsSignalStatus.LOCKED || telemetry.gpsSignalStatus == GpsSignalStatus.FAIR)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isDark) Color(0xDD121619) else Color(0xEEFFFFFF))
                .border(
                    1.dp,
                    if (isLocationOff) (if (isDark) Color(0xFFEF4444) else Color(0xFFDC2626))
                    else if (gpsStatusIsLocked) (if (isDark) Color(0xFF10B981) else Color(0xFF0F9F7A))
                    else (if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706)),
                    RoundedCornerShape(10.dp)
                )
                .clickable { onPromptLocation() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isLocationOff) Icons.Rounded.GpsOff
                    else if (gpsStatusIsLocked) Icons.Rounded.GpsFixed
                    else Icons.Rounded.GpsNotFixed,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = if (isLocationOff) (if (isDark) Color(0xFFF87171) else Color(0xFFDC2626))
                    else if (gpsStatusIsLocked) (if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A))
                    else (if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706))
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (!isGpsEnabled) "Location Off"
                    else if (!hasLocationPermission) "No Permission"
                    else if (gpsStatusIsLocked) "GPS Locked"
                    else "Acquiring...",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                )
            }
        }

        // Bottom-Left: Compact Est. Distance badge ONLY (no Est. Time, sleek and compact)
        if (telemetry.targetLatitude != null && telemetry.targetLongitude != null) {
            val curDistM = telemetry.distanceToTargetMeters ?: routeEstimatedDistanceMeters
            val distText = if (curDistM != null && curDistM > 0f) {
                if (curDistM < 1000f) "${curDistM.roundToInt()} m"
                else String.format(Locale.US, "%.2f km", curDistM / 1000f)
            } else {
                "--"
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) Color(0xEE121619) else Color(0xF5FFFFFF))
                    .border(
                        1.dp,
                        if (isDark) Color(0xFF2A323D) else Color(0xFFCBD5E1),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = VitalPrimaryMint
                    )
                    Text(
                        text = "Est. Distance: $distText",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                    )
                }
            }
        }

        // Bottom-Right: Zoom In/Out Controls (+ / -) stacked vertically above Recenter FAB
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 62.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Zoom In (+)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xDD121619) else Color(0xEEFFFFFF))
                    .border(1.dp, if (isDark) Color(0xFF2A323D) else Color(0xFFCBD5E1), CircleShape)
                    .clickable {
                        localMapView?.let { mv ->
                            mv.controller.zoomIn()
                            onZoomChange(mv.zoomLevelDouble)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                ZoomInIcon(
                    modifier = Modifier.size(16.dp),
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                )
            }

            // Zoom Out (-)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                .background(if (isDark) Color(0xDD121619) else Color(0xEEFFFFFF))
                .border(1.dp, if (isDark) Color(0xFF2A323D) else Color(0xFFCBD5E1), CircleShape)
                .clickable {
                    localMapView?.let { mv ->
                        mv.controller.zoomOut()
                        onZoomChange(mv.zoomLevelDouble)
                    }
                },
                contentAlignment = Alignment.Center
            ) {
                ZoomOutIcon(
                    modifier = Modifier.size(16.dp),
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                )
            }
        }

        // Floating Re-Center / Follow Me FAB on Bottom-Right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isCameraFollowing) (if (isDark) Color(0xFF10B981) else Color(0xFF0F9F7A)) else (if (isDark) Color(0xDD121619) else Color(0xEEFFFFFF)))
                .clickable {
                    onCameraFollowingChange(true)
                    currentGeoPoint?.let { pt ->
                        localMapView?.controller?.apply {
                            setZoom(17.0)
                            animateTo(pt)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.MyLocation,
                contentDescription = "Re-center Camera",
                modifier = Modifier.size(20.dp),
                tint = if (isCameraFollowing) Color.White else (if (isDark) CarbonDarkTextPrimary else Color(0xFF1F2937))
            )
        }
    }
}

@Composable
private fun FullscreenIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val corner = w * 0.3f
        val stroke = 2.dp.toPx()
        val path = Path().apply {
            // Top-Left
            moveTo(0f, corner); lineTo(0f, 0f); lineTo(corner, 0f)
            // Top-Right
            moveTo(w - corner, 0f); lineTo(w, 0f); lineTo(w, corner)
            // Bottom-Right
            moveTo(w, h - corner); lineTo(w, h); lineTo(w - corner, h)
            // Bottom-Left
            moveTo(corner, h); lineTo(0f, h); lineTo(0f, h - corner)
        }
        drawPath(path = path, color = color, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun FullscreenExitIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val corner = w * 0.3f
        val stroke = 2.dp.toPx()
        val path = Path().apply {
            // Top-Left (pointing inwards)
            moveTo(0f, corner); lineTo(corner, corner); lineTo(corner, 0f)
            // Top-Right
            moveTo(w, corner); lineTo(w - corner, corner); lineTo(w - corner, 0f)
            // Bottom-Right
            moveTo(w, h - corner); lineTo(w - corner, h - corner); lineTo(w - corner, h)
            // Bottom-Left
            moveTo(0f, h - corner); lineTo(corner, h - corner); lineTo(corner, h)
        }
        drawPath(path = path, color = color, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun ZoomInIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 2.2.dp.toPx()
        drawLine(color = color, start = Offset(w * 0.2f, h * 0.5f), end = Offset(w * 0.8f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.5f, h * 0.2f), end = Offset(w * 0.5f, h * 0.8f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
private fun ZoomOutIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 2.2.dp.toPx()
        drawLine(color = color, start = Offset(w * 0.2f, h * 0.5f), end = Offset(w * 0.8f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}
