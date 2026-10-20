package com.simats.vitalsync.ui.vitals

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

enum class VitalReadingType {
    HEART_RATE,
    BLOOD_PRESSURE,
    SPO2,
    HYDRATION,
    STEPS,
    SLEEP
}

@Composable
fun AddReadingScreen(
    modifier: Modifier = Modifier,
    readingType: VitalReadingType = VitalReadingType.HEART_RATE,
    onBackClick: () -> Unit = {},
    onDoneClick: () -> Unit = {}
) {
    val (title, value, status) = when (readingType) {
        VitalReadingType.HEART_RATE -> Triple("Heart Rate", "76 BPM", "Normal")
        VitalReadingType.BLOOD_PRESSURE -> Triple("Blood Pressure", "120/80 mmHg", "Normal")
        VitalReadingType.SPO2 -> Triple("SpO₂", "98%", "Optimal")
        VitalReadingType.HYDRATION -> Triple("Hydration", "+250 ml", "Goal Progress")
        VitalReadingType.STEPS -> Triple("Step Count", "6,245 steps", "Goal Progress")
        VitalReadingType.SLEEP -> Triple("Sleep", "7h 32m", "Restful")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFCFB))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
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
            Spacer(modifier = Modifier.height(16.dp))

            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    BackArrowIcon(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF111827)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Add Reading",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Success Checkmark Outer Circle Badge
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF2FAF6)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE6F4EA)),
                    contentAlignment = Alignment.Center
                ) {
                    CheckmarkLargeIcon(modifier = Modifier.size(44.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Success Header
            Text(
                text = "Reading Added\nSuccessfully",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Your latest health reading has been saved.",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Saved Reading Vital Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ReadingTypeIcon(
                                readingType = readingType,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE6F4EA))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F9F7A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = value,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Today at 9:38 AM · Manually added",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Done Button & Footer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Button(
                onClick = onDoneClick,
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
                    text = "Done",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "One more step toward understanding your health.",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReadingTypeIcon(
    readingType: VitalReadingType,
    modifier: Modifier = Modifier
) {
    val tealColor = Color(0xFF0F9F7A)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (readingType) {
            VitalReadingType.HEART_RATE -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.82f)
                    cubicTo(w * 0.15f, h * 0.58f, w * 0.02f, h * 0.30f, w * 0.25f, h * 0.14f)
                    cubicTo(w * 0.38f, h * 0.06f, w * 0.48f, h * 0.16f, w * 0.50f, h * 0.24f)
                    cubicTo(w * 0.52f, h * 0.16f, w * 0.62f, h * 0.06f, w * 0.75f, h * 0.14f)
                    cubicTo(w * 0.98f, h * 0.30f, w * 0.85f, h * 0.58f, w * 0.5f, h * 0.82f)
                    close()
                }
                drawPath(path = path, color = tealColor, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            VitalReadingType.BLOOD_PRESSURE -> {
                val path = Path().apply {
                    moveTo(0f, h * 0.5f)
                    lineTo(w * 0.3f, h * 0.5f)
                    lineTo(w * 0.5f, h * 0.15f)
                    lineTo(w * 0.7f, h * 0.85f)
                    lineTo(w, h * 0.5f)
                }
                drawPath(path = path, color = tealColor, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            VitalReadingType.SPO2 -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, 0f)
                    cubicTo(w * 0.1f, h * 0.5f, w * 0.1f, h * 0.9f, w * 0.5f, h * 0.9f)
                    cubicTo(w * 0.9f, h * 0.9f, w * 0.9f, h * 0.5f, w * 0.5f, 0f)
                    close()
                }
                drawPath(path = path, color = tealColor, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            VitalReadingType.HYDRATION -> {
                val glassPath = Path().apply {
                    moveTo(w * 0.22f, h * 0.18f)
                    lineTo(w * 0.78f, h * 0.18f)
                    lineTo(w * 0.70f, h * 0.85f)
                    lineTo(w * 0.30f, h * 0.85f)
                    close()
                }
                drawPath(path = glassPath, color = tealColor, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            VitalReadingType.STEPS -> {
                drawCircle(color = tealColor, radius = w * 0.16f, center = Offset(w * 0.32f, h * 0.32f))
                drawCircle(color = tealColor, radius = w * 0.14f, center = Offset(w * 0.30f, h * 0.68f))
                drawCircle(color = tealColor, radius = w * 0.16f, center = Offset(w * 0.70f, h * 0.22f))
                drawCircle(color = tealColor, radius = w * 0.14f, center = Offset(w * 0.68f, h * 0.58f))
            }
            VitalReadingType.SLEEP -> {
                val moonPath = Path().apply {
                    moveTo(w * 0.75f, h * 0.15f)
                    cubicTo(w * 0.25f, h * 0.25f, w * 0.25f, h * 0.75f, w * 0.75f, h * 0.85f)
                    cubicTo(w * 0.45f, h * 0.75f, w * 0.45f, h * 0.25f, w * 0.75f, h * 0.15f)
                    close()
                }
                drawPath(path = moonPath, color = tealColor, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}

@Composable
private fun CheckmarkLargeIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.5f)
            lineTo(w * 0.45f, h * 0.75f)
            lineTo(w * 0.85f, h * 0.25f)
        }

        drawPath(
            path = path,
            color = Color(0xFF0F9F7A),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun BackArrowIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Black
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.75f, h * 0.5f)
            lineTo(w * 0.25f, h * 0.5f)
            moveTo(w * 0.45f, h * 0.25f)
            lineTo(w * 0.25f, h * 0.5f)
            lineTo(w * 0.45f, h * 0.75f)
        }

        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun AddReadingScreenPreview() {
    VitalsyncTheme {
        AddReadingScreen(readingType = VitalReadingType.HEART_RATE)
    }
}
