package com.simats.vitalsync.data.model.ai

import com.google.gson.annotations.SerializedName

data class AiChatSessionDto(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("title")
    val title: String = "New Chat",

    @SerializedName("message_count", alternate = ["messageCount"])
    val messageCount: Int = 0,

    @SerializedName("last_message", alternate = ["lastMessage"])
    val lastMessage: String = "",

    @SerializedName("created_at", alternate = ["createdAt"])
    val createdAt: String = "",

    @SerializedName("updated_at", alternate = ["updatedAt"])
    val updatedAt: String = ""
)

data class AiSessionListResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("count")
    val count: Int = 0,

    @SerializedName("sessions")
    val sessions: List<AiChatSessionDto> = emptyList(),

    @SerializedName("message")
    val message: String? = null
)

data class AiCreateSessionRequest(
    @SerializedName("user_id")
    val userId: Int? = null,

    @SerializedName("title")
    val title: String = "New Chat"
)

data class AiCreateSessionResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("session_id", alternate = ["sessionId"])
    val sessionId: Int = 0,

    @SerializedName("session")
    val session: AiChatSessionDto? = null,

    @SerializedName("message")
    val message: String? = null
)

data class AiBaseResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("session_id", alternate = ["sessionId"])
    val sessionId: Int? = null
)

data class AiChatMessageDto(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("session_id", alternate = ["sessionId"])
    val sessionId: Int? = null,

    @SerializedName("sender")
    val sender: String = "AI", // "USER" or "AI"

    @SerializedName("text")
    val text: String = "",

    @SerializedName("timeText", alternate = ["time_text"])
    val timeText: String = "",

    @SerializedName("model_used", alternate = ["modelUsed"])
    val modelUsed: String? = null
)

data class AiChatListResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("session_id", alternate = ["sessionId"])
    val sessionId: Int? = null,

    @SerializedName("session_title", alternate = ["sessionTitle"])
    val sessionTitle: String? = null,

    @SerializedName("count")
    val count: Int = 0,

    @SerializedName("messages")
    val messages: List<AiChatMessageDto> = emptyList(),

    @SerializedName("message")
    val message: String? = null
)

data class AiChatRequest(
    @SerializedName("user_id")
    val user_id: Int = 0,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("session_id", alternate = ["sessionId"])
    val sessionId: Int? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("message_text")
    val messageText: String = message
) {
    val userId: Int? get() = if (user_id > 0) user_id else null

    constructor(
        userId: Int?,
        sessionId: Int?,
        email: String?,
        messageText: String,
        message: String = messageText
    ) : this(
        user_id = userId ?: 0,
        message = message,
        sessionId = sessionId,
        email = email,
        messageText = messageText
    )
}

data class AiChatResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("reply")
    val reply: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("session_id", alternate = ["sessionId"])
    val sessionId: Int? = null,

    @SerializedName("session_title", alternate = ["sessionTitle"])
    val sessionTitle: String? = null,

    @SerializedName("user_message", alternate = ["userMessage"])
    val userMessage: AiChatMessageDto? = null,

    @SerializedName("ai_response", alternate = ["aiResponse"])
    val aiResponse: AiChatMessageDto? = null
)

data class AiDailyInsightDto(
    @SerializedName("id")
    val id: Long = 0L,

    @SerializedName("date")
    val date: String = "",

    @SerializedName("category")
    val category: String = "DAILY_OVERVIEW",

    @SerializedName("title")
    val title: String = "",

    @SerializedName("summary")
    val summary: String = "",

    @SerializedName("actionable_advice", alternate = ["actionableAdvice"])
    val actionableAdvice: String? = null,

    @SerializedName("wellness_score", alternate = ["wellnessScore"])
    val wellnessScore: Int = 85,

    @SerializedName("model_used", alternate = ["modelUsed"])
    val modelUsed: String? = null
)

data class AiDailyInsightsResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("cached")
    val cached: Boolean = false,

    @SerializedName("insight")
    val insight: AiDailyInsightDto? = null,

    @SerializedName("message")
    val message: String? = null
)

data class AiHistoryClearResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("deleted_count")
    val deletedCount: Int? = null
)

data class AiInsightData(
    @SerializedName("score")
    val score: Int = 80,

    @SerializedName("status_title", alternate = ["statusTitle", "title"])
    val statusTitle: String = "AI Health Insight",

    @SerializedName("summary")
    val summary: String = "",

    @SerializedName("action_tip", alternate = ["actionTip", "actionableAdvice", "actionable_advice"])
    val actionTip: String = "",

    @SerializedName("is_bt_connected", alternate = ["isBtConnected", "bluetoothConnected"])
    val isBtConnected: Boolean = false
)

data class AiInsightResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("score")
    val score: Int = 80,

    @SerializedName("status_title", alternate = ["statusTitle", "title"])
    val statusTitle: String = "AI Health Insight",

    @SerializedName("summary")
    val summary: String = "",

    @SerializedName("action_tip", alternate = ["actionTip", "actionableAdvice", "actionable_advice"])
    val actionTip: String = "",

    @SerializedName("is_bt_connected", alternate = ["isBtConnected", "bluetoothConnected"])
    val isBtConnected: Boolean = false,

    @SerializedName("model_used", alternate = ["modelUsed"])
    val modelUsed: String? = null,

    @SerializedName("insight")
    val insight: AiInsightData? = null,

    @SerializedName("message")
    val message: String? = null
)


data class ChatHistoryMessage(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("sender")
    val sender: String = "user", // "user" or "ai"

    @SerializedName("message")
    val message: String = "",

    @SerializedName("created_at")
    val createdAt: String = ""
)

data class ChatHistoryResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("messages")
    val messages: List<ChatHistoryMessage> = emptyList(),

    @SerializedName("message")
    val message: String? = null
)
