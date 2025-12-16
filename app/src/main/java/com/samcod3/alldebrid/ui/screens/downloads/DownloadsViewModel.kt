package com.samcod3.alldebrid.ui.screens.downloads

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samcod3.alldebrid.data.model.Device
import com.samcod3.alldebrid.data.model.Magnet
import com.samcod3.alldebrid.data.repository.AllDebridRepository
import com.samcod3.alldebrid.data.repository.DeviceRepository
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
    val requiresIpAuthorization: Boolean = false,
    val selectedDevice: Device? = null,
    val castingMessage: String? = null,
    val showKodiQueueDialog: Boolean = false,
    val pendingCastLink: String? = null
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val repository: AllDebridRepository,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    init {
        observeSelectedDevice()
        refresh()
    }
    
    private fun observeSelectedDevice() {
        viewModelScope.launch {
            deviceRepository.getSelectedDevice().collect { device ->
                _uiState.update { it.copy(selectedDevice = device) }
            }
        }
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

    fun playLink(link: String) {
        val device = _uiState.value.selectedDevice
        if (device != null) {
            // Check if it's Kodi and if something is playing
            if (device.type == com.samcod3.alldebrid.data.model.DeviceType.KODI) {
                viewModelScope.launch {
                    deviceRepository.checkKodiPlaying(device)
                        .onSuccess { isPlaying ->
                            if (isPlaying) {
                                // Show dialog: Play now or add to queue?
                                _uiState.update { 
                                    it.copy(
                                        showKodiQueueDialog = true,
                                        pendingCastLink = link
                                    ) 
                                }
                            } else {
                                // Not playing, just cast
                                castLink(link, device, addToQueue = false)
                            }
                        }
                        .onFailure {
                            // If check fails, just cast
                            castLink(link, device, addToQueue = false)
                        }
                }
            } else {
                // DLNA: just cast directly
                castLink(link, device, addToQueue = false)
            }
        } else {
            _uiState.update { it.copy(error = "No device selected. Please select a device in Devices tab.") }
        }
    }
    
    fun dismissKodiQueueDialog() {
        _uiState.update { it.copy(showKodiQueueDialog = false, pendingCastLink = null) }
    }
    
    fun playNow() {
        val link = _uiState.value.pendingCastLink
        val device = _uiState.value.selectedDevice
        if (link != null && device != null) {
            dismissKodiQueueDialog()
            castLink(link, device, addToQueue = false)
        }
    }
    
    fun addToQueue() {
        val link = _uiState.value.pendingCastLink
        val device = _uiState.value.selectedDevice
        if (link != null && device != null) {
            dismissKodiQueueDialog()
            castLink(link, device, addToQueue = true)
        }
    }
    
    private fun castLink(link: String, device: Device, addToQueue: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(castingMessage = "Unlocking link...") }
            
            repository.unlockLink(link)
                .onSuccess { unlockedLink ->
                    val action = if (addToQueue) "Adding to queue..." else "Casting to ${device.name}..."
                    _uiState.update { it.copy(castingMessage = action) }
                    
                    deviceRepository.castToDevice(device, unlockedLink.link, addToQueue)
                        .onSuccess {
                             val message = if (addToQueue) "Added to queue!" else "Casting started!"
                             _uiState.update { it.copy(castingMessage = message, error = null) }
                             // Clear message after delay?
                        }
                        .onFailure { error ->
                             _uiState.update { it.copy(castingMessage = null, error = "Casting failed: ${error.message}") }
                        }
                }
                .onFailure { error ->
                    handleError(error)
                    _uiState.update { it.copy(castingMessage = null) }
                }
        }
    }
    
    // Fallback for non-media files or when explicitly requested
    fun unlockAndCopy(link: String) {
         viewModelScope.launch {
            repository.unlockLink(link)
                .onSuccess { unlockedLink ->
                    // UI should observe this event or property to copy/open
                    _uiState.update { it.copy(castingMessage = "Link unlocked: ${unlockedLink.link}") }
                }
                .onFailure { handleError(it) }
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
    
    fun clearMessage() {
        _uiState.update { it.copy(castingMessage = null, error = null) }
    }
}
