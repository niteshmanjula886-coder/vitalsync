package com.simats.vitalsync.data.model.ai

import com.google.gson.annotations.SerializedName

data class AiChatMessageDto(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("sender")
    val sender: String = "AI", // "USER" or "AI"

    @SerializedName("text")
    val text: String = "",

    @SerializedName("timeText")
    val timeText: String = "",

    @SerializedName("model_used", alternate = ["modelUsed"])
    val modelUsed: String? = null
)

data class AiChatListResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("count")
    val count: Int = 0,

    @SerializedName("messages")
    val messages: List<AiChatMessageDto> = emptyList(),

    @SerializedName("message")
    val message: String? = null
)

data class AiChatRequest(
    @SerializedName("user_id")
    val userId: Int? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("message")
    val message: String
)

data class AiChatResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("user_message")
    val userMessage: AiChatMessageDto? = null,

    @SerializedName("ai_response")
    val aiResponse: AiChatMessageDto? = null,

    @SerializedName("message")
    val message: String? = null
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

