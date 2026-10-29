package com.simats.vitalsync.data.repository

import com.simats.vitalsync.data.model.nutrition.CreateFoodRequest
import com.simats.vitalsync.data.model.nutrition.FoodItemDto
import com.simats.vitalsync.data.model.nutrition.LogMealRequest
import com.simats.vitalsync.data.model.nutrition.MealEntryDto
import com.simats.vitalsync.data.model.nutrition.MealsResponse
import com.simats.vitalsync.data.model.nutrition.NutritionGoalDto
import com.simats.vitalsync.data.model.nutrition.SaveNutritionGoalRequest
import com.simats.vitalsync.data.network.NutritionApiService
import com.simats.vitalsync.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject

class NutritionRepository(
    private val apiService: NutritionApiService = RetrofitClient.nutritionApiService
) {

    fun getFoods(
        email: String,
        query: String? = null,
        category: String? = null
    ): Flow<Resource<List<FoodItemDto>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getFoods(email, query, category)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body.foods))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to load foods" }))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error loading food items"))
        }
    }.flowOn(Dispatchers.IO)

    fun addCustomFood(
        request: CreateFoodRequest
    ): Flow<Resource<FoodItemDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.addCustomFood(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.food != null) {
                    emit(Resource.Success(body.food))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to create food item" }))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error creating custom food"))
        }
    }.flowOn(Dispatchers.IO)

    fun getNutritionGoal(
        email: String
    ): Flow<Resource<NutritionGoalDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getNutritionGoal(email)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.nutritionGoal != null) {
                    emit(Resource.Success(body.nutritionGoal))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to load nutrition goals" }))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error loading nutrition goals"))
        }
    }.flowOn(Dispatchers.IO)

    fun saveNutritionGoal(
        request: SaveNutritionGoalRequest
    ): Flow<Resource<NutritionGoalDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.saveNutritionGoal(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.nutritionGoal != null) {
                    emit(Resource.Success(body.nutritionGoal))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to save nutrition goals" }))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error saving nutrition goals"))
        }
    }.flowOn(Dispatchers.IO)

    fun getMeals(
        email: String,
        date: String
    ): Flow<Resource<MealsResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getMeals(email, date)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to load meal entries" }))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error loading meal entries"))
        }
    }.flowOn(Dispatchers.IO)

    fun logMeal(
        request: LogMealRequest
    ): Flow<Resource<MealEntryDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.logMeal(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.entry != null) {
                    emit(Resource.Success(body.entry))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to log meal entry" }))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error logging meal entry"))
        }
    }.flowOn(Dispatchers.IO)

    fun deleteMeal(
        email: String,
        mealId: String
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.deleteMeal(email, mealId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body.message.ifBlank { "Meal entry deleted" }))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to delete meal entry" }))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error deleting meal entry"))
        }
    }.flowOn(Dispatchers.IO)

    private fun parseErrorMessage(errorJson: String?): String {
        if (errorJson.isNullOrBlank()) return "Server returned an error response"
        return try {
            val json = JSONObject(errorJson)
            json.optString("message", "An unexpected server error occurred")
        } catch (_: Exception) {
            "An unexpected server error occurred"
        }
    }
}

