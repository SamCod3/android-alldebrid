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
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
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
    
    /**
     * Fast discovery using SSDP only.
     * For each device found, checks if it's Kodi (responds to JSON-RPC ping).
     * This is the default discovery method - fast (~3 seconds).
     */
    suspend fun discoverAll(): List<Device> = coroutineScope {
        val ssdpDevices = discoverSsdp()
        
        val allDevices = mutableListOf<Device>()
        allDevices.addAll(ssdpDevices)
        
        // Add saved device if it exists and hasn't been found
        val savedDevice = settingsDataStore.selectedDevice.first()
        if (savedDevice != null) {
            val alreadyFound = allDevices.any { 
                it.address == savedDevice.address && it.port == savedDevice.port 
            }
            if (!alreadyFound) {
                allDevices.add(savedDevice)
                Log.d(TAG, "Added saved device to results: ${savedDevice.name}")
            }
        }
        
        allDevices.distinctBy { it.address }
    }
    
    /**
     * Hybrid discovery - SSDP + quick Kodi-only subnet scan in parallel.
     * Best option when router doesn't support multicast (SSDP fails).
     * Faster than manual scan (~5-8 seconds) because it only checks Kodi port 8080.
     */
    suspend fun discoverHybrid(): List<Device> = coroutineScope {
        Log.d(TAG, "Starting hybrid discovery (SSDP + quick Kodi scan)...")

        // Run SSDP and quick Kodi scan in parallel
        val ssdpJob = async { discoverSsdp() }
        val kodiScanJob = async { discoverKodiQuick() }

        val ssdpDevices = ssdpJob.await()
        val kodiDevices = kodiScanJob.await()

        Log.d(TAG, "Hybrid: SSDP found ${ssdpDevices.size}, Kodi scan found ${kodiDevices.size}")

        val allDevices = mutableListOf<Device>()
        allDevices.addAll(ssdpDevices)
        allDevices.addAll(kodiDevices)

        // Add saved device if not found
        val savedDevice = settingsDataStore.selectedDevice.first()
        if (savedDevice != null) {
            val alreadyFound = allDevices.any {
                it.address == savedDevice.address && it.port == savedDevice.port
            }
            if (!alreadyFound) {
                allDevices.add(savedDevice)
            }
        }

        Log.d(TAG, "Hybrid discovery completed, found ${allDevices.distinctBy { it.address }.size} unique devices")
        allDevices.distinctBy { it.address }
    }

    /**
     * Quick subnet scan for Kodi + DLNA with short timeouts.
     * Checks Kodi port 8080 and common DLNA ports.
     * Auto-detects WiFi subnet from phone's network interface.
     */
    private suspend fun discoverKodiQuick(): List<Device> = withContext(Dispatchers.IO) {
        val devices = mutableListOf<Device>()

        val localIpPrefix = getLocalIpPrefix()
        if (localIpPrefix == null) {
            Log.w(TAG, "Could not determine local network prefix for quick scan")
            return@withContext devices
        }

        Log.d(TAG, "Quick Kodi+DLNA scan on network: $localIpPrefix.x")

        // Higher concurrency for speed (50 parallel connections)
        val semaphore = Semaphore(50)

        coroutineScope {
            val scanJobs = (1..254).map { i ->
                async {
                    semaphore.withPermit {
                        checkDeviceQuick("$localIpPrefix.$i")
                    }
                }
            }

            scanJobs.awaitAll().filterNotNull().forEach { device ->
                devices.add(device)
            }
        }

        devices
    }

    /**
     * Quick device check - Kodi first, then DLNA ports
     */
    private suspend fun checkDeviceQuick(ip: String): Device? {
        // 1. Check Kodi first (most common)
        checkKodiQuick(ip)?.let { return it }

        // 2. Check common DLNA ports
        checkDlnaQuick(ip)?.let { return it }

        return null
    }

    /**
     * Quick Kodi check - 300ms timeout
     */
    private suspend fun checkKodiQuick(ip: String): Device? = withTimeoutOrNull(350) {
        try {
            val socket = java.net.Socket()
            val address = java.net.InetSocketAddress(ip, KODI_DEFAULT_PORT)
            socket.connect(address, 250)
            socket.close()

            // Port is open, verify it's Kodi
            val url = "http://$ip:$KODI_DEFAULT_PORT/jsonrpc"
            val response = kodiApi.sendCommand(url, KodiCommands.ping())

            if (response.isSuccessful && response.body()?.result == "pong") {
                Log.d(TAG, "Quick scan found Kodi at $ip")
                val kodiName = getKodiSystemName(ip, KODI_DEFAULT_PORT) ?: "Kodi"

                Device(
                    id = UUID.randomUUID().toString(),
                    name = kodiName,
                    address = ip,
                    port = KODI_DEFAULT_PORT,
                    type = DeviceType.KODI
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Quick DLNA check - tries common ports with short timeout
     */
    private suspend fun checkDlnaQuick(ip: String): Device? {
        // Only check the most common DLNA ports for speed
        val quickPorts = listOf(9197, 8060, 7676)

        for (port in quickPorts) {
            try {
                val device = withTimeoutOrNull(300) {
                    val socket = java.net.Socket()
                    val address = java.net.InetSocketAddress(ip, port)
                    socket.connect(address, 200)
                    socket.close()

                    Log.d(TAG, "Quick scan found open port $port at $ip")

                    // Try to get device name
                    val endpoints = listOf(
                        "http://$ip:$port/dmr",
                        "http://$ip:$port/description.xml"
                    )

                    var friendlyName: String? = null
                    for (endpoint in endpoints) {
                        friendlyName = fetchDeviceFriendlyName(endpoint)
                        if (friendlyName != null) break
                    }

                    val deviceName = friendlyName ?: "DLNA ($ip)"
                    Log.d(TAG, "Quick scan found DLNA: $deviceName at $ip:$port")

                    Device(
                        id = UUID.randomUUID().toString(),
                        name = deviceName,
                        address = ip,
                        port = port,
                        type = DeviceType.DLNA,
                        controlUrl = "http://$ip:$port/"
                    )
                }
                if (device != null) return device
            } catch (e: Exception) {
                // Continue to next port
            }
        }
        return null
    }

    /**
     * Slow manual discovery - scans all IPs in subnet.
     * Use only when SSDP doesn't find devices.
     * This is much slower (~30-60 seconds).
     */
    suspend fun discoverManualScan(): List<Device> = coroutineScope {
        Log.d(TAG, "Starting manual IP scan...")
        val manualDevices = discoverManual()
        
        val allDevices = mutableListOf<Device>()
        allDevices.addAll(manualDevices)
        
        // Add saved device if not found
        val savedDevice = settingsDataStore.selectedDevice.first()
        if (savedDevice != null) {
            val alreadyFound = allDevices.any { 
                it.address == savedDevice.address && it.port == savedDevice.port 
            }
            if (!alreadyFound) {
                allDevices.add(savedDevice)
            }
        }
        
        Log.d(TAG, "Manual scan completed, found ${allDevices.size} devices")
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
        val port = if (location != null) extractPort(location) else 80
        
        if (location != null) {
            // First, check if this device is Kodi (try common Kodi ports)
            val kodiDevice = checkIfKodi(address, KODI_DEFAULT_PORT) 
                ?: checkIfKodi(address, port)
            
            if (kodiDevice != null) {
                Log.d(TAG, "SSDP device at $address is Kodi: ${kodiDevice.name}")
                return@withContext kodiDevice
            }
            
            // Not Kodi, treat as DLNA device
            // Try to get friendlyName from SSDP location first, then try multiple endpoints
            var friendlyName = fetchDeviceFriendlyName(location)
            
            if (friendlyName == null) {
                // Try multiple endpoints like manual scan does
                val endpoints = listOf(
                    "http://$address:$port/dmr",
                    "http://$address:$port/DeviceDescription.xml",
                    "http://$address:$port/description.xml",
                    "http://$address:$port/dmr/description.xml",
                    "http://$address:$port/upnp/devicedesc.xml",
                    "http://$address:$port/device.xml"
                )
                
                for (endpoint in endpoints) {
                    friendlyName = fetchDeviceFriendlyName(endpoint)
                    if (friendlyName != null) {
                        Log.d(TAG, "Found friendlyName '$friendlyName' at $endpoint")
                        break
                    }
                }
            }
            
            val deviceName = friendlyName ?: extractDeviceName(server)
            Log.d(TAG, "SSDP device at $address is DLNA: $deviceName")
            
            Device(
                id = UUID.randomUUID().toString(),
                name = deviceName,
                address = address,
                port = port,
                type = DeviceType.DLNA,
                controlUrl = location
            )
        } else null
    }
    
    private suspend fun fetchDeviceFriendlyName(locationUrl: String): String? = withTimeoutOrNull(3000) {
        var connection: java.net.HttpURLConnection? = null
        try {
            Log.d(TAG, "Fetching device description from: $locationUrl")
            val url = java.net.URL(locationUrl)
            connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 2000
            connection.readTimeout = 2000
            val xml = connection.inputStream.bufferedReader().readText()
            
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
        } finally {
            connection?.disconnect()
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
        // Limit concurrent requests to reduce battery/network load
        val semaphore = Semaphore(30)
        
        coroutineScope {
            val scanJobs = (1..254).map { i ->
                async {
                    semaphore.withPermit {
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
            }
            
            scanJobs.awaitAll().flatten().forEach { device ->
                devices.add(device)
            }
        }
        
        devices
    }
    
    private suspend fun checkKodiDevice(ip: String): Device? = withTimeoutOrNull(1500) {
        try {
            val url = "http://$ip:$KODI_DEFAULT_PORT/jsonrpc"
            val response = kodiApi.sendCommand(url, KodiCommands.ping())
            
            if (response.isSuccessful && response.body()?.result == "pong") {
                Log.d(TAG, "Found Kodi at $ip")
                
                // Try to get the system name
                val kodiName = getKodiSystemName(ip, KODI_DEFAULT_PORT) ?: "Kodi"
                Log.d(TAG, "Kodi name: $kodiName")
                
                Device(
                    id = UUID.randomUUID().toString(),
                    name = kodiName,
                    address = ip,
                    port = KODI_DEFAULT_PORT,
                    type = DeviceType.KODI
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun getKodiSystemName(ip: String, port: Int): String? {
        return try {
            val url = "http://$ip:$port/jsonrpc"
            val response = kodiApi.sendCommand(url, KodiCommands.getSystemName())
            if (response.isSuccessful) {
                val result = response.body()?.result
                if (result is Map<*, *>) {
                    result["name"]?.toString()
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun checkIfKodi(ip: String, port: Int): Device? {
        return try {
            val url = "http://$ip:$port/jsonrpc"
            val response = withTimeoutOrNull(1000) {
                kodiApi.sendCommand(url, KodiCommands.ping())
            }
            
            if (response?.isSuccessful == true && response.body()?.result == "pong") {
                Log.d(TAG, "Device at $ip:$port is Kodi!")
                val kodiName = getKodiSystemName(ip, port) ?: "Kodi"
                Device(
                    id = UUID.randomUUID().toString(),
                    name = kodiName,
                    address = ip,
                    port = port,
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
                
                // First, check if it's actually Kodi on this port
                val kodiDevice = checkIfKodi(ip, port)
                if (kodiDevice != null) {
                    Log.d(TAG, "Device at $ip:$port identified as Kodi: ${kodiDevice.name}")
                    return@withContext kodiDevice
                }
                
                // Not Kodi, try to get DLNA device description from multiple endpoints
                // These are all the endpoints the Chrome extension tries
                val endpoints = listOf(
                    "http://$ip:$port/dmr",
                    "http://$ip:$port/DeviceDescription.xml",
                    "http://$ip:$port/description.xml",
                    "http://$ip:$port/dmr/description.xml",
                    "http://$ip:$port/upnp/devicedesc.xml",
                    "http://$ip:$port/device.xml",
                    "http://$ip:$port/smp_8_"
                )
                
                var friendlyName: String? = null
                for (endpoint in endpoints) {
                    friendlyName = fetchDeviceFriendlyName(endpoint)
                    if (friendlyName != null) {
                        Log.d(TAG, "Found friendlyName '$friendlyName' at $endpoint")
                        break
                    }
                }
                
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
            val candidates = mutableListOf<Pair<String, String>>() // (interfaceName, ipPrefix)

            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue

                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (address.isSiteLocalAddress && !address.isLoopbackAddress) {
                        val ip = address.hostAddress ?: continue
                        if (ip.contains(".") && !ip.contains(":")) { // IPv4 only
                            val prefix = ip.substringBeforeLast(".")
                            candidates.add(networkInterface.name to prefix)
                            Log.d(TAG, "Found network: ${networkInterface.name} -> $prefix.x")
                        }
                    }
                }
            }

            // Prioritize WiFi interfaces (wlan0, wlan1, etc.)
            val wifiInterface = candidates.find { it.first.startsWith("wlan") }
            if (wifiInterface != null) {
                Log.d(TAG, "Using WiFi interface: ${wifiInterface.first} -> ${wifiInterface.second}.x")
                return wifiInterface.second
            }

            // Fallback to first available
            val first = candidates.firstOrNull()
            if (first != null) {
                Log.d(TAG, "Using fallback interface: ${first.first} -> ${first.second}.x")
                return first.second
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting local IP", e)
        }
        return null
    }
}
