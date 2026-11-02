package com.simats.vitalsync.ui.vitals

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.simats.vitalsync.data.device.HealthDeviceManager
import com.simats.vitalsync.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class TrendDay(
    val day: String = "M",
    val date: String = "",
    val bpm: Int? = null,
    val maxBpm: Int? = null,
    val minBpm: Int? = null,
    val category: String = "empty", // "normal", "high", "low", "empty"
    val hasData: Boolean = false,
    val isToday: Boolean = false
)

data class HeartRateUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentBpm: Int? = null,
    val formattedTime: String = "--",
    val bpmStatus: String = "No data", // "Normal resting rhythm", "Elevated", "Low Rate", "No data"
    val isLiveStreaming: Boolean = false,
    val averageBpm: Int? = null,
    val highestBpm: Int? = null,
    val lowestBpm: Int? = null,
    val restingBpm: Int? = null,
    val trendDays: List<TrendDay> = emptyList(),
    val hasTrendData: Boolean = false,
    val aiInsight: String = "No readings recorded this week. Wear your smartwatch to track baseline trends."
)

class HeartRateViewModel(
    private val context: Context,
    private val deviceManager: HealthDeviceManager = HealthDeviceManager.getInstance(context)
) : ViewModel() {

    private val _uiState = MutableStateFlow(HeartRateUiState())
    val uiState: StateFlow<HeartRateUiState> = _uiState.asStateFlow()

    init {
        observeLiveTelemetry()
    }

    private fun observeLiveTelemetry() {
        viewModelScope.launch {
            deviceManager.liveMetrics.collect { metrics ->
                val hr = metrics.heartRate
                if (hr != null && hr > 0) {
                    val statusText = when {
                        hr < 60 -> "Low Rate (<60 BPM)"
                        hr in 60..90 -> "Normal resting rhythm"
                        hr in 91..100 -> "Moderate (91-100 BPM)"
                        else -> "Elevated (>100 BPM)"
                    }
                    _uiState.update { current ->
                        val newMax = if (current.highestBpm != null) maxOf(current.highestBpm, hr) else hr
                        val newMin = if (current.lowestBpm != null) minOf(current.lowestBpm, hr) else hr
                        current.copy(
                            currentBpm = hr,
                            formattedTime = "Just Now",
                            bpmStatus = statusText,
                            isLiveStreaming = true,
                            highestBpm = newMax,
                            lowestBpm = newMin
                        )
                    }
                }
            }
        }
    }

    fun fetchAnalytics(email: String, userId: Int? = null) {
        if (email.isBlank() && (userId == null || userId <= 0)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val api = RetrofitClient.vitalsApiService
                val response = api.getVitalAnalytics(userId = userId, email = email, timeframe = "week")
                if (response.isSuccessful && response.body()?.data != null) {
                    val data = response.body()!!.data!!

                    val days = data.trendDays.map { dto ->
                        TrendDay(
                            day = dto.day,
                            date = dto.date,
                            bpm = dto.bpm,
                            maxBpm = dto.maxBpm,
                            minBpm = dto.minBpm,
                            category = dto.category,
                            hasData = dto.hasData,
                            isToday = dto.isToday
                        )
                    }

                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            currentBpm = if (current.isLiveStreaming && current.currentBpm != null) current.currentBpm else data.currentBpm,
                            formattedTime = if (current.isLiveStreaming) "Just Now" else data.formattedTime,
                            bpmStatus = if (current.isLiveStreaming && current.currentBpm != null) current.bpmStatus else data.bpmStatus,
                            averageBpm = data.averageBpm,
                            highestBpm = if (current.highestBpm != null && data.highestBpm != null) maxOf(current.highestBpm, data.highestBpm) else (data.highestBpm ?: current.highestBpm),
                            lowestBpm = if (current.lowestBpm != null && data.lowestBpm != null) minOf(current.lowestBpm, data.lowestBpm) else (data.lowestBpm ?: current.lowestBpm),
                            restingBpm = data.restingBpm,
                            trendDays = days,
                            hasTrendData = data.hasTrendData,
                            aiInsight = data.aiInsight
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                Log.w("HeartRateViewModel", "Failed to fetch vital analytics: ${e.message}")
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}

class HeartRateViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HeartRateViewModel::class.java)) {
            return HeartRateViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

