package com.simats.vitalsync.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FitnessRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("vitalsync_fitness_prefs", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var INSTANCE: FitnessRepository? = null

        fun getInstance(context: Context): FitnessRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = FitnessRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun getTodayDateString(): String {
        return dateFormat.format(Date())
    }

    // Initial Indian Food Database (Estimates clearly defined per serving)
    val initialIndianFoodDatabase = listOf(
        FoodItem("f1", "Whole Egg (Boiled/Poached)", FoodCategory.EGG, 1f, "large egg (50g)", 78, 6.3f, 0.6f, 5.3f),
        FoodItem("f2", "Chicken Breast (Raw)", FoodCategory.NON_VEG, 100f, "100g", 165, 31.0f, 0.0f, 3.6f),
        FoodItem("f3", "Grilled Chicken Breast (Cooked)", FoodCategory.NON_VEG, 100f, "100g", 195, 29.5f, 0.0f, 7.8f),
        FoodItem("f4", "Cooked Basmati Rice", FoodCategory.VEG, 100f, "100g (1 cup cooked)", 130, 2.7f, 28.0f, 0.3f),
        FoodItem("f5", "Roti / Chapati (Whole Wheat)", FoodCategory.VEG, 1f, "1 chapati (30g)", 85, 3.1f, 15.0f, 0.5f),
        FoodItem("f6", "Paneer (Raw Cottage Cheese)", FoodCategory.VEG, 100f, "100g", 265, 18.0f, 3.5f, 20.0f),
        FoodItem("f7", "Soya Chunks (Raw / Uncooked)", FoodCategory.VEGAN, 50f, "50g", 172, 26.0f, 16.5f, 0.5f),
        FoodItem("f8", "Cow Milk (Toned / Low Fat)", FoodCategory.VEG, 250f, "250 ml (1 glass)", 150, 8.0f, 12.0f, 7.5f),
        FoodItem("f9", "Curd / Dahi (Whole Milk)", FoodCategory.VEG, 150f, "150g (1 bowl)", 98, 5.2f, 7.0f, 5.5f),
        FoodItem("f10", "Banana", FoodCategory.VEGAN, 1f, "1 medium banana (118g)", 105, 1.3f, 27.0f, 0.3f),
        FoodItem("f11", "Almonds (Raw)", FoodCategory.VEGAN, 30f, "30g (approx 20 almonds)", 175, 6.0f, 6.0f, 15.0f),
        FoodItem("f12", "Rolled Oats (Dry)", FoodCategory.VEGAN, 40f, "40g (1/2 cup dry)", 152, 5.3f, 27.0f, 2.7f),
        FoodItem("f13", "Dal Tadka (Cooked Yellow Lentils)", FoodCategory.VEG, 150f, "150g (1 katori)", 140, 7.5f, 18.0f, 4.2f)
    )

    // Initial Default Exercise Library (Chest, Back, Shoulders, Arms, Legs)
    val defaultExerciseLibrary = listOf(
        // Chest
        Exercise("e1", "Barbell Bench Press", "Chest", 60f, 10, "Focus on full range of motion"),
        Exercise("e2", "Incline Dumbbell Press", "Chest", 22f, 10, "Keep elbows at 45 degree angle"),
        Exercise("e11", "Machine Chest Press", "Chest", 50f, 12, "Control the negative movement"),
        Exercise("e12", "Cable Chest Fly", "Chest", 15f, 15, "Squeeze chest at peak contraction"),

        // Back
        Exercise("e7", "Lat Pulldowns", "Back", 50f, 12, "Pull down to upper chest"),
        Exercise("e8", "Seated Cable Rows", "Back", 55f, 12, "Squeeze shoulder blades together"),
        Exercise("e13", "Chest-Supported DB Row", "Back", 20f, 10, "Eliminate momentum"),
        Exercise("e14", "Assisted Pull-Up", "Back", 0f, 8, "Full extension at bottom"),

        // Shoulders
        Exercise("e5", "Overhead Shoulder Press", "Shoulders", 40f, 10, "Core braced, lock out at top"),
        Exercise("e6", "Lateral Raises", "Shoulders", 10f, 15, "Slight bend in elbows"),
        Exercise("e15", "Face Pulls", "Shoulders", 17.5f, 15, "Pull cable towards forehead"),
        Exercise("e16", "Reverse Pec Deck Fly", "Shoulders", 30f, 15, "Isolate rear deltoids"),

        // Arms (Biceps & Triceps)
        Exercise("e9", "Barbell Biceps Curls", "Arms", 25f, 12, "Keep elbows pinned to sides"),
        Exercise("e10", "Triceps Rope Pushdowns", "Arms", 22.5f, 15, "Fully extend arms at bottom"),
        Exercise("e17", "Hammer Curls", "Arms", 14f, 12, "Palms facing each other"),
        Exercise("e18", "Incline Dumbbell Curl", "Arms", 12f, 10, "Deep stretch at bottom"),
        Exercise("e19", "Preacher Curl", "Arms", 20f, 10, "Keep upper arms flush against pad"),
        Exercise("e20", "Overhead Triceps Extension", "Arms", 18f, 12, "Keep elbows tucked in"),

        // Legs
        Exercise("e3", "Barbell Squat", "Legs", 80f, 8, "Keep chest up and knees outward"),
        Exercise("e4", "Romanian Deadlift", "Legs", 70f, 10, "Hinge at hips, keep spine neutral"),
        Exercise("e21", "Leg Press", "Legs", 120f, 10, "Push through heels"),
        Exercise("e22", "Leg Extension", "Legs", 40f, 15, "Hold peak contraction at top"),
        Exercise("e23", "Lying Leg Curl", "Legs", 35f, 12, "Control weight on descent"),
        Exercise("e24", "Standing Calf Raise", "Legs", 50f, 15, "Full stretch at bottom")
    )

    fun getExerciseLibrary(): List<Exercise> {
        val customJson = prefs.getString("key_custom_exercises", null) ?: return defaultExerciseLibrary
        val customList = mutableListOf<Exercise>()
        try {
            val array = JSONArray(customJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                customList.add(
                    Exercise(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        targetMuscle = obj.getString("targetMuscle"),
                        defaultWeightKg = obj.getDouble("defaultWeightKg").toFloat(),
                        defaultReps = obj.getInt("defaultReps"),
                        notes = obj.optString("notes", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return defaultExerciseLibrary + customList
    }

    fun addCustomExercise(exercise: Exercise) {
        val customExercises = getExerciseLibrary().filter { it.id.startsWith("custom_ex_") }.toMutableList()
        customExercises.add(exercise)

        val array = JSONArray()
        for (ex in customExercises) {
            val obj = JSONObject().apply {
                put("id", ex.id)
                put("name", ex.name)
                put("targetMuscle", ex.targetMuscle)
                put("defaultWeightKg", ex.defaultWeightKg)
                put("defaultReps", ex.defaultReps)
                put("notes", ex.notes)
            }
            array.put(obj)
        }
        prefs.edit().putString("key_custom_exercises", array.toString()).apply()
    }

    // NUTRITION PERSISTENCE
    fun getNutritionGoal(): NutritionGoal {
        val jsonStr = prefs.getString("key_nutrition_goal", null) ?: return NutritionGoal()
        return try {
            val obj = JSONObject(jsonStr)
            NutritionGoal(
                age = obj.optInt("age", 28),
                weightKg = obj.optDouble("weightKg", 68.5).toFloat(),
                heightCm = obj.optDouble("heightCm", 175.0).toFloat(),
                activityLevel = obj.optString("activityLevel", "Moderately Active"),
                fitnessGoal = FitnessGoal.valueOf(obj.optString("fitnessGoal", "MUSCLE_GAIN")),
                dietaryPreference = DietaryPreference.valueOf(obj.optString("dietaryPreference", "NON_VEGETARIAN")),
                dailyCalorieTarget = obj.optInt("dailyCalorieTarget", 2200),
                dailyProteinTarget = obj.optDouble("dailyProteinTarget", 130.0).toFloat(),
                dailyCarbsTarget = obj.optDouble("dailyCarbsTarget", 250.0).toFloat(),
                dailyFatTarget = obj.optDouble("dailyFatTarget", 65.0).toFloat()
            )
        } catch (e: Exception) {
            NutritionGoal()
        }
    }

    fun saveNutritionGoal(goal: NutritionGoal) {
        val obj = JSONObject().apply {
            put("age", goal.age)
            put("weightKg", goal.weightKg)
            put("heightCm", goal.heightCm)
            put("activityLevel", goal.activityLevel)
            put("fitnessGoal", goal.fitnessGoal.name)
            put("dietaryPreference", goal.dietaryPreference.name)
            put("dailyCalorieTarget", goal.dailyCalorieTarget)
            put("dailyProteinTarget", goal.dailyProteinTarget)
            put("dailyCarbsTarget", goal.dailyCarbsTarget)
            put("dailyFatTarget", goal.dailyFatTarget)
        }
        prefs.edit().putString("key_nutrition_goal", obj.toString()).apply()
    }

    fun getFoodDatabase(): List<FoodItem> {
        val customJson = prefs.getString("key_custom_foods", null) ?: return initialIndianFoodDatabase
        val customFoods = mutableListOf<FoodItem>()
        try {
            val array = JSONArray(customJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                customFoods.add(
                    FoodItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        category = FoodCategory.valueOf(obj.getString("category")),
                        servingSizeAmount = obj.getDouble("servingSizeAmount").toFloat(),
                        servingUnit = obj.getString("servingUnit"),
                        calories = obj.getInt("calories"),
                        proteinGrams = obj.getDouble("proteinGrams").toFloat(),
                        carbsGrams = obj.getDouble("carbsGrams").toFloat(),
                        fatGrams = obj.getDouble("fatGrams").toFloat(),
                        isCustom = true
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return initialIndianFoodDatabase + customFoods
    }

    fun addCustomFoodItem(foodItem: FoodItem) {
        val currentFoods = getFoodDatabase().filter { it.isCustom }.toMutableList()
        currentFoods.add(foodItem)

        val array = JSONArray()
        for (item in currentFoods) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("category", item.category.name)
                put("servingSizeAmount", item.servingSizeAmount)
                put("servingUnit", item.servingUnit)
                put("calories", item.calories)
                put("proteinGrams", item.proteinGrams)
                put("carbsGrams", item.carbsGrams)
                put("fatGrams", item.fatGrams)
            }
            array.put(obj)
        }
        prefs.edit().putString("key_custom_foods", array.toString()).apply()
    }

    fun getMealEntriesForDate(dateString: String): List<MealEntry> {
        val jsonStr = prefs.getString("key_meals_$dateString", null) ?: return emptyList()
        val entries = mutableListOf<MealEntry>()
        val foodDbMap = getFoodDatabase().associateBy { it.id }

        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val foodId = obj.getString("foodId")
                val food = foodDbMap[foodId] ?: continue
                val quantity = obj.getDouble("quantity").toFloat()

                entries.add(
                    MealEntry(
                        id = obj.getString("id"),
                        category = MealCategory.valueOf(obj.getString("category")),
                        foodItem = food,
                        quantity = quantity,
                        calculatedCalories = obj.getInt("calculatedCalories"),
                        calculatedProtein = obj.getDouble("calculatedProtein").toFloat(),
                        calculatedCarbs = obj.getDouble("calculatedCarbs").toFloat(),
                        calculatedFat = obj.getDouble("calculatedFat").toFloat(),
                        dateString = dateString
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return entries
    }

    fun saveMealEntry(mealEntry: MealEntry) {
        val currentMeals = getMealEntriesForDate(mealEntry.dateString).toMutableList()
        val existingIndex = currentMeals.indexOfFirst { it.id == mealEntry.id }
        if (existingIndex >= 0) {
            currentMeals[existingIndex] = mealEntry
        } else {
            currentMeals.add(mealEntry)
        }

        saveMealEntriesListForDate(mealEntry.dateString, currentMeals)
    }

    fun deleteMealEntry(id: String, dateString: String) {
        val currentMeals = getMealEntriesForDate(dateString).filter { it.id != id }
        saveMealEntriesListForDate(dateString, currentMeals)
    }

    private fun saveMealEntriesListForDate(dateString: String, meals: List<MealEntry>) {
        val array = JSONArray()
        for (m in meals) {
            val obj = JSONObject().apply {
                put("id", m.id)
                put("category", m.category.name)
                put("foodId", m.foodItem.id)
                put("quantity", m.quantity)
                put("calculatedCalories", m.calculatedCalories)
                put("calculatedProtein", m.calculatedProtein)
                put("calculatedCarbs", m.calculatedCarbs)
                put("calculatedFat", m.calculatedFat)
            }
            array.put(obj)
        }
        prefs.edit().putString("key_meals_$dateString", array.toString()).apply()
    }

    // WORKOUT PERSISTENCE
    fun getAllWorkoutSessions(): List<WorkoutSession> {
        val jsonStr = prefs.getString("key_workout_sessions", null) ?: return emptyList()
        val list = mutableListOf<WorkoutSession>()
        val exerciseMap = getExerciseLibrary().associateBy { it.id }

        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val sessionObj = array.getJSONObject(i)
                val exercisesList = mutableListOf<WorkoutExercise>()
                val exArray = sessionObj.getJSONArray("exercises")

                for (j in 0 until exArray.length()) {
                    val exObj = exArray.getJSONObject(j)
                    val exId = exObj.getString("exerciseId")
                    val exercise = exerciseMap[exId] ?: Exercise(exId, exObj.optString("exerciseName", "Exercise"), "General")
                    val setsList = mutableListOf<WorkoutSet>()
                    val setsArray = exObj.getJSONArray("sets")

                    for (k in 0 until setsArray.length()) {
                        val setObj = setsArray.getJSONObject(k)
                        setsList.add(
                            WorkoutSet(
                                setNumber = setObj.getInt("setNumber"),
                                reps = setObj.getInt("reps"),
                                weightKg = setObj.getDouble("weightKg").toFloat(),
                                isCompleted = setObj.getBoolean("isCompleted")
                            )
                        )
                    }

                    exercisesList.add(
                        WorkoutExercise(
                            exercise = exercise,
                            sets = setsList,
                            notes = exObj.optString("notes", "")
                        )
                    )
                }

                list.add(
                    WorkoutSession(
                        id = sessionObj.getString("id"),
                        name = sessionObj.getString("name"),
                        split = WorkoutSplit.valueOf(sessionObj.getString("split")),
                        dateString = sessionObj.getString("dateString"),
                        timestampFormatted = sessionObj.getString("timestampFormatted"),
                        durationMinutes = sessionObj.getInt("durationMinutes"),
                        exercises = exercisesList,
                        totalVolumeKg = sessionObj.getDouble("totalVolumeKg").toFloat(),
                        estimatedCaloriesBurned = sessionObj.getInt("estimatedCaloriesBurned")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveWorkoutSession(session: WorkoutSession) {
        val currentSessions = getAllWorkoutSessions().toMutableList()
        currentSessions.add(0, session) // newest first

        val array = JSONArray()
        for (s in currentSessions) {
            val sessionObj = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("split", s.split.name)
                put("dateString", s.dateString)
                put("timestampFormatted", s.timestampFormatted)
                put("durationMinutes", s.durationMinutes)
                put("totalVolumeKg", s.totalVolumeKg)
                put("estimatedCaloriesBurned", s.estimatedCaloriesBurned)

                val exArray = JSONArray()
                for (we in s.exercises) {
                    val exObj = JSONObject().apply {
                        put("exerciseId", we.exercise.id)
                        put("exerciseName", we.exercise.name)
                        put("notes", we.notes)

                        val setsArray = JSONArray()
                        for (set in we.sets) {
                            val setObj = JSONObject().apply {
                                put("setNumber", set.setNumber)
                                put("reps", set.reps)
                                put("weightKg", set.weightKg)
                                put("isCompleted", set.isCompleted)
                            }
                            setsArray.put(setObj)
                        }
                        put("sets", setsArray)
                    }
                    exArray.put(exObj)
                }
                put("exercises", exArray)
            }
            array.put(sessionObj)
        }
        prefs.edit().putString("key_workout_sessions", array.toString()).apply()
    }
}
