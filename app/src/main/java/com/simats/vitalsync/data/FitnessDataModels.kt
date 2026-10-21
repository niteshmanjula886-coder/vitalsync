package com.simats.vitalsync.data

enum class WorkoutSplit(val displayName: String, val description: String) {
    PUSH("Push", "Chest, Shoulders & Triceps"),
    PULL("Pull", "Back, Biceps & Rear Delts"),
    LEGS("Legs", "Quads, Hamstrings, Glutes & Calves"),
    UPPER_BODY("Upper Body", "Chest, Back, Shoulders & Arms"),
    LOWER_BODY("Lower Body", "Quads, Hamstrings, Glutes & Calves"),
    FULL_BODY("Full Body", "All major muscle groups"),
    CUSTOM("Custom Workout", "Custom exercise routine")
}

data class Exercise(
    val id: String,
    val name: String,
    val targetMuscle: String,
    val defaultWeightKg: Float = 20f,
    val defaultReps: Int = 10,
    val notes: String = ""
)

data class WorkoutSet(
    val setNumber: Int,
    val reps: Int,
    val weightKg: Float,
    val isCompleted: Boolean = false
)

data class WorkoutExercise(
    val exercise: Exercise,
    val sets: List<WorkoutSet>,
    val notes: String = ""
)

data class WorkoutSession(
    val id: String,
    val name: String,
    val split: WorkoutSplit,
    val dateString: String, // YYYY-MM-DD
    val timestampFormatted: String, // e.g., "Oct 15, 2025 at 9:30 AM"
    val durationMinutes: Int,
    val exercises: List<WorkoutExercise>,
    val totalVolumeKg: Float,
    val estimatedCaloriesBurned: Int
)

enum class FoodCategory {
    VEG,
    NON_VEG,
    EGG,
    VEGAN
}

data class FoodItem(
    val id: String,
    val name: String,
    val category: FoodCategory,
    val servingSizeAmount: Float,
    val servingUnit: String, // "egg", "100g", "250ml", "piece", "cup"
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float,
    val isCustom: Boolean = false
)

enum class MealCategory(val displayName: String, val icon: String) {
    BREAKFAST("Breakfast", "🍳"),
    LUNCH("Lunch", "🥗"),
    DINNER("Dinner", "🍲"),
    SNACKS("Snacks", "🍎")
}

data class MealEntry(
    val id: String,
    val category: MealCategory,
    val foodItem: FoodItem,
    val quantity: Float, // multiplier on servingSizeAmount
    val calculatedCalories: Int,
    val calculatedProtein: Float,
    val calculatedCarbs: Float,
    val calculatedFat: Float,
    val dateString: String // YYYY-MM-DD
)

enum class FitnessGoal(val displayName: String) {
    MUSCLE_GAIN("Muscle Gain"),
    FAT_LOSS("Fat Loss & Tone"),
    MAINTENANCE("Weight Maintenance"),
    GENERAL_FITNESS("General Health & Stamina")
}

enum class DietaryPreference(val displayName: String) {
    NON_VEGETARIAN("Non-Vegetarian"),
    VEGETARIAN("Vegetarian"),
    EGGITARIAN("Eggitarian"),
    VEGAN("Vegan")
}

data class NutritionGoal(
    val age: Int = 28,
    val weightKg: Float = 68.5f,
    val heightCm: Float = 175f,
    val activityLevel: String = "Moderately Active",
    val fitnessGoal: FitnessGoal = FitnessGoal.MUSCLE_GAIN,
    val dietaryPreference: DietaryPreference = DietaryPreference.NON_VEGETARIAN,
    val dailyCalorieTarget: Int = 2200,
    val dailyProteinTarget: Float = 130f,
    val dailyCarbsTarget: Float = 250f,
    val dailyFatTarget: Float = 65f
)
