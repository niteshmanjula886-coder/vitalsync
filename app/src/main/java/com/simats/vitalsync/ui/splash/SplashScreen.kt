package com.simats.vitalsync.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    splashDurationMillis: Long = 2800L,
    onTimeout: () -> Unit = {}
) {
    // Animation entrance states
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.72f) }
    val waveAnim = remember { Animatable(0f) }

    // Continuous infinite glowing transitions
    val infiniteTransition = rememberInfiniteTransition(label = "SplashInfiniteTransition")

    // Pulsating Background Radial Ring Scale
    val ringPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringPulseScale"
    )

    // Continuous ECG Traveling Dot Pulse Progress
    val travelingPulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "travelingPulseProgress"
    )

    LaunchedEffect(Unit) {
        // Trigger fade-in
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        // Trigger spring bounce scale-up for logo
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(250)
        waveAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        delay(splashDurationMillis)
        onTimeout()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF022C22), // Deepest Dark Teal
                        Color(0xFF064E3B), // Dark Emerald Green
                        Color(0xFF047857), // Vivid Emerald Green
                        Color(0xFF0D9488)  // Bright Teal Accent
                    )
                )
            )
    ) {
        // Ambient Radial Background Glow & Concentric Rings
        ConcentricBackgroundRings(pulseScale = ringPulseScale)

        // Main Center Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Logo Box Container (Elevated Frosted Glass Card)
            Box(
                modifier = Modifier
                    .size(118.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.28f),
                                Color.White.copy(alpha = 0.12f)
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.55f),
                                Color.White.copy(alpha = 0.15f)
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Heart with pulse inside
                HeartWithPulseIcon(
                    modifier = Modifier.size(60.dp)
                )

                // Top right 4-point Sparkle icon
                SparkleIcon(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 10.dp)
                        .size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Title
            Text(
                text = "VitalSync",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Sync with your health.",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFA7F3D0)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Live Travelling ECG Heartbeat Pulse Wave Graphic
            EcPulseWave(
                progress = waveAnim.value,
                travelingProgress = travelingPulseProgress,
                modifier = Modifier
                    .fillMaxWidth(0.76f)
                    .height(65.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer Section (App Version & Slogan)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 36.dp)
            ) {
                // Mini Loader Dots Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF34D399)))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF34D399).copy(alpha = 0.6f)))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF34D399).copy(alpha = 0.3f)))
                }

                Text(
                    text = "AI-POWERED HEALTH & WELLNESS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.8.sp,
                    color = Color(0xFFA7F3D0),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "A little clarity. A healthier every day.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ConcentricBackgroundRings(pulseScale: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerPoint = Offset(size.width / 2f, size.height * 0.40f)
        val ringColor = Color.White.copy(alpha = 0.05f)
        val strokeWidthPx = 1.5.dp.toPx()

        // Ambient Radial Core Glow Gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF34D399).copy(alpha = 0.25f),
                    Color(0xFF047857).copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = centerPoint,
                radius = size.width * 0.65f * pulseScale
            ),
            radius = size.width * 0.65f * pulseScale,
            center = centerPoint
        )

        drawCircle(
            color = ringColor,
            center = centerPoint,
            radius = size.width * 0.38f * pulseScale,
            style = Stroke(width = strokeWidthPx)
        )
        drawCircle(
            color = ringColor,
            center = centerPoint,
            radius = size.width * 0.58f * pulseScale,
            style = Stroke(width = strokeWidthPx)
        )
        drawCircle(
            color = ringColor,
            center = centerPoint,
            radius = size.width * 0.80f * pulseScale,
            style = Stroke(width = strokeWidthPx)
        )
    }
}

@Composable
private fun HeartWithPulseIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Heart Path
        val heartPath = Path().apply {
            moveTo(width * 0.5f, height * 0.80f)
            cubicTo(
                width * 0.15f, height * 0.58f,
                width * 0.02f, height * 0.32f,
                width * 0.25f, height * 0.16f
            )
            cubicTo(
                width * 0.38f, height * 0.08f,
                width * 0.48f, height * 0.18f,
                width * 0.50f, height * 0.26f
            )
            cubicTo(
                width * 0.52f, height * 0.18f,
                width * 0.62f, height * 0.08f,
                width * 0.75f, height * 0.16f
            )
            cubicTo(
                width * 0.98f, height * 0.32f,
                width * 0.85f, height * 0.58f,
                width * 0.5f, height * 0.80f
            )
            close()
        }

        // Pulse line across the heart
        val pulsePath = Path().apply {
            moveTo(width * 0.12f, height * 0.48f)
            lineTo(width * 0.32f, height * 0.48f)
            lineTo(width * 0.40f, height * 0.36f)
            lineTo(width * 0.48f, height * 0.62f)
            lineTo(width * 0.56f, height * 0.28f)
            lineTo(width * 0.64f, height * 0.54f)
            lineTo(width * 0.70f, height * 0.48f)
            lineTo(width * 0.88f, height * 0.48f)
        }

        // Draw Heart outline
        drawPath(
            path = heartPath,
            color = Color.White,
            style = Stroke(
                width = 2.6.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw ECG line inside Heart
        drawPath(
            path = pulsePath,
            color = Color.White,
            style = Stroke(
                width = 2.6.dp.toPx(),
                cap = StrokeCap.Round,join = StrokeJoin.Round
            )
        )
    }
}

@Composable
private fun SparkleIcon(modifier: Modifier = Modifier) {
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
            color = Color.White
        )
    }
}

@Composable
private fun EcPulseWave(
    progress: Float,
    travelingProgress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height * 0.5f

        // ECG Wave Path
        val fullPath = Path().apply {
            moveTo(0f, centerY)
            lineTo(width * 0.32f, centerY)
            lineTo(width * 0.38f, centerY - height * 0.22f)
            lineTo(width * 0.44f, centerY + height * 0.38f)
            lineTo(width * 0.50f, centerY - height * 0.45f)
            lineTo(width * 0.56f, centerY + height * 0.15f)
            lineTo(width * 0.62f, centerY)
            lineTo(width, centerY)
        }

        // Faint background guide line
        drawPath(
            path = fullPath,
            color = Color.White.copy(alpha = 0.18f),
            style = Stroke(
                width = 1.4.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Animated revealing path
        if (progress > 0f) {
            val pathMeasure = PathMeasure()
            pathMeasure.setPath(fullPath, false)
            val animatedLength = pathMeasure.length * progress

            val animatedPath = Path()
            pathMeasure.getSegment(0f, animatedLength, animatedPath, true)

            // Draw glowing pulse line
            drawPath(
                path = animatedPath,
                color = Color(0xFFA7F3D0),
                style = Stroke(
                    width = 2.8.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Live Traveling Pulse Point Dot
            if (pathMeasure.length > 0f) {
                val currentOffset = pathMeasure.getPosition(pathMeasure.length * travelingProgress)

                drawCircle(
                    color = Color.White,
                    radius = 4.5.dp.toPx(),
                    center = currentOffset
                )
                drawCircle(
                    color = Color(0xFF34D399).copy(alpha = 0.55f),
                    radius = 9.dp.toPx(),
                    center = currentOffset
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun SplashScreenPreview() {
    VitalsyncTheme {
        SplashScreen()
    }
}
