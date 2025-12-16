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
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<SearchResult> = emptyList(),
    val error: String? = null,
    val hasSearched: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val jackettRepository: JackettRepository,
    private val allDebridRepository: AllDebridRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun search() {
        if (_uiState.value.query.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, hasSearched = true) }
            
            jackettRepository.search(_uiState.value.query)
                .onSuccess { results ->
                    _uiState.update { it.copy(isLoading = false, results = results) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun addToDebrid(result: SearchResult) {
        viewModelScope.launch {
            val magnetLink = result.magnetUri ?: result.link ?: return@launch
            
            allDebridRepository.uploadMagnet(magnetLink)
                .onSuccess {
                    // Mark as added in UI
                    _uiState.update { state ->
                        state.copy(
                            results = state.results.map {
                                if (it == result) it.copy(addedToDebrid = true) else it
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }
}
