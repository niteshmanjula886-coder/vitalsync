package com.simats.vitalsync.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.simats.vitalsync.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SleepTrackerService : Service() {

    companion object {
        const val TRACKER_CHANNEL_ID = "vitalsync_sleep_tracker_channel"
        const val ALARM_CHANNEL_ID = "vitalsync_sleep_alarm_channel"
        const val NOTIFICATION_ID = 4001

        fun start(context: Context) {
            val intent = Intent(context, SleepTrackerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, SleepTrackerService::class.java)
            context.stopService(intent)
        }

        fun showAlarmRingingNotification(context: Context) {
            val intent = Intent(context, SleepTrackerService::class.java).apply {
                putExtra("action_type", "ALARM_RINGING")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun showSnoozedNotification(context: Context, snoozeUntilMillis: Long) {
            val intent = Intent(context, SleepTrackerService::class.java).apply {
                putExtra("action_type", "SNOOZED")
                putExtra("snooze_until", snoozeUntilMillis)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.Main)
    private var stateObserverJob: Job? = null
    private var periodicUpdateJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sessionManager = SleepSessionManager.getInstance(this)
        val state = sessionManager.sessionState.value

        if (!state.isActive) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        // Display current notification and start foreground
        val notification = buildCurrentNotification(state)
        startForeground(NOTIFICATION_ID, notification)

        // Observe session state reactively
        stateObserverJob?.cancel()
        stateObserverJob = scope.launch {
            sessionManager.sessionState.collectLatest { s ->
                if (!s.isActive) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    nm?.notify(NOTIFICATION_ID, buildCurrentNotification(s))
                }
            }
        }

        // Periodic update every 60 seconds for live elapsed minutes
        periodicUpdateJob?.cancel()
        periodicUpdateJob = scope.launch {
            while (isActive) {
                delay(60_000L)
                val current = sessionManager.sessionState.value
                if (current.isActive && !current.isAlarmRinging) {
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    nm?.notify(NOTIFICATION_ID, buildCurrentNotification(current))
                }
            }
        }

        return START_STICKY
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            // 1. Silent ongoing tracker channel
            val trackerChannel = NotificationChannel(
                TRACKER_CHANNEL_ID,
                "Sleep Tracker",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing persistent sleep tracker notification"
                setShowBadge(false)
            }
            manager.createNotificationChannel(trackerChannel)

            // 2. High priority alarm channel
            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Wake-Up Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority wake-up and snooze alarm alerts"
                enableLights(true)
                enableVibration(true)
                setBypassDnd(true)
            }
            manager.createNotificationChannel(alarmChannel)
        }
    }

    private fun buildCurrentNotification(state: SleepSessionState): Notification {
        val openAppPendingIntent = createOpenAppPendingIntent()

        return if (state.isAlarmRinging) {
            buildAlarmRingingNotification(openAppPendingIntent)
        } else if (state.isSnoozed) {
            buildSnoozedNotification(state, openAppPendingIntent)
        } else {
            buildTrackingNotification(state, openAppPendingIntent)
        }
    }

    private fun buildTrackingNotification(state: SleepSessionState, openAppIntent: PendingIntent): Notification {
        val wakeFmt = formatTime(state.targetWakeHour, state.targetWakeMinute)
        val alarmInfo = if (state.alarmEnabled) "Alarm set for $wakeFmt" else "Alarm: Off"

        val elapsedHours = state.elapsedSeconds / 3600
        val elapsedMins = (state.elapsedSeconds % 3600) / 60
        val elapsedStr = "${elapsedHours}h ${elapsedMins}m elapsed"

        val contentText = "VitalSync sleep tracker is running • $alarmInfo ($elapsedStr)"

        val stopIntent = Intent(this, SleepAlarmReceiver::class.java).apply {
            action = SleepSessionManager.ACTION_STOP_SLEEP
        }
        val stopPending = PendingIntent.getBroadcast(
            this,
            8001,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, TRACKER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("VitalSync Sleep Tracker")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openAppIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Sleep", stopPending)
            .build()
    }

    private fun buildSnoozedNotification(state: SleepSessionState, openAppIntent: PendingIntent): Notification {
        val remainingMs = (state.snoozeUntilMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        val remMins = (remainingMs / 1000) / 60
        val remSecs = (remainingMs / 1000) % 60
        val remStr = String.format(Locale.US, "%02d:%02d", remMins, remSecs)

        val text = "Snoozed for 2 min (Rings in $remStr) • VitalSync sleep tracker is running"

        val turnOffIntent = Intent(this, SleepAlarmReceiver::class.java).apply {
            action = SleepSessionManager.ACTION_TURN_OFF_ALARM
        }
        val turnOffPending = PendingIntent.getBroadcast(
            this,
            8002,
            turnOffIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, TRACKER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Sleep Alarm Snoozed")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openAppIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Turn Off / Awake", turnOffPending)
            .build()
    }

    private fun buildAlarmRingingNotification(openAppIntent: PendingIntent): Notification {
        val snoozeIntent = Intent(this, SleepAlarmReceiver::class.java).apply {
            action = SleepSessionManager.ACTION_SNOOZE_ALARM
        }
        val snoozePending = PendingIntent.getBroadcast(
            this,
            8003,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val turnOffIntent = Intent(this, SleepAlarmReceiver::class.java).apply {
            action = SleepSessionManager.ACTION_TURN_OFF_ALARM
        }
        val turnOffPending = PendingIntent.getBroadcast(
            this,
            8004,
            turnOffIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Wake Up! Alarm is Ringing")
            .setContentText("Good morning! Time to wake up and start your day.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Good morning! Time to wake up and start your day."))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setContentIntent(openAppIntent)
            .setFullScreenIntent(openAppIntent, true)
            .addAction(android.R.drawable.ic_media_pause, "Snooze (2 min)", snoozePending)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Turn Off / Awake", turnOffPending)
            .build()
    }

    private fun createOpenAppPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "sleep")
            action = SleepSessionManager.ACTION_OPEN_SLEEP
        }
        return PendingIntent.getActivity(
            this,
            8000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        return SimpleDateFormat("hh:mm a", Locale.US).format(cal.time)
    }

    override fun onDestroy() {
        super.onDestroy()
        stateObserverJob?.cancel()
        periodicUpdateJob?.cancel()
    }
}
