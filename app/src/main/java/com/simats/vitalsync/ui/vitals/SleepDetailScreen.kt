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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    var isRecordedSaved by remember { mutableStateOf(false) }
    var isSavingRemote by remember { mutableStateOf(false) }
    var showSuccessToast by remember { mutableStateOf(false) }
    var sleepHistory by remember { mutableStateOf<List<SleepLogEntryDto>>(emptyList()) }

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
            parseFormattedTimeToHourMinute(localToday.bedtime)?.let { (h, m) ->
                bedHour = h
                bedMinute = m
            }
            parseFormattedTimeToHourMinute(localToday.wakeTime)?.let { (h, m) ->
                wakeHour = h
                wakeMinute = m
            }
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

    // Automatic Overnight Duration Calculation
    val totalSleepMinutes = remember(bedHour, bedMinute, wakeHour, wakeMinute) {
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

    val durationHours = totalSleepMinutes / 60
    val durationMins = totalSleepMinutes % 60
    val formattedDurationStr = "${durationHours}h ${durationMins}m"

    val progressRatio = (totalSleepMinutes.toFloat() / targetSleepMinutes.toFloat()).coerceIn(0f, 1f)
    val progressPercent = (progressRatio * 100).toInt()

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

        Spacer(modifier = Modifier.height(16.dp))

        // Confirmation Toast Banner
        AnimatedVisibility(
            visible = showSuccessToast,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFDCFCE7))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌙 Sleep entry saved! $formattedDurationStr recorded ($progressPercent% of 8h goal)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF15803D)
                )
            }
        }

        val qualityPresentation = remember(totalSleepMinutes) {
            SleepColorStandards.getPresentation(totalSleepMinutes)
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
                    Text(
                        text = "🌙 ${qualityPresentation.status} • ${qualityPresentation.qualityLabel}",
                        fontSize = 13.sp,
                        color = if (isDark) CarbonDarkPurpleAccent else qualityPresentation.badgeTextColor,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDark) CarbonDarkPurplePill else Color.White)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isRecordedSaved) "Logged ✓" else qualityPresentation.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) CarbonDarkPurpleText else qualityPresentation.badgeTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = formattedDurationStr,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$progressPercent% of ${targetSleepMinutes / 60}h daily target • ${qualityPresentation.medicalMeaning}",
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
                            .fillMaxWidth(progressRatio)
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
                        Text(text = formattedBedtime, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827))
                    }

                    Column {
                        Text(text = "Wake time", fontSize = 11.sp, color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280))
                        Text(text = formattedWakeTime, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827))
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
                Text(
                    text = "⏱️ Manual Sleep Log & Time Pickers",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                )

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
                                Text(text = "🕒", fontSize = 14.sp)
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
                                Text(text = "⏰", fontSize = 14.sp)
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
                        Text(
                            text = "$formattedDurationStr • ${qualityPresentation.status}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) CarbonDarkPurpleAccent else qualityPresentation.primaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Confirm & Log Sleep Session Button
                Button(
                    onClick = {
                        if (totalSleepMinutes <= 0) {
                            Toast.makeText(context, "Please select a sleep duration greater than 0 minutes.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val derivedScore = SleepColorStandards.deriveSleepScore(totalSleepMinutes)
                        val tierLabel = qualityPresentation.qualityLabel
                        val record = SleepRecord(
                            bedtime = formattedBedtime,
                            wakeTime = formattedWakeTime,
                            durationMinutes = totalSleepMinutes,
                            sleepScore = derivedScore,
                            qualityLabel = tierLabel,
                            dateString = todayDate
                        )
                        repository.saveSleepRecord(record)
                        isRecordedSaved = true
                        showSuccessToast = true
                        sleepHistory = (listOf(
                            SleepLogEntryDto(
                                date = todayDate,
                                durationMinutes = totalSleepMinutes,
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
                                            durationMinutes = totalSleepMinutes,
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
                                if (synced) "🌙 Sleep logged: $formattedDurationStr ($tierLabel)"
                                else "🌙 Saved locally ($tierLabel); server sync unavailable",
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
                    Text(
                        text = if (isSavingRemote) "Syncing Sleep Log..." else "💾 Log Sleep Session",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                val deepStr = "${(totalSleepMinutes * 0.22f).toInt() / 60}h ${(totalSleepMinutes * 0.22f).toInt() % 60}m"
                val lightStr = "${(totalSleepMinutes * 0.52f).toInt() / 60}h ${(totalSleepMinutes * 0.52f).toInt() % 60}m"
                val remStr = "${(totalSleepMinutes * 0.21f).toInt() / 60}h ${(totalSleepMinutes * 0.21f).toInt() % 60}m"
                val awakeStr = "${(totalSleepMinutes * 0.05f).toInt()}m"

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
                Text(
                    text = "✨ AI insight",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) CarbonDarkPurpleAccent else Color(0xFF7C3AED)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your calculated sleep duration of $formattedDurationStr matches your 8h daily recovery target well. Maintain this sleep schedule for optimal energy.",
                    fontSize = 13.sp,
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF374151)
                )
            }
        }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
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
