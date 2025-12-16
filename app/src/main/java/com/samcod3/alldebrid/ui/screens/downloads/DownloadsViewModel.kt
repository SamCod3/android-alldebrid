package com.samcod3.alldebrid.ui.screens.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samcod3.alldebrid.data.model.Magnet
import com.samcod3.alldebrid.data.repository.AllDebridRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DownloadsUiState(
    val isLoading: Boolean = false,
    val magnets: List<Magnet> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val repository: AllDebridRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getMagnets()
                .onSuccess { magnets ->
                    _uiState.update { it.copy(isLoading = false, magnets = magnets) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun deleteMagnet(id: Long) {
        viewModelScope.launch {
            repository.deleteMagnet(id)
                .onSuccess { refresh() }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    fun unlockLink(link: String) {
        viewModelScope.launch {
            repository.unlockLink(link)
                .onSuccess { unlockedLink ->
                    // TODO: Handle unlocked link - show dialog or copy to clipboard
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }
}
