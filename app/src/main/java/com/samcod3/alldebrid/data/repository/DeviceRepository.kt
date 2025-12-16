package com.samcod3.alldebrid.data.repository

import com.samcod3.alldebrid.data.api.KodiApi
import com.samcod3.alldebrid.data.api.KodiCommands
import com.samcod3.alldebrid.data.datastore.SettingsDataStore
import com.samcod3.alldebrid.data.model.Device
import com.samcod3.alldebrid.data.model.DeviceType
import com.samcod3.alldebrid.discovery.DeviceDiscoveryManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor(
    private val kodiApi: KodiApi,
    private val settingsDataStore: SettingsDataStore,
    private val discoveryManager: DeviceDiscoveryManager
) {
    
    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    private val _selectedDevice = MutableStateFlow<Device?>(null)
    
    fun getSavedDevices(): Flow<List<Device>> = _devices.asStateFlow()
    
    fun getSelectedDevice(): Flow<Device?> = _selectedDevice.asStateFlow()
    
    suspend fun discoverDevices(): Result<List<Device>> {
        return try {
            val devices = discoveryManager.discoverAll()
            _devices.value = devices
            Result.success(devices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun setSelectedDevice(device: Device) {
        _selectedDevice.value = device
        settingsDataStore.saveSelectedDeviceId(device.id)
    }
    
    suspend fun castToDevice(device: Device, url: String): Result<Unit> {
        return try {
            when (device.type) {
                DeviceType.KODI -> castToKodi(device, url)
                DeviceType.DLNA -> castToDlna(device, url)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun castToKodi(device: Device, url: String): Result<Unit> {
        return try {
            val kodiUrl = "${device.fullAddress}/jsonrpc"
            val response = kodiApi.sendCommand(kodiUrl, KodiCommands.playUrl(url))
            
            if (response.isSuccessful && response.body()?.error == null) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Kodi playback failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun castToDlna(device: Device, url: String): Result<Unit> {
        // TODO: Implement DLNA casting using jUPnP
        // This requires UPnP AVTransport service interaction
        return Result.failure(Exception("DLNA casting not yet implemented"))
    }
}
