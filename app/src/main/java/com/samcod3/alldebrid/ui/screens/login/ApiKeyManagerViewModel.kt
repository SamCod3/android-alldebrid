package com.samcod3.alldebrid.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samcod3.alldebrid.data.api.ApiKeyData
import com.samcod3.alldebrid.data.api.DashboardApi
import com.samcod3.alldebrid.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ApiKeyManagerUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val keys: List<ApiKeyData> = emptyList(),
    val selectedKey: String? = null,
    val message: String? = null
)

@HiltViewModel
class ApiKeyManagerViewModel @Inject constructor(
    private val dashboardApi: DashboardApi,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApiKeyManagerUiState())
    val uiState: StateFlow<ApiKeyManagerUiState> = _uiState.asStateFlow()

    init {
        // Check login status and load current key
        viewModelScope.launch {
            val currentKey = settingsDataStore.apiKey.first()
            _uiState.update { it.copy(
                isLoggedIn = dashboardApi.isLoggedIn(),
                selectedKey = currentKey.ifBlank { null }
            )}
        }
    }

    fun loadKeys() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            if (!dashboardApi.isLoggedIn()) {
                _uiState.update { it.copy(
                    isLoading = false,
                    isLoggedIn = false,
                    keys = emptyList()
                )}
                return@launch
            }
            
            dashboardApi.fetchKeys()
                .onSuccess { keys ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        keys = keys
                    )}
                }
                .onFailure { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        message = "Error loading keys: ${error.message}"
                    )}
                }
        }
    }

    fun createKey(name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            dashboardApi.createKey(name)
                .onSuccess {
                    _uiState.update { it.copy(message = "Key created successfully") }
                    loadKeys() // Refresh the list
                }
                .onFailure { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        message = "Error creating key: ${error.message}"
                    )}
                }
        }
    }

    fun deleteKey(apikey: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            dashboardApi.deleteKey(apikey)
                .onSuccess {
                    // If deleting the selected key, clear selection
                    if (_uiState.value.selectedKey == apikey) {
                        settingsDataStore.saveApiKey("")
                        _uiState.update { it.copy(selectedKey = null) }
                    }
                    _uiState.update { it.copy(message = "Key deleted successfully") }
                    loadKeys() // Refresh the list
                }
                .onFailure { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        message = "Error deleting key: ${error.message}"
                    )}
                }
        }
    }

    fun renameKey(apikey: String, newName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            dashboardApi.renameKey(apikey, newName)
                .onSuccess {
                    _uiState.update { it.copy(message = "Key renamed successfully") }
                    loadKeys() // Refresh the list
                }
                .onFailure { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        message = "Error renaming key: ${error.message}"
                    )}
                }
        }
    }

    fun selectKey(apikey: String) {
        viewModelScope.launch {
            settingsDataStore.saveApiKey(apikey)
            _uiState.update { it.copy(
                selectedKey = apikey,
                message = "API key selected"
            )}
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun refreshLoginState() {
        _uiState.update { it.copy(isLoggedIn = dashboardApi.isLoggedIn()) }
        if (dashboardApi.isLoggedIn()) {
            loadKeys()
        }
    }
}
