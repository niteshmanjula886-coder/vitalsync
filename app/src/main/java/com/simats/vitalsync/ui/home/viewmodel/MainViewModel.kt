package com.simats.vitalsync.ui.home.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simats.vitalsync.data.device.DeviceConnectionStatus
import com.simats.vitalsync.data.device.HealthDeviceManager
import com.simats.vitalsync.data.model.ai.AiChatRequest
import com.simats.vitalsync.data.model.ai.AiInsightResponse
import com.simats.vitalsync.data.network.ApiConstants
import com.simats.vitalsync.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.net.ConnectException
import java.net.SocketTimeoutException

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceManager = HealthDeviceManager.getInstance(application)

    val isBluetoothConnected: StateFlow<Boolean> = deviceManager.connectionStatus
        .map { it == DeviceConnectionStatus.CONNECTED }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _aiInsight = MutableStateFlow<AiInsightResponse?>(null)
    val aiInsight: StateFlow<AiInsightResponse?> = _aiInsight.asStateFlow()

    private val _qwenResponse = MutableStateFlow<String?>(null)
    val qwenResponse: StateFlow<String?> = _qwenResponse.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentUserId: Int = 1

    init {
        // Observe Bluetooth connection status and trigger non-blocking background AI Insight updates
        viewModelScope.launch(Dispatchers.IO) {
            isBluetoothConnected.collect { connected ->
                fetchAiInsight(currentUserId, connected)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
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
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val response = withTimeout(12_000L) {
                    RetrofitClient.aiApiService.getAiInsight(
                        userId = userId,
                        isBtConnected = isBtConnected,
                        hydrationMl = hydrationMl,
                        sleepText = sleepText,
                        sleepScore = sleepScore,
                        steps = steps,
                        targetWaterMl = targetWaterMl
                    )
                }
                if (response.isSuccessful && response.body() != null) {
                    _aiInsight.value = response.body()
                    _errorMessage.value = null
                } else if (response.code() >= 500) {
                    _errorMessage.value = ApiConstants.SERVER_UNREACHABLE_MESSAGE
                }
            } catch (_: SocketTimeoutException) {
                _errorMessage.value = ApiConstants.SERVER_UNREACHABLE_MESSAGE
            } catch (_: ConnectException) {
                _errorMessage.value = ApiConstants.SERVER_UNREACHABLE_MESSAGE
            } catch (e: Exception) {
                _errorMessage.value = ApiConstants.resolveNetworkErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(userId: Int = currentUserId, prompt: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isAiThinking.value = true
            try {
                val response = withTimeout(15_000L) {
                    RetrofitClient.aiApiService.sendAiChatMessage(
                        AiChatRequest(user_id = userId, message = prompt)
                    )
                }
                if (response.isSuccessful && response.body()?.success == true) {
                    val reply = response.body()?.reply
                    _qwenResponse.value = reply
                    if (!reply.isNullOrBlank()) {
                        val current = _aiInsight.value
                        _aiInsight.value = current?.copy(
                            summary = reply,
                            modelUsed = "qwen2.5:1.5b"
                        )
                    }
                    _errorMessage.value = null
                } else if (response.code() >= 500) {
                    _errorMessage.value = ApiConstants.SERVER_UNREACHABLE_MESSAGE
                }
            } catch (_: SocketTimeoutException) {
                _errorMessage.value = ApiConstants.SERVER_UNREACHABLE_MESSAGE
            } catch (_: ConnectException) {
                _errorMessage.value = ApiConstants.SERVER_UNREACHABLE_MESSAGE
            } catch (e: Exception) {
                _errorMessage.value = ApiConstants.resolveNetworkErrorMessage(e)
            } finally {
                _isAiThinking.value = false
            }
        }
    }
}

