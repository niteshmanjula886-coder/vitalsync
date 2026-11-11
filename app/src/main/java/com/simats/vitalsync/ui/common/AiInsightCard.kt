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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.data.model.ai.AiInsightResponse
import com.simats.vitalsync.data.model.ai.MetricEvaluationDto

@Composable
fun AiInsightCard(
    modifier: Modifier = Modifier,
    insight: AiInsightResponse? = null,
    fallbackScore: Int? = null,
    isLoading: Boolean = false,
    hasLoggedBiometrics: Boolean = false,
    onLogVitalsClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val effectiveTier = (insight?.overallTier?.ifBlank { null }
        ?: insight?.insight?.overallTier?.ifBlank { null }
        ?: "NEEDS_ATTENTION").uppercase()

    val (tierLabel, tierBg, tierTextColor) = when (effectiveTier) {
        "OPTIMAL" -> Triple("Optimal", Color(0xFFE8F6F0), Color(0xFF0F9F7A))
        "ALERT" -> Triple("Alert", Color(0xFFFEE2E2), Color(0xFFDC2626))
        else -> Triple("Needs Focus", Color(0xFFFEF3C7), Color(0xFFD97706))
    }

    val evaluations = (insight?.metricEvaluations?.takeIf { it.isNotEmpty() }
        ?: insight?.insight?.metricEvaluations).orEmpty()

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
            // Header Bar: Sparkles + Title, Overall Tier Badge & Score Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "✨", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Health Insight",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(tierBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tierLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = tierTextColor
                            )
                        }

                        val score = insight?.score ?: fallbackScore ?: 0
                        val (scoreBg, scoreColor) = when {
                            score >= 80 -> Pair(Color(0xFFE8F6F0), Color(0xFF0F9F7A))
                            score >= 55 -> Pair(Color(0xFFE0F2FE), Color(0xFF0284C7))
                            else -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(scoreBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$score/100",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = scoreColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Overarching Headline Diagnosis (status_headline)
            val titleText = insight?.statusTitle?.ifBlank { null }
                ?: insight?.insight?.statusTitle?.ifBlank { null }
                ?: if (hasLoggedBiometrics) "Vitals & Recovery Summary" else "Activity & Recovery Summary"
            Text(
                text = titleText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = tierTextColor
            )

            // Categorized Health State Grid
            if (evaluations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    evaluations.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowItems.forEach { eval ->
                                MetricStateChip(
                                    evaluation = eval,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Holistic Tailored Summary
            val summaryText = insight?.summary?.ifBlank { null }
                ?: insight?.insight?.summary?.ifBlank { null }
                ?: if (hasLoggedBiometrics) {
                    "Synthesizing today's logged Heart Rate, SpO2, BP, steps, sleep, and hydration trends."
                } else {
                    "Tracking today's steps, sleep, and hydration. Tap '+ Log Vitals' to record Heart Rate, SpO2, or Blood Pressure for full AI insights."
                }
            Text(
                text = summaryText,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = Color(0xFF374151)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Targeted Action Tip Card
            val actionTipText = insight?.actionTip?.ifBlank { null }
                ?: insight?.insight?.actionTip?.ifBlank { null }
                ?: "Keep building consistent movement, sleep, and hydration habits today."

            val (tipBg, tipTextColor, tipIcon) = when (effectiveTier) {
                "ALERT" -> Triple(Color(0xFFFEF2F2), Color(0xFF991B1B), "⚠️")
                "NEEDS_ATTENTION" -> Triple(Color(0xFFFFFBEB), Color(0xFF92400E), "💡")
                else -> Triple(Color(0xFFF0FDF4), Color(0xFF166534), "🎯")
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(tipBg)
                    .then(
                        if (!hasLoggedBiometrics) Modifier.clickable { onLogVitalsClick() } else Modifier
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = tipIcon, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = actionTipText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = tipTextColor,
                            lineHeight = 16.sp
                        )
                        if (!hasLoggedBiometrics) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+ Log Vitals manually for complete cardiovascular grading →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F9F7A)
                            )
                        }
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

                if (hasLoggedBiometrics) {
                    Text(
                        text = "• Grounded in Logged Vitals ✓",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F9F7A)
                    )
                } else {
                    Text(
                        text = "• Ready for Vitals Log",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricStateChip(
    evaluation: MetricEvaluationDto,
    modifier: Modifier = Modifier
) {
    val tierUpper = evaluation.tier.uppercase()
    val (chipBg, badgeColor, labelColor) = when {
        evaluation.tier.equals("Not Logged", ignoreCase = true) ->
            Triple(Color(0xFFF3F4F6), Color(0xFF6B7280), Color(0xFF4B5563))
        evaluation.severity >= 2 || tierUpper.contains("ALERT") || tierUpper == "LOW" ->
            Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), Color(0xFF991B1B))
        evaluation.severity == 1 || tierUpper.contains("NEEDS") ->
            Triple(Color(0xFFFFFBEB), Color(0xFFD97706), Color(0xFF92400E))
        else ->
            Triple(Color(0xFFECFDF5), Color(0xFF059669), Color(0xFF065F46))
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(chipBg)
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = evaluation.metric,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = evaluation.tier,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = if (evaluation.value.isNotBlank() && !evaluation.value.equals("Not logged", ignoreCase = true)) {
                "${evaluation.status} • ${evaluation.value}"
            } else {
                evaluation.status
            },
            fontSize = 10.sp,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
