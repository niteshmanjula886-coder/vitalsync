package com.simats.vitalsync.ui.vitals

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme

@Composable
fun BmiDetailScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onUpdateMetricsClick: () -> Unit = {}
) {
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
                            color = Color(0xFF111827)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "BMI (Body Mass Index)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
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

            // Current BMI Score Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current BMI Score",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE6F4EA))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Healthy Range",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F9F7A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "22.4",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Height: 175 cm · Weight: 68.5 kg",
                        fontSize = 13.sp,
                        color = Color(0xFF374151),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // BMI Spectrum Color Gauge Bar
                    Text(
                        text = "BMI Spectrum Range",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Spectrum Bar (Safe fixed weights)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        Box(modifier = Modifier.weight(18.5f).height(12.dp).background(Color(0xFF38BDF8)))
                        Box(modifier = Modifier.weight(6.4f).height(12.dp).background(Color(0xFF0F9F7A)))
                        Box(modifier = Modifier.weight(5f).height(12.dp).background(Color(0xFFF59E0B)))
                        Box(modifier = Modifier.weight(10f).height(12.dp).background(Color(0xFFEF4444)))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Underweight", fontSize = 10.sp, color = Color(0xFF38BDF8))
                        Text(text = "Normal (18.5-24.9)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A))
                        Text(text = "Overweight", fontSize = 10.sp, color = Color(0xFFF59E0B))
                        Text(text = "Obese", fontSize = 10.sp, color = Color(0xFFEF4444))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BMI Trend Chart Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "BMI history trend",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Past 6 months · 22.8 → 22.4",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Smoothly Animated Trend Line Chart Canvas
                    var targetTrendProgress by remember { mutableFloatStateOf(0f) }
                    LaunchedEffect(Unit) {
                        targetTrendProgress = 1f
                    }
                    val trendProgress by animateFloatAsState(
                        targetValue = targetTrendProgress,
                        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                        label = "BmiTrendLineAnimation"
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        val w = size.width
                        val h = size.height

                        val fullPath = Path().apply {
                            moveTo(0f, h * 0.6f)
                            cubicTo(w * 0.1f, h * 0.58f, w * 0.15f, h * 0.55f, w * 0.2f, h * 0.55f)
                            cubicTo(w * 0.3f, h * 0.52f, w * 0.35f, h * 0.50f, w * 0.4f, h * 0.50f)
                            cubicTo(w * 0.5f, h * 0.49f, w * 0.55f, h * 0.48f, w * 0.6f, h * 0.48f)
                            cubicTo(w * 0.7f, h * 0.46f, w * 0.75f, h * 0.45f, w * 0.8f, h * 0.45f)
                            cubicTo(w * 0.9f, h * 0.45f, w * 0.95f, h * 0.45f, w, h * 0.45f)
                        }

                        val pathMeasure = PathMeasure()
                        pathMeasure.setPath(fullPath, false)
                        val animatedPath = Path()
                        pathMeasure.getSegment(0f, pathMeasure.length * trendProgress, animatedPath, true)

                        if (trendProgress > 0f) {
                            val fillPath = Path().apply {
                                addPath(animatedPath)
                                lineTo(w * trendProgress, h)
                                lineTo(0f, h)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0x350F9F7A), Color(0x000F9F7A))
                                )
                            )

                            drawPath(
                                path = animatedPath,
                                color = Color(0xFF0F9F7A),
                                style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("May", "Jun", "Jul", "Aug", "Sep", "Oct").forEach { month ->
                            Text(text = month, fontSize = 12.sp, color = Color(0xFF9CA3AF))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BMI Categories Reference Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "BMI categories",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    BmiCategoryRow(range = "< 18.5", category = "Underweight", color = Color(0xFF38BDF8), isCurrent = false)
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))
                    BmiCategoryRow(range = "18.5 – 24.9", category = "Normal weight", color = Color(0xFF0F9F7A), isCurrent = true)
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))
                    BmiCategoryRow(range = "25.0 – 29.9", category = "Overweight", color = Color(0xFFF59E0B), isCurrent = false)
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))
                    BmiCategoryRow(range = "≥ 30.0", category = "Obese", color = Color(0xFFEF4444), isCurrent = false)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Action Button
        Button(
            onClick = onUpdateMetricsClick,
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
                text = "+ Update Weight & Height",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BmiCategoryRow(
    range: String,
    category: String,
    color: Color,
    isCurrent: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isCurrent) Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE6F4EA))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                else Modifier
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = category,
                fontSize = 14.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                color = Color(0xFF111827)
            )
        }

        Text(
            text = range,
            fontSize = 13.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrent) Color(0xFF0F9F7A) else Color(0xFF6B7280)
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

        drawPath(path = calPath, color = Color(0xFF6B7280), style = Stroke(width = 1.6.dp.toPx()))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun BmiDetailScreenPreview() {
    VitalsyncTheme {
        BmiDetailScreen()
    }
}
