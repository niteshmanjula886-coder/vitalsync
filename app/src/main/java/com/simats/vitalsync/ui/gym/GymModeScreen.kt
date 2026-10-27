package com.simats.vitalsync.ui.gym

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.data.Exercise
import com.simats.vitalsync.data.WorkoutExercise
import com.simats.vitalsync.data.WorkoutSet
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme

// Predefined Varied Routines Library
private val pushExercisesList = listOf(
    Exercise("p1", "Barbell Bench Press", "Chest", 60f, 10),
    Exercise("p2", "Incline Dumbbell Press", "Upper Chest", 24f, 10),
    Exercise("p3", "Machine Chest Press", "Chest", 50f, 12),
    Exercise("p4", "Cable Chest Fly", "Inner Chest", 15f, 15),
    Exercise("p5", "Seated Shoulder Press", "Front Delts", 35f, 10),
    Exercise("p6", "Dumbbell Lateral Raise", "Side Delts", 10f, 15),
    Exercise("p7", "Cable Triceps Pushdown", "Triceps", 22.5f, 15),
    Exercise("p8", "Overhead Triceps Extension", "Long Head Triceps", 18f, 12)
)

private val pullExercisesList = listOf(
    Exercise("pl1", "Lat Pulldown", "Lats", 55f, 12),
    Exercise("pl2", "Seated Cable Row", "Middle Back", 50f, 12),
    Exercise("pl3", "Chest-Supported DB Row", "Upper Back", 20f, 10),
    Exercise("pl4", "Assisted Pull-Up", "Lats & Back", 0f, 8),
    Exercise("pl5", "Face Pull", "Rear Delts", 17.5f, 15),
    Exercise("pl6", "Reverse Pec Deck Fly", "Rear Delts", 30f, 15),
    Exercise("pl7", "Barbell Biceps Curl", "Biceps", 25f, 12),
    Exercise("pl8", "Hammer Curl", "Brachialis", 14f, 12),
    Exercise("pl9", "Incline Dumbbell Curl", "Biceps Long Head", 12f, 10)
)

private val legsExercisesList = listOf(
    Exercise("lg1", "Barbell Squat", "Quads & Glutes", 80f, 8),
    Exercise("lg2", "Leg Press", "Quads", 120f, 10),
    Exercise("lg3", "Romanian Deadlift", "Hamstrings & Glutes", 70f, 10),
    Exercise("lg4", "Leg Extension", "Quads", 40f, 15),
    Exercise("lg5", "Lying Leg Curl", "Hamstrings", 35f, 12),
    Exercise("lg6", "Walking Lunges", "Glutes & Quads", 16f, 12),
    Exercise("lg7", "Standing Calf Raise", "Calves", 50f, 15),
    Exercise("lg8", "Seated Calf Raise", "Soleus", 35f, 15)
)

private val hiitExercisesList = listOf(
    Exercise("h1", "Jumping Jacks", "Cardio / Full Body", 0f, 45),
    Exercise("h2", "Burpees", "Full Body Power", 0f, 15),
    Exercise("h3", "Mountain Climbers", "Core & Stamina", 0f, 30),
    Exercise("h4", "High Knees", "Cardio & Legs", 0f, 45),
    Exercise("h5", "Kettlebell Swings", "Posterior Chain", 16f, 20),
    Exercise("h6", "Battle Ropes", "Shoulders & Cardio", 0f, 30),
    Exercise("h7", "Box Jumps", "Leg Power", 0f, 12),
    Exercise("h8", "Sprint Intervals", "HIIT Stamina", 0f, 100)
)

@Composable
fun GymModeScreen(
    modifier: Modifier = Modifier,
    onStartWorkoutSplitClick: () -> Unit = {},
    onOpenNutritionTrackerClick: () -> Unit = {},
    onOpenProgressAnalyticsClick: () -> Unit = {},
    onViewWorkoutHistoryClick: () -> Unit = {},
    onFinishGymSession: () -> Unit = {}
) {
    var activeSegment by remember { mutableIntStateOf(0) } // 0: Workout, 1: Nutrition, 2: Analytics
    var selectedSplitName by remember { mutableStateOf("Push") }
    var restSecondsLeft by remember { mutableIntStateOf(45) }

    // Dynamically load exercises depending on split
    fun getExercisesForSplit(splitName: String): List<Exercise> {
        return when (splitName) {
            "Push" -> pushExercisesList
            "Pull" -> pullExercisesList
            "Legs" -> legsExercisesList
            "HIIT" -> hiitExercisesList
            else -> pushExercisesList
        }
    }

    val currentExercises = remember(selectedSplitName) {
        mutableStateListOf<WorkoutExercise>().apply {
            addAll(getExercisesForSplit(selectedSplitName).map { ex ->
                WorkoutExercise(
                    exercise = ex,
                    sets = listOf(
                        WorkoutSet(1, ex.defaultReps, ex.defaultWeightKg, true),
                        WorkoutSet(2, ex.defaultReps, ex.defaultWeightKg, false),
                        WorkoutSet(3, ex.defaultReps, ex.defaultWeightKg, false)
                    )
                )
            })
        }
    }

    val totalVolumeKg = currentExercises.sumOf { we ->
        we.sets.filter { it.isCompleted }.sumOf { (it.weightKg * it.reps).toDouble() }
    }.toFloat()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Bar
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
                    text = "Live workout assistant & diet tracker",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Active",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Top Segment Tab Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp)
        ) {
            listOf("🏋️ Workouts", "🥗 Nutrition", "📈 Analytics").forEachIndexed { index, label ->
                val isSelected = activeSegment == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) VitalTealDark else Color.Transparent)
                        .clickable {
                            activeSegment = index
                            when (index) {
                                1 -> onOpenNutritionTrackerClick()
                                2 -> onOpenProgressAnalyticsClick()
                            }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Navigation Shortcut Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onStartWorkoutSplitClick() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "⚡ Routine Splits", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Push, Pull, Legs, HIIT", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onViewWorkoutHistoryClick() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "📜 Workout History", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Logs & Templates", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hero Active Session Card (Dark Teal Gradient)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
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
                            text = "CURRENT SESSION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA7F3D0),
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$selectedSplitName Routine",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Rest Timer Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x30FFFFFF))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "REST TIMER", fontSize = 9.sp, color = Color(0xFFA7F3D0), fontWeight = FontWeight.Bold)
                            Text(text = "00:$restSecondsLeft", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Volume", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                        Text(text = "${totalVolumeKg.toInt()} kg", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Column {
                        Text(text = "Exercises", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                        Text(text = "${currentExercises.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Column {
                        Text(text = "Est. Burn", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                        Text(text = "${(totalVolumeKg * 0.05f + 180).toInt()} kcal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Horizontally Scrollable Routine Split Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Routine split",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "${currentExercises.size} exercises",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontally Scrollable Split Chips List
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Push", "Pull", "Legs", "HIIT", "+ Custom Split").forEach { split ->
                val isSelected = selectedSplitName == split
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) VitalTealDark else MaterialTheme.colorScheme.surface)
                        .clickable {
                            if (split == "+ Custom Split") {
                                onStartWorkoutSplitClick()
                            } else {
                                selectedSplitName = split
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = split,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Display Corresponding Exercises for Selected Split
        currentExercises.forEachIndexed { exIndex, we ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
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

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondary)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${we.sets.size} sets",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    we.sets.forEachIndexed { setIndex, setItem ->
                        if (setIndex > 0) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 6.dp))
                        }
                        SetRow(
                            setNum = "${setItem.setNumber}",
                            reps = "${setItem.reps} reps",
                            weight = if (setItem.weightKg > 0) "${setItem.weightKg} kg" else "Bodyweight",
                            isDone = setItem.isCompleted,
                            onToggleDone = { isDone ->
                                val updatedSets = we.sets.toMutableList()
                                updatedSets[setIndex] = setItem.copy(isCompleted = isDone)
                                currentExercises[exIndex] = we.copy(sets = updatedSets)
                                if (isDone) restSecondsLeft = 60
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons Row (Log Set | Finish Session)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { restSecondsLeft = 60 },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VitalTealDark,
                    contentColor = Color.White
                )
            ) {
                Text(text = "✓ Log Set & Rest", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onFinishGymSession,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VitalTealDark)
            ) {
                Text(text = "Finish Session", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

private fun getExerciseEmoji(muscle: String): String {
    return when {
        muscle.contains("Chest", ignoreCase = true) -> "🏋️"
        muscle.contains("Back", ignoreCase = true) -> "💪"
        muscle.contains("Leg", ignoreCase = true) || muscle.contains("Quad", ignoreCase = true) -> "🦵"
        muscle.contains("Delt", ignoreCase = true) || muscle.contains("Shoulder", ignoreCase = true) -> "👕"
        muscle.contains("Cardio", ignoreCase = true) || muscle.contains("HIIT", ignoreCase = true) -> "🏃"
        else -> "⚡"
    }
}

@Composable
private fun SetRow(
    setNum: String,
    reps: String,
    weight: String,
    isDone: Boolean,
    onToggleDone: (Boolean) -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Set $setNum", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = reps, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = weight, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isDone) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface)
                .clickable { onToggleDone(!isDone) }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isDone) "✓ Completed" else "Mark Done",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun GymModeScreenPreview() {
    VitalsyncTheme {
        GymModeScreen()
    }
}
