package com.simats.vitalsync.ui.nutrition.viewmodel

import com.simats.vitalsync.data.model.nutrition.FoodItemDto
import com.simats.vitalsync.data.model.nutrition.MealEntryDto
import com.simats.vitalsync.data.model.nutrition.MealTotalsDto
import com.simats.vitalsync.data.model.nutrition.NutritionGoalDto

data class FoodsUiState(
    val isLoading: Boolean = false,
    val foods: List<FoodItemDto> = emptyList(),
    val errorMessage: String? = null
)

data class AddFoodUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val createdFood: FoodItemDto? = null,
    val errorMessage: String? = null
)

data class NutritionGoalUiState(
    val isLoading: Boolean = false,
    val goal: NutritionGoalDto? = null,
    val isSaveSuccess: Boolean = false,
    val errorMessage: String? = null
)

data class MealsUiState(
    val isLoading: Boolean = false,
    val date: String = "",
    val totals: MealTotalsDto? = null,
    val meals: List<MealEntryDto> = emptyList(),
    val errorMessage: String? = null
)

data class LogMealUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val loggedEntry: MealEntryDto? = null,
    val errorMessage: String? = null
)

