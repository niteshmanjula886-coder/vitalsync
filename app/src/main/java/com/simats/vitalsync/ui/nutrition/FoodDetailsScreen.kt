package com.simats.vitalsync.ui.nutrition

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.data.FoodCategory
import com.simats.vitalsync.data.FoodItem
import com.simats.vitalsync.data.MealCategory
import com.simats.vitalsync.data.MealEntry
import com.simats.vitalsync.ui.nutrition.viewmodel.LogMealUiState
import com.simats.vitalsync.ui.nutrition.viewmodel.NutritionViewModel
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import java.util.Locale

@Composable
fun FoodDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: NutritionViewModel? = null,
    userEmail: String = "alex.morgan@email.com",
    foodItem: FoodItem = FoodItem("f1", "Whole Egg (Boiled)", FoodCategory.EGG, 1f, "large egg (50g)", 78, 6.3f, 0.6f, 5.3f),
    initialMealCategory: MealCategory = MealCategory.BREAKFAST,
    dateString: String = "2025-10-15",
    onBackClick: () -> Unit = {},
    onLogMealSuccess: (MealEntry) -> Unit = {}
) {
    val logMealState by viewModel?.logMealState?.collectAsState() ?: remember { mutableStateOf(LogMealUiState()) }

    var quantityMultiplier by remember { mutableFloatStateOf(1.0f) }
    var selectedMealCategory by remember { mutableStateOf(initialMealCategory) }

    val calculatedCalories = (foodItem.calories * quantityMultiplier).toInt()
    val calculatedProtein = (foodItem.proteinGrams * quantityMultiplier)
    val calculatedCarbs = (foodItem.carbsGrams * quantityMultiplier)
    val calculatedFat = (foodItem.fatGrams * quantityMultiplier)

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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    text = "Nutrition Breakdown",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Food Title & Indicator Badge
            Text(
                text = foodItem.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (foodItem.category) {
                        FoodCategory.NON_VEG -> "🔴 Non-Vegetarian"
                        FoodCategory.EGG -> "🟡 Egg"
                        FoodCategory.VEGAN -> "🌱 Vegan"
                        else -> "🟢 Vegetarian"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B7280)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "· 1 serving = ${foodItem.servingUnit}",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Big Calories Hero Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = VitalTealDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL ENERGY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA7F3D0),
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "$calculatedCalories",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "kilocalories",
                        fontSize = 13.sp,
                        color = Color(0xFFA7F3D0)
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = Color(0x30FFFFFF))
                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 Macros in Hero
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Protein", fontSize = 11.sp, color = Color(0xFFA7F3D0))
                            Text(text = String.format(Locale.US, "%.1fg", calculatedProtein), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Carbs", fontSize = 11.sp, color = Color(0xFFA7F3D0))
                            Text(text = String.format(Locale.US, "%.1fg", calculatedCarbs), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Fat", fontSize = 11.sp, color = Color(0xFFA7F3D0))
                            Text(text = String.format(Locale.US, "%.1fg", calculatedFat), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Portion Multiplier Stepper Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Portion Size", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Text(text = "Adjust servings eaten", fontSize = 12.sp, color = Color(0xFF6B7280))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .clickable {
                                    if (quantityMultiplier > 0.5f) quantityMultiplier -= 0.5f
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = String.format(Locale.US, "%.1fx", quantityMultiplier),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .clickable {
                                    quantityMultiplier += 0.5f
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Target Meal Category Selector
            Text(
                text = "Log Under Category",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MealCategory.entries.forEach { cat ->
                    val isCatSelected = selectedMealCategory == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isCatSelected) VitalTealDark else Color(0xFFF1F5F9))
                            .clickable { selectedMealCategory = cat }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${cat.icon} ${cat.displayName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCatSelected) Color.White else Color(0xFF475569)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Log Meal Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            if (logMealState.errorMessage != null) {
                Text(
                    text = logMealState.errorMessage ?: "",
                    color = Color(0xFFEF4444),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    val rawDate = if (dateString.contains("·")) dateString.substringAfter("·").trim() else dateString.trim()
                    val finalDate = if (rawDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) rawDate else java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date())

                    if (viewModel != null) {
                        viewModel.logMeal(
                            email = userEmail,
                            foodId = foodItem.id,
                            category = selectedMealCategory.name,
                            quantity = quantityMultiplier,
                            calculatedCalories = calculatedCalories,
                            calculatedProtein = calculatedProtein,
                            calculatedCarbs = calculatedCarbs,
                            calculatedFat = calculatedFat,
                            dateString = finalDate,
                            onSuccess = { loggedEntry ->
                                onLogMealSuccess(loggedEntry)
                            }
                        )
                    } else {
                        val entry = MealEntry(
                            id = System.currentTimeMillis().toString(),
                            category = selectedMealCategory,
                            foodItem = foodItem,
                            quantity = quantityMultiplier,
                            calculatedCalories = calculatedCalories,
                            calculatedProtein = calculatedProtein,
                            calculatedCarbs = calculatedCarbs,
                            calculatedFat = calculatedFat,
                            dateString = finalDate
                        )
                        onLogMealSuccess(entry)
                    }
                },
                enabled = !logMealState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VitalTealDark,
                    contentColor = Color.White
                )
            ) {
                if (logMealState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Log Meal Entry",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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

        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun FoodDetailsScreenPreview() {
    VitalsyncTheme {
        FoodDetailsScreen()
    }
}
