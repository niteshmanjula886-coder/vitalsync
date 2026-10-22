package com.simats.vitalsync.ui.ai

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
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme

@Composable
fun AiScreen(
    modifier: Modifier = Modifier,
    onAskAiClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFCFB))
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
                    color = Color(0xFF111827)
                )

                IconButton(
                    onClick = onAskAiClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    ChatBubbleIcon(modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Hero Wellness Card (Soft Lavender)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAskAiClick() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            BrainAiIcon(modifier = Modifier.size(26.dp))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "YOUR WELLNESS SCORE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C3AED),
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "82 / 100",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your overall wellness has improved this week.",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Insights based on your last 7 days of data.",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Your weekly trends
            Text(
                text = "Your weekly trends",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2x2 Trend Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TrendCard(
                    title = "Activity",
                    value = "↑ 12%",
                    color = Color(0xFF0F9F7A),
                    iconType = TrendIcon.STEPS,
                    modifier = Modifier.weight(1f)
                )

                TrendCard(
                    title = "Sleep",
                    value = "↑ 6%",
                    color = Color(0xFF0F9F7A),
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
                    value = "↓ 4%",
                    color = Color(0xFFD97706),
                    iconType = TrendIcon.WATER,
                    modifier = Modifier.weight(1f)
                )

                TrendCard(
                    title = "Heart Rate",
                    value = "Stable",
                    color = Color(0xFF0F9F7A),
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
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                color = Color(0xFF9CA3AF),
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
    color: Color,
    iconType: TrendIcon,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
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
            color = Color(0xFF111827)
        )
    }
}

@Composable
private fun BrainAiIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.2f)
            cubicTo(w * 0.2f, h * 0.2f, w * 0.1f, h * 0.5f, w * 0.2f, h * 0.8f)
            cubicTo(w * 0.5f, h * 0.9f, w * 0.8f, h * 0.9f, w * 0.8f, h * 0.8f)
            cubicTo(w * 0.9f, h * 0.5f, w * 0.8f, h * 0.2f, w * 0.5f, h * 0.2f)
            close()
        }

        drawPath(path = path, color = Color(0xFF7C3AED), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun ChatBubbleIcon(modifier: Modifier = Modifier) {
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

        drawPath(path = path, color = Color(0xFF374151), style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun StepsIconSmall(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color = Color(0xFF0F9F7A), radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.5f))
    }
}

@Composable
private fun MoonIconSmall(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color = Color(0xFF0F9F7A), radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.5f))
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
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color = Color(0xFF0F9F7A), radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.5f))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun AiScreenPreview() {
    VitalsyncTheme {
        AiScreen()
    }
}
