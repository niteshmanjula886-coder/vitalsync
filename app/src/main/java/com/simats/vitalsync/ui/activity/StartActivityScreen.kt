package com.simats.vitalsync.ui.activity

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.data.ActivityStatsSummary
import com.simats.vitalsync.ui.theme.CarbonDarkAccent
import com.simats.vitalsync.ui.theme.CarbonDarkBackground
import com.simats.vitalsync.ui.theme.CarbonDarkBorder
import com.simats.vitalsync.ui.theme.CarbonDarkSurface
import com.simats.vitalsync.ui.theme.CarbonDarkSurfaceVariant
import com.simats.vitalsync.ui.theme.CarbonDarkTextPrimary
import com.simats.vitalsync.ui.theme.CarbonDarkTextSecondary
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import java.util.Locale

enum class ActivityType {
    WALKING,
    RUNNING,
    CYCLING,
    WORKOUT
}

@Composable
fun StartActivityScreen(
    modifier: Modifier = Modifier,
    walkingStats: ActivityStatsSummary = ActivityStatsSummary("0s", "0.0 km", "0 kcal"),
    runningStats: ActivityStatsSummary = ActivityStatsSummary("0s", "0.0 km", "0 kcal"),
    cyclingStats: ActivityStatsSummary = ActivityStatsSummary("0s", "0.0 km", "0 kcal"),
    onBackClick: () -> Unit = {},
    onRefreshStatsClick: () -> Unit = {},
    onViewHistoryClick: () -> Unit = {},
    onStartTracking: (type: ActivityType, goalTab: String, targetDurationMinutes: Int, targetDistanceKm: Float) -> Unit = { _, _, _, _ -> }
) {
    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground

    var selectedType by remember { mutableStateOf(ActivityType.WALKING) }
    var selectedGoalTab by remember { mutableStateOf("Time") }
    var targetDurationMinutes by remember { mutableIntStateOf(30) }
    var targetDistanceKm by remember { mutableFloatStateOf(5.0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                        color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Start Activity",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // OVERALL ACTIVITY PERFORMANCE STATS Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OVERALL ACTIVITY PERFORMANCE STATS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) CarbonDarkTextSecondary else Color(0xFF9CA3AF),
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0xFF3B2D14) else Color(0xFFFEF3C7))
                        .clickable { onRefreshStatsClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🔄 Refresh Stats",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Activity Stats Summary",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                        )
                        Text(
                            text = "Time & Distance",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Walking Stat Row
                    ActivityStatRowItem(
                        title = "Walking",
                        icon = "🚶",
                        duration = walkingStats.durationFormatted,
                        distance = walkingStats.distanceKmFormatted,
                        calories = walkingStats.caloriesFormatted,
                        isDark = isDark
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    // Running Stat Row
                    ActivityStatRowItem(
                        title = "Running",
                        icon = "🏃",
                        duration = runningStats.durationFormatted,
                        distance = runningStats.distanceKmFormatted,
                        calories = runningStats.caloriesFormatted,
                        isDark = isDark
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    // Cycling Stat Row
                    ActivityStatRowItem(
                        title = "Cycling",
                        icon = "🚴",
                        duration = cyclingStats.durationFormatted,
                        distance = cyclingStats.distanceKmFormatted,
                        calories = cyclingStats.caloriesFormatted,
                        isDark = isDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // View Activity History Button Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDark) Color(0xFF1B382B) else Color(0xFFE8F6F0))
                    .clickable { onViewHistoryClick() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📜 View Live Activity Session History",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CHOOSE AN ACTIVITY Section
            SectionHeader(title = "CHOOSE AN ACTIVITY", isDark = isDark)

            // Walking & Running Options Row
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
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                ActivityOptionCard(
                    title = "Running",
                    subtitle = "Outdoor & treadmill",
                    icon = "🏃",
                    isSelected = selectedType == ActivityType.RUNNING,
                    onClick = { selectedType = ActivityType.RUNNING },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cycling Option Card
            ActivityOptionCard(
                title = "Cycling",
                subtitle = "Outdoor & stationary bike",
                icon = "🚴",
                isSelected = selectedType == ActivityType.CYCLING,
                onClick = { selectedType = ActivityType.CYCLING },
                isDark = isDark,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SET YOUR GOAL Section
            SectionHeader(title = "SET YOUR GOAL", isDark = isDark)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Goal Type Tabs (Time vs Distance)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF1F5F9))
                            .padding(4.dp)
                    ) {
                        listOf("Time", "Distance").forEach { tab ->
                            val isTabSelected = selectedGoalTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isTabSelected) {
                                            if (isDark) CarbonDarkAccent else VitalTealDark
                                        } else Color.Transparent
                                    )
                                    .clickable { selectedGoalTab = tab }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTabSelected) Color.White else (if (isDark) CarbonDarkTextSecondary else Color(0xFF64748B))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (selectedGoalTab == "Time") {
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
                                    .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF1F5F9))
                                    .clickable { if (targetDurationMinutes > 5) targetDurationMinutes -= 5 },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "−",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF334155)
                                )
                            }

                            Text(
                                text = "$targetDurationMinutes min",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                            )

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF1F5F9))
                                    .clickable { targetDurationMinutes += 5 },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF334155)
                                )
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
                                        .background(
                                            if (isChipSelected) {
                                                if (isDark) Color(0xFF1B382B) else Color(0xFFE6F4EA)
                                            } else {
                                                if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF8FAFC)
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (isChipSelected) {
                                                if (isDark) Color(0xFF34D399) else VitalTealPrimary
                                            } else {
                                                if (isDark) CarbonDarkBorder else Color(0xFFE2E8F0)
                                            },
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { targetDurationMinutes = mins }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$mins min",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChipSelected) {
                                            if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A)
                                        } else {
                                            if (isDark) CarbonDarkTextSecondary else Color(0xFF475569)
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        // Distance Incrementor Box
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF1F5F9))
                                    .clickable { if (targetDistanceKm > 0.5f) targetDistanceKm -= 0.5f },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "−",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF334155)
                                )
                            }

                            Text(
                                text = String.format(Locale.US, "%.1f km", targetDistanceKm),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                            )

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF1F5F9))
                                    .clickable { targetDistanceKm += 0.5f },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF334155)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Distance Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(2.0f, 5.0f, 10.0f).forEach { km ->
                                val isChipSelected = targetDistanceKm == km
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isChipSelected) {
                                                if (isDark) Color(0xFF1B382B) else Color(0xFFE6F4EA)
                                            } else {
                                                if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF8FAFC)
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (isChipSelected) {
                                                if (isDark) Color(0xFF34D399) else VitalTealPrimary
                                            } else {
                                                if (isDark) CarbonDarkBorder else Color(0xFFE2E8F0)
                                            },
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { targetDistanceKm = km }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${km.toInt()} km",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChipSelected) {
                                            if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A)
                                        } else {
                                            if (isDark) CarbonDarkTextSecondary else Color(0xFF475569)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Start Activity Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Button(
                onClick = {
                    onStartTracking(selectedType, selectedGoalTab, targetDurationMinutes, targetDistanceKm)
                },
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) CarbonDarkAccent else VitalTealDark,
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
}

@Composable
private fun ActivityStatRowItem(
    title: String,
    icon: String,
    duration: String,
    distance: String,
    calories: String,
    isDark: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                )
                Text(
                    text = "Duration: $duration",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color(0xFF34D399) else Color(0xFF0F9F7A)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = distance,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827),
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = calories,
                fontSize = 12.sp,
                color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280),
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, isDark: Boolean = false) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = if (isDark) CarbonDarkTextSecondary else Color(0xFF9CA3AF),
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
    isDark: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                if (isDark) Color(0xFF1B382B) else Color(0xFFE6F4EA)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) {
                if (isDark) Color(0xFF34D399) else VitalTealPrimary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 0.dp else 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Text(text = icon, fontSize = 22.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280),
                    maxLines = 2,
                    lineHeight = 15.sp
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
