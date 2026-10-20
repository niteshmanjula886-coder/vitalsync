package com.simats.vitalsync.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.simats.vitalsync.ui.theme.VitalMintAccent
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme

data class OnboardingPageData(
    val badgeText: String,
    val title: String,
    val description: String,
    val bpmValue: String = "76",
    val bpmStatus: String = "BPM - NORMAL",
    val stepCount: String = "6,245 steps"
)

val onboardingPages = listOf(
    OnboardingPageData(
        badgeText = "YOUR HEALTH, CONNECTED",
        title = "Track Your Health",
        description = "Monitor your vital signs, activity and wellness in one place.",
        bpmValue = "76",
        bpmStatus = "BPM - NORMAL",
        stepCount = "6,245 steps"
    ),
    OnboardingPageData(
        badgeText = "AI-POWERED INSIGHTS",
        title = "Smart Clarity",
        description = "Receive personalized health analysis and intelligent wellness recommendations.",
        bpmValue = "72",
        bpmStatus = "BPM - OPTIMAL",
        stepCount = "8,410 steps"
    ),
    OnboardingPageData(
        badgeText = "EVERYDAY WELLBEING",
        title = "Sync Every Moment",
        description = "Seamlessly connect your daily habits for a happier, healthier future.",
        bpmValue = "68",
        bpmStatus = "BPM - RESTING",
        stepCount = "10,120 steps"
    )
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onFinishOnboarding: () -> Unit = {}
) {
    var currentPageIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFCFB))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VitalSync",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = VitalTealDark
            )

            TextButton(
                onClick = onFinishOnboarding
            ) {
                Text(
                    text = "Skip",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B7280)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Center Illustration Card Container
        AnimatedContent(
            targetState = currentPageIndex,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "IllustrationCardTransition",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
        ) { pageIdx ->
            val page = onboardingPages[pageIdx]
            IllustrationCard(pageData = page)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Text & Content Section
        AnimatedContent(
            targetState = currentPageIndex,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "TextSectionTransition",
            modifier = Modifier.fillMaxWidth()
        ) { pageIdx ->
            val page = onboardingPages[pageIdx]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Badge Pill
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFFE6F4EA),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = page.badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46),
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = page.title,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                Text(
                    text = page.description,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Page Indicator Dots
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            onboardingPages.indices.forEach { index ->
                val isSelected = index == currentPageIndex
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(if (isSelected) 28.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) VitalTealDark else Color(0xFFD1D5DB)
                        )
                        .clickable { currentPageIndex = index }
                )
            }
        }

        // Action Button ("Get Started" or "Next")
        Button(
            onClick = {
                if (currentPageIndex < onboardingPages.size - 1) {
                    currentPageIndex++
                } else {
                    onFinishOnboarding()
                }
            },
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
                text = if (currentPageIndex == onboardingPages.size - 1) "Get Started" else "Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Footer Text
        Text(
            text = "Built for your everyday wellbeing.",
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun IllustrationCard(pageData: OnboardingPageData) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFFEFF8F3)),
        contentAlignment = Alignment.Center
    ) {
        // Concentric Soft Background Rings
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerPoint = Offset(size.width / 2f, size.height / 2f)
            val ringColor = Color(0xFF0F9F7A).copy(alpha = 0.12f)
            val strokePx = 1.dp.toPx()

            drawCircle(
                color = ringColor,
                center = centerPoint,
                radius = size.width * 0.28f,
                style = Stroke(width = strokePx)
            )
            drawCircle(
                color = ringColor,
                center = centerPoint,
                radius = size.width * 0.42f,
                style = Stroke(width = strokePx)
            )
            drawCircle(
                color = ringColor,
                center = centerPoint,
                radius = size.width * 0.58f,
                style = Stroke(width = strokePx)
            )
        }

        // Decorative Dots
        // Top-Left Purple Dot
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 52.dp, top = 48.dp)
                .size(10.dp)
                .background(Color(0xFF8B5CF6), CircleShape)
        )

        // Bottom-Right Teal Dot
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 64.dp, bottom = 44.dp)
                .size(12.dp)
                .background(Color(0xFF0F9F7A), CircleShape)
        )

        // Center Heart Rate Main Pill Box
        Box(
            modifier = Modifier
                .width(152.dp)
                .height(210.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(36.dp),
                    spotColor = Color(0xFF033B30).copy(alpha = 0.25f)
                )
                .background(
                    color = Color(0xFF033B30),
                    shape = RoundedCornerShape(36.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                // Heart with pulse icon
                HeartIcon(modifier = Modifier.size(32.dp))

                Spacer(modifier = Modifier.height(12.dp))

                // Big BPM Number
                Text(
                    text = pageData.bpmValue,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Status Label
                Text(
                    text = pageData.bpmStatus,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = VitalMintAccent,
                    letterSpacing = 1.1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mini Pulse Wave Graphic
                MiniPulseLine(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(24.dp)
                )
            }
        }

        // Top Right Floating AI Badge
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 28.dp, top = 36.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                )
                .background(Color.White, RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                SparkleIconPurple(modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B21A8)
                )
            }
        }

        // Bottom Left Floating Steps Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 32.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                )
                .background(Color.White, RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                FootstepsIcon(modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = pageData.stepCount,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }
        }
    }
}

@Composable
private fun HeartIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val heartPath = Path().apply {
            moveTo(w * 0.5f, h * 0.82f)
            cubicTo(
                w * 0.15f, h * 0.58f,
                w * 0.02f, h * 0.30f,
                w * 0.25f, h * 0.14f
            )
            cubicTo(
                w * 0.38f, h * 0.06f,
                w * 0.48f, h * 0.16f,
                w * 0.50f, h * 0.24f
            )
            cubicTo(
                w * 0.52f, h * 0.16f,
                w * 0.62f, h * 0.06f,
                w * 0.75f, h * 0.14f
            )
            cubicTo(
                w * 0.98f, h * 0.30f,
                w * 0.85f, h * 0.58f,
                w * 0.5f, h * 0.82f
            )
            close()
        }

        val pulseLine = Path().apply {
            moveTo(w * 0.22f, h * 0.48f)
            lineTo(w * 0.38f, h * 0.48f)
            lineTo(w * 0.45f, h * 0.34f)
            lineTo(w * 0.53f, h * 0.62f)
            lineTo(w * 0.62f, h * 0.48f)
            lineTo(w * 0.78f, h * 0.48f)
        }

        drawPath(
            path = heartPath,
            color = Color.White,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = pulseLine,
            color = Color.White,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun MiniPulseLine(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerY = h * 0.5f

        val path = Path().apply {
            moveTo(0f, centerY)
            lineTo(w * 0.28f, centerY)
            lineTo(w * 0.35f, centerY - h * 0.30f)
            lineTo(w * 0.43f, centerY + h * 0.40f)
            lineTo(w * 0.51f, centerY - h * 0.48f)
            lineTo(w * 0.58f, centerY + h * 0.20f)
            lineTo(w * 0.65f, centerY)
            lineTo(w, centerY)
        }

        drawPath(
            path = path,
            color = Color(0xFFA7F3D0),
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun SparkleIconPurple(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            quadraticTo(w * 0.5f, h * 0.5f, w, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.5f, w * 0.5f, h)
            quadraticTo(w * 0.5f, h * 0.5f, 0f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.5f, w * 0.5f, 0f)
            close()
        }

        drawPath(
            path = path,
            color = Color(0xFF8B5CF6)
        )
    }
}

@Composable
private fun FootstepsIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Left Shoe footprint
        drawCircle(
            color = Color(0xFF0F9F7A),
            radius = w * 0.16f,
            center = Offset(w * 0.32f, h * 0.32f)
        )
        drawCircle(
            color = Color(0xFF0F9F7A),
            radius = w * 0.14f,
            center = Offset(w * 0.30f, h * 0.68f)
        )

        // Right Shoe footprint
        drawCircle(
            color = Color(0xFF0F9F7A),
            radius = w * 0.16f,
            center = Offset(w * 0.70f, h * 0.22f)
        )
        drawCircle(
            color = Color(0xFF0F9F7A),
            radius = w * 0.14f,
            center = Offset(w * 0.68f, h * 0.58f)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun OnboardingScreenPreview() {
    VitalsyncTheme {
        OnboardingScreen()
    }
}
