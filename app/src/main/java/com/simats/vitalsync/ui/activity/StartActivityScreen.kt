package com.simats.vitalsync.ui.activity

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme

enum class ActivityType {
    WALKING,
    RUNNING,
    CYCLING,
    WORKOUT
}

@Composable
fun StartActivityScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onStartTracking: (ActivityType) -> Unit = {}
) {
    var selectedType by remember { mutableStateOf(ActivityType.WALKING) }
    var selectedGoalTab by remember { mutableStateOf("Time") }
    var targetDurationMinutes by remember { mutableIntStateOf(30) }

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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
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
                    text = "Start Activity",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CHOOSE AN ACTIVITY Section
            SectionHeader("CHOOSE AN ACTIVITY")

            // 2x2 Activity Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActivityOptionCard(
                    title = "Walking",
                    subtitle = "Outdoor & treadmill",
                    icon = "🚶",
                    isSelected = selectedType == ActivityType.WALKING,
                    onClick = { selectedType = ActivityType.WALKING },
                    modifier = Modifier.weight(1f)
                )

                ActivityOptionCard(
                    title = "Running",
                    subtitle = "Outdoor & treadmill",
                    icon = "🏃",
                    isSelected = selectedType == ActivityType.RUNNING,
                    onClick = { selectedType = ActivityType.RUNNING },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActivityOptionCard(
                    title = "Cycling",
                    subtitle = "Outdoor & stationary",
                    icon = "🚴",
                    isSelected = selectedType == ActivityType.CYCLING,
                    onClick = { selectedType = ActivityType.CYCLING },
                    modifier = Modifier.weight(1f)
                )

                ActivityOptionCard(
                    title = "Workout",
                    subtitle = "Strength & cardio",
                    icon = "🏋️",
                    isSelected = selectedType == ActivityType.WORKOUT,
                    onClick = { selectedType = ActivityType.WORKOUT },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SET YOUR GOAL Section
            SectionHeader("SET YOUR GOAL")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Goal Type Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(4.dp)
                    ) {
                        listOf("Time", "Distance", "Open goal").forEach { tab ->
                            val isTabSelected = selectedGoalTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isTabSelected) VitalTealDark else Color.Transparent)
                                    .clickable { selectedGoalTab = tab }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTabSelected) Color.White else Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Duration Incrementor Box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .clickable { if (targetDurationMinutes > 5) targetDurationMinutes -= 5 },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "−", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                        }

                        Text(
                            text = "$targetDurationMinutes min",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .clickable { targetDurationMinutes += 5 },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Duration Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(15, 30, 45).forEach { mins ->
                            val isChipSelected = targetDurationMinutes == mins
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isChipSelected) Color(0xFFE6F4EA) else Color(0xFFF8FAFC))
                                    .border(1.dp, if (isChipSelected) VitalTealPrimary else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                    .clickable { targetDurationMinutes = mins }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$mins min",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChipSelected) Color(0xFF0F9F7A) else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BEFORE YOU START Section
            SectionHeader("BEFORE YOU START")

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
                            Text(text = "📍", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Location access", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                Text(text = "To track route map & pace", fontSize = 12.sp, color = Color(0xFF6B7280))
                            }
                        }
                        Text(text = "Active ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📱", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Track with your phone", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                Text(text = "No external sensor required", fontSize = 12.sp, color = Color(0xFF6B7280))
                            }
                        }
                        Text(text = "Ready", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A))
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Start Activity Button
        Button(
            onClick = { onStartTracking(selectedType) },
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
                text = "Start ${selectedType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF9CA3AF),
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun ActivityOptionCard(
    title: String,
    subtitle: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE6F4EA) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 0.dp else 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (isSelected) VitalTealPrimary else Color.Transparent,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Text(text = icon, fontSize = 22.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280)
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun StartActivityScreenPreview() {
    VitalsyncTheme {
        StartActivityScreen()
    }
}
