package com.samcod3.alldebrid.ui.screens.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samcod3.alldebrid.data.model.Magnet
import com.samcod3.alldebrid.data.repository.AllDebridRepository
import com.samcod3.alldebrid.data.repository.IpAuthorizationRequiredException
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
    val error: String? = null,
    val requiresIpAuthorization: Boolean = false
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
            _uiState.update { it.copy(isLoading = true, error = null, requiresIpAuthorization = false) }
            repository.getMagnets()
                .onSuccess { magnets ->
                    _uiState.update { it.copy(isLoading = false, magnets = magnets) }
                }
                .onFailure { error ->
                    handleError(error)
                }
        }
    }

    fun deleteMagnet(id: Long) {
        viewModelScope.launch {
            repository.deleteMagnet(id)
                .onSuccess { refresh() }
                .onFailure { error ->
                    handleError(error)
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
                    handleError(error)
                }
        }
    }
    
    private fun handleError(error: Throwable) {
        if (error is IpAuthorizationRequiredException) {
            _uiState.update { it.copy(
                isLoading = false,
                requiresIpAuthorization = true,
                error = "IP authorization required. Tap to authorize."
            )}
        } else {
            _uiState.update { it.copy(isLoading = false, error = error.message) }
        }
    }
    
    fun clearIpAuthorizationFlag() {
        _uiState.update { it.copy(requiresIpAuthorization = false) }
    }
}
