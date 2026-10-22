package com.simats.vitalsync.ui.nutrition

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.data.NutritionGoal
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme

@Composable
fun NutritionSettingsScreen(
    modifier: Modifier = Modifier,
    initialGoal: NutritionGoal = NutritionGoal(),
    onBackClick: () -> Unit = {},
    onSaveGoalsSuccess: (NutritionGoal) -> Unit = {}
) {
    var ageInput by remember { mutableStateOf("${initialGoal.age}") }
    var weightInput by remember { mutableStateOf("${initialGoal.weightKg}") }
    var heightInput by remember { mutableStateOf("${initialGoal.heightCm}") }
    var calorieTargetInput by remember { mutableStateOf("${initialGoal.dailyCalorieTarget}") }
    var proteinTargetInput by remember { mutableStateOf("${initialGoal.dailyProteinTarget.toInt()}") }

    var selectedGoal by remember { mutableStateOf(initialGoal.fitnessGoal) }
    var selectedDietPref by remember { mutableStateOf(initialGoal.dietaryPreference) }

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
                    text = "Nutrition & Fitness Goals",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 1: User Metrics
            Text(text = "Body Metrics", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Age", fontSize = 12.sp, color = Color(0xFF374151))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { ageInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedTextColor = Color(0xFF111827),
    unfocusedTextColor = Color(0xFF111827),
    focusedBorderColor = VitalTealPrimary
)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Weight (kg)", fontSize = 12.sp, color = Color(0xFF374151))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedTextColor = Color(0xFF111827),
    unfocusedTextColor = Color(0xFF111827),
    focusedBorderColor = VitalTealPrimary
)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Height (cm)", fontSize = 12.sp, color = Color(0xFF374151))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedTextColor = Color(0xFF111827),
    unfocusedTextColor = Color(0xFF111827),
    focusedBorderColor = VitalTealPrimary
)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Targets
            Text(text = "Daily Targets", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Calorie Target (kcal)", fontSize = 12.sp, color = Color(0xFF374151))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = calorieTargetInput,
                        onValueChange = { calorieTargetInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedTextColor = Color(0xFF111827),
    unfocusedTextColor = Color(0xFF111827),
    focusedBorderColor = VitalTealPrimary
)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Protein Target (g)", fontSize = 12.sp, color = Color(0xFF374151))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = proteinTargetInput,
                        onValueChange = { proteinTargetInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VitalTealPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Save Goals Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Button(
                onClick = {
                    val updated = initialGoal.copy(
                        age = ageInput.toIntOrNull() ?: initialGoal.age,
                        weightKg = weightInput.toFloatOrNull() ?: initialGoal.weightKg,
                        heightCm = heightInput.toFloatOrNull() ?: initialGoal.heightCm,
                        dailyCalorieTarget = calorieTargetInput.toIntOrNull() ?: initialGoal.dailyCalorieTarget,
                        dailyProteinTarget = proteinTargetInput.toFloatOrNull() ?: initialGoal.dailyProteinTarget,
                        fitnessGoal = selectedGoal,
                        dietaryPreference = selectedDietPref
                    )
                    onSaveGoalsSuccess(updated)
                },
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
                    text = "Save Goals & Targets",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Target estimates are guidelines for daily nutrition tracking.",
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
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
fun NutritionSettingsScreenPreview() {
    VitalsyncTheme {
        NutritionSettingsScreen()
    }
}
