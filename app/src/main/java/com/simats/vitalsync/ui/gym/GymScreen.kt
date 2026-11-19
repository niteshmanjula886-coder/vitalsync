package com.simats.vitalsync.ui.gym

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.vitalsync.data.Exercise
import com.simats.vitalsync.data.WorkoutExercise
import com.simats.vitalsync.data.WorkoutSet
import com.simats.vitalsync.data.local.SessionManager
import com.simats.vitalsync.data.model.gym.ExerciseDto
import com.simats.vitalsync.data.model.gym.GymNutritionLogDto
import com.simats.vitalsync.data.model.gym.WorkoutExerciseDto
import com.simats.vitalsync.data.model.gym.WorkoutSessionDto
import com.simats.vitalsync.data.model.gym.WorkoutSetDto
import com.simats.vitalsync.ui.gym.viewmodel.GymViewModel
import com.simats.vitalsync.ui.gym.viewmodel.GymViewModelFactory
import com.simats.vitalsync.ui.theme.VitalPrimaryMint
import com.simats.vitalsync.ui.theme.VitalTealDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GymScreen(
    modifier: Modifier = Modifier,
    viewModel: GymViewModel = viewModel(factory = GymViewModelFactory()),
    onStartWorkoutSplitClick: () -> Unit = {},
    onOpenNutritionTrackerClick: () -> Unit = {},
    onOpenProgressAnalyticsClick: () -> Unit = {},
    onViewWorkoutHistoryClick: () -> Unit = {},
    onFinishGymSession: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }
    val userId = remember { sessionManager.getUser()?.id }
    val userEmail = remember { sessionManager.getUserEmail() }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Initialize persistent local state & remote telemetry for active user
    LaunchedEffect(userId, userEmail) {
        viewModel.initSession(context, userId, userEmail)
    }

    // Refresh telemetry when switching tabs
    LaunchedEffect(uiState.selectedTab) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (uiState.selectedTab == 0) {
            viewModel.loadGymWorkouts(userId = userId, email = userEmail, date = todayStr)
        } else if (uiState.selectedTab == 1) {
            viewModel.loadGymNutrition(userId = userId, email = userEmail, date = todayStr)
        } else if (uiState.selectedTab == 2) {
            viewModel.loadGymAnalytics(userId = userId, email = userEmail)
        }
    }

    // Show Snackbars for feedback
    LaunchedEffect(uiState.saveWorkoutSuccessMessage) {
        uiState.saveWorkoutSuccessMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.nutritionSuccessMessage) {
        uiState.nutritionSuccessMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.workoutErrorMessage) {
        uiState.workoutErrorMessage?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.nutritionErrorMessage) {
        uiState.nutritionErrorMessage?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearMessages()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // -----------------------------------------------------------------
            // Top Header Bar
            // -----------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Gym & Fitness 🏋️",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Workouts, Smart Nutrition & Analytics",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -----------------------------------------------------------------
            // Top Segmented 3-Tab Selector
            // -----------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf(
                    "🏋️ Workouts",
                    "🥗 Nutrition",
                    "📈 Analytics"
                )
                tabs.forEachIndexed { index, label ->
                    val isSelected = uiState.selectedTab == index
                    val tabBgColor by animateColorAsState(
                        targetValue = if (isSelected) VitalTealDark else Color.Transparent,
                        label = "tabBg"
                    )
                    val tabTextColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "tabText"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tabBgColor)
                            .clickable { viewModel.selectTab(index) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = tabTextColor,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // -----------------------------------------------------------------
            // 3-Tab Content Containers
            // -----------------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (uiState.selectedTab) {
                    0 -> WorkoutsTabContent(
                        viewModel = viewModel,
                        userId = userId,
                        userEmail = userEmail
                    )
                    1 -> NutritionTabContent(
                        viewModel = viewModel,
                        userId = userId,
                        userEmail = userEmail
                    )
                    2 -> AnalyticsTabContent(
                        viewModel = viewModel,
                        userId = userId,
                        userEmail = userEmail
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

// =============================================================================
// TAB 1: Workouts (Persistent Per-Split Exercises, Rest Timer Controls, Today's History)
// =============================================================================

@Composable
private fun WorkoutsTabContent(
    viewModel: GymViewModel,
    userId: Int?,
    userEmail: String?
) {
    val uiState by viewModel.uiState.collectAsState()
    val splitExercisesMap by viewModel.splitExercises.collectAsState()
    val currentSplitExercises = splitExercisesMap[uiState.selectedSplit] ?: emptyList()

    // Dialog state for adding custom exercise
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var customExName by remember { mutableStateOf("") }
    var customExMuscle by remember { mutableStateOf("") }
    var customExWeight by remember { mutableStateOf("20") }
    var customExSets by remember { mutableStateOf("3") }
    var customExReps by remember { mutableStateOf("10") }

    // Dialog state for custom split name
    var showAddSplitDialog by remember { mutableStateOf(false) }
    var newSplitNameInput by remember { mutableStateOf("") }

    // Completion summary dialog state
    var showFinishSummaryDialog by remember { mutableStateOf(false) }

    // Live Metrics Calculation
    val totalVolume = currentSplitExercises.sumOf { we ->
        we.sets.filter { it.isCompleted }.sumOf { (it.weightKg * it.reps).toDouble() }
    }.toFloat()

    val completedSets = currentSplitExercises.sumOf { we ->
        we.sets.count { it.isCompleted }
    }
    val totalSetsDone = completedSets

    val activeMinutes = (uiState.workoutTimerSeconds / 60).toInt().coerceAtLeast(1)
    val estimatedCalories = viewModel.calculateEstimatedCalories(
        splitName = uiState.selectedSplit,
        durationMinutes = activeMinutes,
        completedSets = completedSets,
        totalVolumeKg = totalVolume
    )

    // Keep ViewModel in sync with live metrics
    LaunchedEffect(totalVolume, totalSetsDone, estimatedCalories) {
        viewModel.updateLiveWorkoutMetrics(totalVolume, totalSetsDone, estimatedCalories)
    }

    val timerFormatted = run {
        val totalSec = uiState.workoutTimerSeconds
        val hrs = totalSec / 3600
        val mins = (totalSec % 3600) / 60
        val secs = totalSec % 60
        if (hrs > 0) String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
        else String.format(Locale.US, "%02d:%02d", mins, secs)
    }

    val restFormatted = run {
        val sec = uiState.restTimerSeconds
        val mins = sec / 60
        val s = sec % 60
        String.format(Locale.US, "%02d:%02d", mins, s)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ---------------------------------------------------------------------
        // Hero Active Workout Session Banner (Dark Teal Container)
        // ---------------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = VitalTealDark),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ACTIVE SESSION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA7F3D0),
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${uiState.selectedSplit} Routine",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Live Workout Timer Play/Pause
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x28FFFFFF))
                            .clickable {
                                if (uiState.isWorkoutTimerRunning) viewModel.pauseWorkoutTimer()
                                else viewModel.startWorkoutTimer()
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (uiState.isWorkoutTimerRunning) "⏸ " else "▶ ",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            Text(
                                text = timerFormatted,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // -------------------------------------------------------------
                // Rest Timer Bar with Start / Stop / Reset (Issue 3)
                // -------------------------------------------------------------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x1EFFFFFF))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⏱ Rest: ", fontSize = 12.sp, color = Color(0xFFA7F3D0), fontWeight = FontWeight.Medium)
                        Text(
                            text = if (uiState.restTimerSeconds > 0) restFormatted else "Ready",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isRestTimerRunning) Color(0xFFFDE047) else Color.White
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Presets: 60s, 90s, 120s
                        listOf(60, 90, 120).forEach { sec ->
                            val isPresetActive = uiState.initialRestTimerPreset == sec
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isPresetActive) Color(0x55FFFFFF) else Color(0x24FFFFFF))
                                    .clickable { viewModel.setRestTimerPreset(sec) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${sec}s",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Start / Stop Toggle Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (uiState.isRestTimerRunning) Color(0xFFDC2626) else Color(0xFF059669))
                                .clickable {
                                    if (uiState.isRestTimerRunning) viewModel.pauseRestTimer()
                                    else viewModel.startRestTimer()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (uiState.isRestTimerRunning) "Stop" else "Start",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Reset Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33FFFFFF))
                                .clickable { viewModel.resetRestTimer() }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "↺",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Total Volume", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                        Text(
                            text = String.format(Locale.US, "%,d kg", totalVolume.toInt()),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column {
                        Text(text = "Sets Completed", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                        Text(
                            text = "$totalSetsDone sets",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column {
                        Text(text = "Est. Calories", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                        Text(
                            text = "$estimatedCalories kcal",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ---------------------------------------------------------------------
        // Horizontal Training Split Chips & Removable Custom Splits (Issue 1 & 2)
        // ---------------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Training Splits",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Reset Default Exercises Option (Issue 1)
            Text(
                text = "↺ Reset Defaults",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = VitalPrimaryMint,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.resetSplitToDefault(uiState.selectedSplit) }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val allSplits = listOf("Push", "Pull", "Legs", "Arms", "Cardio", "Abs") + uiState.customSplits
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allSplits.forEach { split ->
                val isSelected = uiState.selectedSplit.equals(split, ignoreCase = true)
                val isCustom = uiState.customSplits.any { it.equals(split, ignoreCase = true) }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) VitalTealDark else MaterialTheme.colorScheme.surface)
                        .clickable { viewModel.selectSplit(split) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = split,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Close/Delete icon for custom split chips (Issue 2)
                        if (isCustom) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0x33FFFFFF) else Color(0x1F000000))
                                    .clickable { viewModel.removeCustomSplit(split) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✕",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // + Custom Split Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showAddSplitDialog = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "+ Custom Split",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = VitalPrimaryMint
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ---------------------------------------------------------------------
        // Persistent Exercise Cards & Interactive Set-by-Set Logging (Issue 1)
        // ---------------------------------------------------------------------
        if (currentSplitExercises.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🏋️", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Exercises in ${uiState.selectedSplit}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You removed all exercises from this split. Tap below to add custom exercises or restore the default split routine.",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = { viewModel.resetSplitToDefault(uiState.selectedSplit) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Restore Default Routine", color = VitalPrimaryMint)
                    }
                }
            }
        } else {
            currentSplitExercises.forEachIndexed { exIndex, we ->
                androidx.compose.runtime.key(we.exercise.id.ifEmpty { "${we.exercise.name}_$exIndex" }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${getExerciseEmoji(we.exercise.targetMuscle)} ${we.exercise.name}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Target: ${we.exercise.targetMuscle}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Remove exercise permanently from this split (Issue 1)
                                IconButton(
                                    onClick = { viewModel.removeExerciseFromSplit(uiState.selectedSplit, exIndex) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Text(text = "✕", fontSize = 14.sp, color = Color(0xFF9CA3AF))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "SET", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(36.dp))
                                Text(text = "KG", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(text = "REPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(text = "DONE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(52.dp), textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.width(28.dp))
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Set Rows
                            we.sets.forEachIndexed { setIndex, st ->
                                androidx.compose.runtime.key("${we.exercise.id}_set_${st.setNumber}_$setIndex") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${st.setNumber}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.width(36.dp)
                                        )

                                        // Editable Weight (kg)
                                        var weightText by remember(st.weightKg) { mutableStateOf(if (st.weightKg % 1f == 0f) st.weightKg.toInt().toString() else st.weightKg.toString()) }
                                        OutlinedTextField(
                                            value = weightText,
                                            onValueChange = { input ->
                                                weightText = input
                                                val newW = input.toFloatOrNull() ?: st.weightKg
                                                viewModel.updateSetInExercise(
                                                    splitName = uiState.selectedSplit,
                                                    exerciseIndex = exIndex,
                                                    setIndex = setIndex,
                                                    weightKg = newW
                                                )
                                            },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .padding(horizontal = 4.dp),
                                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, textAlign = TextAlign.Center),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                                focusedBorderColor = VitalTealDark
                                            )
                                        )

                                        // Editable Reps
                                        var repsText by remember(st.reps) { mutableStateOf(st.reps.toString()) }
                                        OutlinedTextField(
                                            value = repsText,
                                            onValueChange = { input ->
                                                repsText = input
                                                val newR = input.toIntOrNull() ?: st.reps
                                                viewModel.updateSetInExercise(
                                                    splitName = uiState.selectedSplit,
                                                    exerciseIndex = exIndex,
                                                    setIndex = setIndex,
                                                    reps = newR
                                                )
                                            },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .padding(horizontal = 4.dp),
                                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, textAlign = TextAlign.Center),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                                focusedBorderColor = VitalTealDark
                                            )
                                        )

                                        // Done Checkbox
                                        Box(
                                            modifier = Modifier.width(52.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Checkbox(
                                                checked = st.isCompleted,
                                                onCheckedChange = { checked ->
                                                    viewModel.updateSetInExercise(
                                                        splitName = uiState.selectedSplit,
                                                        exerciseIndex = exIndex,
                                                        setIndex = setIndex,
                                                        isCompleted = checked
                                                    )
                                                    // Trigger rest timer auto-start on set completion
                                                    if (checked) {
                                                        viewModel.startRestTimer()
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = VitalPrimaryMint,
                                                    checkmarkColor = Color.White
                                                )
                                            )
                                        }

                                        // Delete Set Button
                                        IconButton(
                                            onClick = {
                                                if (we.sets.size > 1) {
                                                    viewModel.removeSetFromExercise(
                                                        splitName = uiState.selectedSplit,
                                                        exerciseIndex = exIndex,
                                                        setIndex = setIndex
                                                    )
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Text(text = "–", fontSize = 16.sp, color = Color(0xFF9CA3AF))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // + Add Set Button (Issue 3: Atomic Appending to Exercise's Discrete List)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable {
                                        viewModel.addSetToExercise(
                                            splitName = uiState.selectedSplit,
                                            exerciseIndex = exIndex
                                        )
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "+ Add Set",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VitalPrimaryMint
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ---------------------------------------------------------------------
        // + Add Workout / Exercise Button (Manual Entry)
        // ---------------------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFE8F6F0))
                .clickable { showAddExerciseDialog = true }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+ Add Workout / Exercise to ${uiState.selectedSplit}",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = VitalPrimaryMint
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ---------------------------------------------------------------------
        // Finish & Save Workout Button
        // ---------------------------------------------------------------------
        Button(
            onClick = { showFinishSummaryDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VitalTealDark,
                contentColor = Color.White
            ),
            enabled = !uiState.isSavingWorkout
        ) {
            if (uiState.isSavingWorkout) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "Saving Workout...", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            } else {
                Text(text = "Finish & Save Workout ✓", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ---------------------------------------------------------------------
        // Today's Logged Workouts History Section (Issue 6)
        // ---------------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Logged Workouts (${uiState.todayWorkouts.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (uiState.isWorkoutsLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = VitalPrimaryMint)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.todayWorkouts.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "📝", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No Workouts Logged Today",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Save your completed routines above to record your daily sets, volume, and calories.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            uiState.todayWorkouts.forEach { workout ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(VitalTealDark)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = workout.split.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = workout.timestampFormatted,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Delete Mistaken Workout Button
                            IconButton(
                                onClick = {
                                    val wId = workout.id.toIntOrNull()
                                    if (wId != null && wId > 0) {
                                        viewModel.deleteGymWorkout(wId, userId, userEmail)
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text(text = "🗑️", fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stats Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "⏱ ${workout.durationMinutes} mins",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "🏋️ ${workout.totalVolumeKg.toInt()} kg",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "📊 ${workout.totalSets} sets",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "🔥 ${workout.estimatedCaloriesBurned} kcal",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = VitalPrimaryMint
                            )
                        }

                        // Completed exercises breakdown (strictly completed only)
                        val completedExercises = workout.exercises.map { exercise ->
                            exercise.copy(sets = exercise.sets.filter { it.isCompleted })
                        }.filter { it.sets.isNotEmpty() }

                        if (completedExercises.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(8.dp))
                            completedExercises.forEach { we ->
                                val setsText = "${we.sets.size} sets (${we.sets.firstOrNull()?.weightKg?.toInt() ?: 0}kg)"
                                Text(
                                    text = "• ${we.exercise.name}: $setsText",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    // -------------------------------------------------------------------------
    // Dialog 1: Add Custom Exercise
    // -------------------------------------------------------------------------
    if (showAddExerciseDialog) {
        AlertDialog(
            onDismissRequest = { showAddExerciseDialog = false },
            title = { Text(text = "Add Custom Exercise", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = customExName,
                        onValueChange = { customExName = it },
                        label = { Text("Exercise Name") },
                        placeholder = { Text("e.g. Dumbbell Shrugs") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customExMuscle,
                        onValueChange = { customExMuscle = it },
                        label = { Text("Target Muscle") },
                        placeholder = { Text("e.g. Traps") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customExWeight,
                            onValueChange = { customExWeight = it },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customExReps,
                            onValueChange = { customExReps = it },
                            label = { Text("Reps") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = customExName.trim()
                        if (name.isNotBlank()) {
                            val w = customExWeight.toFloatOrNull() ?: 20f
                            val r = customExReps.toIntOrNull() ?: 10
                            val setsCount = customExSets.toIntOrNull() ?: 3
                            val ex = Exercise(
                                id = "custom_${System.currentTimeMillis()}",
                                name = name,
                                targetMuscle = customExMuscle.ifBlank { uiState.selectedSplit },
                                defaultWeightKg = w,
                                defaultReps = r
                            )
                            val sets = (1..setsCount).map { WorkoutSet(it, r, w, false) }
                            viewModel.addExerciseToSplit(uiState.selectedSplit, WorkoutExercise(exercise = ex, sets = sets))
                            customExName = ""
                            customExMuscle = ""
                            showAddExerciseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VitalTealDark)
                ) {
                    Text("Add to Routine")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExerciseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // -------------------------------------------------------------------------
    // Dialog 2: Add Custom Split
    // -------------------------------------------------------------------------
    if (showAddSplitDialog) {
        AlertDialog(
            onDismissRequest = { showAddSplitDialog = false },
            title = { Text(text = "Create Custom Split", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newSplitNameInput,
                    onValueChange = { newSplitNameInput = it },
                    label = { Text("Split Name") },
                    placeholder = { Text("e.g. Upper Body, Full Body") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSplitNameInput.isNotBlank()) {
                            viewModel.addCustomSplit(newSplitNameInput.trim())
                            newSplitNameInput = ""
                            showAddSplitDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VitalTealDark)
                ) {
                    Text("Create Split")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSplitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // -------------------------------------------------------------------------
    // Dialog 3: Post-Workout Summary & Confirmation
    // -------------------------------------------------------------------------
    if (showFinishSummaryDialog) {
        AlertDialog(
            onDismissRequest = { showFinishSummaryDialog = false },
            title = { Text(text = "Complete Workout Session?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Summary of your ${uiState.selectedSplit} workout:")
                    Text(text = "• Total Volume: ${totalVolume.toInt()} kg", fontWeight = FontWeight.SemiBold)
                    Text(text = "• Completed Sets: $totalSetsDone sets", fontWeight = FontWeight.SemiBold)
                    Text(text = "• Active Duration: $activeMinutes minutes", fontWeight = FontWeight.SemiBold)
                    Text(text = "• Calories Burned: $estimatedCalories kcal", fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishSummaryDialog = false
                        val dtoList = currentSplitExercises.mapNotNull { we ->
                            val doneSets = we.sets.filter { it.isCompleted }
                            if (doneSets.isNotEmpty()) {
                                WorkoutExerciseDto(
                                    exercise = ExerciseDto(
                                        id = we.exercise.id,
                                        name = we.exercise.name,
                                        targetMuscle = we.exercise.targetMuscle,
                                        defaultWeightKg = we.exercise.defaultWeightKg,
                                        defaultReps = we.exercise.defaultReps
                                    ),
                                    sets = doneSets.map { st ->
                                        WorkoutSetDto(
                                            setNumber = st.setNumber,
                                            reps = st.reps,
                                            weightKg = st.weightKg,
                                            isCompleted = true
                                        )
                                    }
                                )
                            } else null
                        }
                        viewModel.finishAndSaveWorkout(
                            userId = userId,
                            email = userEmail,
                            splitName = uiState.selectedSplit,
                            durationMins = activeMinutes,
                            volumeKg = totalVolume,
                            totalSets = totalSetsDone,
                            caloriesBurned = estimatedCalories,
                            exercises = dtoList
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VitalTealDark)
                ) {
                    Text("Save to Cloud")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishSummaryDialog = false }) {
                    Text("Continue Workout")
                }
            }
        )
    }
}

// =============================================================================
// TAB 2: Nutrition (Dual-Mode Protein Target, Ollama AI & Meal Logs)
// =============================================================================

@Composable
private fun NutritionTabContent(
    viewModel: GymViewModel,
    userId: Int?,
    userEmail: String?
) {
    val uiState by viewModel.uiState.collectAsState()
    val todayNutrition = uiState.todayNutrition

    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Pre-Workout", "Post-Workout / Snack")
    var showManualMacroOverrides by remember { mutableStateOf(false) }

    // Dialogs for target configuration
    var showWeightDialog by remember { mutableStateOf(false) }
    var weightInput by remember { mutableStateOf(uiState.bodyWeightKg.toInt().toString()) }

    var showCustomTargetDialog by remember { mutableStateOf(false) }
    var customProtInput by remember { mutableStateOf(uiState.customProteinTargetG.toInt().toString()) }
    var customCalInput by remember { mutableStateOf(uiState.customCaloriesTarget.toString()) }

    // Dynamic targets from dual-mode settings (Issue 5)
    val totalCal = todayNutrition?.totalCalories ?: 0
    val targetCal = uiState.activeCaloriesTarget
    val totalProt = todayNutrition?.totalProtein ?: 0f
    val targetProt = uiState.activeProteinTargetG

    val targetCarbs = (targetCal * 0.45f / 4f)
    val totalCarbs = todayNutrition?.totalCarbs ?: 0f
    val targetFats = (targetCal * 0.25f / 9f)
    val totalFats = todayNutrition?.totalFats ?: 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ---------------------------------------------------------------------
        // Protein Target Mode Selector (Issue 5)
        // ---------------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Daily Protein Target Mode",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(3.dp)
                ) {
                    val isBodyWeight = uiState.proteinTargetMode == "BODY_WEIGHT"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isBodyWeight) VitalTealDark else Color.Transparent)
                            .clickable { viewModel.setProteinTargetMode("BODY_WEIGHT") }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚖️ Body Weight (Auto)",
                            fontSize = 12.sp,
                            fontWeight = if (isBodyWeight) FontWeight.Bold else FontWeight.Medium,
                            color = if (isBodyWeight) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isBodyWeight) VitalTealDark else Color.Transparent)
                            .clickable { viewModel.setProteinTargetMode("CUSTOM") }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✏️ Custom Manual",
                            fontSize = 12.sp,
                            fontWeight = if (!isBodyWeight) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isBodyWeight) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.proteinTargetMode == "BODY_WEIGHT") {
                    // Body Weight Multipliers & Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Weight: ${uiState.bodyWeightKg.toInt()} kg",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "✎ Edit",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = VitalPrimaryMint,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { showWeightDialog = true }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "Goal: ${targetProt.toInt()}g protein",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = VitalPrimaryMint
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple(1.6f, "1.6g/kg", "Build"),
                            Triple(1.8f, "1.8g/kg", "Optimal"),
                            Triple(2.0f, "2.0g/kg", "Cut")
                        ).forEach { (mult, label, desc) ->
                            val isSel = (uiState.bodyWeightMultiplier == mult)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) Color(0xFFE8F6F0) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .clickable { viewModel.setBodyWeightMultiplier(mult) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = label,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) VitalTealDark else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 10.sp,
                                        color = if (isSel) VitalPrimaryMint else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Custom Targets View
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Target: ${uiState.customProteinTargetG.toInt()}g Protein • ${uiState.customCaloriesTarget} kcal",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Custom user-defined daily macronutrient goal",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextButton(onClick = { showCustomTargetDialog = true }) {
                            Text(text = "Edit Targets", fontSize = 12.sp, color = VitalPrimaryMint, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ---------------------------------------------------------------------
        // Daily Macro Summary Card
        // ---------------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TODAY'S NUTRITION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = VitalPrimaryMint,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$totalCal / $targetCal kcal",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    val calPct = ((totalCal.toFloat() / targetCal.toFloat()) * 100).toInt().coerceIn(0, 100)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE8F6F0))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$calPct% Daily Target",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = VitalPrimaryMint
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Macro Progress Bars bound to active targets
                MacroBar(label = "Protein", current = totalProt, target = targetProt, unit = "g", barColor = Color(0xFF026C54))
                Spacer(modifier = Modifier.height(8.dp))
                MacroBar(label = "Carbs", current = totalCarbs, target = targetCarbs, unit = "g", barColor = Color(0xFF0284C7))
                Spacer(modifier = Modifier.height(8.dp))
                MacroBar(label = "Fats", current = totalFats, target = targetFats, unit = "g", barColor = Color(0xFFD97706))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ---------------------------------------------------------------------
        // AI Nutrition Coach Tip Card
        // ---------------------------------------------------------------------
        val aiTip = todayNutrition?.aiCoachTip ?: if (totalProt >= targetProt) {
            "Great work! You reached your daily protein target of ${targetProt.toInt()}g, priming your body for optimal recovery."
        } else {
            "Aim to reach ${targetProt.toInt()}g protein today. Log meals below to receive instant USDA macro analysis."
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
            border = BorderStroke(1.dp, Color(0xFFBBF7D0))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(text = "✨", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "VitalSync AI Nutrition Coach",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF166534)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = aiTip,
                        fontSize = 12.5.sp,
                        color = Color(0xFF1E293B),
                        lineHeight = 17.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ---------------------------------------------------------------------
        // Food Logging Form with USDA Ollama AI Analysis (Issue 4)
        // ---------------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Log Meal or Snack",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Meal Type Selector Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    mealTypes.forEach { mt ->
                        val isSelected = uiState.selectedMealType.equals(mt, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) VitalTealDark else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { viewModel.updateFoodInputs(mealType = mt) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = mt,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Food Description Input
                OutlinedTextField(
                    value = uiState.foodDescriptionInput,
                    onValueChange = { viewModel.updateFoodInputs(description = it) },
                    placeholder = { Text("e.g. potato chips 100g, 3 boiled eggs, 1 cup rice & dal") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VitalTealDark,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Optional Manual Macro Overrides Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showManualMacroOverrides = !showManualMacroOverrides }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showManualMacroOverrides) "▲ Hide manual macro inputs" else "▼ Enter exact macros manually (Optional)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(visible = showManualMacroOverrides) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = uiState.manualCaloriesInput,
                                onValueChange = { viewModel.updateFoodInputs(calories = it) },
                                label = { Text("Calories") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = uiState.manualProteinInput,
                                onValueChange = { viewModel.updateFoodInputs(protein = it) },
                                label = { Text("Protein (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = uiState.manualCarbsInput,
                                onValueChange = { viewModel.updateFoodInputs(carbs = it) },
                                label = { Text("Carbs (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = uiState.manualFatsInput,
                                onValueChange = { viewModel.updateFoodInputs(fats = it) },
                                label = { Text("Fats (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dual Action Buttons: AI Analysis vs Manual
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.analyzeAndLogNutrition(userId = userId, email = userEmail, saveManually = false) },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VitalTealDark,
                            contentColor = Color.White
                        ),
                        enabled = !uiState.isAnalyzingNutrition
                    ) {
                        if (uiState.isAnalyzingNutrition) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Analyzing...", fontSize = 13.sp)
                        } else {
                            Text(text = "Analyze & Log ✨", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.analyzeAndLogNutrition(userId = userId, email = userEmail, saveManually = true) },
                        modifier = Modifier
                            .weight(0.9f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
                    ) {
                        Text(text = "Save Direct", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ---------------------------------------------------------------------
        // Today's Logged Meals
        // ---------------------------------------------------------------------
        val meals = todayNutrition?.meals ?: emptyList()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Meals (${meals.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (uiState.isNutritionLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = VitalPrimaryMint)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (meals.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🥗", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No Meals Logged Today",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Log your meals or snacks above to track calories and protein towards your daily goals.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            meals.forEach { meal ->
                MealCardItem(
                    meal = meal,
                    onDelete = { viewModel.deleteGymNutrition(meal.id, userId, userEmail) }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    // Dialog: Edit Body Weight
    if (showWeightDialog) {
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            title = { Text(text = "Set Body Weight", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Body Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = weightInput.toFloatOrNull() ?: 70f
                        viewModel.setBodyWeight(w)
                        showWeightDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VitalTealDark)
                ) {
                    Text("Save Weight")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWeightDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Edit Custom Target
    if (showCustomTargetDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTargetDialog = false },
            title = { Text(text = "Custom Nutritional Targets", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customProtInput,
                        onValueChange = { customProtInput = it },
                        label = { Text("Daily Protein Target (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customCalInput,
                        onValueChange = { customCalInput = it },
                        label = { Text("Daily Calorie Target (kcal)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = customProtInput.toFloatOrNull() ?: 140f
                        val c = customCalInput.toIntOrNull() ?: 2200
                        viewModel.setCustomTargets(p, c)
                        showCustomTargetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VitalTealDark)
                ) {
                    Text("Save Targets")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTargetDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun MacroBar(label: String, current: Float, target: Float, unit: String, barColor: Color) {
    val progress = if (target > 0f) (current / target).coerceIn(0f, 1f) else 0f
    val pct = (progress * 100).toInt()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$label: ${current.toInt()}$unit / ${target.toInt()}$unit",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "$pct%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceAtLeast(0.02f))
                    .height(7.dp)
                    .clip(CircleShape)
                    .background(barColor)
            )
        }
    }
}

@Composable
private fun MealCardItem(
    meal: GymNutritionLogDto,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8F6F0))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = meal.mealType,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = VitalPrimaryMint
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = meal.foodDescription,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Text(text = "🗑️", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Macros Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "🔥 ${meal.calories} kcal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = "P: ${meal.proteinG.toInt()}g", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF026C54))
                Text(text = "C: ${meal.carbsG.toInt()}g", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0284C7))
                Text(text = "F: ${meal.fatsG.toInt()}g", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFD97706))
            }

            if (meal.aiFeedback.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 ${meal.aiFeedback}",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

// =============================================================================
// TAB 3: Analytics (Day-to-Day Strictly Daily View - Issue 7)
// =============================================================================

@Composable
private fun AnalyticsTabContent(
    viewModel: GymViewModel,
    userId: Int?,
    userEmail: String?
) {
    val uiState by viewModel.uiState.collectAsState()
    val analytics = uiState.analyticsData

    val dailyWorkouts = analytics?.dailyWorkoutsCount ?: 0
    val dailySplits = analytics?.dailySplitsTrained ?: "Rest Day"
    val dailyVolume = analytics?.dailyVolumeKg ?: 0f
    val dailySets = analytics?.dailySets ?: 0
    val dailyDuration = analytics?.dailyDurationMins ?: 0
    val dailyCaloriesIn = analytics?.dailyCaloriesIn ?: 0
    val dailyProtein = analytics?.dailyProteinG ?: 0f
    val dailyBurned = analytics?.dailyCaloriesBurned ?: 0
    val dailyNet = analytics?.dailyNetEnergy ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ---------------------------------------------------------------------
        // Day-to-Day Date Switcher (Issue 7)
        // ---------------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { viewModel.changeAnalyticsDay(-1, userId, userEmail) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(text = "◀ Prev", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = analytics?.selectedDateFormatted?.ifBlank { "Today" } ?: "Today",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (analytics?.isToday == true) {
                            Text(
                                text = "Current Day",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = VitalPrimaryMint
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { viewModel.changeAnalyticsDay(1, userId, userEmail) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(text = "Next ▶", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 7-Day Horizontal Quick Selector Pills
                val daysComparison = analytics?.last7DaysComparison ?: emptyList()
                if (daysComparison.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        daysComparison.forEach { d ->
                            val isSel = d.isSelected
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) VitalTealDark else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .clickable { viewModel.selectAnalyticsDate(d.date, userId, userEmail) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = d.dayLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = d.dateFormatted,
                                        fontSize = 10.sp,
                                        color = if (isSel) Color(0xFFA7F3D0) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (d.workoutsCount > 0) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(if (isSel) Color(0xFFFDE047) else VitalPrimaryMint)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ---------------------------------------------------------------------
        // Daily Summary 2x2 Performance Grid (Selected Day)
        // ---------------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnalyticsMetricCard(
                title = "Today's Workouts",
                value = "$dailyWorkouts session${if (dailyWorkouts == 1) "" else "s"}",
                subtext = "Trained: $dailySplits",
                modifier = Modifier.weight(1f)
            )
            AnalyticsMetricCard(
                title = "Volume Lifted",
                value = String.format(Locale.US, "%,d kg", dailyVolume.toInt()),
                subtext = "$dailySets sets • $dailyDuration mins",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnalyticsMetricCard(
                title = "Nutrition Intake",
                value = "$dailyCaloriesIn kcal",
                subtext = "Protein: ${dailyProtein.toInt()}g / ${uiState.activeProteinTargetG.toInt()}g",
                modifier = Modifier.weight(1f)
            )
            AnalyticsMetricCard(
                title = "Net Energy Balance",
                value = "${if (dailyNet >= 0) "+$dailyNet" else "$dailyNet"} kcal",
                subtext = "$dailyCaloriesIn in – $dailyBurned out",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ---------------------------------------------------------------------
        // Selected Day Workout Routine Breakdown
        // ---------------------------------------------------------------------
        val dayWorkouts = analytics?.dailyWorkoutsList ?: emptyList()
        Text(
            text = "Workouts on This Day (${dayWorkouts.size})",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (dayWorkouts.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🛌", fontSize = 26.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rest Day",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "No gym sessions logged on this calendar date.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            dayWorkouts.forEach { w ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${w.split} Routine", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Text(text = w.timestampFormatted, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${w.totalVolumeKg.toInt()} kg volume • ${w.totalSets} sets • ${w.durationMinutes} mins • ${w.estimatedCaloriesBurned} kcal burned",
                            fontSize = 12.sp,
                            color = VitalPrimaryMint,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ---------------------------------------------------------------------
        // Day-by-Day 7-Day Comparison Table (Issue 7)
        // ---------------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "7-Day Daily History Breakdown",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Daily volume tonnage, split patterns, and nutrition intake",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                val comparisonList = analytics?.last7DaysComparison ?: emptyList()
                if (comparisonList.isEmpty()) {
                    Text(text = "No history recorded yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    comparisonList.forEach { day ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (day.isSelected) Color(0xFFE8F6F0) else Color.Transparent)
                                .clickable { viewModel.selectAnalyticsDate(day.date, userId, userEmail) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${day.dayLabel}, ${day.dateFormatted}",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (day.isSelected) VitalTealDark else MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Split: ${day.splitsTrained}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (day.volumeKg > 0f) "${day.volumeKg.toInt()} kg" else "–",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (day.volumeKg > 0f) VitalPrimaryMint else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${day.caloriesIn} kcal • ${day.proteinG.toInt()}g P",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun AnalyticsMetricCard(
    title: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                fontSize = 11.sp,
                color = VitalPrimaryMint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun getExerciseEmoji(muscle: String): String {
    return when {
        muscle.contains("Chest", ignoreCase = true) -> "🏋️"
        muscle.contains("Back", ignoreCase = true) || muscle.contains("Lats", ignoreCase = true) -> "🚣"
        muscle.contains("Leg", ignoreCase = true) || muscle.contains("Quad", ignoreCase = true) || muscle.contains("Glute", ignoreCase = true) -> "🦵"
        muscle.contains("Bicep", ignoreCase = true) || muscle.contains("Arm", ignoreCase = true) -> "💪"
        muscle.contains("Tricep", ignoreCase = true) -> "⚡"
        muscle.contains("Shoulder", ignoreCase = true) || muscle.contains("Delt", ignoreCase = true) -> "🎯"
        muscle.contains("Cardio", ignoreCase = true) || muscle.contains("HIIT", ignoreCase = true) -> "🏃"
        muscle.contains("Abs", ignoreCase = true) || muscle.contains("Core", ignoreCase = true) -> "🧘"
        else -> "🔥"
    }
}
