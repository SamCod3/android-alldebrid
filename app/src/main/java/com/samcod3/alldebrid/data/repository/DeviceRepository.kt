package com.samcod3.alldebrid.data.repository

import com.samcod3.alldebrid.data.api.KodiApi
import com.samcod3.alldebrid.data.api.KodiCommands
import com.samcod3.alldebrid.data.datastore.SettingsDataStore
import com.samcod3.alldebrid.data.model.Device
import com.samcod3.alldebrid.data.model.DeviceType
import com.samcod3.alldebrid.discovery.DeviceDiscoveryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor(
    private val kodiApi: KodiApi,
    private val settingsDataStore: SettingsDataStore,
    private val discoveryManager: DeviceDiscoveryManager
) {
    
    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    
    init {
        // Load cached devices on init
        GlobalScope.launch {
            val cachedDevices = settingsDataStore.getDiscoveredDevicesCache().first()
            if (cachedDevices.isNotEmpty()) {
                _devices.value = cachedDevices
            }
        }
    }
    
    fun getDiscoveredDevices(): Flow<List<Device>> = _devices.asStateFlow()
    
    fun getSelectedDevice(): Flow<Device?> = settingsDataStore.selectedDevice
    
    suspend fun discoverDevices(): Result<List<Device>> {
        return try {
            val devices = discoveryManager.discoverAll()
            _devices.value = devices
            // Save to cache
            settingsDataStore.saveDiscoveredDevices(devices)
            Result.success(devices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun setSelectedDevice(device: Device) {
        settingsDataStore.saveSelectedDevice(device)
    }
    
    suspend fun clearSelectedDevice() {
        settingsDataStore.clearSelectedDevice()
    }
    
    suspend fun castToDevice(device: Device, url: String, addToQueue: Boolean = false): Result<Unit> {
        return try {
            when (device.type) {
                DeviceType.KODI -> castToKodi(device, url, addToQueue)
                DeviceType.DLNA -> castToDlna(device, url)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun checkKodiPlaying(device: Device): Result<Boolean> {
        return try {
            val kodiUrl = "${device.fullAddress}/jsonrpc"
            val response = kodiApi.sendCommand(kodiUrl, KodiCommands.getActivePlayers())
            
            if (response.isSuccessful && response.body()?.error == null) {
                // Parse result to check if there are active players
                val result = response.body()?.result
                val isPlaying = result != null && result.toString().contains("playerid")
                Result.success(isPlaying)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.success(false)
        }
    }
    
    private suspend fun castToKodi(device: Device, url: String, addToQueue: Boolean = false): Result<Unit> {
        return try {
            val kodiUrl = "${device.fullAddress}/jsonrpc"
            val command = if (addToQueue) {
                KodiCommands.addToPlaylist(url)
            } else {
                KodiCommands.playUrl(url)
            }
            val response = kodiApi.sendCommand(kodiUrl, command)
            
            if (response.isSuccessful && response.body()?.error == null) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Kodi playback failed: ${response.body()?.error}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun castToDlna(device: Device, videoUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        // Try multiple common DLNA endpoints
        val possibleEndpoints = listOf(
            "${device.controlUrl ?: "http://${device.address}:${device.port}"}",
            "http://${device.address}:${device.port}/upnp/control/rendertransport1",
            "http://${device.address}:${device.port}/AVTransport/control",
            "http://${device.address}:${device.port}/MediaRenderer/AVTransport/Control",
            "http://${device.address}:${device.port}/dmr" // Samsung Smart TVs
        ).distinct()
        
        var lastError: Exception? = null
        
        for (endpoint in possibleEndpoints) {
            try {
                android.util.Log.d("DLNA_CAST", "Trying endpoint: $endpoint")
                
                // 1. SetAVTransportURI
                val setUriSoap = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <s:Envelope s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:s="http://schemas.xmlsoap.org/soap/envelope/">
                        <s:Body>
                            <u:SetAVTransportURI xmlns:u="urn:schemas-upnp-org:service:AVTransport:1">
                                <InstanceID>0</InstanceID>
                                <CurrentURI>$videoUrl</CurrentURI>
                                <CurrentURIMetaData></CurrentURIMetaData>
                            </u:SetAVTransportURI>
                        </s:Body>
                    </s:Envelope>
                """.trimIndent()
                
                sendSoapAction(endpoint, "urn:schemas-upnp-org:service:AVTransport:1#SetAVTransportURI", setUriSoap)
                
                // 2. Play
                val playSoap = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <s:Envelope s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:s="http://schemas.xmlsoap.org/soap/envelope/">
                        <s:Body>
                            <u:Play xmlns:u="urn:schemas-upnp-org:service:AVTransport:1">
                                <InstanceID>0</InstanceID>
                                <Speed>1</Speed>
                            </u:Play>
                        </s:Body>
                    </s:Envelope>
                """.trimIndent()
                
                sendSoapAction(endpoint, "urn:schemas-upnp-org:service:AVTransport:1#Play", playSoap)
                
                android.util.Log.d("DLNA_CAST", "SUCCESS with endpoint: $endpoint")
                return@withContext Result.success(Unit)
            } catch (e: Exception) {
                android.util.Log.w("DLNA_CAST", "Failed with $endpoint: ${e.message}")
                lastError = e
                // Continue to next endpoint
            }
        }
        
        // All endpoints failed
        Result.failure(lastError ?: Exception("All DLNA endpoints failed"))
    }
    
    private fun sendSoapAction(urlStr: String, soapAction: String, xmlBody: String) {
        val url = URL(urlStr)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "text/xml; charset=\"utf-8\"")
        connection.setRequestProperty("SOAPAction", "\"$soapAction\"")
        
        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(xmlBody)
        writer.flush()
        writer.close()
        
        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            throw Exception("SOAP Action failed with code $responseCode")
        }
    }
}
