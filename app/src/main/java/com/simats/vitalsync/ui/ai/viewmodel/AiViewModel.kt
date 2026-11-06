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
import kotlinx.coroutines.Dispatchers
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
        loadChatHistory()
        loadSessions()
    }

    private fun getCurrentTimeText(): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    }

    fun loadChatHistory(userId: Int? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMessages = true, errorMessage = null) }
            val resolvedUserId = userId ?: repository.getCurrentUserId() ?: 1
            when (val result = repository.getChatHistory(resolvedUserId)) {
                is Resource.Success -> {
                    val historyMessages = result.data.map { dto ->
                        val timeStr = formatTimestampToTime(dto.createdAt)
                        ChatMessage(
                            id = dto.id.toString(),
                            sender = if (dto.sender.equals("user", ignoreCase = true)) "USER" else "AI",
                            text = dto.message,
                            timeText = timeStr
                        )
                    }
                    _uiState.update {
                        it.copy(
                            messages = historyMessages,
                            isLoadingMessages = false
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoadingMessages = false) }
                    loadSessionsAndInitialChat()
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun formatTimestampToTime(timestamp: String?): String {
        if (timestamp.isNullOrBlank()) return getCurrentTimeText()
        return try {
            val sdfIn = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val d = sdfIn.parse(timestamp)
            if (d != null) {
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(d)
            } else {
                timestamp
            }
        } catch (_: Exception) {
            timestamp
        }
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

    fun sendMessage(userId: Int, prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) return

        val userMsgId = UUID.randomUUID().toString()
        val timeNow = getCurrentTimeText()
        val optimisticUserMsg = ChatMessage(
            id = userMsgId,
            sender = "USER",
            text = trimmed,
            timeText = timeNow
        )

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

        viewModelScope.launch(Dispatchers.IO) {
            try {
                when (val result = repository.sendAiChatMessage(userId = userId, message = trimmed)) {
                    is Resource.Success -> {
                        val replyText = result.data.reply
                            ?: result.data.aiResponse?.text
                            ?: result.data.message
                            ?: "VitalSync AI response received."

                        val aiMsg = ChatMessage(
                            id = UUID.randomUUID().toString(),
                            sender = "AI",
                            text = replyText,
                            timeText = getCurrentTimeText()
                        )

                        _uiState.update {
                            it.copy(
                                messages = it.messages + aiMsg,
                                activeModelLabel = "Qwen2.5 (1.5B) • Active"
                            )
                        }

                        // Refresh sessions list in background
                        loadSessions()
                    }
                    is Resource.Error -> {
                        val errorMsg = result.message ?: "Failed to receive response from AI."
                        val errorNotice = ChatMessage(
                            id = UUID.randomUUID().toString(),
                            sender = "AI",
                            text = "⚠️ Unable to reach VitalSync AI: $errorMsg. Please ensure the local server and Ollama (qwen2.5:1.5b) are running.",
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

    fun sendMessage(messageText: String) {
        val resolvedUserId = repository.getCurrentUserId() ?: 1
        sendMessage(userId = resolvedUserId, prompt = messageText)
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

