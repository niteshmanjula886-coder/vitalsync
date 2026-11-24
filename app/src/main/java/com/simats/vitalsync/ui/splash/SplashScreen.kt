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
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.simats.vitalsync.R
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
                        Color(0xFF0D1013), // Deep Obsidian Charcoal
                        Color(0xFF121619), // Concept 24 Launch Screen Dark Canvas
                        Color(0xFF181C20), // Ambient Charcoal
                        Color(0xFF0F171A)  // Deep Subtle Teal-Charcoal Undertone
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

            // Concept 24 Frameless Emblem (Seamlessly Integrated, No Square Box Frame)
            Box(
                modifier = Modifier.size(118.dp),
                contentAlignment = Alignment.Center
            ) {
                // Ambient Radial Glow behind the emblem
                Canvas(modifier = Modifier.size(118.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF34D399).copy(alpha = 0.20f),
                                Color(0xFF10B981).copy(alpha = 0.06f),
                                Color.Transparent
                            ),
                            radius = size.width * 0.48f
                        )
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.ic_splash_emblem),
                    contentDescription = "VitalSync Logo",
                    modifier = Modifier.size(108.dp)
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
