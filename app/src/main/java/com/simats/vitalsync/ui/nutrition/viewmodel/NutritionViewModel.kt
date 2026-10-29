package com.simats.vitalsync.ui.nutrition.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.vitalsync.data.FoodItem
import com.simats.vitalsync.data.MealEntry
import com.simats.vitalsync.data.NutritionGoal
import com.simats.vitalsync.data.model.nutrition.CreateFoodRequest
import com.simats.vitalsync.data.model.nutrition.LogMealRequest
import com.simats.vitalsync.data.model.nutrition.toSaveRequest
import com.simats.vitalsync.data.repository.NutritionRepository
import com.simats.vitalsync.data.repository.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NutritionViewModel(
    private val repository: NutritionRepository = NutritionRepository()
) : ViewModel() {

    private val _foodsState = MutableStateFlow(FoodsUiState())
    val foodsState: StateFlow<FoodsUiState> = _foodsState.asStateFlow()

    private val _addFoodState = MutableStateFlow(AddFoodUiState())
    val addFoodState: StateFlow<AddFoodUiState> = _addFoodState.asStateFlow()

    private val _goalState = MutableStateFlow(NutritionGoalUiState())
    val goalState: StateFlow<NutritionGoalUiState> = _goalState.asStateFlow()

    private val _mealsState = MutableStateFlow(MealsUiState())
    val mealsState: StateFlow<MealsUiState> = _mealsState.asStateFlow()

    private val _logMealState = MutableStateFlow(LogMealUiState())
    val logMealState: StateFlow<LogMealUiState> = _logMealState.asStateFlow()

    fun loadFoods(email: String, query: String? = null, category: String? = null) {
        viewModelScope.launch {
            repository.getFoods(email, query, category).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _foodsState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _foodsState.update {
                            it.copy(
                                isLoading = false,
                                foods = resource.data,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _foodsState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun addCustomFood(
        email: String,
        name: String,
        category: String,
        servingAmount: Float,
        servingUnit: String,
        calories: Int,
        protein: Float,
        carbs: Float,
        fat: Float,
        onSuccess: (FoodItem) -> Unit = {}
    ) {
        viewModelScope.launch {
            val req = CreateFoodRequest(
                email = email,
                name = name,
                category = category,
                servingSizeAmount = servingAmount,
                servingUnit = servingUnit,
                calories = calories,
                proteinGrams = protein,
                carbsGrams = carbs,
                fatGrams = fat
            )
            repository.addCustomFood(req).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _addFoodState.update { it.copy(isLoading = true, isSuccess = false, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        val foodDto = resource.data
                        val domainFood = foodDto.toFoodItem()
                        _addFoodState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = true,
                                createdFood = foodDto,
                                errorMessage = null
                            )
                        }
                        // Prepend newly created food to the food list state
                        _foodsState.update { cur ->
                            cur.copy(foods = listOf(foodDto) + cur.foods.filter { it.id != foodDto.id })
                        }
                        onSuccess(domainFood)
                    }
                    is Resource.Error -> {
                        _addFoodState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun resetAddFoodState() {
        _addFoodState.value = AddFoodUiState()
    }

    fun loadNutritionGoal(email: String) {
        viewModelScope.launch {
            repository.getNutritionGoal(email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _goalState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _goalState.update {
                            it.copy(
                                isLoading = false,
                                goal = resource.data,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _goalState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun saveNutritionGoal(
        email: String,
        goal: NutritionGoal,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val req = goal.toSaveRequest(email)
            repository.saveNutritionGoal(req).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _goalState.update { it.copy(isLoading = true, isSaveSuccess = false, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _goalState.update {
                            it.copy(
                                isLoading = false,
                                isSaveSuccess = true,
                                goal = resource.data,
                                errorMessage = null
                            )
                        }
                        onSuccess()
                    }
                    is Resource.Error -> {
                        _goalState.update {
                            it.copy(
                                isLoading = false,
                                isSaveSuccess = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun loadMeals(email: String, date: String) {
        viewModelScope.launch {
            repository.getMeals(email, date).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _mealsState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _mealsState.update {
                            it.copy(
                                isLoading = false,
                                date = resource.data.date,
                                totals = resource.data.totals,
                                meals = resource.data.meals,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _mealsState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun logMeal(
        email: String,
        foodId: String?,
        category: String,
        quantity: Float,
        calculatedCalories: Int,
        calculatedProtein: Float,
        calculatedCarbs: Float,
        calculatedFat: Float,
        dateString: String,
        onSuccess: (MealEntry) -> Unit = {}
    ) {
        viewModelScope.launch {
            val req = LogMealRequest(
                email = email,
                foodId = foodId,
                category = category,
                quantity = quantity,
                calculatedCalories = calculatedCalories,
                calculatedProtein = calculatedProtein,
                calculatedCarbs = calculatedCarbs,
                calculatedFat = calculatedFat,
                dateString = dateString
            )
            repository.logMeal(req).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _logMealState.update { it.copy(isLoading = true, isSuccess = false, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        val entryDto = resource.data
                        val domainEntry = entryDto.toMealEntry()
                        _logMealState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = true,
                                loggedEntry = entryDto,
                                errorMessage = null
                            )
                        }
                        // Refresh meals for this date
                        loadMeals(email, dateString)
                        onSuccess(domainEntry)
                    }
                    is Resource.Error -> {
                        _logMealState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun deleteMeal(
        email: String,
        mealId: String,
        dateString: String
    ) {
        viewModelScope.launch {
            // Optimistically remove the meal from _mealsState
            _mealsState.update { current ->
                val remaining = current.meals.filter { it.id != mealId }
                val newCalories = remaining.sumOf { it.calculatedCalories }
                val newProtein = remaining.sumOf { it.calculatedProtein.toDouble() }.toFloat()
                val newCarbs = remaining.sumOf { it.calculatedCarbs.toDouble() }.toFloat()
                val newFat = remaining.sumOf { it.calculatedFat.toDouble() }.toFloat()
                current.copy(
                    meals = remaining,
                    totals = current.totals?.copy(
                        calories = newCalories,
                        protein = newProtein,
                        carbs = newCarbs,
                        fat = newFat
                    )
                )
            }
            repository.deleteMeal(email, mealId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> { }
                    is Resource.Success -> {
                        // Reload meals after successful deletion to sync accurately with backend
                        loadMeals(email, dateString)
                    }
                    is Resource.Error -> {
                        _mealsState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message
                            )
                        }
                        // Re-sync if there was an error
                        loadMeals(email, dateString)
                    }
                }
            }
        }
    }
}

