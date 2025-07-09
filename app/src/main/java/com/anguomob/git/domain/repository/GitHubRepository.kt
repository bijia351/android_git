package com.anguomob.git.domain.repository

import androidx.paging.PagingData
import com.anguomob.git.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * GitHub 数据仓库接口
 * 定义所有数据操作的抽象方法
 */
interface GitHubRepository {
    
    // ========== 仓库相关操作 ==========
    
    /**
     * 搜索仓库（支持分页）
     */
    fun searchRepositories(
        query: String,
        sort: String? = null,
        order: String? = null
    ): Flow<PagingData<Repository>>
    
    /**
     * 获取热门仓库（支持分页）
     */
    fun getTrendingRepositories(): Flow<PagingData<Repository>>

    /**
     * 获取每日趋势仓库（支持分页）
     */
    fun getDailyTrendingRepositories(): Flow<PagingData<Repository>>

    /**
     * 获取每周趋势仓库（支持分页）
     */
    fun getWeeklyTrendingRepositories(): Flow<PagingData<Repository>>

    /**
     * 获取每月趋势仓库（支持分页）
     */
    fun getMonthlyTrendingRepositories(): Flow<PagingData<Repository>>
    
    /**
     * 按语言获取仓库（支持分页）
     */
    fun getRepositoriesByLanguage(language: String): Flow<PagingData<Repository>>
    
    /**
     * 获取仓库详情
     */
    suspend fun getRepositoryDetails(owner: String, repo: String): Result<Repository>
    
    /**
     * 获取仓库贡献者
     */
    suspend fun getRepositoryContributors(
        owner: String, 
        repo: String, 
        page: Int = 1
    ): Result<List<User>>
    
    /**
     * 获取仓库语言统计
     */
    suspend fun getRepositoryLanguages(owner: String, repo: String): Result<Map<String, Int>>
    
    // ========== 主题相关操作 ==========
    
    /**
     * 搜索主题（支持分页）
     */
    fun searchTopics(query: String): Flow<PagingData<Topic>>
    
    /**
     * 获取热门主题
     */
    suspend fun getTrendingTopics(): Result<List<Topic>>

    /**
     * Get a single topic by its name
     */
    suspend fun getTopicByName(name: String): Result<Topic?>
    
    /**
     * 获取精选主题
     */
    suspend fun getFeaturedTopics(): Result<List<Topic>>

    /**
     * 获取主题活跃度统计
     */
    suspend fun getTopicActivityStats(topic: String): Result<List<Int>>
    
    /**
     * 按主题获取仓库
     */
    fun getRepositoriesByTopic(topic: String): Flow<PagingData<Repository>>
    
    // ========== 用户相关操作 ==========
    
    /**
     * 搜索用户（支持分页）
     */
    fun searchUsers(
        query: String,
        sort: String? = null,
        order: String? = null
    ): Flow<PagingData<User>>
    
    /**
     * 获取用户详情
     */
    suspend fun getUserDetails(username: String): Result<User>
    
    /**
     * 获取用户仓库
     */
    suspend fun getUserRepositories(
        username: String,
        type: String = "all",
        sort: String = "updated",
        page: Int = 1
    ): Result<List<Repository>>
    
    // ========== 搜索历史管理 ==========
    
    /**
     * 保存搜索历史
     */
    suspend fun saveSearchHistory(query: String, type: SearchType)
    
    /**
     * 获取搜索历史
     */
    fun getSearchHistory(): Flow<List<SearchHistory>>
    
    /**
     * 获取按类型的搜索历史
     */
    suspend fun getSearchHistoryByType(type: SearchType): Result<List<SearchHistory>>
    
    /**
     * 清空搜索历史
     */
    suspend fun clearSearchHistory()
    
    /**
     * 删除特定搜索历史
     */
    suspend fun deleteSearchHistory(id: Long)
    
    // ========== 收藏管理 ==========
    
    /**
     * 添加收藏
     */
    suspend fun addFavorite(favorite: Favorite)
    
    /**
     * 删除收藏
     */
    suspend fun removeFavorite(itemId: String, type: FavoriteType)
    
    /**
     * 检查是否已收藏
     */
    suspend fun isFavorited(itemId: String, type: FavoriteType): Boolean
    
    /**
     * 获取所有收藏
     */
    fun getAllFavorites(): Flow<List<Favorite>>
    
    /**
     * 按类型获取收藏
     */
    fun getFavoritesByType(type: FavoriteType): Flow<List<Favorite>>

    /**
     * 获取收藏的主题
     */
    fun getFavoritedTopics(): Flow<List<Favorite>>
    
    /**
     * 搜索收藏
     */
    suspend fun searchFavorites(query: String): Result<List<Favorite>>
    
    // ========== AI 分析相关 ==========
    
    /**
     * 获取技术趋势分析
     */
    suspend fun getTechTrendsAnalysis(
        repositories: List<Repository>,
        topics: List<Topic>
    ): Result<ChatResponse>
    
    /**
     * 分析仓库
     */
    suspend fun analyzeRepository(repository: Repository): Result<ChatResponse>
    
    /**
     * 生成个人技术雷达
     */
    suspend fun generatePersonalRadar(
        user: User,
        repositories: List<Repository>
    ): Result<ChatResponse>

    /**
     * Get README summary
     */
    suspend fun getReadmeSummary(owner: String, repo: String, model: String): Result<ChatResponse>
    
    // ========== 缓存管理 ==========
    
    /**
     * 清空所有缓存
     */
    suspend fun clearAllCache()
    
    /**
     * 刷新缓存
     */
    suspend fun refreshCache()
}
