package com.samcod3.alldebrid.ui.screens.downloads

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import com.samcod3.alldebrid.data.repository.DlnaQueueItem
import javax.inject.Inject

data class DownloadsUiState(
    val isLoading: Boolean = false,
    val magnets: List<Magnet> = emptyList(),
    val error: String? = null,
    val requiresIpAuthorization: Boolean = false,
    val selectedDevice: Device? = null,
    val discoveredDevices: List<Device> = emptyList(),
    val castingMessage: String? = null,
    val showKodiQueueDialog: Boolean = false,
    val showDlnaQueueDialog: Boolean = false,
    val showNoDeviceDialog: Boolean = false,
    val pendingCastLink: String? = null,
    val pendingCastTitle: String? = null,
    val dlnaQueue: List<DlnaQueueItem> = emptyList(),
    val isPlaying: Boolean = false // Track if currently playing
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val repository: AllDebridRepository,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()
    
    private var messageClearJob: Job? = null

    init {
        observeSelectedDevice()
        observeDiscoveredDevices()
        observeDlnaQueue()
        refresh()
    }
    
    private fun observeSelectedDevice() {
        viewModelScope.launch {
            deviceRepository.getSelectedDevice().collect { device ->
                _uiState.update { it.copy(selectedDevice = device) }
            }
        }
    }
    
    private fun observeDiscoveredDevices() {
        viewModelScope.launch {
            deviceRepository.getDiscoveredDevices().collect { devices ->
                _uiState.update { it.copy(discoveredDevices = devices) }
            }
        }
    }
    
    private fun observeDlnaQueue() {
        viewModelScope.launch {
            deviceRepository.dlnaQueue.queue.collect { queue ->
                _uiState.update { it.copy(dlnaQueue = queue) }
            }
        }
    }
    
    fun selectDevice(device: Device) {
        viewModelScope.launch {
            deviceRepository.setSelectedDevice(device)
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
    
    fun refreshSilent() {
        viewModelScope.launch {
            // Don't set isLoading = true
            repository.getMagnets()
                .onSuccess { magnets ->
                    _uiState.update { it.copy(magnets = magnets) }
                }
                .onFailure { error ->
                    // Silently ignore or log error
                    // handleError(error) 
                }
        }
    }
    
    /**
     * Fetch files for a magnet using the new /v4/magnet/files endpoint.
     * Called by DownloadCard when user opens the bottom sheet.
     */
    suspend fun fetchMagnetFiles(magnetId: Long): Result<List<com.samcod3.alldebrid.data.model.FlatFile>> {
        return repository.getMagnetFiles(magnetId)
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

    fun playLink(link: String, title: String = "Video") {
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
                                        pendingCastLink = link,
                                        pendingCastTitle = title
                                    ) 
                                }
                            } else {
                                // Not playing, just cast
                                castLink(link, device, addToQueue = false, title = title)
                            }
                        }
                        .onFailure {
                            // If check fails, just cast
                            castLink(link, device, addToQueue = false, title = title)
                        }
                }
            } else if (device.type == com.samcod3.alldebrid.data.model.DeviceType.DLNA) {
                // DLNA: Check if queue has items - offer to add to queue
                if (deviceRepository.dlnaQueue.queueSize() > 0) {
                    _uiState.update {
                        it.copy(
                            showDlnaQueueDialog = true,
                            pendingCastLink = link,
                            pendingCastTitle = title
                        )
                    }
                } else {
                    // Queue is empty, play directly
                    castLink(link, device, addToQueue = false, title = title)
                }
            }
        } else {
            // No device selected - show dialog to help user
            _uiState.update { it.copy(showNoDeviceDialog = true, pendingCastLink = link, pendingCastTitle = title) }
        }
    }
    
    fun dismissNoDeviceDialog() {
        _uiState.update { it.copy(showNoDeviceDialog = false) }
    }
    
    fun dismissKodiQueueDialog() {
        _uiState.update { it.copy(showKodiQueueDialog = false, pendingCastLink = null, pendingCastTitle = null) }
    }
    
    fun dismissDlnaQueueDialog() {
        _uiState.update { it.copy(showDlnaQueueDialog = false, pendingCastLink = null, pendingCastTitle = null) }
    }
    
    fun playNow() {
        val link = _uiState.value.pendingCastLink
        val title = _uiState.value.pendingCastTitle ?: "Video"
        val device = _uiState.value.selectedDevice
        if (link != null && device != null) {
            dismissKodiQueueDialog()
            dismissDlnaQueueDialog()
            castLink(link, device, addToQueue = false, title = title)
        }
    }
    
    fun addToQueue() {
        val link = _uiState.value.pendingCastLink
        val title = _uiState.value.pendingCastTitle ?: "Video"
        val device = _uiState.value.selectedDevice
        if (link != null && device != null) {
            dismissKodiQueueDialog()
            dismissDlnaQueueDialog()
            castLink(link, device, addToQueue = true, title = title)
        }
    }
    
    fun playNextInDlnaQueue() {
        val device = _uiState.value.selectedDevice
        if (device != null && device.type == com.samcod3.alldebrid.data.model.DeviceType.DLNA) {
            viewModelScope.launch {
                _uiState.update { it.copy(castingMessage = "Playing next...") }
                deviceRepository.playNextInDlnaQueue(device)
                    .onSuccess {
                        _uiState.update { it.copy(castingMessage = "Playing next!", error = null) }
                        scheduleMessageClear()
                    }
                    .onFailure { error ->
                        _uiState.update { it.copy(castingMessage = null, error = "Queue: ${error.message}") }
                    }
            }
        }
    }
    
    fun removeFromDlnaQueue(itemId: String) {
        deviceRepository.dlnaQueue.removeFromQueue(itemId)
    }
    
    fun clearDlnaQueue() {
        deviceRepository.dlnaQueue.clearQueue()
    }
    
    // Playback Controls (works for both Kodi and DLNA)
    fun stopPlayback() {
        val device = _uiState.value.selectedDevice ?: return
        viewModelScope.launch {
            val result = when (device.type) {
                com.samcod3.alldebrid.data.model.DeviceType.DLNA -> deviceRepository.stopDlna(device)
                com.samcod3.alldebrid.data.model.DeviceType.KODI -> deviceRepository.stopKodi(device)
            }
            result
                .onSuccess { 
                    _uiState.update { it.copy(castingMessage = "Stopped", isPlaying = false) }
                }
                .onFailure { 
                    _uiState.update { it.copy(error = "Stop failed") } 
                }
            scheduleMessageClear()
        }
    }
    
    fun pausePlayback() {
        val device = _uiState.value.selectedDevice ?: return
        viewModelScope.launch {
            val result = when (device.type) {
                com.samcod3.alldebrid.data.model.DeviceType.DLNA -> deviceRepository.pauseDlna(device)
                com.samcod3.alldebrid.data.model.DeviceType.KODI -> deviceRepository.pauseKodi(device)
            }
            result
                .onSuccess { 
                    _uiState.update { it.copy(castingMessage = "Paused") }
                }
                .onFailure { 
                    _uiState.update { it.copy(error = "Pause failed") } 
                }
            scheduleMessageClear()
        }
    }
    
    fun resumePlayback() {
        val device = _uiState.value.selectedDevice ?: return
        viewModelScope.launch {
            // For Kodi, pause toggles play/pause, for DLNA use resume
            val result = when (device.type) {
                com.samcod3.alldebrid.data.model.DeviceType.DLNA -> deviceRepository.resumeDlna(device)
                com.samcod3.alldebrid.data.model.DeviceType.KODI -> deviceRepository.pauseKodi(device) // Toggle play/pause
            }
            result
                .onSuccess { 
                    _uiState.update { it.copy(castingMessage = "Playing", isPlaying = true) }
                }
                .onFailure { 
                    _uiState.update { it.copy(error = "Resume failed") } 
                }
            scheduleMessageClear()
        }
    }
    
    private fun castLink(link: String, device: Device, addToQueue: Boolean, title: String = "Video") {
        viewModelScope.launch {
            val cleanedTitle = cleanTitle(title)
            _uiState.update { it.copy(castingMessage = "Unlocking link...") }
            
            repository.unlockLink(link)
                .onSuccess { unlockedLink ->
                    val action = if (addToQueue) "Adding to queue..." else "Casting to ${device.name}..."
                    _uiState.update { it.copy(castingMessage = action) }
                    
                    deviceRepository.castToDevice(device, unlockedLink.link, addToQueue, cleanedTitle)
                        .onSuccess {
                             val message = if (addToQueue) "Added to queue!" else "Casting started!"
                             _uiState.update { it.copy(castingMessage = message, error = null, isPlaying = !addToQueue) }
                             scheduleMessageClear()
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

    private fun cleanTitle(originalTitle: String): String {
        // 1. Remove extension
        var title = originalTitle.substringBeforeLast('.')
        
        // 2. Remove URL phrases
        val urlRegex = Regex("(?i)(www\\.[\\w-]+\\.[a-z]+|\\.com|\\.org|\\.net)")
        title = title.replace(urlRegex, "")

        // 3. Remove Video/Audio quality & formats (with word boundaries)
        val formatRegex = Regex("""(?i)\b(1080p|720p|480p|2160p|4k|uhd|fhd|hd|sd|bluray|blu-ray|bdrip|brrip|web-dl|webdl|webrip|hdtv|dvdrip|dvdscr|hdcam|cam|ts|tc|h\.?264|x\.?264|h\.?265|x\.?265|hevc|avc|av1|aac|ac3|eac3|dts|dts-hd|dolby|atmos|truehd|hdr|hdr10|hdr10\+|sdr|10bit|8bit|proper|repack|remux|extended|unrated|directors\.?cut|multi|dual|latino|castellano|spanish|english|subbed|dubbed)\b""")
        title = title.replace(formatRegex, "")
        
        // 3b. Also remove concatenated format strings (no word boundaries) like "4kremux2160"
        val concatenatedRegex = Regex("""(?i)(2160p?|1080p?|720p?|480p?|4k|remux|bluray|webrip|webdl|hevc|x265|x264|hdr|uhd|aac|dts|ac3)""")
        title = title.replace(concatenatedRegex, "")

        // 4. Replace dots with spaces
        title = title.replace(".", " ")
        
        // Remove residual brackets [] ()
        title = title.replace(Regex("[\\[\\]\\(\\)]"), "")

        // 5. Trim whitespace and extra spaces
        title = title.replace(Regex("\\s+"), " ").trim()

        return title.ifBlank { originalTitle } // Fallback
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
    
    fun copyLinkToClipboard(context: Context, link: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(castingMessage = "Unlocking...") }
            repository.unlockLink(link)
                .onSuccess { unlockedLink ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("AllDebrid Link", unlockedLink.link)
                    clipboard.setPrimaryClip(clip)
                    _uiState.update { it.copy(castingMessage = "Link copied!") }
                    scheduleMessageClear()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(castingMessage = null, error = "Failed: ${error.message}") }
                }
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
        messageClearJob?.cancel()
        messageClearJob = null
    }
    
    fun clearAllMessages() {
        _uiState.update { it.copy(castingMessage = null, error = null) }
        messageClearJob?.cancel()
        messageClearJob = null
    }
    
    private fun scheduleMessageClear() {
        messageClearJob?.cancel()
        messageClearJob = viewModelScope.launch {
            delay(3000) // 3 seconds
            _uiState.update { it.copy(castingMessage = null) }
        }
    }
}
