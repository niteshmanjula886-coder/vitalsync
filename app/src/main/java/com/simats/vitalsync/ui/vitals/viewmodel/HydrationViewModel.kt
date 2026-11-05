package com.simats.vitalsync.ui.vitals.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.vitalsync.data.model.ai.AiInsightResponse
import com.simats.vitalsync.data.model.hydration.HydrationAiInsightDto
import com.simats.vitalsync.data.model.hydration.HydrationLogDto
import com.simats.vitalsync.data.model.hydration.WeeklyHistoryItemDto
import com.simats.vitalsync.data.model.hydration.WeeklyHydrationDayDto
import com.simats.vitalsync.data.model.hydration.WeeklyHydrationSummaryDto
import com.simats.vitalsync.data.network.RetrofitClient
import com.simats.vitalsync.data.repository.HydrationRepository
import com.simats.vitalsync.data.repository.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HydrationUiState(
    val isLoading: Boolean = false,
    val date: String = "",
    val consumedWaterMl: Int = 1250,
    val dailyTargetMl: Int = 2000,
    val glassEquivalent: Float = 5.0f,
    val progressPercentage: Int = 62,
    val isReminderEnabled: Boolean = true,
    val logs: List<HydrationLogDto> = emptyList(),
    val weeklyHistory: List<WeeklyHydrationDayDto> = emptyList(),
    val weeklyAggregations: List<WeeklyHistoryItemDto> = emptyList(),
    val weeklySummary: WeeklyHydrationSummaryDto? = null,
    val aiInsight: HydrationAiInsightDto? = null,
    val aiInsightResponse: AiInsightResponse? = null,
    val lastLoggedAmountMl: Int = 0,
    val showToast: Boolean = false,
    val isActionLoading: Boolean = false,
    val errorMessage: String? = null
)

class HydrationViewModel(
    private val repository: HydrationRepository = HydrationRepository()
) : ViewModel() {

    private val _hydrationState = MutableStateFlow(HydrationUiState())
    val hydrationState: StateFlow<HydrationUiState> = _hydrationState.asStateFlow()

    fun loadHydration(email: String, date: String) {
        viewModelScope.launch {
            repository.getHydration(email, date).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _hydrationState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        val data = resource.data
                        _hydrationState.update {
                            it.copy(
                                isLoading = false,
                                date = date,
                                consumedWaterMl = data.consumedMl,
                                dailyTargetMl = data.targetMl,
                                glassEquivalent = data.glassEquivalent,
                                progressPercentage = data.progressPercent,
                                isReminderEnabled = data.remindersEnabled,
                                logs = data.logs,
                                weeklyHistory = data.weeklyHistory,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _hydrationState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
            }
        }
        loadWeeklyHistory(email)
    }

    fun loadWeeklyHistory(email: String, userId: Int? = null) {
        viewModelScope.launch {
            repository.getWeeklyHistory(userId = userId, email = email).collect { resource ->
                if (resource is Resource.Success) {
                    val historyData = resource.data
                    _hydrationState.update {
                        it.copy(
                            weeklyAggregations = historyData.weeklyHistory,
                            weeklySummary = historyData.summary,
                            aiInsight = historyData.aiInsight
                        )
                    }
                }
            }
        }
    }

    fun fetchAiInsight(
        userId: Int = 1,
        isBtConnected: Boolean = false,
        hydrationMl: Int? = null,
        sleepText: String? = null,
        sleepScore: Int? = null,
        steps: Int? = null,
        targetWaterMl: Int? = null
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.aiApiService.getAiInsight(
                    userId = userId,
                    isBtConnected = isBtConnected,
                    hydrationMl = hydrationMl,
                    sleepText = sleepText,
                    sleepScore = sleepScore,
                    steps = steps,
                    targetWaterMl = targetWaterMl
                )
                if (response.isSuccessful && response.body() != null) {
                    _hydrationState.update { it.copy(aiInsightResponse = response.body()) }
                }
            } catch (e: Exception) {
                // Keep existing insight
            }
        }
    }

    fun addWater(
        email: String,
        amountMl: Int,
        date: String,
        onSuccess: () -> Unit = {}
    ) {
        if (amountMl <= 0) return

        // 1. Optimistic UI update
        _hydrationState.update { current ->
            val newConsumed = (current.consumedWaterMl + amountMl).coerceAtMost(5000)
            val newRatio = (newConsumed.toFloat() / current.dailyTargetMl.toFloat()).coerceIn(0f, 1f)
            val newProgress = (newRatio * 100).toInt()
            val newGlasses = newConsumed / 250f

            val updatedWeekly = if (current.weeklyHistory.isNotEmpty()) {
                current.weeklyHistory.map { day ->
                    if (day.isToday || day.date == date) {
                        day.copy(consumedMl = newConsumed, ratio = newRatio)
                    } else {
                        day
                    }
                }
            } else {
                current.weeklyHistory
            }

            current.copy(
                consumedWaterMl = newConsumed,
                glassEquivalent = newGlasses,
                progressPercentage = newProgress,
                weeklyHistory = updatedWeekly,
                lastLoggedAmountMl = amountMl,
                showToast = true,
                errorMessage = null
            )
        }

        onSuccess()

        // 2. Synchronize with backend
        viewModelScope.launch {
            repository.logWater(email, amountMl, date).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _hydrationState.update { it.copy(isActionLoading = true) }
                    }
                    is Resource.Success -> {
                        val body = resource.data
                        _hydrationState.update { current ->
                            val updatedWeekly = if (current.weeklyHistory.isNotEmpty()) {
                                current.weeklyHistory.map { day ->
                                    if (day.isToday || day.date == date) {
                                        val ratio = if (body.targetMl > 0) {
                                            (body.totalConsumedMl.toFloat() / body.targetMl.toFloat()).coerceIn(0f, 1f)
                                        } else 0f
                                        day.copy(consumedMl = body.totalConsumedMl, ratio = ratio)
                                    } else {
                                        day
                                    }
                                }
                            } else {
                                current.weeklyHistory
                            }

                            current.copy(
                                isActionLoading = false,
                                consumedWaterMl = body.totalConsumedMl,
                                dailyTargetMl = body.targetMl,
                                glassEquivalent = body.glassEquivalent,
                                progressPercentage = body.progressPercent,
                                weeklyHistory = updatedWeekly,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _hydrationState.update {
                            it.copy(
                                isActionLoading = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun updateReminder(
        email: String,
        enabled: Boolean
    ) {
        // Optimistic update
        _hydrationState.update { it.copy(isReminderEnabled = enabled) }

        viewModelScope.launch {
            val target = _hydrationState.value.dailyTargetMl
            repository.updateSettings(email, target, enabled).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        _hydrationState.update {
                            it.copy(
                                isReminderEnabled = resource.data.remindersEnabled,
                                dailyTargetMl = resource.data.dailyTargetMl
                            )
                        }
                    }
                    is Resource.Error -> {
                        _hydrationState.update {
                            it.copy(errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun updateDailyTarget(
        email: String,
        targetMl: Int
    ) {
        if (targetMl <= 0) return
        val currentReminders = _hydrationState.value.isReminderEnabled

        _hydrationState.update {
            val newRatio = (it.consumedWaterMl.toFloat() / targetMl.toFloat()).coerceIn(0f, 1f)
            it.copy(
                dailyTargetMl = targetMl,
                progressPercentage = (newRatio * 100).toInt()
            )
        }

        viewModelScope.launch {
            repository.updateSettings(email, targetMl, currentReminders).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        _hydrationState.update {
                            it.copy(dailyTargetMl = resource.data.dailyTargetMl)
                        }
                    }
                    is Resource.Error -> {
                        _hydrationState.update { it.copy(errorMessage = resource.message) }
                    }
                }
            }
        }
    }

    fun resetHydration(
        email: String,
        date: String
    ) {
        // Optimistic reset
        _hydrationState.update { current ->
            val updatedWeekly = if (current.weeklyHistory.isNotEmpty()) {
                current.weeklyHistory.map { day ->
                    if (day.isToday || day.date == date) {
                        day.copy(consumedMl = null, ratio = null)
                    } else {
                        day
                    }
                }
            } else {
                current.weeklyHistory
            }

            current.copy(
                consumedWaterMl = 0,
                glassEquivalent = 0f,
                progressPercentage = 0,
                weeklyHistory = updatedWeekly,
                showToast = false,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            repository.resetHydration(email, date).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _hydrationState.update { it.copy(isActionLoading = true) }
                    }
                    is Resource.Success -> {
                        val body = resource.data
                        _hydrationState.update {
                            it.copy(
                                isActionLoading = false,
                                consumedWaterMl = body.totalConsumedMl,
                                glassEquivalent = body.glassEquivalent,
                                progressPercentage = body.progressPercent,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _hydrationState.update {
                            it.copy(
                                isActionLoading = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun dismissToast() {
        _hydrationState.update { it.copy(showToast = false) }
    }
}
