package com.simats.vitalsync.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
import com.simats.vitalsync.ui.theme.CarbonDarkAccent
import com.simats.vitalsync.ui.theme.CarbonDarkBackground
import com.simats.vitalsync.ui.theme.CarbonDarkBorder
import com.simats.vitalsync.ui.theme.CarbonDarkSurface
import com.simats.vitalsync.ui.theme.CarbonDarkSurfaceVariant
import com.simats.vitalsync.ui.theme.CarbonDarkTextPrimary
import com.simats.vitalsync.ui.theme.CarbonDarkTextSecondary
import com.simats.vitalsync.ui.theme.CarbonDarkPurpleAccent

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
    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground
    val effectiveTier = (insight?.overallTier?.ifBlank { null }
        ?: insight?.insight?.overallTier?.ifBlank { null }
        ?: "NEEDS_ATTENTION").uppercase()

    val (tierLabel, tierBg, tierTextColor) = when (effectiveTier) {
        "OPTIMAL" -> Triple("Optimal", if (isDark) Color(0xFF142923) else Color(0xFFE8F6F0), if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A))
        "ALERT" -> Triple("Alert", if (isDark) Color(0xFF2E1A1A) else Color(0xFFFEE2E2), if (isDark) Color(0xFFF87171) else Color(0xFFDC2626))
        else -> Triple("Needs Focus", if (isDark) Color(0xFF292314) else Color(0xFFFEF3C7), if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706))
    }

    val evaluations = (insight?.metricEvaluations?.takeIf { it.isNotEmpty() }
        ?: insight?.insight?.metricEvaluations).orEmpty()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) CarbonDarkSurface else Color.White),
        border = if (isDark) BorderStroke(1.dp, CarbonDarkBorder) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.dp)
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
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isDark) CarbonDarkPurpleAccent else Color(0xFF7C3AED)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Health Insight",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827),
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    evaluations.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { eval ->
                                MetricStateChip(
                                    evaluation = eval,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
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
                color = if (isDark) CarbonDarkTextSecondary else Color(0xFF374151)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Targeted Action Tip Card
            val actionTipText = insight?.actionTip?.ifBlank { null }
                ?: insight?.insight?.actionTip?.ifBlank { null }
                ?: "Keep building consistent movement, sleep, and hydration habits today."

            val (tipBg, tipTextColor, tipIcon) = when (effectiveTier) {
                "ALERT" -> Triple(if (isDark) Color(0xFF2E1A1A) else Color(0xFFFEF2F2), if (isDark) Color(0xFFFCA5A5) else Color(0xFF991B1B), Icons.Rounded.WarningAmber)
                "NEEDS_ATTENTION" -> Triple(if (isDark) Color(0xFF292314) else Color(0xFFFFFBEB), if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E), Icons.Rounded.Lightbulb)
                else -> Triple(if (isDark) Color(0xFF142923) else Color(0xFFF0FDF4), if (isDark) Color(0xFFA7F3D0) else Color(0xFF166534), Icons.Rounded.TrackChanges)
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
                    Icon(
                        imageVector = tipIcon,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = tipTextColor
                    )
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
                    color = Color(0xFF9CA3AF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasLoggedBiometrics) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFF0F9F7A)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = if (hasLoggedBiometrics) "Grounded in Logged Vitals" else "• Ready for Vitals Log",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasLoggedBiometrics) Color(0xFF0F9F7A) else Color(0xFF0284C7),
                        maxLines = 1,
                        softWrap = false
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
    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground
    val tierUpper = evaluation.tier.uppercase()
    val (chipBg, badgeColor, labelColor) = when {
        evaluation.tier.equals("Not Logged", ignoreCase = true) ->
            Triple(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF3F4F6), Color(0xFF6B7280), if (isDark) CarbonDarkTextSecondary else Color(0xFF4B5563))
        evaluation.severity >= 2 || tierUpper.contains("ALERT") || tierUpper == "LOW" ->
            Triple(if (isDark) Color(0xFF2E1A1A) else Color(0xFFFEF2F2), Color(0xFFEF4444), if (isDark) Color(0xFFFCA5A5) else Color(0xFF991B1B))
        evaluation.severity == 1 || tierUpper.contains("NEEDS") ->
            Triple(if (isDark) Color(0xFF292314) else Color(0xFFFFFBEB), Color(0xFFF59E0B), if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E))
        else ->
            Triple(if (isDark) Color(0xFF142923) else Color(0xFFECFDF5), Color(0xFF10A37F), if (isDark) Color(0xFFA7F3D0) else Color(0xFF065F46))
    }

    val displayValue = if (evaluation.value.isNotBlank() && !evaluation.value.equals("Not logged", ignoreCase = true)) {
        evaluation.value
    } else {
        "Not logged"
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(chipBg)
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = evaluation.metric,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF374151),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = displayValue,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (displayValue == "Not logged") (if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280)) else (if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = evaluation.status,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                color = labelColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeColor.copy(alpha = 0.12f))
                .padding(horizontal = 7.dp, vertical = 2.dp)
        ) {
            Text(
                text = evaluation.tier,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
