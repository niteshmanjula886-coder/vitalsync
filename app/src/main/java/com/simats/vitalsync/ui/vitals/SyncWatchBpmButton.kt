package com.simats.vitalsync.ui.vitals

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BluetoothDisabled
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.simats.vitalsync.data.device.BluetoothReadiness
import com.simats.vitalsync.data.device.HealthConnectAvailability
import com.simats.vitalsync.data.device.HealthConnectVitalResult
import com.simats.vitalsync.data.device.WatchBpReading
import com.simats.vitalsync.data.device.WatchBpmReading
import com.simats.vitalsync.data.device.WatchBpmSync
import com.simats.vitalsync.data.device.WatchBpmSyncResult
import com.simats.vitalsync.data.device.WatchSpo2Reading
import com.simats.vitalsync.ui.theme.CarbonDarkBackground
import com.simats.vitalsync.ui.theme.CarbonDarkSurface
import com.simats.vitalsync.ui.theme.VitalTealDark
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class InlineSyncStatusKind {
    SUCCESS,
    INFO_EMPTY,
    ERROR
}

/**
 * Compact, local-first Jetpack Compose button that checks Bluetooth readiness,
 * verifies Health Connect availability and READ_HEART_RATE permission, and syncs
 * the latest watch BPM reading without any external paid APIs.
 */
@Composable
fun SyncWatchBpmButton(
    modifier: Modifier = Modifier,
    label: String = "Sync Watch BPM",
    windowHours: Long = 24L,
    showInlineStatus: Boolean = true,
    autoOpenPlayStoreOnUpdateRequired: Boolean = true,
    onBpmSynced: (WatchBpmReading) -> Unit = {},
    onStatusMessage: (message: String, isError: Boolean) -> Unit = { _, _ -> },
    onLoadingChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground ||
        MaterialTheme.colorScheme.surface == CarbonDarkSurface

    var isSyncing by remember { mutableStateOf(false) }
    var inlineStatusText by remember { mutableStateOf<String?>(null) }
    var inlineStatusKind by remember { mutableStateOf(InlineSyncStatusKind.INFO_EMPTY) }

    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
    }

    fun updateSyncing(loading: Boolean) {
        isSyncing = loading
        onLoadingChange(loading)
    }

    fun emitStatus(message: String, kind: InlineSyncStatusKind) {
        inlineStatusText = message
        inlineStatusKind = kind
        onStatusMessage(message, kind == InlineSyncStatusKind.ERROR)
    }

    fun handleQueryResult(result: WatchBpmSyncResult) {
        when (result) {
            is WatchBpmSyncResult.Success -> {
                val formattedTime = try {
                    timeFormatter.format(result.reading.time)
                } catch (_: Exception) {
                    "recently"
                }
                emitStatus(
                    message = "Synced ${result.reading.bpm} BPM from watch ($formattedTime)",
                    kind = InlineSyncStatusKind.SUCCESS
                )
                onBpmSynced(result.reading)
            }
            WatchBpmSyncResult.NoRecentReading -> {
                emitStatus(
                    message = "No heart rate readings found in Health Connect from the last ${windowHours}h. Manual entries remain intact.",
                    kind = InlineSyncStatusKind.INFO_EMPTY
                )
            }
            WatchBpmSyncResult.BluetoothDisabled -> {
                emitStatus(
                    message = "Bluetooth is turned off. Please enable Bluetooth to sync watch BPM.",
                    kind = InlineSyncStatusKind.ERROR
                )
            }
            WatchBpmSyncResult.BluetoothAdapterUnavailable -> {
                emitStatus(
                    message = "Bluetooth hardware is unavailable on this device.",
                    kind = InlineSyncStatusKind.ERROR
                )
            }
            WatchBpmSyncResult.BluetoothPermissionRequired -> {
                emitStatus(
                    message = "Bluetooth permission is required to check watch connection readiness.",
                    kind = InlineSyncStatusKind.ERROR
                )
            }
            WatchBpmSyncResult.HealthConnectUnavailable -> {
                emitStatus(
                    message = "Health Connect is unavailable on this device.",
                    kind = InlineSyncStatusKind.ERROR
                )
            }
            is WatchBpmSyncResult.HealthConnectUpdateRequired -> {
                if (autoOpenPlayStoreOnUpdateRequired) {
                    WatchBpmSync.openHealthConnectPlayStore(context)
                }
                emitStatus(
                    message = "Health Connect requires installation or update from the Google Play Store.",
                    kind = InlineSyncStatusKind.ERROR
                )
            }
            WatchBpmSyncResult.PermissionRequired -> {
                emitStatus(
                    message = "Heart rate read permission is required in Health Connect.",
                    kind = InlineSyncStatusKind.ERROR
                )
            }
            is WatchBpmSyncResult.Error -> {
                emitStatus(
                    message = result.message,
                    kind = InlineSyncStatusKind.ERROR
                )
            }
        }
    }

    val healthPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        if (grantedPermissions.contains(WatchBpmSync.HEART_RATE_READ_PERMISSION)) {
            scope.launch {
                val result = WatchBpmSync.queryLatestHeartRateSample(context, windowHours)
                handleQueryResult(result)
                updateSyncing(false)
            }
        } else {
            emitStatus(
                message = "Heart rate permission was denied in Health Connect.",
                kind = InlineSyncStatusKind.ERROR
            )
            updateSyncing(false)
        }
    }

    suspend fun proceedWithHealthConnectCheck() {
        when (WatchBpmSync.getHealthConnectAvailability(context)) {
            HealthConnectAvailability.UNAVAILABLE -> {
                handleQueryResult(WatchBpmSyncResult.HealthConnectUnavailable)
                updateSyncing(false)
                return
            }
            HealthConnectAvailability.UPDATE_REQUIRED -> {
                handleQueryResult(
                    WatchBpmSyncResult.HealthConnectUpdateRequired(
                        WatchBpmSync.buildHealthConnectPlayStoreIntent(context)
                    )
                )
                updateSyncing(false)
                return
            }
            HealthConnectAvailability.AVAILABLE -> Unit
        }

        val client = try {
            HealthConnectClient.getOrCreate(context)
        } catch (e: Exception) {
            emitStatus(
                message = e.localizedMessage ?: "Unable to connect to Health Connect.",
                kind = InlineSyncStatusKind.ERROR
            )
            updateSyncing(false)
            return
        }

        if (!WatchBpmSync.hasHeartRatePermission(client)) {
            healthPermissionLauncher.launch(WatchBpmSync.REQUIRED_PERMISSIONS)
        } else {
            val result = WatchBpmSync.queryLatestHeartRateSample(context, windowHours)
            handleQueryResult(result)
            updateSyncing(false)
        }
    }

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                when (WatchBpmSync.checkBluetoothReadiness(context)) {
                    BluetoothReadiness.ENABLED -> proceedWithHealthConnectCheck()
                    BluetoothReadiness.DISABLED -> {
                        handleQueryResult(WatchBpmSyncResult.BluetoothDisabled)
                        updateSyncing(false)
                    }
                    BluetoothReadiness.ADAPTER_UNAVAILABLE -> {
                        handleQueryResult(WatchBpmSyncResult.BluetoothAdapterUnavailable)
                        updateSyncing(false)
                    }
                    BluetoothReadiness.PERMISSION_REQUIRED -> {
                        handleQueryResult(WatchBpmSyncResult.BluetoothPermissionRequired)
                        updateSyncing(false)
                    }
                }
            }
        } else {
            emitStatus(
                message = "Bluetooth permission is required on Android 12+ to verify watch readiness.",
                kind = InlineSyncStatusKind.ERROR
            )
            updateSyncing(false)
        }
    }

    fun startSyncFlow() {
        if (isSyncing) return
        updateSyncing(true)
        inlineStatusText = null

        scope.launch {
            when (WatchBpmSync.checkBluetoothReadiness(context)) {
                BluetoothReadiness.ADAPTER_UNAVAILABLE -> {
                    handleQueryResult(WatchBpmSyncResult.BluetoothAdapterUnavailable)
                    updateSyncing(false)
                }
                BluetoothReadiness.PERMISSION_REQUIRED -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                    } else {
                        handleQueryResult(WatchBpmSyncResult.BluetoothPermissionRequired)
                        updateSyncing(false)
                    }
                }
                BluetoothReadiness.DISABLED -> {
                    handleQueryResult(WatchBpmSyncResult.BluetoothDisabled)
                    updateSyncing(false)
                }
                BluetoothReadiness.ENABLED -> {
                    proceedWithHealthConnectCheck()
                }
            }
        }
    }

    SyncVitalButtonLayout(
        modifier = modifier,
        label = label,
        syncingLabel = "Syncing Watch BPM...",
        icon = Icons.Rounded.Watch,
        isSyncing = isSyncing,
        isDark = isDark,
        showInlineStatus = showInlineStatus,
        inlineStatusText = inlineStatusText,
        inlineStatusKind = inlineStatusKind,
        onClick = { startSyncFlow() }
    )
}

/**
 * Optional Health Connect sync button for SpO2 (OxygenSaturationRecord), keeping manual
 * SpO2 entries intact whenever no synced records exist.
 */
@Composable
fun SyncHealthConnectSpo2Button(
    modifier: Modifier = Modifier,
    label: String = "Sync Health Connect SpO2",
    windowHours: Long = 24L,
    showInlineStatus: Boolean = true,
    autoOpenPlayStoreOnUpdateRequired: Boolean = true,
    onSpo2Synced: (WatchSpo2Reading) -> Unit = {},
    onStatusMessage: (message: String, isError: Boolean) -> Unit = { _, _ -> },
    onLoadingChange: (Boolean) -> Unit = {}
) {
    SyncHealthConnectVitalButton(
        modifier = modifier,
        label = label,
        syncingLabel = "Syncing SpO2...",
        icon = Icons.Rounded.Watch,
        requiredPermission = WatchBpmSync.OXYGEN_SATURATION_READ_PERMISSION,
        permissionDeniedMessage = "SpO2 read permission was denied in Health Connect. Manual entry remains available.",
        emptyResultMessage = "No synced SpO2 records found in Health Connect (last ${windowHours}h). Existing manual entries remain intact.",
        showInlineStatus = showInlineStatus,
        autoOpenPlayStoreOnUpdateRequired = autoOpenPlayStoreOnUpdateRequired,
        queryRecords = { ctx -> WatchBpmSync.queryLatestSpo2(ctx, windowHours) },
        formatSuccessMessage = { reading, formattedTime ->
            "Synced ${reading.percentage}% SpO2 via Health Connect ($formattedTime)"
        },
        extractTime = { it.time },
        onSynced = onSpo2Synced,
        onStatusMessage = onStatusMessage,
        onLoadingChange = onLoadingChange
    )
}

/**
 * Optional Health Connect sync button for Blood Pressure (BloodPressureRecord), keeping
 * manual Blood Pressure entries intact whenever no synced records exist.
 */
@Composable
fun SyncHealthConnectBpButton(
    modifier: Modifier = Modifier,
    label: String = "Sync Health Connect BP",
    windowHours: Long = 24L,
    showInlineStatus: Boolean = true,
    autoOpenPlayStoreOnUpdateRequired: Boolean = true,
    onBpSynced: (WatchBpReading) -> Unit = {},
    onStatusMessage: (message: String, isError: Boolean) -> Unit = { _, _ -> },
    onLoadingChange: (Boolean) -> Unit = {}
) {
    SyncHealthConnectVitalButton(
        modifier = modifier,
        label = label,
        syncingLabel = "Syncing Blood Pressure...",
        icon = Icons.Rounded.MonitorHeart,
        requiredPermission = WatchBpmSync.BLOOD_PRESSURE_READ_PERMISSION,
        permissionDeniedMessage = "Blood pressure read permission was denied in Health Connect. Manual entry remains available.",
        emptyResultMessage = "No synced BP records found in Health Connect (last ${windowHours}h). Existing manual entries remain intact.",
        showInlineStatus = showInlineStatus,
        autoOpenPlayStoreOnUpdateRequired = autoOpenPlayStoreOnUpdateRequired,
        queryRecords = { ctx -> WatchBpmSync.queryLatestBloodPressure(ctx, windowHours) },
        formatSuccessMessage = { reading, formattedTime ->
            "Synced ${reading.systolic}/${reading.diastolic} mmHg via Health Connect ($formattedTime)"
        },
        extractTime = { it.time },
        onSynced = onBpSynced,
        onStatusMessage = onStatusMessage,
        onLoadingChange = onLoadingChange
    )
}

@Composable
private fun <T> SyncHealthConnectVitalButton(
    modifier: Modifier = Modifier,
    label: String,
    syncingLabel: String,
    icon: ImageVector,
    requiredPermission: String,
    permissionDeniedMessage: String,
    emptyResultMessage: String,
    showInlineStatus: Boolean,
    autoOpenPlayStoreOnUpdateRequired: Boolean,
    queryRecords: suspend (android.content.Context) -> HealthConnectVitalResult<T>,
    formatSuccessMessage: (T, String) -> String,
    extractTime: (T) -> java.time.Instant,
    onSynced: (T) -> Unit,
    onStatusMessage: (message: String, isError: Boolean) -> Unit,
    onLoadingChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground ||
        MaterialTheme.colorScheme.surface == CarbonDarkSurface

    var isSyncing by remember { mutableStateOf(false) }
    var inlineStatusText by remember { mutableStateOf<String?>(null) }
    var inlineStatusKind by remember { mutableStateOf(InlineSyncStatusKind.INFO_EMPTY) }

    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
    }

    fun updateSyncing(loading: Boolean) {
        isSyncing = loading
        onLoadingChange(loading)
    }

    fun emitStatus(message: String, kind: InlineSyncStatusKind) {
        inlineStatusText = message
        inlineStatusKind = kind
        onStatusMessage(message, kind == InlineSyncStatusKind.ERROR)
    }

    fun handleVitalResult(result: HealthConnectVitalResult<T>) {
        when (result) {
            is HealthConnectVitalResult.Success -> {
                val formattedTime = try {
                    timeFormatter.format(extractTime(result.reading))
                } catch (_: Exception) {
                    "recently"
                }
                emitStatus(
                    message = formatSuccessMessage(result.reading, formattedTime),
                    kind = InlineSyncStatusKind.SUCCESS
                )
                onSynced(result.reading)
            }
            HealthConnectVitalResult.NoRecentReading -> {
                emitStatus(
                    message = emptyResultMessage,
                    kind = InlineSyncStatusKind.INFO_EMPTY
                )
            }
            HealthConnectVitalResult.HealthConnectUnavailable -> {
                emitStatus(
                    message = "Health Connect is unavailable on this device. Manual entry remains active.",
                    kind = InlineSyncStatusKind.INFO_EMPTY
                )
            }
            is HealthConnectVitalResult.HealthConnectUpdateRequired -> {
                if (autoOpenPlayStoreOnUpdateRequired) {
                    WatchBpmSync.openHealthConnectPlayStore(context)
                }
                emitStatus(
                    message = "Health Connect needs installation or update from the Google Play Store.",
                    kind = InlineSyncStatusKind.ERROR
                )
            }
            HealthConnectVitalResult.PermissionRequired -> {
                emitStatus(
                    message = permissionDeniedMessage,
                    kind = InlineSyncStatusKind.ERROR
                )
            }
            is HealthConnectVitalResult.Error -> {
                emitStatus(
                    message = result.message,
                    kind = InlineSyncStatusKind.ERROR
                )
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        if (grantedPermissions.contains(requiredPermission)) {
            scope.launch {
                val result = queryRecords(context)
                handleVitalResult(result)
                updateSyncing(false)
            }
        } else {
            emitStatus(
                message = permissionDeniedMessage,
                kind = InlineSyncStatusKind.ERROR
            )
            updateSyncing(false)
        }
    }

    fun startSyncFlow() {
        if (isSyncing) return
        updateSyncing(true)
        inlineStatusText = null

        scope.launch {
            when (WatchBpmSync.getHealthConnectAvailability(context)) {
                HealthConnectAvailability.UNAVAILABLE -> {
                    handleVitalResult(HealthConnectVitalResult.HealthConnectUnavailable)
                    updateSyncing(false)
                    return@launch
                }
                HealthConnectAvailability.UPDATE_REQUIRED -> {
                    handleVitalResult(
                        HealthConnectVitalResult.HealthConnectUpdateRequired(
                            WatchBpmSync.buildHealthConnectPlayStoreIntent(context)
                        )
                    )
                    updateSyncing(false)
                    return@launch
                }
                HealthConnectAvailability.AVAILABLE -> Unit
            }

            val client = try {
                HealthConnectClient.getOrCreate(context)
            } catch (e: Exception) {
                emitStatus(
                    message = e.localizedMessage ?: "Unable to connect to Health Connect.",
                    kind = InlineSyncStatusKind.ERROR
                )
                updateSyncing(false)
                return@launch
            }

            if (!WatchBpmSync.hasPermission(client, requiredPermission)) {
                permissionLauncher.launch(setOf(requiredPermission))
            } else {
                val result = queryRecords(context)
                handleVitalResult(result)
                updateSyncing(false)
            }
        }
    }

    SyncVitalButtonLayout(
        modifier = modifier,
        label = label,
        syncingLabel = syncingLabel,
        icon = icon,
        isSyncing = isSyncing,
        isDark = isDark,
        showInlineStatus = showInlineStatus,
        inlineStatusText = inlineStatusText,
        inlineStatusKind = inlineStatusKind,
        onClick = { startSyncFlow() }
    )
}

@Composable
private fun SyncVitalButtonLayout(
    modifier: Modifier,
    label: String,
    syncingLabel: String,
    icon: ImageVector,
    isSyncing: Boolean,
    isDark: Boolean,
    showInlineStatus: Boolean,
    inlineStatusText: String?,
    inlineStatusKind: InlineSyncStatusKind,
    onClick: () -> Unit
) {
    val accentColor = if (isDark) Color(0xFF34D399) else VitalTealDark
    val containerColor = if (isDark) Color(0xFF142B22) else Color(0xFFECFDF5)
    val borderColor = if (isDark) Color(0xFF1F4D3A) else Color(0xFFA7F3D0)

    Column(modifier = modifier) {
        OutlinedButton(
            onClick = onClick,
            enabled = !isSyncing,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, borderColor),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = containerColor,
                contentColor = accentColor,
                disabledContainerColor = containerColor.copy(alpha = 0.6f),
                disabledContentColor = accentColor.copy(alpha = 0.6f)
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = syncingLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                        tint = accentColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (showInlineStatus && !inlineStatusText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            val statusBg = when (inlineStatusKind) {
                InlineSyncStatusKind.ERROR -> if (isDark) Color(0xFF3B1D1D) else Color(0xFFFEF2F2)
                InlineSyncStatusKind.INFO_EMPTY -> if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                InlineSyncStatusKind.SUCCESS -> if (isDark) Color(0xFF132E24) else Color(0xFFF0FDF4)
            }
            val statusTint = when (inlineStatusKind) {
                InlineSyncStatusKind.ERROR -> if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C)
                InlineSyncStatusKind.INFO_EMPTY -> if (isDark) Color(0xFF93C5FD) else Color(0xFF334155)
                InlineSyncStatusKind.SUCCESS -> if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857)
            }
            val statusIcon = when {
                inlineStatusKind == InlineSyncStatusKind.ERROR &&
                    inlineStatusText.contains("Bluetooth", ignoreCase = true) ->
                    Icons.Rounded.BluetoothDisabled
                inlineStatusKind == InlineSyncStatusKind.ERROR -> Icons.Rounded.ErrorOutline
                inlineStatusKind == InlineSyncStatusKind.INFO_EMPTY -> Icons.Rounded.Info
                else -> Icons.Rounded.CheckCircle
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(statusBg)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = statusTint
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = inlineStatusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusTint,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
