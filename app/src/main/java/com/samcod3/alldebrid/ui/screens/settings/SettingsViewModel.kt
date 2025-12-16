package com.samcod3.alldebrid.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samcod3.alldebrid.data.datastore.SettingsDataStore
import com.samcod3.alldebrid.data.model.User
import com.samcod3.alldebrid.data.repository.AllDebridRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val apiKey: String = "",
    val jackettUrl: String = "",
    val jackettApiKey: String = "",
    val user: User? = null,
    val message: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val repository: AllDebridRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val apiKey = settingsDataStore.apiKey.first()
            val jackettUrl = settingsDataStore.jackettUrl.first()
            val jackettApiKey = settingsDataStore.jackettApiKey.first()
            
            _uiState.update {
                it.copy(
                    apiKey = apiKey,
                    jackettUrl = jackettUrl,
                    jackettApiKey = jackettApiKey
                )
            }
            
            // Test connection if we have an API key
            if (apiKey.isNotBlank()) {
                testConnection()
            }
        }
    }

    fun updateApiKey(value: String) {
        _uiState.update { it.copy(apiKey = value) }
    }

    fun updateJackettUrl(value: String) {
        _uiState.update { it.copy(jackettUrl = value) }
    }

    fun updateJackettApiKey(value: String) {
        _uiState.update { it.copy(jackettApiKey = value) }
    }

    fun saveApiKey() {
        viewModelScope.launch {
            settingsDataStore.saveApiKey(_uiState.value.apiKey)
            _uiState.update { it.copy(message = "API Key saved") }
        }
    }

    fun saveJackettConfig() {
        viewModelScope.launch {
            settingsDataStore.saveJackettConfig(
                url = _uiState.value.jackettUrl,
                apiKey = _uiState.value.jackettApiKey
            )
            _uiState.update { it.copy(message = "Jackett configuration saved") }
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.validateApiKey(_uiState.value.apiKey)
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            message = "Connected successfully!"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = null,
                            message = "Connection failed: ${error.message}"
                        )
                    }
                }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
