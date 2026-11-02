package com.simats.vitalsync.data.device

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.ArrayDeque
import java.util.UUID

enum class DeviceConnectionStatus {
    DISCONNECTED,            // "Connect your device"
    SCANNING,                // "Scanning for devices..."
    CONNECTING,              // "Connecting..."
    CONNECTED,               // "Connected"
    DISCONNECTED_UNEXPECTED, // "Device disconnected"
    PERMISSION_REQUIRED      // "Bluetooth permission required"
}

data class DiscoveredDevice(
    val name: String,
    val address: String,
    val rssi: Int = 0,
    val isBonded: Boolean = false
)

data class LiveHealthMetrics(
    val heartRate: Int? = null,
    val spo2: Int? = null,
    val bloodPressure: Pair<Int, Int>? = null, // systolic, diastolic
    val steps: Int? = null,
    val calories: Int? = null,
    val distanceKm: Float? = null,
    val activeMinutes: Int? = null,
    val sleepMinutes: Int? = null
)

/**
 * HealthDeviceManager: Native Android Kotlin BLE Health Device Manager.
 * Connects to smartwatches, fitness bands, pulse oximeters, and blood pressure monitors.
 * Supports standard Bluetooth SIG profiles and Health Connect synchronization.
 */
class HealthDeviceManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "HealthDeviceManager"

        // BLE Standard SIG Services & Characteristics
        val UUID_HEART_RATE_SERVICE: UUID = UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB")
        val UUID_HEART_RATE_MEASUREMENT: UUID = UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB")
        val UUID_BODY_SENSOR_LOCATION: UUID = UUID.fromString("00002A38-0000-1000-8000-00805F9B34FB")

        val UUID_BLOOD_PRESSURE_SERVICE: UUID = UUID.fromString("00001810-0000-1000-8000-00805F9B34FB")
        val UUID_BLOOD_PRESSURE_MEASUREMENT: UUID = UUID.fromString("00002A35-0000-1000-8000-00805F9B34FB")

        val UUID_PULSE_OXIMETER_SERVICE: UUID = UUID.fromString("00001822-0000-1000-8000-00805F9B34FB")
        val UUID_SPO2_SPOT_CHECK: UUID = UUID.fromString("00002A5E-0000-1000-8000-00805F9B34FB")
        val UUID_SPO2_CONTINUOUS: UUID = UUID.fromString("00002A5F-0000-1000-8000-00805F9B34FB")

        val UUID_BATTERY_SERVICE: UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB")
        val UUID_BATTERY_LEVEL: UUID = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB")

        val UUID_RUNNING_SPEED_CADENCE: UUID = UUID.fromString("00001814-0000-1000-8000-00805F9B34FB")
        val UUID_RSC_MEASUREMENT: UUID = UUID.fromString("00002A53-0000-1000-8000-00805F9B34FB")

        val UUID_CYCLING_SPEED_CADENCE: UUID = UUID.fromString("00001816-0000-1000-8000-00805F9B34FB")
        val UUID_CSC_MEASUREMENT: UUID = UUID.fromString("00002A5B-0000-1000-8000-00805F9B34FB")

        // Popular Smartwatch / Fitness Band Custom Services (Mi Band, Huami, Generic BLE Bands)
        val UUID_WEARABLE_STEP_SERVICE: UUID = UUID.fromString("0000FEE0-0000-1000-8000-00805F9B34FB")
        val UUID_WEARABLE_REALTIME_STEPS: UUID = UUID.fromString("00000007-0000-3512-2118-0009AF100700")

        val UUID_CLIENT_CHARACTERISTIC_CONFIG: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: HealthDeviceManager? = null

        fun getInstance(context: Context): HealthDeviceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HealthDeviceManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var activeGatt: BluetoothGatt? = null
    private var lastTargetDevice: DiscoveredDevice? = null
    private var reconnectJob: Job? = null
    private var reconnectAttempts = 0
    private val MAX_RECONNECT_ATTEMPTS = 3

    // Descriptor write queue to prevent GATT concurrency drops
    private val descriptorWriteQueue = ArrayDeque<Pair<BluetoothGatt, BluetoothGattDescriptor>>()
    private var isWritingDescriptor = false

    // Connection & Device State
    private val _connectionStatus = MutableStateFlow(DeviceConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<DeviceConnectionStatus> = _connectionStatus.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    private val _connectedDeviceAddress = MutableStateFlow<String?>(null)
    val connectedDeviceAddress: StateFlow<String?> = _connectedDeviceAddress.asStateFlow()

    private val _batteryLevel = MutableStateFlow<Int?>(null)
    val batteryLevel: StateFlow<Int?> = _batteryLevel.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long?>(null)
    val lastSyncTimestamp: StateFlow<Long?> = _lastSyncTimestamp.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    // Live Metrics
    private val _liveMetrics = MutableStateFlow(LiveHealthMetrics())
    val liveMetrics: StateFlow<LiveHealthMetrics> = _liveMetrics.asStateFlow()

    // Supported measurements for current device (null = checking/unknown, false = unsupported, true = supported)
    private val _isHeartRateSupported = MutableStateFlow<Boolean?>(null)
    val isHeartRateSupported: StateFlow<Boolean?> = _isHeartRateSupported.asStateFlow()

    private val _isSpo2Supported = MutableStateFlow<Boolean?>(null)
    val isSpo2Supported: StateFlow<Boolean?> = _isSpo2Supported.asStateFlow()

    private val _isBloodPressureSupported = MutableStateFlow<Boolean?>(null)
    val isBloodPressureSupported: StateFlow<Boolean?> = _isBloodPressureSupported.asStateFlow()

    private val _isStepsSupported = MutableStateFlow<Boolean?>(null)
    val isStepsSupported: StateFlow<Boolean?> = _isStepsSupported.asStateFlow()

    // Callback invoked when a real health measurement is received, to persist to MySQL database
    var onReadingReceivedListener: ((type: String, val1: Float, val2: Float?, text: String, unit: String) -> Unit)? = null

    // Health Connect Client
    private val healthConnectClient: HealthConnectClient? by lazy {
        try {
            if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
                HealthConnectClient.getOrCreate(context)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Health Connect client init: ${e.message}")
            null
        }
    }

    private val connectionTimeoutRunnable = Runnable {
        if (_connectionStatus.value == DeviceConnectionStatus.CONNECTING) {
            Log.w(TAG, "Connection attempt timed out after 15 seconds")
            disconnect()
            _connectionStatus.value = DeviceConnectionStatus.DISCONNECTED
        }
    }

    init {
        registerBondReceiver()
    }

    private fun registerBondReceiver() {
        try {
            val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            context.registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(c: Context?, intent: Intent?) {
                    if (intent?.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                        val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
                        Log.d(TAG, "Bond state changed for ${device?.address}: $bondState")
                        if (bondState == BluetoothDevice.BOND_BONDED && device?.address == lastTargetDevice?.address) {
                            // Resume or refresh connection after bonding
                            activeGatt?.discoverServices()
                        }
                    }
                }
            }, filter)
        } catch (e: Exception) {
            Log.w(TAG, "registerBondReceiver: ${e.message}")
        }
    }

    fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result ?: return
            val dev = result.device ?: return
            try {
                @SuppressLint("MissingPermission")
                val name = dev.name ?: "Health Device"
                val addr = dev.address ?: return
                val rssi = result.rssi
                val isBonded = dev.bondState == BluetoothDevice.BOND_BONDED

                // Filter out blank names
                if (!dev.name.isNullOrBlank()) {
                    val currentList = _discoveredDevices.value.toMutableList()
                    val existingIndex = currentList.indexOfFirst { it.address == addr }
                    val item = DiscoveredDevice(name = name, address = addr, rssi = rssi, isBonded = isBonded)
                    if (existingIndex >= 0) {
                        currentList[existingIndex] = item
                    } else {
                        currentList.add(item)
                    }
                    _discoveredDevices.value = currentList
                }
            } catch (e: SecurityException) {
                _connectionStatus.value = DeviceConnectionStatus.PERMISSION_REQUIRED
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "BLE Scan failed with code: $errorCode")
            _connectionStatus.value = DeviceConnectionStatus.DISCONNECTED
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!hasBluetoothPermissions()) {
            _connectionStatus.value = DeviceConnectionStatus.PERMISSION_REQUIRED
            return
        }
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            _connectionStatus.value = DeviceConnectionStatus.DISCONNECTED
            return
        }

        try {
            _discoveredDevices.value = emptyList()
            _connectionStatus.value = DeviceConnectionStatus.SCANNING

            val scanner = adapter.bluetoothLeScanner
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            // Pre-populate already bonded health devices
            val bonded = adapter.bondedDevices
            if (!bonded.isNullOrEmpty()) {
                val pairedList = bonded.map {
                    DiscoveredDevice(name = it.name ?: "Paired Device", address = it.address, isBonded = true)
                }
                _discoveredDevices.value = pairedList
            }

            scanner?.startScan(emptyList(), settings, scanCallback)

            // Auto-stop scan after 12 seconds
            mainHandler.postDelayed({
                stopScan()
            }, 12000L)
        } catch (e: SecurityException) {
            _connectionStatus.value = DeviceConnectionStatus.PERMISSION_REQUIRED
        } catch (e: Exception) {
            Log.e(TAG, "startScan error: ${e.message}")
            _connectionStatus.value = DeviceConnectionStatus.DISCONNECTED
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        try {
            if (hasBluetoothPermissions()) {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            }
            if (_connectionStatus.value == DeviceConnectionStatus.SCANNING) {
                _connectionStatus.value = if (_connectedDeviceName.value != null) {
                    DeviceConnectionStatus.CONNECTED
                } else {
                    DeviceConnectionStatus.DISCONNECTED
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "stopScan error: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun connectDevice(device: DiscoveredDevice) {
        if (!hasBluetoothPermissions()) {
            _connectionStatus.value = DeviceConnectionStatus.PERMISSION_REQUIRED
            return
        }
        stopScan()
        reconnectJob?.cancel()
        reconnectAttempts = 0
        lastTargetDevice = device

        try {
            disconnect()
            _connectionStatus.value = DeviceConnectionStatus.CONNECTING
            _connectedDeviceName.value = device.name
            _connectedDeviceAddress.value = device.address

            // Reset capability flags
            _isHeartRateSupported.value = null
            _isSpo2Supported.value = null
            _isBloodPressureSupported.value = null
            _isStepsSupported.value = null

            val bleDevice = bluetoothAdapter?.getRemoteDevice(device.address)
            if (bleDevice == null) {
                _connectionStatus.value = DeviceConnectionStatus.DISCONNECTED
                return
            }

            // Set 15s connection timeout
            mainHandler.removeCallbacks(connectionTimeoutRunnable)
            mainHandler.postDelayed(connectionTimeoutRunnable, 15000L)

            activeGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                bleDevice.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            } else {
                bleDevice.connectGatt(context, false, gattCallback)
            }
        } catch (e: SecurityException) {
            _connectionStatus.value = DeviceConnectionStatus.PERMISSION_REQUIRED
        } catch (e: Exception) {
            Log.e(TAG, "connectDevice error: ${e.message}")
            _connectionStatus.value = DeviceConnectionStatus.DISCONNECTED
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        mainHandler.removeCallbacks(connectionTimeoutRunnable)
        reconnectJob?.cancel()
        try {
            activeGatt?.disconnect()
            activeGatt?.close()
            activeGatt = null
        } catch (e: Exception) {
            Log.e(TAG, "disconnect error: ${e.message}")
        }
        descriptorWriteQueue.clear()
        isWritingDescriptor = false
        _connectionStatus.value = DeviceConnectionStatus.DISCONNECTED
        _connectedDeviceName.value = null
        _connectedDeviceAddress.value = null
        _batteryLevel.value = null
    }

    private fun attemptAutoReconnect() {
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS && lastTargetDevice != null) {
            reconnectJob?.cancel()
            reconnectJob = scope.launch {
                reconnectAttempts++
                val delayTime = (reconnectAttempts * 2500L)
                Log.d(TAG, "Attempting auto-reconnect #$reconnectAttempts in ${delayTime}ms")
                delay(delayTime)
                lastTargetDevice?.let { dev ->
                    mainHandler.post {
                        connectDevice(dev)
                    }
                }
            }
        } else {
            _connectionStatus.value = DeviceConnectionStatus.DISCONNECTED_UNEXPECTED
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            mainHandler.removeCallbacks(connectionTimeoutRunnable)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "GATT connected successfully to ${gatt?.device?.address}")
                reconnectAttempts = 0
                _connectionStatus.value = DeviceConnectionStatus.CONNECTED
                _lastSyncTimestamp.value = System.currentTimeMillis()
                // Discover services with a brief delay for BLE stability
                mainHandler.postDelayed({
                    gatt?.discoverServices()
                }, 300L)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.w(TAG, "GATT disconnected, status code: $status")
                val wasConnected = _connectionStatus.value == DeviceConnectionStatus.CONNECTED
                gatt?.close()
                activeGatt = null
                descriptorWriteQueue.clear()
                isWritingDescriptor = false

                if (wasConnected) {
                    _connectionStatus.value = DeviceConnectionStatus.DISCONNECTED_UNEXPECTED
                    attemptAutoReconnect()
                } else {
                    _connectionStatus.value = DeviceConnectionStatus.DISCONNECTED
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS || gatt == null) {
                Log.w(TAG, "onServicesDiscovered failed with status: $status")
                return
            }

            var hrFound = false
            var spo2Found = false
            var bpFound = false
            var stepsFound = false

            gatt.services?.forEach { service ->
                when (service.uuid) {
                    UUID_HEART_RATE_SERVICE -> {
                        hrFound = true
                        _isHeartRateSupported.value = true
                        val char = service.getCharacteristic(UUID_HEART_RATE_MEASUREMENT)
                        if (char != null) queueEnableNotification(gatt, char)
                    }
                    UUID_BLOOD_PRESSURE_SERVICE -> {
                        bpFound = true
                        _isBloodPressureSupported.value = true
                        val char = service.getCharacteristic(UUID_BLOOD_PRESSURE_MEASUREMENT)
                        if (char != null) queueEnableIndication(gatt, char)
                    }
                    UUID_PULSE_OXIMETER_SERVICE -> {
                        spo2Found = true
                        _isSpo2Supported.value = true
                        val char = service.getCharacteristic(UUID_SPO2_CONTINUOUS)
                            ?: service.getCharacteristic(UUID_SPO2_SPOT_CHECK)
                        if (char != null) queueEnableNotification(gatt, char)
                    }
                    UUID_RUNNING_SPEED_CADENCE -> {
                        stepsFound = true
                        _isStepsSupported.value = true
                        val char = service.getCharacteristic(UUID_RSC_MEASUREMENT)
                        if (char != null) queueEnableNotification(gatt, char)
                    }
                    UUID_WEARABLE_STEP_SERVICE -> {
                        stepsFound = true
                        _isStepsSupported.value = true
                        val char = service.getCharacteristic(UUID_WEARABLE_REALTIME_STEPS)
                        if (char != null) queueEnableNotification(gatt, char)
                    }
                    UUID_BATTERY_SERVICE -> {
                        val char = service.getCharacteristic(UUID_BATTERY_LEVEL)
                        if (char != null) {
                            queueEnableNotification(gatt, char)
                            gatt.readCharacteristic(char)
                        }
                    }
                }
            }

            if (!hrFound) _isHeartRateSupported.value = false
            if (!spo2Found) _isSpo2Supported.value = false
            if (!bpFound) _isBloodPressureSupported.value = false
            if (!stepsFound) _isStepsSupported.value = false

            // Process queued descriptor writes sequentially
            processNextDescriptorWrite()
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            isWritingDescriptor = false
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "Descriptor write failed for ${descriptor?.uuid}: $status")
            }
            processNextDescriptorWrite()
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            characteristic ?: return
            if (characteristic.uuid == UUID_BATTERY_LEVEL) {
                val level = characteristic.value?.firstOrNull()?.toInt()?.and(0xFF)?.coerceIn(0, 100)
                _batteryLevel.value = level
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            characteristic ?: return
            handleCharacteristicUpdate(characteristic)
        }

        @Suppress("DEPRECATION")
        private fun handleCharacteristicUpdate(characteristic: BluetoothGattCharacteristic) {
            val data = characteristic.value ?: return
            if (data.isEmpty()) return
            _lastSyncTimestamp.value = System.currentTimeMillis()

            when (characteristic.uuid) {
                UUID_HEART_RATE_MEASUREMENT -> {
                    // Standard Bluetooth SIG Heart Rate Measurement Parsing (0x2A37)
                    val flag = data[0].toInt()
                    val is16Bit = (flag and 0x01) != 0
                    val hr = if (!is16Bit) {
                        data.getOrNull(1)?.toInt()?.and(0xFF) ?: 0
                    } else {
                        val b1 = data.getOrNull(1)?.toInt()?.and(0xFF) ?: 0
                        val b2 = data.getOrNull(2)?.toInt()?.and(0xFF) ?: 0
                        (b2 shl 8) or b1
                    }
                    if (hr in 35..240) {
                        _liveMetrics.value = _liveMetrics.value.copy(heartRate = hr)
                        onReadingReceivedListener?.invoke("heart_rate", hr.toFloat(), null, "$hr BPM", "bpm")
                    }
                }

                UUID_BLOOD_PRESSURE_MEASUREMENT -> {
                    // Standard Bluetooth SIG Blood Pressure Measurement Parsing (0x2A35)
                    val flag = data[0].toInt()
                    val isKpa = (flag and 0x01) != 0

                    if (data.size >= 5) {
                        val rawSys = parseSFloat(data[1], data[2])
                        val rawDia = parseSFloat(data[3], data[4])
                        val sys = (if (isKpa) rawSys * 7.50062f else rawSys).toInt()
                        val dia = (if (isKpa) rawDia * 7.50062f else rawDia).toInt()

                        if (sys in 50..260 && dia in 30..180 && sys > dia) {
                            _liveMetrics.value = _liveMetrics.value.copy(bloodPressure = Pair(sys, dia))
                            onReadingReceivedListener?.invoke("blood_pressure", sys.toFloat(), dia.toFloat(), "$sys/$dia mmHg", "mmHg")
                        }
                    }
                }

                UUID_SPO2_CONTINUOUS, UUID_SPO2_SPOT_CHECK -> {
                    // Standard Bluetooth SIG Pulse Oximeter Parsing (0x2A5F / 0x2A5E)
                    if (data.size >= 4) {
                        val rawSpo2 = parseSFloat(data[1], data[2]).toInt()
                        if (rawSpo2 in 65..100) {
                            _liveMetrics.value = _liveMetrics.value.copy(spo2 = rawSpo2)
                            onReadingReceivedListener?.invoke("spo2", rawSpo2.toFloat(), null, "$rawSpo2%", "%")
                        }
                    } else if (data.size >= 2) {
                        // Fallback for consumer finger oximeter raw percentage
                        val raw = data[1].toInt() and 0xFF
                        if (raw in 70..100) {
                            _liveMetrics.value = _liveMetrics.value.copy(spo2 = raw)
                            onReadingReceivedListener?.invoke("spo2", raw.toFloat(), null, "$raw%", "%")
                        }
                    }
                }

                UUID_RSC_MEASUREMENT -> {
                    // Standard RSC Measurement: speed, cadence, total distance
                    if (data.size >= 4) {
                        val flags = data[0].toInt()
                        val rawSpeed = ((data[2].toInt() and 0xFF) shl 8) or (data[1].toInt() and 0xFF)
                        val speedMps = rawSpeed / 256.0f
                        val cadence = data.getOrNull(3)?.toInt()?.and(0xFF) ?: 0
                        val estDist = (speedMps * 60f / 1000f)
                        if (estDist > 0f) {
                            val currentDist = _liveMetrics.value.distanceKm ?: 0f
                            _liveMetrics.value = _liveMetrics.value.copy(distanceKm = currentDist + estDist)
                        }
                    }
                }

                UUID_WEARABLE_REALTIME_STEPS -> {
                    // Wearable steps characteristic parsing
                    if (data.size >= 4) {
                        val steps = ((data[3].toInt() and 0xFF) shl 24) or
                                ((data[2].toInt() and 0xFF) shl 16) or
                                ((data[1].toInt() and 0xFF) shl 8) or
                                (data[0].toInt() and 0xFF)
                        if (steps > 0) {
                            _liveMetrics.value = _liveMetrics.value.copy(steps = steps)
                            onReadingReceivedListener?.invoke("steps", steps.toFloat(), null, "$steps steps", "steps")
                        }
                    }
                }

                UUID_BATTERY_LEVEL -> {
                    val level = data[0].toInt() and 0xFF
                    _batteryLevel.value = level.coerceIn(0, 100)
                }
            }
        }
    }

    /**
     * Parses an IEEE-11073 16-bit SFLOAT into a Float.
     */
    private fun parseSFloat(b0: Byte, b1: Byte): Float {
        val u0 = b0.toInt() and 0xFF
        val u1 = b1.toInt() and 0xFF
        var mantissa = (u1 and 0x0F shl 8) or u0
        if (mantissa >= 0x0800) mantissa -= 0x1000 // 12-bit signed conversion
        var exponent = u1 shr 4
        if (exponent >= 0x08) exponent -= 0x10 // 4-bit signed conversion
        return (mantissa * Math.pow(10.0, exponent.toDouble())).toFloat()
    }

    @SuppressLint("MissingPermission")
    private fun queueEnableNotification(gatt: BluetoothGatt, char: BluetoothGattCharacteristic) {
        gatt.setCharacteristicNotification(char, true)
        val desc = char.getDescriptor(UUID_CLIENT_CHARACTERISTIC_CONFIG) ?: return
        desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        descriptorWriteQueue.add(Pair(gatt, desc))
    }

    @SuppressLint("MissingPermission")
    private fun queueEnableIndication(gatt: BluetoothGatt, char: BluetoothGattCharacteristic) {
        gatt.setCharacteristicNotification(char, true)
        val desc = char.getDescriptor(UUID_CLIENT_CHARACTERISTIC_CONFIG) ?: return
        desc.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
        descriptorWriteQueue.add(Pair(gatt, desc))
    }

    @SuppressLint("MissingPermission")
    @Synchronized
    private fun processNextDescriptorWrite() {
        if (isWritingDescriptor || descriptorWriteQueue.isEmpty()) return
        val item = descriptorWriteQueue.poll() ?: return
        val gatt = item.first
        val descriptor = item.second
        isWritingDescriptor = true
        val success = gatt.writeDescriptor(descriptor)
        if (!success) {
            isWritingDescriptor = false
            processNextDescriptorWrite()
        }
    }

    // Health Connect Sync
    suspend fun syncHealthConnectData() {
        val client = healthConnectClient ?: return
        try {
            val now = Instant.now()
            val startOfDay = now.truncatedTo(ChronoUnit.DAYS)

            // 1. Read Steps
            try {
                val stepRequest = ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
                val stepResponse = client.readRecords(stepRequest)
                val totalSteps = stepResponse.records.sumOf { it.count.toInt() }
                if (totalSteps > 0) {
                    _liveMetrics.value = _liveMetrics.value.copy(steps = totalSteps)
                    _isStepsSupported.value = true
                    onReadingReceivedListener?.invoke("steps", totalSteps.toFloat(), null, "$totalSteps steps", "steps")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Health Connect steps read: ${e.message}")
            }

            // 2. Read Calories
            try {
                val calRequest = ReadRecordsRequest(
                    recordType = TotalCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
                val calResponse = client.readRecords(calRequest)
                val totalKcal = calResponse.records.sumOf { it.energy.inKilocalories.toInt() }
                if (totalKcal > 0) {
                    _liveMetrics.value = _liveMetrics.value.copy(calories = totalKcal)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Health Connect calories read: ${e.message}")
            }

            // 3. Read Distance
            try {
                val distRequest = ReadRecordsRequest(
                    recordType = DistanceRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
                val distResponse = client.readRecords(distRequest)
                val totalDistMeters = distResponse.records.sumOf { it.distance.inMeters }
                if (totalDistMeters > 0) {
                    val distKm = (totalDistMeters / 1000.0).toFloat()
                    _liveMetrics.value = _liveMetrics.value.copy(distanceKm = distKm)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Health Connect distance read: ${e.message}")
            }

            // 4. Read Heart Rate
            try {
                val hrRequest = ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(now.minus(4, ChronoUnit.HOURS), now)
                )
                val hrResponse = client.readRecords(hrRequest)
                val latestHrSample = hrResponse.records.flatMap { it.samples }.lastOrNull()
                if (latestHrSample != null && latestHrSample.beatsPerMinute > 0) {
                    val bpm = latestHrSample.beatsPerMinute.toInt()
                    _liveMetrics.value = _liveMetrics.value.copy(heartRate = bpm)
                    _isHeartRateSupported.value = true
                    onReadingReceivedListener?.invoke("heart_rate", bpm.toFloat(), null, "$bpm BPM", "bpm")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Health Connect heart rate read: ${e.message}")
            }

            // 5. Read SpO2
            try {
                val spo2Request = ReadRecordsRequest(
                    recordType = OxygenSaturationRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(now.minus(24, ChronoUnit.HOURS), now)
                )
                val spo2Response = client.readRecords(spo2Request)
                val latestSpo2 = spo2Response.records.lastOrNull()
                if (latestSpo2 != null && latestSpo2.percentage.value > 0) {
                    val pct = latestSpo2.percentage.value.toInt()
                    _liveMetrics.value = _liveMetrics.value.copy(spo2 = pct)
                    _isSpo2Supported.value = true
                    onReadingReceivedListener?.invoke("spo2", pct.toFloat(), null, "$pct%", "%")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Health Connect SpO2 read: ${e.message}")
            }

            // 6. Read Blood Pressure
            try {
                val bpRequest = ReadRecordsRequest(
                    recordType = BloodPressureRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(now.minus(24, ChronoUnit.HOURS), now)
                )
                val bpResponse = client.readRecords(bpRequest)
                val latestBp = bpResponse.records.lastOrNull()
                if (latestBp != null) {
                    val sys = latestBp.systolic.inMillimetersOfMercury.toInt()
                    val dia = latestBp.diastolic.inMillimetersOfMercury.toInt()
                    if (sys > 0 && dia > 0) {
                        _liveMetrics.value = _liveMetrics.value.copy(bloodPressure = Pair(sys, dia))
                        _isBloodPressureSupported.value = true
                        onReadingReceivedListener?.invoke("blood_pressure", sys.toFloat(), dia.toFloat(), "$sys/$dia mmHg", "mmHg")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Health Connect Blood Pressure read: ${e.message}")
            }

            // 7. Read Sleep
            try {
                val sleepRequest = ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(now.minus(24, ChronoUnit.HOURS), now)
                )
                val sleepResponse = client.readRecords(sleepRequest)
                val latestSleep = sleepResponse.records.lastOrNull()
                if (latestSleep != null) {
                    val durationMin = ChronoUnit.MINUTES.between(latestSleep.startTime, latestSleep.endTime).toInt()
                    if (durationMin > 0) {
                        _liveMetrics.value = _liveMetrics.value.copy(sleepMinutes = durationMin)
                        onReadingReceivedListener?.invoke("sleep", durationMin.toFloat(), null, "${durationMin / 60}h ${durationMin % 60}m", "minutes")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Health Connect sleep read: ${e.message}")
            }

            _lastSyncTimestamp.value = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e(TAG, "syncHealthConnectData exception: ${e.message}")
        }
    }
}
