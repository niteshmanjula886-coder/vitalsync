package com.simats.vitalsync.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.data.device.DeviceConnectionStatus
import com.simats.vitalsync.data.device.DiscoveredDevice
import com.simats.vitalsync.data.device.HealthDeviceManager
import com.simats.vitalsync.ui.theme.VitalPrimaryMint
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlePairingBottomSheet(
    deviceManager: HealthDeviceManager,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val discoveredDevices by deviceManager.discoveredDevices.collectAsState()
    val connectionStatus by deviceManager.connectionStatus.collectAsState()
    val connectedDeviceName by deviceManager.connectedDeviceName.collectAsState()

    BlePairingBottomSheet(
        modifier = modifier,
        discoveredDevices = discoveredDevices,
        connectionStatus = connectionStatus,
        connectedDeviceName = connectedDeviceName,
        onStartScan = { deviceManager.startScan() },
        onStopScan = { deviceManager.stopScan() },
        onConnectDevice = { device -> deviceManager.connectDevice(device) },
        onDisconnectDevice = { deviceManager.disconnect() },
        onDismissRequest = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlePairingBottomSheet(
    modifier: Modifier = Modifier,
    discoveredDevices: List<DiscoveredDevice> = emptyList(),
    connectionStatus: DeviceConnectionStatus = DeviceConnectionStatus.DISCONNECTED,
    connectedDeviceName: String? = null,
    onStartScan: () -> Unit = {},
    onStopScan: () -> Unit = {},
    onConnectDevice: (DiscoveredDevice) -> Unit = {},
    onDisconnectDevice: () -> Unit = {},
    onDismissRequest: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Auto-launch scan on sheet opening
    LaunchedEffect(Unit) {
        if (connectionStatus != DeviceConnectionStatus.CONNECTED) {
            onStartScan()
        }
    }

    // Auto-dismiss 1.2s after successful connection
    LaunchedEffect(connectionStatus) {
        if (connectionStatus == DeviceConnectionStatus.CONNECTED) {
            delay(1200)
            onDismissRequest()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE6FFFA)),
                        contentAlignment = Alignment.Center
                    ) {
                        BluetoothIcon(modifier = Modifier.size(20.dp), color = VitalPrimaryMint)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Pair Health Watch",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (connectionStatus) {
                                DeviceConnectionStatus.CONNECTED -> "Connected to ${connectedDeviceName ?: "Watch"}"
                                DeviceConnectionStatus.CONNECTING -> "Connecting..."
                                DeviceConnectionStatus.SCANNING -> "Scanning for nearby watches (10s)..."
                                else -> "Tap a smartwatch to connect"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (connectionStatus == DeviceConnectionStatus.SCANNING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = VitalPrimaryMint
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Connected Banner
            if (connectionStatus == DeviceConnectionStatus.CONNECTED) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFDDF7EC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "✓", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00A884))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = connectedDeviceName ?: "Connected Smartwatch",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF006952)
                                )
                                Text(
                                    text = "Streaming live vitals to dashboard",
                                    fontSize = 11.sp,
                                    color = Color(0xFF008968)
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onDisconnectDevice,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF00A884))
                        ) {
                            Text("Disconnect", fontSize = 11.sp, color = Color(0xFF00A884))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Discovered Devices List
            if (discoveredDevices.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "⌚", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (connectionStatus == DeviceConnectionStatus.SCANNING)
                                "Searching for Bluetooth trackers..."
                            else
                                "No nearby devices found",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Make sure your watch has Bluetooth turned on and is within 5 meters.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                Text(
                    text = "AVAILABLE DEVICES (${discoveredDevices.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(discoveredDevices) { device ->
                        DeviceItemRow(
                            device = device,
                            isConnecting = (connectionStatus == DeviceConnectionStatus.CONNECTING),
                            isConnected = (connectionStatus == DeviceConnectionStatus.CONNECTED && device.name == connectedDeviceName),
                            onClick = { onConnectDevice(device) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (connectionStatus == DeviceConnectionStatus.SCANNING) {
                    OutlinedButton(
                        onClick = onStopScan,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Stop Scan")
                    }
                } else {
                    Button(
                        onClick = onStartScan,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VitalPrimaryMint)
                    ) {
                        Text("Scan Again", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun DeviceItemRow(
    device: DiscoveredDevice,
    isConnecting: Boolean,
    isConnected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isConnected) 2.dp else 1.dp,
            color = if (isConnected) Color(0xFF00A884) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !isConnecting && !isConnected) { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⌚", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = device.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = device.address,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (device.rssi != 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${device.rssi} dBm",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                when {
                    isConnected -> {
                        Text(text = "Connected", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00A884))
                    }
                    isConnecting -> {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = VitalPrimaryMint)
                    }
                    else -> {
                        Text(text = "Connect →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VitalPrimaryMint)
                    }
                }
            }
        }
    }
}

@Composable
private fun BluetoothIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Black
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            lineTo(w * 0.5f, h)
            lineTo(w * 0.85f, h * 0.75f)
            lineTo(w * 0.15f, h * 0.25f)
            moveTo(w * 0.15f, h * 0.75f)
            lineTo(w * 0.85f, h * 0.25f)
            lineTo(w * 0.5f, 0f)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
