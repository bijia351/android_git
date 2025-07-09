package com.anguomob.git.data.repository

import androidx.paging.*
import com.anguomob.git.data.api.GitHubApiService
import com.anguomob.git.data.api.OpenRouterApiService
import com.anguomob.git.data.local.GitRadarDatabase
import com.anguomob.git.data.model.*
import com.anguomob.git.data.paging.*
import com.anguomob.git.domain.repository.GitHubRepository
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GitHub Repository 实现类
 * 整合网络请求和本地缓存
 */
@Singleton
class GitHubRepositoryImpl @Inject constructor(
    private val gitHubApiService: GitHubApiService,
    private val openRouterApiService: OpenRouterApiService,
    private val database: GitRadarDatabase,
    private val gson: Gson
) : GitHubRepository {
    
    companion object {
        private const val PAGE_SIZE = com.anguomob.git.util.Constants.PAGE_SIZE
    }
    
    // ========== 仓库相关操作 ==========
    
    override fun searchRepositories(
        query: String,
        sort: String?,
        order: String?
    ): Flow<PagingData<Repository>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                RepositorySearchPagingSource(
                    gitHubApiService = gitHubApiService,
                    query = query,
                    sort = sort,
                    order = order
                )
            }
        ).flow
    }
    
    override fun getTrendingRepositories(): Flow<PagingData<Repository>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                TrendingRepositoriesPagingSource(
                    gitHubApiService = gitHubApiService
                )
            }
        ).flow
    }

    override fun getDailyTrendingRepositories(): Flow<PagingData<Repository>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.format(calendar.time)
        val query = "created:>$date"

        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                DailyTrendingPagingSource(
                    gitHubApiService = gitHubApiService,
                    query = query
                )
            }
        ).flow
    }

    override fun getWeeklyTrendingRepositories(): Flow<PagingData<Repository>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, -1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.format(calendar.time)
        val query = "created:>$date"

        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                // We can reuse DailyTrendingPagingSource as it's generic enough
                DailyTrendingPagingSource(
                    gitHubApiService = gitHubApiService,
                    query = query
                )
            }
        ).flow
    }

    override fun getMonthlyTrendingRepositories(): Flow<PagingData<Repository>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.format(calendar.time)
        val query = "created:>$date"

        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                MonthlyTrendingPagingSource(
                    gitHubApiService = gitHubApiService,
                    query = query
                )
            }
        ).flow
    }
    
    override fun getRepositoriesByLanguage(language: String): Flow<PagingData<Repository>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                LanguageRepositoriesPagingSource(
                    gitHubApiService = gitHubApiService,
                    language = language
                )
            }
        ).flow
    }
    
    override suspend fun getRepositoryDetails(owner: String, repo: String): Result<Repository> {
        return try {
            val repository = gitHubApiService.getRepository(owner, repo)
            // 缓存到本地数据库
            database.repositoryDao().insertRepository(repository)
            Result.success(repository)
        } catch (e: Exception) {
            // 如果网络失败，尝试从本地缓存获取（需要通过owner/repo查找）
            // 注意：这里需要改进，因为我们没有直接的owner/repo到ID的映射
            Result.failure(e)
        }
    }
    
    override suspend fun getRepositoryContributors(
        owner: String,
        repo: String,
        page: Int
    ): Result<List<User>> {
        return try {
            val contributors = gitHubApiService.getRepositoryContributors(owner, repo, page = page)
            Result.success(contributors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getRepositoryLanguages(owner: String, repo: String): Result<Map<String, Int>> {
        return try {
            val languages = gitHubApiService.getRepositoryLanguages(owner, repo)
            Result.success(languages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 主题相关操作 ==========
    
    override fun searchTopics(query: String): Flow<PagingData<Topic>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                TopicSearchPagingSource(
                    gitHubApiService = gitHubApiService,
                    query = query
                )
            }
        ).flow
    }
    
    override suspend fun getTrendingTopics(): Result<List<Topic>> {
        return try {
            // GitHub API 没有直接的趋势主题接口，可以通过搜索热门主题实现
            val response = gitHubApiService.searchTopics("is:featured", perPage = 20)
            val topics = response.items
            // 缓存到本地
            database.topicDao().insertTopics(topics)
            Result.success(topics)
        } catch (e: Exception) {
            // 尝试从缓存获取
            val cachedTopics = database.topicDao().getTrendingTopics(20)
            if (cachedTopics.isNotEmpty()) {
                Result.success(cachedTopics)
            } else {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getTopicByName(name: String): Result<Topic?> {
        return try {
            val response = gitHubApiService.searchTopics(name, perPage = 1)
            val topic = response.items.firstOrNull()
            Result.success(topic)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFeaturedTopics(): Result<List<Topic>> {
        return try {
            val response = gitHubApiService.searchTopics("is:featured")
            val topics = response.items
            database.topicDao().insertTopics(topics)
            Result.success(topics)
        } catch (e: Exception) {
            val cachedTopics = database.topicDao().getFeaturedTopics()
            if (cachedTopics.isNotEmpty()) {
                Result.success(cachedTopics)
            } else {
                Result.failure(e)
            }
        }
    }
    
    override fun getRepositoriesByTopic(topic: String): Flow<PagingData<Repository>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                TopicRepositoriesPagingSource(
                    gitHubApiService = gitHubApiService,
                    topic = topic
                )
            }
        ).flow
    }

    override suspend fun getTopicActivityStats(topic: String): Result<List<Int>> {
        return try {
            val calendar = Calendar.getInstance()
            val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, -30)
            val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            val query = "topic:$topic created:$startDate..$endDate"
            val response = gitHubApiService.getRepoCountForQuery(query)

            // This gives us the total count for the last 30 days, not daily.
            // To get daily counts, we would still need to make daily calls or process results client-side.
            // The API does not provide a way to get daily counts in a single call.
            // The best we can do is get the total for the last 30 days and distribute it, which is not accurate.
            // Or, we can get all the repos and group them by date client-side.
            // Let's try the second approach. It will be more accurate.

            val allRepos = mutableListOf<Repository>()
            var page = 1
            while (true) {
                val pagedResponse = gitHubApiService.searchRepositories(query, page = page, perPage = 100)
                allRepos.addAll(pagedResponse.items)
                if (pagedResponse.items.size < 100) {
                    break
                }
                page++
                delay(100) // Avoid rate limiting
            }

            val dailyCounts = IntArray(30)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = Calendar.getInstance()

            for (repo in allRepos) {
                try {
                    val createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(repo.createdAt)
                    val diff = today.time.time - createdAt.time
                    val days = (diff / (1000 * 60 * 60 * 24)).toInt()
                    if (days in 0..29) {
                        dailyCounts[29 - days]++
                    }
                } catch (e: Exception) {
                    // Ignore parsing errors
                }
            }

            Result.success(dailyCounts.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 用户相关操作 ==========
    
    override fun searchUsers(
        query: String,
        sort: String?,
        order: String?
    ): Flow<PagingData<User>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                UserSearchPagingSource(
                    gitHubApiService = gitHubApiService,
                    query = query,
                    sort = sort,
                    order = order
                )
            }
        ).flow
    }
    
    override suspend fun getUserDetails(username: String): Result<User> {
        return try {
            val user = gitHubApiService.getUser(username)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserRepositories(
        username: String,
        type: String,
        sort: String,
        page: Int
    ): Result<List<Repository>> {
        return try {
            val repositories = gitHubApiService.getUserRepositories(
                username = username,
                type = type,
                sort = sort,
                page = page
            )
            // 缓存用户仓库
            database.repositoryDao().insertRepositories(repositories)
            Result.success(repositories)
        } catch (e: Exception) {
            // 尝试从缓存获取
            val cachedRepos = database.repositoryDao().getRepositoriesByOwner(username)
            if (cachedRepos.isNotEmpty()) {
                Result.success(cachedRepos)
            } else {
                Result.failure(e)
            }
        }
    }
    
    // ========== 搜索历史管理 ==========
    
    override suspend fun saveSearchHistory(query: String, type: SearchType) {
        val searchHistory = SearchHistory(
            query = query,
            type = type
        )
        database.searchHistoryDao().insertSearchHistory(searchHistory)
    }
    
    override fun getSearchHistory(): Flow<List<SearchHistory>> {
        return database.searchHistoryDao().getAllSearchHistory()
    }
    
    override suspend fun getSearchHistoryByType(type: SearchType): Result<List<SearchHistory>> {
        return try {
            val history = database.searchHistoryDao().getSearchHistoryByType(type)
            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun clearSearchHistory() {
        database.searchHistoryDao().clearAllSearchHistory()
    }
    
    override suspend fun deleteSearchHistory(id: Long) {
        database.searchHistoryDao().deleteSearchHistory(id)
    }
    
    // ========== 收藏管理 ==========
    
    override suspend fun addFavorite(favorite: Favorite) {
        database.favoriteDao().insertFavorite(favorite)
    }
    
    override suspend fun removeFavorite(itemId: String, type: FavoriteType) {
        database.favoriteDao().deleteFavorite(itemId, type)
    }
    
    override suspend fun isFavorited(itemId: String, type: FavoriteType): Boolean {
        return database.favoriteDao().isFavorited(itemId, type)
    }
    
    override fun getAllFavorites(): Flow<List<Favorite>> {
        return database.favoriteDao().getAllFavorites()
    }
    
    override fun getFavoritesByType(type: FavoriteType): Flow<List<Favorite>> {
        return database.favoriteDao().getFavoritesByType(type)
    }

    override fun getFavoritedTopics(): Flow<List<Favorite>> {
        return database.favoriteDao().getFavoritesByType(FavoriteType.TOPIC)
    }
    
    override suspend fun searchFavorites(query: String): Result<List<Favorite>> {
        return try {
            val favorites = database.favoriteDao().searchFavorites(query)
            Result.success(favorites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== AI 分析相关 ==========
    
    override suspend fun getTechTrendsAnalysis(
        repositories: List<Repository>,
        topics: List<Topic>
    ): Result<ChatResponse> {
        return try {
            val data = buildString {
                appendLine("=== 仓库数据 ===")
                repositories.take(10).forEach { repo ->
                    appendLine("仓库: ${repo.fullName}")
                    appendLine("语言: ${repo.language}")
                    appendLine("星数: ${repo.stargazersCount}")
                    appendLine("描述: ${repo.description}")
                    appendLine()
                }
                
                appendLine("=== 主题数据 ===")
                topics.take(10).forEach { topic ->
                    appendLine("主题: ${topic.name}")
                    appendLine("描述: ${topic.description}")
                    appendLine()
                }
            }
            
            val response = openRouterApiService.analyzeTechTrends(
                data = data
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun analyzeRepository(repository: Repository): Result<ChatResponse> {
        return try {
            val repositoryData = gson.toJson(repository)
            val response = openRouterApiService.analyzeRepository(
                repositoryData = repositoryData
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun generatePersonalRadar(
        user: User,
        repositories: List<Repository>
    ): Result<ChatResponse> {
        return try {
            val userProfile = gson.toJson(user)
            val repositoriesData = gson.toJson(repositories.take(20)) // 限制数据量
            
            val response = openRouterApiService.generatePersonalRadar(
                userProfile = userProfile,
                repositories = repositoriesData
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReadmeSummary(owner: String, repo: String, model: String): Result<ChatResponse> {
        var attempts = 0
        while (attempts < 3) {
            Log.d("ReadmeSummary", "Attempt ${attempts + 1} for $owner/$repo with model $model")
            try {
                Log.d("ReadmeSummary", "Fetching README from GitHub...")
                val readme = gitHubApiService.getReadme(owner, repo)
                Log.d("ReadmeSummary", "Successfully fetched README. Original size: ${readme.content.length}")

                val decodedContent = String(Base64.decode(readme.content, Base64.DEFAULT))
                val truncatedContent = decodedContent.take(10000)
                Log.d("ReadmeSummary", "Truncated README content to ${truncatedContent.length} characters.")

                Log.d("ReadmeSummary", "Sending to OpenRouter for summarization...")
                val response = openRouterApiService.summarizeReadme(
                    readmeContent = truncatedContent,
                    model = model
                )
                Log.d("ReadmeSummary", "Successfully received summary from OpenRouter.")
                return Result.success(response)
            } catch (e: Exception) {
                Log.e("ReadmeSummary", "Error on attempt ${attempts + 1}", e)
                attempts++
                if (attempts >= 3) {
                    return Result.failure(e)
                }
                delay(2000L * attempts) // Exponential backoff
            }
        }
        return Result.failure(Exception("Failed after multiple attempts"))
    }
    
    // ========== 缓存管理 ==========
    
    override suspend fun clearAllCache() {
        GitRadarDatabase.clearAllData(database)
    }
    
    override suspend fun refreshCache() {
        // 实现缓存刷新逻辑
        // 可以根据需要清空特定缓存或更新过期数据
    }
}
