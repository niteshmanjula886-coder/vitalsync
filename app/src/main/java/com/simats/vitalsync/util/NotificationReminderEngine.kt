package com.simats.vitalsync.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.simats.vitalsync.MainActivity

object NotificationReminderEngine {

    const val CHANNEL_ID = "vitalsync_reminders"
    private const val CHANNEL_NAME = "VitalSync Reminders"
    private const val CHANNEL_DESC = "Hydration, workout, and sleep health reminders"

    const val NOTIF_ID_HYDRATION = 1001
    const val NOTIF_ID_WORKOUT = 1002
    const val NOTIF_ID_SLEEP = 1003

    fun init(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
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
            return
        }

        // Reminders are active; ensure notification channel is registered
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
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify(notificationId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

