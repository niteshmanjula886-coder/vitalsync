package com.simats.vitalsync.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class FitnessRepository private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("vitalsync_fitness_prefs", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var INSTANCE: FitnessRepository? = null

        fun getInstance(context: Context): FitnessRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FitnessRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // EXERCISES DATABASE
    val defaultExerciseLibrary = listOf(
        // Push
        Exercise("e1", "Barbell Bench Press", "Chest", 60f, 10, "Keep shoulder blades retracted and feet planted."),
        Exercise("e2", "Incline Dumbbell Press", "Upper Chest", 22f, 10, "Focus on upper chest contraction at top."),
        Exercise("e3", "Overhead Barbell Press", "Shoulders", 40f, 8, "Core braced, press vertically overhead."),
        Exercise("e4", "Triceps Rope Pushdown", "Triceps", 25f, 12, "Keep elbows stationary at sides."),
        Exercise("e5", "Incline Chest Fly", "Chest", 16f, 12, "Slight bend in elbows throughout movement."),
        Exercise("e6", "Lateral Raises", "Side Deltoids", 10f, 15, "Lead with elbows for side shoulder isolation."),

        // Pull
        Exercise("e7", "Barbell Deadlift", "Back & Posture", 100f, 5, "Flat spine, drive through heels."),
        Exercise("e8", "Lat Pulldown", "Lats", 50f, 10, "Pull bar to upper chest squeezing lats."),
        Exercise("e9", "Seated Cable Row", "Mid Back", 45f, 12, "Keep torso upright and squeeze shoulder blades."),
        Exercise("e10", "Dumbbell Bicep Curls", "Biceps", 14f, 12, "Control eccentric negative movement."),
        Exercise("e11", "Face Pulls", "Rear Delts", 20f, 15, "Pull rope towards forehead, flare elbows."),

        // Legs
        Exercise("e12", "Barbell Back Squat", "Quadriceps", 80f, 8, "Break at hips, squat to parallel depth."),
        Exercise("e13", "Romanian Deadlift", "Hamstrings", 70f, 10, "Hinge at hips feeling hamstring stretch."),
        Exercise("e14", "Leg Press", "Quadriceps & Glutes", 140f, 12, "Controlled depth without lower back rounding."),
        Exercise("e15", "Standing Calf Raises", "Calves", 50f, 15, "Full stretch at bottom, squeeze at peak.")
    )

    fun getExerciseLibrary(): List<Exercise> {
        val customExList = loadCustomExercisesFromPrefs()
        return defaultExerciseLibrary + customExList
    }

    fun addCustomExercise(exercise: Exercise) {
        val customExList = loadCustomExercisesFromPrefs().toMutableList()
        customExList.add(exercise)
        saveCustomExercisesToPrefs(customExList)
    }

    private fun loadCustomExercisesFromPrefs(): List<Exercise> {
        val jsonStr = prefs.getString("key_custom_exercises", null) ?: return emptyList()
        val list = mutableListOf<Exercise>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
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
        } catch (_: Exception) {
        }
        return list
    }

    private fun saveCustomExercisesToPrefs(customExercises: List<Exercise>) {
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
        } catch (_: Exception) {
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

    // MEALS FOR DATE
    fun getMealEntriesForDate(dateStr: String): List<MealEntry> {
        val jsonStr = prefs.getString("key_meals_$dateStr", null) ?: return getSampleMealsForToday()
        val list = mutableListOf<MealEntry>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val foodObj = obj.getJSONObject("foodItem")
                val foodItem = FoodItem(
                    id = foodObj.getString("id"),
                    name = foodObj.getString("name"),
                    category = FoodCategory.valueOf(foodObj.getString("category")),
                    servingSizeAmount = foodObj.getDouble("servingSizeAmount").toFloat(),
                    servingUnit = foodObj.getString("servingUnit"),
                    calories = foodObj.getInt("calories"),
                    proteinGrams = foodObj.getDouble("proteinGrams").toFloat(),
                    carbsGrams = foodObj.getDouble("carbsGrams").toFloat(),
                    fatGrams = foodObj.getDouble("fatGrams").toFloat(),
                    isCustom = foodObj.optBoolean("isCustom", false)
                )

                list.add(
                    MealEntry(
                        id = obj.getString("id"),
                        category = MealCategory.valueOf(obj.getString("category")),
                        foodItem = foodItem,
                        quantity = obj.getDouble("quantity").toFloat(),
                        calculatedCalories = obj.getInt("calculatedCalories"),
                        calculatedProtein = obj.getDouble("calculatedProtein").toFloat(),
                        calculatedCarbs = obj.getDouble("calculatedCarbs").toFloat(),
                        calculatedFat = obj.getDouble("calculatedFat").toFloat(),
                        dateString = obj.getString("dateString")
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun saveMealEntry(mealEntry: MealEntry) {
        val currentList = getMealEntriesForDate(mealEntry.dateString).toMutableList()
        currentList.add(mealEntry)

        val array = JSONArray()
        for (m in currentList) {
            val obj = JSONObject().apply {
                put("id", m.id)
                put("category", m.category.name)
                put("quantity", m.quantity)
                put("calculatedCalories", m.calculatedCalories)
                put("calculatedProtein", m.calculatedProtein)
                put("calculatedCarbs", m.calculatedCarbs)
                put("calculatedFat", m.calculatedFat)
                put("dateString", m.dateString)

                val foodObj = JSONObject().apply {
                    put("id", m.foodItem.id)
                    put("name", m.foodItem.name)
                    put("category", m.foodItem.category.name)
                    put("servingSizeAmount", m.foodItem.servingSizeAmount)
                    put("servingUnit", m.foodItem.servingUnit)
                    put("calories", m.foodItem.calories)
                    put("proteinGrams", m.foodItem.proteinGrams)
                    put("carbsGrams", m.foodItem.carbsGrams)
                    put("fatGrams", m.foodItem.fatGrams)
                    put("isCustom", m.foodItem.isCustom)
                }
                put("foodItem", foodObj)
            }
            array.put(obj)
        }
        prefs.edit().putString("key_meals_${mealEntry.dateString}", array.toString()).apply()
    }

    fun deleteMealEntry(id: String, dateStr: String) {
        val currentList = getMealEntriesForDate(dateStr).filter { it.id != id }
        val array = JSONArray()
        for (m in currentList) {
            val obj = JSONObject().apply {
                put("id", m.id)
                put("category", m.category.name)
                put("quantity", m.quantity)
                put("calculatedCalories", m.calculatedCalories)
                put("calculatedProtein", m.calculatedProtein)
                put("calculatedCarbs", m.calculatedCarbs)
                put("calculatedFat", m.calculatedFat)
                put("dateString", m.dateString)

                val foodObj = JSONObject().apply {
                    put("id", m.foodItem.id)
                    put("name", m.foodItem.name)
                    put("category", m.foodItem.category.name)
                    put("servingSizeAmount", m.foodItem.servingSizeAmount)
                    put("servingUnit", m.foodItem.servingUnit)
                    put("calories", m.foodItem.calories)
                    put("proteinGrams", m.foodItem.proteinGrams)
                    put("carbsGrams", m.foodItem.carbsGrams)
                    put("fatGrams", m.foodItem.fatGrams)
                    put("isCustom", m.foodItem.isCustom)
                }
                put("foodItem", foodObj)
            }
            array.put(obj)
        }
        prefs.edit().putString("key_meals_$dateStr", array.toString()).apply()
    }

    // WORKOUT HISTORY PERSISTENCE
    fun getAllWorkoutSessions(): List<WorkoutSession> {
        val jsonStr = prefs.getString("key_workout_sessions", null) ?: return getSampleWorkoutSessions()
        val list = mutableListOf<WorkoutSession>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val exercisesArray = obj.getJSONArray("exercises")
                val exercisesList = mutableListOf<WorkoutExercise>()

                for (j in 0 until exercisesArray.length()) {
                    val weObj = exercisesArray.getJSONObject(j)
                    val exObj = weObj.getJSONObject("exercise")
                    val exercise = Exercise(
                        id = exObj.getString("id"),
                        name = exObj.getString("name"),
                        targetMuscle = exObj.getString("targetMuscle"),
                        defaultWeightKg = exObj.getDouble("defaultWeightKg").toFloat(),
                        defaultReps = exObj.getInt("defaultReps"),
                        notes = exObj.optString("notes", "")
                    )

                    val setsArray = weObj.getJSONArray("sets")
                    val setsList = mutableListOf<WorkoutSet>()
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

                    exercisesList.add(WorkoutExercise(exercise, setsList))
                }

                list.add(
                    WorkoutSession(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        split = WorkoutSplit.valueOf(obj.getString("split")),
                        dateString = obj.getString("dateString"),
                        timestampFormatted = obj.getString("timestampFormatted"),
                        durationMinutes = obj.getInt("durationMinutes"),
                        exercises = exercisesList,
                        totalVolumeKg = obj.getDouble("totalVolumeKg").toFloat(),
                        estimatedCaloriesBurned = obj.getInt("estimatedCaloriesBurned")
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun saveWorkoutSession(session: WorkoutSession) {
        val currentList = getAllWorkoutSessions().toMutableList()
        currentList.add(0, session)

        val array = JSONArray()
        for (s in currentList) {
            val sObj = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("split", s.split.name)
                put("dateString", s.dateString)
                put("timestampFormatted", s.timestampFormatted)
                put("durationMinutes", s.durationMinutes)
                put("totalVolumeKg", s.totalVolumeKg)
                put("estimatedCaloriesBurned", s.estimatedCaloriesBurned)

                val exercisesArray = JSONArray()
                for (we in s.exercises) {
                    val weObj = JSONObject().apply {
                        val exObj = JSONObject().apply {
                            put("id", we.exercise.id)
                            put("name", we.exercise.name)
                            put("targetMuscle", we.exercise.targetMuscle)
                            put("defaultWeightKg", we.exercise.defaultWeightKg)
                            put("defaultReps", we.exercise.defaultReps)
                            put("notes", we.exercise.notes)
                        }
                        put("exercise", exObj)

                        val setsArray = JSONArray()
                        for (st in we.sets) {
                            val stObj = JSONObject().apply {
                                put("setNumber", st.setNumber)
                                put("reps", st.reps)
                                put("weightKg", st.weightKg)
                                put("isCompleted", st.isCompleted)
                            }
                            setsArray.put(stObj)
                        }
                        put("sets", setsArray)
                    }
                    exercisesArray.put(weObj)
                }
                put("exercises", exercisesArray)
            }
            array.put(sObj)
        }
        prefs.edit().putString("key_workout_sessions", array.toString()).apply()
    }

    // INDIAN FOOD DATABASE
    val initialIndianFoodDatabase = listOf(
        FoodItem("f1", "Paneer Tikka (Grilled Cottage Cheese)", FoodCategory.VEG, 100f, "100g (4 pieces)", 260, 18f, 6f, 18f),
        FoodItem("f2", "Roti / Phulka (Whole Wheat Flatbread)", FoodCategory.VEG, 30f, "1 piece (30g)", 80, 2.8f, 15f, 0.8f),
        FoodItem("f3", "Chicken Breast Curry (Home Cooked)", FoodCategory.NON_VEG, 150f, "150g (1 bowl)", 220, 28f, 4f, 10f),
        FoodItem("f4", "Egg Bhurji (Scrambled Eggs with Spices)", FoodCategory.EGG, 100f, "2 eggs (100g)", 170, 13f, 2.5f, 12f),
        FoodItem("f5", "Dal Tadka (Cooked Yellow Lentils)", FoodCategory.VEG, 150f, "150g (1 katori)", 140, 7.5f, 18f, 4.2f),
        FoodItem("f6", "Steamed Basmati Rice", FoodCategory.VEG, 150f, "150g (1 bowl)", 195, 3.8f, 42f, 0.6f),
        FoodItem("f7", "Greek Yogurt (Hung Curd)", FoodCategory.VEG, 100f, "100g (1 cup)", 95, 10f, 4.5f, 4f),
        FoodItem("f8", "Whey Protein Scoop in Water", FoodCategory.VEG, 30f, "1 scoop (30g)", 120, 24f, 2f, 1.5f),
        FoodItem("f9", "Masala Dosa with Chutney", FoodCategory.VEG, 150f, "1 dosa (150g)", 320, 6f, 48f, 11f),
        FoodItem("f10", "Idli with Sambar (2 pieces)", FoodCategory.VEG, 120f, "2 idlis + sambar", 180, 6.5f, 36f, 1.2f),
        FoodItem("f11", "Moong Dal Chilla (Lentil Pancake)", FoodCategory.VEG, 80f, "1 chilla (80g)", 130, 8f, 18f, 3f),
        FoodItem("f12", "Chana Masala (Chickpea Curry)", FoodCategory.VEG, 150f, "150g (1 bowl)", 210, 9f, 32f, 5f)
    )

    fun getFoodDatabase(): List<FoodItem> {
        val customFoods = loadCustomFoodsFromPrefs()
        return initialIndianFoodDatabase + customFoods
    }

    fun addCustomFoodItem(foodItem: FoodItem) {
        val customFoods = loadCustomFoodsFromPrefs().toMutableList()
        customFoods.add(foodItem)
        saveCustomFoodsToPrefs(customFoods)
    }

    private fun loadCustomFoodsFromPrefs(): List<FoodItem> {
        val jsonStr = prefs.getString("key_custom_foods", null) ?: return emptyList()
        val list = mutableListOf<FoodItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
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
                        isCustom = obj.optBoolean("isCustom", false)
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    private fun saveCustomFoodsToPrefs(foods: List<FoodItem>) {
        val array = JSONArray()
        for (f in foods) {
            val obj = JSONObject().apply {
                put("id", f.id)
                put("name", f.name)
                put("category", f.category.name)
                put("servingSizeAmount", f.servingSizeAmount)
                put("servingUnit", f.servingUnit)
                put("calories", f.calories)
                put("proteinGrams", f.proteinGrams)
                put("carbsGrams", f.carbsGrams)
                put("fatGrams", f.fatGrams)
                put("isCustom", f.isCustom)
            }
            array.put(obj)
        }
        prefs.edit().putString("key_custom_foods", array.toString()).apply()
    }

    fun getTodayDateString(): String = "2025-10-15"

    private fun getSampleMealsForToday(): List<MealEntry> {
        return listOf(
            MealEntry("m1", MealCategory.BREAKFAST, initialIndianFoodDatabase[3], 1.5f, 255, 19.5f, 3.75f, 18.0f, "2025-10-15"),
            MealEntry("m2", MealCategory.BREAKFAST, initialIndianFoodDatabase[1], 2.0f, 160, 5.6f, 30.0f, 1.6f, "2025-10-15"),
            MealEntry("m3", MealCategory.LUNCH, initialIndianFoodDatabase[2], 1.0f, 220, 28.0f, 4.0f, 10.0f, "2025-10-15"),
            MealEntry("m4", MealCategory.LUNCH, initialIndianFoodDatabase[5], 1.0f, 195, 3.8f, 42.0f, 0.6f, "2025-10-15"),
            MealEntry("m5", MealCategory.SNACKS, initialIndianFoodDatabase[7], 1.0f, 120, 24.0f, 2.0f, 1.5f, "2025-10-15")
        )
    }

    private fun getSampleWorkoutSessions(): List<WorkoutSession> {
        return listOf(
            WorkoutSession(
                id = "s1",
                name = "Hypertrophy Push Heavy",
                split = WorkoutSplit.PUSH,
                dateString = "2025-10-14",
                timestampFormatted = "Yesterday at 6:30 PM",
                durationMinutes = 58,
                exercises = listOf(
                    WorkoutExercise(
                        defaultExerciseLibrary[0],
                        listOf(WorkoutSet(1, 10, 60f, true), WorkoutSet(2, 10, 65f, true), WorkoutSet(3, 8, 70f, true))
                    ),
                    WorkoutExercise(
                        defaultExerciseLibrary[1],
                        listOf(WorkoutSet(1, 10, 22f, true), WorkoutSet(2, 10, 24f, true), WorkoutSet(3, 10, 24f, true))
                    )
                ),
                totalVolumeKg = 4280f,
                estimatedCaloriesBurned = 380
            )
        )
    }
}
