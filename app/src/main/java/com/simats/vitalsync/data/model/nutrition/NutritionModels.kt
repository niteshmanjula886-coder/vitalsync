package com.simats.vitalsync.data.model.nutrition

import com.google.gson.annotations.SerializedName
import com.simats.vitalsync.data.DietaryPreference
import com.simats.vitalsync.data.FitnessGoal
import com.simats.vitalsync.data.FoodCategory
import com.simats.vitalsync.data.FoodItem
import com.simats.vitalsync.data.MealCategory
import com.simats.vitalsync.data.MealEntry
import com.simats.vitalsync.data.NutritionGoal

// ==========================================
// FOOD ITEMS MODELS
// ==========================================

data class FoodsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("count") val count: Int = 0,
    @SerializedName("foods") val foods: List<FoodItemDto> = emptyList(),
    @SerializedName("message") val message: String = ""
)

data class FoodItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("db_id") val dbId: Int = 0,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("servingSizeAmount") val servingSizeAmount: Float = 100f,
    @SerializedName("servingUnit") val servingUnit: String = "100g",
    @SerializedName("calories") val calories: Int = 0,
    @SerializedName("proteinGrams") val proteinGrams: Float = 0f,
    @SerializedName("carbsGrams") val carbsGrams: Float = 0f,
    @SerializedName("fatGrams") val fatGrams: Float = 0f,
    @SerializedName("isCustom") val isCustom: Boolean = false
) {
    fun toFoodItem(): FoodItem {
        val cat = try {
            FoodCategory.valueOf(category.uppercase())
        } catch (_: Exception) {
            FoodCategory.VEG
        }
        return FoodItem(
            id = id,
            name = name,
            category = cat,
            servingSizeAmount = servingSizeAmount,
            servingUnit = servingUnit,
            calories = calories,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            isCustom = isCustom
        )
    }
}

data class CreateFoodRequest(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("servingSizeAmount") val servingSizeAmount: Float,
    @SerializedName("servingUnit") val servingUnit: String,
    @SerializedName("calories") val calories: Int,
    @SerializedName("proteinGrams") val proteinGrams: Float,
    @SerializedName("carbsGrams") val carbsGrams: Float,
    @SerializedName("fatGrams") val fatGrams: Float
)

data class CreateFoodResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String = "",
    @SerializedName("food") val food: FoodItemDto? = null
)

// ==========================================
// NUTRITION GOAL MODELS
// ==========================================

data class NutritionGoalResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String = "",
    @SerializedName("nutrition_goal") val nutritionGoal: NutritionGoalDto? = null
)

data class NutritionGoalDto(
    @SerializedName("age") val age: Int = 28,
    @SerializedName("weightKg") val weightKg: Float = 68.5f,
    @SerializedName("heightCm") val heightCm: Float = 175.0f,
    @SerializedName("activityLevel") val activityLevel: String = "Moderately Active",
    @SerializedName("fitnessGoal") val fitnessGoal: String = "MUSCLE_GAIN",
    @SerializedName("dietaryPreference") val dietaryPreference: String = "NON_VEGETARIAN",
    @SerializedName("dailyCalorieTarget") val dailyCalorieTarget: Int = 2200,
    @SerializedName("dailyProteinTarget") val dailyProteinTarget: Float = 130.0f,
    @SerializedName("dailyCarbsTarget") val dailyCarbsTarget: Float = 250.0f,
    @SerializedName("dailyFatTarget") val dailyFatTarget: Float = 65.0f
) {
    fun toNutritionGoal(): NutritionGoal {
        val fGoal = try {
            FitnessGoal.valueOf(fitnessGoal.uppercase())
        } catch (_: Exception) {
            FitnessGoal.MUSCLE_GAIN
        }
        val dPref = try {
            DietaryPreference.valueOf(dietaryPreference.uppercase())
        } catch (_: Exception) {
            DietaryPreference.NON_VEGETARIAN
        }
        return NutritionGoal(
            age = age,
            weightKg = weightKg,
            heightCm = heightCm,
            activityLevel = activityLevel,
            fitnessGoal = fGoal,
            dietaryPreference = dPref,
            dailyCalorieTarget = dailyCalorieTarget,
            dailyProteinTarget = dailyProteinTarget,
            dailyCarbsTarget = dailyCarbsTarget,
            dailyFatTarget = dailyFatTarget
        )
    }
}

data class SaveNutritionGoalRequest(
    @SerializedName("email") val email: String,
    @SerializedName("age") val age: Int,
    @SerializedName("weightKg") val weightKg: Float,
    @SerializedName("heightCm") val heightCm: Float,
    @SerializedName("activityLevel") val activityLevel: String,
    @SerializedName("fitnessGoal") val fitnessGoal: String,
    @SerializedName("dietaryPreference") val dietaryPreference: String,
    @SerializedName("dailyCalorieTarget") val dailyCalorieTarget: Int,
    @SerializedName("dailyProteinTarget") val dailyProteinTarget: Float,
    @SerializedName("dailyCarbsTarget") val dailyCarbsTarget: Float,
    @SerializedName("dailyFatTarget") val dailyFatTarget: Float
)

// ==========================================
// MEAL ENTRIES MODELS
// ==========================================

data class MealsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("date") val date: String = "",
    @SerializedName("totals") val totals: MealTotalsDto? = null,
    @SerializedName("meals_count") val mealsCount: Int = 0,
    @SerializedName("meals") val meals: List<MealEntryDto> = emptyList(),
    @SerializedName("message") val message: String = ""
)

data class MealTotalsDto(
    @SerializedName("calories") val calories: Int = 0,
    @SerializedName("calorie_goal") val calorieGoal: Int = 2200,
    @SerializedName("protein") val protein: Float = 0f,
    @SerializedName("protein_goal") val proteinGoal: Float = 130f,
    @SerializedName("carbs") val carbs: Float = 0f,
    @SerializedName("carbs_goal") val carbsGoal: Float = 250f,
    @SerializedName("fat") val fat: Float = 0f,
    @SerializedName("fat_goal") val fatGoal: Float = 65f
)

data class MealEntryDto(
    @SerializedName("id") val id: String,
    @SerializedName("category") val category: String,
    @SerializedName("quantity") val quantity: Float = 1.0f,
    @SerializedName("calculatedCalories") val calculatedCalories: Int = 0,
    @SerializedName("calculatedProtein") val calculatedProtein: Float = 0f,
    @SerializedName("calculatedCarbs") val calculatedCarbs: Float = 0f,
    @SerializedName("calculatedFat") val calculatedFat: Float = 0f,
    @SerializedName("dateString") val dateString: String = "",
    @SerializedName("foodItem") val foodItem: FoodItemDto? = null
) {
    fun toMealEntry(): MealEntry {
        val cat = try {
            MealCategory.valueOf(category.uppercase())
        } catch (_: Exception) {
            MealCategory.BREAKFAST
        }
        val defaultFood = FoodItem(
            id = id,
            name = "Meal Item",
            category = FoodCategory.VEG,
            servingSizeAmount = 100f,
            servingUnit = "serving",
            calories = calculatedCalories,
            proteinGrams = calculatedProtein,
            carbsGrams = calculatedCarbs,
            fatGrams = calculatedFat
        )
        return MealEntry(
            id = id,
            category = cat,
            foodItem = foodItem?.toFoodItem() ?: defaultFood,
            quantity = quantity,
            calculatedCalories = calculatedCalories,
            calculatedProtein = calculatedProtein,
            calculatedCarbs = calculatedCarbs,
            calculatedFat = calculatedFat,
            dateString = dateString
        )
    }
}

data class LogMealRequest(
    @SerializedName("email") val email: String,
    @SerializedName("foodId") val foodId: String? = null,
    @SerializedName("category") val category: String,
    @SerializedName("quantity") val quantity: Float,
    @SerializedName("calculatedCalories") val calculatedCalories: Int,
    @SerializedName("calculatedProtein") val calculatedProtein: Float,
    @SerializedName("calculatedCarbs") val calculatedCarbs: Float,
    @SerializedName("calculatedFat") val calculatedFat: Float,
    @SerializedName("dateString") val dateString: String
)

data class LogMealResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String = "",
    @SerializedName("entry") val entry: MealEntryDto? = null
)

data class DeleteMealResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String = ""
)

// Extension converters for Domain Models -> Request DTOs
fun FoodItem.toCreateRequest(email: String): CreateFoodRequest {
    return CreateFoodRequest(
        email = email,
        name = name,
        category = category.name,
        servingSizeAmount = servingSizeAmount,
        servingUnit = servingUnit,
        calories = calories,
        proteinGrams = proteinGrams,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams
    )
}

fun NutritionGoal.toSaveRequest(email: String): SaveNutritionGoalRequest {
    return SaveNutritionGoalRequest(
        email = email,
        age = age,
        weightKg = weightKg,
        heightCm = heightCm,
        activityLevel = activityLevel,
        fitnessGoal = fitnessGoal.name,
        dietaryPreference = dietaryPreference.name,
        dailyCalorieTarget = dailyCalorieTarget,
        dailyProteinTarget = dailyProteinTarget,
        dailyCarbsTarget = dailyCarbsTarget,
        dailyFatTarget = dailyFatTarget
    )
}

fun MealEntry.toLogRequest(email: String): LogMealRequest {
    return LogMealRequest(
        email = email,
        foodId = foodItem.id,
        category = category.name,
        quantity = quantity,
        calculatedCalories = calculatedCalories,
        calculatedProtein = calculatedProtein,
        calculatedCarbs = calculatedCarbs,
        calculatedFat = calculatedFat,
        dateString = dateString
    )
}

