package com.simats.vitalsync.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import com.simats.vitalsync.data.FitnessRepository
import com.simats.vitalsync.data.SleepRecord
import com.simats.vitalsync.data.model.vitals.LogSleepRequest
import com.simats.vitalsync.data.network.RetrofitClient
import com.simats.vitalsync.ui.theme.SleepColorStandards
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * State representing an active sleep session with timer, alarm, and snooze information.
 */
data class SleepSessionState(
    val isActive: Boolean = false,
    val sleepStartMillis: Long = 0L,
    val elapsedSeconds: Long = 0L,
    val targetWakeHour: Int = 6,
    val targetWakeMinute: Int = 30,
    val alarmEnabled: Boolean = true,
    val isAlarmRinging: Boolean = false,
    val isSnoozed: Boolean = false,
    val snoozeUntilMillis: Long = 0L,
    val snoozeCount: Int = 0,
    val lastCompletedRecord: SleepRecord? = null
)

/**
 * Singleton manager coordinating live sleep sessions, background timers,
 * AlarmManager scheduling, 2-minute snooze cycles, and session logging.
 */
class SleepSessionManager private constructor(private val appContext: Context) {

    companion object {
        private const val TAG = "SleepSessionManager"
        private const val PREFS_NAME = "vitalsync_sleep_session_prefs"

        const val ACTION_WAKE_UP_ALARM = "com.simats.vitalsync.ACTION_WAKE_UP_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.simats.vitalsync.ACTION_SNOOZE_ALARM"
        const val ACTION_TURN_OFF_ALARM = "com.simats.vitalsync.ACTION_TURN_OFF_ALARM"
        const val ACTION_STOP_SLEEP = "com.simats.vitalsync.ACTION_STOP_SLEEP"
        const val ACTION_BEDTIME_REMINDER = "com.simats.vitalsync.ACTION_BEDTIME_REMINDER"
        const val ACTION_OPEN_SLEEP = "com.simats.vitalsync.ACTION_OPEN_SLEEP"

        const val ALARM_REQ_CODE_WAKE = 7001
        const val ALARM_REQ_CODE_SNOOZE = 7002
        const val ALARM_REQ_CODE_BEDTIME = 7003

        private const val KEY_BEDTIME_REMINDER_ENABLED = "key_bedtime_reminder_enabled"
        private const val KEY_BEDTIME_HOUR = "key_bedtime_hour"
        private const val KEY_BEDTIME_MINUTE = "key_bedtime_minute"
        private const val KEY_BEDTIME_MODE = "key_bedtime_mode"

        // 2 Minutes Snooze in milliseconds
        const val SNOOZE_INTERVAL_MS = 2 * 60 * 1000L

        @Volatile
        private var instance: SleepSessionManager? = null

        fun getInstance(context: Context): SleepSessionManager {
            return instance ?: synchronized(this) {
                instance ?: SleepSessionManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.Default)
    private var timerJob: Job? = null

    private val _sessionState = MutableStateFlow(loadStateFromPrefs())
    val sessionState: StateFlow<SleepSessionState> = _sessionState.asStateFlow()

    init {
        if (_sessionState.value.isActive) {
            startTicker()
        }
    }

    private fun loadStateFromPrefs(): SleepSessionState {
        val isActive = prefs.getBoolean("key_is_active", false)
        val startMillis = prefs.getLong("key_start_millis", 0L)
        val wakeHour = prefs.getInt("key_wake_hour", 6)
        val wakeMinute = prefs.getInt("key_wake_minute", 30)
        val alarmEnabled = prefs.getBoolean("key_alarm_enabled", true)
        val isAlarmRinging = prefs.getBoolean("key_is_alarm_ringing", false)
        val isSnoozed = prefs.getBoolean("key_is_snoozed", false)
        val snoozeUntilMillis = prefs.getLong("key_snooze_until_millis", 0L)
        val snoozeCount = prefs.getInt("key_snooze_count", 0)

        val elapsed = if (isActive && startMillis > 0L) {
            (System.currentTimeMillis() - startMillis) / 1000L
        } else 0L

        return SleepSessionState(
            isActive = isActive,
            sleepStartMillis = startMillis,
            elapsedSeconds = elapsed.coerceAtLeast(0L),
            targetWakeHour = wakeHour,
            targetWakeMinute = wakeMinute,
            alarmEnabled = alarmEnabled,
            isAlarmRinging = isAlarmRinging,
            isSnoozed = isSnoozed,
            snoozeUntilMillis = snoozeUntilMillis,
            snoozeCount = snoozeCount
        )
    }

    private fun saveStateToPrefs(state: SleepSessionState) {
        prefs.edit().apply {
            putBoolean("key_is_active", state.isActive)
            putLong("key_start_millis", state.sleepStartMillis)
            putInt("key_wake_hour", state.targetWakeHour)
            putInt("key_wake_minute", state.targetWakeMinute)
            putBoolean("key_alarm_enabled", state.alarmEnabled)
            putBoolean("key_is_alarm_ringing", state.isAlarmRinging)
            putBoolean("key_is_snoozed", state.isSnoozed)
            putLong("key_snooze_until_millis", state.snoozeUntilMillis)
            putInt("key_snooze_count", state.snoozeCount)
            apply()
        }
    }

    private fun startTicker() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _sessionState.value.isActive) {
                val now = System.currentTimeMillis()
                val current = _sessionState.value
                val elapsed = ((now - current.sleepStartMillis) / 1000L).coerceAtLeast(0L)

                // Check if snooze timer reached
                var newlyRinging = current.isAlarmRinging
                var stillSnoozed = current.isSnoozed
                if (current.isSnoozed && current.snoozeUntilMillis in 1..now) {
                    newlyRinging = true
                    stillSnoozed = false
                    withContext(Dispatchers.Main) {
                        onAlarmFired(appContext)
                    }
                }

                _sessionState.update {
                    it.copy(
                        elapsedSeconds = elapsed,
                        isAlarmRinging = newlyRinging,
                        isSnoozed = stillSnoozed
                    )
                }
                delay(1000L)
            }
        }
    }

    /**
     * Starts sleep tracking session, schedules wake-up alarm, and starts persistent background service.
     */
    fun startSleepSession(
        context: Context,
        wakeHour: Int,
        wakeMinute: Int,
        alarmEnabled: Boolean = true,
        userId: Int? = null,
        userEmail: String? = null
    ) {
        val now = System.currentTimeMillis()
        val newState = SleepSessionState(
            isActive = true,
            sleepStartMillis = now,
            elapsedSeconds = 0L,
            targetWakeHour = wakeHour,
            targetWakeMinute = wakeMinute,
            alarmEnabled = alarmEnabled,
            isAlarmRinging = false,
            isSnoozed = false,
            snoozeUntilMillis = 0L,
            snoozeCount = 0,
            lastCompletedRecord = null
        )

        val sessionManager = com.simats.vitalsync.data.local.SessionManager(context)
        val resolvedId = userId ?: sessionManager.getUser()?.id?.takeIf { it > 0 }
        val resolvedEmail = userEmail?.ifBlank { null } ?: sessionManager.getUserEmail()

        // Store user credentials for background logging
        prefs.edit().apply {
            putInt("key_user_id", resolvedId ?: 0)
            putString("key_user_email", resolvedEmail ?: "")
            apply()
        }

        saveStateToPrefs(newState)
        _sessionState.value = newState
        startTicker()

        // Schedule wake alarm if enabled
        if (alarmEnabled) {
            val triggerMillis = computeNextWakeTimeMillis(wakeHour, wakeMinute)
            scheduleExactAlarm(context, triggerMillis, ACTION_WAKE_UP_ALARM, ALARM_REQ_CODE_WAKE)
            Log.d(TAG, "Wake-up alarm scheduled for $wakeHour:$wakeMinute (in ${(triggerMillis - now)/1000/60} mins)")
        } else {
            cancelPendingAlarms(context)
        }

        // Start Foreground Service for persistent notification
        SleepTrackerService.start(context)
    }

    /**
     * Early Wake ("Stop Sleep"): Stops tracking before alarm rings, cancels alarms, logs session.
     */
    fun stopSleepEarly(context: Context, onComplete: ((SleepRecord) -> Unit)? = null) {
        val current = _sessionState.value
        if (!current.isActive) return

        cancelPendingAlarms(context)
        SleepAlarmSoundPlayer.stop()
        SleepTrackerService.stop(context)

        finalizeAndSaveSession(context, onComplete)
    }

    /**
     * Alarm fired: starts alarm audio/vibration and triggers ringing state.
     */
    fun onAlarmFired(context: Context) {
        val current = _sessionState.value
        if (!current.isActive) return

        val ringingState = current.copy(
            isAlarmRinging = true,
            isSnoozed = false,
            snoozeUntilMillis = 0L
        )
        saveStateToPrefs(ringingState)
        _sessionState.value = ringingState

        SleepAlarmSoundPlayer.play(context)
        SleepTrackerService.showAlarmRingingNotification(context)
    }

    /**
     * Snooze (2 Minutes): silences current alarm, sets snooze timer, and reschedules in 2 minutes.
     */
    fun snoozeAlarm(context: Context) {
        val current = _sessionState.value
        if (!current.isActive) return

        SleepAlarmSoundPlayer.stop()
        val now = System.currentTimeMillis()
        val snoozeUntil = now + SNOOZE_INTERVAL_MS
        val newCount = current.snoozeCount + 1

        val snoozedState = current.copy(
            isAlarmRinging = false,
            isSnoozed = true,
            snoozeUntilMillis = snoozeUntil,
            snoozeCount = newCount
        )
        saveStateToPrefs(snoozedState)
        _sessionState.value = snoozedState

        // Schedule next snooze alarm
        scheduleExactAlarm(context, snoozeUntil, ACTION_SNOOZE_ALARM, ALARM_REQ_CODE_SNOOZE)
        SleepTrackerService.showSnoozedNotification(context, snoozeUntil)
        Log.d(TAG, "Alarm snoozed for 2 minutes (Snooze #$newCount)")
    }

    /**
     * Turn Off Alarm / Awake: Finalizes sleep session, cancels snooze, and saves sleep record.
     */
    fun turnOffAlarmAndLog(context: Context, onComplete: ((SleepRecord) -> Unit)? = null) {
        val current = _sessionState.value
        if (!current.isActive) return

        cancelPendingAlarms(context)
        SleepAlarmSoundPlayer.stop()
        SleepTrackerService.stop(context)

        finalizeAndSaveSession(context, onComplete)
    }

    private fun finalizeAndSaveSession(context: Context, onComplete: ((SleepRecord) -> Unit)?) {
        val startMillis = _sessionState.value.sleepStartMillis
        val endMillis = System.currentTimeMillis()

        // Duration in exact minutes (with minimum 1 minute guarantee for test/quick stops)
        val rawMinutes = ((endMillis - startMillis) / (1000 * 60)).toInt()
        val durationMinutes = if (rawMinutes <= 0) 1 else rawMinutes

        val sdfTime = SimpleDateFormat("hh:mm a", Locale.US)
        val formattedBedtime = sdfTime.format(startMillis)
        val formattedWakeTime = sdfTime.format(endMillis)

        val repository = FitnessRepository.getInstance(context)
        val todayStr = repository.getTodayDateString()

        val derivedScore = SleepColorStandards.deriveSleepScore(durationMinutes)
        val presentation = SleepColorStandards.getPresentation(durationMinutes)

        val completedRecord = SleepRecord(
            bedtime = formattedBedtime,
            wakeTime = formattedWakeTime,
            durationMinutes = durationMinutes,
            sleepScore = derivedScore,
            qualityLabel = presentation.qualityLabel,
            dateString = todayStr
        )

        // 1. Save locally to repository
        repository.saveSleepRecord(completedRecord)

        // 2. Sync to remote PHP/MySQL API in background
        val sessionManager = com.simats.vitalsync.data.local.SessionManager(context)
        val userId = prefs.getInt("key_user_id", 0).takeIf { it > 0 } ?: sessionManager.getUser()?.id?.takeIf { it > 0 }
        val userEmail = prefs.getString("key_user_email", "")?.ifBlank { null } ?: sessionManager.getUserEmail()

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.vitalsApiService.logSleep(
                        LogSleepRequest(
                            userId = userId,
                            email = userEmail?.ifBlank { null },
                            dateString = todayStr,
                            bedtime = formattedBedtime,
                            wakeTime = formattedWakeTime,
                            durationMinutes = durationMinutes,
                            sleepScore = derivedScore,
                            qualityLabel = presentation.qualityLabel
                        )
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Offline/Failed to sync sleep log to server: ${e.message}")
            }
        }

        // 3. Reset session state
        val resetState = SleepSessionState(
            isActive = false,
            sleepStartMillis = 0L,
            elapsedSeconds = 0L,
            targetWakeHour = _sessionState.value.targetWakeHour,
            targetWakeMinute = _sessionState.value.targetWakeMinute,
            alarmEnabled = _sessionState.value.alarmEnabled,
            isAlarmRinging = false,
            isSnoozed = false,
            snoozeUntilMillis = 0L,
            snoozeCount = 0,
            lastCompletedRecord = completedRecord
        )
        saveStateToPrefs(resetState)
        _sessionState.value = resetState
        timerJob?.cancel()

        onComplete?.invoke(completedRecord)
    }

    /**
     * Resets sleep record for the specified date (clears locally and in MySQL database).
     */
    fun resetSleepData(context: Context, dateStr: String, onComplete: (() -> Unit)? = null) {
        // Cancel active tracking if running
        cancelPendingAlarms(context)
        SleepAlarmSoundPlayer.stop()
        SleepTrackerService.stop(context)
        timerJob?.cancel()

        val resetState = SleepSessionState(
            isActive = false,
            sleepStartMillis = 0L,
            elapsedSeconds = 0L,
            targetWakeHour = _sessionState.value.targetWakeHour,
            targetWakeMinute = _sessionState.value.targetWakeMinute,
            alarmEnabled = _sessionState.value.alarmEnabled,
            isAlarmRinging = false,
            isSnoozed = false,
            snoozeUntilMillis = 0L,
            snoozeCount = 0,
            lastCompletedRecord = null
        )
        saveStateToPrefs(resetState)
        _sessionState.value = resetState

        // 1. Clear locally
        FitnessRepository.getInstance(context).clearSleepRecord(dateStr)

        // 2. Reset remotely in MySQL
        val sessionManager = com.simats.vitalsync.data.local.SessionManager(context)
        val userId = prefs.getInt("key_user_id", 0).takeIf { it > 0 } ?: sessionManager.getUser()?.id?.takeIf { it > 0 }
        val userEmail = prefs.getString("key_user_email", "")?.ifBlank { null } ?: sessionManager.getUserEmail()

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.vitalsApiService.resetSleep(
                        LogSleepRequest(
                            userId = userId,
                            email = userEmail?.ifBlank { null },
                            dateString = dateStr,
                            bedtime = "",
                            wakeTime = "",
                            durationMinutes = 0,
                            sleepScore = 0,
                            qualityLabel = "No record",
                            action = "reset"
                        )
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Offline/Failed to reset sleep on server: ${e.message}")
            }
        }

        onComplete?.invoke()
    }

    private fun scheduleExactAlarm(context: Context, triggerMillis: Long, action: String, reqCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, SleepAlarmReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException scheduling exact alarm, falling back to setAndAllowWhileIdle: ${e.message}")
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Fatal failure scheduling alarm: ${ex.message}", ex)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm: ${e.message}", e)
        }
    }

    fun cancelPendingAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val wakeIntent = Intent(context, SleepAlarmReceiver::class.java).apply { action = ACTION_WAKE_UP_ALARM }
        val wakePending = PendingIntent.getBroadcast(context, ALARM_REQ_CODE_WAKE, wakeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(wakePending)
        wakePending.cancel()

        val snoozeIntent = Intent(context, SleepAlarmReceiver::class.java).apply { action = ACTION_SNOOZE_ALARM }
        val snoozePending = PendingIntent.getBroadcast(context, ALARM_REQ_CODE_SNOOZE, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(snoozePending)
        snoozePending.cancel()

        Log.d(TAG, "Cancelled all pending wake and snooze alarms.")
    }

    private fun computeNextWakeTimeMillis(hour: Int, minute: Int): Long {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If the time is already past today, advance to next day
            if (timeInMillis <= now) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return calendar.timeInMillis
    }

    // -------------------------------------------------------------
    // BEDTIME REMINDER & WIND-DOWN SCHEDULER
    // -------------------------------------------------------------
    fun isBedtimeReminderEnabled(): Boolean = prefs.getBoolean(KEY_BEDTIME_REMINDER_ENABLED, false)
    fun getBedtimeReminderHour(): Int = prefs.getInt(KEY_BEDTIME_HOUR, 22)
    fun getBedtimeReminderMinute(): Int = prefs.getInt(KEY_BEDTIME_MINUTE, 30)
    fun getBedtimeReminderMode(): String = prefs.getString(KEY_BEDTIME_MODE, "notification") ?: "notification"

    fun saveBedtimeReminder(context: Context, enabled: Boolean, hour: Int, minute: Int, mode: String) {
        prefs.edit().apply {
            putBoolean(KEY_BEDTIME_REMINDER_ENABLED, enabled)
            putInt(KEY_BEDTIME_HOUR, hour)
            putInt(KEY_BEDTIME_MINUTE, minute)
            putString(KEY_BEDTIME_MODE, mode)
            apply()
        }

        if (enabled) {
            val triggerMillis = computeNextBedtimeMillis(hour, minute)
            scheduleExactAlarm(context, triggerMillis, ACTION_BEDTIME_REMINDER, ALARM_REQ_CODE_BEDTIME)
            Log.d(TAG, "Scheduled Bedtime Reminder for $hour:$minute (mode: $mode)")
        } else {
            cancelBedtimeAlarm(context)
            Log.d(TAG, "Cancelled Bedtime Reminder.")
        }
    }

    fun cancelBedtimeAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, SleepAlarmReceiver::class.java).apply { action = ACTION_BEDTIME_REMINDER }
        val pending = PendingIntent.getBroadcast(context, ALARM_REQ_CODE_BEDTIME, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pending)
        pending.cancel()
    }

    fun computeNextBedtimeMillis(hour: Int, minute: Int): Long {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return calendar.timeInMillis
    }

    fun onBedtimeReminderTriggered(context: Context) {
        val mode = getBedtimeReminderMode()
        val hour = getBedtimeReminderHour()
        val minute = getBedtimeReminderMinute()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val timeFmt = SimpleDateFormat("hh:mm a", Locale.US).format(cal.time)

        // Post notification
        NotificationReminderEngine.postNotification(
            context = context,
            notificationId = NotificationReminderEngine.NOTIF_ID_SLEEP,
            title = "🌙 Bedtime Reminder",
            message = "It's $timeFmt! Time to wind down and get ready for restful sleep."
        )

        if (mode == "alarm") {
            SleepAlarmSoundPlayer.play(context)
        }

        // Reschedule for next day if still enabled
        if (isBedtimeReminderEnabled()) {
            val nextTrigger = computeNextBedtimeMillis(hour, minute)
            scheduleExactAlarm(context, nextTrigger, ACTION_BEDTIME_REMINDER, ALARM_REQ_CODE_BEDTIME)
        }
    }
}
