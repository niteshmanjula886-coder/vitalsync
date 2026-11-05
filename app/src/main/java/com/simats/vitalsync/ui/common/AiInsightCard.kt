package com.simats.vitalsync.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.data.model.ai.AiInsightResponse

@Composable
fun AiInsightCard(
    modifier: Modifier = Modifier,
    insight: AiInsightResponse? = null,
    isLoading: Boolean = false,
    isBtConnected: Boolean = false,
    onPairWatchClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Bar: Sparkles + Title & Score Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "✨", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Health Insight",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3E8FF))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Analyzing...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED)
                        )
                    }
                } else {
                    val score = insight?.score ?: if (isBtConnected) 85 else 68
                    val (scoreBg, scoreColor) = when {
                        score >= 80 -> Pair(Color(0xFFE8F6F0), Color(0xFF0F9F7A))
                        score >= 60 -> Pair(Color(0xFFE0F2FE), Color(0xFF0284C7))
                        else -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706))
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(scoreBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Score: $score/100",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            val titleText = insight?.statusTitle?.ifBlank { null }
                ?: if (isBtConnected) "Vitals & Telemetry Synchronized" else "Logged Metrics Active • Sensor Offline"
            Text(
                text = titleText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isBtConnected) Color(0xFF0F9F7A) else Color(0xFFD97706)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Summary description
            val summaryText = insight?.summary?.ifBlank { null }
                ?: if (isBtConnected) {
                    "Your connected smartwatch shows synchronized heart rate, SpO2, and BP telemetry alongside your logged hydration and sleep."
                } else {
                    "Your logged hydration and sleep are being evaluated. Real-time heart rate, SpO2, and blood pressure sensors are currently offline."
                }
            Text(
                text = summaryText,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = Color(0xFF374151)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Tip Pill (with warning indicator if Bluetooth is disconnected)
            if (!isBtConnected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFFBEB))
                        .clickable { onPairWatchClick() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚠️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = insight?.actionTip?.ifBlank { null }
                                    ?: "Connect your Bluetooth smartwatch or tracker in Settings > Connected Devices to track live heart rate, SpO2, and blood pressure.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF92400E),
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tap here to connect device →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0FDF4))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💡", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = insight?.actionTip?.ifBlank { null }
                                ?: "Hydration is progressing well. Keep up your active momentum and stay properly hydrated throughout the day!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF166534),
                            lineHeight = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Powered by ${insight?.modelUsed ?: "qwen2.5:1.5b"}",
                    fontSize = 10.sp,
                    color = Color(0xFF9CA3AF)
                )

                if (isBtConnected) {
                    Text(
                        text = "• Live BLE Connected ✓",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F9F7A)
                    )
                } else {
                    Text(
                        text = "• Bluetooth Disconnected",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                }
            }
        }
    }
}

