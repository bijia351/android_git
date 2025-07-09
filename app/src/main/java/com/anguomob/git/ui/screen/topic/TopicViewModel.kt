package com.anguomob.git.ui.screen.topic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.anguomob.git.data.model.*
import com.anguomob.git.domain.repository.GitHubRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 主题页面 ViewModel
 * 管理技术主题和相关仓库
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TopicViewModel @Inject constructor(
    private val gitHubRepository: GitHubRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    // UI 状态
    private val _uiState = MutableStateFlow(TopicUiState())
    val uiState: StateFlow<TopicUiState> = _uiState.asStateFlow()
    
    // 搜索查询
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // 当前选择的主题
    private val _selectedTopic = MutableStateFlow<Topic?>(null)
    val selectedTopic: StateFlow<Topic?> = _selectedTopic.asStateFlow()

    // 主题活跃度图表数据
    private val _topicActivityData = MutableStateFlow<List<Int>>(emptyList())
    val topicActivityData: StateFlow<List<Int>> = _topicActivityData.asStateFlow()
    
    // 精选主题
    private val _featuredTopics = MutableStateFlow<List<Topic>>(emptyList())
    val featuredTopics: StateFlow<List<Topic>> = _featuredTopics.asStateFlow()
    
    // 热门主题
    private val _trendingTopics = MutableStateFlow<List<Topic>>(emptyList())
    val trendingTopics: StateFlow<List<Topic>> = _trendingTopics.asStateFlow()

    // 主题分类
    private val _topicCategories = MutableStateFlow<Map<String, List<Topic>>>(emptyMap())
    val topicCategories: StateFlow<Map<String, List<Topic>>> = _topicCategories.asStateFlow()

    // 收藏的主题
    private val _favoritedTopics = MutableStateFlow<Set<String>>(emptySet())
    val favoritedTopics: StateFlow<Set<String>> = _favoritedTopics.asStateFlow()

    // 收藏的仓库
    private val _favoritedRepositories = MutableStateFlow<Set<String>>(emptySet())
    val favoritedRepositories: StateFlow<Set<String>> = _favoritedRepositories.asStateFlow()
    
    // 主题搜索结果（分页）
    val searchResults: Flow<PagingData<Topic>> = searchQuery
        .filter { it.isNotBlank() }
        .flatMapLatest { query ->
            gitHubRepository.searchTopics(query)
        }
        .cachedIn(viewModelScope)
    
    // 选中主题的相关仓库（分页）
    val topicRepositories: Flow<PagingData<Repository>> = selectedTopic
        .filterNotNull()
        .flatMapLatest { topic ->
            gitHubRepository.getRepositoriesByTopic(topic.name)
        }
        .cachedIn(viewModelScope)
    
    init {
        savedStateHandle.get<String>("topicName")?.let { topicName ->
            loadTopicByName(topicName)
        }
        loadTopics()
        loadFavoritedTopics()
        loadFavoritedRepositories()
    }
    
    /**
     * 搜索主题
     */
    fun searchTopics(query: String) {
        _searchQuery.value = query.trim()
        if (query.isNotBlank()) {
            saveSearchHistory(query, SearchType.TOPICS)
        }
    }
    
    /**
     * 选择主题
     */
    fun selectTopic(topic: Topic) {
        _selectedTopic.value = topic
        updateUiState { it.copy(showTopicDetail = true) }
        loadTopicActivity(topic.name)
    }
    
    /**
     * 关闭主题详情
     */
    fun closeTopic() {
        _selectedTopic.value = null
        _topicActivityData.value = emptyList()
        updateUiState { it.copy(showTopicDetail = false) }
    }
    
    /**
     * 清空搜索
     */
    fun clearSearch() {
        _searchQuery.value = ""
    }
    
    /**
     * 刷新数据
     */
    fun refresh() {
        loadTopics(isRefresh = true)
    }

    /**
     * 加载所有主题数据
     */
    private fun loadTopics(isRefresh: Boolean = false) {
        if (isRefresh) {
            updateUiState { it.copy(isRefreshing = true) }
        } else {
            updateUiState { it.copy(isLoading = true) }
        }

        viewModelScope.launch {
            // 并行加载精选和热门主题
            val featuredResult = gitHubRepository.getFeaturedTopics()
            val trendingResult = gitHubRepository.getTrendingTopics()

            featuredResult.onSuccess { topics ->
                _featuredTopics.value = topics
            }.onFailure { exception ->
                if (!isRefresh) { // 仅在初次加载时显示错误
                    updateUiState { it.copy(error = exception.message) }
                }
            }

            trendingResult.onSuccess { topics ->
                _trendingTopics.value = topics
                categorizeTopics(topics)
            }.onFailure { exception ->
                // 热门主题失败不影响核心功能，可以只记录日志
            }

            if (isRefresh) {
                updateUiState { it.copy(isRefreshing = false) }
            } else {
                updateUiState { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * 加载主题活跃度数据
     */
    private fun loadTopicActivity(topicName: String) {
        viewModelScope.launch {
            updateUiState { it.copy(isLoading = true) }
            gitHubRepository.getTopicActivityStats(topicName)
                .onSuccess { data ->
                    _topicActivityData.value = data
                }
                .onFailure {
                    // Handle error, maybe show a toast
                }
            updateUiState { it.copy(isLoading = false) }
        }
    }

    private fun loadTopicByName(topicName: String) {
        viewModelScope.launch {
            gitHubRepository.getTopicByName(topicName)
                .onSuccess { topic ->
                    topic?.let { selectTopic(it) }
                }
                .onFailure {
                    // Handle error if needed
                }
        }
    }

    private fun categorizeTopics(topicsToCategorize: List<Topic>) {
        val categories = mapOf(
            "Programming Languages" to listOf("javascript", "python", "java", "typescript", "csharp", "cpp", "php", "c", "shell", "ruby", "go", "kotlin", "rust", "swift"),
            "Frameworks" to listOf("react", "angular", "vue", "django", "flask", "spring", "laravel", "rubyonrails", "aspnet", "express", "nodejs"),
            "Databases" to listOf("mysql", "postgresql", "sqlite", "mongodb", "redis", "firebase"),
            "AI & ML" to listOf("tensorflow", "pytorch", "keras", "scikitlearn", "deeplearning", "machinelearning", "nlp"),
            "Web3" to listOf("blockchain", "ethereum", "solidity", "web3", "crypto"),
            "Mobile" to listOf("android", "ios", "flutter", "reactnative", "swiftui")
        )

        val categorizedTopics = mutableMapOf<String, MutableList<Topic>>()
        topicsToCategorize.forEach { topic ->
            var foundCategory = false
            for ((category, keywords) in categories) {
                if (keywords.any { keyword -> topic.name.contains(keyword, ignoreCase = true) }) {
                    categorizedTopics.computeIfAbsent(category) { mutableListOf() }.add(topic)
                    foundCategory = true
                    break
                }
            }
            if (!foundCategory) {
                categorizedTopics.computeIfAbsent("Other") { mutableListOf() }.add(topic)
            }
        }
        _topicCategories.value = categorizedTopics
    }
    
    /**
     * 保存搜索历史
     */
    private fun saveSearchHistory(query: String, type: SearchType) {
        viewModelScope.launch {
            gitHubRepository.saveSearchHistory(query, type)
        }
    }
    
    /**
     * 添加/移除主题收藏
     */
    fun toggleTopicFavorite(topic: Topic) {
        viewModelScope.launch {
            val isFavorited = _favoritedTopics.value.contains(topic.name)
            if (isFavorited) {
                gitHubRepository.removeFavorite(topic.name, FavoriteType.TOPIC)
                _favoritedTopics.value = _favoritedTopics.value - topic.name
            } else {
                val favorite = Favorite(
                    itemId = topic.name,
                    itemType = FavoriteType.TOPIC,
                    title = topic.displayName ?: topic.name,
                    description = topic.description,
                    avatarUrl = null
                )
                gitHubRepository.addFavorite(favorite)
                _favoritedTopics.value = _favoritedTopics.value + topic.name
            }
        }
    }
    
    /**
     * 添加/移除仓库收藏
     */
    fun toggleRepositoryFavorite(repository: Repository) {
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
            loadFavoritedRepositories()
        }
    }
    
    /**
     * 加载收藏的主题
     */
    private fun loadFavoritedTopics() {
        viewModelScope.launch {
            gitHubRepository.getFavoritedTopics().collect { favorites ->
                _favoritedTopics.value = favorites.map { it.itemId }.toSet()
            }
        }
    }
    
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
    private fun updateUiState(update: (TopicUiState) -> TopicUiState) {
        _uiState.value = update(_uiState.value)
    }
}

/**
 * 主题页面 UI 状态
 */
data class TopicUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val showTopicDetail: Boolean = false,
    val error: String? = null
)
