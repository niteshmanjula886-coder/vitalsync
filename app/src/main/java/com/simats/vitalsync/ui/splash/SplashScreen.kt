package com.simats.vitalsync.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.simats.vitalsync.ui.theme.SplashBgDark
import com.simats.vitalsync.ui.theme.SplashBgLight
import com.simats.vitalsync.ui.theme.SplashBgMid
import com.simats.vitalsync.ui.theme.VitalTextFooterBody
import com.simats.vitalsync.ui.theme.VitalTextFooterTitle
import com.simats.vitalsync.ui.theme.VitalTextSubtitle
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    splashDurationMillis: Long = 2800L,
    onTimeout: () -> Unit = {}
) {
    // Animation states
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.85f) }
    val waveAnim = remember { Animatable(0f) }

    // Subtle breathing glow animation for heartbeat line
    val infiniteTransition = rememberInfiniteTransition(label = "pulseGlow")
    val pulseGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlowAlpha"
    )

    LaunchedEffect(Unit) {
        // Trigger fade-in and scale-up concurrently
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        delay(300)
        waveAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = LinearEasing)
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
                        SplashBgDark,
                        SplashBgMid,
                        Color(0xFF096C54),
                        SplashBgLight
                    )
                )
            )
    ) {
        // Concentric subtle background rings centered around the logo
        ConcentricBackgroundRings()

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

            // Logo Box Container (Translucent glass card)
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .background(
                        color = Color(0x2BFFFFFF),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Heart with pulse inside
                HeartWithPulseIcon(
                    modifier = Modifier.size(56.dp)
                )

                // Top right 4-point Sparkle icon
                SparkleIcon(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 10.dp)
                        .size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Title
            Text(
                text = "VitalSync",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle
            Text(
                text = "Sync with your health.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = VitalTextSubtitle
            )

            Spacer(modifier = Modifier.height(44.dp))

            // Animated Heartbeat Pulse Wave Graphic
            EcPulseWave(
                progress = waveAnim.value,
                glowAlpha = pulseGlowAlpha,
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(60.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 36.dp)
            ) {
                Text(
                    text = "AI-POWERED HEALTH & WELLNESS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.6.sp,
                    color = VitalTextFooterTitle,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "A little clarity. A healthier every day.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = VitalTextFooterBody,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ConcentricBackgroundRings() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerPoint = Offset(size.width / 2f, size.height * 0.40f)
        val ringColor = Color.White.copy(alpha = 0.04f)
        val strokeWidthPx = 1.2.dp.toPx()

        drawCircle(
            color = ringColor,
            center = centerPoint,
            radius = size.width * 0.38f,
            style = Stroke(width = strokeWidthPx)
        )
        drawCircle(
            color = ringColor,
            center = centerPoint,
            radius = size.width * 0.58f,
            style = Stroke(width = strokeWidthPx)
        )
        drawCircle(
            color = ringColor,
            center = centerPoint,
            radius = size.width * 0.80f,
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
            // Left curve
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
            // Right curve
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
                width = 2.4.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw ECG line inside Heart
        drawPath(
            path = pulsePath,
            color = Color.White,
            style = Stroke(
                width = 2.4.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
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
    glowAlpha: Float,
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
            color = Color.White.copy(alpha = 0.15f),
            style = Stroke(
                width = 1.2.dp.toPx(),
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
                color = Color(0xFFA7F3D0).copy(alpha = glowAlpha),
                style = Stroke(
                    width = 2.6.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
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
