package com.samcod3.alldebrid.ui.screens.downloads

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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
    if (uiState.error?.contains("No device selected") == true) {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessage() },
            title = { Text("No Device Selected") },
            text = { Text("Please select a device to cast media to.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.clearMessage()
                    onNavigateToDevices()
                }) {
                    Text("Select Device")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearMessage() }) {
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
                                    Text(device.name, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${device.type.name} • ${device.address}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
        topBar = {
            TopAppBar(
                modifier = Modifier.height(48.dp),
                title = { 
                    Text(
                        stringResource(R.string.nav_downloads),
                        style = MaterialTheme.typography.titleMedium
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // Cast icon with device selector
                    IconButton(
                        onClick = { 
                            if (uiState.discoveredDevices.isEmpty()) {
                                // No devices - navigate to Devices tab and scan
                                onNavigateToDevices()
                            } else {
                                showDeviceSelector = true
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.selectedDevice != null) 
                                Icons.Default.CastConnected else Icons.Default.Cast,
                            contentDescription = "Cast",
                            tint = if (uiState.selectedDevice != null)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.magnets) { magnet ->
                            DownloadCard(
                                magnet = magnet,
                                onDelete = { viewModel.deleteMagnet(magnet.id) },
                                onUnlock = { link -> viewModel.unlockAndCopy(link) },
                                onPlay = { link -> viewModel.playLink(link) }
                            )
                        }
                    }
                }
            }
            
            // Casting Overlay/Toast
            uiState.castingMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
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
