package com.simats.vitalsync.data.network

import com.simats.vitalsync.data.model.nutrition.CreateFoodRequest
import com.simats.vitalsync.data.model.nutrition.CreateFoodResponse
import com.simats.vitalsync.data.model.nutrition.DeleteMealResponse
import com.simats.vitalsync.data.model.nutrition.FoodsResponse
import com.simats.vitalsync.data.model.nutrition.LogMealRequest
import com.simats.vitalsync.data.model.nutrition.LogMealResponse
import com.simats.vitalsync.data.model.nutrition.MealsResponse
import com.simats.vitalsync.data.model.nutrition.NutritionGoalResponse
import com.simats.vitalsync.data.model.nutrition.SaveNutritionGoalRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface NutritionApiService {

    @GET("nutrition/foods.php")
    suspend fun getFoods(
        @Query("email") email: String,
        @Query("q") query: String? = null,
        @Query("category") category: String? = null
    ): Response<FoodsResponse>

    @POST("nutrition/foods.php")
    suspend fun addCustomFood(
        @Body request: CreateFoodRequest
    ): Response<CreateFoodResponse>

    @GET("nutrition/goals.php")
    suspend fun getNutritionGoal(
        @Query("email") email: String
    ): Response<NutritionGoalResponse>

    @POST("nutrition/goals.php")
    suspend fun saveNutritionGoal(
        @Body request: SaveNutritionGoalRequest
    ): Response<NutritionGoalResponse>

    @GET("nutrition/meals.php")
    suspend fun getMeals(
        @Query("email") email: String,
        @Query("date") date: String
    ): Response<MealsResponse>

    @POST("nutrition/meals.php")
    suspend fun logMeal(
        @Body request: LogMealRequest
    ): Response<LogMealResponse>

    @DELETE("nutrition/meals.php")
    suspend fun deleteMeal(
        @Query("email") email: String,
        @Query("id") mealId: String
    ): Response<DeleteMealResponse>
}

