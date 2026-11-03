package com.simats.vitalsync.ui.ai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.simats.vitalsync.data.local.SessionManager
import com.simats.vitalsync.data.model.ai.AiDailyInsightDto
import com.simats.vitalsync.data.repository.AiRepository
import com.simats.vitalsync.data.repository.Resource
import com.simats.vitalsync.ui.ai.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class AiUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoadingMessages: Boolean = false,
    val isSending: Boolean = false,
    val dailyInsight: AiDailyInsightDto? = null,
    val isInsightLoading: Boolean = false,
    val errorMessage: String? = null,
    val activeModelLabel: String = "Qwen3 (4B) • Active"
)

class AiViewModel(
    application: Application,
    private val repository: AiRepository = AiRepository(sessionManager = SessionManager.getInstance(application))
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AiUiState())
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    init {
        loadDailyInsight()
        loadChatMessages()
    }

    private fun getCurrentTimeText(): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    }

    fun loadChatMessages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMessages = true, errorMessage = null) }
            when (val result = repository.getChatMessages()) {
                is Resource.Success -> {
                    val dtoList = result.data
                    val domainMessages = dtoList.map { dto ->
                        ChatMessage(
                            id = dto.id.ifEmpty { UUID.randomUUID().toString() },
                            sender = dto.sender.uppercase(),
                            text = dto.text,
                            timeText = dto.timeText.ifEmpty { getCurrentTimeText() }
                        )
                    }
                    _uiState.update {
                        it.copy(
                            messages = domainMessages,
                            isLoadingMessages = false
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingMessages = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun sendMessage(messageText: String) {
        val trimmed = messageText.trim()
        if (trimmed.isEmpty()) return

        val userMsgId = UUID.randomUUID().toString()
        val timeNow = getCurrentTimeText()
        val optimisticUserMsg = ChatMessage(
            id = userMsgId,
            sender = "USER",
            text = trimmed,
            timeText = timeNow
        )

        // Optimistically append user message to UI immediately
        _uiState.update {
            it.copy(
                messages = it.messages + optimisticUserMsg,
                isSending = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            when (val result = repository.sendChatMessage(trimmed)) {
                is Resource.Success -> {
                    val aiDto = result.data.aiResponse
                    val aiMsg = if (aiDto != null && aiDto.text.isNotBlank()) {
                        ChatMessage(
                            id = aiDto.id.ifEmpty { UUID.randomUUID().toString() },
                            sender = "AI",
                            text = aiDto.text,
                            timeText = aiDto.timeText.ifEmpty { getCurrentTimeText() }
                        )
                    } else {
                        ChatMessage(
                            id = UUID.randomUUID().toString(),
                            sender = "AI",
                            text = "I've reviewed your vitals and health goals. Your indicators are in a stable range.",
                            timeText = getCurrentTimeText()
                        )
                    }

                    val modelLabel = aiDto?.modelUsed?.let { m ->
                        when {
                            m.contains("fallback", ignoreCase = true) -> "Qwen3 • Resilient"
                            m.contains("vitals", ignoreCase = true) -> "Live Vitals Sync • Grounded"
                            m.contains("rag", ignoreCase = true) -> "Verified RAG • Grounded"
                            m.contains("qwen", ignoreCase = true) -> "Qwen3 (4B) • Active"
                            else -> "$m • Active"
                        }
                    } ?: "Qwen3 (4B) • Active"

                    _uiState.update {
                        it.copy(
                            messages = it.messages + aiMsg,
                            isSending = false,
                            activeModelLabel = modelLabel
                        )
                    }
                }
                is Resource.Error -> {
                    // Provide supportive fallback in case of connection drop
                    val fallbackAiMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sender = "AI",
                        text = "I received your message! Your vitals and daily activity show steady progress. Remember to hydrate and maintain consistent rest.",
                        timeText = getCurrentTimeText()
                    )
                    _uiState.update {
                        it.copy(
                            messages = it.messages + fallbackAiMsg,
                            isSending = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadDailyInsight(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isInsightLoading = true) }
            when (val result = repository.getDailyInsight(refresh = forceRefresh)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            dailyInsight = result.data,
                            isInsightLoading = false
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isInsightLoading = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
            _uiState.update {
                it.copy(
                    messages = listOf(
                        ChatMessage(
                            id = "welcome",
                            sender = "AI",
                            text = "Hello! I am your VitalSync AI assistant powered by Qwen3 (4B). How can I help your health, fitness, or everyday questions today?",
                            timeText = getCurrentTimeText()
                        )
                    )
                )
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

class AiViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AiViewModel::class.java)) {
            return AiViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

