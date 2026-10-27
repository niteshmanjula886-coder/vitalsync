package com.simats.vitalsync.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.simats.vitalsync.ui.theme.VitalPrimaryMint
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    userName: String = "Alex",
    onSetUpProfileClick: () -> Unit = {},
    onSkipToDashboardClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Heading Title
            Text(
                text = "Welcome to Your\nHealth Journey!",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle
            Text(
                text = "We're so glad you're here!\nLet's get to know you better.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Winding Pathway Sun Illustration matching reference image
            PathwaySunIllustration(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                sunColor = VitalPrimaryMint
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Action Button: Get Started ->
        Button(
            onClick = onSetUpProfileClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VitalTealDark,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Get Started →",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PathwaySunIllustration(
    modifier: Modifier = Modifier,
    sunColor: Color = Color(0xFF00A884)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Horizon Line
        drawLine(
            color = Color(0xFF14202D),
            start = Offset(w * 0.05f, h * 0.42f),
            end = Offset(w * 0.95f, h * 0.42f),
            strokeWidth = 1.8.dp.toPx()
        )

        // Sun Rays
        val sunCx = w * 0.5f
        val sunCy = h * 0.30f
        repeat(8) { i ->
            val angle = (i * 45f) * (Math.PI.toFloat() / 180f)
            val startR = w * 0.14f
            val endR = startR + 8.dp.toPx()
            val sx = sunCx + startR * cos(angle)
            val sy = sunCy + startR * sin(angle)
            val ex = sunCx + endR * cos(angle)
            val ey = sunCy + endR * sin(angle)
            drawLine(
                color = sunColor,
                start = Offset(sx, sy),
                end = Offset(ex, ey),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Sun Sphere Fill
        drawCircle(
            color = Color(0xFFC7EBDD),
            radius = w * 0.11f,
            center = Offset(sunCx, sunCy)
        )
        drawCircle(
            color = sunColor,
            radius = w * 0.11f,
            center = Offset(sunCx, sunCy),
            style = Stroke(width = 2.dp.toPx())
        )

        // Sprout Leaf Icon inside Sun
        val sproutPath = Path().apply {
            moveTo(sunCx, sunCy + 8.dp.toPx())
            lineTo(sunCx, sunCy - 8.dp.toPx())
            moveTo(sunCx, sunCy)
            cubicTo(sunCx - 10.dp.toPx(), sunCy - 12.dp.toPx(), sunCx - 14.dp.toPx(), sunCy - 2.dp.toPx(), sunCx, sunCy)
            cubicTo(sunCx + 10.dp.toPx(), sunCy - 12.dp.toPx(), sunCx + 14.dp.toPx(), sunCy - 2.dp.toPx(), sunCx, sunCy)
        }
        drawPath(
            path = sproutPath,
            color = sunColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Winding Road / Pathway Path Fill
        val roadFill = Path().apply {
            moveTo(sunCx - 8.dp.toPx(), h * 0.42f)
            cubicTo(
                w * 0.35f, h * 0.52f,
                w * 0.65f, h * 0.62f,
                w * 0.25f, h * 0.72f
            )
            cubicTo(
                w * 0.10f, h * 0.78f,
                w * 0.05f, h * 0.90f,
                0f, h * 0.98f
            )
            lineTo(w * 0.35f, h * 0.98f)
            cubicTo(
                w * 0.35f, h * 0.90f,
                w * 0.40f, h * 0.78f,
                w * 0.55f, h * 0.72f
            )
            cubicTo(
                w * 0.85f, h * 0.62f,
                w * 0.48f, h * 0.52f,
                sunCx + 8.dp.toPx(), h * 0.42f
            )
            close()
        }

        drawPath(
            path = roadFill,
            color = Color(0xFFE8F6F0)
        )

        val roadOutlineLeft = Path().apply {
            moveTo(sunCx - 8.dp.toPx(), h * 0.42f)
            cubicTo(
                w * 0.35f, h * 0.52f,
                w * 0.65f, h * 0.62f,
                w * 0.25f, h * 0.72f
            )
            cubicTo(
                w * 0.10f, h * 0.78f,
                w * 0.05f, h * 0.90f,
                0f, h * 0.98f
            )
        }

        val roadOutlineRight = Path().apply {
            moveTo(sunCx + 8.dp.toPx(), h * 0.42f)
            cubicTo(
                w * 0.48f, h * 0.52f,
                w * 0.85f, h * 0.62f,
                w * 0.55f, h * 0.72f
            )
            cubicTo(
                w * 0.40f, h * 0.78f,
                w * 0.35f, h * 0.90f,
                w * 0.35f, h * 0.98f
            )
        }

        drawPath(
            path = roadOutlineLeft,
            color = Color(0xFF14202D),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        drawPath(
            path = roadOutlineRight,
            color = Color(0xFF14202D),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Road Perspective Steps
        repeat(5) { step ->
            val ratio = (step + 1) / 6f
            val yPos = h * 0.42f + (h * 0.56f) * ratio
            drawLine(
                color = Color(0xFF14202D).copy(alpha = 0.25f),
                start = Offset(w * (0.45f - ratio * 0.20f), yPos),
                end = Offset(w * (0.55f + ratio * 0.10f), yPos),
                strokeWidth = 1.2.dp.toPx()
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun WelcomeScreenPreview() {
    VitalsyncTheme {
        WelcomeScreen()
    }
}
