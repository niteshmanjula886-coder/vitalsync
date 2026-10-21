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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import java.util.Locale

@Composable
fun FoodDetailsScreen(
    modifier: Modifier = Modifier,
    foodItem: FoodItem = FoodItem("f1", "Whole Egg (Boiled)", FoodCategory.EGG, 1f, "large egg (50g)", 78, 6.3f, 0.6f, 5.3f),
    initialMealCategory: MealCategory = MealCategory.BREAKFAST,
    dateString: String = "2025-10-15",
    onBackClick: () -> Unit = {},
    onLogMealSuccess: (MealEntry) -> Unit = {}
) {
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
                    text = "Food Details & Serving",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Food Name & Category Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = foodItem.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE8F6F0))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = foodItem.category.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F9F7A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Standard serving unit: ${foodItem.servingUnit}",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quantity Incrementor Box Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F6F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "SERVING QUANTITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F9F7A),
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { if (quantityMultiplier > 0.5f) quantityMultiplier -= 0.5f },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "−", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", quantityMultiplier)}x",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = foodItem.servingUnit,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { quantityMultiplier += 0.5f },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Calculated Nutrition Values
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Calculated Nutrition Summary",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Calories", fontSize = 14.sp, color = Color(0xFF6B7280))
                        Text(text = "$calculatedCalories kcal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Protein", fontSize = 14.sp, color = Color(0xFF6B7280))
                        Text(text = String.format("%.1fg", calculatedProtein), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A))
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Carbohydrates", fontSize = 14.sp, color = Color(0xFF6B7280))
                        Text(text = String.format("%.1fg", calculatedCarbs), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Fat", fontSize = 14.sp, color = Color(0xFF6B7280))
                        Text(text = String.format("%.1fg", calculatedFat), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Meal Category Target Selector
            Text(
                text = "Log to Meal Category",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MealCategory.entries.forEach { category ->
                    val isCatSelected = selectedMealCategory == category
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCatSelected) VitalTealDark else Color(0xFFF1F5F9))
                            .clickable { selectedMealCategory = category }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${category.icon} ${category.displayName}",
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
        Button(
            onClick = {
                val entry = MealEntry(
                    id = System.currentTimeMillis().toString(),
                    category = selectedMealCategory,
                    foodItem = foodItem,
                    quantity = quantityMultiplier,
                    calculatedCalories = calculatedCalories,
                    calculatedProtein = calculatedProtein,
                    calculatedCarbs = calculatedCarbs,
                    calculatedFat = calculatedFat,
                    dateString = dateString
                )
                onLogMealSuccess(entry)
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
                text = "Log Meal Entry",
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
fun FoodDetailsScreenPreview() {
    VitalsyncTheme {
        FoodDetailsScreen()
    }
}
