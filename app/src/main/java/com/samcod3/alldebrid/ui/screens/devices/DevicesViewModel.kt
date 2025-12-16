package com.samcod3.alldebrid.ui.screens.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samcod3.alldebrid.data.model.Device
import com.samcod3.alldebrid.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DevicesUiState(
    val isDiscovering: Boolean = false,
    val devices: List<Device> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevicesUiState())
    val uiState: StateFlow<DevicesUiState> = _uiState.asStateFlow()

    init {
        loadSavedDevices()
    }

    private fun loadSavedDevices() {
        viewModelScope.launch {
            deviceRepository.getSavedDevices().collect { devices ->
                _uiState.update { it.copy(devices = devices) }
            }
        }
    }

    fun discoverDevices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDiscovering = true, error = null) }
            
            deviceRepository.discoverDevices()
                .onSuccess { devices ->
                    _uiState.update { it.copy(isDiscovering = false, devices = devices) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isDiscovering = false, error = error.message) }
                }
        }
    }

    fun selectDevice(device: Device) {
        viewModelScope.launch {
            deviceRepository.setSelectedDevice(device)
        }
    }
}
