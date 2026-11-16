package com.simats.vitalsync.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.simats.vitalsync.data.local.SessionManager

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return
        val alarmId = intent.getIntExtra("alarm_id", 0)
        val title = intent.getStringExtra("title") ?: "VitalSync Reminder"
        val message = intent.getStringExtra("message") ?: "Time to check your health goals!"

        Log.d("ReminderReceiver", "1-Hour recurring alarm triggered: $action, id: $alarmId")

        // 1. Post notification
        NotificationReminderEngine.postNotification(
            context = context,
            notificationId = alarmId,
            title = title,
            message = message
        )

        // 2. Reschedule for the next interval according to active reminder mode (HOURLY or MANUAL)
        val sessionManager = SessionManager.getInstance(context)
        when (action) {
            NotificationReminderEngine.ACTION_HYDRATION_REMINDER -> {
                if (sessionManager.isHydrationReminderEnabled()) {
                    NotificationReminderEngine.syncHydrationReminder(
                        context = context,
                        enabled = true,
                        mode = sessionManager.getHydrationReminderMode(),
                        timeStr = sessionManager.getHydrationReminderTime()
                    )
                }
            }
            NotificationReminderEngine.ACTION_STEPS_REMINDER -> {
                if (sessionManager.isStepsReminderEnabled()) {
                    NotificationReminderEngine.syncStepsReminder(
                        context = context,
                        enabled = true,
                        mode = sessionManager.getStepsReminderMode(),
                        timeStr = sessionManager.getStepsReminderTime()
                    )
                }
            }
        }
    }
}
