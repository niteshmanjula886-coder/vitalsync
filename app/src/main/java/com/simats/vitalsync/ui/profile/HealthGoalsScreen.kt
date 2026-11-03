package com.simats.vitalsync.ui.profile

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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme

enum class GoalCardIconType {
    MUSCLE,
    WEIGHT_LOSS,
    FITNESS,
    ENDURANCE
}

data class GoalItem(
    val id: String,
    val title: String,
    val description: String,
    val iconType: GoalCardIconType
)

@Composable
fun HealthGoalsScreen(
    modifier: Modifier = Modifier,
    initialGoalId: String = "3",
    currentGoalTitle: String = "Improve Fitness",
    onGoalSelected: (goalTitle: String) -> Unit = {},
    onBackClick: () -> Unit = {},
    onSaveGoalClick: (selectedGoalId: String, selectedGoalTitle: String) -> Unit = { _, _ -> }
) {
    val goalsList = remember {
        listOf(
            GoalItem("1", "Build Muscle", "Increase strength and lean muscle mass", GoalCardIconType.MUSCLE),
            GoalItem("2", "Weight Loss", "Burn fat and maintain a calorie deficit", GoalCardIconType.WEIGHT_LOSS),
            GoalItem("3", "Improve Fitness", "General health, endurance, and daily activity", GoalCardIconType.FITNESS),
            GoalItem("4", "Endurance", "Stamina and cardiovascular performance", GoalCardIconType.ENDURANCE)
        )
    }

    var selectedGoalTitle by remember(currentGoalTitle) {
        mutableStateOf(currentGoalTitle.ifBlank { "Improve Fitness" })
    }
    var selectedGoalId by remember(initialGoalId, currentGoalTitle) {
        val matched = goalsList.find { it.title.equals(currentGoalTitle, ignoreCase = true) }?.id ?: initialGoalId
        mutableStateOf(matched)
    }

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
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Health Goals",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "What would you like to focus on?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Choose your main goal. You can change it whenever your priorities shift.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Goals Selection List
            goalsList.forEach { goal ->
                val isSelected = selectedGoalTitle.equals(goal.title, ignoreCase = true)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedGoalTitle = goal.title
                            selectedGoalId = goal.id
                            // 1. Instantly update in-memory UI state so UI responds with zero delay
                            onGoalSelected(goal.title)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GoalIcon(
                            iconType = goal.iconType,
                            modifier = Modifier.size(26.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = goal.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = goal.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isSelected) {
                            CheckCircleIcon(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selected Goal Summary Notice
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎯", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Active Goal: $selectedGoalTitle. Your daily telemetry and profile targets adapt to this focus.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Save Goal Button
        Button(
            onClick = {
                onSaveGoalClick(selectedGoalId, selectedGoalTitle)
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
                text = "Save Goal",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GoalIcon(
    iconType: GoalCardIconType,
    modifier: Modifier = Modifier,
    tint: Color = VitalTealPrimary
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (iconType) {
            GoalCardIconType.MUSCLE -> {
                // Dumbbell / Barbell
                val barThickness = 3.dp.toPx()
                val capWidth = w * 0.18f
                val capHeight = h * 0.55f

                // Center bar
                drawLine(
                    color = tint,
                    start = Offset(capWidth, h * 0.5f),
                    end = Offset(w - capWidth, h * 0.5f),
                    strokeWidth = barThickness,
                    cap = StrokeCap.Round
                )
                // Left weight plate
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.08f, (h - capHeight) / 2f),
                    size = Size(capWidth, capHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                // Right weight plate
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w - w * 0.08f - capWidth, (h - capHeight) / 2f),
                    size = Size(capWidth, capHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
            GoalCardIconType.WEIGHT_LOSS -> {
                // Trending down chart arrow
                val path = Path().apply {
                    moveTo(w * 0.15f, h * 0.25f)
                    lineTo(w * 0.5f, h * 0.55f)
                    lineTo(w * 0.7f, h * 0.4f)
                    lineTo(w * 0.88f, h * 0.78f)
                    // Arrowhead
                    moveTo(w * 0.88f, h * 0.52f)
                    lineTo(w * 0.88f, h * 0.78f)
                    lineTo(w * 0.62f, h * 0.78f)
                }
                drawPath(
                    path = path,
                    color = tint,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            GoalCardIconType.FITNESS -> {
                // Runner / Activity icon
                drawCircle(
                    color = tint,
                    radius = w * 0.12f,
                    center = Offset(w * 0.55f, h * 0.2f)
                )
                val bodyPath = Path().apply {
                    // Torso
                    moveTo(w * 0.55f, h * 0.32f)
                    lineTo(w * 0.45f, h * 0.6f)
                    // Back leg
                    lineTo(w * 0.25f, h * 0.85f)
                    // Front leg
                    moveTo(w * 0.45f, h * 0.6f)
                    lineTo(w * 0.65f, h * 0.7f)
                    lineTo(w * 0.8f, h * 0.88f)
                    // Arms
                    moveTo(w * 0.3f, h * 0.45f)
                    lineTo(w * 0.52f, h * 0.4f)
                    lineTo(w * 0.75f, h * 0.48f)
                }
                drawPath(
                    path = bodyPath,
                    color = tint,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            GoalCardIconType.ENDURANCE -> {
                // Stopwatch / Timer
                val r = w * 0.38f
                val center = Offset(w * 0.5f, h * 0.55f)
                drawCircle(
                    color = tint,
                    radius = r,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )
                // Top button
                drawLine(
                    color = tint,
                    start = Offset(w * 0.5f, h * 0.17f),
                    end = Offset(w * 0.5f, h * 0.05f),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Top cap
                drawLine(
                    color = tint,
                    start = Offset(w * 0.42f, h * 0.05f),
                    end = Offset(w * 0.58f, h * 0.05f),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Clock hands
                drawLine(
                    color = tint,
                    start = center,
                    end = Offset(center.x, center.y - r * 0.55f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = center,
                    end = Offset(center.x + r * 0.45f, center.y),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun CheckCircleIcon(
    modifier: Modifier = Modifier,
    color: Color = VitalTealPrimary
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Outer filled circle
        drawCircle(
            color = color,
            radius = w * 0.48f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // White checkmark
        val checkPath = Path().apply {
            moveTo(w * 0.28f, h * 0.52f)
            lineTo(w * 0.44f, h * 0.68f)
            lineTo(w * 0.72f, h * 0.36f)
        }
        drawPath(
            path = checkPath,
            color = Color.White,
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
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
fun HealthGoalsScreenPreview() {
    VitalsyncTheme {
        HealthGoalsScreen()
    }
}
