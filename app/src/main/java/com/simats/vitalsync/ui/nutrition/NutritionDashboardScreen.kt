package com.simats.vitalsync.ui.nutrition

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.data.MealCategory
import com.simats.vitalsync.data.MealEntry
import com.simats.vitalsync.data.NutritionGoal
import com.simats.vitalsync.ui.nutrition.viewmodel.MealsUiState
import com.simats.vitalsync.ui.nutrition.viewmodel.NutritionGoalUiState
import com.simats.vitalsync.ui.nutrition.viewmodel.NutritionViewModel
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import kotlin.math.max

@Composable
fun NutritionDashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: NutritionViewModel? = null,
    userEmail: String = "alex.morgan@email.com",
    dateString: String = "Today · Oct 15",
    goal: NutritionGoal = NutritionGoal(),
    loggedMeals: List<MealEntry> = emptyList(),
    onBackClick: () -> Unit = {},
    onAddMealClick: (MealCategory) -> Unit = {},
    onDeleteMealEntry: (String) -> Unit = {},
    onViewMealHistoryClick: () -> Unit = {},
    onNutritionSettingsClick: () -> Unit = {}
) {
    val rawDate = remember(dateString) {
        val trimmed = if (dateString.contains("·")) dateString.substringAfter("·").trim() else dateString.trim()
        if (trimmed.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) trimmed else java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
    }

    val mealsState by viewModel?.mealsState?.collectAsState() ?: remember { mutableStateOf(MealsUiState()) }
    val goalState by viewModel?.goalState?.collectAsState() ?: remember { mutableStateOf(NutritionGoalUiState()) }
    var deletedMealIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(userEmail, rawDate) {
        viewModel?.loadMeals(userEmail, rawDate)
        viewModel?.loadNutritionGoal(userEmail)
    }

    val activeGoal = goalState.goal?.toNutritionGoal() ?: goal
    val hasLoadedFromBackend = mealsState.date.isNotBlank() || mealsState.errorMessage != null
    val baseMeals = if (viewModel != null && (hasLoadedFromBackend || mealsState.meals.isNotEmpty())) {
        mealsState.meals.map { it.toMealEntry() }
    } else {
        loggedMeals
    }
    val activeMeals = baseMeals.filter { it.id !in deletedMealIds }

    val totalCalories = activeMeals.sumOf { it.calculatedCalories }
    val totalProtein = activeMeals.sumOf { it.calculatedProtein.toDouble() }.toFloat()
    val totalCarbs = activeMeals.sumOf { it.calculatedCarbs.toDouble() }.toFloat()
    val totalFat = activeMeals.sumOf { it.calculatedFat.toDouble() }.toFloat()

    val caloriesRemaining = max(0, activeGoal.dailyCalorieTarget - totalCalories)
    val proteinRemaining = max(0f, activeGoal.dailyProteinTarget - totalProtein)

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
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    text = "Diet & Protein Tracker",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = onNutritionSettingsClick,
                modifier = Modifier.size(36.dp)
            ) {
                GearSettingsIcon(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Date Navigator Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onViewMealHistoryClick() }
                ) {
                    CalendarIcon(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = dateString, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }

                Text(
                    text = "Meal Logs →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onViewMealHistoryClick() }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Daily Calorie & Protein Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = VitalTealDark),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "DAILY NUTRITION PROGRESS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA7F3D0),
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Calories",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA7F3D0)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$totalCalories / ${activeGoal.dailyCalorieTarget}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "$caloriesRemaining kcal left",
                            fontSize = 11.sp,
                            color = Color(0xFFA7F3D0)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Protein",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA7F3D0)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${totalProtein.toInt()}g / ${activeGoal.dailyProteinTarget.toInt()}g",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${proteinRemaining.toInt()}g needed",
                            fontSize = 11.sp,
                            color = Color(0xFFA7F3D0)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Protein Progress Bar
                val proteinRatio = (totalProtein / activeGoal.dailyProteinTarget).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0x30FFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(proteinRatio)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFF38BDF8))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Macros Breakdown Cards (3 Columns)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MacroMiniCard("Protein", "${totalProtein.toInt()}g", "/ ${activeGoal.dailyProteinTarget.toInt()}g", Color(0xFF38BDF8), Modifier.weight(1f))
            MacroMiniCard("Carbs", "${totalCarbs.toInt()}g", "/ ${activeGoal.dailyCarbsTarget.toInt()}g", Color(0xFFF59E0B), Modifier.weight(1f))
            MacroMiniCard("Fat", "${totalFat.toInt()}g", "/ ${activeGoal.dailyFatTarget.toInt()}g", Color(0xFFEC4899), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Meal Categories Section
        Text(
            text = "Today's Meals",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        MealCategory.entries.forEach { category ->
            val categoryMeals = activeMeals.filter { it.category == category }
            val categoryCals = categoryMeals.sumOf { it.calculatedCalories }
            val categoryProtein = categoryMeals.sumOf { it.calculatedProtein.toDouble() }.toFloat()

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
                        Column {
                            Text(
                                text = category.displayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "$categoryCals kcal · ${categoryProtein.toInt()}g protein",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondary)
                                .clickable { onAddMealClick(category) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PlusAddIcon(modifier = Modifier.size(12.dp), color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Add Food",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (categoryMeals.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(10.dp))

                        categoryMeals.forEach { meal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${meal.foodItem.name} (${meal.quantity}x)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "${meal.calculatedCalories} kcal | ${meal.calculatedProtein}g P",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Professional Delete Button with Trash Bin Icon
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFEE2E2))
                                        .clickable {
                                            deletedMealIds = deletedMealIds + meal.id
                                            viewModel?.deleteMeal(userEmail, meal.id, rawDate)
                                            onDeleteMealEntry(meal.id)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        TrashBinIcon(modifier = Modifier.size(13.dp), color = Color(0xFFDC2626))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Delete",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDC2626)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun MacroMiniCard(
    title: String,
    value: String,
    targetStr: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(text = targetStr, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TrashBinIcon(modifier: Modifier = Modifier, color: Color = Color(0xFFDC2626)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val lidLine = Path().apply {
            moveTo(w * 0.15f, h * 0.28f)
            lineTo(w * 0.85f, h * 0.28f)
            moveTo(w * 0.38f, h * 0.28f)
            lineTo(w * 0.38f, h * 0.15f)
            lineTo(w * 0.62f, h * 0.15f)
            lineTo(w * 0.62f, h * 0.28f)
        }

        val binBody = Path().apply {
            moveTo(w * 0.22f, h * 0.28f)
            lineTo(w * 0.28f, h * 0.88f)
            lineTo(w * 0.72f, h * 0.88f)
            lineTo(w * 0.78f, h * 0.28f)
        }

        val innerLines = Path().apply {
            moveTo(w * 0.40f, h * 0.40f)
            lineTo(w * 0.40f, h * 0.76f)
            moveTo(w * 0.60f, h * 0.40f)
            lineTo(w * 0.60f, h * 0.76f)
        }

        drawPath(path = lidLine, color = color, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(path = binBody, color = color, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(path = innerLines, color = color, style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun PlusAddIcon(modifier: Modifier = Modifier, color: Color = Color(0xFF0F9F7A)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.5f, h * 0.85f)
            moveTo(w * 0.15f, h * 0.5f)
            lineTo(w * 0.85f, h * 0.5f)
        }

        drawPath(path = path, color = color, style = Stroke(width = 2.0.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun GearSettingsIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(color = color, radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = 1.8.dp.toPx()))
    }
}

@Composable
private fun CalendarIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val calPath = Path().apply {
            moveTo(0f, h * 0.25f)
            lineTo(w, h * 0.25f)
            lineTo(w, h * 0.85f)
            lineTo(0f, h * 0.85f)
            close()
        }

        drawPath(path = calPath, color = color, style = Stroke(width = 1.6.dp.toPx()))
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
fun NutritionDashboardScreenPreview() {
    VitalsyncTheme {
        NutritionDashboardScreen()
    }
}
