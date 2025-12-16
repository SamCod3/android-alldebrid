package com.samcod3.alldebrid.discovery

import android.content.Context
import android.util.Log
import com.samcod3.alldebrid.data.api.KodiApi
import com.samcod3.alldebrid.data.api.KodiCommands
import com.samcod3.alldebrid.data.datastore.SettingsDataStore
import com.samcod3.alldebrid.data.model.Device
import com.samcod3.alldebrid.data.model.DeviceType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceDiscoveryManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val kodiApi: KodiApi,
    private val settingsDataStore: SettingsDataStore
) {
    
    companion object {
        private const val TAG = "DeviceDiscovery"
        private const val SSDP_ADDRESS = "239.255.255.250"
        private const val SSDP_PORT = 1900
        private const val DISCOVERY_TIMEOUT = 5000L
        private const val KODI_DEFAULT_PORT = 8080
        
        // DLNA Ports from Chrome Extension
        private val DLNA_PORTS = listOf(9197, 8060, 7676, 7678, 1234, 52235, 2869)
    }
    
    suspend fun discoverAll(): List<Device> = coroutineScope {
        val ssdpDevices = async { discoverSsdp() }
        val manualDevices = async { discoverManual() }
        
        val allDevices = mutableListOf<Device>()
        allDevices.addAll(ssdpDevices.await())
        allDevices.addAll(manualDevices.await())
        
        // Add saved device if it exists and hasn't been found
        val savedDevice = settingsDataStore.selectedDevice.first()
        if (savedDevice != null) {
            // Check if already found (by address and port)
            val alreadyFound = allDevices.any { 
                it.address == savedDevice.address && it.port == savedDevice.port 
            }
            if (!alreadyFound) {
                // If saved device is not in scan results, add it manually
                // We assume it's valid since user selected it before
                allDevices.add(savedDevice)
                Log.d(TAG, "Added saved device to results: ${savedDevice.name}")
            }
        }
        
        // Remove duplicates by address
        allDevices.distinctBy { it.address }
    }
    
    private suspend fun discoverSsdp(): List<Device> = withContext(Dispatchers.IO) {
        val devices = mutableListOf<Device>()
        var multicastLock: android.net.wifi.WifiManager.MulticastLock? = null
        
        try {
            // Acquire multicast lock - critical for receiving SSDP responses
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
            multicastLock = wifiManager.createMulticastLock("AllDebrid_SSDP")
            multicastLock.acquire()
            Log.d(TAG, "Multicast lock acquired")
            
            val socket = DatagramSocket()
            socket.soTimeout = DISCOVERY_TIMEOUT.toInt()
            socket.broadcast = true
            
            // SSDP M-SEARCH request for MediaRenderer devices
            val searchMessage = """
                M-SEARCH * HTTP/1.1
                HOST: $SSDP_ADDRESS:$SSDP_PORT
                MAN: "ssdp:discover"
                MX: 3
                ST: urn:schemas-upnp-org:device:MediaRenderer:1
                
            """.trimIndent().replace("\n", "\r\n")
            
            val sendData = searchMessage.toByteArray()
            val sendPacket = DatagramPacket(
                sendData,
                sendData.size,
                InetAddress.getByName(SSDP_ADDRESS),
                SSDP_PORT
            )
            
            socket.send(sendPacket)
            Log.d(TAG, "Sent SSDP M-SEARCH to $SSDP_ADDRESS:$SSDP_PORT")
            
            // Receive responses
            val receiveData = ByteArray(2048)
            val startTime = System.currentTimeMillis()
            
            while (System.currentTimeMillis() - startTime < DISCOVERY_TIMEOUT) {
                try {
                    val receivePacket = DatagramPacket(receiveData, receiveData.size)
                    socket.receive(receivePacket)
                    
                    val response = String(receivePacket.data, 0, receivePacket.length)
                    Log.d(TAG, "SSDP Response from ${receivePacket.address.hostAddress}")
                    
                    parseSsdpResponse(response, receivePacket.address.hostAddress ?: "")?.let {
                        devices.add(it)
                        Log.d(TAG, "Found DLNA device: ${it.name} at ${it.address}")
                    }
                } catch (e: Exception) {
                    // Timeout or other error, continue
                }
            }
            
            socket.close()
            Log.d(TAG, "SSDP discovery completed, found ${devices.size} devices")
        } catch (e: Exception) {
            Log.e(TAG, "SSDP discovery error", e)
        } finally {
            // Always release multicast lock
            multicastLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d(TAG, "Multicast lock released")
                }
            }
        }
        
        devices
    }
    
    private suspend fun parseSsdpResponse(response: String, address: String): Device? = withContext(Dispatchers.IO) {
        val locationRegex = Regex("LOCATION:\\s*(.+)", RegexOption.IGNORE_CASE)
        val serverRegex = Regex("SERVER:\\s*(.+)", RegexOption.IGNORE_CASE)
        
        val location = locationRegex.find(response)?.groupValues?.get(1)?.trim()
        val server = serverRegex.find(response)?.groupValues?.get(1)?.trim() ?: "Unknown Device"
        
        if (location != null) {
            // Try to fetch friendlyName from device description
            val friendlyName = fetchDeviceFriendlyName(location) ?: extractDeviceName(server)
            
            Device(
                id = UUID.randomUUID().toString(),
                name = friendlyName,
                address = address,
                port = extractPort(location),
                type = DeviceType.DLNA,
                controlUrl = location
            )
        } else null
    }
    
    private suspend fun fetchDeviceFriendlyName(locationUrl: String): String? = withTimeoutOrNull(3000) {
        try {
            Log.d(TAG, "Fetching device description from: $locationUrl")
            val url = java.net.URL(locationUrl)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 2000
            connection.readTimeout = 2000
            val xml = connection.inputStream.bufferedReader().readText()
            connection.disconnect()
            
            Log.d(TAG, "Device description XML length: ${xml.length} chars")
            
            // Parse friendlyName from XML (try multiple patterns)
            val friendlyNameRegex = Regex("<friendlyName>(.+?)</friendlyName>", RegexOption.IGNORE_CASE)
            val modelNameRegex = Regex("<modelName>(.+?)</modelName>", RegexOption.IGNORE_CASE)
            
            val friendlyName = friendlyNameRegex.find(xml)?.groupValues?.get(1)?.trim()
            val modelName = modelNameRegex.find(xml)?.groupValues?.get(1)?.trim()
            
            val result = friendlyName ?: modelName
            Log.d(TAG, "Parsed device name: $result (friendlyName=$friendlyName, modelName=$modelName)")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch device description from $locationUrl: ${e.message}")
            null
        }
    }
    
    private fun extractDeviceName(server: String): String {
        // Try to extract a friendly name from SERVER header
        return server.split("/").firstOrNull()?.trim() ?: server
    }
    
    private fun extractPort(location: String): Int {
        val portRegex = Regex(":(\\d+)")
        return portRegex.find(location)?.groupValues?.get(1)?.toIntOrNull() ?: 80
    }
    
    // Combined manual scan for Kodi and DLNA ports
    private suspend fun discoverManual(): List<Device> = withContext(Dispatchers.IO) {
        val devices = mutableListOf<Device>()
        
        // Check if custom IP range is enabled
        val useCustomRange = settingsDataStore.useCustomIpRange.first()
        val customPrefix = settingsDataStore.customIpPrefix.first()
        
        val localIpPrefix = if (useCustomRange && customPrefix.isNotBlank()) {
            Log.d(TAG, "Using custom IP range: $customPrefix")
            customPrefix
        } else {
            getLocalIpPrefix()
        }
        
        if (localIpPrefix == null) {
            Log.w(TAG, "Could not determine local network prefix")
            return@withContext devices
        }
        
        Log.d(TAG, "Scanning network: $localIpPrefix.x for devices (Kodi & DLNA)")
        
        // Scan common IP range (1-254)
        coroutineScope {
            val scanJobs = (1..254).map { i ->
                async {
                    val ip = "$localIpPrefix.$i"
                    val foundDevices = mutableListOf<Device>()
                    
                    // 1. Check Kodi
                    checkKodiDevice(ip)?.let { foundDevices.add(it) }
                    
                    // 2. Check DLNA ports if custom range is enabled (to save time in production)
                    // If we suspect emulation or issues with multicast, manual port scan is useful.
                    if (useCustomRange || foundDevices.isEmpty()) {
                        checkDlnaPorts(ip)?.let { foundDevices.add(it) }
                    }
                    
                    foundDevices
                }
            }
            
            scanJobs.awaitAll().flatten().forEach { device ->
                devices.add(device)
            }
        }
        
        devices
    }
    
    private suspend fun checkKodiDevice(ip: String): Device? = withTimeoutOrNull(1000) {
        try {
            val url = "http://$ip:$KODI_DEFAULT_PORT/jsonrpc"
            val response = kodiApi.sendCommand(url, KodiCommands.ping())
            
            if (response.isSuccessful && response.body()?.result == "pong") {
                Log.d(TAG, "Found Kodi at $ip")
                Device(
                    id = UUID.randomUUID().toString(),
                    name = "Kodi ($ip)",
                    address = ip,
                    port = KODI_DEFAULT_PORT,
                    type = DeviceType.KODI
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun checkDlnaPorts(ip: String): Device? = withContext(Dispatchers.IO) {
        for (port in DLNA_PORTS) {
            try {
                // Try quick socket connection first
                val socket = java.net.Socket()
                val address = java.net.InetSocketAddress(ip, port)
                socket.connect(address, 200) // 200ms timeout per port
                socket.close()
                
                Log.d(TAG, "Found potential DLNA port $port open at $ip")
                
                // Try to get device description to find the friendly name
                val descriptionUrl = "http://$ip:$port/description.xml"
                val friendlyName = fetchDeviceFriendlyName(descriptionUrl) 
                    ?: fetchDeviceFriendlyName("http://$ip:$port/dmr/description.xml")
                    ?: fetchDeviceFriendlyName("http://$ip:$port/DeviceDescription.xml")
                
                val deviceName = friendlyName ?: "DLNA ($ip)"
                Log.d(TAG, "DLNA device name resolved to: $deviceName")
                
                return@withContext Device(
                    id = UUID.randomUUID().toString(),
                    name = deviceName,
                    address = ip,
                    port = port,
                    type = DeviceType.DLNA,
                    controlUrl = "http://$ip:$port/"
                )
            } catch (e: Exception) {
                // Ignore connection failure
            }
        }
        null
    }
    
    private fun getLocalIpPrefix(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue
                
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (address.isSiteLocalAddress && !address.isLoopbackAddress) {
                        val ip = address.hostAddress ?: continue
                        if (ip.contains(".")) {
                            return ip.substringBeforeLast(".")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting local IP", e)
        }
        return null
    }
}
