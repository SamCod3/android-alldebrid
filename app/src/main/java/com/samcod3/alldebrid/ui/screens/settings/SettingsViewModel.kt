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
    val useCustomIpRange: Boolean = false,
    val customIpPrefix: String = "",
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
            val useCustomIpRange = settingsDataStore.useCustomIpRange.first()
            val customIpPrefix = settingsDataStore.customIpPrefix.first()
            
            _uiState.update {
                it.copy(
                    apiKey = apiKey,
                    jackettUrl = jackettUrl,
                    jackettApiKey = jackettApiKey,
                    useCustomIpRange = useCustomIpRange,
                    customIpPrefix = customIpPrefix
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
    
    fun updateUseCustomIpRange(value: Boolean) {
        _uiState.update { it.copy(useCustomIpRange = value) }
    }
    
    fun updateCustomIpPrefix(value: String) {
        _uiState.update { it.copy(customIpPrefix = value) }
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
    
    fun saveCustomIpRange() {
        viewModelScope.launch {
            settingsDataStore.saveCustomIpRange(
                enabled = _uiState.value.useCustomIpRange,
                ipPrefix = _uiState.value.customIpPrefix
            )
            _uiState.update { it.copy(message = "IP range saved") }
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
    
    /**
     * Logout: Clear API key, user state, and WebView cookies
     */
    fun logout() {
        viewModelScope.launch {
            // Clear API key
            settingsDataStore.saveApiKey("")
            
            // Clear WebView cookies
            android.webkit.CookieManager.getInstance().apply {
                removeAllCookies(null)
                flush()
            }
            
            // Update UI state
            _uiState.update { 
                it.copy(
                    apiKey = "",
                    user = null,
                    message = "Logged out successfully"
                ) 
            }
        }
    }
}
