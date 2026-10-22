package com.simats.vitalsync.ui.gym

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.data.Exercise
import com.simats.vitalsync.data.WorkoutExercise
import com.simats.vitalsync.data.WorkoutSession
import com.simats.vitalsync.data.WorkoutSet
import com.simats.vitalsync.data.WorkoutSplit
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun ActiveWorkoutSessionScreen(
    modifier: Modifier = Modifier,
    split: WorkoutSplit = WorkoutSplit.PUSH,
    initialExercises: List<Exercise> = emptyList(),
    onBackClick: () -> Unit = {},
    onAddExerciseClick: () -> Unit = {},
    onFinishSession: (WorkoutSession) -> Unit = {}
) {
    var workoutName by remember { mutableStateOf("${split.displayName} Workout") }

    // Live Executable Workout Timer (Count Up & Play/Pause Controls)
    var sessionElapsedSeconds by remember { mutableIntStateOf(0) }
    var isSessionTimerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isSessionTimerRunning) {
        while (isSessionTimerRunning) {
            delay(1000L)
            sessionElapsedSeconds++
        }
    }

    val timerMinutes = sessionElapsedSeconds / 60
    val timerSecs = sessionElapsedSeconds % 60
    val formattedWorkoutTimer = String.format(Locale.US, "%02d:%02d", timerMinutes, timerSecs)

    // Customizable Rest Countdown Timer State (Strictly manual Play/Pause, no auto-start)
    var restTimerConfigSeconds by remember { mutableIntStateOf(60) } // Default 60s configurable
    var restTimerSeconds by remember { mutableIntStateOf(60) }
    var isRestTimerActive by remember { mutableStateOf(false) }

    var showCustomRestDialog by remember { mutableStateOf(false) }
    var customRestInputText by remember { mutableStateOf("45") }

    LaunchedEffect(isRestTimerActive, restTimerSeconds) {
        if (isRestTimerActive && restTimerSeconds > 0) {
            delay(1000L)
            restTimerSeconds--
            if (restTimerSeconds == 0) {
                isRestTimerActive = false
            }
        }
    }

    // Exercises with mutable sets state
    val activeExercises = remember {
        mutableStateListOf<WorkoutExercise>().apply {
            val listToLoad = if (initialExercises.isNotEmpty()) initialExercises else listOf(
                Exercise("e1", "Barbell Bench Press", "Chest", 60f, 10),
                Exercise("e2", "Incline Dumbbell Press", "Upper Chest", 22f, 10),
                Exercise("e6", "Lateral Raises", "Side Deltoids", 10f, 15)
            )
            addAll(listToLoad.map { ex ->
                WorkoutExercise(
                    exercise = ex,
                    sets = listOf(
                        WorkoutSet(1, ex.defaultReps, ex.defaultWeightKg, false),
                        WorkoutSet(2, ex.defaultReps, ex.defaultWeightKg, false),
                        WorkoutSet(3, ex.defaultReps, ex.defaultWeightKg, false)
                    )
                )
            })
        }
    }

    // Calculated total volume
    val totalVolumeKg = activeExercises.sumOf { we ->
        we.sets.filter { it.isCompleted }.sumOf { (it.weightKg * it.reps).toDouble() }
    }.toFloat()

    val totalSetsCompleted = activeExercises.sumOf { we -> we.sets.count { it.isCompleted } }

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
                        text = "Active Session",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                // Play / Pause Session Timer Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSessionTimerRunning) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                        .clickable { isSessionTimerRunning = !isSessionTimerRunning }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isSessionTimerRunning) "⏸" else "▶",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = if (isSessionTimerRunning) "⏱️ $formattedWorkoutTimer" else "Paused ($formattedWorkoutTimer)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSessionTimerRunning) Color(0xFF15803D) else Color(0xFFD97706)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Workout Name Title Input
            OutlinedTextField(
                value = workoutName,
                onValueChange = { workoutName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = VitalTealPrimary,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedTextColor = Color(0xFF111827)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Workout Live Stats Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = VitalTealDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "VOLUME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA7F3D0))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "${totalVolumeKg.toInt()} kg", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "SETS COMPLETED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA7F3D0))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "$totalSetsCompleted", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "REST TIMER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA7F3D0))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (restTimerSeconds > 0) "${restTimerSeconds}s" else "Ready!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Customizable Rest & Recover Controls Card with Strict Play/Pause Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F6F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⏱️ Rest & Recover", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                            Spacer(modifier = Modifier.width(8.dp))

                            // Strict Manual Play / Pause Rest Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isRestTimerActive) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                                    .clickable {
                                        if (!isRestTimerActive && restTimerSeconds == 0) {
                                            restTimerSeconds = restTimerConfigSeconds
                                        }
                                        isRestTimerActive = !isRestTimerActive
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isRestTimerActive) "⏸ Pause" else "▶ Play",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRestTimerActive) Color(0xFF15803D) else Color(0xFFD97706)
                                )
                            }
                        }

                        Text(
                            text = "Custom ✏️",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F9F7A),
                            modifier = Modifier.clickable {
                                customRestInputText = restTimerConfigSeconds.toString()
                                showCustomRestDialog = true
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(30, 45, 60, 90, 120).forEach { sec ->
                            val isSecSelected = restTimerConfigSeconds == sec
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSecSelected) VitalTealPrimary else Color.White)
                                    .clickable {
                                        restTimerConfigSeconds = sec
                                        restTimerSeconds = sec
                                        isRestTimerActive = false // Selected duration preset; tap Play to start
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${sec}s",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSecSelected) Color.White else Color(0xFF374151)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Exercise Set Tracking Cards List
            activeExercises.forEachIndexed { exIndex, we ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
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
                            Column {
                                Text(
                                    text = we.exercise.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                Text(
                                    text = we.exercise.targetMuscle,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }

                            Text(
                                text = "Remove",
                                fontSize = 11.sp,
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    activeExercises.removeAt(exIndex)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "SET", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF), modifier = Modifier.width(36.dp))
                            Text(text = "KG", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF), modifier = Modifier.width(70.dp))
                            Text(text = "REPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF), modifier = Modifier.width(70.dp))
                            Text(text = "DONE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 8.dp))

                        we.sets.forEachIndexed { setIndex, setItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${setItem.setNumber}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827),
                                    modifier = Modifier.width(36.dp)
                                )

                                // Editable Weight
                                var weightInput by remember(setItem.weightKg) { mutableStateOf("${setItem.weightKg}") }
                                OutlinedTextField(
                                    value = weightInput,
                                    onValueChange = {
                                        weightInput = it
                                        val newWeight = it.toFloatOrNull() ?: setItem.weightKg
                                        val updatedSets = we.sets.toMutableList()
                                        updatedSets[setIndex] = setItem.copy(weightKg = newWeight)
                                        activeExercises[exIndex] = we.copy(sets = updatedSets)
                                    },
                                    modifier = Modifier.width(70.dp).height(48.dp),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedTextColor = Color(0xFF111827),
                                        unfocusedTextColor = Color(0xFF111827),
                                        focusedBorderColor = VitalTealPrimary
                                    )
                                )

                                // Editable Reps
                                var repsInput by remember(setItem.reps) { mutableStateOf("${setItem.reps}") }
                                OutlinedTextField(
                                    value = repsInput,
                                    onValueChange = {
                                        repsInput = it
                                        val newReps = it.toIntOrNull() ?: setItem.reps
                                        val updatedSets = we.sets.toMutableList()
                                        updatedSets[setIndex] = setItem.copy(reps = newReps)
                                        activeExercises[exIndex] = we.copy(sets = updatedSets)
                                    },
                                    modifier = Modifier.width(70.dp).height(48.dp),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedTextColor = Color(0xFF111827),
                                        unfocusedTextColor = Color(0xFF111827),
                                        focusedBorderColor = VitalTealPrimary
                                    )
                                )

                                Checkbox(
                                    checked = setItem.isCompleted,
                                    onCheckedChange = { isChecked ->
                                        val updatedSets = we.sets.toMutableList()
                                        updatedSets[setIndex] = setItem.copy(isCompleted = isChecked)
                                        activeExercises[exIndex] = we.copy(sets = updatedSets)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = VitalTealPrimary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Add Set Button
                        Text(
                            text = "+ Add Set",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F9F7A),
                            modifier = Modifier
                                .clickable {
                                    val lastSet = we.sets.lastOrNull()
                                    val newSetNum = (lastSet?.setNumber ?: 0) + 1
                                    val newSets = we.sets + WorkoutSet(newSetNum, lastSet?.reps ?: 10, lastSet?.weightKg ?: 20f, false)
                                    activeExercises[exIndex] = we.copy(sets = newSets)
                                }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Add Exercise Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE8F6F0))
                    .clickable { onAddExerciseClick() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+ Add Exercise to Routine", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A))
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Finish Session Button
        Button(
            onClick = {
                val session = WorkoutSession(
                    id = System.currentTimeMillis().toString(),
                    name = workoutName,
                    split = split,
                    dateString = "2025-10-15",
                    timestampFormatted = "Oct 15, 2025 at 9:30 AM",
                    durationMinutes = (sessionElapsedSeconds / 60).coerceAtLeast(1),
                    exercises = activeExercises.toList(),
                    totalVolumeKg = totalVolumeKg,
                    estimatedCaloriesBurned = (totalVolumeKg * 0.05f + 150).toInt()
                )
                onFinishSession(session)
            },
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
                text = "Finish Workout Session",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Custom Rest Seconds Dialog
    if (showCustomRestDialog) {
        AlertDialog(
            onDismissRequest = { showCustomRestDialog = false },
            title = {
                Text(text = "Custom Rest Duration", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(text = "Set custom rest duration in seconds:", fontSize = 13.sp, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customRestInputText,
                        onValueChange = { customRestInputText = it.filter { c -> c.isDigit() } },
                        label = { Text("Rest Duration (seconds)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VitalTealPrimary,
                            focusedLabelColor = VitalTealPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sec = customRestInputText.toIntOrNull() ?: 60
                        if (sec > 0) {
                            restTimerConfigSeconds = sec
                            restTimerSeconds = sec
                            isRestTimerActive = false // Strict Play/Pause control
                        }
                        showCustomRestDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VitalTealDark)
                ) {
                    Text("Set Rest Timer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomRestDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            }
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
fun ActiveWorkoutSessionScreenPreview() {
    VitalsyncTheme {
        ActiveWorkoutSessionScreen()
    }
}
