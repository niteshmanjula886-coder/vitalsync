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
import com.simats.vitalsync.data.device.WatchSpo2Reading
import com.simats.vitalsync.data.model.vitals.VitalDetailGuidanceResponse
import com.simats.vitalsync.data.model.vitals.VitalReadingItem
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
fun Spo2DetailScreen(
    modifier: Modifier = Modifier,
    detailState: VitalDetailGuidanceResponse? = null,
    isDetailLoading: Boolean = false,
    initialPercentage: Int = 0,
    readings: List<VitalReadingItem> = emptyList(),
    onBackClick: () -> Unit = {},
    onAddReadingClick: () -> Unit = {},
    onSpo2Synced: (WatchSpo2Reading) -> Unit = {}
) {
    var syncedReading by remember { mutableStateOf<WatchSpo2Reading?>(null) }

    val currentSpo2 = syncedReading?.percentage?.takeIf { it > 0 }
        ?: detailState?.currentSpo2?.takeIf { it > 0 }
        ?: initialPercentage.takeIf { it > 0 }

    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground ||
        MaterialTheme.colorScheme.surface == CarbonDarkSurface

    val syncTimeFormatter = remember {
        DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
    }

    val (statusLabel, statusColor, statusBg) = when {
        currentSpo2 == null -> Triple("No Readings Yet", if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280), if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF3F4F6))
        currentSpo2 >= 95 -> Triple("Optimal Oxygenation", if (isDark) Color(0xFF34D399) else Color(0xFF059669), if (isDark) CarbonDarkActivityBg else Color(0xFFECFDF5))
        currentSpo2 >= 90 -> Triple("Mild Hypoxia", if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706), if (isDark) CarbonDarkWarningBg else Color(0xFFFEF3C7))
        else -> Triple("Low Oxygen / Consult Doctor", if (isDark) Color(0xFFF87171) else Color(0xFFDC2626), if (isDark) CarbonDarkCriticalBg else Color(0xFFFEE2E2))
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
        ?: "Tap '+ Log SpO₂' or sync from Health Connect"

    val showMedicalBanner = currentSpo2 != null && currentSpo2 < 90

    val (fallbackExplanation, fallbackActivity, fallbackTip) = when {
        currentSpo2 == null -> Triple(
            "No SpO₂ readings are logged yet. Measure blood oxygen with your hand warm and resting flat at chest height.",
            "Take 3 slow deep breaths while seated upright before logging SpO₂.",
            "Avoid cold fingers or movement during pulse oximetry for the cleanest reading."
        )
        currentSpo2 >= 95 -> Triple(
            "Your SpO₂ saturation of $currentSpo2% indicates optimal arterial oxygenation and healthy lung-to-blood gas exchange. Tissues are receiving abundant oxygen to fuel energy and recovery.",
            "Practice light upright mobility or proceed normally with today's cardio or walking goals.",
            "Keep upright posture and steady water intake to maintain healthy airway hydration."
        )
        currentSpo2 >= 90 -> Triple(
            "Your SpO₂ level of $currentSpo2% sits in the Mild Hypoxia band (90–94%), slightly below optimal oxygenation. Stuffy indoor rooms, shallow chest breathing, or cold extremities can contribute.",
            "Practice diaphragmatic breathing or light upright chest-opening stretches for 5 minutes.",
            "Open a window for fresh ventilation, warm your fingers, and re-measure in 10 minutes."
        )
        else -> Triple(
            "Your SpO₂ saturation of $currentSpo2% is critically low (< 90%), meaning systemic oxygen delivery is reduced. Prompt rest and medical attention are important.",
            "Stop all physical exertion immediately and remain seated upright calmly.",
            "Ensure fresh room airflow and seek clinical medical evaluation urgently."
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
            title = "Blood Oxygen (SpO₂)",
            actionLabel = "+ Log SpO₂",
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
                        text = "Oxygen Saturation",
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
                        text = "${currentSpo2 ?: "--"}%",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SpO₂",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = recordedLabel,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF8FAFC))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Optimal >=95%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF34D399) else Color(0xFF059669)
                    )
                    Text(
                        text = "Mild 90-94%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
                    )
                    Text(
                        text = "Alert <90%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                SyncHealthConnectSpo2Button(
                    onSpo2Synced = { reading ->
                        syncedReading = reading
                        onSpo2Synced(reading)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Analytical Trend Graph with Dynamic Clinical Colors
        Spo2TrendChart(
            readings = readings,
            latestSpo2 = currentSpo2
        )

        if (showMedicalBanner) {
            Spacer(modifier = Modifier.height(14.dp))
            VitalMedicalAlertBanner(
                message = "Critical Oxygen Alert: Readings below 90% require immediate medical attention. Ensure you are upright and seek clinical evaluation."
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val isDetailMatchingCurrent = currentSpo2 != null && detailState?.currentSpo2 == currentSpo2
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
            title = "Recent SpO₂ Logs",
            logs = detailState?.recentLogs.orEmpty(),
            emptyMessage = "No blood oxygen logs recorded yet. Tap '+ Log SpO₂' to record your saturation."
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun Spo2DetailScreenPreview() {
    VitalsyncTheme {
        Spo2DetailScreen(initialPercentage = 98)
    }
}
