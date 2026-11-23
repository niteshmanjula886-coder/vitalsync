package com.simats.vitalsync.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.simats.vitalsync.MainActivity
import com.simats.vitalsync.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object NotificationReminderEngine {

    private const val TAG = "NotificationReminder"

    const val CHANNEL_ID = "vitalsync_reminders"
    private const val CHANNEL_NAME = "VitalSync Reminders"
    private const val CHANNEL_DESC = "Hydration, steps, workout, and sleep health reminders"

    const val ACTION_HYDRATION_REMINDER = "com.simats.vitalsync.ACTION_HYDRATION_REMINDER"
    const val ACTION_STEPS_REMINDER = "com.simats.vitalsync.ACTION_STEPS_REMINDER"

    const val NOTIF_ID_HYDRATION = 1001
    const val NOTIF_ID_WORKOUT = 1002
    const val NOTIF_ID_SLEEP = 1003
    const val NOTIF_ID_STEPS = 1004

    // 1-Hour recurring interval (60 minutes = 3,600,000 milliseconds)
    const val ONE_HOUR_INTERVAL_MS = 60 * 60 * 1000L

    fun init(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    /**
     * Schedules or cancels hydration reminders supporting both HOURLY and MANUAL modes.
     */
    fun syncHydrationReminder(
        context: Context,
        enabled: Boolean,
        mode: String = "HOURLY",
        timeStr: String = "09:00 AM"
    ) {
        init(context)
        cancelAlarm(context, NOTIF_ID_HYDRATION, ACTION_HYDRATION_REMINDER)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (!enabled) {
            manager?.cancel(NOTIF_ID_HYDRATION)
            Log.d(TAG, "Hydration reminder disabled and cancelled.")
            return
        }

        if (mode.equals("MANUAL", ignoreCase = true)) {
            val (h, m) = parseHourMinute(timeStr)
            scheduleExactDailyAlarm(
                context = context,
                alarmId = NOTIF_ID_HYDRATION,
                action = ACTION_HYDRATION_REMINDER,
                hour = h,
                minute = m,
                title = "Hydration Reminder",
                message = "Time for a fresh glass of water! Drink up to keep your body energized and hydrated."
            )
            Log.d(TAG, "Hydration manual reminder scheduled for $timeStr.")
        } else {
            scheduleRecurringAlarm(
                context = context,
                alarmId = NOTIF_ID_HYDRATION,
                action = ACTION_HYDRATION_REMINDER,
                intervalMs = ONE_HOUR_INTERVAL_MS,
                title = "Hydration Reminder",
                message = "Time for a fresh glass of water! Drink up to keep your body energized and hydrated."
            )
            Log.d(TAG, "Hydration hourly reminder scheduled (every 60 minutes).")
        }
    }

    /**
     * Schedules or cancels steps reminders supporting both HOURLY and MANUAL modes.
     */
    fun syncStepsReminder(
        context: Context,
        enabled: Boolean,
        mode: String = "HOURLY",
        timeStr: String = "07:00 PM"
    ) {
        init(context)
        cancelAlarm(context, NOTIF_ID_STEPS, ACTION_STEPS_REMINDER)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (!enabled) {
            manager?.cancel(NOTIF_ID_STEPS)
            Log.d(TAG, "Steps reminder disabled and cancelled.")
            return
        }

        if (mode.equals("MANUAL", ignoreCase = true)) {
            val (h, m) = parseHourMinute(timeStr)
            scheduleExactDailyAlarm(
                context = context,
                alarmId = NOTIF_ID_STEPS,
                action = ACTION_STEPS_REMINDER,
                hour = h,
                minute = m,
                title = "Steps Reminder",
                message = "Time to get moving! Take a short walk and stay on track for your daily step goal."
            )
            Log.d(TAG, "Steps manual reminder scheduled for $timeStr.")
        } else {
            scheduleRecurringAlarm(
                context = context,
                alarmId = NOTIF_ID_STEPS,
                action = ACTION_STEPS_REMINDER,
                intervalMs = ONE_HOUR_INTERVAL_MS,
                title = "Steps Reminder",
                message = "Time to get moving! Take a short walk and stay on track for your daily step goal."
            )
            Log.d(TAG, "Steps hourly reminder scheduled (every 60 minutes).")
        }
    }

    /**
     * Schedules or cancels the 1-hour recurring hydration reminder.
     */
    fun scheduleHydrationHourlyReminder(context: Context, enabled: Boolean) {
        syncHydrationReminder(context, enabled, mode = "HOURLY")
    }

    /**
     * Schedules or cancels the 1-hour recurring daily step goal reminder.
     */
    fun scheduleStepsHourlyReminder(context: Context, enabled: Boolean) {
        syncStepsReminder(context, enabled, mode = "HOURLY")
    }

    // Backward-compatible delegates
    fun scheduleHydrationReminder(context: Context, enabled: Boolean, timeStr: String = "") {
        val mode = if (timeStr.isNotBlank()) "MANUAL" else "HOURLY"
        val time = if (timeStr.isNotBlank()) timeStr else "09:00 AM"
        syncHydrationReminder(context, enabled, mode, time)
    }

    fun scheduleStepsReminder(context: Context, enabled: Boolean, timeStr: String = "") {
        val mode = if (timeStr.isNotBlank()) "MANUAL" else "HOURLY"
        val time = if (timeStr.isNotBlank()) timeStr else "07:00 PM"
        syncStepsReminder(context, enabled, mode, time)
    }

    private fun scheduleExactDailyAlarm(
        context: Context,
        alarmId: Int,
        action: String,
        hour: Int,
        minute: Int,
        title: String,
        message: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            this.action = action
            putExtra("alarm_id", alarmId)
            putExtra("title", title)
            putExtra("message", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm restricted, using inexact: ${e.message}")
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule exact daily alarm: ${e.message}", e)
        }
    }

    private fun scheduleRecurringAlarm(
        context: Context,
        alarmId: Int,
        action: String,
        intervalMs: Long,
        title: String,
        message: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            this.action = action
            putExtra("alarm_id", alarmId)
            putExtra("title", title)
            putExtra("message", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + intervalMs

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    intervalMs,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission restricted, falling back to inexact repeating: ${e.message}")
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                intervalMs,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule recurring alarm: ${e.message}", e)
        }
    }

    private fun cancelAlarm(context: Context, alarmId: Int, action: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cancel alarm: ${e.message}")
        }
    }

    fun parseHourMinute(timeStr: String): Pair<Int, Int> {
        val trimmed = timeStr.trim()
        try {
            val sdf12 = SimpleDateFormat("hh:mm a", Locale.US)
            val date = sdf12.parse(trimmed)
            if (date != null) {
                val cal = Calendar.getInstance().apply { time = date }
                return Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
            }
        } catch (_: Exception) {}

        try {
            val sdf24 = SimpleDateFormat("HH:mm", Locale.US)
            val date = sdf24.parse(trimmed)
            if (date != null) {
                val cal = Calendar.getInstance().apply { time = date }
                return Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
            }
        } catch (_: Exception) {}

        return Pair(9, 0)
    }

    fun formatHourMinute(hour: Int, minute: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        return SimpleDateFormat("hh:mm a", Locale.US).format(cal.time)
    }

    fun syncReminders(
        context: Context,
        dailyRemindersEnabled: Boolean,
        waterIntervalMinutes: Int = 120,
        workoutReminderEnabled: Boolean = true,
        sleepReminderEnabled: Boolean = true
    ) {
        init(context)
        if (!dailyRemindersEnabled) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.cancel(NOTIF_ID_HYDRATION)
            manager?.cancel(NOTIF_ID_WORKOUT)
            manager?.cancel(NOTIF_ID_SLEEP)
            manager?.cancel(NOTIF_ID_STEPS)
            return
        }
    }

    fun postNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        try {
            init(context)
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_vitalsync)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            if (notificationId == NOTIF_ID_SLEEP) {
                val stopIntent = Intent(context, SleepAlarmReceiver::class.java).apply {
                    action = SleepSessionManager.ACTION_STOP_BEDTIME
                }
                val stopPending = PendingIntent.getBroadcast(
                    context,
                    SleepSessionManager.ALARM_REQ_CODE_BEDTIME + 10,
                    stopIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Bedtime", stopPending)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify(notificationId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
