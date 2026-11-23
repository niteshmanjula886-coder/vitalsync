package com.simats.vitalsync.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log

class SleepAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return
        Log.d("SleepAlarmReceiver", "Received action: $action")

        val sessionManager = SleepSessionManager.getInstance(context)

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.d("SleepAlarmReceiver", "Device boot or package update detected: restoring active alarms.")
                sessionManager.rescheduleAllActiveAlarms(context)
            }
            SleepSessionManager.ACTION_WAKE_UP_ALARM,
            SleepSessionManager.ACTION_SNOOZE_ALARM_FIRED -> {
                // Acquire temporary wake lock to keep CPU awake while playing sound & posting notification
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                val wakeLock = powerManager?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "vitalsync:SleepAlarmWakeLock"
                )
                try {
                    wakeLock?.acquire(60_000L) // 60 seconds
                } catch (e: Exception) {
                    Log.e("SleepAlarmReceiver", "Error acquiring wake lock: ${e.message}")
                }
                sessionManager.onAlarmFired(context)
            }
            SleepSessionManager.ACTION_SNOOZE_ALARM -> {
                sessionManager.snoozeAlarm(context)
            }
            SleepSessionManager.ACTION_TURN_OFF_ALARM -> {
                sessionManager.turnOffAlarmAndLog(context)
            }
            SleepSessionManager.ACTION_STOP_SLEEP -> {
                sessionManager.stopSleepEarly(context)
            }
            SleepSessionManager.ACTION_BEDTIME_REMINDER -> {
                sessionManager.onBedtimeReminderTriggered(context)
            }
            SleepSessionManager.ACTION_STOP_BEDTIME -> {
                sessionManager.stopBedtimeAlert(context)
            }
        }
    }
}
