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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.data.model.vitals.VitalDetailGuidanceResponse
import com.simats.vitalsync.ui.theme.VitalsyncTheme

@Composable
fun HeartRateDetailScreen(
    modifier: Modifier = Modifier,
    uiState: HeartRateUiState = HeartRateUiState(),
    detailState: VitalDetailGuidanceResponse? = null,
    isDetailLoading: Boolean = false,
    initialBpm: Int = 0,
    restingBpm: Int = 0,
    minBpm: Int = 0,
    maxBpm: Int = 0,
    avgBpm: Int = 0,
    showAiInsights: Boolean = true,
    onBackClick: () -> Unit = {},
    onAddReadingClick: () -> Unit = {},
    onReadingReviewClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {}
) {
    val currentBpm = detailState?.currentBpm?.takeIf { it > 0 }
        ?: uiState.currentBpm?.takeIf { it > 0 }
        ?: initialBpm.takeIf { it > 0 }

    val avgBpmVal = uiState.averageBpm ?: avgBpm.takeIf { it > 0 } ?: currentBpm
    val maxBpmVal = uiState.highestBpm ?: maxBpm.takeIf { it > 0 } ?: currentBpm
    val minBpmVal = uiState.lowestBpm ?: minBpm.takeIf { it > 0 } ?: currentBpm
    val restingBpmVal = uiState.restingBpm ?: restingBpm.takeIf { it > 0 }

    val (statusLabel, statusColor, statusBg) = when {
        currentBpm == null -> Triple("No Readings Yet", Color(0xFF6B7280), Color(0xFFF3F4F6))
        currentBpm < 50 -> Triple("Severe Bradycardia", Color(0xFFDC2626), Color(0xFFFEE2E2))
        currentBpm < 60 -> Triple("Athletic / Low Resting", Color(0xFF0284C7), Color(0xFFE0F2FE))
        currentBpm <= 100 -> Triple("Optimal Resting", Color(0xFF059669), Color(0xFFECFDF5))
        currentBpm <= 129 -> Triple("Elevated", Color(0xFFD97706), Color(0xFFFEF3C7))
        else -> Triple("Tachycardia / High", Color(0xFFDC2626), Color(0xFFFEE2E2))
    }

    val recordedLabel = detailState?.recordedAt?.takeIf { it.isNotBlank() }?.let { "Recorded $it" }
        ?: uiState.formattedTime.takeIf { it.isNotBlank() && it != "--" }?.let { "Recorded $it" }
        ?: "Tap '+ Log Reading' to log your resting pulse"

    val showMedicalBanner = currentBpm != null && (currentBpm < 50 || currentBpm >= 130)

    val (fallbackExplanation, fallbackActivity, fallbackTip) = when {
        currentBpm == null -> Triple(
            "No heart rate measurements are logged yet. Record a manual pulse reading while seated calmly for baseline tracking.",
            "Rest seated for 2–3 minutes before logging your pulse.",
            "Morning measurements prior to caffeine yield your most consistent resting baseline."
        )
        currentBpm < 50 -> Triple(
            "Your heart rate of $currentBpm BPM falls in the Severe Bradycardia range (< 50 BPM). Unless you are an endurance athlete, very slow pulse rates require clinical review.",
            "Stop strenuous exertion immediately and rest safely in a seated or lying position.",
            "Consult a healthcare provider promptly if you experience dizziness, faintness, or chest tightness."
        )
        currentBpm < 60 -> Triple(
            "Your heart rate of $currentBpm BPM reflects strong vagal efficiency in the Athletic / Low Resting range (50–59 BPM). Heart muscle stroke volume is keeping circulation steady at low beat frequency.",
            "Include a 5–10 minute gradual warm-up before higher-intensity training.",
            "Stay hydrated and track if any fatigue accompanies sub-60 BPM readings."
        )
        currentBpm <= 100 -> Triple(
            "Your resting heart rate of $currentBpm BPM sits squarely in the Optimal Resting range (60–100 BPM). This indicates balanced autonomic nervous system recovery right now.",
            "Engage in 10–25 minutes of brisk walking or steady aerobic movement.",
            "Keep consistent hydration and balanced sleep pacing to maintain optimal resting pulse."
        )
        currentBpm <= 129 -> Triple(
            "Your heart rate of $currentBpm BPM is Elevated above resting baseline (101–129 BPM). Temporary elevation can stem from recent exertion, dehydration, caffeine, or acute stress.",
            "Avoid vigorous exercise; perform 5 minutes of slow nasal or box breathing.",
            "Drink a glass of water, limit stimulants, and recheck after 15 minutes of calm rest."
        )
        else -> Triple(
            "Your heart rate of $currentBpm BPM sits in the Tachycardia / High zone (>= 130 BPM). Resting pulses at this level increase myocardial workload and need prompt attention.",
            "Cease physical exertion immediately, sit upright calmly, and focus on slow breathing.",
            "Take small sips of water and contact a healthcare professional urgently if high pulse persists."
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFCFB))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        VitalDetailTopBar(
            title = "Heart Rate",
            actionLabel = "+ Log Reading",
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
                        text = "Latest Reading",
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
                        text = currentBpm?.toString() ?: "--",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BPM",
                        fontSize = 16.sp,
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

                if (avgBpmVal != null || minBpmVal != null || maxBpmVal != null || restingBpmVal != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatMiniMetric(label = "Avg", value = avgBpmVal?.let { "$it BPM" } ?: "--")
                        StatMiniMetric(label = "Min", value = minBpmVal?.let { "$it BPM" } ?: "--")
                        StatMiniMetric(label = "Max", value = maxBpmVal?.let { "$it BPM" } ?: "--")
                        StatMiniMetric(label = "Resting", value = restingBpmVal?.let { "$it BPM" } ?: "--")
                    }
                }
            }
        }

        if (showMedicalBanner) {
            Spacer(modifier = Modifier.height(14.dp))
            VitalMedicalAlertBanner(
                message = "Medical Alert: If you feel dizzy, faint, or experience chest tightness, sit down and consult a healthcare provider immediately."
            )
        }

        if (showAiInsights) {
            Spacer(modifier = Modifier.height(16.dp))
            VitalOllamaGuidanceCard(
                isLoading = isDetailLoading,
                modelUsed = detailState?.modelUsed,
                explanation = detailState?.explanation?.takeIf { it.isNotBlank() } ?: fallbackExplanation,
                recommendedActivity = detailState?.recommendedActivity?.takeIf { it.isNotBlank() } ?: fallbackActivity,
                lifestyleTip = detailState?.lifestyleTip?.takeIf { it.isNotBlank() } ?: fallbackTip
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        VitalRecentLogsCard(
            title = "Recent Heart Rate Logs",
            logs = detailState?.recentLogs.orEmpty(),
            emptyMessage = "No heart rate logs recorded yet. Tap '+ Log Reading' to add your first entry."
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StatMiniMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = Color(0xFF6B7280))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun HeartRateDetailScreenPreview() {
    VitalsyncTheme {
        HeartRateDetailScreen(initialBpm = 74)
    }
}
