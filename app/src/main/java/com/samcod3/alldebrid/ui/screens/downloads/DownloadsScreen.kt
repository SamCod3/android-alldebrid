package com.samcod3.alldebrid.ui.screens.downloads

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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

    // Show IP Authorization dialog when needed
    if (uiState.requiresIpAuthorization) {
        AlertDialog(
            onDismissRequest = { viewModel.clearIpAuthorizationFlag() },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("IP Authorization Required") },
            text = { 
                Text("AllDebrid has detected a new IP address (VPN?). You need to authorize this IP to continue using the service.")
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.clearIpAuthorizationFlag()
                    onNavigateToIpAuth()
                }) {
                    Text("Authorize IP")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearIpAuthorizationFlag() }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Show Device Selection Required dialog
    if (uiState.showNoDeviceDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissNoDeviceDialog() },
            icon = { Icon(Icons.Default.Cast, null) },
            title = { Text("No Device Selected") },
            text = { 
                Column {
                    Text("Please select a device to cast media to.")
                    if (uiState.discoveredDevices.isEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "No devices found. Go to Devices tab to discover devices on your network.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.dismissNoDeviceDialog()
                    onNavigateToDevices()
                }) {
                    Text(if (uiState.discoveredDevices.isEmpty()) "Discover Devices" else "Select Device")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissNoDeviceDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Show Kodi Queue Dialog
    if (uiState.showKodiQueueDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissKodiQueueDialog() },
            icon = { Icon(Icons.Default.PlayArrow, null) },
            title = { Text("Kodi is Playing") },
            text = { Text("Kodi is currently playing content. Do you want to play this now or add it to the queue?") },
            confirmButton = {
                Button(onClick = { viewModel.playNow() }) {
                    Text("Play Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.addToQueue() }) {
                    Text("Add to Queue")
                }
            }
        )
    }
    
    // Show DLNA Queue Dialog
    if (uiState.showDlnaQueueDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDlnaQueueDialog() },
            icon = { Icon(Icons.Default.Cast, null) },
            title = { Text("Queue has items") },
            text = { 
                Column {
                    Text("You have ${uiState.dlnaQueue.size} video(s) in queue.")
                    Spacer(Modifier.height(8.dp))
                    Text("Do you want to play this now or add it to the queue?")
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.playNow() }) {
                    Text("Play Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.addToQueue() }) {
                    Text("Add to Queue")
                }
            }
        )
    }

    // Device selector dialog
    var showDeviceSelector by remember { mutableStateOf(false) }
    
    if (showDeviceSelector) {
        AlertDialog(
            onDismissRequest = { showDeviceSelector = false },
            icon = { Icon(Icons.Default.Cast, null) },
            title = { Text("Select Device") },
            text = {
                Column {
                    if (uiState.discoveredDevices.isEmpty()) {
                        Text("No devices found. Tap 'Discover' to scan.")
                    } else {
                        uiState.discoveredDevices.forEach { device ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectDevice(device)
                                        showDeviceSelector = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.selectedDevice?.id == device.id,
                                    onClick = {
                                        viewModel.selectDevice(device)
                                        showDeviceSelector = false
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(device.displayName, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${device.type.name} • ${device.address}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        // DLNA Queue info
                        if (uiState.selectedDevice?.type == com.samcod3.alldebrid.data.model.DeviceType.DLNA && 
                            uiState.dlnaQueue.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Queue: ${uiState.dlnaQueue.size} video(s)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row {
                                    TextButton(
                                        onClick = { 
                                            viewModel.playNextInDlnaQueue()
                                            showDeviceSelector = false
                                        }
                                    ) {
                                        Text("Play Next")
                                    }
                                    TextButton(
                                        onClick = { viewModel.clearDlnaQueue() }
                                    ) {
                                        Text("Clear", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showDeviceSelector = false
                    onNavigateToDevices()
                }) {
                    Icon(Icons.Default.Search, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Discover")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeviceSelector = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.refresh() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
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
            // Cast device row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Show device name if selected
                uiState.selectedDevice?.let { device ->
                    Text(
                        text = "📺 ${device.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } ?: Text(
                    text = "No device",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Cast icon with device selector
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
                            Icons.Default.CastConnected else Icons.Default.Cast,
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    // Filter states
                    var searchFilter by remember { mutableStateOf("") }
                    var statusFilter by remember { mutableStateOf("ready") } // Default to ready (Disponibles)
                    
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
                    
                    Column {
                        // Search filter
                        OutlinedTextField(
                            value = searchFilter,
                            onValueChange = { searchFilter = it },
                            placeholder = { Text("Filtrar por nombre...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            singleLine = true,
                            trailingIcon = {
                                if (searchFilter.isNotBlank()) {
                                    IconButton(onClick = { searchFilter = "" }) {
                                        Icon(Icons.Default.Close, "Clear")
                                    }
                                }
                            }
                        )
                        
                        // Status filter chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = statusFilter == "ready",
                                onClick = { statusFilter = "ready" },
                                label = { Text("✅ ($readyCount)") }
                            )
                            FilterChip(
                                selected = statusFilter == "downloading",
                                onClick = { statusFilter = "downloading" },
                                label = { Text("📥 ($downloadingCount)") }
                            )
                            FilterChip(
                                selected = statusFilter == "all",
                                onClick = { statusFilter = "all" },
                                label = { Text("Todos (${uiState.magnets.size})") }
                            )
                        }
                        
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredMagnets) { magnet ->
                                val context = LocalContext.current
                                DownloadCard(
                                    magnet = magnet,
                                    onDelete = { viewModel.deleteMagnet(magnet.id) },
                                    onCopyLink = { link ->
                                        viewModel.copyLinkToClipboard(context, link)
                                    },
                                    onPlay = { link, title -> viewModel.playLink(link, title) },
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
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }
            
            // Casting Overlay/Toast
            uiState.castingMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inverseSurface
                    )
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
