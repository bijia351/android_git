package com.anguomob.git.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anguomob.git.data.model.*
import com.anguomob.git.domain.repository.GitHubRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val gitHubRepository: GitHubRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchType = MutableStateFlow(SearchType.REPOSITORIES)
    val searchType: StateFlow<SearchType> = _searchType.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<SearchHistory>>(emptyList())
    val searchHistory: StateFlow<List<SearchHistory>> = _searchHistory.asStateFlow()

    private val _favoritedRepositories = MutableStateFlow<Set<String>>(emptySet())
    val favoritedRepositories: StateFlow<Set<String>> = _favoritedRepositories.asStateFlow()

    private val _favoritedTopics = MutableStateFlow<Set<String>>(emptySet())
    val favoritedTopics: StateFlow<Set<String>> = _favoritedTopics.asStateFlow()

    val searchSuggestions: StateFlow<List<String>> = searchQuery
        .debounce(300)
        .map { query ->
            if (query.isEmpty()) {
                emptyList()
            } else {
                gitHubRepository.getSearchHistoryByType(searchType.value)
                    .getOrNull()
                    ?.map { it.query }
                    ?.filter { it.contains(query, ignoreCase = true) }
                    ?: emptyList()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val searchTrigger =
        MutableSharedFlow<Pair<String, SearchType>>(replay = 1, extraBufferCapacity = 1)

    val repositories: Flow<PagingData<Repository>> = searchTrigger
        .filter { it.second == SearchType.REPOSITORIES && it.first.isNotBlank() }
        .flatMapLatest { (query, _) ->
            gitHubRepository.searchRepositories(query).cachedIn(viewModelScope)
        }

    val topics: Flow<PagingData<Topic>> = searchTrigger
        .filter { it.second == SearchType.TOPICS && it.first.isNotBlank() }
        .flatMapLatest { (query, _) ->
            gitHubRepository.searchTopics(query).cachedIn(viewModelScope)
        }

    init {
        loadSearchHistory()
        loadFavoritedRepositories()
        loadFavoritedTopics()
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSearchTriggered(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isNotEmpty()) {
            searchTrigger.tryEmit(Pair(trimmedQuery, _searchType.value))
            saveSearchHistory(trimmedQuery, _searchType.value)
        }
    }

    fun changeSearchType(type: SearchType) {
        _searchType.value = type
        onSearchTriggered(_searchQuery.value)
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun selectHistoryQuery(query: String, type: SearchType) {
        _searchQuery.value = query
        _searchType.value = type
    }

    private fun loadSearchHistory() {
        viewModelScope.launch {
            gitHubRepository.getSearchHistory().collect {
                _searchHistory.value = it
            }
        }
    }

    private fun saveSearchHistory(query: String, type: SearchType) {
        viewModelScope.launch {
            gitHubRepository.saveSearchHistory(query, type)
        }
    }

    fun clearAllSearchHistory() {
        viewModelScope.launch {
            gitHubRepository.clearSearchHistory()
        }
    }

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

    private fun loadFavoritedRepositories() {
        viewModelScope.launch {
            gitHubRepository.getFavoritesByType(FavoriteType.REPOSITORY).collect { favorites ->
                _favoritedRepositories.value = favorites.map { it.itemId }.toSet()
            }
        }
    }

    fun toggleTopicFavorite(topic: Topic) {
        viewModelScope.launch {
            val isFavorited = _favoritedTopics.value.contains(topic.name)
            if (isFavorited) {
                gitHubRepository.removeFavorite(topic.name, FavoriteType.TOPIC)
            } else {
                val favorite = Favorite(
                    itemId = topic.name,
                    itemType = FavoriteType.TOPIC,
                    title = topic.displayName ?: topic.name,
                    description = topic.description,
                    avatarUrl = null
                )
                gitHubRepository.addFavorite(favorite)
            }
            // Reload the state to reflect the change in UI
            loadFavoritedTopics()
        }
    }

    private fun loadFavoritedTopics() {
        viewModelScope.launch {
            gitHubRepository.getFavoritesByType(FavoriteType.TOPIC).collect { favorites ->
                _favoritedTopics.value = favorites.map { it.itemId }.toSet()
            }
        }
    }

    fun isTopicFavorited(topic: Topic): Boolean {
        return _favoritedTopics.value.contains(topic.name)
    }
}

/**
 * 搜索页面 UI 状态
 */
data class SearchUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
