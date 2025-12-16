package com.samcod3.alldebrid.data.repository

import com.samcod3.alldebrid.data.api.KodiApi
import com.samcod3.alldebrid.data.api.KodiCommands
import com.samcod3.alldebrid.data.datastore.SettingsDataStore
import com.samcod3.alldebrid.data.model.Device
import com.samcod3.alldebrid.data.model.DeviceType
import com.samcod3.alldebrid.discovery.DeviceDiscoveryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    
    fun getDiscoveredDevices(): Flow<List<Device>> = _devices.asStateFlow()
    
    fun getSelectedDevice(): Flow<Device?> = settingsDataStore.selectedDevice
    
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
        settingsDataStore.saveSelectedDevice(device)
    }
    
    suspend fun clearSelectedDevice() {
        settingsDataStore.clearSelectedDevice()
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
                Result.failure(Exception("Kodi playback failed: ${response.body()?.error}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun castToDlna(device: Device, videoUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Basic DLNA casting via SOAP
            // 1. SetAVTransportURI
            // 2. Play
            
            val controlUrl = device.controlUrl ?: "http://${device.address}:${device.port}/"
            // Note: In a real UPnP impl, we need the specific control URL for AVTransport service.
            // For now, we try checking if we can guess or if discovery provided a better URL.
            // If discovery gave us a root desc URL (e.g. http://ip:port/desc.xml), we likely need to resolve it.
            // For simple implementation (emulator testing), we assume standard paths or try common ones if controlUrl is just root.
            
            // NOTE: This is a simplified "Blind Cast" approach. Better approach parses description.xml.
            // Assuming controlUrl might need suffix like /AVTransport/control or /Control/AVTransport
            
            // Construct target URL (often requires service path)
            // Ideally we should have parsed this during discovery. 
            // For now let's try a common heuristic or use what discovery provided.
            
            val targetUrl = if (controlUrl.endsWith(".xml")) {
                 // It's likely description URL, need to find control URL. 
                 // Skipping XML parsing for now, assuming standard paths.
                 "http://${device.address}:${device.port}/AVTransport/control" 
            } else {
                 controlUrl
            }

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
            
            sendSoapAction(targetUrl, "urn:schemas-upnp-org:service:AVTransport:1#SetAVTransportURI", setUriSoap)
            
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
            
            sendSoapAction(targetUrl, "urn:schemas-upnp-org:service:AVTransport:1#Play", playSoap)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
