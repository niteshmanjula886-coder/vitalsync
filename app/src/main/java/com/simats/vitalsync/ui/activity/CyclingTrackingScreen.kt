package com.simats.vitalsync.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import android.content.Context
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
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay
import com.simats.vitalsync.R
import com.simats.vitalsync.data.CyclingDayMetric
import com.simats.vitalsync.data.CyclingOverallStats
import com.simats.vitalsync.data.FitnessRepository
import com.simats.vitalsync.data.sensor.GpsSignalStatus
import com.simats.vitalsync.data.sensor.WorkoutTrackingService
import com.simats.vitalsync.ui.theme.CarbonDarkAccent
import com.simats.vitalsync.ui.theme.CarbonDarkBackground
import com.simats.vitalsync.ui.theme.CarbonDarkBorder
import com.simats.vitalsync.ui.theme.CarbonDarkSurface
import com.simats.vitalsync.ui.theme.CarbonDarkSurfaceVariant
import com.simats.vitalsync.ui.theme.CarbonDarkTextPrimary
import com.simats.vitalsync.ui.theme.CarbonDarkTextSecondary
import com.simats.vitalsync.ui.theme.VitalTealDark
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

    // Follow camera mode state & MapView reference
    var isCameraFollowing by remember { mutableStateOf(true) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Single overlay references to prevent duplicate instances and memory leaks
    val routePolylineRef = remember { mutableStateOf<Polyline?>(null) }
    val startMarkerRef = remember { mutableStateOf<Marker?>(null) }
    val riderMarkerRef = remember { mutableStateOf<Marker?>(null) }

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

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewRef?.onResume()
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
        }
    }

    // Statistics weekly breakdown selected metric: 0 = Distance, 1 = Calories, 2 = Duration
    var statsMetricIndex by remember { mutableIntStateOf(0) }
    var overallStats by remember { mutableStateOf(repository.getDetailedCyclingStats()) }

    // Location Permission Handler
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) {
            workoutEngine.startCyclingTracking()
        }
    }

    // Start workout session
    LaunchedEffect(targetGoalTab, targetMinutes, targetDistanceKm, userWeightKg) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        workoutEngine.startWorkout(
            activityType = "Cycling",
            targetType = targetGoalTab,
            targetMinutes = targetMinutes,
            targetDistanceKm = targetDistanceKm,
            userWeightKg = userWeightKg
        )
        overallStats = repository.getDetailedCyclingStats()
    }

    DisposableEffect(Unit) {
        onDispose {
            workoutEngine.stopWorkout()
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

    val animatedGoalProgress by animateFloatAsState(
        targetValue = telemetry.progressRatio,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "CyclingGoalProgressAnimation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                        onClick = onBackClick,
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
                    status = telemetry.gpsSignalStatus,
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
                        } else if (telemetry.gpsSignalStatus == GpsSignalStatus.DISABLED) {
                            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
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

                // Interactive OpenStreetMap (osmdroid) Card with Polyline & Camera Controls
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(245.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isDark) CarbonDarkBorder else Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val lat = telemetry.currentLatitude
                        val lng = telemetry.currentLongitude
                        val currentGeoPoint = if (lat != null && lng != null) GeoPoint(lat, lng) else null

                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                MapView(ctx).apply {
                                    setTileSource(TileSourceFactory.MAPNIK)
                                    setMultiTouchControls(true)
                                    isTilesScaledToDpi = true
                                    setUseDataConnection(true)
                                    minZoomLevel = 12.0
                                    maxZoomLevel = 20.0
                                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                                    controller.setZoom(17.5)
                                    val initialCenter = currentGeoPoint ?: GeoPoint(13.0827, 80.2707)
                                    controller.setCenter(initialCenter)

                                    // Touch handling: Disallow parent scroll only when user touches map directly
                                    setOnTouchListener { v, event ->
                                        when (event.actionMasked) {
                                            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                                                v.parent?.requestDisallowInterceptTouchEvent(true)
                                                isCameraFollowing = false
                                            }
                                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                                v.parent?.requestDisallowInterceptTouchEvent(false)
                                            }
                                        }
                                        false
                                    }

                                    // Add single overlay instances ONCE during setup:
                                    val polyline = Polyline(this).apply {
                                        outlinePaint.color = AndroidColor.parseColor("#10B981")
                                        outlinePaint.strokeWidth = 10f
                                        outlinePaint.strokeCap = Paint.Cap.ROUND
                                        outlinePaint.strokeJoin = Paint.Join.ROUND
                                    }
                                    val startM = Marker(this).apply {
                                        icon = ContextCompat.getDrawable(ctx, R.drawable.ic_ride_start_marker)
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                        setOnMarkerClickListener { _, _ -> true } // Suppress info popup
                                    }
                                    val riderM = Marker(this).apply {
                                        icon = ContextCompat.getDrawable(ctx, R.drawable.ic_rider_marker)
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                        setOnMarkerClickListener { _, _ -> true } // Suppress info popup
                                    }

                                    overlays.add(polyline)
                                    overlays.add(startM)
                                    overlays.add(riderM)

                                    routePolylineRef.value = polyline
                                    startMarkerRef.value = startM
                                    riderMarkerRef.value = riderM

                                    mapViewRef = this
                                }
                            },
                            update = { mapView ->
                                // 1. Sharp High-Contrast Dark Mode Filter
                                mapView.overlayManager.tilesOverlay.setColorFilter(
                                    if (isDark) darkMapColorFilter else null
                                )

                                // 2. Update Live Route Polyline (reusing single instance)
                                routePolylineRef.value?.let { polyline ->
                                    if (routeGeoPoints.size > 1) {
                                        polyline.setPoints(routeGeoPoints)
                                        polyline.setVisible(true)
                                    } else {
                                        polyline.setVisible(false)
                                    }
                                }

                                // 3. Update Start Point Marker (reusing single instance)
                                startMarkerRef.value?.let { startM ->
                                    if (routeGeoPoints.isNotEmpty()) {
                                        startM.position = routeGeoPoints.first()
                                        startM.setVisible(true)
                                        startM.closeInfoWindow()
                                    } else {
                                        startM.setVisible(false)
                                    }
                                }

                                // 4. Update Current Rider Position Marker (reusing single instance)
                                riderMarkerRef.value?.let { riderM ->
                                    if (currentGeoPoint != null) {
                                        riderM.position = currentGeoPoint
                                        riderM.setVisible(true)
                                        riderM.closeInfoWindow()

                                        // Auto-follow rider camera
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

                        // If no GPS coordinates yet, display acquiring radar overlay
                        if (currentGeoPoint == null) {
                            GpsAcquiringMapOverlay(isDark = isDark)
                        }

                        // Floating Live Speed HUD Badge on Top-Left
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
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
                                    text = String.format(Locale.US, "%.1f km/h", liveCurrentSpeedKmH),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                                )
                            }
                        }

                        // Floating GPS Status / Locked Badge on Top-Right
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) Color(0xDD121619) else Color(0xEEFFFFFF))
                                .border(
                                    1.dp,
                                    if (telemetry.gpsSignalStatus == GpsSignalStatus.LOCKED) (if (isDark) Color(0xFF10B981) else Color(0xFF0F9F7A))
                                    else (if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706)),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (telemetry.gpsSignalStatus == GpsSignalStatus.LOCKED) Icons.Rounded.GpsFixed else Icons.Rounded.GpsNotFixed,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = if (telemetry.gpsSignalStatus == GpsSignalStatus.LOCKED) (if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A))
                                    else (if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706))
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (telemetry.gpsSignalStatus == GpsSignalStatus.LOCKED) "GPS Locked" else "Acquiring...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
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
                                    isCameraFollowing = true
                                    currentGeoPoint?.let { pt ->
                                        mapViewRef?.controller?.apply {
                                            setZoom(17.5)
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

                Spacer(modifier = Modifier.height(14.dp))

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

                    // Pause / Resume Toggle
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
                                    text = String.format(Locale.US, "%.1f km/h", liveCurrentSpeedKmH),
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
                                    .background(if (isDark) CarbonDarkAccent else Color(0xFF0F9F7A))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (!telemetry.isPaused) "GPS Active" else "Ride Paused",
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

        // Primary Action Button at Bottom
        if (selectedScreenTab == 0) {
            Button(
                onClick = {
                    val completed = workoutEngine.finishWorkout()
                    onFinishTrackingWithData?.invoke(
                        completed.durationSeconds,
                        completed.distanceKm,
                        completed.caloriesBurned,
                        completed.avgPace,
                        138,
                        completed.steps
                    )
                    onFinishTracking()
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
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Finish Cycling Session",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
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
