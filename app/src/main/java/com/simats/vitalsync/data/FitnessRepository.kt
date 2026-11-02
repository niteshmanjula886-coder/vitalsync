package com.simats.vitalsync.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class ActivitySessionRecord(
    val activityType: String,
    val durationSeconds: Int,
    val distanceKm: Float,
    val caloriesBurned: Int,
    val dateString: String = "2025-10-15"
)

data class ActivityStatsSummary(
    val durationFormatted: String,
    val distanceKmFormatted: String,
    val caloriesFormatted: String
)

data class SleepRecord(
    val bedtime: String,
    val wakeTime: String,
    val durationMinutes: Int,
    val sleepScore: Int = 85,
    val dateString: String = "2025-10-15"
)

class FitnessRepository private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("vitalsync_prefs", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var INSTANCE: FitnessRepository? = null

        fun getInstance(context: Context): FitnessRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FitnessRepository(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun formatDurationDynamic(totalSecs: Int): String {
            if (totalSecs <= 0) return "0s"
            val hours = totalSecs / 3600
            val mins = (totalSecs % 3600) / 60
            val secs = totalSecs % 60

            return when {
                hours > 0 -> if (secs > 0) "${hours}h ${mins}m ${secs}s" else "${hours}h ${mins}m"
                mins > 0 -> if (secs > 0) "${mins}m ${secs}s" else "${mins}m"
                else -> "${secs}s"
            }
        }
    }

    // SLEEP RECORD PERSISTENCE
    fun getSleepRecord(dateStr: String): SleepRecord? {
        val jsonStr = prefs.getString("key_sleep_record_$dateStr", null) ?: return null
        return try {
            val obj = JSONObject(jsonStr)
            SleepRecord(
                bedtime = obj.getString("bedtime"),
                wakeTime = obj.getString("wakeTime"),
                durationMinutes = obj.getInt("durationMinutes"),
                sleepScore = obj.optInt("sleepScore", 85),
                dateString = obj.optString("dateString", dateStr)
            )
        } catch (_: Exception) {
            null
        }
    }

    fun saveSleepRecord(record: SleepRecord) {
        val obj = JSONObject().apply {
            put("bedtime", record.bedtime)
            put("wakeTime", record.wakeTime)
            put("durationMinutes", record.durationMinutes)
            put("sleepScore", record.sleepScore)
            put("dateString", record.dateString)
        }
        prefs.edit().putString("key_sleep_record_${record.dateString}", obj.toString()).apply()
    }

    // ACTIVITY SESSIONS PERSISTENCE
    fun getActivitySessionRecords(): List<ActivitySessionRecord> {
        val jsonStr = prefs.getString("key_activity_session_records", null) ?: return emptyList()
        val list = mutableListOf<ActivitySessionRecord>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ActivitySessionRecord(
                        activityType = obj.getString("activityType"),
                        durationSeconds = obj.getInt("durationSeconds"),
                        distanceKm = obj.getDouble("distanceKm").toFloat(),
                        caloriesBurned = obj.getInt("caloriesBurned"),
                        dateString = obj.optString("dateString", "2025-10-15")
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun saveActivitySessionRecord(record: ActivitySessionRecord) {
        val currentList = getActivitySessionRecords().toMutableList()
        currentList.add(0, record)

        val array = JSONArray()
        for (r in currentList) {
            val obj = JSONObject().apply {
                put("activityType", r.activityType)
                put("durationSeconds", r.durationSeconds)
                put("distanceKm", r.distanceKm)
                put("caloriesBurned", r.caloriesBurned)
                put("dateString", r.dateString)
            }
            array.put(obj)
        }
        prefs.edit().putString("key_activity_session_records", array.toString()).apply()
    }

    fun clearActivitySessionRecords() {
        prefs.edit().remove("key_activity_session_records").apply()
    }

    fun getActivityStatsByType(activityType: String): ActivityStatsSummary {
        val records = getActivitySessionRecords().filter { it.activityType.equals(activityType, ignoreCase = true) }
        if (records.isEmpty()) {
            return ActivityStatsSummary(
                durationFormatted = "0s",
                distanceKmFormatted = "0.0 km",
                caloriesFormatted = "0 kcal"
            )
        }
        val totalSecs = records.sumOf { it.durationSeconds }
        val totalKm = records.sumOf { it.distanceKm.toDouble() }.toFloat()
        val totalCal = records.sumOf { it.caloriesBurned }

        val distanceFormatted = when {
            totalKm <= 0f -> "0.0 km"
            totalKm < 0.1f -> String.format(Locale.US, "%.2f km", totalKm)
            else -> String.format(Locale.US, "%.1f km", totalKm)
        }

        return ActivityStatsSummary(
            durationFormatted = formatDurationDynamic(totalSecs),
            distanceKmFormatted = distanceFormatted,
            caloriesFormatted = "$totalCal kcal"
        )
    }

    // EXERCISE LIBRARY DATA
    val defaultExerciseLibrary = listOf(
        Exercise("e1", "Barbell Bench Press", "Chest", 60f, 10, "Keep shoulder blades retracted"),
        Exercise("e2", "Incline Dumbbell Press", "Upper Chest", 22f, 10, "Control full stretch at bottom"),
        Exercise("e3", "Overhead Shoulder Press", "Shoulders", 35f, 10, "Core tight, press overhead"),
        Exercise("e4", "Cable Triceps Pushdown", "Triceps", 22.5f, 12, "Elbows locked to sides"),
        Exercise("e5", "Dumbbell Lateral Raise", "Side Delts", 10f, 15, "Lead with elbows"),
        Exercise("e6", "Lat Pulldown", "Lats", 55f, 12, "Drive elbows down to hips"),
        Exercise("e7", "Seated Cable Row", "Middle Back", 50f, 12, "Squeeze shoulder blades"),
        Exercise("e8", "Barbell Biceps Curl", "Biceps", 25f, 12, "Strict form, no swinging"),
        Exercise("e9", "Barbell Squat", "Quads & Glutes", 80f, 8, "Break at knees and hips"),
        Exercise("e10", "Leg Press", "Quads", 120f, 10, "Full depth, don't lock knees"),
        Exercise("e11", "Romanian Deadlift", "Hamstrings", 70f, 10, "Hinge at hips, flat back"),
        Exercise("e12", "Jumping Jacks", "Cardio / Full Body", 0f, 45, "High intensity cardio"),
        Exercise("e13", "Burpees", "Full Body Power", 0f, 15, "Explosive movement"),
        Exercise("e14", "Mountain Climbers", "Core & Stamina", 0f, 30, "Keep hips level")
    )

    fun getExerciseLibrary(): List<Exercise> {
        val customList = loadCustomExercisesFromPrefs()
        return defaultExerciseLibrary + customList
    }

    fun addCustomExercise(exercise: Exercise) {
        val currentCustoms = loadCustomExercisesFromPrefs().toMutableList()
        currentCustoms.add(exercise)
        saveCustomExercisesToPrefs(currentCustoms)
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

    private fun saveCustomExercisesToPrefs(list: List<Exercise>) {
        val array = JSONArray()
        for (e in list) {
            val obj = JSONObject().apply {
                put("id", e.id)
                put("name", e.name)
                put("targetMuscle", e.targetMuscle)
                put("defaultWeightKg", e.defaultWeightKg)
                put("defaultReps", e.defaultReps)
                put("notes", e.notes)
            }
            array.put(obj)
        }
        prefs.edit().putString("key_custom_exercises", array.toString()).apply()
    }

    // NUTRITION & MEAL PERSISTENCE
    fun getNutritionGoal(): NutritionGoal {
        val jsonStr = prefs.getString("key_nutrition_goal", null) ?: return NutritionGoal()
        return try {
            val obj = JSONObject(jsonStr)
            NutritionGoal(
                age = obj.optInt("age", 28),
                weightKg = obj.optDouble("weightKg", 68.5).toFloat(),
                heightCm = obj.optDouble("heightCm", 175.0).toFloat(),
                activityLevel = obj.optString("activityLevel", "Moderately Active"),
                fitnessGoal = FitnessGoal.valueOf(obj.optString("fitnessGoal", FitnessGoal.MUSCLE_GAIN.name)),
                dietaryPreference = DietaryPreference.valueOf(obj.optString("dietaryPreference", DietaryPreference.NON_VEGETARIAN.name)),
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

    fun getMealEntriesForDate(dateStr: String): List<MealEntry> {
        val jsonStr = prefs.getString("key_meals_$dateStr", null) ?: return if (dateStr == "2025-10-15") getSampleMealsForToday() else emptyList()
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
        currentList.add(0, mealEntry)

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
