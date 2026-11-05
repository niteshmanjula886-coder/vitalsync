package com.simats.vitalsync.ui.ai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.simats.vitalsync.data.local.SessionManager
import com.simats.vitalsync.data.model.ai.AiChatSessionDto
import com.simats.vitalsync.data.model.ai.AiDailyInsightDto
import com.simats.vitalsync.data.model.ai.AiInsightResponse
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
    val sessions: List<AiChatSessionDto> = emptyList(),
    val currentSessionId: Int? = null,
    val currentSessionTitle: String = "New Chat",
    val isLoadingMessages: Boolean = false,
    val isLoadingSessions: Boolean = false,
    val isSending: Boolean = false,
    val isAiThinking: Boolean = false,
    val dailyInsight: AiDailyInsightDto? = null,
    val aiInsight: AiInsightResponse? = null,
    val isInsightLoading: Boolean = false,
    val errorMessage: String? = null,
    val activeModelLabel: String = "Qwen2.5 (1.5B) • Active"
)

class AiViewModel(
    application: Application,
    private val repository: AiRepository = AiRepository(sessionManager = SessionManager.getInstance(application))
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AiUiState())
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    init {
        loadDailyInsight()
        loadSessionsAndInitialChat()
    }

    private fun getCurrentTimeText(): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    }

    fun loadSessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSessions = true) }
            when (val result = repository.getSessions()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            sessions = result.data,
                            isLoadingSessions = false
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoadingSessions = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadSessionsAndInitialChat() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSessions = true, isLoadingMessages = true) }
            when (val sessResult = repository.getSessions()) {
                is Resource.Success -> {
                    val sessions = sessResult.data
                    val firstSession = sessions.firstOrNull()
                    val targetSessionId = firstSession?.id
                    val targetTitle = firstSession?.title ?: "New Chat"

                    _uiState.update {
                        it.copy(
                            sessions = sessions,
                            currentSessionId = targetSessionId,
                            currentSessionTitle = targetTitle,
                            isLoadingSessions = false
                        )
                    }

                    if (targetSessionId != null) {
                        loadChatMessages(targetSessionId)
                    } else {
                        _uiState.update { it.copy(isLoadingMessages = false, messages = emptyList()) }
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoadingSessions = false) }
                    loadChatMessages(null)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadChatMessages(sessionId: Int? = _uiState.value.currentSessionId) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMessages = true, errorMessage = null) }
            when (val result = repository.getChatMessages(sessionId = sessionId)) {
                is Resource.Success -> {
                    val resp = result.data
                    val dtoList = resp.messages
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
                            currentSessionId = resp.sessionId ?: sessionId,
                            currentSessionTitle = resp.sessionTitle ?: it.currentSessionTitle,
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

    fun selectSession(session: AiChatSessionDto) {
        if (_uiState.value.currentSessionId == session.id && _uiState.value.messages.isNotEmpty()) {
            return
        }
        _uiState.update {
            it.copy(
                currentSessionId = session.id,
                currentSessionTitle = session.title,
                messages = emptyList(),
                errorMessage = null
            )
        }
        loadChatMessages(session.id)
    }

    fun startNewChat() {
        _uiState.update {
            it.copy(
                currentSessionId = null,
                currentSessionTitle = "New Chat",
                messages = emptyList(),
                errorMessage = null
            )
        }
    }

    fun deleteSession(sessionId: Int) {
        viewModelScope.launch {
            when (repository.deleteSession(sessionId)) {
                is Resource.Success -> {
                    val wasActive = (_uiState.value.currentSessionId == sessionId)
                    loadSessions()
                    if (wasActive) {
                        startNewChat()
                    }
                }
                is Resource.Error -> {
                    loadSessions()
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

        val targetSessionId = _uiState.value.currentSessionId

        // Optimistically append user message to UI immediately and activate thinking state
        _isAiThinking.value = true
        _uiState.update {
            it.copy(
                messages = it.messages + optimisticUserMsg,
                isSending = true,
                isAiThinking = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                when (val result = repository.sendChatMessage(trimmed, sessionId = targetSessionId)) {
                    is Resource.Success -> {
                        val aiDto = result.data.aiResponse
                        val newSessionId = result.data.sessionId ?: targetSessionId
                        val newTitle = result.data.sessionTitle ?: _uiState.value.currentSessionTitle

                        val aiMsg = if (aiDto != null && aiDto.text.isNotBlank()) {
                            ChatMessage(
                                id = aiDto.id.ifEmpty { UUID.randomUUID().toString() },
                                sender = if (aiDto.sender.isNotBlank()) aiDto.sender.uppercase() else "AI",
                                text = aiDto.text,
                                timeText = aiDto.timeText.ifEmpty { getCurrentTimeText() }
                            )
                        } else {
                            ChatMessage(
                                id = UUID.randomUUID().toString(),
                                sender = "AI",
                                text = result.data.message ?: "VitalSync AI response received.",
                                timeText = getCurrentTimeText()
                            )
                        }

                        val modelLabel = aiDto?.modelUsed?.let { m ->
                            when {
                                m.contains("qwen", ignoreCase = true) -> "Qwen2.5 (1.5B) • Active"
                                m.contains("vitals", ignoreCase = true) -> "Live Vitals Sync • Grounded"
                                m.contains("rag", ignoreCase = true) -> "Verified RAG • Grounded"
                                m.contains("fallback", ignoreCase = true) -> "VitalSync AI • Resilient"
                                else -> "$m • Active"
                            }
                        } ?: "Qwen2.5 (1.5B) • Active"

                        _uiState.update {
                            it.copy(
                                messages = it.messages + aiMsg,
                                currentSessionId = newSessionId,
                                currentSessionTitle = newTitle,
                                activeModelLabel = modelLabel
                            )
                        }

                        // Refresh sessions list in background so new session or updated timestamp shows up
                        loadSessions()
                    }
                    is Resource.Error -> {
                        val errorMsg = result.message ?: "Failed to receive response from AI."
                        val errorNotice = ChatMessage(
                            id = UUID.randomUUID().toString(),
                            sender = "AI",
                            text = "⚠️ Unable to reach VitalSync AI: $errorMsg. Please ensure the local server is reachable and try again.",
                            timeText = getCurrentTimeText()
                        )
                        _uiState.update {
                            it.copy(
                                messages = it.messages + errorNotice,
                                errorMessage = errorMsg
                            )
                        }
                    }
                    is Resource.Loading -> {}
                }
            } finally {
                _isAiThinking.value = false
                _uiState.update {
                    it.copy(
                        isSending = false,
                        isAiThinking = false
                    )
                }
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

    fun fetchDynamicAiInsight(
        userId: Int? = null,
        isBtConnected: Boolean = false,
        hydrationMl: Int? = null,
        sleepText: String? = null,
        sleepScore: Int? = null,
        steps: Int? = null,
        targetWaterMl: Int? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isInsightLoading = true) }
            when (val result = repository.getAiInsight(
                userId = userId,
                isBtConnected = isBtConnected,
                hydrationMl = hydrationMl,
                sleepText = sleepText,
                sleepScore = sleepScore,
                steps = steps,
                targetWaterMl = targetWaterMl
            )) {
                is Resource.Success -> {
                    val insightResp = result.data
                    _uiState.update {
                        it.copy(
                            aiInsight = insightResp,
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
            val currSessionId = _uiState.value.currentSessionId
            if (currSessionId != null && currSessionId > 0) {
                repository.deleteSession(currSessionId)
                loadSessions()
                startNewChat()
            } else {
                repository.clearChatHistory()
                loadSessions()
                startNewChat()
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

