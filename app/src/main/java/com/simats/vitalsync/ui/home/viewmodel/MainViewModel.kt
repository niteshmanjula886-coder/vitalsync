package com.simats.vitalsync.ui.home.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simats.vitalsync.data.device.DeviceConnectionStatus
import com.simats.vitalsync.data.device.HealthDeviceManager
import com.simats.vitalsync.data.model.ai.AiInsightResponse
import com.simats.vitalsync.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceManager = HealthDeviceManager.getInstance(application)

    val isBluetoothConnected: StateFlow<Boolean> = deviceManager.connectionStatus
        .map { it == DeviceConnectionStatus.CONNECTED }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _aiInsight = MutableStateFlow<AiInsightResponse?>(null)
    val aiInsight: StateFlow<AiInsightResponse?> = _aiInsight.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentUserId: Int = 1

    init {
        // Observe Bluetooth connection status and trigger real-time AI Insight updates
        viewModelScope.launch {
            isBluetoothConnected.collect { connected ->
                fetchAiInsight(currentUserId, connected)
            }
        }
    }

    fun setUserId(userId: Int) {
        if (userId > 0 && userId != currentUserId) {
            currentUserId = userId
            fetchAiInsight(currentUserId, isBluetoothConnected.value)
        }
    }

    fun fetchAiInsight(
        userId: Int = currentUserId,
        isBtConnected: Boolean = isBluetoothConnected.value,
        hydrationMl: Int? = null,
        sleepText: String? = null,
        sleepScore: Int? = null,
        steps: Int? = null,
        targetWaterMl: Int? = null
    ) {
        currentUserId = userId
        viewModelScope.launch {
            _isLoading.value = true
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
                    _aiInsight.value = response.body()
                }
            } catch (e: Exception) {
                // Silently keep previous state or fallback
            } finally {
                _isLoading.value = false
            }
        }
    }
}

