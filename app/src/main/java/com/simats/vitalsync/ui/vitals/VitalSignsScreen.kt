package com.simats.vitalsync.ui.vitals

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.VitalCardBg
import com.simats.vitalsync.ui.theme.VitalMainBackground
import com.simats.vitalsync.ui.theme.VitalNavSelectedBg
import com.simats.vitalsync.ui.theme.VitalPrimaryMint
import com.simats.vitalsync.ui.theme.VitalSoftBorder
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTextPrimary
import com.simats.vitalsync.ui.theme.VitalTextSecondary
import com.simats.vitalsync.ui.theme.VitalsyncTheme

@Composable
fun VitalSignsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onHeartRateClick: () -> Unit = {},
    onBloodPressureClick: () -> Unit = {},
    onSpO2Click: () -> Unit = {},
    onBmiClick: () -> Unit = {},
    onAddReadingClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VitalMainBackground)
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        BackArrowIcon(
                            modifier = Modifier.size(20.dp),
                            color = VitalTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Vital Signs",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = VitalTextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    CalendarIcon(modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6 Grid Metric Cards with Dynamic Classification Colors
            // Row 1: Heart Rate & Blood Pressure
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VitalTileCard(
                    title = "Heart Rate",
                    value = "76 bpm",
                    status = "Normal",
                    updatedAt = "Updated 2m ago",
                    iconType = VitalTileIcon.HEART,
                    modifier = Modifier.weight(1f),
                    onClick = onHeartRateClick
                )

                VitalTileCard(
                    title = "Blood Pressure",
                    value = "140/90 mmHg",
                    status = "Elevated",
                    updatedAt = "Updated 15m ago",
                    iconType = VitalTileIcon.BP,
                    modifier = Modifier.weight(1f),
                    onClick = onBloodPressureClick
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: SpO2 & Temperature
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VitalTileCard(
                    title = "SpO₂",
                    value = "92%",
                    status = "Low Rate",
                    updatedAt = "Updated 2m ago",
                    iconType = VitalTileIcon.SPO2,
                    modifier = Modifier.weight(1f),
                    onClick = onSpO2Click
                )

                VitalTileCard(
                    title = "Temperature",
                    value = "36.7 °C",
                    status = "Normal",
                    updatedAt = "Updated 1h ago",
                    iconType = VitalTileIcon.TEMP,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 3: Weight & BMI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VitalTileCard(
                    title = "Weight",
                    value = "68.5 kg",
                    status = "Stable",
                    updatedAt = "Updated 8h ago",
                    iconType = VitalTileIcon.WEIGHT,
                    modifier = Modifier.weight(1f)
                )

                VitalTileCard(
                    title = "BMI",
                    value = "22.4",
                    status = "Healthy Range",
                    updatedAt = "Updated 8h ago",
                    iconType = VitalTileIcon.BMI,
                    modifier = Modifier.weight(1f),
                    onClick = onBmiClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Add Reading Button
        Button(
            onClick = onAddReadingClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VitalTealDark,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "+ Add Reading",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

enum class VitalTileIcon {
    HEART,
    BP,
    SPO2,
    TEMP,
    WEIGHT,
    BMI
}

@Composable
private fun VitalTileCard(
    title: String,
    value: String,
    status: String,
    updatedAt: String,
    iconType: VitalTileIcon,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // Dynamic Classification Color Mapping based on Status / Trend
    val (statusColor, statusBgColor) = when {
        status.contains("Low", ignoreCase = true) || status.contains("High", ignoreCase = true) || status.contains("Critical", ignoreCase = true) ->
            Pair(Color(0xFFDC2626), Color(0xFFFEE2E2)) // Red
        status.contains("Warning", ignoreCase = true) || status.contains("Elevated", ignoreCase = true) ->
            Pair(Color(0xFFD97706), Color(0xFFFEF3C7)) // Amber/Orange
        else ->
            Pair(VitalPrimaryMint, VitalNavSelectedBg) // Green
    }

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = VitalCardBg),
        border = BorderStroke(1.dp, VitalSoftBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (iconType) {
                    VitalTileIcon.HEART -> HeartIconSmall(modifier = Modifier.size(16.dp), color = statusColor)
                    VitalTileIcon.BP -> BpIconSmall(modifier = Modifier.size(16.dp), color = statusColor)
                    VitalTileIcon.SPO2 -> WaterDropIconSmall(modifier = Modifier.size(16.dp), color = statusColor)
                    VitalTileIcon.TEMP -> TempIconSmall(modifier = Modifier.size(16.dp), color = statusColor)
                    VitalTileIcon.WEIGHT -> WeightIconSmall(modifier = Modifier.size(16.dp), color = statusColor)
                    VitalTileIcon.BMI -> BmiIconSmall(modifier = Modifier.size(16.dp), color = statusColor)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = VitalTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = VitalTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBgColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                Text(
                    text = updatedAt,
                    fontSize = 9.sp,
                    color = VitalTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mini Wave Chart with Trend Color Matching Status
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
            ) {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.5f)
                    lineTo(size.width * 0.25f, size.height * 0.6f)
                    lineTo(size.width * 0.45f, size.height * 0.2f)
                    lineTo(size.width * 0.65f, size.height * 0.8f)
                    lineTo(size.width * 0.85f, size.height * 0.4f)
                    lineTo(size.width, size.height * 0.5f)
                }
                drawPath(
                    path = path,
                    color = statusColor,
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
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

@Composable
private fun CalendarIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val calPath = Path().apply {
            moveTo(0f, h * 0.25f)
            lineTo(w, h * 0.25f)
            lineTo(w, h * 0.85f)
            lineTo(0f, h * 0.85f)
            close()
        }

        drawPath(path = calPath, color = VitalTextSecondary, style = Stroke(width = 1.6.dp.toPx()))
    }
}

@Composable
private fun HeartIconSmall(
    modifier: Modifier = Modifier,
    color: Color = VitalPrimaryMint
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.8f)
            cubicTo(w * 0.1f, h * 0.5f, w * 0.1f, h * 0.2f, w * 0.5f, h * 0.35f)
            cubicTo(w * 0.9f, h * 0.2f, w * 0.9f, h * 0.5f, w * 0.5f, h * 0.8f)
            close()
        }
        drawPath(path = path, color = color, style = Stroke(width = 1.6.dp.toPx()))
    }
}

@Composable
private fun BpIconSmall(
    modifier: Modifier = Modifier,
    color: Color = VitalPrimaryMint
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(0f, h * 0.5f)
            lineTo(w * 0.3f, h * 0.5f)
            lineTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.7f, h * 0.85f)
            lineTo(w, h * 0.5f)
        }
        drawPath(path = path, color = color, style = Stroke(width = 1.6.dp.toPx()))
    }
}

@Composable
private fun WaterDropIconSmall(
    modifier: Modifier = Modifier,
    color: Color = VitalPrimaryMint
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            cubicTo(w * 0.1f, h * 0.5f, w * 0.1f, h * 0.9f, w * 0.5f, h * 0.9f)
            cubicTo(w * 0.9f, h * 0.9f, w * 0.9f, h * 0.5f, w * 0.5f, 0f)
            close()
        }
        drawPath(path = path, color = color, style = Stroke(width = 1.6.dp.toPx()))
    }
}

@Composable
private fun TempIconSmall(
    modifier: Modifier = Modifier,
    color: Color = VitalPrimaryMint
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(color = color, radius = w * 0.22f, center = Offset(w * 0.5f, h * 0.7f), style = Stroke(width = 1.6.dp.toPx()))
        drawLine(color = color, start = Offset(w * 0.5f, h * 0.1f), end = Offset(w * 0.5f, h * 0.5f), strokeWidth = 1.6.dp.toPx())
    }
}

@Composable
private fun WeightIconSmall(
    modifier: Modifier = Modifier,
    color: Color = VitalPrimaryMint
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.1f, h * 0.2f)
            lineTo(w * 0.9f, h * 0.2f)
            lineTo(w * 0.9f, h * 0.8f)
            lineTo(w * 0.1f, h * 0.8f)
            close()
        }
        drawPath(path = path, color = color, style = Stroke(width = 1.6.dp.toPx()))
    }
}

@Composable
private fun BmiIconSmall(
    modifier: Modifier = Modifier,
    color: Color = VitalPrimaryMint
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(color = color, radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.3f), style = Stroke(width = 1.6.dp.toPx()))
        drawLine(color = color, start = Offset(w * 0.2f, h * 0.8f), end = Offset(w * 0.8f, h * 0.8f), strokeWidth = 1.6.dp.toPx())
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun VitalSignsScreenPreview() {
    VitalsyncTheme {
        VitalSignsScreen()
    }
}
