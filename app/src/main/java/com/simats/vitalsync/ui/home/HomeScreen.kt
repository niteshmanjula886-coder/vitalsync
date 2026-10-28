package com.simats.vitalsync.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.ai.AiScreen
import com.simats.vitalsync.ui.gym.GymModeScreen
import com.simats.vitalsync.ui.profile.ProfileScreen
import com.simats.vitalsync.ui.theme.VitalCardBg
import com.simats.vitalsync.ui.theme.VitalPrimaryMint
import com.simats.vitalsync.ui.theme.VitalSoftBorder
import com.simats.vitalsync.ui.theme.VitalTextPrimary
import com.simats.vitalsync.ui.theme.VitalTextSecondary
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    initialTab: Int = 0,
    onWellnessScoreClick: () -> Unit = {},
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
        containerColor = MaterialTheme.colorScheme.background,
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
            AnimatedContent(
                targetState = selectedBottomTab,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)) +
                        scaleIn(
                            animationSpec = spring(
                                dampingRatio = 0.72f,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            initialScale = 0.92f
                        ))
                        .togetherWith(
                            fadeOut(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)) +
                                scaleOut(
                                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                                    targetScale = 1.05f
                                )
                        )
                },
                label = "AppleSpringPopTabTransition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> HomeDashboardContent(
                        userName = userName,
                        onWellnessScoreClick = onWellnessScoreClick,
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
}

@Composable
private fun HomeDashboardContent(
    userName: String,
    onWellnessScoreClick: () -> Unit,
    onVitalsClick: () -> Unit,
    onHeartRateClick: () -> Unit,
    onBloodPressureClick: () -> Unit,
    onSpO2Click: () -> Unit,
    onStepsClick: () -> Unit,
    onActivityClick: () -> Unit,
    onSleepClick: () -> Unit,
    onHydrationClick: () -> Unit
) {
    var targetAngle by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        targetAngle = 210f
    }
    val animatedSweepAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "GaugeSweepAnimation"
    )

    val firstName = userName.split(" ").firstOrNull() ?: "Alex"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        // Good Morning Header (Clean iOS Style)
        Column {
            Text(
                text = "Good Morning, $firstName",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Here's your health overview",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Wellness Score Hero Card (Clicking opens Weekly Health Performance Report Screen)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onWellnessScoreClick() },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated Dark Purple Gauge Progress Ring
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
                            color = Color(0xFFC4B5FD),
                            startAngle = -220f,
                            sweepAngle = 260f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                        // Smoothly Animated Active Progress Sweep
                        drawArc(
                            color = Color(0xFF7C3AED),
                            startAngle = -220f,
                            sweepAngle = animatedSweepAngle,
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
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "/ 100",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column {
                    Text(
                        text = "Wellness Score",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Good",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C3AED)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "+6% from last week →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C3AED)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Your Vitals Section Header
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
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "4 key health stats",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "View all →",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = VitalPrimaryMint,
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
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Activity",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Text(
                        text = "6,245 steps",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "320 kcal",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "42 active min",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MoonIcon(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sleep",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "7h 32m",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Sleep Score 88",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onHydrationClick() },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        WaterGlassIcon(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hydration",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "5 / 8 glasses",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "3 more to your goal",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Insight Banner Card (Adaptive Dark/Light Theme)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SparkleIconPurple(modifier = Modifier.size(18.dp), color = Color(0xFF7C3AED))
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
                    color = MaterialTheme.colorScheme.onBackground,
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
    val statusColor = when {
        status.contains("Low", ignoreCase = true) || status.contains("High", ignoreCase = true) || status.contains("Critical", ignoreCase = true) ->
            Color(0xFFDC2626)
        status.contains("Warning", ignoreCase = true) || status.contains("Elevated", ignoreCase = true) ->
            Color(0xFFD97706)
        else ->
            VitalPrimaryMint
    }

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = VitalCardBg),
        border = BorderStroke(1.dp, VitalSoftBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (iconType) {
                    VitalIconType.HEART -> HeartPulseIcon(modifier = Modifier.size(18.dp), color = statusColor)
                    VitalIconType.SPO2 -> WaterDropIcon(modifier = Modifier.size(18.dp), color = statusColor)
                    VitalIconType.BLOOD_PRESSURE -> PulseWaveIcon(modifier = Modifier.size(18.dp), color = statusColor)
                    VitalIconType.STEPS -> FootstepsIcon(modifier = Modifier.size(18.dp), color = statusColor)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = VitalTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = VitalTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = status,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// 🔮 Interactive Parabolic Arc Jumping & Drag/Swipe Circular Bubble Navigation Bar
@Composable
private fun HomeBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabCount = 5
    val items = listOf("Home", "Vitals", "AI", "Profile", "Gym")

    var isDragging by remember { mutableStateOf(false) }
    var dragTabPos by remember { mutableFloatStateOf(selectedTab.toFloat()) }
    var startTabPos by remember { mutableFloatStateOf(selectedTab.toFloat()) }
    var targetTabPos by remember { mutableFloatStateOf(selectedTab.toFloat()) }

    LaunchedEffect(selectedTab) {
        if (!isDragging) {
            startTabPos = targetTabPos
            targetTabPos = selectedTab.toFloat()
            dragTabPos = selectedTab.toFloat()
        }
    }

    val animatedTabPos by animateFloatAsState(
        targetValue = targetTabPos,
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "JumpingBubbleHorizontal"
    )

    val totalDistance = abs(targetTabPos - startTabPos)
    val traveled = abs(animatedTabPos - startTabPos)
    val jumpFraction = if (totalDistance > 0.05f) (traveled / totalDistance).coerceIn(0f, 1f) else 0f

    val jumpArcYDp = (-18.dp) * sin(jumpFraction * Math.PI.toFloat())
    val bubbleScale = (1.0f + 0.12f * sin(jumpFraction * Math.PI.toFloat()))

    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopStart
            ) {
                val totalWidth = maxWidth
                val tabWidth = totalWidth / tabCount

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            val totalWidthPx = with(density) { maxWidth.toPx() }
                            val tabWidthPx = totalWidthPx / tabCount

                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    isDragging = true
                                    val startTab = (offset.x / tabWidthPx).coerceIn(0f, (tabCount - 1).toFloat())
                                    dragTabPos = startTab
                                    targetTabPos = startTab
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    dragTabPos = (dragTabPos + dragAmount / tabWidthPx).coerceIn(0f, (tabCount - 1).toFloat())
                                    targetTabPos = dragTabPos
                                },
                                onDragEnd = {
                                    isDragging = false
                                    val snappedTab = dragTabPos.roundToInt().coerceIn(0, tabCount - 1)
                                    startTabPos = dragTabPos
                                    targetTabPos = snappedTab.toFloat()
                                    dragTabPos = snappedTab.toFloat()
                                    onTabSelected(snappedTab)
                                },
                                onDragCancel = {
                                    isDragging = false
                                    val snappedTab = dragTabPos.roundToInt().coerceIn(0, tabCount - 1)
                                    startTabPos = dragTabPos
                                    targetTabPos = snappedTab.toFloat()
                                    dragTabPos = snappedTab.toFloat()
                                    onTabSelected(snappedTab)
                                }
                            )
                        }
                ) {
                    // 🔮 Active Selected Circular Bubble
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .offset(
                                x = (tabWidth * animatedTabPos) + (tabWidth / 2f) - 20.dp,
                                y = jumpArcYDp + 6.dp
                            )
                            .scale(bubbleScale)
                            .shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            )
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary)
                    )

                    // Navigation Items Row
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items.forEachIndexed { index, label ->
                            val isSelected = selectedTab == index
                            val activeColor = MaterialTheme.colorScheme.primary
                            val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

                            val iconScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.12f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "FixedIconScale"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        startTabPos = animatedTabPos
                                        targetTabPos = index.toFloat()
                                        dragTabPos = index.toFloat()
                                        onTabSelected(index)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .scale(iconScale),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when (index) {
                                            0 -> AnimatedHomeNavIcon(isSelected = isSelected, modifier = Modifier.size(20.dp), color = if (isSelected) Color(0xFF8B4513) else inactiveColor)
                                            1 -> AnimatedVitalsNavIcon(isSelected = isSelected, modifier = Modifier.size(20.dp), color = if (isSelected) activeColor else inactiveColor)
                                            2 -> AnimatedAiNavSparkleIcon(isSelected = isSelected, modifier = Modifier.size(20.dp), color = if (isSelected) Color(0xFF7C3AED) else inactiveColor)
                                            3 -> AnimatedProfileNavIcon(isSelected = isSelected, modifier = Modifier.size(20.dp), color = if (isSelected) activeColor else inactiveColor)
                                            4 -> AnimatedGymNavIcon(isSelected = isSelected, modifier = Modifier.size(20.dp), color = if (isSelected) Color(0xFF10B981) else inactiveColor)
                                            else -> AnimatedHomeNavIcon(isSelected = isSelected, modifier = Modifier.size(20.dp), color = if (isSelected) Color(0xFF8B4513) else inactiveColor)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) activeColor else inactiveColor,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 💓 Interactive Heart Pulse Beat Animation Component for Navigation Bar
@Composable
private fun AnimatedVitalsNavIcon(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VitalsNavPulseAnim")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HeartBeatPulse"
    )

    Canvas(
        modifier = modifier.scale(if (isSelected) pulseScale else 1.0f)
    ) {
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

// 💪 Flexed Biceps Emoji Structure Component for Gym Navigation
@Composable
private fun AnimatedGymNavIcon(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GymBicepFlexAnim")

    val flexProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BicepFlexProgress"
    )

    val activeGymColor = if (isSelected) Color(0xFF10B981) else color
    val surfaceColor = MaterialTheme.colorScheme.surface

    Canvas(
        modifier = modifier.scale(if (isSelected) 1.15f else 1.0f)
    ) {
        val w = size.width
        val h = size.height

        val flexHeight = if (isSelected) (flexProgress * 4.0.dp.toPx()) else 0f

        // 1. Flexed Bicep Arm Main Outline (Matching Bicep Emoji Structure)
        val bicepOutline = Path().apply {
            moveTo(w * 0.28f, h * 0.16f)
            cubicTo(w * 0.12f, h * 0.16f, w * 0.08f, h * 0.32f, w * 0.12f, h * 0.45f)
            lineTo(w * 0.16f, h * 0.76f)
            quadraticTo(w * 0.18f, h * 0.90f, w * 0.35f, h * 0.88f)
            quadraticTo(w * 0.62f, h * 0.82f, w * 0.88f, h * 0.65f)
            lineTo(w * 0.82f, h * 0.36f)
            quadraticTo(w * 0.68f, h * 0.45f, w * 0.60f, h * 0.50f)
            cubicTo(
                w * 0.52f, h * 0.30f - flexHeight,
                w * 0.38f, h * 0.28f - flexHeight,
                w * 0.30f, h * 0.42f
            )
            quadraticTo(w * 0.28f, h * 0.28f, w * 0.28f, h * 0.16f)
            close()
        }

        drawPath(
            path = bicepOutline,
            color = activeGymColor
        )

        // 2. Inner Bicep Muscle Definition Crease Line
        val muscleLine = Path().apply {
            moveTo(w * 0.38f, h * 0.56f)
            quadraticTo(w * 0.55f, h * 0.42f - (flexHeight * 0.5f), w * 0.72f, h * 0.46f)
        }
        drawPath(
            path = muscleLine,
            color = surfaceColor,
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // 3. Knuckle Joint Lines on Clenched Fist (Top Left)
        val knuckleLine = Path().apply {
            moveTo(w * 0.20f, h * 0.20f)
            lineTo(w * 0.26f, h * 0.25f)
            moveTo(w * 0.18f, h * 0.26f)
            lineTo(w * 0.24f, h * 0.31f)
            moveTo(w * 0.18f, h * 0.32f)
            lineTo(w * 0.24f, h * 0.37f)
        }
        drawPath(
            path = knuckleLine,
            color = surfaceColor,
            style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
        )

        // 4. Power Burst Rays
        if (isSelected) {
            val peakX = w * 0.46f
            val peakY = h * 0.26f - flexHeight

            val ray1Start = Offset(peakX - 5.dp.toPx(), peakY - 2.dp.toPx())
            val ray1End = Offset(peakX - 8.dp.toPx(), peakY - 7.dp.toPx())
            drawLine(color = activeGymColor, start = ray1Start, end = ray1End, strokeWidth = 1.6.dp.toPx(), cap = StrokeCap.Round)

            val ray2Start = Offset(peakX, peakY - 3.dp.toPx())
            val ray2End = Offset(peakX, peakY - 9.dp.toPx())
            drawLine(color = activeGymColor, start = ray2Start, end = ray2End, strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)

            val ray3Start = Offset(peakX + 5.dp.toPx(), peakY - 2.dp.toPx())
            val ray3End = Offset(peakX + 8.dp.toPx(), peakY - 7.dp.toPx())
            drawLine(color = activeGymColor, start = ray3Start, end = ray3End, strokeWidth = 1.6.dp.toPx(), cap = StrokeCap.Round)
        }
    }
}

// 👤 Emoji-Style Person Structure Waving "Hi!" Component for Navigation Bar
@Composable
private fun AnimatedProfileNavIcon(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ProfileEmojiNavWave")

    val handWaveAngle by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "EmojiHandWave"
    )

    val headTiltY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -2f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "EmojiHeadTilt"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val headCenterX = if (isSelected) w * 0.42f else w * 0.50f
        val headCenterY = if (isSelected) h * 0.28f + headTiltY.dp.toPx() else h * 0.28f
        val headRadius = w * 0.21f

        // 1. Emoji Person Head (Filled Circle)
        drawCircle(
            color = color,
            radius = headRadius,
            center = Offset(headCenterX, headCenterY)
        )

        // 2. Emoji Person Body / Torso (Filled Shoulders)
        val bodyPath = Path().apply {
            val leftX = if (isSelected) w * 0.12f else w * 0.16f
            val rightX = if (isSelected) w * 0.65f else w * 0.84f
            moveTo(leftX, h * 0.90f)
            lineTo(leftX, h * 0.78f)
            cubicTo(leftX, h * 0.52f, rightX, h * 0.52f, rightX, h * 0.78f)
            lineTo(rightX, h * 0.90f)
            close()
        }

        drawPath(
            path = bodyPath,
            color = color
        )

        // 3. Emoji Person Raised Waving Arm & Hand ("Hi!") 👋
        if (isSelected) {
            val shoulderX = w * 0.62f
            val shoulderY = h * 0.65f

            val waveRad = (handWaveAngle * Math.PI.toFloat() / 180f)
            val handX = shoulderX + (13.dp.toPx() * sin(waveRad + 0.35f))
            val handY = shoulderY - (15.dp.toPx() * cos(waveRad + 0.35f))

            // Raised Arm
            val armPath = Path().apply {
                moveTo(shoulderX, shoulderY)
                quadraticTo(shoulderX + 5.dp.toPx(), shoulderY - 8.dp.toPx(), handX, handY)
            }

            drawPath(
                path = armPath,
                color = color,
                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
            )

            // Waving Hand Palm Circle
            drawCircle(
                color = color,
                radius = 3.0.dp.toPx(),
                center = Offset(handX, handY)
            )

            // Finger Rays (Waving Hand Saying Hi!)
            repeat(4) { fingerIndex ->
                val fingerAngle = (-60f + fingerIndex * 35f + handWaveAngle * 0.8f) * (Math.PI.toFloat() / 180f)
                val fingerStartX = handX + (2.5.dp.toPx() * cos(fingerAngle))
                val fingerStartY = handY + (2.5.dp.toPx() * sin(fingerAngle))
                val fingerEndX = handX + (5.5.dp.toPx() * cos(fingerAngle))
                val fingerEndY = handY + (5.5.dp.toPx() * sin(fingerAngle))

                drawLine(
                    color = color,
                    start = Offset(fingerStartX, fingerStartY),
                    end = Offset(fingerEndX, fingerEndY),
                    strokeWidth = 1.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

// 🏠 Brown House with Black Animated Chimney Smoke Puff Component for Navigation Bar
@Composable
private fun AnimatedHomeNavIcon(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    val infiniteTransition = rememberInfiniteTransition(label = "HomeNavIconAnim")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HomePulseScale"
    )

    val smokeProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ChimneySmokeProgress"
    )

    val houseColor = if (isSelected) Color(0xFF8B4513) else color
    val blackSmokeColor = Color(0xFF111827)

    Canvas(
        modifier = modifier.scale(if (isSelected) pulseScale else 1.0f)
    ) {
        val w = size.width
        val h = size.height

        // 1. Chimney on Roof Right Side
        val chimneyTopY = h * 0.22f
        val chimneyPath = Path().apply {
            moveTo(w * 0.68f, h * 0.30f)
            lineTo(w * 0.68f, chimneyTopY)
            lineTo(w * 0.80f, chimneyTopY)
            lineTo(w * 0.80f, h * 0.42f)
        }
        drawPath(
            path = chimneyPath,
            color = houseColor,
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 2. House Main Outline (Roof + Walls)
        val housePath = Path().apply {
            moveTo(w * 0.50f, h * 0.16f) // Roof Peak
            lineTo(w * 0.88f, h * 0.48f) // Right roof corner
            lineTo(w * 0.88f, h * 0.88f) // Right wall bottom
            lineTo(w * 0.12f, h * 0.88f) // Left wall bottom
            lineTo(w * 0.12f, h * 0.48f) // Left roof corner
            close()
        }
        drawPath(
            path = housePath,
            color = houseColor,
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 3. Front Door
        val doorPath = Path().apply {
            moveTo(w * 0.38f, h * 0.88f)
            lineTo(w * 0.38f, h * 0.58f)
            lineTo(w * 0.62f, h * 0.58f)
            lineTo(w * 0.62f, h * 0.88f)
        }
        drawPath(
            path = doorPath,
            color = houseColor,
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 4. Black Chimney Smoke Puff Clouds (Active when Home Tab is Selected)
        if (isSelected) {
            val chimneyCenterX = w * 0.74f
            val smokePuffs = listOf(0.0f, 0.33f, 0.66f)

            smokePuffs.forEach { offsetFraction ->
                val progress = (smokeProgress + offsetFraction) % 1.0f
                val riseY = chimneyTopY - (progress * 14.dp.toPx())
                val driftX = chimneyCenterX + (sin(progress * Math.PI.toFloat() * 2f) * 2.5.dp.toPx())
                val radius = (1.8.dp.toPx() + (progress * 2.2.dp.toPx()))
                val alpha = (1.0f - progress).coerceIn(0f, 1f)

                drawCircle(
                    color = blackSmokeColor.copy(alpha = alpha * 0.88f),
                    radius = radius,
                    center = Offset(driftX, riseY)
                )
            }
        }
    }
}

// 🔮 Interactive Pulsating AI Sparkle Star Animation Component for Navigation Bar
@Composable
private fun AnimatedAiNavSparkleIcon(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF7C3AED)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AiNavSparkleAnim")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AiSparklePulse"
    )

    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AiSparkleSpin"
    )

    Canvas(
        modifier = modifier
            .scale(if (isSelected) pulseScale else 1.0f)
    ) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        val path = Path().apply {
            moveTo(cx, 0f)
            quadraticTo(cx, cy, w, cy)
            quadraticTo(cx, cy, cx, h)
            quadraticTo(cx, cy, 0f, cy)
            quadraticTo(cx, cy, cx, 0f)
            close()
        }

        drawPath(path = path, color = color)

        if (isSelected) {
            repeat(4) { i ->
                val angle = (i * 90f + spinAngle) * (Math.PI.toFloat() / 180f)
                val r = w * 0.55f
                val dotX = cx + r * cos(angle)
                val dotY = cy + r * sin(angle)
                drawCircle(
                    color = Color(0xFFC4B5FD),
                    radius = 2.2.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }
        }
    }
}

// Icons
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
    color: Color = Color(0xFF4C1D95)
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun HomeScreenPreview() {
    VitalsyncTheme {
        HomeScreen()
    }
}
