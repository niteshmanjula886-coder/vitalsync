package com.simats.vitalsync.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.simats.vitalsync.MainActivity
import com.simats.vitalsync.R
import com.simats.vitalsync.data.device.DeviceConnectionStatus
import com.simats.vitalsync.data.repository.HealthDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * BleHealthTrackingService:
 * Foreground Service that maintains continuous Bluetooth Low Energy health tracking
 * when the app is in the background or screen is off.
 */
class BleHealthTrackingService : Service() {

    companion object {
        const val CHANNEL_ID = "vitalsync_ble_monitoring_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_START = "com.simats.vitalsync.action.START_BLE_TRACKING"
        const val ACTION_STOP = "com.simats.vitalsync.action.STOP_BLE_TRACKING"

        fun start(context: Context) {
            val intent = Intent(context, BleHealthTrackingService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, BleHealthTrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val initialNotification = buildNotification("Monitoring health sensors", "Connecting...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                initialNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        val repo = HealthDataRepository.getInstance(applicationContext)
        observerJob?.cancel()
        observerJob = serviceScope.launch {
            launch {
                repo.connectionStatus.collect { status ->
                    val devName = repo.connectedDeviceName.value ?: "Health Device"
                    val content = when (status) {
                        DeviceConnectionStatus.CONNECTED -> "Connected to $devName"
                        DeviceConnectionStatus.CONNECTING -> "Connecting to $devName..."
                        DeviceConnectionStatus.SCANNING -> "Scanning for nearby devices..."
                        DeviceConnectionStatus.DISCONNECTED_UNEXPECTED -> "Device disconnected. Reconnecting..."
                        else -> "Waiting for connection"
                    }
                    updateNotification("VitalSync Health Tracking", content)
                }
            }

            launch {
                repo.liveMetrics.collect { metrics ->
                    val hr = metrics.heartRate
                    val spo2 = metrics.spo2
                    val steps = metrics.steps
                    val devName = repo.connectedDeviceName.value ?: "Device"

                    val vitalsParts = mutableListOf<String>()
                    if (hr != null && hr > 0) vitalsParts.add("$hr BPM")
                    if (spo2 != null && spo2 > 0) vitalsParts.add("$spo2% SpO₂")
                    if (steps != null && steps > 0) vitalsParts.add("$steps steps")

                    val text = if (vitalsParts.isNotEmpty()) {
                        "$devName • ${vitalsParts.joinToString(" • ")}"
                    } else {
                        "Connected to $devName"
                    }

                    if (repo.connectionStatus.value == DeviceConnectionStatus.CONNECTED) {
                        updateNotification("VitalSync Live Health Monitor", text)
                    }
                }
            }
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = IntentIntentFlags()
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun IntentIntentFlags(): Int {
        return Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    private fun updateNotification(title: String, content: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm?.notify(NOTIFICATION_ID, buildNotification(title, content))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VitalSync Bluetooth Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live health vitals received from Bluetooth devices."
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        observerJob?.cancel()
        serviceScope.cancel()
    }
}

