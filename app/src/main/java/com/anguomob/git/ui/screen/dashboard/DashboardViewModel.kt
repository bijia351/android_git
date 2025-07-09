package com.anguomob.git.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anguomob.git.data.model.*
import com.anguomob.git.domain.repository.GitHubRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Dashboard 页面 ViewModel
 * 管理仓库数据、搜索、过滤等功能
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val gitHubRepository: GitHubRepository
) : ViewModel() {
    
    // UI 状态
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    // 搜索查询
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // 当前选择的语言筛选
    private val _selectedLanguage = MutableStateFlow<String?>(null)
    val selectedLanguage: StateFlow<String?> = _selectedLanguage.asStateFlow()

    // 当前选择的主题筛选
    private val _selectedTopic = MutableStateFlow<String?>(null)
    val selectedTopic: StateFlow<String?> = _selectedTopic.asStateFlow()
    
    // 排序选项
    private val _sortOption = MutableStateFlow(SortOption.STARS)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()
    
    // 分页数据流
    val repositories: Flow<PagingData<Repository>> = combine(
        searchQuery,
        selectedLanguage,
        selectedTopic,
        sortOption
    ) { query, language, topic, sort ->
        Quad(query, language, topic, sort)
    }.flatMapLatest { (query, language, topic, sort) ->
        val finalQuery = buildString {
            if (query.isNotBlank()) {
                append(query)
            }
            language?.let {
                if (isNotBlank()) append(" ")
                append("language:$it")
            }
            topic?.let {
                if (isNotBlank()) append(" ")
                append("topic:$it")
            }
            // 如果没有任何筛选条件，则搜索星标最多的仓库
            if (isBlank()) {
                append("stars:>1")
            }
        }
        gitHubRepository.searchRepositories(
            query = finalQuery,
            sort = sort.apiValue,
            order = "desc"
        )
    }.cachedIn(viewModelScope)

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    // 时间范围选项
    private val _timeRange = MutableStateFlow(TrendingTimeRange.DAILY)
    val timeRange: StateFlow<TrendingTimeRange> = _timeRange.asStateFlow()

    // 收藏的仓库
    private val _favoritedRepositories = MutableStateFlow<Set<String>>(emptySet())
    val favoritedRepositories: StateFlow<Set<String>> = _favoritedRepositories.asStateFlow()

    val weeklyTrendingRepositories: Flow<PagingData<Repository>> =
        gitHubRepository.getWeeklyTrendingRepositories().cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val trendingRepositories: Flow<PagingData<Repository>> = timeRange.flatMapLatest { range ->
        when (range) {
            TrendingTimeRange.DAILY -> gitHubRepository.getDailyTrendingRepositories()
            TrendingTimeRange.WEEKLY -> gitHubRepository.getWeeklyTrendingRepositories()
            TrendingTimeRange.MONTHLY -> gitHubRepository.getMonthlyTrendingRepositories()
        }
    }.cachedIn(viewModelScope)
    
    // 热门主题
    private val _trendingTopics = MutableStateFlow<List<Topic>>(emptyList())
    val trendingTopics: StateFlow<List<Topic>> = _trendingTopics.asStateFlow()
    
    // 编程语言列表
    private val _programmingLanguages = MutableStateFlow(
        listOf(
            "Kotlin", "Java", "JavaScript", "TypeScript", "Python", 
            "Go", "Rust", "Swift", "C++", "C#", "React", "Vue"
        )
    )
    val programmingLanguages: StateFlow<List<String>> = _programmingLanguages.asStateFlow()
    
    init {
        loadTrendingTopics()
        loadFavoritedRepositories()
    }
    
    /**
     * 搜索仓库
     */
    fun searchRepositories(query: String) {
        _searchQuery.value = query.trim()
        saveSearchHistory(query, SearchType.REPOSITORIES)
    }
    
    /**
     * 选择编程语言筛选
     */
    fun selectLanguage(language: String?) {
        _selectedLanguage.value = language
    }

    /**
     * 选择主题筛选
     */
    fun selectTopic(topic: String?) {
        _selectedTopic.value = topic
    }

    /**
     * 选择时间范围
     */
    fun selectTimeRange(timeRange: TrendingTimeRange) {
        _timeRange.value = timeRange
    }
    
    /**
     * 更改排序选项
     */
    fun changeSortOption(sortOption: SortOption) {
        _sortOption.value = sortOption
    }
    
    /**
     * 清空搜索
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _selectedLanguage.value = null
    }
    
    /**
     * 刷新数据
     */
    fun refresh() {
        loadTrendingTopics()
        updateUiState { it.copy(isRefreshing = true) }
        
        viewModelScope.launch {
            try {
                // 这里可以添加额外的刷新逻辑
                updateUiState { it.copy(isRefreshing = false) }
            } catch (e: Exception) {
                updateUiState { 
                    it.copy(
                        isRefreshing = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    /**
     * 加载热门主题
     */
    private fun loadTrendingTopics() {
        viewModelScope.launch {
            updateUiState { it.copy(isLoading = true) }
            
            gitHubRepository.getTrendingTopics()
                .onSuccess { topics ->
                    _trendingTopics.value = topics
                    updateUiState { it.copy(isLoading = false, error = null) }
                }
                .onFailure { exception ->
                    updateUiState { 
                        it.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                }
        }
    }
    
    /**
     * 保存搜索历史
     */
    private fun saveSearchHistory(query: String, type: SearchType) {
        if (query.isNotBlank()) {
            viewModelScope.launch {
                gitHubRepository.saveSearchHistory(query, type)
            }
        }
    }
    
    /**
     * 添加/移除收藏
     */
    fun toggleFavorite(repository: Repository) {
        viewModelScope.launch {
            val isFavorited = _favoritedRepositories.value.contains(repository.fullName)
            if (isFavorited) {
                gitHubRepository.removeFavorite(repository.fullName, FavoriteType.REPOSITORY)
            } else {
                val favorite = Favorite(
                    itemId = repository.fullName,
                    itemType = FavoriteType.REPOSITORY,
                    title = repository.fullName,
                    description = repository.description,
                    avatarUrl = repository.owner.avatarUrl
                )
                gitHubRepository.addFavorite(favorite)
            }
            loadFavoritedRepositories() // Refresh the list
        }
    }
    
    /**
     * 加载收藏的仓库
     */
    private fun loadFavoritedRepositories() {
        viewModelScope.launch {
            gitHubRepository.getFavoritesByType(FavoriteType.REPOSITORY).collect { favorites ->
                _favoritedRepositories.value = favorites.map { it.itemId }.toSet()
            }
        }
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        updateUiState { it.copy(error = null) }
    }
    
    /**
     * 更新UI状态
     */
    private fun updateUiState(update: (DashboardUiState) -> DashboardUiState) {
        _uiState.value = update(_uiState.value)
    }
}

/**
 * Dashboard UI 状态
 */
data class DashboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

/**
 * 排序选项
 */
enum class SortOption(val displayName: String, val apiValue: String) {
    STARS("按星数", "stars"),
    FORKS("按分叉数", "forks"),
    UPDATED("按更新时间", "updated"),
    CREATED("按创建时间", "created")
}

/**
 * 趋势时间范围
 */
enum class TrendingTimeRange(val displayName: String) {
    DAILY("今日"),
    WEEKLY("本周"),
    MONTHLY("本月")
}
