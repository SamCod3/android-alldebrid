package com.samcod3.alldebrid.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samcod3.alldebrid.data.model.SearchResult
import com.samcod3.alldebrid.data.repository.AllDebridRepository
import com.samcod3.alldebrid.data.repository.JackettRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<SearchResult> = emptyList(),
    val error: String? = null,
    val hasSearched: Boolean = false,
    val message: String? = null,
    val isAdding: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val jackettRepository: JackettRepository,
    private val allDebridRepository: AllDebridRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun search() {
        if (_uiState.value.query.isBlank()) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, hasSearched = true) }

            jackettRepository.search(_uiState.value.query)
                .onSuccess { results ->
                    val sortedResults = results.sortedByDescending { it.size ?: 0 }
                    _uiState.update { it.copy(isLoading = false, results = sortedResults, error = null) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "Search failed") }
                }
        }
    }

    fun addToDebrid(result: SearchResult) {
        if (_uiState.value.isAdding) return

        viewModelScope.launch {
            val magnetLink = result.magnetUri ?: result.link

            if (magnetLink == null) {
                _uiState.update { it.copy(error = "No magnet or link available") }
                return@launch
            }

            _uiState.update { it.copy(isAdding = true, message = "Adding...") }

            allDebridRepository.uploadLink(magnetLink)
                .onSuccess { isReady ->
                    val message = if (isReady) "Added! (Cached)" else "Added! (Downloading...)"
                    updateResult(result) { it.copy(addedToDebrid = true, isDownloading = !isReady) }
                    _uiState.update { it.copy(isAdding = false, message = message) }
                }
                .onFailure {
                    updateResult(result) { it.copy(failed = true) }
                    _uiState.update { it.copy(isAdding = false, message = null) }
                }
        }
    }

    private fun updateResult(target: SearchResult, transform: (SearchResult) -> SearchResult) {
        _uiState.update { state ->
            state.copy(results = state.results.map { if (it == target) transform(it) else it })
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

}
