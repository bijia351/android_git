package com.anguomob.git.ui.screen.personal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.anguomob.git.data.model.Favorite
import com.anguomob.git.data.model.FavoriteType
import com.anguomob.git.data.model.Repository
import com.anguomob.git.ui.components.RepositoryCard
import com.anguomob.git.ui.components.TopicCard
import com.anguomob.git.ui.components.UserCard
import com.anguomob.git.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalRadarScreen(
    navController: NavController,
    viewModel: PersonalRadarViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("My Radar", "Favorites")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Personal Radar") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTabIndex) {
                0 -> MyRadarTab(navController, viewModel)
                1 -> FavoritesTab(navController, viewModel)
            }
        }
    }
}

@Composable
fun MyRadarTab(navController: NavController, viewModel: PersonalRadarViewModel) {
    val followedTopics by viewModel.followedTopics.collectAsState()
    val repositoriesByTopic by viewModel.followedTopicRepositories.collectAsState(initial = emptyMap())
    val favoritedRepositories by viewModel.favoritedRepositories.collectAsState()
    val username by viewModel.username.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val analysisResult by viewModel.analysisResult.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("AI-Powered Personal Radar", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { viewModel.setUsername(it) },
                    label = { Text("Enter GitHub Username") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.analyzeUser() },
                    enabled = !uiState.isAnalyzing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Analyze")
                    }
                }
            }
        }

        if (uiState.hasAnalysis) {
            item {
                currentUser?.let { user ->
                    UserCard(user = user, onUserClick = {})
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(analysisResult ?: "", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.clearAnalysis() }) {
                    Text("Clear Analysis")
                }
            }
        }

        item {
            Text(
                "Followed Topics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        if (followedTopics.isEmpty()) {
            item {
                Text(
                    "You are not following any topics yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(followedTopics) { topic ->
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = topic.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    repositoriesByTopic[topic.title]?.let { repositoriesFlow ->
                        val repositories = repositoriesFlow.collectAsLazyPagingItems()
                        Column {
                            for (i in 0 until repositories.itemCount) {
                                val repository = repositories[i]
                                repository?.let {
                                    RepositoryCard(
                                        repository = it,
                                        onRepositoryClick = {
                                            navController.navigate(Screen.RepositoryDetail.route + "/${it.owner.login}/${it.name}")
                                        },
                                        isFavorited = favoritedRepositories.contains(it.fullName),
                                        onFavoriteClick = { viewModel.toggleRepositoryFavorite(it) }
                                    )
                                    if (i < repositories.itemCount - 1) {
                                        Spacer(Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesTab(navController: NavController, viewModel: PersonalRadarViewModel) {
    val favoritesByType by viewModel.favoritesByType.collectAsState(initial = emptyMap())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Favorite Repositories",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        items(favoritesByType[FavoriteType.REPOSITORY] ?: emptyList()) { favorite ->
            FavoriteCard(favorite = favorite, onCardClick = {
                navController.navigate(Screen.RepositoryDetail.route + "/${it.itemId}")
            })
        }

        item {
            Text(
                "Favorite Topics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        items(favoritesByType[FavoriteType.TOPIC] ?: emptyList()) { favorite ->
            FavoriteCard(favorite = favorite, onCardClick = {
                navController.navigate(Screen.Topics.route + "?topicName=${it.itemId}")
            })
        }

        item {
            Text(
                "Favorite Users",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        items(favoritesByType[FavoriteType.USER] ?: emptyList()) { favorite ->
            FavoriteCard(favorite = favorite, onCardClick = {
                // navController.navigate(Screen.UserDetail.route + "/${it.itemId}")
            })
        }
    }
}

@Composable
fun FavoriteCard(favorite: Favorite, onCardClick: (Favorite) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick(favorite) },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(favorite.title, fontWeight = FontWeight.Bold)
            favorite.description?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
