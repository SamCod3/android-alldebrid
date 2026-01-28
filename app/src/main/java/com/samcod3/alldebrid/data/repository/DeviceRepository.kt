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
    private val discoveryManager: DeviceDiscoveryManager,
    val dlnaQueue: DlnaQueueManager
) {
    
    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    
    init {
        // Load cached devices on init - using GlobalScope is intentional for Singleton
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
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
            // Merge with existing to preserve customNames
            val existing = _devices.value
            val merged = mergeDevices(existing, devices)
            _devices.value = merged
            // Save to cache
            settingsDataStore.saveDiscoveredDevices(merged)
            Result.success(merged)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Hybrid discovery - SSDP + quick Kodi-only subnet scan.
     * Best when router doesn't support multicast but you mainly use Kodi.
     */
    suspend fun discoverDevicesHybrid(): Result<List<Device>> {
        return try {
            val devices = discoveryManager.discoverHybrid()
            val existing = _devices.value
            val merged = mergeDevices(existing, devices)
            _devices.value = merged
            settingsDataStore.saveDiscoveredDevices(merged)
            Result.success(merged)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Manual discovery - slower but scans all IPs in subnet.
     * Use when SSDP doesn't find devices.
     */
    suspend fun discoverDevicesManual(): Result<List<Device>> {
        return try {
            val devices = discoveryManager.discoverManualScan()
            // Merge with existing devices - update name but preserve customName
            val existing = _devices.value
            val merged = mergeDevices(existing, devices)
            _devices.value = merged
            settingsDataStore.saveDiscoveredDevices(merged)
            Result.success(merged)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Merges new devices with existing ones.
     * - Updates device name (friendlyName) from new discovery
     * - Preserves user's customName if set
     * - Adds new devices not in existing list
     */
    private fun mergeDevices(existing: List<Device>, new: List<Device>): List<Device> {
        val existingMap = existing.associateBy { "${it.address}:${it.port}" }
        val result = mutableListOf<Device>()
        
        // First, add all new devices, merging with existing if present
        for (newDevice in new) {
            val key = "${newDevice.address}:${newDevice.port}"
            val existingDevice = existingMap[key]
            
            if (existingDevice != null) {
                // Merge: keep customName from existing, update name from new
                result.add(newDevice.copy(customName = existingDevice.customName))
            } else {
                result.add(newDevice)
            }
        }
        
        // Add any existing devices not found in new scan
        for (existingDevice in existing) {
            val key = "${existingDevice.address}:${existingDevice.port}"
            if (result.none { "${it.address}:${it.port}" == key }) {
                result.add(existingDevice)
            }
        }
        
        return result
    }
    
    suspend fun setSelectedDevice(device: Device) {
        settingsDataStore.saveSelectedDevice(device)
    }
    
    suspend fun clearSelectedDevice() {
        settingsDataStore.clearSelectedDevice()
    }
    
    suspend fun renameDevice(device: Device, customName: String?) {
        val updatedDevice = device.copy(customName = customName)
        // Update in devices list
        val updatedDevices = _devices.value.map {
            if (it.id == device.id) updatedDevice else it
        }
        _devices.value = updatedDevices
        // Save to cache
        settingsDataStore.saveDiscoveredDevices(updatedDevices)
        // Update selected device if this was the selected one
        val currentSelected = settingsDataStore.selectedDevice.first()
        if (currentSelected?.id == device.id) {
            settingsDataStore.saveSelectedDevice(updatedDevice)
        }
    }

    suspend fun deleteDevice(device: Device) {
        // Remove from devices list
        val updatedDevices = _devices.value.filter { it.id != device.id }
        _devices.value = updatedDevices
        // Save to cache
        settingsDataStore.saveDiscoveredDevices(updatedDevices)
        // Clear selected device if this was the selected one
        val currentSelected = settingsDataStore.selectedDevice.first()
        if (currentSelected?.id == device.id ||
            (currentSelected?.address == device.address && currentSelected.port == device.port)) {
            settingsDataStore.clearSelectedDevice()
        }
    }
    
    suspend fun castToDevice(device: Device, url: String, addToQueue: Boolean = false, title: String = "Video"): Result<Unit> {
        return try {
            when (device.type) {
                DeviceType.KODI -> castToKodi(device, url, addToQueue)
                DeviceType.DLNA -> {
                    if (addToQueue) {
                        // Add to DLNA queue
                        dlnaQueue.addToQueue(url, title)
                        Result.success(Unit)
                    } else {
                        // Play immediately
                        castToDlna(device, url, title)
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Play next item in DLNA queue
     */
    suspend fun playNextInDlnaQueue(device: Device): Result<Unit> {
        val nextItem = dlnaQueue.popNext() ?: return Result.failure(Exception("Queue is empty"))
        return castToDlna(device, nextItem.url, nextItem.title)
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
    
    // Kodi Playback Controls
    suspend fun stopKodi(device: Device): Result<Unit> {
        return try {
            val kodiUrl = "${device.fullAddress}/jsonrpc"
            val response = kodiApi.sendCommand(kodiUrl, KodiCommands.stop())
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Stop failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun pauseKodi(device: Device): Result<Unit> {
        return try {
            val kodiUrl = "${device.fullAddress}/jsonrpc"
            val response = kodiApi.sendCommand(kodiUrl, KodiCommands.pause())
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Pause failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun castToDlna(device: Device, videoUrl: String, title: String = "Video"): Result<Unit> = withContext(Dispatchers.IO) {
        // Try multiple common DLNA endpoints - AVTransport1 is the correct one for Samsung
        val possibleEndpoints = listOf(
            "http://${device.address}:${device.port}/upnp/control/AVTransport1", // Samsung/LG - CORRECT
            "${device.controlUrl ?: "http://${device.address}:${device.port}"}",
            "http://${device.address}:9197/upnp/control/AVTransport1", // Samsung default port
            "http://${device.address}:${device.port}/MediaRenderer/AVTransport/Control",
            "http://${device.address}:${device.port}/AVTransport/control"
        ).distinct()
        
        var lastError: Exception? = null
        
        for (endpoint in possibleEndpoints) {
            try {
                android.util.Log.d("DLNA_CAST", "Trying endpoint: $endpoint")
                
                // Escape special XML characters in URL and title
                val escapedUrl = videoUrl
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                
                val escapedTitle = title
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                
                // Determine MIME type from URL
                val mimeType = when {
                    videoUrl.contains(".mkv", ignoreCase = true) -> "video/x-mkv"
                    videoUrl.contains(".mp4", ignoreCase = true) -> "video/mp4"
                    videoUrl.contains(".avi", ignoreCase = true) -> "video/avi"
                    videoUrl.contains(".webm", ignoreCase = true) -> "video/webm"
                    else -> "video/mp4" // default
                }
                
                // DIDL-Lite metadata required by Samsung TVs (error 714 = Illegal MIME-type)
                val didlMetadata = """&lt;DIDL-Lite xmlns=&quot;urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/&quot; xmlns:dc=&quot;http://purl.org/dc/elements/1.1/&quot; xmlns:upnp=&quot;urn:schemas-upnp-org:metadata-1-0/upnp/&quot;&gt;&lt;item id=&quot;0&quot; parentID=&quot;-1&quot; restricted=&quot;1&quot;&gt;&lt;dc:title&gt;$escapedTitle&lt;/dc:title&gt;&lt;upnp:class&gt;object.item.videoItem&lt;/upnp:class&gt;&lt;res protocolInfo=&quot;http-get:*:$mimeType:*&quot;&gt;$escapedUrl&lt;/res&gt;&lt;/item&gt;&lt;/DIDL-Lite&gt;"""
                
                // 0. Stop any current playback first (helps when switching videos)
                val stopSoap = """<?xml version="1.0" encoding="utf-8"?><s:Envelope s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:s="http://schemas.xmlsoap.org/soap/envelope/"><s:Body><u:Stop xmlns:u="urn:schemas-upnp-org:service:AVTransport:1"><InstanceID>0</InstanceID></u:Stop></s:Body></s:Envelope>"""
                try {
                    sendSoapAction(endpoint, "urn:schemas-upnp-org:service:AVTransport:1#Stop", stopSoap)
                } catch (e: Exception) {
                    // Ignore stop errors - might not be playing
                    android.util.Log.d("DLNA_CAST", "Stop ignored: ${e.message}")
                }
                
                // 1. SetAVTransportURI with DIDL-Lite metadata
                val setUriSoap = """<?xml version="1.0" encoding="utf-8"?><s:Envelope s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:s="http://schemas.xmlsoap.org/soap/envelope/"><s:Body><u:SetAVTransportURI xmlns:u="urn:schemas-upnp-org:service:AVTransport:1"><InstanceID>0</InstanceID><CurrentURI>$escapedUrl</CurrentURI><CurrentURIMetaData>$didlMetadata</CurrentURIMetaData></u:SetAVTransportURI></s:Body></s:Envelope>"""
                
                sendSoapAction(endpoint, "urn:schemas-upnp-org:service:AVTransport:1#SetAVTransportURI", setUriSoap)
                
                // 2. Play - compact XML
                val playSoap = """<?xml version="1.0" encoding="utf-8"?><s:Envelope s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:s="http://schemas.xmlsoap.org/soap/envelope/"><s:Body><u:Play xmlns:u="urn:schemas-upnp-org:service:AVTransport:1"><InstanceID>0</InstanceID><Speed>1</Speed></u:Play></s:Body></s:Envelope>"""
                
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
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        
        // Headers matching browser extension
        connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8")
        connection.setRequestProperty("SOAPAction", "\"$soapAction\"")
        connection.setRequestProperty("Connection", "close")
        
        android.util.Log.d("DLNA_CAST", "Sending to: $urlStr")
        android.util.Log.d("DLNA_CAST", "SOAPAction: $soapAction")
        
        val writer = OutputStreamWriter(connection.outputStream, Charsets.UTF_8)
        writer.write(xmlBody)
        writer.flush()
        writer.close()
        
        val responseCode = connection.responseCode
        android.util.Log.d("DLNA_CAST", "Response code: $responseCode")
        
        if (responseCode !in 200..299) {
            // Read error response for debugging
            val errorStream = connection.errorStream
            val errorBody = errorStream?.bufferedReader()?.readText() ?: "No error body"
            android.util.Log.e("DLNA_CAST", "Error body: $errorBody")
            throw Exception("SOAP failed ($responseCode): ${errorBody.take(100)}")
        }
        
        connection.disconnect()
    }
    
    // DLNA Playback Controls
    suspend fun stopDlna(device: Device): Result<Unit> = withContext(Dispatchers.IO) {
        val endpoint = "http://${device.address}:${device.port}/upnp/control/AVTransport1"
        val stopSoap = """<?xml version="1.0" encoding="utf-8"?><s:Envelope s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:s="http://schemas.xmlsoap.org/soap/envelope/"><s:Body><u:Stop xmlns:u="urn:schemas-upnp-org:service:AVTransport:1"><InstanceID>0</InstanceID></u:Stop></s:Body></s:Envelope>"""
        try {
            sendSoapAction(endpoint, "urn:schemas-upnp-org:service:AVTransport:1#Stop", stopSoap)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun pauseDlna(device: Device): Result<Unit> = withContext(Dispatchers.IO) {
        val endpoint = "http://${device.address}:${device.port}/upnp/control/AVTransport1"
        val pauseSoap = """<?xml version="1.0" encoding="utf-8"?><s:Envelope s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:s="http://schemas.xmlsoap.org/soap/envelope/"><s:Body><u:Pause xmlns:u="urn:schemas-upnp-org:service:AVTransport:1"><InstanceID>0</InstanceID></u:Pause></s:Body></s:Envelope>"""
        try {
            sendSoapAction(endpoint, "urn:schemas-upnp-org:service:AVTransport:1#Pause", pauseSoap)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun resumeDlna(device: Device): Result<Unit> = withContext(Dispatchers.IO) {
        val endpoint = "http://${device.address}:${device.port}/upnp/control/AVTransport1"
        val playSoap = """<?xml version="1.0" encoding="utf-8"?><s:Envelope s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:s="http://schemas.xmlsoap.org/soap/envelope/"><s:Body><u:Play xmlns:u="urn:schemas-upnp-org:service:AVTransport:1"><InstanceID>0</InstanceID><Speed>1</Speed></u:Play></s:Body></s:Envelope>"""
        try {
            sendSoapAction(endpoint, "urn:schemas-upnp-org:service:AVTransport:1#Play", playSoap)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
