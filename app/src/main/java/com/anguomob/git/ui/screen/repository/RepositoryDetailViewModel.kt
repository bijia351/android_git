package com.anguomob.git.ui.screen.repository

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anguomob.git.data.model.Repository
import com.anguomob.git.domain.repository.GitHubRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.anguomob.git.data.api.OpenRouterApiService
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepositoryDetailViewModel @Inject constructor(
    private val gitHubRepository: GitHubRepository, savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val owner: String = savedStateHandle.get<String>("owner")!!
    private val repo: String = savedStateHandle.get<String>("repo")!!

    private val _uiState = MutableStateFlow(RepositoryDetailUiState())
    val uiState: StateFlow<RepositoryDetailUiState> = _uiState.asStateFlow()

    private val _selectedModel = MutableStateFlow(OpenRouterApiService.FREE_MODEL_QWEN)
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    val availableModels = listOf(
        OpenRouterApiService.FREE_MODEL_DEEPSEEK,
        OpenRouterApiService.FREE_MODEL_QWEN,
        OpenRouterApiService.FREE_MODEL_GEMINI_FLASH,
        OpenRouterApiService.FREE_MODEL_PHI3
    )

    init {
        fetchRepositoryDetails()
    }

    fun fetchRepositoryDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = gitHubRepository.getRepositoryDetails(owner, repo)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(repository = it, isLoading = false)
            }.onFailure {
                _uiState.value = _uiState.value.copy(error = it.message, isLoading = false)
            }
        }
    }

    fun summarizeReadme() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, readmeSummary = null)
            val result = gitHubRepository.getReadmeSummary(owner, repo, selectedModel.value)
            result.onSuccess { response ->
                val summary = response.choices.firstOrNull()?.message?.content
                _uiState.value = _uiState.value.copy(readmeSummary = summary, isLoading = false)
            }.onFailure {
                _uiState.value = _uiState.value.copy(error = it.message, isLoading = false)
            }
        }
    }

    fun selectModel(model: String) {
        _selectedModel.value = model
        summarizeReadme()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class RepositoryDetailUiState(
    val repository: Repository? = null,
    val readmeSummary: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
