package com.simats.vitalsync.ui.vitals

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
fun StepCountDetailScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onEditGoalClick: () -> Unit = {}
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
                        text = "Step Count",
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

            // Top Gauge Progress Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Today, October 15",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                        Text(
                            text = "Sample data",
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ring Gauge
                        Box(
                            modifier = Modifier.size(90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokePx = 9.dp.toPx()
                                val arcSize = Size(size.width - strokePx, size.height - strokePx)
                                val topLeft = Offset(strokePx / 2f, strokePx / 2f)

                                drawArc(
                                    color = Color(0xFFE5E7EB),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokePx)
                                )
                                drawArc(
                                    color = Color(0xFF0F9F7A),
                                    startAngle = -90f,
                                    sweepAngle = 280f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "78%",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                Text(
                                    text = "daily goal",
                                    fontSize = 10.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column {
                            Text(
                                text = "6,245",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "steps",
                                fontSize = 14.sp,
                                color = Color(0xFF111827),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Goal: 8,000 steps ✏️",
                                fontSize = 12.sp,
                                color = Color(0xFF0F9F7A),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { onEditGoalClick() }
                            )
                            Text(
                                text = "1,755 steps remaining",
                                fontSize = 12.sp,
                                color = Color(0xFF0F9F7A),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Distance", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "4.2 km", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }
                        Column {
                            Text(text = "Energy", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "320 kcal", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }
                        Column {
                            Text(text = "Active time", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "42 min", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bar Chart Card ("This week")
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
                        Text(
                            text = "This week",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "Daily steps",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F9F7A)
                        )
                    }

                    Text(
                        text = "October 9–15 · goal 8,000 steps / day",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Bar Chart
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        val barWidth = 24.dp.toPx()
                        val heights = listOf(0.45f, 0.60f, 0.50f, 0.70f, 0.55f, 0.95f, 0.78f)
                        val totalWidth = size.width
                        val spacing = (totalWidth - (barWidth * 7)) / 8f

                        heights.forEachIndexed { i, ratio ->
                            val x = spacing + i * (barWidth + spacing)
                            val barHeight = size.height * ratio
                            val y = size.height - barHeight

                            val barColor = if (i == 6) Color(0xFF0F9F7A) else Color(0xFFA7F3D0)

                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("9", "10", "11", "12", "13", "14", "15").forEach { date ->
                            Text(
                                text = date,
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "5,978 steps per day on average",
                        fontSize = 12.sp,
                        color = Color(0xFF0F9F7A),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Daily Totals History Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Recent daily totals",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    StepHistoryRow(dateStr = "Today · Oct 15", valStr = "6,245", subStr = "78% of goal")
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))
                    StepHistoryRow(dateStr = "Yesterday · Oct 14", valStr = "7,520", subStr = "94% of goal")
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))
                    StepHistoryRow(dateStr = "Tuesday · Oct 13", valStr = "5,600", subStr = "70% of goal")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom Button & Footer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Button(
                onClick = onEditGoalClick,
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
                    text = "Edit Goal",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Activity estimates - updated at 9:38 AM",
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StepHistoryRow(
    dateStr: String,
    valStr: String,
    subStr: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = dateStr, fontSize = 13.sp, color = Color(0xFF6B7280))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = valStr, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = subStr, fontSize = 11.sp, color = Color(0xFF9CA3AF))
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

        drawPath(path = calPath, color = Color(0xFF6B7280), style = Stroke(width = 1.6.dp.toPx()))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun StepCountDetailScreenPreview() {
    VitalsyncTheme {
        StepCountDetailScreen()
    }
}
