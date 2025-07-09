package com.anguomob.git.ui.screen.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.anguomob.git.ui.components.RepositoryCard
import com.anguomob.git.data.model.Repository
import com.anguomob.git.ui.navigation.Screen
import androidx.paging.compose.LazyPagingItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController, viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val repositories = viewModel.repositories.collectAsLazyPagingItems()
    val trendingRepositories = viewModel.trendingRepositories.collectAsLazyPagingItems()
    val favoritedRepositories by viewModel.favoritedRepositories.collectAsState()
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedTopic by viewModel.selectedTopic.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val programmingLanguages by viewModel.programmingLanguages.collectAsState()
    val trendingTopics by viewModel.trendingTopics.collectAsState()
    val timeRange by viewModel.timeRange.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dashboard") }, actions = {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = expanded, onDismissRequest = { expanded = false }) {
                        SortOption.values().forEach { option ->
                            DropdownMenuItem(text = { Text(option.displayName) }, onClick = {
                                viewModel.changeSortOption(option)
                                expanded = false
                            })
                        }
                    }
                }
            })
        }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            com.anguomob.git.ui.components.SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.searchRepositories(it) },
                onSearch = { viewModel.searchRepositories(it) },
                active = false,
                onActiveChange = {},
                placeholder = { Text("Search Repositories") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }) {

            }

            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Repositories") })
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Trending") })
            }

            if (selectedTabIndex == 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TrendingTimeRange.values().forEach { range ->
                        FilterChip(
                            selected = timeRange == range,
                            onClick = { viewModel.selectTimeRange(range) },
                            label = { Text(range.displayName) },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Language:", modifier = Modifier.padding(start = 16.dp))
                var languageExpanded by remember { mutableStateOf(false) }
                Box {
                    Button(onClick = { languageExpanded = true }) {
                        Text(selectedLanguage ?: "All")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = languageExpanded,
                        onDismissRequest = { languageExpanded = false }) {
                        DropdownMenuItem(text = { Text("All") }, onClick = {
                            viewModel.selectLanguage(null)
                            languageExpanded = false
                        })
                        programmingLanguages.forEach { language ->
                            DropdownMenuItem(text = { Text(language) }, onClick = {
                                viewModel.selectLanguage(language)
                                languageExpanded = false
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text("Topic:", modifier = Modifier.padding(start = 16.dp))
                var topicExpanded by remember { mutableStateOf(false) }
                Box {
                    Button(onClick = { topicExpanded = true }) {
                        Text(selectedTopic ?: "All")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = topicExpanded, onDismissRequest = { topicExpanded = false }) {
                        DropdownMenuItem(text = { Text("All") }, onClick = {
                            viewModel.selectTopic(null)
                            topicExpanded = false
                        })
                        trendingTopics.forEach { topic ->
                            DropdownMenuItem(text = { Text(topic.name) }, onClick = {
                                viewModel.selectTopic(topic.name)
                                topicExpanded = false
                            })
                        }
                    }
                }
            }

            val items: LazyPagingItems<Repository> =
                if (selectedTabIndex == 0) repositories else trendingRepositories
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items.itemCount) { index ->
                        items[index]?.let {
                            RepositoryCard(
                                repository = it,
                                onRepositoryClick = {
                                    navController.navigate(Screen.RepositoryDetail.route + "/${it.owner.login}/${it.name}")
                                },
                                isFavorited = favoritedRepositories.contains(it.fullName),
                                onFavoriteClick = { viewModel.toggleFavorite(it) },
                                isTrending = selectedTabIndex == 1
                            )
                        }
                    }
                }
                if (items.loadState.refresh is LoadState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
