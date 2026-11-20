package com.simats.vitalsync.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class SleepAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return
        Log.d("SleepAlarmReceiver", "Received action: $action")

        val sessionManager = SleepSessionManager.getInstance(context)

        when (action) {
            SleepSessionManager.ACTION_WAKE_UP_ALARM -> {
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
        }
    }
}
