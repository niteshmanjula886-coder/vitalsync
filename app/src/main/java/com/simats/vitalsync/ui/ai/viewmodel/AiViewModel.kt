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
import com.simats.vitalsync.data.model.profile.WeeklyReportDto
import com.simats.vitalsync.data.network.ApiConstants
import com.simats.vitalsync.data.repository.AiRepository
import com.simats.vitalsync.data.repository.Resource
import com.simats.vitalsync.ui.ai.ChatMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.net.ConnectException
import java.net.SocketTimeoutException
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
    val isGenerating: Boolean = false,
    val dailyInsight: AiDailyInsightDto? = null,
    val aiInsight: AiInsightResponse? = null,
    val isInsightLoading: Boolean = false,
    val weeklyReport: WeeklyReportDto? = null,
    val isWeeklyTrendsLoading: Boolean = false,
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

    private var generationJob: Job? = null

    init {
        if (SessionManager.getInstance(application).isLoggedIn()) {
            // Launch optional AI daily insight & weekly trends asynchronously without blocking initial UI render
            loadDailyInsight()
            loadWeeklyTrends()
        }
    }

    private fun getCurrentTimeText(): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    }

    fun loadChatHistory(userId: Int? = null, email: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoadingMessages = true, errorMessage = null) }
            try {
                val resolvedUserId = userId ?: repository.getCurrentUserId()
                val resolvedEmail = email?.takeIf { it.isNotBlank() } ?: repository.getCurrentUserEmail()
                when (val result = withTimeout(8_000L) { repository.getChatHistory(userId = resolvedUserId, email = resolvedEmail) }) {
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
                        _uiState.update {
                            it.copy(
                                isLoadingMessages = false,
                                errorMessage = result.message
                            )
                        }
                    }
                    is Resource.Loading -> {}
                }
            } catch (_: SocketTimeoutException) {
                _uiState.update { it.copy(isLoadingMessages = false, errorMessage = ApiConstants.SERVER_UNREACHABLE_MESSAGE) }
            } catch (_: ConnectException) {
                _uiState.update { it.copy(isLoadingMessages = false, errorMessage = ApiConstants.SERVER_UNREACHABLE_MESSAGE) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingMessages = false, errorMessage = ApiConstants.resolveNetworkErrorMessage(e)) }
            } finally {
                _uiState.update { it.copy(isLoadingMessages = false) }
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

    fun loadSessions(userId: Int? = null, email: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoadingSessions = true) }
            try {
                val resolvedUserId = userId ?: repository.getCurrentUserId()
                val resolvedEmail = email?.takeIf { it.isNotBlank() } ?: repository.getCurrentUserEmail()
                when (val result = withTimeout(8_000L) { repository.getSessions(userId = resolvedUserId, email = resolvedEmail) }) {
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
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoadingSessions = false) }
            } finally {
                _uiState.update { it.copy(isLoadingSessions = false) }
            }
        }
    }

    fun loadSessionsAndInitialChat(userId: Int? = null, email: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoadingSessions = true, isLoadingMessages = true) }
            val resolvedUserId = userId ?: repository.getCurrentUserId()
            val resolvedEmail = email?.takeIf { it.isNotBlank() } ?: repository.getCurrentUserEmail()
            try {
                when (val sessResult = withTimeout(8_000L) { repository.getSessions(userId = resolvedUserId, email = resolvedEmail) }) {
                    is Resource.Success -> {
                        val sessions = sessResult.data
                        val activeId = _uiState.value.currentSessionId?.takeIf { id -> sessions.any { it.id == id } }
                        val targetSession = if (activeId != null) sessions.firstOrNull { it.id == activeId } else sessions.firstOrNull()
                        val targetSessionId = targetSession?.id
                        val targetTitle = targetSession?.title ?: "New Chat"

                        _uiState.update {
                            it.copy(
                                sessions = sessions,
                                currentSessionId = targetSessionId,
                                currentSessionTitle = targetTitle,
                                isLoadingSessions = false
                            )
                        }

                        if (targetSessionId != null) {
                            loadChatMessages(targetSessionId, userId = resolvedUserId, email = resolvedEmail)
                        } else {
                            loadChatHistory(userId = resolvedUserId, email = resolvedEmail)
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoadingSessions = false) }
                        loadChatHistory(userId = resolvedUserId, email = resolvedEmail)
                    }
                    is Resource.Loading -> {}
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoadingSessions = false, isLoadingMessages = false) }
            } finally {
                _uiState.update { it.copy(isLoadingSessions = false) }
            }
        }
    }

    fun loadChatMessages(
        sessionId: Int? = _uiState.value.currentSessionId,
        userId: Int? = null,
        email: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoadingMessages = true, errorMessage = null) }
            val resolvedUserId = userId ?: repository.getCurrentUserId()
            val resolvedEmail = email?.takeIf { it.isNotBlank() } ?: repository.getCurrentUserEmail()
            try {
                when (val result = withTimeout(8_000L) { repository.getChatMessages(sessionId = sessionId, userId = resolvedUserId, email = resolvedEmail) }) {
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
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoadingMessages = false) }
            } finally {
                _uiState.update { it.copy(isLoadingMessages = false) }
            }
        }
    }

    fun selectSession(session: AiChatSessionDto, userId: Int? = null, email: String? = null) {
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
        loadChatMessages(session.id, userId = userId, email = email)
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

    fun deleteSession(sessionId: Int, userId: Int? = null, email: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val resolvedUserId = userId ?: repository.getCurrentUserId()
            val resolvedEmail = email?.takeIf { it.isNotBlank() } ?: repository.getCurrentUserEmail()
            when (repository.deleteSession(sessionId, userId = resolvedUserId, email = resolvedEmail)) {
                is Resource.Success -> {
                    val wasActive = (_uiState.value.currentSessionId == sessionId)
                    loadSessions(userId = resolvedUserId, email = resolvedEmail)
                    if (wasActive) {
                        startNewChat()
                    }
                }
                is Resource.Error -> {
                    loadSessions(userId = resolvedUserId, email = resolvedEmail)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun sendMessage(userId: Int, prompt: String, email: String? = null) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) return

        val userMsgId = UUID.randomUUID().toString()
        val timeNow = getCurrentTimeText()
        val activeSessionId = _uiState.value.currentSessionId
        val resolvedUserId = if (userId > 0) userId else (repository.getCurrentUserId() ?: 0)
        val resolvedEmail = email?.takeIf { it.isNotBlank() } ?: repository.getCurrentUserEmail()

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
                isGenerating = true,
                errorMessage = null
            )
        }

        fun appendErrorBubble(rawError: String) {
            val formatted = if (rawError.startsWith("VitalSync AI", ignoreCase = true) ||
                rawError.startsWith("Unable to reach", ignoreCase = true)
            ) {
                rawError
            } else {
                "Unable to reach VitalSync AI: $rawError"
            }
            val errorNotice = ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = "AI",
                text = formatted,
                timeText = getCurrentTimeText()
            )
            _uiState.update {
                it.copy(
                    messages = it.messages + errorNotice,
                    errorMessage = rawError
                )
            }
        }

        generationJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                when (val result = withTimeout(180_000L) {
                    repository.sendAiChatMessage(
                        userId = resolvedUserId,
                        message = trimmed,
                        sessionId = activeSessionId,
                        email = resolvedEmail
                    )
                }) {
                    is Resource.Success -> {
                        val replyText = result.data.reply?.takeIf { it.isNotBlank() }
                            ?: result.data.message?.takeIf { it.isNotBlank() }
                            ?: result.data.aiResponse?.text?.takeIf { it.isNotBlank() }
                            ?: "VitalSync AI engine returned an empty response."

                        val returnedSessionId = result.data.sessionId ?: result.data.aiResponse?.sessionId ?: activeSessionId
                        val returnedSessionTitle = result.data.sessionTitle?.takeIf { it.isNotBlank() } ?: _uiState.value.currentSessionTitle

                        val aiMsg = ChatMessage(
                            id = result.data.aiResponse?.id?.ifBlank { null } ?: UUID.randomUUID().toString(),
                            sender = "AI",
                            text = replyText,
                            timeText = result.data.aiResponse?.timeText?.ifBlank { null } ?: getCurrentTimeText()
                        )

                        _uiState.update {
                            it.copy(
                                messages = it.messages + aiMsg,
                                currentSessionId = returnedSessionId,
                                currentSessionTitle = returnedSessionTitle,
                                activeModelLabel = "Qwen2.5 (1.5B) • Active"
                            )
                        }

                        // Refresh sessions list so drawer shows saved conversation immediately
                        loadSessions(userId = resolvedUserId.takeIf { id -> id > 0 }, email = resolvedEmail)
                    }
                    is Resource.Error -> {
                        appendErrorBubble(result.message ?: "Failed to receive response from AI.")
                    }
                    is Resource.Loading -> {}
                }
            } catch (ce: CancellationException) {
                val stoppedNotice = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = "AI",
                    text = "Generation stopped.",
                    timeText = getCurrentTimeText()
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + stoppedNotice,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                appendErrorBubble(ApiConstants.resolveNetworkErrorMessage(e))
            } finally {
                _isAiThinking.value = false
                generationJob = null
                _uiState.update {
                    it.copy(
                        isSending = false,
                        isAiThinking = false,
                        isGenerating = false
                    )
                }
            }
        }
    }

    fun stopGeneration() {
        generationJob?.cancel()
        generationJob = null
        _isAiThinking.value = false
        _uiState.update {
            it.copy(
                isSending = false,
                isAiThinking = false,
                isGenerating = false
            )
        }
    }

    fun editMessageAndResend(
        targetMessageIndex: Int,
        newText: String,
        explicitUserId: Int? = null,
        explicitEmail: String? = null
    ) {
        val currentMessages = _uiState.value.messages
        if (targetMessageIndex in currentMessages.indices) {
            val truncatedList = currentMessages.take(targetMessageIndex)
            _uiState.update { it.copy(messages = truncatedList) }
        }
        val resolvedUserId = explicitUserId?.takeIf { it > 0 } ?: repository.getCurrentUserId() ?: 0
        sendMessage(userId = resolvedUserId, prompt = newText, email = explicitEmail)
    }

    fun sendMessage(messageText: String, explicitUserId: Int? = null, explicitEmail: String? = null) {
        val resolvedUserId = explicitUserId?.takeIf { it > 0 } ?: repository.getCurrentUserId() ?: 0
        sendMessage(userId = resolvedUserId, prompt = messageText, email = explicitEmail)
    }

    fun loadDailyInsight(userId: Int? = null, forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isInsightLoading = true) }
            try {
                when (val result = withTimeout(6_000L) { repository.getDailyInsight(userId = userId, refresh = forceRefresh) }) {
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
            } catch (_: Exception) {
                _uiState.update { it.copy(isInsightLoading = false) }
            } finally {
                _uiState.update { it.copy(isInsightLoading = false) }
            }
        }
    }

    /**
     * Fetch aggregated 7-day telemetry for "Your weekly trends" on the AI screen.
     */
    fun loadWeeklyTrends(userId: Int? = null, email: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isWeeklyTrendsLoading = true) }
            try {
                when (val result = withTimeout(6_000L) { repository.getWeeklyReport(userId = userId, email = email) }) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                weeklyReport = result.data,
                                isWeeklyTrendsLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isWeeklyTrendsLoading = false) }
                    }
                    is Resource.Loading -> {}
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isWeeklyTrendsLoading = false) }
            } finally {
                _uiState.update { it.copy(isWeeklyTrendsLoading = false) }
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
        stepGoal: Int? = null,
        targetWaterMl: Int? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isInsightLoading = true) }
            try {
                when (val result = withTimeout(6_000L) {
                    repository.getAiInsight(
                        userId = userId,
                        isBtConnected = isBtConnected,
                        hydrationMl = hydrationMl,
                        sleepText = sleepText,
                        sleepScore = sleepScore,
                        steps = steps,
                        stepGoal = stepGoal,
                        targetWaterMl = targetWaterMl
                    )
                }) {
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
            } catch (_: Exception) {
                _uiState.update { it.copy(isInsightLoading = false) }
            } finally {
                _uiState.update { it.copy(isInsightLoading = false) }
            }
        }
    }

    fun clearChatHistory(userId: Int? = null, email: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val resolvedUserId = userId ?: repository.getCurrentUserId()
            val resolvedEmail = email?.takeIf { it.isNotBlank() } ?: repository.getCurrentUserEmail()
            val currSessionId = _uiState.value.currentSessionId
            if (currSessionId != null && currSessionId > 0) {
                repository.deleteSession(currSessionId, userId = resolvedUserId, email = resolvedEmail)
                loadSessions(userId = resolvedUserId, email = resolvedEmail)
                startNewChat()
            } else {
                repository.clearChatHistory(userId = resolvedUserId, email = resolvedEmail)
                loadSessions(userId = resolvedUserId, email = resolvedEmail)
                startNewChat()
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetState() {
        _uiState.value = AiUiState()
        _isAiThinking.value = false
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

