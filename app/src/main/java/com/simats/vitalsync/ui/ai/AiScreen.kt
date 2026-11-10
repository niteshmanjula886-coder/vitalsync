package com.simats.vitalsync.ui.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.data.model.ai.AiInsightResponse
import com.simats.vitalsync.data.model.profile.WeeklyReportDto
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import java.util.Locale

@Composable
fun AiScreen(
    modifier: Modifier = Modifier,
    wellnessScore: Int = 0,
    aiInsightTitle: String? = null,
    aiInsightSummary: String? = null,
    aiInsight: AiInsightResponse? = null,
    isInsightLoading: Boolean = false,
    isBtConnected: Boolean = false,
    weeklyReport: WeeklyReportDto? = null,
    onPairWatchClick: () -> Unit = {},
    onAskAiClick: () -> Unit = {}
) {
    val metrics = weeklyReport?.metrics
    val totalSteps = metrics?.totalSteps ?: 0
    val workoutCount = metrics?.workoutCount ?: 0
    val activityTrendValue = String.format(Locale.US, "%,d steps", totalSteps)
    val activityTrendSub = "$workoutCount workout${if (workoutCount == 1) "" else "s"} (7d)"

    val avgSleepMinutes = weeklyReport?.resolvedAvgSleepMinutes() ?: 0
    val sleepTrendValue = if (avgSleepMinutes > 0) {
        String.format(Locale.US, "%.1f hrs", avgSleepMinutes / 60f)
    } else {
        "--"
    }
    val sleepTrendSub = weeklyReport?.resolvedSleepTrendTier() ?: "No logs yet"
    val sleepTrendColor = when (sleepTrendSub) {
        "Optimal" -> MaterialTheme.colorScheme.primary
        "Fair" -> Color(0xFFD97706)
        "Needs Attention" -> Color(0xFFDC2626)
        else -> Color(0xFF6B7280)
    }

    val totalWaterMl = metrics?.totalHydrationMl ?: 0
    val hydrationTrendValue = when {
        totalWaterMl >= 1000 -> String.format(Locale.US, "%.1f L", totalWaterMl / 1000f)
        else -> "$totalWaterMl ml"
    }
    val hydrationTrendSub = "Rolling 7d total"

    val avgHr = metrics?.avgHrBpm ?: 0
    val hrTrendValue = when {
        avgHr > 0 -> "$avgHr BPM"
        !isBtConnected -> "No Device"
        !metrics?.heartRateAvg.isNullOrBlank() && metrics?.heartRateAvg != "-- BPM" -> metrics!!.heartRateAvg
        else -> "--"
    }
    val hrTrendSub = when {
        avgHr > 0 -> "7d average"
        !isBtConnected -> "Pair wearable"
        else -> "No readings"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VitalSync AI ✨",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = onAskAiClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    ChatBubbleIcon(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onBackground)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Your weekly trends
            Text(
                text = "Your weekly trends",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2x2 Trend Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TrendCard(
                    title = "Activity",
                    value = activityTrendValue,
                    subtitle = activityTrendSub,
                    color = MaterialTheme.colorScheme.primary,
                    iconType = TrendIcon.STEPS,
                    modifier = Modifier.weight(1f)
                )

                TrendCard(
                    title = "Sleep",
                    value = sleepTrendValue,
                    subtitle = sleepTrendSub,
                    color = sleepTrendColor,
                    iconType = TrendIcon.SLEEP,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TrendCard(
                    title = "Hydration",
                    value = hydrationTrendValue,
                    subtitle = hydrationTrendSub,
                    color = if (totalWaterMl > 0) MaterialTheme.colorScheme.primary else Color(0xFFD97706),
                    iconType = TrendIcon.WATER,
                    modifier = Modifier.weight(1f)
                )

                TrendCard(
                    title = "Heart Rate",
                    value = hrTrendValue,
                    subtitle = hrTrendSub,
                    color = if (avgHr > 0) MaterialTheme.colorScheme.primary else Color(0xFF6B7280),
                    iconType = TrendIcon.HEART,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Small steps for today
            Text(
                text = "Small steps for today",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    StepActionRow(icon = "👟", text = "Take a 15-minute walk today.")
                    Spacer(modifier = Modifier.height(14.dp))
                    StepActionRow(icon = "🌙", text = "Maintain a consistent bedtime.")
                    Spacer(modifier = Modifier.height(14.dp))
                    StepActionRow(icon = "💧", text = "Drink another glass of water.")
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Ask VitalSync AI Button Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Button(
                onClick = onAskAiClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VitalTealDark,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Ask VitalSync AI",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Wellness guidance, not a medical diagnosis.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

enum class TrendIcon {
    STEPS,
    SLEEP,
    WATER,
    HEART
}

@Composable
private fun TrendCard(
    title: String,
    value: String,
    subtitle: String? = null,
    color: Color,
    iconType: TrendIcon,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                    when (iconType) {
                        TrendIcon.STEPS -> StepsIconSmall(modifier = Modifier.size(14.dp))
                        TrendIcon.SLEEP -> MoonIconSmall(modifier = Modifier.size(14.dp))
                        TrendIcon.WATER -> WaterIconSmall(modifier = Modifier.size(14.dp))
                        TrendIcon.HEART -> HeartIconSmall(modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun StepActionRow(
    icon: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3E8FF)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ChatBubbleIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF374151)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.15f, h * 0.2f)
            lineTo(w * 0.85f, h * 0.2f)
            lineTo(w * 0.85f, h * 0.65f)
            lineTo(w * 0.4f, h * 0.65f)
            lineTo(w * 0.2f, h * 0.85f)
            lineTo(w * 0.2f, h * 0.65f)
            lineTo(w * 0.15f, h * 0.65f)
            close()
        }

        drawPath(path = path, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun StepsIconSmall(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color = primary, radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.5f))
    }
}

@Composable
private fun MoonIconSmall(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color = primary, radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.5f))
    }
}

@Composable
private fun WaterIconSmall(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color = Color(0xFFD97706), radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.5f))
    }
}

@Composable
private fun HeartIconSmall(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color = primary, radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.5f))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun AiScreenPreview() {
    VitalsyncTheme {
        AiScreen()
    }
}
