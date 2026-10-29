package com.simats.vitalsync.ui.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme

enum class GoalIconType {
    FITNESS,
    SLEEP,
    WEIGHT,
    WELLNESS
}

data class HealthGoal(
    val id: String,
    val title: String,
    val iconType: GoalIconType
)

val sampleHealthGoals = listOf(
    HealthGoal("1", "Fitness", GoalIconType.FITNESS),
    HealthGoal("2", "Better Sleep", GoalIconType.SLEEP),
    HealthGoal("3", "Weight Management", GoalIconType.WEIGHT),
    HealthGoal("4", "General Wellness", GoalIconType.WELLNESS)
)

@Composable
fun ProfileSetupScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaveAndContinue: (name: String, age: String, height: String, weight: String) -> Unit = { _, _, _, _ -> }
) {
    var name by remember { mutableStateOf("Alex Morgan") }
    var age by remember { mutableStateOf("28 years") }
    var gender by remember { mutableStateOf("Male") }
    var height by remember { mutableStateOf("175 cm") }
    var weight by remember { mutableStateOf("68.5 kg") }
    var activityLevel by remember { mutableStateOf("Moderately active") }

    var isGenderDropdownExpanded by remember { mutableStateOf(false) }
    var isActivityDropdownExpanded by remember { mutableStateOf(false) }

    val selectedGoals = remember { mutableStateListOf("1", "4") }

    val genderOptions = listOf("Male", "Female", "Non-binary", "Prefer not to say")
    val activityOptions = listOf("Sedentary", "Lightly active", "Moderately active", "Very active")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Bar
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
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Complete Your Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Subtitle
            Text(
                text = "We'll use this data to calibrate your health baselines and daily recommendations.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Full Name Input
            Text(text = "Full Name", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = VitalTealPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Age & Gender Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Age", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = VitalTealPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Gender", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isGenderDropdownExpanded = true },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = VitalTealPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                            ),
                            trailingIcon = {
                                ChevronDownIcon(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )

                        DropdownMenu(
                            expanded = isGenderDropdownExpanded,
                            onDismissRequest = { isGenderDropdownExpanded = false }
                        ) {
                            genderOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(text = option) },
                                    onClick = {
                                        gender = option
                                        isGenderDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Height & Weight Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Height", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = VitalTealPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Weight", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = VitalTealPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Activity Level Dropdown
            Text(text = "Daily Activity Level", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = activityLevel,
                    onValueChange = { },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isActivityDropdownExpanded = true },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = VitalTealPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    ),
                    trailingIcon = {
                        ChevronDownIcon(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                DropdownMenu(
                    expanded = isActivityDropdownExpanded,
                    onDismissRequest = { isActivityDropdownExpanded = false }
                ) {
                    activityOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option) },
                            onClick = {
                                activityLevel = option
                                isActivityDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Health Goals Grid Selection
            Text(
                text = "Primary Health Goals",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                sampleHealthGoals.chunked(2).forEach { rowGoals ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowGoals.forEach { goal ->
                            val isSelected = selectedGoals.contains(goal.id)
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (isSelected) {
                                            selectedGoals.remove(goal.id)
                                        } else {
                                            selectedGoals.add(goal.id)
                                        }
                                    },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFE6F4EA) else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) VitalTealPrimary else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    GoalIcon(
                                        iconType = goal.iconType,
                                        modifier = Modifier.size(20.dp),
                                        color = if (isSelected) VitalTealPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = goal.title,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) VitalTealDark else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Continue Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Button(
                onClick = { onSaveAndContinue(name, age, height, weight) },
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
                    text = "Save & Go to Dashboard",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GoalIcon(
    iconType: GoalIconType,
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (iconType) {
            GoalIconType.FITNESS -> {
                val bar = Path().apply {
                    moveTo(w * 0.15f, h * 0.5f)
                    lineTo(w * 0.85f, h * 0.5f)
                }
                drawPath(path = bar, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                drawCircle(color = color, radius = w * 0.15f, center = Offset(w * 0.20f, h * 0.5f))
                drawCircle(color = color, radius = w * 0.15f, center = Offset(w * 0.80f, h * 0.5f))
            }
            GoalIconType.SLEEP -> {
                val moonPath = Path().apply {
                    moveTo(w * 0.75f, h * 0.15f)
                    cubicTo(w * 0.25f, h * 0.25f, w * 0.25f, h * 0.75f, w * 0.75f, h * 0.85f)
                    cubicTo(w * 0.45f, h * 0.75f, w * 0.45f, h * 0.25f, w * 0.75f, h * 0.15f)
                    close()
                }
                drawPath(path = moonPath, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            GoalIconType.WEIGHT -> {
                val scalePath = Path().apply {
                    moveTo(w * 0.2f, h * 0.2f)
                    lineTo(w * 0.8f, h * 0.2f)
                    lineTo(w * 0.85f, h * 0.85f)
                    lineTo(w * 0.15f, h * 0.85f)
                    close()
                }
                drawPath(path = scalePath, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            GoalIconType.WELLNESS -> {
                val heartPath = Path().apply {
                    moveTo(w * 0.5f, h * 0.82f)
                    cubicTo(w * 0.15f, h * 0.58f, w * 0.02f, h * 0.30f, w * 0.25f, h * 0.14f)
                    cubicTo(w * 0.38f, h * 0.06f, w * 0.48f, h * 0.16f, w * 0.50f, h * 0.24f)
                    cubicTo(w * 0.52f, h * 0.16f, w * 0.62f, h * 0.06f, w * 0.75f, h * 0.14f)
                    cubicTo(w * 0.98f, h * 0.30f, w * 0.85f, h * 0.58f, w * 0.5f, h * 0.82f)
                    close()
                }
                drawPath(path = heartPath, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
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

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun ChevronDownIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.35f)
            lineTo(w * 0.5f, h * 0.65f)
            lineTo(w * 0.8f, h * 0.35f)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun ProfileSetupScreenPreview() {
    VitalsyncTheme {
        ProfileSetupScreen()
    }
}
