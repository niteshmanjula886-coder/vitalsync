package com.simats.vitalsync.ui.gym.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simats.vitalsync.data.Exercise
import com.simats.vitalsync.data.WorkoutExercise
import com.simats.vitalsync.data.WorkoutSession
import com.simats.vitalsync.data.WorkoutSet
import com.simats.vitalsync.data.local.SessionManager
import com.simats.vitalsync.data.model.gym.CreateExerciseRequest
import com.simats.vitalsync.data.model.gym.LogNutritionRequest
import com.simats.vitalsync.data.model.gym.SaveGymWorkoutRequest
import com.simats.vitalsync.data.model.gym.WorkoutExerciseDto
import com.simats.vitalsync.data.model.gym.createDefaultWorkoutExercises
import com.simats.vitalsync.data.model.gym.toSaveRequest
import com.simats.vitalsync.data.network.ApiConstants
import com.simats.vitalsync.data.repository.GymRepository
import com.simats.vitalsync.data.repository.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GymViewModel(
    private val repository: GymRepository
) : ViewModel() {

    // Modern 3-Tab Gym State Flow
    private val _uiState = MutableStateFlow(GymUiState())
    val uiState: StateFlow<GymUiState> = _uiState.asStateFlow()

    // Persistent split exercises map: splitName -> list of WorkoutExercise
    private val _splitExercises = MutableStateFlow<Map<String, List<WorkoutExercise>>>(emptyMap())
    val splitExercises: StateFlow<Map<String, List<WorkoutExercise>>> = _splitExercises.asStateFlow()

    private var prefs: SharedPreferences? = null
    private val gson = Gson()

    // Timer coroutine jobs
    private var workoutTimerJob: Job? = null
    private var restTimerJob: Job? = null

    // Legacy State Flows for backward compatibility with sub-screens
    private val _splitsState = MutableStateFlow(SplitsUiState())
    val splitsState: StateFlow<SplitsUiState> = _splitsState.asStateFlow()

    private val _exercisesState = MutableStateFlow(ExercisesUiState())
    val exercisesState: StateFlow<ExercisesUiState> = _exercisesState.asStateFlow()

    private val _addExerciseState = MutableStateFlow(AddExerciseUiState())
    val addExerciseState: StateFlow<AddExerciseUiState> = _addExerciseState.asStateFlow()

    private val _sessionsState = MutableStateFlow(WorkoutSessionsUiState())
    val sessionsState: StateFlow<WorkoutSessionsUiState> = _sessionsState.asStateFlow()

    private val _saveSessionState = MutableStateFlow(SaveWorkoutSessionUiState())
    val saveSessionState: StateFlow<SaveWorkoutSessionUiState> = _saveSessionState.asStateFlow()

    private val _progressState = MutableStateFlow(GymProgressUiState())
    val progressState: StateFlow<GymProgressUiState> = _progressState.asStateFlow()

    private var currentUserId: Int? = null
    private var currentUserEmail: String? = null

    // =========================================================================
    // Initialization & Local SharedPreferences Persistence (Issue 1, 2, 5)
    // =========================================================================

    fun initSession(context: Context, userId: Int?, email: String?) {
        currentUserId = userId
        currentUserEmail = email
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences("vitalsync_gym_preferences", Context.MODE_PRIVATE)
        }
        val p = prefs ?: return
        val sessionManager = SessionManager.getInstance(context)

        // 1. Load Custom Splits
        val savedSplitsSet = p.getStringSet("key_custom_splits", emptySet()) ?: emptySet()
        val customSplitsList = savedSplitsSet.toList()

        // 2. Load Protein Target Settings (Issue 5)
        val savedMode = p.getString("key_protein_mode", "BODY_WEIGHT") ?: "BODY_WEIGHT"
        val savedMult = p.getFloat("key_protein_multiplier", 1.8f)
        val savedCustomProt = p.getFloat("key_custom_protein", 140f)
        val savedCustomCal = p.getInt("key_custom_calories", 2200)

        val profileWeight = sessionManager.getUser()?.weight ?: 0f
        val savedWeight = p.getFloat("key_body_weight", if (profileWeight > 0f) profileWeight else 70f)
        val activeWeight = if (profileWeight > 0f) profileWeight else savedWeight

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        _uiState.update {
            it.copy(
                customSplits = customSplitsList,
                proteinTargetMode = savedMode,
                bodyWeightMultiplier = savedMult,
                customProteinTargetG = savedCustomProt,
                customCaloriesTarget = savedCustomCal,
                bodyWeightKg = activeWeight,
                selectedAnalyticsDate = todayStr
            )
        }

        // 3. Load & Initialize Split Exercises (Issue 1)
        val allSplits = listOf("Push", "Pull", "Legs", "Arms", "Cardio", "Abs") + customSplitsList
        val map = mutableMapOf<String, List<WorkoutExercise>>()
        allSplits.forEach { split ->
            val isInitKey = "isSplitInitialized_$split"
            val isInit = p.getBoolean(isInitKey, false)
            if (!isInit) {
                val defaults = createDefaultWorkoutExercises(split)
                map[split] = defaults
                saveSplitExercisesToPrefs(split, defaults)
                p.edit().putBoolean(isInitKey, true).apply()
            } else {
                val json = p.getString("split_exercises_$split", null)
                if (json != null) {
                    val type = object : TypeToken<List<WorkoutExercise>>() {}.type
                    val list: List<WorkoutExercise> = try {
                        gson.fromJson(json, type) ?: emptyList()
                    } catch (_: Exception) {
                        createDefaultWorkoutExercises(split)
                    }
                    map[split] = list
                } else {
                    map[split] = emptyList()
                }
            }
        }
        _splitExercises.value = map

        // 4. Load Remote Gym Data
        loadGymWorkouts(userId = userId, email = email, date = todayStr)
        loadGymNutrition(userId = userId, email = email, date = todayStr)
        loadGymAnalytics(userId = userId, email = email, date = todayStr)
    }

    private fun saveSplitExercisesToPrefs(split: String, list: List<WorkoutExercise>) {
        val p = prefs ?: return
        val json = gson.toJson(list)
        p.edit().putString("split_exercises_$split", json).apply()
    }

    // =========================================================================
    // 3-Tab Segment Navigation
    // =========================================================================

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex.coerceIn(0, 2)) }
    }

    // =========================================================================
    // Workouts Tab Logic (Splits, Timers, Sets, Metrics)
    // =========================================================================

    fun selectSplit(splitName: String) {
        _uiState.update { it.copy(selectedSplit = splitName) }
    }

    fun addCustomSplit(splitName: String) {
        val trimmed = splitName.trim()
        if (trimmed.isNotBlank()) {
            val current = _uiState.value.customSplits
            if (!current.contains(trimmed)) {
                val updated = current + trimmed
                _uiState.update { it.copy(customSplits = updated, selectedSplit = trimmed) }
                prefs?.edit()?.putStringSet("key_custom_splits", updated.toSet())?.apply()

                val currentMap = _splitExercises.value.toMutableMap()
                if (!currentMap.containsKey(trimmed)) {
                    currentMap[trimmed] = emptyList()
                    _splitExercises.value = currentMap
                    saveSplitExercisesToPrefs(trimmed, emptyList())
                    prefs?.edit()?.putBoolean("isSplitInitialized_$trimmed", true)?.apply()
                }
            } else {
                _uiState.update { it.copy(selectedSplit = trimmed) }
            }
        }
    }

    // Issue 2: Removable Custom Splits
    fun removeCustomSplit(splitName: String) {
        val current = _uiState.value.customSplits
        val updated = current.filter { !it.equals(splitName, ignoreCase = true) }
        val newSelected = if (_uiState.value.selectedSplit.equals(splitName, ignoreCase = true)) "Push" else _uiState.value.selectedSplit

        _uiState.update { it.copy(customSplits = updated, selectedSplit = newSelected) }
        prefs?.edit()?.putStringSet("key_custom_splits", updated.toSet())?.apply()
        prefs?.edit()?.remove("split_exercises_$splitName")?.remove("isSplitInitialized_$splitName")?.apply()

        val currentMap = _splitExercises.value.toMutableMap()
        currentMap.remove(splitName)
        _splitExercises.value = currentMap
    }

    // Issue 1: Split Exercise Management
    fun removeExerciseFromSplit(splitName: String, index: Int) {
        val currentMap = _splitExercises.value.toMutableMap()
        val list = (currentMap[splitName] ?: emptyList()).toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            currentMap[splitName] = list
            _splitExercises.value = currentMap
            saveSplitExercisesToPrefs(splitName, list)
        }
    }

    fun addExerciseToSplit(splitName: String, exercise: WorkoutExercise) {
        val currentMap = _splitExercises.value.toMutableMap()
        val list = (currentMap[splitName] ?: emptyList()).toMutableList()
        list.add(exercise)
        currentMap[splitName] = list
        _splitExercises.value = currentMap
        saveSplitExercisesToPrefs(splitName, list)
    }

    fun updateExerciseInSplit(splitName: String, index: Int, updated: WorkoutExercise) {
        val currentMap = _splitExercises.value.toMutableMap()
        val list = (currentMap[splitName] ?: emptyList()).toMutableList()
        if (index in list.indices) {
            list[index] = updated
            currentMap[splitName] = list
            _splitExercises.value = currentMap
            saveSplitExercisesToPrefs(splitName, list)
        }
    }

    // Issue 3: Atomic, Safe Set Addition & State Management
    fun addSetToExercise(splitName: String, exerciseIndex: Int) {
        val currentMap = _splitExercises.value.toMutableMap()
        val list = (currentMap[splitName] ?: emptyList()).toMutableList()
        if (exerciseIndex in list.indices) {
            val we = list[exerciseIndex]
            val lastSet = we.sets.lastOrNull()
            val nextNumber = we.sets.size + 1
            val newSet = WorkoutSet(
                setNumber = nextNumber,
                reps = lastSet?.reps ?: we.exercise.defaultReps,
                weightKg = lastSet?.weightKg ?: we.exercise.defaultWeightKg,
                isCompleted = false
            )
            list[exerciseIndex] = we.copy(sets = we.sets + newSet)
            currentMap[splitName] = list
            _splitExercises.value = currentMap
            saveSplitExercisesToPrefs(splitName, list)
        }
    }

    fun removeSetFromExercise(splitName: String, exerciseIndex: Int, setIndex: Int) {
        val currentMap = _splitExercises.value.toMutableMap()
        val list = (currentMap[splitName] ?: emptyList()).toMutableList()
        if (exerciseIndex in list.indices) {
            val we = list[exerciseIndex]
            if (we.sets.size > 1 && setIndex in we.sets.indices) {
                val newSets = we.sets.toMutableList().apply {
                    removeAt(setIndex)
                }.mapIndexed { idx, s -> s.copy(setNumber = idx + 1) }
                list[exerciseIndex] = we.copy(sets = newSets)
                currentMap[splitName] = list
                _splitExercises.value = currentMap
                saveSplitExercisesToPrefs(splitName, list)
            }
        }
    }

    fun updateSetInExercise(
        splitName: String,
        exerciseIndex: Int,
        setIndex: Int,
        weightKg: Float? = null,
        reps: Int? = null,
        isCompleted: Boolean? = null
    ) {
        val currentMap = _splitExercises.value.toMutableMap()
        val list = (currentMap[splitName] ?: emptyList()).toMutableList()
        if (exerciseIndex in list.indices) {
            val we = list[exerciseIndex]
            if (setIndex in we.sets.indices) {
                val currentSet = we.sets[setIndex]
                val updatedSet = currentSet.copy(
                    weightKg = weightKg ?: currentSet.weightKg,
                    reps = reps ?: currentSet.reps,
                    isCompleted = isCompleted ?: currentSet.isCompleted
                )
                val newSets = we.sets.toMutableList().apply {
                    this[setIndex] = updatedSet
                }
                list[exerciseIndex] = we.copy(sets = newSets)
                currentMap[splitName] = list
                _splitExercises.value = currentMap
                saveSplitExercisesToPrefs(splitName, list)
            }
        }
    }

    // Work and Volume-based MET Calorie Burn Formula
    // Scales dynamically with completed sets, volume moved, and active session time
    fun calculateEstimatedCalories(
        splitName: String,
        durationMinutes: Int = 1,
        completedSets: Int = 0,
        totalVolumeKg: Float = 0f
    ): Int {
        if (completedSets <= 0) return 0
        val splitLower = splitName.lowercase(Locale.US).trim()
        val met = when {
            splitLower.contains("cardio") -> 7.0f
            splitLower.contains("push") || splitLower.contains("pull") || splitLower.contains("leg") -> 5.5f
            splitLower.contains("arm") || splitLower.contains("ab") -> 4.5f
            else -> 5.0f
        }
        val weight = if (_uiState.value.bodyWeightKg > 0f) _uiState.value.bodyWeightKg else 70f
        val mins = durationMinutes.coerceAtLeast(1)

        val baseCaloriesPerSet = when {
            splitLower.contains("leg") -> 10.0f
            splitLower.contains("cardio") -> 12.0f
            splitLower.contains("push") || splitLower.contains("pull") -> 8.5f
            else -> 6.5f
        } * (weight / 70f)

        val setWorkCalories = completedSets * baseCaloriesPerSet
        val volumeCalories = (totalVolumeKg * 0.0025f)
        val timeBaseBurn = ((met * 3.5f * weight / 200f) * mins * 0.20f)

        return (setWorkCalories + volumeCalories + timeBaseBurn).toInt().coerceAtLeast(completedSets * 4)
    }

    fun resetSplitToDefault(splitName: String) {
        val currentMap = _splitExercises.value.toMutableMap()
        val defaults = createDefaultWorkoutExercises(splitName)
        currentMap[splitName] = defaults
        _splitExercises.value = currentMap
        saveSplitExercisesToPrefs(splitName, defaults)
        _uiState.update {
            it.copy(
                totalVolumeKg = 0f,
                totalSetsCompleted = 0,
                estimatedCaloriesBurned = 0
            )
        }
    }

    // Active Workout Timer Controls
    fun startWorkoutTimer() {
        if (_uiState.value.isWorkoutTimerRunning) return
        _uiState.update { it.copy(isWorkoutTimerRunning = true) }
        workoutTimerJob?.cancel()
        workoutTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _uiState.update { it.copy(workoutTimerSeconds = it.workoutTimerSeconds + 1) }
            }
        }
    }

    fun pauseWorkoutTimer() {
        workoutTimerJob?.cancel()
        workoutTimerJob = null
        _uiState.update { it.copy(isWorkoutTimerRunning = false) }
    }

    fun resetWorkoutTimer() {
        pauseWorkoutTimer()
        _uiState.update { it.copy(workoutTimerSeconds = 0L) }
    }

    // Issue 3: Rest Timer Controls (Preset, Start/Resume, Pause/Stop, Reset)
    fun setRestTimerPreset(seconds: Int) {
        pauseRestTimer()
        _uiState.update {
            it.copy(
                restTimerSeconds = seconds,
                initialRestTimerPreset = seconds,
                isRestTimerRunning = false
            )
        }
    }

    fun startRestTimer() {
        val currentSec = _uiState.value.restTimerSeconds
        val secToRun = if (currentSec > 0) currentSec else _uiState.value.initialRestTimerPreset
        if (secToRun <= 0) return

        _uiState.update { it.copy(restTimerSeconds = secToRun, isRestTimerRunning = true) }
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            while (_uiState.value.restTimerSeconds > 0) {
                delay(1000L)
                _uiState.update {
                    val next = it.restTimerSeconds - 1
                    it.copy(
                        restTimerSeconds = next,
                        isRestTimerRunning = next > 0
                    )
                }
            }
        }
    }

    fun pauseRestTimer() {
        restTimerJob?.cancel()
        restTimerJob = null
        _uiState.update { it.copy(isRestTimerRunning = false) }
    }

    fun resetRestTimer() {
        pauseRestTimer()
        _uiState.update {
            it.copy(
                restTimerSeconds = it.initialRestTimerPreset,
                isRestTimerRunning = false
            )
        }
    }

    fun resetRestTimerToReady() {
        pauseRestTimer()
        _uiState.update {
            it.copy(
                restTimerSeconds = 0,
                isRestTimerRunning = false
            )
        }
    }

    fun uncheckAllSetsInSplit(splitName: String) {
        val currentMap = _splitExercises.value.toMutableMap()
        val list = currentMap[splitName] ?: return
        val uncompletedList = list.map { we ->
            we.copy(
                sets = we.sets.map { st ->
                    st.copy(isCompleted = false)
                }
            )
        }
        currentMap[splitName] = uncompletedList
        _splitExercises.value = currentMap
        saveSplitExercisesToPrefs(splitName, uncompletedList)
    }

    fun updateLiveWorkoutMetrics(volumeKg: Float, setsCompleted: Int, caloriesBurned: Int) {
        _uiState.update {
            it.copy(
                totalVolumeKg = volumeKg,
                totalSetsCompleted = setsCompleted,
                estimatedCaloriesBurned = caloriesBurned
            )
        }
    }

    fun finishAndSaveWorkout(
        userId: Int? = null,
        email: String? = null,
        splitName: String,
        durationMins: Int,
        volumeKg: Float,
        totalSets: Int,
        caloriesBurned: Int,
        exercises: List<WorkoutExerciseDto> = emptyList(),
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingWorkout = true, workoutErrorMessage = null, saveWorkoutSuccessMessage = null) }
            try {
                // 1. Filter out unchecked / incomplete exercises and sets
                val completedExercises = exercises.map { exercise ->
                    exercise.copy(sets = exercise.sets.filter { it.isCompleted })
                }.filter { it.sets.isNotEmpty() }

                val finalTotalSets = completedExercises.sumOf { it.sets.size }
                val finalVolumeKg = completedExercises.sumOf { ex ->
                    ex.sets.sumOf { (it.weightKg * it.reps).toDouble() }
                }.toFloat()

                // 2. Calorie calculation right before saving the session
                val durationMin = maxOf(1, if (durationMins > 0) durationMins else (_uiState.value.workoutTimerSeconds / 60).toInt())
                val finalCalories = if (finalTotalSets > 0) {
                    if (caloriesBurned > 0) caloriesBurned else calculateEstimatedCalories(splitName, durationMin, finalTotalSets, finalVolumeKg)
                } else 0

                // Bind calculatedCalories to UI state
                _uiState.update {
                    it.copy(
                        estimatedCaloriesBurned = finalCalories,
                        totalVolumeKg = finalVolumeKg,
                        totalSetsCompleted = finalTotalSets
                    )
                }

                val req = SaveGymWorkoutRequest(
                    userId = userId,
                    email = email,
                    splitName = splitName,
                    durationMinutes = durationMin,
                    totalVolumeKg = finalVolumeKg,
                    totalSets = finalTotalSets,
                    caloriesBurned = finalCalories,
                    exercises = completedExercises
                )

                withTimeoutOrNull(60_000L) {
                    repository.saveGymWorkout(req).collect { res ->
                        when (res) {
                            is Resource.Loading -> _uiState.update { it.copy(isSavingWorkout = true) }
                            is Resource.Success -> {
                                // 1. Uncheck all sets in the current split
                                uncheckAllSetsInSplit(splitName)

                                // 2. Reset timers and summary metrics to zero
                                resetWorkoutTimer()
                                resetRestTimerToReady()

                                _uiState.update {
                                    it.copy(
                                        isSavingWorkout = false,
                                        saveWorkoutSuccessMessage = "Workout saved! Awesome job lifting ${volumeKg.toInt()} kg.",
                                        workoutErrorMessage = null,
                                        totalVolumeKg = 0f,
                                        totalSetsCompleted = 0,
                                        estimatedCaloriesBurned = 0,
                                        workoutTimerSeconds = 0L,
                                        isWorkoutTimerRunning = false,
                                        restTimerSeconds = 0,
                                        isRestTimerRunning = false
                                    )
                                }

                                // Refresh Today's workouts and analytics immediately (Issue 6)
                                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                                loadGymWorkouts(userId = userId, email = email, date = todayStr)
                                loadGymAnalytics(userId = userId, email = email, date = todayStr)
                                if (!email.isNullOrBlank()) {
                                    loadWorkoutSessions(email)
                                    loadGymProgress(email)
                                }
                                onSuccess?.invoke()
                            }
                            is Resource.Error -> {
                                _uiState.update {
                                    it.copy(
                                        isSavingWorkout = false,
                                        workoutErrorMessage = res.message ?: "Failed to save workout session"
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSavingWorkout = false,
                        workoutErrorMessage = ApiConstants.resolveNetworkErrorMessage(e)
                    )
                }
            } finally {
                _uiState.update { it.copy(isSavingWorkout = false) }
            }
        }
    }

    // Issue 6: Load & Delete Workout Sessions
    fun loadGymWorkouts(userId: Int? = null, email: String? = null, date: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorkoutsLoading = true) }
            try {
                withTimeoutOrNull(30_000L) {
                    repository.getGymWorkouts(userId = userId, email = email, date = date).collect { res ->
                        when (res) {
                            is Resource.Loading -> _uiState.update { it.copy(isWorkoutsLoading = true) }
                            is Resource.Success -> {
                                val filteredWorkouts = (res.data ?: emptyList()).map { workout ->
                                    val completedExercises = workout.exercises.map { exercise ->
                                        exercise.copy(sets = exercise.sets.filter { it.isCompleted })
                                    }.filter { it.sets.isNotEmpty() }
                                    workout.copy(exercises = completedExercises)
                                }
                                _uiState.update {
                                    it.copy(
                                        todayWorkouts = filteredWorkouts,
                                        isWorkoutsLoading = false
                                    )
                                }
                            }
                            is Resource.Error -> {
                                _uiState.update { it.copy(isWorkoutsLoading = false) }
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isWorkoutsLoading = false) }
            }
        }
    }

    fun deleteGymWorkout(workoutId: Int, userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            try {
                withTimeoutOrNull(30_000L) {
                    repository.deleteGymWorkout(id = workoutId, userId = userId, email = email).collect { res ->
                        when (res) {
                            is Resource.Loading -> {}
                            is Resource.Success -> {
                                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                                loadGymWorkouts(userId = userId, email = email, date = todayStr)
                                loadGymAnalytics(userId = userId, email = email, date = _uiState.value.selectedAnalyticsDate)
                            }
                            is Resource.Error -> {
                                _uiState.update { it.copy(workoutErrorMessage = res.message) }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(workoutErrorMessage = ApiConstants.resolveNetworkErrorMessage(e)) }
            }
        }
    }

    // =========================================================================
    // Nutrition Tab Logic (Issue 4, 5)
    // =========================================================================

    fun setProteinTargetMode(mode: String) {
        _uiState.update { it.copy(proteinTargetMode = mode) }
        prefs?.edit()?.putString("key_protein_mode", mode)?.apply()
        refreshNutritionWithCurrentTargets()
    }

    fun setBodyWeightMultiplier(multiplier: Float) {
        _uiState.update { it.copy(bodyWeightMultiplier = multiplier) }
        prefs?.edit()?.putFloat("key_protein_multiplier", multiplier)?.apply()
        refreshNutritionWithCurrentTargets()
    }

    fun setCustomTargets(proteinG: Float, calories: Int) {
        _uiState.update { it.copy(customProteinTargetG = proteinG, customCaloriesTarget = calories) }
        prefs?.edit()
            ?.putFloat("key_custom_protein", proteinG)
            ?.putInt("key_custom_calories", calories)
            ?.apply()
        refreshNutritionWithCurrentTargets()
    }

    fun setBodyWeight(weightKg: Float) {
        if (weightKg > 0f) {
            _uiState.update { it.copy(bodyWeightKg = weightKg) }
            prefs?.edit()?.putFloat("key_body_weight", weightKg)?.apply()
            refreshNutritionWithCurrentTargets()
        }
    }

    fun refreshNutritionWithCurrentTargets() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        loadGymNutrition(
            userId = currentUserId,
            email = currentUserEmail,
            date = todayStr,
            targetCalories = _uiState.value.activeCaloriesTarget,
            targetProtein = _uiState.value.activeProteinTargetG
        )
    }

    fun updateFoodInputs(
        mealType: String? = null,
        description: String? = null,
        calories: String? = null,
        protein: String? = null,
        carbs: String? = null,
        fats: String? = null
    ) {
        _uiState.update {
            it.copy(
                selectedMealType = mealType ?: it.selectedMealType,
                foodDescriptionInput = description ?: it.foodDescriptionInput,
                manualCaloriesInput = calories ?: it.manualCaloriesInput,
                manualProteinInput = protein ?: it.manualProteinInput,
                manualCarbsInput = carbs ?: it.manualCarbsInput,
                manualFatsInput = fats ?: it.manualFatsInput
            )
        }
    }

    fun analyzeAndLogNutrition(
        userId: Int? = null,
        email: String? = null,
        saveManually: Boolean = false,
        onSuccess: (() -> Unit)? = null
    ) {
        val state = _uiState.value
        val desc = state.foodDescriptionInput.trim()
        if (desc.isBlank()) {
            _uiState.update { it.copy(nutritionErrorMessage = "Please enter food name and quantity.") }
            return
        }

        val cal = state.manualCaloriesInput.trim().toIntOrNull()
        val prot = state.manualProteinInput.trim().toFloatOrNull()
        val carb = state.manualCarbsInput.trim().toFloatOrNull()
        val fat = state.manualFatsInput.trim().toFloatOrNull()

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAnalyzingNutrition = true,
                    nutritionErrorMessage = null,
                    nutritionSuccessMessage = null
                )
            }
            try {
                val req = LogNutritionRequest(
                    userId = userId,
                    email = email,
                    mealType = state.selectedMealType,
                    foodDescription = desc,
                    calories = cal,
                    proteinG = prot,
                    carbsG = carb,
                    fatsG = fat,
                    saveManually = saveManually
                )

                withTimeoutOrNull(60_000L) {
                    repository.analyzeAndLogNutrition(req).collect { res ->
                        when (res) {
                            is Resource.Loading -> _uiState.update { it.copy(isAnalyzingNutrition = true) }
                            is Resource.Success -> {
                                val msg = if (saveManually) "Meal saved manually!" else "Meal logged with AI macro analysis!"
                                _uiState.update {
                                    it.copy(
                                        isAnalyzingNutrition = false,
                                        foodDescriptionInput = "",
                                        manualCaloriesInput = "",
                                        manualProteinInput = "",
                                        manualCarbsInput = "",
                                        manualFatsInput = "",
                                        nutritionSuccessMessage = msg,
                                        nutritionErrorMessage = null
                                    )
                                }
                                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                                loadGymNutrition(userId = userId, email = email, date = todayStr)
                                loadWeeklyNutritionTrend(userId = userId, email = email, date = todayStr)
                                loadGymAnalytics(userId = userId, email = email, date = todayStr)
                                onSuccess?.invoke()
                            }
                            is Resource.Error -> {
                                _uiState.update {
                                    it.copy(
                                        isAnalyzingNutrition = false,
                                        nutritionErrorMessage = res.message ?: "Failed to log meal."
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAnalyzingNutrition = false,
                        nutritionErrorMessage = ApiConstants.resolveNetworkErrorMessage(e)
                    )
                }
            } finally {
                _uiState.update { it.copy(isAnalyzingNutrition = false) }
            }
        }
    }

    fun selectNutritionTrendMetric(metric: NutritionTrendMetric) {
        _uiState.update { it.copy(selectedWeeklyNutritionMetric = metric) }
    }

    fun selectWeeklyTrendDate(date: String?) {
        _uiState.update { it.copy(selectedWeeklyTrendDate = date) }
    }

    fun loadWeeklyNutritionTrend(
        userId: Int? = null,
        email: String? = null,
        date: String? = null,
        targetCalories: Int? = null,
        targetProtein: Float? = null,
        targetCarbs: Float? = null,
        targetFats: Float? = null
    ) {
        val uid = userId ?: currentUserId
        val em = email ?: currentUserEmail
        val activeCal = targetCalories ?: _uiState.value.activeCaloriesTarget
        val activeProt = targetProtein ?: _uiState.value.activeProteinTargetG
        val activeCarbs = targetCarbs ?: (activeCal * 0.45f / 4f)
        val activeFats = targetFats ?: (activeCal * 0.25f / 9f)

        viewModelScope.launch {
            _uiState.update { it.copy(isWeeklyNutritionLoading = true) }
            try {
                withTimeoutOrNull(30_000L) {
                    repository.getWeeklyNutritionTrend(
                        userId = uid,
                        email = em,
                        date = date,
                        targetCalories = activeCal,
                        targetProtein = activeProt,
                        targetCarbs = activeCarbs,
                        targetFats = activeFats
                    ).collect { res ->
                        when (res) {
                            is Resource.Loading -> _uiState.update { it.copy(isWeeklyNutritionLoading = true) }
                            is Resource.Success -> {
                                val trendData = res.data
                                _uiState.update { current ->
                                    val fallbackDate = trendData?.days?.find { it.isToday }?.date
                                        ?: trendData?.days?.lastOrNull()?.date
                                    current.copy(
                                        isWeeklyNutritionLoading = false,
                                        weeklyNutritionTrend = trendData,
                                        weeklyNutritionErrorMessage = null,
                                        selectedWeeklyTrendDate = current.selectedWeeklyTrendDate ?: fallbackDate
                                    )
                                }
                            }
                            is Resource.Error -> {
                                _uiState.update {
                                    it.copy(
                                        isWeeklyNutritionLoading = false,
                                        weeklyNutritionErrorMessage = res.message
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isWeeklyNutritionLoading = false,
                        weeklyNutritionErrorMessage = ApiConstants.resolveNetworkErrorMessage(e)
                    )
                }
            } finally {
                _uiState.update { it.copy(isWeeklyNutritionLoading = false) }
            }
        }
    }

    fun loadGymNutrition(
        userId: Int? = null,
        email: String? = null,
        date: String? = null,
        targetCalories: Int? = null,
        targetProtein: Float? = null,
        targetCarbs: Float? = null,
        targetFats: Float? = null
    ) {
        val uid = userId ?: currentUserId
        val em = email ?: currentUserEmail
        val activeCal = targetCalories ?: _uiState.value.activeCaloriesTarget
        val activeProt = targetProtein ?: _uiState.value.activeProteinTargetG
        val activeCarbs = targetCarbs ?: (activeCal * 0.45f / 4f)
        val activeFats = targetFats ?: (activeCal * 0.25f / 9f)

        loadWeeklyNutritionTrend(
            userId = uid,
            email = em,
            date = date,
            targetCalories = activeCal,
            targetProtein = activeProt,
            targetCarbs = activeCarbs,
            targetFats = activeFats
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isNutritionLoading = true) }
            try {
                withTimeoutOrNull(30_000L) {
                    repository.getGymNutrition(
                        userId = uid,
                        email = em,
                        date = date,
                        targetCalories = activeCal,
                        targetProtein = activeProt,
                        targetCarbs = activeCarbs,
                        targetFats = activeFats
                    ).collect { res ->
                        when (res) {
                            is Resource.Loading -> _uiState.update { it.copy(isNutritionLoading = true) }
                            is Resource.Success -> {
                                _uiState.update { current ->
                                    val updatedNutrition = res.data?.copy(
                                        targetCalories = activeCal,
                                        targetProtein = activeProt,
                                        targetCarbs = activeCarbs,
                                        targetFats = activeFats
                                    )
                                    current.copy(
                                        isNutritionLoading = false,
                                        todayNutrition = updatedNutrition,
                                        nutritionErrorMessage = null
                                    )
                                }
                            }
                            is Resource.Error -> {
                                _uiState.update {
                                    it.copy(
                                        isNutritionLoading = false,
                                        nutritionErrorMessage = res.message
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isNutritionLoading = false,
                        nutritionErrorMessage = ApiConstants.resolveNetworkErrorMessage(e)
                    )
                }
            } finally {
                _uiState.update { it.copy(isNutritionLoading = false) }
            }
        }
    }

    fun deleteGymNutrition(mealId: Int, userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            try {
                withTimeoutOrNull(30_000L) {
                    repository.deleteGymNutrition(id = mealId, userId = userId, email = email).collect { res ->
                        when (res) {
                            is Resource.Loading -> {}
                            is Resource.Success -> {
                                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                                loadGymNutrition(userId = userId, email = email, date = todayStr)
                                loadWeeklyNutritionTrend(userId = userId, email = email, date = todayStr)
                                loadGymAnalytics(userId = userId, email = email, date = _uiState.value.selectedAnalyticsDate)
                            }
                            is Resource.Error -> {
                                _uiState.update { it.copy(nutritionErrorMessage = res.message) }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(nutritionErrorMessage = ApiConstants.resolveNetworkErrorMessage(e)) }
            }
        }
    }

    // =========================================================================
    // Analytics Tab Logic (Issue 7 Day-to-Day)
    // =========================================================================

    fun selectAnalyticsDate(date: String, userId: Int? = null, email: String? = null) {
        _uiState.update { it.copy(selectedAnalyticsDate = date) }
        loadGymAnalytics(userId = userId, email = email, date = date)
    }

    fun changeAnalyticsDay(offsetDays: Int, userId: Int? = null, email: String? = null) {
        val currentStr = _uiState.value.selectedAnalyticsDate.ifBlank {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val curDate = try { sdf.parse(currentStr) ?: Date() } catch (_: Exception) { Date() }
        val cal = Calendar.getInstance().apply {
            time = curDate
            add(Calendar.DAY_OF_YEAR, offsetDays)
        }
        val newDateStr = sdf.format(cal.time)
        selectAnalyticsDate(newDateStr, userId = userId, email = email)
    }

    fun loadGymAnalytics(userId: Int? = null, email: String? = null, date: String? = null) {
        val targetDate = date ?: _uiState.value.selectedAnalyticsDate.ifBlank {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyticsLoading = true) }
            try {
                withTimeoutOrNull(30_000L) {
                    repository.getGymAnalytics(userId = userId, email = email, date = targetDate).collect { res ->
                        when (res) {
                            is Resource.Loading -> _uiState.update { it.copy(isAnalyticsLoading = true) }
                            is Resource.Success -> {
                                _uiState.update {
                                    it.copy(
                                        isAnalyticsLoading = false,
                                        analyticsData = res.data,
                                        analyticsErrorMessage = null
                                    )
                                }
                            }
                            is Resource.Error -> {
                                _uiState.update {
                                    it.copy(
                                        isAnalyticsLoading = false,
                                        analyticsErrorMessage = res.message
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAnalyticsLoading = false,
                        analyticsErrorMessage = ApiConstants.resolveNetworkErrorMessage(e)
                    )
                }
            } finally {
                _uiState.update { it.copy(isAnalyticsLoading = false) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                saveWorkoutSuccessMessage = null,
                workoutErrorMessage = null,
                nutritionSuccessMessage = null,
                nutritionErrorMessage = null,
                analyticsErrorMessage = null,
                globalMessage = null
            )
        }
    }

    // =========================================================================
    // Legacy Methods for Backward Compatibility
    // =========================================================================

    fun loadSplits() {
        viewModelScope.launch {
            _splitsState.value = SplitsUiState(isLoading = true)
            try {
                repository.getSplits().collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _splitsState.value = SplitsUiState(isLoading = true)
                        is Resource.Success -> _splitsState.value = SplitsUiState(splits = resource.data ?: emptyList())
                        is Resource.Error -> _splitsState.value = SplitsUiState(errorMessage = resource.message)
                    }
                }
            } catch (e: Exception) {
                _splitsState.value = SplitsUiState(errorMessage = ApiConstants.resolveNetworkErrorMessage(e))
            }
        }
    }

    fun loadExercises(email: String) {
        viewModelScope.launch {
            _exercisesState.value = ExercisesUiState(isLoading = true)
            try {
                repository.getExercises(email).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _exercisesState.value = ExercisesUiState(isLoading = true)
                        is Resource.Success -> _exercisesState.value = ExercisesUiState(exercises = resource.data ?: emptyList())
                        is Resource.Error -> _exercisesState.value = ExercisesUiState(errorMessage = resource.message)
                    }
                }
            } catch (e: Exception) {
                _exercisesState.value = ExercisesUiState(errorMessage = ApiConstants.resolveNetworkErrorMessage(e))
            }
        }
    }

    fun addCustomExercise(
        email: String,
        name: String,
        targetMuscle: String,
        defaultWeightKg: Float,
        defaultReps: Int,
        notes: String = ""
    ) {
        viewModelScope.launch {
            _addExerciseState.value = AddExerciseUiState(isLoading = true)
            try {
                val req = CreateExerciseRequest(
                    email = email,
                    name = name,
                    targetMuscle = targetMuscle,
                    defaultWeightKg = defaultWeightKg,
                    defaultReps = defaultReps,
                    notes = notes
                )
                repository.addCustomExercise(req).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _addExerciseState.value = AddExerciseUiState(isLoading = true)
                        is Resource.Success -> {
                            _addExerciseState.value = AddExerciseUiState(
                                isSuccess = true,
                                createdExercise = resource.data
                            )
                            loadExercises(email)
                        }
                        is Resource.Error -> _addExerciseState.value = AddExerciseUiState(errorMessage = resource.message)
                    }
                }
            } catch (e: Exception) {
                _addExerciseState.value = AddExerciseUiState(errorMessage = ApiConstants.resolveNetworkErrorMessage(e))
            }
        }
    }

    fun loadWorkoutSessions(email: String) {
        viewModelScope.launch {
            _sessionsState.value = WorkoutSessionsUiState(isLoading = true)
            try {
                repository.getWorkoutSessions(email).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _sessionsState.value = WorkoutSessionsUiState(isLoading = true)
                        is Resource.Success -> _sessionsState.value = WorkoutSessionsUiState(sessions = resource.data ?: emptyList())
                        is Resource.Error -> _sessionsState.value = WorkoutSessionsUiState(errorMessage = resource.message)
                    }
                }
            } catch (e: Exception) {
                _sessionsState.value = WorkoutSessionsUiState(errorMessage = ApiConstants.resolveNetworkErrorMessage(e))
            }
        }
    }

    fun saveWorkoutSession(session: WorkoutSession, email: String) {
        viewModelScope.launch {
            _saveSessionState.value = SaveWorkoutSessionUiState(isLoading = true)
            try {
                val req = session.toSaveRequest(email)
                repository.saveWorkoutSession(req).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _saveSessionState.value = SaveWorkoutSessionUiState(isLoading = true)
                        is Resource.Success -> {
                            _saveSessionState.value = SaveWorkoutSessionUiState(
                                isSuccess = true,
                                sessionId = resource.data?.sessionId
                            )
                            val split = when (session.split) {
                                com.simats.vitalsync.data.WorkoutSplit.PUSH -> "Push"
                                com.simats.vitalsync.data.WorkoutSplit.PULL -> "Pull"
                                com.simats.vitalsync.data.WorkoutSplit.LEGS -> "Legs"
                                else -> _uiState.value.selectedSplit
                            }
                            uncheckAllSetsInSplit(split)
                            resetWorkoutTimer()
                            resetRestTimerToReady()
                            _uiState.update {
                                it.copy(
                                    totalVolumeKg = 0f,
                                    totalSetsCompleted = 0,
                                    estimatedCaloriesBurned = 0,
                                    workoutTimerSeconds = 0L,
                                    isWorkoutTimerRunning = false,
                                    restTimerSeconds = 0,
                                    isRestTimerRunning = false
                                )
                            }
                            loadWorkoutSessions(email)
                            loadGymProgress(email)
                        }
                        is Resource.Error -> _saveSessionState.value = SaveWorkoutSessionUiState(errorMessage = resource.message)
                    }
                }
            } catch (e: Exception) {
                _saveSessionState.value = SaveWorkoutSessionUiState(errorMessage = ApiConstants.resolveNetworkErrorMessage(e))
            }
        }
    }

    fun loadGymProgress(email: String) {
        viewModelScope.launch {
            _progressState.value = GymProgressUiState(isLoading = true)
            try {
                repository.getGymProgress(email).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _progressState.value = GymProgressUiState(isLoading = true)
                        is Resource.Success -> _progressState.value = GymProgressUiState(progress = resource.data)
                        is Resource.Error -> _progressState.value = GymProgressUiState(errorMessage = resource.message)
                    }
                }
            } catch (e: Exception) {
                _progressState.value = GymProgressUiState(errorMessage = ApiConstants.resolveNetworkErrorMessage(e))
            }
        }
    }

    fun resetState() {
        workoutTimerJob?.cancel()
        restTimerJob?.cancel()
        _uiState.value = GymUiState()
        _splitsState.value = SplitsUiState()
        _exercisesState.value = ExercisesUiState()
        _addExerciseState.value = AddExerciseUiState()
        _sessionsState.value = WorkoutSessionsUiState()
        _saveSessionState.value = SaveWorkoutSessionUiState()
        _progressState.value = GymProgressUiState()
    }
}
