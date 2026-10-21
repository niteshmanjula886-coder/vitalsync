package com.simats.vitalsync.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.ai.AiScreen
import com.simats.vitalsync.ui.gym.GymModeScreen
import com.simats.vitalsync.ui.profile.ProfileScreen
import com.simats.vitalsync.ui.theme.VitalsyncTheme

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    initialTab: Int = 0,
    onVitalsClick: () -> Unit = {},
    onHeartRateClick: () -> Unit = {},
    onBloodPressureClick: () -> Unit = {},
    onSpO2Click: () -> Unit = {},
    onStepsClick: () -> Unit = {},
    onActivityClick: () -> Unit = {},
    onSleepClick: () -> Unit = {},
    onHydrationClick: () -> Unit = {},
    onPersonalInformationClick: () -> Unit = {},
    onHealthGoalsClick: () -> Unit = {},
    onConnectedDevicesClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onPrivacySecurityClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onHelpSupportClick: () -> Unit = {},
    onAskAiClick: () -> Unit = {},
    onStartWorkoutSplitClick: () -> Unit = {},
    onOpenNutritionTrackerClick: () -> Unit = {},
    onOpenProgressAnalyticsClick: () -> Unit = {},
    onViewWorkoutHistoryClick: () -> Unit = {},
    userName: String = "Alex Morgan",
    userInitials: String = "AM",
    userAge: String = "28",
    userHeight: String = "175",
    userWeight: String = "68.5",
    userPhotoBitmap: ImageBitmap? = null,
    onLogoutClick: () -> Unit = {}
) {
    var selectedBottomTab by remember(initialTab) { mutableIntStateOf(initialTab) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFFAFCFB),
        bottomBar = {
            HomeBottomNavigationBar(
                selectedTab = selectedBottomTab,
                onTabSelected = { tab ->
                    if (tab == 1) {
                        onVitalsClick()
                    } else {
                        selectedBottomTab = tab
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedBottomTab) {
                0 -> HomeDashboardContent(
                    onVitalsClick = onVitalsClick,
                    onHeartRateClick = onHeartRateClick,
                    onBloodPressureClick = onBloodPressureClick,
                    onSpO2Click = onSpO2Click,
                    onStepsClick = onStepsClick,
                    onActivityClick = onActivityClick,
                    onSleepClick = onSleepClick,
                    onHydrationClick = onHydrationClick
                )
                2 -> AiScreen(onAskAiClick = onAskAiClick)
                3 -> ProfileScreen(
                    userName = userName,
                    userInitials = userInitials,
                    userAge = userAge,
                    userHeight = userHeight,
                    userWeight = userWeight,
                    userPhotoBitmap = userPhotoBitmap,
                    onEditProfileClick = onPersonalInformationClick,
                    onPersonalInformationClick = onPersonalInformationClick,
                    onHealthGoalsClick = onHealthGoalsClick,
                    onConnectedDevicesClick = onConnectedDevicesClick,
                    onNotificationsClick = onNotificationsClick,
                    onPrivacySecurityClick = onPrivacySecurityClick,
                    onSettingsClick = onSettingsClick,
                    onHelpSupportClick = onHelpSupportClick,
                    onLogoutClick = onLogoutClick
                )
                4 -> GymModeScreen(
                    onStartWorkoutSplitClick = onStartWorkoutSplitClick,
                    onOpenNutritionTrackerClick = onOpenNutritionTrackerClick,
                    onOpenProgressAnalyticsClick = onOpenProgressAnalyticsClick,
                    onViewWorkoutHistoryClick = onViewWorkoutHistoryClick,
                    onFinishGymSession = { selectedBottomTab = 0 }
                )
            }
        }
    }
}

@Composable
private fun HomeDashboardContent(
    onVitalsClick: () -> Unit,
    onHeartRateClick: () -> Unit,
    onBloodPressureClick: () -> Unit,
    onSpO2Click: () -> Unit,
    onStepsClick: () -> Unit,
    onActivityClick: () -> Unit,
    onSleepClick: () -> Unit,
    onHydrationClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        // Good Morning Header
        Column {
            Text(
                text = "Good Morning, Alex 👋",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Here's your health overview",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Wellness Score Hero Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onVitalsClick() },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F6F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gauge Progress Ring
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokePx = 9.dp.toPx()
                        val arcSize = Size(size.width - strokePx, size.height - strokePx)
                        val topLeft = Offset(strokePx / 2f, strokePx / 2f)

                        // Track
                        drawArc(
                            color = Color(0xFFC7EBDD),
                            startAngle = -220f,
                            sweepAngle = 260f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                        // Active Progress
                        drawArc(
                            color = Color(0xFF0F9F7A),
                            startAngle = -220f,
                            sweepAngle = 210f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "82",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "/ 100",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column {
                    Text(
                        text = "Wellness Score",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Good",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F9F7A)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "+6% from last week",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0F9F7A)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Your Vitals Section Header (4 Key Stats Overview)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Your vitals",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "4 key health stats",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }

            Text(
                text = "View all →",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F9F7A),
                modifier = Modifier.clickable { onVitalsClick() }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Vitals 2x2 Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            VitalCard(
                title = "Heart Rate",
                value = "76 BPM",
                status = "Normal",
                iconType = VitalIconType.HEART,
                modifier = Modifier.weight(1f),
                onClick = onHeartRateClick
            )

            VitalCard(
                title = "SpO₂",
                value = "98%",
                status = "Optimal",
                iconType = VitalIconType.SPO2,
                modifier = Modifier.weight(1f),
                onClick = onSpO2Click
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            VitalCard(
                title = "Blood Pressure",
                value = "120/80",
                status = "Normal",
                iconType = VitalIconType.BLOOD_PRESSURE,
                modifier = Modifier.weight(1f),
                onClick = onBloodPressureClick
            )

            VitalCard(
                title = "Steps",
                value = "6,245",
                status = "78% Goal",
                iconType = VitalIconType.STEPS,
                modifier = Modifier.weight(1f),
                onClick = onStepsClick
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Activity Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onActivityClick() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FootstepsIcon(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF0F9F7A)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Activity",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    }

                    Text(
                        text = "6,245 steps",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F9F7A)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "4.2 km",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = "320 kcal",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = "42 active min",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sleep & Hydration Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSleepClick() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MoonIcon(
                            modifier = Modifier.size(18.dp),
                            color = Color(0xFF0F9F7A)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sleep",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "7h 32m",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Sleep Score 88",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onHydrationClick() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        WaterGlassIcon(
                            modifier = Modifier.size(18.dp),
                            color = Color(0xFF0F9F7A)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hydration",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "5 / 8 glasses",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "3 more to your goal",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Insight Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SparkleIconPurple(modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI insight",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C3AED)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your activity is 12% higher than your weekly average.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

enum class VitalIconType {
    HEART,
    SPO2,
    BLOOD_PRESSURE,
    STEPS
}

@Composable
private fun VitalCard(
    title: String,
    value: String,
    status: String,
    iconType: VitalIconType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (iconType) {
                    VitalIconType.HEART -> HeartPulseIcon(modifier = Modifier.size(18.dp), color = Color(0xFF0F9F7A))
                    VitalIconType.SPO2 -> WaterDropIcon(modifier = Modifier.size(18.dp), color = Color(0xFF0F9F7A))
                    VitalIconType.BLOOD_PRESSURE -> PulseWaveIcon(modifier = Modifier.size(18.dp), color = Color(0xFF0F9F7A))
                    VitalIconType.STEPS -> FootstepsIcon(modifier = Modifier.size(18.dp), color = Color(0xFF0F9F7A))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = status,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF0F9F7A)
            )
        }
    }
}

@Composable
private fun HomeBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.height(72.dp)
    ) {
        val items = listOf("Home", "Vitals", "AI", "Profile", "Gym")

        items.forEachIndexed { index, label ->
            val isSelected = selectedTab == index

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    when (index) {
                        0 -> HouseIcon(modifier = Modifier.size(22.dp), color = if (isSelected) Color(0xFF0F9F7A) else Color(0xFF9CA3AF))
                        1 -> HeartPulseIcon(modifier = Modifier.size(22.dp), color = if (isSelected) Color(0xFF0F9F7A) else Color(0xFF9CA3AF))
                        2 -> SparkleIconPurple(modifier = Modifier.size(22.dp), color = if (isSelected) Color(0xFF0F9F7A) else Color(0xFF9CA3AF))
                        3 -> UserPersonIcon(modifier = Modifier.size(22.dp), color = if (isSelected) Color(0xFF0F9F7A) else Color(0xFF9CA3AF))
                        4 -> DumbbellIcon(modifier = Modifier.size(22.dp), color = if (isSelected) Color(0xFF0F9F7A) else Color(0xFF9CA3AF))
                    }
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF0F9F7A) else Color(0xFF9CA3AF)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFFE6F4EA)
                )
            )
        }
    }
}

// Icons
@Composable
private fun DumbbellIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val bar = Path().apply {
            moveTo(w * 0.15f, h * 0.5f)
            lineTo(w * 0.85f, h * 0.5f)
        }

        drawPath(path = bar, color = color, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
        drawCircle(color = color, radius = w * 0.15f, center = Offset(w * 0.20f, h * 0.5f))
        drawCircle(color = color, radius = w * 0.15f, center = Offset(w * 0.80f, h * 0.5f))
    }
}

@Composable
private fun HeartPulseIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val heartPath = Path().apply {
            moveTo(w * 0.5f, h * 0.82f)
            cubicTo(w * 0.15f, h * 0.58f, w * 0.02f, h * 0.30f, w * 0.25f, h * 0.14f)
            cubicTo(w * 0.38f, h * 0.06f, w * 0.48f, h * 0.16f, w * 0.50f, h * 0.24f)
            cubicTo(w * 0.52f, h * 0.16f, w * 0.62f, h * 0.06f, w * 0.75f, h * 0.14f)
            cubicTo(w * 0.98f, h * 0.30f, w * 0.85f, h * 0.58f, w * 0.5f, h * 0.82f)
            close()
        }

        val pulseLine = Path().apply {
            moveTo(w * 0.22f, h * 0.48f)
            lineTo(w * 0.38f, h * 0.48f)
            lineTo(w * 0.45f, h * 0.34f)
            lineTo(w * 0.53f, h * 0.62f)
            lineTo(w * 0.62f, h * 0.48f)
            lineTo(w * 0.78f, h * 0.48f)
        }

        drawPath(path = heartPath, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(path = pulseLine, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun WaterDropIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val dropPath = Path().apply {
            moveTo(w * 0.5f, h * 0.12f)
            cubicTo(w * 0.15f, h * 0.55f, w * 0.15f, h * 0.85f, w * 0.5f, h * 0.88f)
            cubicTo(w * 0.85f, h * 0.85f, w * 0.85f, h * 0.55f, w * 0.5f, h * 0.12f)
            close()
        }

        drawPath(path = dropPath, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun PulseWaveIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(0f, h * 0.5f)
            lineTo(w * 0.25f, h * 0.5f)
            lineTo(w * 0.38f, h * 0.22f)
            lineTo(w * 0.52f, h * 0.78f)
            lineTo(w * 0.65f, h * 0.38f)
            lineTo(w * 0.75f, h * 0.5f)
            lineTo(w, h * 0.5f)
        }

        drawPath(path = path, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun FootstepsIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(color = color, radius = w * 0.16f, center = Offset(w * 0.32f, h * 0.32f))
        drawCircle(color = color, radius = w * 0.14f, center = Offset(w * 0.30f, h * 0.68f))
        drawCircle(color = color, radius = w * 0.16f, center = Offset(w * 0.70f, h * 0.22f))
        drawCircle(color = color, radius = w * 0.14f, center = Offset(w * 0.68f, h * 0.58f))
    }
}

@Composable
private fun MoonIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val moonPath = Path().apply {
            moveTo(w * 0.75f, h * 0.15f)
            cubicTo(w * 0.25f, h * 0.25f, w * 0.25f, h * 0.75f, w * 0.75f, h * 0.85f)
            cubicTo(w * 0.45f, h * 0.75f, w * 0.45f, h * 0.25f, w * 0.75f, h * 0.15f)
            close()
        }

        drawPath(path = moonPath, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun WaterGlassIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val glassPath = Path().apply {
            moveTo(w * 0.22f, h * 0.18f)
            lineTo(w * 0.78f, h * 0.18f)
            lineTo(w * 0.70f, h * 0.85f)
            lineTo(w * 0.30f, h * 0.85f)
            close()
        }

        val waterLine = Path().apply {
            moveTo(w * 0.25f, h * 0.45f)
            lineTo(w * 0.75f, h * 0.45f)
        }

        drawPath(path = glassPath, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(path = waterLine, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun SparkleIconPurple(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF7C3AED)
) {
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

        drawPath(path = path, color = color)
    }
}

@Composable
private fun HouseIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val housePath = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.88f, h * 0.48f)
            lineTo(w * 0.88f, h * 0.88f)
            lineTo(w * 0.12f, h * 0.88f)
            lineTo(w * 0.12f, h * 0.48f)
            close()
        }

        drawPath(path = housePath, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun UserPersonIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(color = Color(0xFF0F9F7A), radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.30f), style = Stroke(width = 1.6.dp.toPx()))

        val bodyPath = Path().apply {
            moveTo(w * 0.15f, h * 0.85f)
            cubicTo(w * 0.15f, h * 0.60f, w * 0.85f, h * 0.60f, w * 0.85f, h * 0.85f)
        }

        drawPath(path = bodyPath, color = Color(0xFF0F9F7A), style = Stroke(width = 1.6.dp.toPx()))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun HomeScreenPreview() {
    VitalsyncTheme {
        HomeScreen()
    }
}
