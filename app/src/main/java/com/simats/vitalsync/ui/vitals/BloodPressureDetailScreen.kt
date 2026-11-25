package com.simats.vitalsync.ui.vitals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.data.device.WatchBpReading
import com.simats.vitalsync.data.model.vitals.VitalDetailGuidanceResponse
import com.simats.vitalsync.data.model.vitals.VitalReadingItem
import com.simats.vitalsync.data.model.vitals.evaluateBloodPressure
import com.simats.vitalsync.ui.theme.CarbonDarkActivityBg
import com.simats.vitalsync.ui.theme.CarbonDarkBackground
import com.simats.vitalsync.ui.theme.CarbonDarkCriticalBg
import com.simats.vitalsync.ui.theme.CarbonDarkSurface
import com.simats.vitalsync.ui.theme.CarbonDarkSurfaceVariant
import com.simats.vitalsync.ui.theme.CarbonDarkTextSecondary
import com.simats.vitalsync.ui.theme.CarbonDarkWarningBg
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun BloodPressureDetailScreen(
    modifier: Modifier = Modifier,
    detailState: VitalDetailGuidanceResponse? = null,
    isDetailLoading: Boolean = false,
    systolic: Int = 0,
    diastolic: Int = 0,
    status: String = "No data available",
    readings: List<VitalReadingItem> = emptyList(),
    onBackClick: () -> Unit = {},
    onAddReadingClick: () -> Unit = {},
    onBpSynced: (WatchBpReading) -> Unit = {}
) {
    var syncedReading by remember { mutableStateOf<WatchBpReading?>(null) }

    val currentSys = syncedReading?.systolic?.takeIf { it > 0 }
        ?: detailState?.systolic?.takeIf { it > 0 }
        ?: systolic.takeIf { it > 0 }
    val currentDia = syncedReading?.diastolic?.takeIf { it > 0 }
        ?: detailState?.diastolic?.takeIf { it > 0 }
        ?: diastolic.takeIf { it > 0 }
    val hasBp = currentSys != null && currentDia != null
    val bpEval = evaluateBloodPressure(currentSys, currentDia)
    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground ||
        MaterialTheme.colorScheme.surface == CarbonDarkSurface

    val syncTimeFormatter = remember {
        DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
    }

    val (statusLabel, statusColor, statusBg) = when {
        !hasBp -> Triple("No Readings Yet", if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280), if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF3F4F6))
        bpEval.statusTitle == "Low Blood Pressure (Hypotension)" ->
            Triple(bpEval.statusTitle, if (isDark) Color(0xFFF87171) else Color(0xFFE53935), if (isDark) CarbonDarkCriticalBg else Color(0xFFFEE2E2))
        bpEval.statusTitle == "Hypertensive Crisis" ->
            Triple(bpEval.statusTitle, if (isDark) Color(0xFFF87171) else Color(0xFFD32F2F), if (isDark) CarbonDarkCriticalBg else Color(0xFFFEE2E2))
        bpEval.statusTitle == "Stage 2 Hypertension" ->
            Triple(bpEval.statusTitle, if (isDark) Color(0xFFFBBF24) else Color(0xFFF57C00), if (isDark) CarbonDarkWarningBg else Color(0xFFFED7AA))
        bpEval.statusTitle == "Stage 1 Hypertension" ->
            Triple(bpEval.statusTitle, if (isDark) Color(0xFFFBBF24) else Color(0xFFFFA000), if (isDark) CarbonDarkWarningBg else Color(0xFFFFEDD5))
        bpEval.statusTitle == "Elevated Blood Pressure" ->
            Triple(bpEval.statusTitle, if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706), if (isDark) CarbonDarkWarningBg else Color(0xFFFEF3C7))
        else ->
            Triple(bpEval.statusTitle, if (isDark) Color(0xFF34D399) else Color(0xFF43A047), if (isDark) CarbonDarkActivityBg else Color(0xFFECFDF5))
    }

    val recordedLabel = syncedReading?.let { reading ->
        val fmtTime = try {
            syncTimeFormatter.format(reading.time)
        } catch (_: Exception) {
            "recently"
        }
        val sourceSuffix = reading.sourcePackageName?.substringAfterLast('.')?.takeIf { it.isNotBlank() }
            ?.let { " ($it)" } ?: ""
        "Synced via Health Connect at $fmtTime$sourceSuffix"
    } ?: detailState?.recordedAt?.takeIf { it.isNotBlank() }?.let { "Recorded $it" }
        ?: "Tap '+ Log BP' or sync from Health Connect"

    val showMedicalBanner = hasBp && (bpEval.urgency == "MEDICAL_ATTENTION" || bpEval.statusTitle == "Low Blood Pressure (Hypotension)")
    val medicalBannerText = when {
        bpEval.statusTitle == "Low Blood Pressure (Hypotension)" && bpEval.urgency == "MEDICAL_ATTENTION" ->
            "Hypotension Alert: Your blood pressure is critically low ($currentSys/$currentDia mmHg). Lie down safely with legs slightly elevated, hydrate slowly, and seek urgent medical care if dizzy or faint."
        bpEval.statusTitle == "Low Blood Pressure (Hypotension)" ->
            "Low Blood Pressure Warning: Sit or rise slowly to prevent dizziness, drink 300–500 ml of water or electrolytes, and monitor closely."
        else ->
            "Hypertensive Crisis Alert: Rest for 5 minutes and re-test. If readings remain elevated, contact emergency medical care immediately."
    }

    val (fallbackExplanation, fallbackActivity, fallbackTip) = when {
        !hasBp -> Triple(
            "No blood pressure measurements are logged yet. Measure while seated with back supported and your arm resting at heart level.",
            "Sit quietly for 5 minutes with feet flat on the floor before recording BP.",
            "Avoid caffeine, smoking, or brisk exercise 30 minutes prior to cuff testing."
        )
        bpEval.statusTitle == "Low Blood Pressure (Hypotension)" -> Triple(
            "Your blood pressure of $currentSys/$currentDia mmHg is below healthy bounds (Hypotension: systolic < 90 or diastolic < 60 mmHg). Insufficient arterial pressure can reduce oxygen perfusion to your brain.",
            "Avoid sudden posture changes or vigorous standing exertion; sit or rest comfortably with legs slightly elevated.",
            "Drink 300–500 ml of water or electrolytes slowly and seek prompt medical advice if dizziness or blurred vision occurs."
        )
        bpEval.statusTitle == "Hypertensive Crisis" -> Triple(
            "Your reading of $currentSys/$currentDia mmHg reaches the Hypertensive Crisis threshold (>= 180 systolic or >= 120 diastolic). Severely elevated arterial pressure stresses blood vessels and organs.",
            "Avoid all physical exertion; sit quietly calmly and re-test after 5 minutes.",
            "Seek emergency medical evaluation immediately if repeat values stay critical or symptoms appear."
        )
        bpEval.statusTitle == "Stage 2 Hypertension" -> Triple(
            "Your blood pressure of $currentSys/$currentDia mmHg falls inside Stage 2 Hypertension (>= 140 systolic or >= 90 diastolic). Sustained arterial resistance in this band should be tracked consistently and shared with a physician.",
            "Perform gentle mobility stretching and paced deep breathing; avoid heavy isometric straining.",
            "Keep sodium intake below 1,500 mg daily, pace water intake, and log a resting morning follow-up."
        )
        bpEval.statusTitle == "Stage 1 Hypertension" -> Triple(
            "Your reading of $currentSys/$currentDia mmHg classifies as Stage 1 Hypertension (130–139 systolic or 80–89 diastolic). Mild increases in arterial tone respond well to consistent aerobic pacing and reduced dietary sodium.",
            "Take an easy 20-minute walk to support endothelial nitric oxide release and lower vascular resistance.",
            "Choose potassium-rich foods (leafy greens, bananas), hydrate steadily, and unwind stress."
        )
        bpEval.statusTitle == "Elevated Blood Pressure" -> Triple(
            "Your blood pressure of $currentSys/$currentDia mmHg is in the Elevated Blood Pressure range (120–129 systolic with diastolic below 80). Addressing mild systolic pressure early protects long-term arterial flexibility.",
            "Enjoy a brisk 20–30 minute walk or gentle cycling session.",
            "Moderate salt intake, ensure steady hydration, and maintain regular sleep hours."
        )
        else -> Triple(
            "Your blood pressure of $currentSys/$currentDia mmHg sits cleanly within Optimal Blood Pressure (90–119 / 60–79 mmHg). Vascular load and resting arterial tension look healthy right now.",
            "Cleared for regular cardio, resistance workouts, or daily brisk activity.",
            "Keep balanced nutrition, hydration, and calm sleep habits to sustain normal arterial pressure."
        )
    }

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

        VitalDetailTopBar(
            title = "Blood Pressure",
            actionLabel = "+ Log BP",
            onBackClick = onBackClick,
            onActionClick = onAddReadingClick
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Hero Vital Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Arterial Pressure",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (hasBp) "$currentSys / $currentDia" else "-- / --",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "mmHg",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 7.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = recordedLabel,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                SyncHealthConnectBpButton(
                    onBpSynced = { reading ->
                        syncedReading = reading
                        onBpSynced(reading)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Analytical Trend Graph with Dynamic Clinical Colors
        BloodPressureTrendChart(
            readings = readings,
            latestSys = currentSys,
            latestDia = currentDia
        )

        if (showMedicalBanner) {
            Spacer(modifier = Modifier.height(14.dp))
            VitalMedicalAlertBanner(message = medicalBannerText)
        }

        Spacer(modifier = Modifier.height(16.dp))

        val isDetailMatchingCurrent = hasBp &&
            detailState?.systolic == currentSys &&
            detailState?.diastolic == currentDia
        VitalOllamaGuidanceCard(
            isLoading = isDetailLoading,
            modelUsed = if (isDetailMatchingCurrent) detailState?.modelUsed else "vitalsync:clinical-engine",
            explanation = if (isDetailMatchingCurrent) {
                detailState?.explanation?.takeIf { it.isNotBlank() } ?: fallbackExplanation
            } else {
                fallbackExplanation
            },
            recommendedActivity = if (isDetailMatchingCurrent) {
                detailState?.recommendedActivity?.takeIf { it.isNotBlank() } ?: fallbackActivity
            } else {
                fallbackActivity
            },
            lifestyleTip = if (isDetailMatchingCurrent) {
                detailState?.lifestyleTip?.takeIf { it.isNotBlank() } ?: fallbackTip
            } else {
                fallbackTip
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        VitalRecentLogsCard(
            title = "Recent BP Logs",
            logs = detailState?.recentLogs.orEmpty(),
            emptyMessage = "No blood pressure logs recorded yet. Tap '+ Log BP' to record your cuff measurement."
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun BloodPressureDetailScreenPreview() {
    VitalsyncTheme {
        BloodPressureDetailScreen(systolic = 118, diastolic = 76)
    }
}
