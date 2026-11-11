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
fun Spo2DetailScreen(
    modifier: Modifier = Modifier,
    detailState: VitalDetailGuidanceResponse? = null,
    isDetailLoading: Boolean = false,
    initialPercentage: Int = 0,
    onBackClick: () -> Unit = {},
    onAddReadingClick: () -> Unit = {}
) {
    val currentSpo2 = detailState?.currentSpo2?.takeIf { it > 0 }
        ?: initialPercentage.takeIf { it > 0 }

    val (statusLabel, statusColor, statusBg) = when {
        currentSpo2 == null -> Triple("No Readings Yet", Color(0xFF6B7280), Color(0xFFF3F4F6))
        currentSpo2 >= 95 -> Triple("Optimal Oxygenation", Color(0xFF059669), Color(0xFFECFDF5))
        currentSpo2 >= 90 -> Triple("Mild Hypoxia", Color(0xFFD97706), Color(0xFFFEF3C7))
        else -> Triple("Low Oxygen / Consult Doctor", Color(0xFFDC2626), Color(0xFFFEE2E2))
    }

    val recordedLabel = detailState?.recordedAt?.takeIf { it.isNotBlank() }?.let { "Recorded $it" }
        ?: "Tap '+ Log SpO₂' to record blood oxygen saturation"

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
            .background(Color(0xFFFAFCFB))
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
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "● Optimal ≥95%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                    Text(text = "● Mild 90–94%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                    Text(text = "● Alert <90%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                }
            }
        }

        if (showMedicalBanner) {
            Spacer(modifier = Modifier.height(14.dp))
            VitalMedicalAlertBanner(
                message = "Critical Oxygen Alert: Readings below 90% require immediate medical attention. Ensure you are upright and seek clinical evaluation."
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        VitalOllamaGuidanceCard(
            isLoading = isDetailLoading,
            modelUsed = detailState?.modelUsed,
            explanation = detailState?.explanation?.takeIf { it.isNotBlank() } ?: fallbackExplanation,
            recommendedActivity = detailState?.recommendedActivity?.takeIf { it.isNotBlank() } ?: fallbackActivity,
            lifestyleTip = detailState?.lifestyleTip?.takeIf { it.isNotBlank() } ?: fallbackTip
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
