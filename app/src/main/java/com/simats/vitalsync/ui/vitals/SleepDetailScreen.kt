package com.simats.vitalsync.ui.vitals

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import com.simats.vitalsync.data.FitnessRepository
import com.simats.vitalsync.data.SleepRecord
import com.simats.vitalsync.data.local.SessionManager
import com.simats.vitalsync.data.model.vitals.LogSleepRequest
import com.simats.vitalsync.data.model.vitals.SleepLogEntryDto
import com.simats.vitalsync.data.network.RetrofitClient
import com.simats.vitalsync.util.SleepSessionManager
import com.simats.vitalsync.ui.theme.CarbonDarkBackground
import com.simats.vitalsync.ui.theme.CarbonDarkBorder
import com.simats.vitalsync.ui.theme.CarbonDarkPurpleAccent
import com.simats.vitalsync.ui.theme.CarbonDarkPurpleBorder
import com.simats.vitalsync.ui.theme.CarbonDarkPurplePill
import com.simats.vitalsync.ui.theme.CarbonDarkPurpleSurface
import com.simats.vitalsync.ui.theme.CarbonDarkPurpleText
import com.simats.vitalsync.ui.theme.CarbonDarkSurface
import com.simats.vitalsync.ui.theme.CarbonDarkSurfaceVariant
import com.simats.vitalsync.ui.theme.CarbonDarkTextPrimary
import com.simats.vitalsync.ui.theme.CarbonDarkTextSecondary
import com.simats.vitalsync.ui.theme.SleepColorStandards
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun SleepDetailScreen(
    modifier: Modifier = Modifier,
    userId: Int? = null,
    userEmail: String = "",
    targetSleepMinutes: Int = 480, // Default 8 Hours Daily Goal
    showAiInsights: Boolean = true,
    onSleepLogged: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { FitnessRepository.getInstance(context) }
    val sessionManager = remember { SessionManager.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    val todayDate = remember { repository.getTodayDateString() }
    val resolvedUserId = remember(userId) {
        userId?.takeIf { it > 0 } ?: sessionManager.getUser()?.id?.takeIf { it > 0 }
    }
    val resolvedEmail = remember(userEmail) {
        userEmail.ifBlank { sessionManager.getUserEmail() ?: "" }
    }

    // Time Pickers State (Default: Bedtime 10:30 PM [22:30], Wake 06:30 AM [06:30])
    var bedHour by remember { mutableIntStateOf(22) }
    var bedMinute by remember { mutableIntStateOf(30) }

    var wakeHour by remember { mutableIntStateOf(6) }
    var wakeMinute by remember { mutableIntStateOf(30) }

    // Live Sleep Tracking Session Manager & State
    val sleepSessionManager = remember { SleepSessionManager.getInstance(context) }
    val liveSessionState by sleepSessionManager.sessionState.collectAsState()
    var liveAlarmEnabled by remember { mutableStateOf(liveSessionState.alarmEnabled) }

    // Dynamic timer tick for updating countdowns
    var currentTick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(liveAlarmEnabled, liveSessionState.isActive) {
        while (true) {
            delay(15_000L)
            currentTick = System.currentTimeMillis()
        }
    }

    // Keep local picker states synced with active session
    LaunchedEffect(liveSessionState.targetWakeHour, liveSessionState.targetWakeMinute, liveSessionState.alarmEnabled) {
        wakeHour = liveSessionState.targetWakeHour
        wakeMinute = liveSessionState.targetWakeMinute
        liveAlarmEnabled = liveSessionState.alarmEnabled
    }

    var isRecordedSaved by remember { mutableStateOf(false) }
    var loggedDurationMinutes by remember { mutableIntStateOf(0) }
    var loggedBedtime by remember { mutableStateOf<String?>(null) }
    var loggedWakeTime by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var isSavingRemote by remember { mutableStateOf(false) }
    var showSuccessToast by remember { mutableStateOf(false) }
    var sleepHistory by remember { mutableStateOf<List<SleepLogEntryDto>>(emptyList()) }

    // Bedtime Reminder settings (stored in SleepSessionManager)
    var bedtimeReminderEnabled by remember { mutableStateOf(sleepSessionManager.isBedtimeReminderEnabled()) }
    var bedtimeHour by remember { mutableIntStateOf(sleepSessionManager.getBedtimeReminderHour()) }
    var bedtimeMinute by remember { mutableIntStateOf(sleepSessionManager.getBedtimeReminderMinute()) }
    var bedtimeReminderMode by remember { mutableStateOf(sleepSessionManager.getBedtimeReminderMode()) }

    // When background sleep session completes, update local UI state immediately
    LaunchedEffect(liveSessionState.lastCompletedRecord) {
        liveSessionState.lastCompletedRecord?.let { record ->
            isRecordedSaved = true
            loggedDurationMinutes = record.durationMinutes
            loggedBedtime = record.bedtime
            loggedWakeTime = record.wakeTime
            parseFormattedTimeToHourMinute(record.bedtime)?.let { (h, m) ->
                bedHour = h
                bedMinute = m
            }
            parseFormattedTimeToHourMinute(record.wakeTime)?.let { (h, m) ->
                wakeHour = h
                wakeMinute = m
            }
            showSuccessToast = true
            sleepHistory = (listOf(
                SleepLogEntryDto(
                    date = record.dateString,
                    durationMinutes = record.durationMinutes,
                    sleepScore = record.sleepScore,
                    quality = record.qualityLabel
                )
            ) + sleepHistory.filterNot { it.date == record.dateString }).take(7)
            onSleepLogged()
        }
    }

    fun buildNormalizedLocalHistory(): List<SleepLogEntryDto> {
        return repository.getRecentSleepRecords(limit = 7).map { rec ->
            val pres = SleepColorStandards.getPresentation(rec.durationMinutes)
            val score = if (rec.sleepScore > 0) rec.sleepScore else SleepColorStandards.deriveSleepScore(rec.durationMinutes)
            SleepLogEntryDto(
                date = rec.dateString,
                durationMinutes = rec.durationMinutes,
                sleepScore = score,
                quality = pres.qualityLabel
            )
        }
    }

    // Load live sleep logs from backend (falling back cleanly to local records if offline)
    LaunchedEffect(todayDate, resolvedUserId, resolvedEmail) {
        val localToday = repository.getSleepRecord(todayDate)
        if (localToday != null && localToday.durationMinutes > 0) {
            isRecordedSaved = true
            loggedDurationMinutes = localToday.durationMinutes
            loggedBedtime = localToday.bedtime
            loggedWakeTime = localToday.wakeTime
            parseFormattedTimeToHourMinute(localToday.bedtime)?.let { (h, m) ->
                bedHour = h
                bedMinute = m
            }
            parseFormattedTimeToHourMinute(localToday.wakeTime)?.let { (h, m) ->
                wakeHour = h
                wakeMinute = m
            }
        } else {
            isRecordedSaved = false
            loggedDurationMinutes = 0
            loggedBedtime = null
            loggedWakeTime = null
        }
        sleepHistory = buildNormalizedLocalHistory()

        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.vitalsApiService.getSleepDetail(
                    userId = resolvedUserId,
                    email = resolvedEmail.ifBlank { null },
                    date = todayDate
                )
            }
            val sleepPayload = if (response.isSuccessful) response.body()?.sleep else null
            if (sleepPayload != null) {
                if (sleepPayload.date == todayDate && sleepPayload.durationMinutes > 0) {
                    isRecordedSaved = true
                    loggedDurationMinutes = sleepPayload.durationMinutes
                    loggedBedtime = sleepPayload.bedtime
                    loggedWakeTime = sleepPayload.wakeTime
                    parseFormattedTimeToHourMinute(sleepPayload.bedtime)?.let { (h, m) ->
                        bedHour = h
                        bedMinute = m
                    }
                    parseFormattedTimeToHourMinute(sleepPayload.wakeTime)?.let { (h, m) ->
                        wakeHour = h
                        wakeMinute = m
                    }
                    val pres = SleepColorStandards.getPresentation(sleepPayload.durationMinutes)
                    repository.saveSleepRecord(
                        SleepRecord(
                            bedtime = sleepPayload.bedtime.ifBlank { formatTime12Hour(bedHour, bedMinute) },
                            wakeTime = sleepPayload.wakeTime.ifBlank { formatTime12Hour(wakeHour, wakeMinute) },
                            durationMinutes = sleepPayload.durationMinutes,
                            sleepScore = if (sleepPayload.sleepScore > 0) sleepPayload.sleepScore else SleepColorStandards.deriveSleepScore(sleepPayload.durationMinutes),
                            qualityLabel = pres.qualityLabel,
                            dateString = todayDate
                        )
                    )
                } else if (sleepPayload.date == todayDate && sleepPayload.durationMinutes <= 0) {
                    isRecordedSaved = false
                    loggedDurationMinutes = 0
                    loggedBedtime = null
                    loggedWakeTime = null
                    repository.clearSleepRecord(todayDate)
                }
                val remoteHistory = sleepPayload.recentHistory
                    .filter { it.durationMinutes > 0 }
                    .map { item ->
                        val pres = SleepColorStandards.getPresentation(item.durationMinutes)
                        item.copy(
                            sleepScore = if (item.sleepScore > 0) item.sleepScore else SleepColorStandards.deriveSleepScore(item.durationMinutes),
                            quality = pres.qualityLabel
                        )
                    }
                if (remoteHistory.isNotEmpty()) {
                    sleepHistory = remoteHistory
                }
            }
        } catch (_: Exception) {
            // Keep locally cached history gracefully when offline
        }
    }

    // Manual Time Pickers Calculated Duration
    val manualCalculatedSleepMinutes = remember(bedHour, bedMinute, wakeHour, wakeMinute) {
        val bedTotalMins = bedHour * 60 + bedMinute
        val wakeTotalMins = wakeHour * 60 + wakeMinute
        if (wakeTotalMins >= bedTotalMins) {
            wakeTotalMins - bedTotalMins
        } else {
            (wakeTotalMins + 24 * 60) - bedTotalMins
        }
    }

    val formattedBedtime = remember(bedHour, bedMinute) {
        formatTime12Hour(bedHour, bedMinute)
    }

    val formattedWakeTime = remember(wakeHour, wakeMinute) {
        formatTime12Hour(wakeHour, wakeMinute)
    }

    // Overview Card Duration Display:
    // If recorded/saved for today, show exact recorded sleep; if not saved/reset, show 0h 0m (No Data).
    val displaySleepMinutes = if (isRecordedSaved && loggedDurationMinutes > 0) loggedDurationMinutes else 0
    val displayHours = displaySleepMinutes / 60
    val displayMins = displaySleepMinutes % 60
    val formattedDisplayDurationStr = "${displayHours}h ${displayMins}m"

    val displayProgressRatio = (displaySleepMinutes.toFloat() / targetSleepMinutes.toFloat()).coerceIn(0f, 1f)
    val displayProgressPercent = (displayProgressRatio * 100).toInt()

    val manualHours = manualCalculatedSleepMinutes / 60
    val manualMins = manualCalculatedSleepMinutes % 60
    val formattedManualDurationStr = "${manualHours}h ${manualMins}m"

    LaunchedEffect(showSuccessToast) {
        if (showSuccessToast) {
            delay(2500L)
            showSuccessToast = false
        }
    }

    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    BackArrowIcon(
                        modifier = Modifier.size(20.dp),
                        color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Sleep Tracker",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isRecordedSaved || loggedDurationMinutes > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFFEE2E2))
                            .clickable { showResetDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "↺ Reset (0h)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    CalendarIcon(modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Confirmation Toast Banner
        AnimatedVisibility(
            visible = showSuccessToast,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFDCFCE7))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF15803D)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sleep entry saved! $formattedDisplayDurationStr recorded ($displayProgressPercent% of 8h goal)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF15803D)
                )
            }
        }

        val qualityPresentation = remember(displaySleepMinutes, isRecordedSaved) {
            if (isRecordedSaved && displaySleepMinutes > 0) {
                SleepColorStandards.getPresentation(displaySleepMinutes)
            } else {
                SleepColorStandards.QualityPresentation(
                    qualityLabel = "No record",
                    status = "No Sleep Logged",
                    primaryColor = Color(0xFF94A3B8),
                    containerColor = Color(0xFFF8FAFC),
                    badgeTextColor = Color(0xFF64748B),
                    medicalMeaning = "No sleep logged for today yet"
                )
            }
        }

        // Top Sleep Overview & Goal Progress Card with Dynamic Quality Color Coding
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) CarbonDarkPurpleSurface else qualityPresentation.containerColor),
            border = if (isDark) BorderStroke(1.dp, CarbonDarkPurpleBorder) else null,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Bedtime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isDark) CarbonDarkPurpleAccent else qualityPresentation.badgeTextColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${qualityPresentation.status} • ${qualityPresentation.qualityLabel}",
                            fontSize = 13.sp,
                            color = if (isDark) CarbonDarkPurpleAccent else qualityPresentation.badgeTextColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isDark) CarbonDarkPurplePill else (if (isRecordedSaved) Color.White else Color(0xFFF1F5F9)))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isRecordedSaved) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(11.dp),
                                        tint = if (isDark) CarbonDarkPurpleText else qualityPresentation.badgeTextColor
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = if (isRecordedSaved) "Logged" else "No Data",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) CarbonDarkPurpleText else (if (isRecordedSaved) qualityPresentation.badgeTextColor else Color(0xFF64748B))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = formattedDisplayDurationStr,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isRecordedSaved) "$displayProgressPercent% of ${targetSleepMinutes / 60}h daily target • ${qualityPresentation.medicalMeaning}"
                    else "0% of ${targetSleepMinutes / 60}h daily target • Tap Start Sleep or Log below",
                    fontSize = 12.sp,
                    color = if (isDark) CarbonDarkTextSecondary else Color(0xFF4B5563),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Daily Sleep Target Progress Bar with Dynamic Severity Fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(qualityPresentation.primaryColor.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(displayProgressRatio)
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(qualityPresentation.primaryColor)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Bedtime", fontSize = 11.sp, color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280))
                        Text(text = if (isRecordedSaved) (loggedBedtime ?: formattedBedtime) else "--", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827))
                    }

                    Column {
                        Text(text = "Wake time", fontSize = 11.sp, color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280))
                        Text(text = if (isRecordedSaved) (loggedWakeTime ?: formattedWakeTime) else "--", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ---------------------------------------------------------------------
        // LIVE SLEEP TRACKER & WAKE-UP ALARM CARD (Background Timer + 2-Min Snooze)
        // ---------------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (liveSessionState.isActive) {
                    if (isDark) CarbonDarkPurpleSurface else Color(0xFFF3E8FF)
                } else {
                    if (isDark) CarbonDarkSurface else Color.White
                }
            ),
            border = BorderStroke(
                1.5.dp,
                if (liveSessionState.isActive) {
                    if (isDark) CarbonDarkPurpleAccent else Color(0xFF9333EA)
                } else {
                    if (isDark) CarbonDarkBorder else Color.Transparent
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (liveSessionState.isActive) 3.dp else 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (!liveSessionState.isActive) {
                    // --- IDLE STATE: CONFIGURE & START LIVE SLEEP SESSION ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Bedtime,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Live Sleep Tracker & Alarm",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "Track sleep in background with live timer, alarm & 2-min snooze",
                                fontSize = 12.sp,
                                color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Alarm ON/OFF Switch Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (liveAlarmEnabled) (if (isDark) Color(0xFF134E4A) else VitalTealDark.copy(alpha = 0.15f))
                                    else if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF1F5F9)
                                )
                                .clickable { liveAlarmEnabled = !liveAlarmEnabled }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (liveAlarmEnabled) Icons.Rounded.Alarm else Icons.Rounded.AlarmOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = if (liveAlarmEnabled) (if (isDark) Color(0xFF5EEAD4) else VitalTealDark) else (if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (liveAlarmEnabled) "Alarm ON" else "Alarm OFF",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (liveAlarmEnabled) (if (isDark) Color(0xFF5EEAD4) else VitalTealDark) else (if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Wake-Up Alarm Time (Full-Width Interactive Selector)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (liveAlarmEnabled) (if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF8FAFC))
                                else (if (isDark) CarbonDarkSurfaceVariant.copy(alpha = 0.5f) else Color(0xFFF1F5F9))
                            )
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        wakeHour = h
                                        wakeMinute = m
                                    },
                                    wakeHour,
                                    wakeMinute,
                                    false
                                ).show()
                            }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Alarm,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Wake-Up Alarm Time",
                                        fontSize = 12.sp,
                                        color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formattedWakeTime,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                                )
                                if (liveAlarmEnabled) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isDark) Color(0xFF134E4A) else VitalTealDark.copy(alpha = 0.12f))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = sleepSessionManager.formatAlarmCountdown(wakeHour, wakeMinute, currentTick),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isDark) Color(0xFF5EEAD4) else VitalTealDark
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = if (liveAlarmEnabled) "Tap to change" else "Alarm disabled",
                                    fontSize = 12.sp,
                                    color = if (liveAlarmEnabled) (if (isDark) Color(0xFF5EEAD4) else VitalTealDark) else (if (isDark) CarbonDarkTextSecondary else Color(0xFF94A3B8)),
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                if (liveAlarmEnabled) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = if (isDark) Color(0xFF5EEAD4) else VitalTealDark
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // "▶ Start Sleep" Button
                    Button(
                        onClick = {
                            sleepSessionManager.startSleepSession(
                                context = context,
                                wakeHour = wakeHour,
                                wakeMinute = wakeMinute,
                                alarmEnabled = liveAlarmEnabled,
                                userId = resolvedUserId,
                                userEmail = resolvedEmail
                            )
                            Toast.makeText(
                                context,
                                if (liveAlarmEnabled) "Sleep tracker started! Wake-up alarm set for $formattedWakeTime"
                                else "Sleep tracker started! Running in background",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VitalTealDark,
                            contentColor = Color.White
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Start Sleep",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                } else {
                    // --- ACTIVE STATE: LIVE ELAPSED TIMER & ALARM CONTROLS ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (liveSessionState.isAlarmRinging) Color(0xFFDC2626) else Color(0xFF16A34A))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (liveSessionState.isAlarmRinging) {
                                    Icon(
                                        imageVector = Icons.Rounded.Alarm,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFFDC2626)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                } else if (liveSessionState.isSnoozed) {
                                    Icon(
                                        imageVector = Icons.Rounded.Snooze,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isDark) CarbonDarkPurpleAccent else Color(0xFF7E22CE)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = if (liveSessionState.isAlarmRinging) "ALARM IS RINGING!"
                                    else if (liveSessionState.isSnoozed) "SNOOZED (2 MIN)"
                                    else "SLEEP TRACKER ACTIVE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (liveSessionState.isAlarmRinging) Color(0xFFDC2626)
                                    else if (isDark) CarbonDarkPurpleAccent else Color(0xFF7E22CE),
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        val startTimeStr = remember(liveSessionState.sleepStartMillis) {
                            if (liveSessionState.sleepStartMillis > 0L) {
                                SimpleDateFormat("hh:mm a", Locale.US).format(liveSessionState.sleepStartMillis)
                            } else ""
                        }
                        if (startTimeStr.isNotBlank()) {
                            Text(
                                text = "Started $startTimeStr",
                                fontSize = 11.5.sp,
                                color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Big Live Elapsed Time Display
                    val elapsedFormatted = formatElapsedTimer(liveSessionState.elapsedSeconds)
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = elapsedFormatted,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Live sleep elapsed • VitalSync sleep tracker is running",
                            fontSize = 12.sp,
                            color = if (isDark) CarbonDarkTextSecondary else Color(0xFF4B5563)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scheduled Alarm Status Line (Adjustable While Sleep Session Is Running)
                    val targetWakeFmt = formatTime12Hour(liveSessionState.targetWakeHour, liveSessionState.targetWakeMinute)
                    val activeAlarmCountdown = sleepSessionManager.formatAlarmCountdown(
                        liveSessionState.targetWakeHour,
                        liveSessionState.targetWakeMinute,
                        currentTick
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) CarbonDarkSurfaceVariant else Color.White.copy(alpha = 0.9f))
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        wakeHour = h
                                        wakeMinute = m
                                        sleepSessionManager.updateWakeAlarm(
                                            context = context,
                                            newWakeHour = h,
                                            newWakeMinute = m,
                                            newAlarmEnabled = true
                                        )
                                        val cd = sleepSessionManager.formatAlarmCountdown(h, m, System.currentTimeMillis())
                                        Toast.makeText(
                                            context,
                                            "Alarm updated to ${formatTime12Hour(h, m)} • $cd",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    liveSessionState.targetWakeHour,
                                    liveSessionState.targetWakeMinute,
                                    false
                                ).show()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (liveSessionState.alarmEnabled) Icons.Rounded.Alarm else Icons.Rounded.AlarmOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                        tint = if (liveSessionState.alarmEnabled) (if (isDark) Color(0xFF5EEAD4) else VitalTealDark) else (if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (liveSessionState.alarmEnabled) "Wake-up Alarm: $targetWakeFmt" else "Wake-up Alarm: Disabled",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) CarbonDarkTextPrimary else Color(0xFF1F2937)
                                    )
                                }

                                if (liveSessionState.alarmEnabled) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = activeAlarmCountdown,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isDark) Color(0xFF5EEAD4) else VitalTealDark
                                        )
                                        if (liveSessionState.isSnoozed) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "• Rings in ${formatSnoozeRemaining(liveSessionState.snoozeUntilMillis)}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFD97706)
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Tap to set alarm time",
                                        fontSize = 11.5.sp,
                                        color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Tap to Edit Pill / Hint
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDark) CarbonDarkSurface else Color(0xFFF1F5F9))
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = if (isDark) Color(0xFF5EEAD4) else VitalTealDark
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Change",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color(0xFF5EEAD4) else VitalTealDark
                                    )
                                }

                                // Toggle Alarm ON / OFF while running
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (liveSessionState.alarmEnabled) (if (isDark) Color(0xFF134E4A) else VitalTealDark.copy(alpha = 0.15f))
                                            else (if (isDark) CarbonDarkSurface else Color(0xFFE2E8F0))
                                        )
                                        .clickable {
                                            val toggled = !liveSessionState.alarmEnabled
                                            sleepSessionManager.updateWakeAlarm(
                                                context = context,
                                                newWakeHour = liveSessionState.targetWakeHour,
                                                newWakeMinute = liveSessionState.targetWakeMinute,
                                                newAlarmEnabled = toggled
                                            )
                                            Toast.makeText(
                                                context,
                                                if (toggled) "Alarm enabled for $targetWakeFmt" else "Alarm disabled",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = if (liveSessionState.alarmEnabled) "ON" else "OFF",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (liveSessionState.alarmEnabled) (if (isDark) Color(0xFF5EEAD4) else VitalTealDark) else (if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B))
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons based on alarm ringing / snooze / normal tracking
                    if (liveSessionState.isAlarmRinging) {
                        // Alarm Ringing -> Show Snooze (2 min) & Turn Off / Awake buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    sleepSessionManager.snoozeAlarm(context)
                                    Toast.makeText(context, "Alarm snoozed for 2 minutes", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF59E0B),
                                    contentColor = Color.White
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Snooze,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Snooze (2 min)",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    sleepSessionManager.turnOffAlarmAndLog(context) { rec ->
                                        isRecordedSaved = true
                                        loggedDurationMinutes = rec.durationMinutes
                                        loggedBedtime = rec.bedtime
                                        loggedWakeTime = rec.wakeTime
                                        parseFormattedTimeToHourMinute(rec.bedtime)?.let { (h, m) -> bedHour = h; bedMinute = m }
                                        parseFormattedTimeToHourMinute(rec.wakeTime)?.let { (h, m) -> wakeHour = h; wakeMinute = m }
                                        showSuccessToast = true
                                        sleepHistory = (listOf(
                                            SleepLogEntryDto(
                                                date = rec.dateString,
                                                durationMinutes = rec.durationMinutes,
                                                sleepScore = rec.sleepScore,
                                                quality = rec.qualityLabel
                                            )
                                        ) + sleepHistory.filterNot { it.date == rec.dateString }).take(7)
                                        onSleepLogged()
                                    }
                                    Toast.makeText(context, "Good morning! Sleep logged successfully", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VitalTealDark,
                                    contentColor = Color.White
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.WbSunny,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Turn Off / Awake",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else if (liveSessionState.isSnoozed) {
                        // Snoozed State -> Allow Turn Off early or let it ring again
                        Button(
                            onClick = {
                                sleepSessionManager.turnOffAlarmAndLog(context) { rec ->
                                    isRecordedSaved = true
                                    loggedDurationMinutes = rec.durationMinutes
                                    loggedBedtime = rec.bedtime
                                    loggedWakeTime = rec.wakeTime
                                    parseFormattedTimeToHourMinute(rec.bedtime)?.let { (h, m) -> bedHour = h; bedMinute = m }
                                    parseFormattedTimeToHourMinute(rec.wakeTime)?.let { (h, m) -> wakeHour = h; wakeMinute = m }
                                    showSuccessToast = true
                                    sleepHistory = (listOf(
                                        SleepLogEntryDto(
                                            date = rec.dateString,
                                            durationMinutes = rec.durationMinutes,
                                            sleepScore = rec.sleepScore,
                                            quality = rec.qualityLabel
                                        )
                                    ) + sleepHistory.filterNot { it.date == rec.dateString }).take(7)
                                    onSleepLogged()
                                }
                                Toast.makeText(context, "Good morning! Sleep logged successfully", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VitalTealDark,
                                contentColor = Color.White
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.WbSunny,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Turn Off Alarm & Log Sleep",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Normal Active Session -> Early Wake ("Stop Sleep")
                        Button(
                            onClick = {
                                sleepSessionManager.stopSleepEarly(context) { rec ->
                                    isRecordedSaved = true
                                    loggedDurationMinutes = rec.durationMinutes
                                    loggedBedtime = rec.bedtime
                                    loggedWakeTime = rec.wakeTime
                                    parseFormattedTimeToHourMinute(rec.bedtime)?.let { (h, m) -> bedHour = h; bedMinute = m }
                                    parseFormattedTimeToHourMinute(rec.wakeTime)?.let { (h, m) -> wakeHour = h; wakeMinute = m }
                                    showSuccessToast = true
                                    sleepHistory = (listOf(
                                        SleepLogEntryDto(
                                            date = rec.dateString,
                                            durationMinutes = rec.durationMinutes,
                                            sleepScore = rec.sleepScore,
                                            quality = rec.qualityLabel
                                        )
                                    ) + sleepHistory.filterNot { it.date == rec.dateString }).take(7)
                                    onSleepLogged()
                                }
                                Toast.makeText(context, "Sleep session saved!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFBE185D), // Deep Rose / Berry
                                contentColor = Color.White
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.WbSunny,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Stop Sleep (I'm Awake)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ---------------------------------------------------------------------
        // SEPARATE BEDTIME REMINDER & WIND-DOWN CARD
        // ---------------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) CarbonDarkSurface else Color.White),
            border = BorderStroke(1.dp, if (isDark) CarbonDarkBorder else Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Bedtime,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bedtime Reminder",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "Get alerted when it's time to wind down for bed",
                            fontSize = 12.sp,
                            color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Bedtime Reminder Toggle Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (bedtimeReminderEnabled) (if (isDark) Color(0xFF134E4A) else VitalTealDark.copy(alpha = 0.15f))
                                else if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF1F5F9)
                            )
                            .clickable {
                                bedtimeReminderEnabled = !bedtimeReminderEnabled
                                sleepSessionManager.saveBedtimeReminder(
                                    context = context,
                                    enabled = bedtimeReminderEnabled,
                                    hour = bedtimeHour,
                                    minute = bedtimeMinute,
                                    mode = bedtimeReminderMode
                                )
                                if (!bedtimeReminderEnabled) {
                                    sleepSessionManager.stopBedtimeAlert(context)
                                }
                                Toast.makeText(
                                    context,
                                    if (bedtimeReminderEnabled) "Bedtime reminder turned ON" else "Bedtime reminder turned OFF",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (bedtimeReminderEnabled) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = if (bedtimeReminderEnabled) (if (isDark) Color(0xFF5EEAD4) else VitalTealDark) else (if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (bedtimeReminderEnabled) "Reminder ON" else "Reminder OFF",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (bedtimeReminderEnabled) (if (isDark) Color(0xFF5EEAD4) else VitalTealDark) else (if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B))
                            )
                        }
                    }
                }

                if (liveSessionState.isBedtimeRinging) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) Color(0xFF4C1D95) else Color(0xFFF3E8FF))
                            .border(1.dp, if (isDark) Color(0xFFA78BFA) else Color(0xFFC084FC), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (isDark) Color(0xFFF3E8FF) else Color(0xFF581C87)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Bedtime Chime Ringing",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFF3E8FF) else Color(0xFF581C87)
                                    )
                                    Text(
                                        text = "Time to wind down for restful sleep",
                                        fontSize = 12.sp,
                                        color = if (isDark) Color(0xFFDDD6FE) else Color(0xFF7E22CE)
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    sleepSessionManager.stopBedtimeAlert(context)
                                    Toast.makeText(context, "Bedtime chime silenced", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDC2626),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Stop,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Stop Bedtime", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val formattedBedtimeReminder = remember(bedtimeHour, bedtimeMinute) {
                    formatTime12Hour(bedtimeHour, bedtimeMinute)
                }

                // Bedtime Picker Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (bedtimeReminderEnabled) (if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF8FAFC))
                            else (if (isDark) CarbonDarkSurfaceVariant.copy(alpha = 0.5f) else Color(0xFFF1F5F9))
                        )
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    bedtimeHour = h
                                    bedtimeMinute = m
                                    if (bedtimeReminderEnabled) {
                                        sleepSessionManager.saveBedtimeReminder(
                                            context = context,
                                            enabled = true,
                                            hour = h,
                                            minute = m,
                                            mode = bedtimeReminderMode
                                        )
                                    }
                                },
                                bedtimeHour,
                                bedtimeMinute,
                                false
                            ).show()
                        }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Target Bedtime",
                                    fontSize = 12.sp,
                                    color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formattedBedtimeReminder,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = if (bedtimeReminderEnabled) "Tap to change" else "Reminder off",
                                fontSize = 12.sp,
                                color = if (bedtimeReminderEnabled) (if (isDark) Color(0xFF5EEAD4) else VitalTealDark) else (if (isDark) CarbonDarkTextSecondary else Color(0xFF94A3B8)),
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                            if (bedtimeReminderEnabled) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = if (isDark) Color(0xFF5EEAD4) else VitalTealDark
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reminder Alert Mode Selector: Notification vs Alarm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alert Type:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) CarbonDarkTextPrimary else Color(0xFF374151)
                    )

                    // Notification Mode Chip
                    val isNotifMode = bedtimeReminderMode == "notification"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isNotifMode) (if (isDark) Color(0xFF134E4A) else VitalTealDark.copy(alpha = 0.15f))
                                else if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF1F5F9)
                            )
                            .clickable {
                                bedtimeReminderMode = "notification"
                                if (bedtimeReminderEnabled) {
                                    sleepSessionManager.saveBedtimeReminder(
                                        context = context,
                                        enabled = true,
                                        hour = bedtimeHour,
                                        minute = bedtimeMinute,
                                        mode = "notification"
                                    )
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.ChatBubbleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = if (isNotifMode) (if (isDark) Color(0xFF5EEAD4) else VitalTealDark) else (if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Notification",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isNotifMode) (if (isDark) Color(0xFF5EEAD4) else VitalTealDark) else (if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B))
                        )
                    }
                    }

                    // Alarm / Chime Mode Chip
                    val isAlarmMode = bedtimeReminderMode == "alarm"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isAlarmMode) (if (isDark) Color(0xFF134E4A) else VitalTealDark.copy(alpha = 0.15f))
                                else if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF1F5F9)
                            )
                            .clickable {
                                bedtimeReminderMode = "alarm"
                                if (bedtimeReminderEnabled) {
                                    sleepSessionManager.saveBedtimeReminder(
                                        context = context,
                                        enabled = true,
                                        hour = bedtimeHour,
                                        minute = bedtimeMinute,
                                        mode = "alarm"
                                    )
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = if (isAlarmMode) (if (isDark) Color(0xFF5EEAD4) else VitalTealDark) else (if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Alarm Chime",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isAlarmMode) (if (isDark) Color(0xFF5EEAD4) else VitalTealDark) else (if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B))
                        )
                    }
                    }
                }

                if (bedtimeReminderEnabled || liveSessionState.isBedtimeRinging) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            sleepSessionManager.stopBedtimeAlert(context)
                            bedtimeReminderEnabled = false
                            sleepSessionManager.saveBedtimeReminder(
                                context = context,
                                enabled = false,
                                hour = bedtimeHour,
                                minute = bedtimeMinute,
                                mode = bedtimeReminderMode
                            )
                            Toast.makeText(context, "Bedtime reminder & alert stopped", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (liveSessionState.isBedtimeRinging) {
                                Color(0xFFDC2626)
                            } else {
                                if (isDark) Color(0xFF3B1818) else Color(0xFFFEE2E2)
                            },
                            contentColor = if (liveSessionState.isBedtimeRinging) {
                                Color.White
                            } else {
                                if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
                            }
                        ),
                        border = if (!liveSessionState.isBedtimeRinging) {
                            BorderStroke(1.dp, if (isDark) Color(0xFF7F1D1D) else Color(0xFFFCA5A5))
                        } else null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (liveSessionState.isBedtimeRinging) "Stop Ringing Bedtime Chime" else "Stop Bedtime Alert Schedule",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Manual Entry Time Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) CarbonDarkSurface else Color.White),
            border = BorderStroke(1.dp, if (isDark) CarbonDarkBorder else Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Manual Sleep Log & Time Pickers",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                    )
                }

                Text(
                    text = "Tap to pick Bedtime and Wake time for automatic calculation:",
                    fontSize = 12.sp,
                    color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Bedtime & Wake Time Selector Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bedtime Picker Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF8FAFC))
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        bedHour = h
                                        bedMinute = m
                                    },
                                    bedHour,
                                    bedMinute,
                                    false
                                ).show()
                            }
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Bedtime", fontSize = 12.sp, color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = formattedBedtime, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827))
                        }
                    }

                    // Wake Time Picker Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF8FAFC))
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        wakeHour = h
                                        wakeMinute = m
                                    },
                                    wakeHour,
                                    wakeMinute,
                                    false
                                ).show()
                            }
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Alarm,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Wake-up Time", fontSize = 12.sp, color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = formattedWakeTime, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Calculated Duration Status Line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF1F5F9))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Calculated Total Sleep:", fontSize = 13.sp, color = if (isDark) CarbonDarkTextSecondary else Color(0xFF475569))
                        val manualQuality = remember(manualCalculatedSleepMinutes) {
                            SleepColorStandards.getPresentation(manualCalculatedSleepMinutes)
                        }
                        Text(
                            text = "$formattedManualDurationStr • ${manualQuality.status}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) CarbonDarkPurpleAccent else manualQuality.primaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Confirm & Log Sleep Session Button
                Button(
                    onClick = {
                        if (manualCalculatedSleepMinutes <= 0) {
                            Toast.makeText(context, "Please select a sleep duration greater than 0 minutes.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val derivedScore = SleepColorStandards.deriveSleepScore(manualCalculatedSleepMinutes)
                        val manualPres = SleepColorStandards.getPresentation(manualCalculatedSleepMinutes)
                        val tierLabel = manualPres.qualityLabel
                        val record = SleepRecord(
                            bedtime = formattedBedtime,
                            wakeTime = formattedWakeTime,
                            durationMinutes = manualCalculatedSleepMinutes,
                            sleepScore = derivedScore,
                            qualityLabel = tierLabel,
                            dateString = todayDate
                        )
                        repository.saveSleepRecord(record)
                        isRecordedSaved = true
                        loggedDurationMinutes = manualCalculatedSleepMinutes
                        loggedBedtime = formattedBedtime
                        loggedWakeTime = formattedWakeTime
                        showSuccessToast = true
                        sleepHistory = (listOf(
                            SleepLogEntryDto(
                                date = todayDate,
                                durationMinutes = manualCalculatedSleepMinutes,
                                sleepScore = derivedScore,
                                quality = tierLabel
                            )
                        ) + sleepHistory.filterNot { it.date == todayDate }).take(7)

                        isSavingRemote = true
                        coroutineScope.launch {
                            var synced = false
                            try {
                                val resp = withContext(Dispatchers.IO) {
                                    RetrofitClient.vitalsApiService.logSleep(
                                        LogSleepRequest(
                                            userId = resolvedUserId,
                                            email = resolvedEmail.ifBlank { null },
                                            dateString = todayDate,
                                            bedtime = formattedBedtime,
                                            wakeTime = formattedWakeTime,
                                            durationMinutes = manualCalculatedSleepMinutes,
                                            sleepScore = derivedScore,
                                            qualityLabel = tierLabel
                                        )
                                    )
                                }
                                if (resp.isSuccessful && resp.body()?.success == true) {
                                    synced = true
                                    val refreshed = withContext(Dispatchers.IO) {
                                        RetrofitClient.vitalsApiService.getSleepDetail(
                                            userId = resolvedUserId,
                                            email = resolvedEmail.ifBlank { null },
                                            date = todayDate
                                        )
                                    }
                                    val refreshedHist = if (refreshed.isSuccessful) {
                                        refreshed.body()?.sleep?.recentHistory
                                            ?.filter { it.durationMinutes > 0 }
                                            ?.map { item ->
                                                val p = SleepColorStandards.getPresentation(item.durationMinutes)
                                                item.copy(
                                                    sleepScore = if (item.sleepScore > 0) item.sleepScore else SleepColorStandards.deriveSleepScore(item.durationMinutes),
                                                    quality = p.qualityLabel
                                                )
                                            }
                                            ?: emptyList()
                                    } else emptyList()
                                    if (refreshedHist.isNotEmpty()) {
                                        sleepHistory = refreshedHist
                                    }
                                    onSleepLogged()
                                }
                            } catch (_: Exception) {
                            } finally {
                                isSavingRemote = false
                            }
                            Toast.makeText(
                                context,
                                if (synced) "Sleep logged: $formattedManualDurationStr ($tierLabel)"
                                else "Saved locally ($tierLabel); server sync unavailable",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    enabled = !isSavingRemote,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VitalTealDark,
                        contentColor = Color.White
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isSavingRemote) {
                            Icon(
                                imageVector = Icons.Rounded.Save,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = if (isSavingRemote) "Syncing Sleep Log..." else "Log Sleep Session",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Sleep Logs Card with Quality Badges (Excellent / Optimal / Fair / Poor)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) CarbonDarkSurface else Color.White),
            border = BorderStroke(1.dp, if (isDark) CarbonDarkBorder else Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Recent Sleep Logs",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                )
                Text(
                    text = "Your logged nights rated as Excellent, Optimal, Fair, or Poor:",
                    fontSize = 12.sp,
                    color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (sleepHistory.isEmpty()) {
                    Text(
                        text = "No sleep logs recorded yet. Tap \"Log Sleep Session\" above to save your first log.",
                        fontSize = 13.sp,
                        color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        sleepHistory.forEach { entry ->
                            val pres = SleepColorStandards.getPresentation(entry.durationMinutes)
                            val durH = entry.durationMinutes / 60
                            val durM = entry.durationMinutes % 60
                            val scoreVal = if (entry.sleepScore > 0) entry.sleepScore else SleepColorStandards.deriveSleepScore(entry.durationMinutes)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF8FAFC))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = entry.date.ifBlank { todayDate },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                                    )
                                    Text(
                                        text = "${durH}h ${durM}m · Score $scoreVal",
                                        fontSize = 12.sp,
                                        color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDark) CarbonDarkSurface else pres.containerColor)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = pres.qualityLabel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = pres.badgeTextColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sleep Stages Bar Chart Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) CarbonDarkSurface else Color.White),
            border = BorderStroke(1.dp, if (isDark) CarbonDarkBorder else Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Your sleep stages",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Smoothly Animated Stacked Bar Stages Chart
                var targetProgress by remember { mutableFloatStateOf(0f) }
                LaunchedEffect(Unit) {
                    targetProgress = 1f
                }
                val chartProgress by animateFloatAsState(
                    targetValue = targetProgress,
                    animationSpec = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
                    label = "SleepBarChartAnimation"
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val barWidth = 14.dp.toPx()
                    val totalWidth = size.width
                    val spacing = (totalWidth - (barWidth * 12)) / 13f

                    repeat(12) { i ->
                        val x = spacing + i * (barWidth + spacing)
                        val hDeep = size.height * (0.22f + (i % 3) * 0.04f) * chartProgress
                        val hLight = size.height * (0.46f + (i % 2) * 0.05f) * chartProgress
                        val hRem = size.height * (0.22f) * chartProgress
                        val hAwake = size.height * 0.06f * chartProgress

                        // 1. Deep Sleep Stage (Bottom) - Non-REM Stage 3/4 (#3F51B5)
                        drawRoundRect(
                            color = SleepColorStandards.StageDeep,
                            topLeft = Offset(x, size.height - hDeep),
                            size = Size(barWidth, hDeep),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        // 2. Light Sleep Stage (Middle) - Non-REM Stage 1/2 (#03A9F4)
                        drawRoundRect(
                            color = SleepColorStandards.StageLight,
                            topLeft = Offset(x, size.height - hDeep - hLight),
                            size = Size(barWidth, hLight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        // 3. REM Sleep Stage - Rapid Eye Movement (#7E57C2)
                        drawRoundRect(
                            color = SleepColorStandards.StageRem,
                            topLeft = Offset(x, size.height - hDeep - hLight - hRem),
                            size = Size(barWidth, hRem),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        // 4. Awake / Disruptions (Top) - Micro-arousals (#FF7043)
                        drawRoundRect(
                            color = SleepColorStandards.StageAwake,
                            topLeft = Offset(x, size.height - hDeep - hLight - hRem - hAwake),
                            size = Size(barWidth, hAwake),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stage Breakdown List with Standardized High-Contrast Colors
                val deepStr = "${(displaySleepMinutes * 0.22f).toInt() / 60}h ${(displaySleepMinutes * 0.22f).toInt() % 60}m"
                val lightStr = "${(displaySleepMinutes * 0.52f).toInt() / 60}h ${(displaySleepMinutes * 0.52f).toInt() % 60}m"
                val remStr = "${(displaySleepMinutes * 0.21f).toInt() / 60}h ${(displaySleepMinutes * 0.21f).toInt() % 60}m"
                val awakeStr = "${(displaySleepMinutes * 0.05f).toInt()}m"

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SleepStageRow(label = "Deep Sleep (Physical Repair)", duration = deepStr, color = SleepColorStandards.StageDeep, isDark = isDark)
                    SleepStageRow(label = "REM Sleep (Cognitive Restoration)", duration = remStr, color = SleepColorStandards.StageRem, isDark = isDark)
                    SleepStageRow(label = "Light Sleep (Physiological Rest)", duration = lightStr, color = SleepColorStandards.StageLight, isDark = isDark)
                    SleepStageRow(label = "Awake / Disruptions", duration = awakeStr, color = SleepColorStandards.StageAwake, isDark = isDark)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Standardized Sleep Quality Thresholds & Medical Meaning Legend Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) CarbonDarkSurface else Color.White),
            border = BorderStroke(1.dp, if (isDark) CarbonDarkBorder else Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Sleep Quality Thresholds",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                )
                Text(
                    text = "Clinical recovery standards and stage color codes:",
                    fontSize = 12.sp,
                    color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    QualityThresholdRow(
                        title = "Excellent",
                        threshold = "> 8.5 hrs • Score 90–100",
                        meaning = "Excellent deep restorative sleep recovery",
                        color = SleepColorStandards.ExcessivePurple,
                        bgColor = if (isDark) CarbonDarkSurfaceVariant else SleepColorStandards.ExcessivePurpleBg,
                        isDark = isDark
                    )
                    QualityThresholdRow(
                        title = "Optimal",
                        threshold = "7.0 – 8.5 hrs • Score 75–90",
                        meaning = "Optimal recovery, restorative sleep",
                        color = SleepColorStandards.OptimalGreen,
                        bgColor = if (isDark) CarbonDarkSurfaceVariant else SleepColorStandards.OptimalGreenBg,
                        isDark = isDark
                    )
                    QualityThresholdRow(
                        title = "Fair",
                        threshold = "6.0 – 6.9 hrs • Score 60–74",
                        meaning = "Mild deficit, acceptable baseline recovery",
                        color = SleepColorStandards.ModerateAmber,
                        bgColor = if (isDark) CarbonDarkSurfaceVariant else SleepColorStandards.ModerateAmberBg,
                        isDark = isDark
                    )
                    QualityThresholdRow(
                        title = "Poor",
                        threshold = "< 6.0 hrs • Score 40–55",
                        meaning = "Critical sleep debt, elevated cortisol",
                        color = SleepColorStandards.DeficitRed,
                        bgColor = if (isDark) CarbonDarkSurfaceVariant else SleepColorStandards.DeficitRedBg,
                        isDark = isDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Insight Banner
        if (showAiInsights) {
            Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) CarbonDarkPurpleSurface else Color(0xFFF3E8FF)),
            border = BorderStroke(1.dp, if (isDark) CarbonDarkPurpleBorder else Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isDark) CarbonDarkPurpleAccent else Color(0xFF7C3AED)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI insight",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) CarbonDarkPurpleAccent else Color(0xFF7C3AED)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isRecordedSaved && displaySleepMinutes > 0) {
                        "Your recorded sleep duration of $formattedDisplayDurationStr matches your 8h daily recovery target well. Maintain this sleep schedule for optimal energy."
                    } else {
                        "No sleep recorded for today yet. Use the Live Sleep Tracker or Manual Log to record your sleep session."
                    },
                    fontSize = 13.sp,
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF374151)
                )
            }
        }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Reset Sleep Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Reset Today's Sleep Data?",
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                )
            },
            text = {
                Text(
                    text = "This will clear today's logged sleep session and reset duration back to 0h across local storage and the server database.",
                    color = if (isDark) CarbonDarkTextSecondary else Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        sleepSessionManager.resetSleepData(context, todayDate) {
                            isRecordedSaved = false
                            loggedDurationMinutes = 0
                            loggedBedtime = null
                            loggedWakeTime = null
                            repository.clearSleepRecord(todayDate)
                            sleepHistory = sleepHistory.filterNot { it.date == todayDate }
                            onSleepLogged()
                        }
                        Toast.makeText(context, "Sleep data reset to 0h", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Reset to 0h", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280))
                }
            },
            containerColor = if (isDark) CarbonDarkSurface else Color.White
        )
    }
}

private fun formatTime12Hour(hour24: Int, minute: Int): String {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour24)
        set(Calendar.MINUTE, minute)
    }
    return SimpleDateFormat("hh:mm a", Locale.US).format(cal.time)
}

private fun parseFormattedTimeToHourMinute(timeStr: String): Pair<Int, Int>? {
    return try {
        val date = SimpleDateFormat("hh:mm a", Locale.US).parse(timeStr) ?: return null
        val cal = Calendar.getInstance().apply { time = date }
        Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    } catch (_: Exception) {
        null
    }
}

private fun formatElapsedTimer(totalSeconds: Long): String {
    val hrs = totalSeconds / 3600
    val mins = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
}

private fun formatSnoozeRemaining(snoozeUntilMillis: Long): String {
    val remainingSecs = ((snoozeUntilMillis - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
    val mins = remainingSecs / 60
    val secs = remainingSecs % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}

@Composable
private fun SleepStageRow(
    label: String,
    duration: String,
    color: Color,
    isDark: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = label, fontSize = 14.sp, color = if (isDark) CarbonDarkTextSecondary else Color(0xFF374151))
        }

        Text(text = duration, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827))
    }
}

@Composable
private fun BackArrowIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Black
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.75f, h * 0.5f)
            lineTo(w * 0.25f, h * 0.5f)
            moveTo(w * 0.45f, h * 0.25f)
            lineTo(w * 0.25f, h * 0.5f)
            lineTo(w * 0.45f, h * 0.75f)
        }

        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun CalendarIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val calPath = Path().apply {
            moveTo(0f, h * 0.25f)
            lineTo(w, h * 0.25f)
            lineTo(w, h * 0.85f)
            lineTo(0f, h * 0.85f)
            close()
        }

        drawPath(path = calPath, color = Color(0xFF6B7280), style = Stroke(width = 1.6.dp.toPx()))
    }
}

@Composable
private fun QualityThresholdRow(
    title: String,
    threshold: String,
    meaning: String,
    color: Color,
    bgColor: Color,
    isDark: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF8FAFC))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF1E293B)
                )
                Text(
                    text = meaning,
                    fontSize = 11.sp,
                    color = if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B),
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(bgColor)
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = threshold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun SleepDetailScreenPreview() {
    VitalsyncTheme {
        SleepDetailScreen()
    }
}
