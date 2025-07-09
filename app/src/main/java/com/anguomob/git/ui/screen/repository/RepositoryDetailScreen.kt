package com.anguomob.git.ui.screen.repository

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anguomob.git.data.model.Repository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoryDetailScreen(
    navController: NavController,
    viewModel: RepositoryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.repository?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading && uiState.repository == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null && uiState.repository == null) {
                Text(
                    text = "Error: ${uiState.error}",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                uiState.repository?.let {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        RepositoryDetailContent(repository = it)
                        Spacer(modifier = Modifier.height(16.dp))
                        ReadmeSummarySection(
                            viewModel = viewModel,
                            uiState = uiState,
                            selectedModel = selectedModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RepositoryDetailContent(repository: Repository) {
    Column {
        Text(text = repository.fullName, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = repository.description ?: "No description", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Text("Stars: ${repository.stargazersCount}", modifier = Modifier.weight(1f))
            Text("Forks: ${repository.forksCount}", modifier = Modifier.weight(1f))
            Text("Language: ${repository.language ?: "N/A"}", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ReadmeSummarySection(
    viewModel: RepositoryDetailViewModel,
    uiState: RepositoryDetailUiState,
    selectedModel: String
) {
    Column {
        Text("README Summary", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ModelSelector(viewModel = viewModel, selectedModel = selectedModel)
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.summarizeReadme() }, enabled = !uiState.isLoading) {
                Text("Summarize")
            }
        }
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
        } else if (uiState.error != null) {
            Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
        }
        uiState.readmeSummary?.let { summary ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Summary (Model: $selectedModel):", style = MaterialTheme.typography.titleMedium)
            Text(text = summary)
        }
    }
}

@Composable
fun ModelSelector(viewModel: RepositoryDetailViewModel, selectedModel: String) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedModel)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            viewModel.availableModels.forEach { model ->
                DropdownMenuItem(
                    text = { Text(model) },
                    onClick = {
                        viewModel.selectModel(model)
                        expanded = false
                    }
                )
            }
        }
    }
}
