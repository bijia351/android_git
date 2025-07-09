package com.anguomob.git.data.model

import com.google.gson.annotations.SerializedName

/**
 * OpenRouter 聊天请求模型
 */
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    @SerializedName("max_tokens")
    val maxTokens: Int? = null,
    val temperature: Float? = null,
    @SerializedName("top_p")
    val topP: Float? = null,
    val stream: Boolean = false
)

/**
 * 聊天消息模型
 */
data class ChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

/**
 * OpenRouter 聊天响应模型
 */
data class ChatResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<ChatChoice>,
    val usage: ChatUsage?
)

/**
 * 聊天选择模型
 */
data class ChatChoice(
    val index: Int,
    val message: ChatMessage,
    @SerializedName("finish_reason")
    val finishReason: String?
)

/**
 * 使用统计模型
 */
data class ChatUsage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)

/**
 * 技术雷达分析结果
 */
data class TechRadarAnalysis(
    val id: String = "",
    val summary: String,
    val trendingTopics: List<TrendingTopic>,
    val recommendations: List<String>,
    val riskAssessment: List<RiskItem>,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 热门趋势主题
 */
data class TrendingTopic(
    val name: String,
    val description: String,
    val growthRate: Float,
    val category: String,
    val repositoryCount: Int,
    val starGrowth: Int
)

/**
 * 风险评估项
 */
data class RiskItem(
    val topic: String,
    val riskLevel: RiskLevel,
    val description: String,
    val recommendation: String
)

/**
 * 风险等级
 */
enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * AI 分析请求
 */
data class AnalysisRequest(
    val type: AnalysisType,
    val data: Map<String, Any>,
    val options: AnalysisOptions? = null
)

/**
 * 分析类型
 */
enum class AnalysisType {
    TECH_TRENDS,
    REPOSITORY_ANALYSIS,
    USER_PROFILE,
    TOPIC_SUMMARY
}

/**
 * 分析选项
 */
data class AnalysisOptions(
    val language: String = "zh-CN",
    val depth: String = "standard", // "brief", "standard", "detailed"
    val focus: List<String>? = null
) 