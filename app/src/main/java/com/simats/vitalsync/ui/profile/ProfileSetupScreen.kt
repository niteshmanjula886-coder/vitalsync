package com.simats.vitalsync.ui.profile

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
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

data class HealthGoal(
    val id: String,
    val title: String,
    val iconType: GoalIconType
)

enum class GoalIconType {
    FITNESS,
    SLEEP,
    WEIGHT,
    WELLNESS
}

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
    onSaveAndContinue: () -> Unit = {}
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
            .background(Color(0xFFFAFCFB))
            .statusBarsPadding()
            .navigationBarsPadding()
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
                        color = Color(0xFF111827)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Profile setup",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Title
            Text(
                text = "Personalize Your Experience",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "A few details help us tailor your insights.",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Name Field
            Text(
                text = "Name",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151)
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = VitalTealPrimary,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedTextColor = Color(0xFF111827),
                    unfocusedTextColor = Color(0xFF111827)
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Age & Gender Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Age Field
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Age",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = VitalTealPrimary,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                }

                // Gender Dropdown Field
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Gender",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isGenderDropdownExpanded = true },
                            enabled = false,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledContainerColor = Color.White,
                                disabledBorderColor = Color(0xFFE5E7EB),
                                disabledTextColor = Color(0xFF111827)
                            ),
                            trailingIcon = {
                                DropdownArrowIcon(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { isGenderDropdownExpanded = true }
                                )
                            }
                        )

                        DropdownMenu(
                            expanded = isGenderDropdownExpanded,
                            onDismissRequest = { isGenderDropdownExpanded = false }
                        ) {
                            genderOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
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
                // Height Field
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Height",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = VitalTealPrimary,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                }

                // Weight Field
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Weight",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = VitalTealPrimary,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Activity Level Field
            Text(
                text = "Activity Level",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box {
                OutlinedTextField(
                    value = activityLevel,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isActivityDropdownExpanded = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledContainerColor = Color.White,
                        disabledBorderColor = Color(0xFFE5E7EB),
                        disabledTextColor = Color(0xFF111827)
                    ),
                    trailingIcon = {
                        DropdownArrowIcon(
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { isActivityDropdownExpanded = true }
                        )
                    }
                )

                DropdownMenu(
                    expanded = isActivityDropdownExpanded,
                    onDismissRequest = { isActivityDropdownExpanded = false }
                ) {
                    activityOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                activityLevel = option
                                isActivityDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // What are your health goals? Section
            Text(
                text = "What are your health goals?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Goal Options Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                sampleHealthGoals.forEach { goal ->
                    val isSelected = selectedGoals.contains(goal.id)
                    GoalOptionCard(
                        goal = goal,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelected) {
                                selectedGoals.remove(goal.id)
                            } else {
                                selectedGoals.add(goal.id)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Save & Continue Button
            Button(
                onClick = onSaveAndContinue,
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
                    text = "Save & Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun GoalOptionCard(
    goal: HealthGoal,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) Color(0xFFE6F4EA) else Color.White
            )
            .border(
                width = 1.dp,
                color = if (isSelected) VitalTealPrimary else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoalIcon(
                    iconType = goal.iconType,
                    modifier = Modifier.size(20.dp),
                    color = if (isSelected) VitalTealDark else Color(0xFF4B5563)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = goal.title,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = Color(0xFF1F2937)
                )
            }

            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) VitalTealPrimary else Color.Transparent)
                    .border(
                        width = if (isSelected) 0.dp else 1.5.dp,
                        color = if (isSelected) Color.Transparent else Color(0xFF9CA3AF),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    CheckmarkIcon(
                        modifier = Modifier.size(12.dp),
                        color = Color.White
                    )
                }
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
                // Running / Activity Dumbbell Icon
                drawCircle(color = color, radius = w * 0.18f, center = Offset(w * 0.25f, h * 0.25f))
                drawCircle(color = color, radius = w * 0.18f, center = Offset(w * 0.75f, h * 0.75f))
                drawLine(
                    color = color,
                    start = Offset(w * 0.32f, h * 0.32f),
                    end = Offset(w * 0.68f, h * 0.68f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            GoalIconType.SLEEP -> {
                // Crescent Moon Icon
                val moonPath = Path().apply {
                    moveTo(w * 0.75f, h * 0.15f)
                    cubicTo(w * 0.25f, h * 0.25f, w * 0.25f, h * 0.75f, w * 0.75f, h * 0.85f)
                    cubicTo(w * 0.45f, h * 0.75f, w * 0.45f, h * 0.25f, w * 0.75f, h * 0.15f)
                    close()
                }
                drawPath(
                    path = moonPath,
                    color = color,
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            GoalIconType.WEIGHT -> {
                // Scales / Balance Icon
                drawLine(
                    color = color,
                    start = Offset(w * 0.15f, h * 0.30f),
                    end = Offset(w * 0.85f, h * 0.30f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(w * 0.50f, h * 0.30f),
                    end = Offset(w * 0.50f, h * 0.80f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(w * 0.30f, h * 0.80f),
                    end = Offset(w * 0.70f, h * 0.80f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            GoalIconType.WELLNESS -> {
                // Heart Outline Icon
                val heartPath = Path().apply {
                    moveTo(w * 0.5f, h * 0.82f)
                    cubicTo(
                        w * 0.15f, h * 0.58f,
                        w * 0.02f, h * 0.30f,
                        w * 0.25f, h * 0.14f
                    )
                    cubicTo(
                        w * 0.38f, h * 0.06f,
                        w * 0.48f, h * 0.16f,
                        w * 0.50f, h * 0.24f
                    )
                    cubicTo(
                        w * 0.52f, h * 0.16f,
                        w * 0.62f, h * 0.06f,
                        w * 0.75f, h * 0.14f
                    )
                    cubicTo(
                        w * 0.98f, h * 0.30f,
                        w * 0.85f, h * 0.58f,
                        w * 0.5f, h * 0.82f
                    )
                    close()
                }
                drawPath(
                    path = heartPath,
                    color = color,
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}

@Composable
private fun DropdownArrowIcon(modifier: Modifier = Modifier) {
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
            color = Color(0xFF6B7280),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
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

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun CheckmarkIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.5f)
            lineTo(w * 0.45f, h * 0.75f)
            lineTo(w * 0.8f, h * 0.25f)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
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
