package com.anguomob.git.data.api

import com.anguomob.git.BuildConfig
import com.anguomob.git.data.model.ChatRequest
import com.anguomob.git.data.model.ChatResponse
import retrofit2.http.*
import java.net.URLEncoder

/**
 * OpenRouter API 服务接口
 * 用于AI聊天和技术趋势分析
 */
interface OpenRouterApiService {
    
    companion object {
        const val BASE_URL = "https://openrouter.ai/api/v1/"
        const val CONTENT_TYPE = "application/json"
        
        // 推荐的免费模型
        const val FREE_MODEL_QWEN = "qwen/qwen3-30b-a3b:free"
        const val FREE_MODEL_GEMINI_FLASH = "google/gemini-2.0-flash-exp:free"
        const val FREE_MODEL_PHI3 = "microsoft/phi-3-mini-128k-instruct:free"
        const val FREE_MODEL_DEEPSEEK = "deepseek/deepseek-chat-v3-0324:free"

        // 付费模型
        const val PAID_MODEL_GPT4O = "openai/chatgpt-4o-latest"
        const val PAID_MODEL_DEEPSEEK = "deepseek/deepseek-chat"
        const val PAID_MODEL_GEMINI_PRO = "google/gemini-pro-1.5-exp"
    }
    
    /**
     * 发送聊天请求
     * POST /chat/completions
     */
    @POST("chat/completions")
    @Headers("Content-Type: application/json")
    suspend fun chat(
        @Header("Authorization") authorization: String = "Bearer ${BuildConfig.OPENROUTER_API_KEY}",
        @Body request: ChatRequest
    ): ChatResponse
    
    /**
     * 获取可用模型列表
     * GET /models
     */
    @GET("models")
    suspend fun getModels(
        @Header("Authorization") authorization: String
    ): ModelsResponse
    
    /**
     * 技术趋势分析专用聊天
     * 使用预设的系统提示词进行技术趋势分析
     */
    suspend fun analyzeTechTrends(
        data: String,
        model: String = FREE_MODEL_DEEPSEEK
    ): ChatResponse {
        val systemPrompt = """
            你是一个专业的技术趋势分析师，专门分析GitHub上的技术发展趋势。
            
            请根据提供的GitHub数据，进行深入的技术趋势分析，包括：
            1. 技术热点识别
            2. 增长趋势分析
            3. 技术生态评估
            4. 风险评估
            5. 发展建议
            
            请用中文回答，内容要专业、详细、有洞察力。
        """.trimIndent()
        
        val request = ChatRequest(
            model = model,
            messages = listOf(
                com.anguomob.git.data.model.ChatMessage("system", systemPrompt),
                com.anguomob.git.data.model.ChatMessage("user", data)
            ),
            maxTokens = 2000,
            temperature = 0.7f
        )
        
        return chat(
            request = request
        )
    }
    
    /**
     * 仓库分析专用聊天
     */
    suspend fun analyzeRepository(
        repositoryData: String,
        model: String = FREE_MODEL_DEEPSEEK
    ): ChatResponse {
        val systemPrompt = """
            你是一个专业的代码仓库分析师，请分析GitHub仓库的特点和价值。
            
            基于提供的仓库信息，请分析：
            1. 项目技术栈和架构
            2. 代码质量和活跃度
            3. 社区影响力
            4. 学习价值
            5. 发展前景
            
            请用中文回答，内容要客观、专业。
        """.trimIndent()
        
        val request = ChatRequest(
            model = model,
            messages = listOf(
                com.anguomob.git.data.model.ChatMessage("system", systemPrompt),
                com.anguomob.git.data.model.ChatMessage("user", repositoryData)
            ),
            maxTokens = 1500,
            temperature = 0.6f
        )
        
        return chat(
            request = request
        )
    }
    
    /**
     * 个人技术雷达分析
     */
    suspend fun generatePersonalRadar(
        userProfile: String,
        repositories: String,
        model: String = FREE_MODEL_DEEPSEEK
    ): ChatResponse {
        val systemPrompt = """
            你是一个个人技术发展顾问，请基于用户的GitHub活动数据生成个人技术雷达报告。
            
            请分析并生成：
            1. 技术技能评估
            2. 学习路径建议
            3. 趋势技术推荐
            4. 项目发展建议
            5. 职业发展方向
            
            请用中文回答，内容要个性化、实用。
        """.trimIndent()
        
        val request = ChatRequest(
            model = model,
            messages = listOf(
                com.anguomob.git.data.model.ChatMessage("system", systemPrompt),
                com.anguomob.git.data.model.ChatMessage("user", "用户资料：$userProfile\n\n仓库信息：$repositories")
            ),
            maxTokens = 2500,
            temperature = 0.8f
        )
        
        return chat(
            request = request
        )
    }

    /**
     * Summarize README
     */
    suspend fun summarizeReadme(
        readmeContent: String,
        model: String = FREE_MODEL_DEEPSEEK
    ): ChatResponse {
        val systemPrompt = """
            You are an expert in summarizing technical documents. Please summarize the following README.md file.
            The summary should be concise and highlight the key features, technologies, and purpose of the project.
        """.trimIndent()

        val request = ChatRequest(
            model = model,
            messages = listOf(
                com.anguomob.git.data.model.ChatMessage("system", systemPrompt),
                com.anguomob.git.data.model.ChatMessage("user", readmeContent)
            ),
            maxTokens = 500,
            temperature = 0.5f
        )

        return chat(
            request = request
        )
    }
}

/**
 * 模型列表响应
 */
data class ModelsResponse(
    val data: List<ModelInfo>
)

/**
 * 模型信息
 */
data class ModelInfo(
    val id: String,
    val name: String?,
    val description: String?,
    val pricing: ModelPricing?
)

/**
 * 模型定价信息
 */
data class ModelPricing(
    val prompt: String?,
    val completion: String?
)
