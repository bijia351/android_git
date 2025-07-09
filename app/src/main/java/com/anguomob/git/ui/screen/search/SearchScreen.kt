package com.anguomob.git.ui.screen.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.anguomob.git.data.model.*
import com.anguomob.git.ui.components.RepositoryCard
import com.anguomob.git.ui.components.TopicCard
import com.anguomob.git.ui.components.UserCard
import com.anguomob.git.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController, viewModel: SearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchType by viewModel.searchType.collectAsState()
    val searchSuggestions by viewModel.searchSuggestions.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()

    var active by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Search") })
        }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                query = searchQuery,
                onQueryChange = viewModel::onQueryChange,
                onSearch = { query ->
                    viewModel.onSearchTriggered(query)
                    active = false
                },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("Search GitHub") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                }) {
                if (searchSuggestions.isNotEmpty()) {
                    LazyColumn {
                        items(searchSuggestions.count()) { suggestionIndex ->
                            val suggestion = searchSuggestions[suggestionIndex]
                            ListItem(
                                headlineContent = { Text(suggestion) },
                                modifier = Modifier.clickable {
                                    viewModel.onQueryChange(suggestion)
                                    viewModel.onSearchTriggered(suggestion)
                                    active = false
                                })
                        }
                    }
                } else if (searchHistory.isNotEmpty()) {
                    SearchHistoryList(
                        history = searchHistory, onHistoryClick = { query, type ->
                            viewModel.selectHistoryQuery(query, type)
                            viewModel.onSearchTriggered(query)
                            active = false
                        }, onClearHistory = viewModel::clearAllSearchHistory
                    )
                }
            }

            if (!active) {
                TabRow(selectedTabIndex = searchType.ordinal) {
                    SearchType.values().forEach { type ->
                        Tab(
                            selected = searchType == type,
                            onClick = { viewModel.changeSearchType(type) },
                            text = { Text(type.name.replaceFirstChar { it.uppercase() }) })
                    }
                }
                SearchResults(navController = navController, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SearchHistoryList(
    history: List<SearchHistory>,
    onHistoryClick: (String, SearchType) -> Unit,
    onClearHistory: () -> Unit
) {
    LazyColumn {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Search History", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onClearHistory) {
                    Text("Clear All")
                }
            }
        }
        items(history.size) { index ->
            val item = history[index]
            ListItem(
                headlineContent = { Text(item.query) },
                leadingContent = { Icon(Icons.Filled.History, contentDescription = null) },
                modifier = Modifier.clickable { onHistoryClick(item.query, item.type) })
        }
    }
}

@Composable
fun SearchResults(
    navController: NavController, viewModel: SearchViewModel
) {
    val searchType by viewModel.searchType.collectAsState()
    val repositories = viewModel.repositories.collectAsLazyPagingItems()
    val topics = viewModel.topics.collectAsLazyPagingItems()
    val favoritedRepositories by viewModel.favoritedRepositories.collectAsState()
    val favoritedTopics by viewModel.favoritedTopics.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (searchType) {
            SearchType.REPOSITORIES -> {
                items(repositories.itemCount) { index ->
                    val repository = repositories[index]
                    repository?.let {
                        RepositoryCard(
                            repository = it,
                            onRepositoryClick = {
                                navController.navigate(Screen.RepositoryDetail.route + "/${it.owner.login}/${it.name}")
                            },
                            isFavorited = favoritedRepositories.contains(it.fullName),
                            onFavoriteClick = { viewModel.toggleRepositoryFavorite(it) }
                        )
                    }
                }
            }
            SearchType.TOPICS -> {
                items(topics.itemCount) { index ->
                    val topic = topics[index]
                    topic?.let {
                        TopicCard(
                            topic = it,
                            onTopicClick = {
                                navController.navigate(Screen.Topics.route + "?topicName=${it.name}")
                            },
                            isFavorited = favoritedTopics.contains(it.name),
                            onFavoriteClick = { viewModel.toggleTopicFavorite(it) }
                        )
                    }
                }
            }
        }
    }
}
