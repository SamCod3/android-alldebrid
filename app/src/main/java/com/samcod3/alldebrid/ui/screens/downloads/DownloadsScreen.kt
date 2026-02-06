package com.samcod3.alldebrid.ui.screens.downloads

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import com.samcod3.alldebrid.ui.components.AppSnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.samcod3.alldebrid.ui.theme.Spacing
import com.samcod3.alldebrid.R
import com.samcod3.alldebrid.ui.components.DownloadCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel = hiltViewModel(),
    onNavigateToIpAuth: () -> Unit = {},
    onNavigateToDevices: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Filter states - moved here to persist across recompositions
    var searchFilter by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ready") }

    // Dialogs
    if (uiState.requiresIpAuthorization) {
        IpAuthorizationDialog(
            onDismiss = { viewModel.clearIpAuthorizationFlag() },
            onAuthorize = {
                viewModel.clearIpAuthorizationFlag()
                onNavigateToIpAuth()
            }
        )
    }

    if (uiState.showNoDeviceDialog) {
        NoDeviceSelectedDialog(
            hasDevices = uiState.discoveredDevices.isNotEmpty(),
            onDismiss = { viewModel.dismissNoDeviceDialog() },
            onNavigateToDevices = {
                viewModel.dismissNoDeviceDialog()
                onNavigateToDevices()
            }
        )
    }

    if (uiState.showKodiQueueDialog) {
        KodiQueueDialog(
            onDismiss = { viewModel.dismissKodiQueueDialog() },
            onPlayNow = { viewModel.playNow() },
            onAddToQueue = { viewModel.addToQueue() }
        )
    }

    if (uiState.showDlnaQueueDialog) {
        DlnaQueueDialog(
            queueSize = uiState.dlnaQueue.size,
            onDismiss = { viewModel.dismissDlnaQueueDialog() },
            onPlayNow = { viewModel.playNow() },
            onAddToQueue = { viewModel.addToQueue() }
        )
    }

    var showDeviceSelector by remember { mutableStateOf(false) }

    if (showDeviceSelector) {
        DeviceSelectorDialog(
            devices = uiState.discoveredDevices,
            selectedDevice = uiState.selectedDevice,
            dlnaQueue = uiState.dlnaQueue,
            onDismiss = { showDeviceSelector = false },
            onSelectDevice = { device ->
                viewModel.selectDevice(device)
                showDeviceSelector = false
            },
            onNavigateToDevices = {
                showDeviceSelector = false
                onNavigateToDevices()
            },
            onPlayNext = {
                viewModel.playNextInDlnaQueue()
                showDeviceSelector = false
            },
            onClearQueue = { viewModel.clearDlnaQueue() }
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show casting messages as Snackbar
    LaunchedEffect(uiState.castingMessage) {
        uiState.castingMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.refresh() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.downloads_refresh)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Cast control bar - compact with playback controls when playing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Device name (show when connected) + Playback controls (only when playing)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Device name with ellipsis
                    if (uiState.selectedDevice != null) {
                        Text(
                            text = uiState.selectedDevice?.displayName ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 150.dp)
                        )
                    }
                    
                    // Playback controls (only show when actively playing)
                    if (uiState.isPlaying && uiState.selectedDevice != null) {
                        // Stop button
                        IconButton(
                            onClick = { viewModel.stopPlayback() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Stop,
                                contentDescription = "Stop",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        // Pause button
                        IconButton(
                            onClick = { viewModel.pausePlayback() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Pause,
                                contentDescription = "Pause",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Play/Resume button
                        IconButton(
                            onClick = { viewModel.resumePlayback() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Play",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Cast icon (always visible) - shows connected/disconnected state
                IconButton(
                    onClick = { 
                        if (uiState.discoveredDevices.isEmpty()) {
                            onNavigateToDevices()
                        } else {
                            showDeviceSelector = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (uiState.selectedDevice != null) 
                            Icons.Rounded.CastConnected else Icons.Rounded.Cast,
                        contentDescription = "Cast",
                        tint = if (uiState.selectedDevice != null)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Content
            Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null && 
                !uiState.requiresIpAuthorization && 
                !uiState.error!!.contains("No device selected") -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Text(
                            text = uiState.error ?: stringResource(R.string.error_network),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                uiState.magnets.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.downloads_empty),
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    // Apply filters
                    val filteredMagnets = uiState.magnets.filter { magnet ->
                        val matchesSearch = searchFilter.isBlank() || magnet.filename.contains(searchFilter, ignoreCase = true)
                        val matchesStatus = when (statusFilter) {
                            "downloading" -> magnet.status != "Ready"
                            "ready" -> magnet.status == "Ready"
                            else -> true
                        }
                        matchesSearch && matchesStatus
                    }
                    
                    // Count for chips
                    val downloadingCount = uiState.magnets.count { it.status != "Ready" }
                    val readyCount = uiState.magnets.count { it.status == "Ready" }
                    
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column {
                            // Search filter
                            OutlinedTextField(
                            value = searchFilter,
                            onValueChange = { searchFilter = it },
                            placeholder = { Text("Filtrar por nombre...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                            shape = MaterialTheme.shapes.medium, // Less rounded (16dp)
                            singleLine = true,
                            trailingIcon = {
                                if (searchFilter.isNotBlank()) {
                                    IconButton(onClick = { searchFilter = "" }) {
                                        Icon(Icons.Rounded.Close, "Clear")
                                    }
                                }
                            }
                        )
                        
                        // Status filter chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.lg, vertical = Spacing.xs)
                                .horizontalScroll(rememberScrollState()), // Fix: Horizontal scroll to prevent wrapping
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            FilterChip(
                                selected = statusFilter == "ready",
                                onClick = { statusFilter = "ready" },
                                label = { Text("Downloaded ($readyCount)", style = MaterialTheme.typography.bodySmall) },
                                leadingIcon = { 
                                    if (statusFilter == "ready") {
                                        Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(16.dp))
                                    }
                                },
                                modifier = Modifier.height(32.dp) // Compact height
                            )
                            FilterChip(
                                selected = statusFilter == "downloading",
                                onClick = { statusFilter = "downloading" },
                                label = { Text("Downloading ($downloadingCount)", style = MaterialTheme.typography.bodySmall) },
                                leadingIcon = {
                                    if (statusFilter == "downloading") {
                                        Icon(Icons.Rounded.Downloading, null, modifier = Modifier.size(16.dp))
                                    }
                                },
                                modifier = Modifier.height(32.dp) // Compact height
                            )
                            FilterChip(
                                selected = statusFilter == "all",
                                onClick = { statusFilter = "all" },
                                label = { Text("All", style = MaterialTheme.typography.bodySmall) },
                                leadingIcon = {
                                    if (statusFilter == "all") {
                                         Icon(Icons.Rounded.List, null, modifier = Modifier.size(16.dp))
                                    }
                                },
                                modifier = Modifier.height(32.dp) // Compact height
                            )
                        }
                        
                        LazyColumn(
                                contentPadding = PaddingValues(Spacing.lg),
                                verticalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                items(
                                    items = filteredMagnets,
                                    key = { it.id }
                                ) { magnet ->
                                    val context = LocalContext.current
                                    val isReady = magnet.status == "Ready"
                                    DownloadCard(
                                        magnet = magnet,
                                        onCopyLink = { link ->
                                            viewModel.copyLinkToClipboard(context, link)
                                        },
                                        onPlay = { link, title -> viewModel.playLink(link, title) },
                                        onFetchFiles = { magnetId -> viewModel.fetchMagnetFiles(magnetId) },
                                        onDelete = { id -> viewModel.deleteMagnet(id) },
                                        onShareLink = if (isReady) {
                                            { link, filename ->
                                                viewModel.shareLink(context, link, filename)
                                            }
                                        } else null,
                                        showDeleteButton = !isReady,
                                        refreshCallback = { viewModel.refreshSilent() }
                                    )
                                }

                                // No results message
                                if (filteredMagnets.isEmpty()) {
                                    item {
                                        Text(
                                            text = if (searchFilter.isNotBlank())
                                                "No se encontraron resultados para \"$searchFilter\""
                                            else
                                                "No hay elementos en esta categoría",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(Spacing.lg)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
            
        }
    }
}
