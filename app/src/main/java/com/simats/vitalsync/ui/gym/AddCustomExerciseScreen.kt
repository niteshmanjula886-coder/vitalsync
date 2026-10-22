package com.simats.vitalsync.ui.gym

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme

@Composable
fun AddCustomExerciseScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaveCustomExerciseSuccess: (Exercise) -> Unit = {}
) {
    var exerciseNameInput by remember { mutableStateOf("") }
    var selectedMuscleGroup by remember { mutableStateOf("Arms") }
    var weightInput by remember { mutableStateOf("25") }
    var repsInput by remember { mutableStateOf("10") }
    var notesInput by remember { mutableStateOf("") }

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
                    text = "Add Custom Exercise",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Exercise Name Input
            Text(text = "Exercise Name", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = exerciseNameInput,
                onValueChange = { exerciseNameInput = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g. Preacher Curl, Cable Crossover, T-Bar Row", fontSize = 14.sp, color = Color(0xFF9CA3AF)) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF111827),
                    unfocusedTextColor = Color(0xFF111827),
                    focusedBorderColor = VitalTealPrimary,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Target Muscle Group Selector
            Text(text = "Target Body Part / Muscle", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Arms", "Chest", "Back", "Shoulders", "Legs", "Core").forEach { muscle ->
                    val isSelected = selectedMuscleGroup == muscle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) VitalTealDark else Color(0xFFF1F5F9))
                            .clickable { selectedMuscleGroup = muscle }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = muscle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFF475569)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Default Weight & Reps Fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Default Weight (kg)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color(0xFF111827),
                            unfocusedTextColor = Color(0xFF111827),
                            focusedBorderColor = VitalTealPrimary,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Target Reps", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = repsInput,
                        onValueChange = { repsInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color(0xFF111827),
                            unfocusedTextColor = Color(0xFF111827),
                            focusedBorderColor = VitalTealPrimary,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Notes Field
            Text(text = "Notes / Form Cue (Optional)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Squeeze at top contraction for 1 sec", fontSize = 14.sp, color = Color(0xFF9CA3AF)) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF111827),
                    unfocusedTextColor = Color(0xFF111827),
                    focusedBorderColor = VitalTealPrimary,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                )
            )

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Save Custom Exercise Button
        Button(
            onClick = {
                val nameToUse = exerciseNameInput.ifBlank { "Custom Exercise" }
                val customEx = Exercise(
                    id = "custom_ex_" + System.currentTimeMillis(),
                    name = nameToUse,
                    targetMuscle = selectedMuscleGroup,
                    defaultWeightKg = weightInput.toFloatOrNull() ?: 20f,
                    defaultReps = repsInput.toIntOrNull() ?: 10,
                    notes = notesInput
                )
                onSaveCustomExerciseSuccess(customEx)
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
                text = "Save Custom Exercise",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
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
fun AddCustomExerciseScreenPreview() {
    VitalsyncTheme {
        AddCustomExerciseScreen()
    }
}
