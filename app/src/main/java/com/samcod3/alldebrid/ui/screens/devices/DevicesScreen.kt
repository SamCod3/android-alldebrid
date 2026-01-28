package com.samcod3.alldebrid.ui.screens.devices

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cable
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.samcod3.alldebrid.R
import com.samcod3.alldebrid.data.model.Device
import com.samcod3.alldebrid.ui.components.DeviceItem
import com.samcod3.alldebrid.ui.components.SwipeToDeleteContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    viewModel: DevicesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Rename dialog state
    var deviceToRename by remember { mutableStateOf<Device?>(null) }
    var newName by remember { mutableStateOf("") }

    // Track which device has swipe revealed (only one at a time)
    var revealedDeviceId by remember { mutableStateOf<String?>(null) }
    
    // Rename Dialog
    deviceToRename?.let { device ->
        AlertDialog(
            onDismissRequest = { deviceToRename = null },
            icon = { 
                Icon(
                    Icons.Rounded.Edit, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                ) 
            },
            title = {
                 Text(
                     text = "Rename Device",
                     style = MaterialTheme.typography.headlineSmall
                 ) 
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Device Info Card
                    androidx.compose.material3.Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Device",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${device.type.name} • ${device.address}:${device.port}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Input Field
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Custom Name") },
                        placeholder = { Text("Enter a friendly name") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (device.customName != null) {
                        Text(
                            text = "Current: ${device.customName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameDevice(device, newName.ifBlank { null })
                        deviceToRename = null
                        newName = ""
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (device.customName != null) {
                        TextButton(
                            onClick = {
                                viewModel.renameDevice(device, null)
                                deviceToRename = null
                                newName = ""
                            },
                            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Reset")
                        }
                    }
                    TextButton(onClick = { 
                        deviceToRename = null
                        newName = ""
                    }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.Companion.navigationBars,
        floatingActionButton = {
            var isExpanded by remember { mutableStateOf(false) }
            val isAnyScanning = uiState.isDiscovering || uiState.isHybridScanning || uiState.isManualScanning

            // Animación de rotación para iconos de carga
            val infiniteTransition = rememberInfiniteTransition(label = "scan_rotation")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing)
                ),
                label = "rotation"
            )

            // Rotación del icono principal cuando se expande
            val mainIconRotation by animateFloatAsState(
                targetValue = if (isExpanded) 45f else 0f,
                label = "main_rotation"
            )

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Opciones expandibles
                AnimatedVisibility(
                    visible = isExpanded && !isAnyScanning,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it }
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Opción: IP Scan (lento)
                        ScanOptionFab(
                            icon = Icons.Rounded.Cable,
                            label = "Completo",
                            onClick = {
                                isExpanded = false
                                viewModel.discoverManual()
                            }
                        )

                        // Opción: SSDP (rápido, necesita router compatible)
                        ScanOptionFab(
                            icon = Icons.Rounded.Wifi,
                            label = "SSDP",
                            onClick = {
                                isExpanded = false
                                viewModel.discoverDevices()
                            }
                        )

                        // Opción: Híbrido (recomendado)
                        ScanOptionFab(
                            icon = Icons.Rounded.Radar,
                            label = "Rápido",
                            onClick = {
                                isExpanded = false
                                viewModel.discoverHybrid()
                            }
                        )
                    }
                }

                // Botón principal
                FloatingActionButton(
                    onClick = {
                        if (isAnyScanning) return@FloatingActionButton
                        isExpanded = !isExpanded
                    },
                    containerColor = if (isAnyScanning)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = when {
                            isAnyScanning -> Icons.Rounded.Sync
                            isExpanded -> Icons.Rounded.Close
                            else -> Icons.Rounded.Search
                        },
                        contentDescription = stringResource(R.string.devices_discover),
                        modifier = when {
                            isAnyScanning -> Modifier.rotate(rotation)
                            isExpanded -> Modifier.rotate(mainIconRotation)
                            else -> Modifier
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isDiscovering -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Buscando dispositivos (SSDP)...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                uiState.isHybridScanning -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Buscando Kodi + SSDP...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                uiState.isManualScanning -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Escaneando red (lento)...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                uiState.devices.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.devices_empty),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Pulsa IP para escaneo manual",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.devices,
                                key = { it.id }
                            ) { device ->
                                val isSelected = uiState.selectedDevice?.id == device.id ||
                                        (uiState.selectedDevice?.address == device.address &&
                                         uiState.selectedDevice?.port == device.port)

                                SwipeToDeleteContainer(
                                    item = device,
                                    onDelete = { viewModel.deleteDevice(device) },
                                    isRevealed = revealedDeviceId == device.id,
                                    onRevealChange = { isOpen ->
                                        revealedDeviceId = if (isOpen) device.id else null
                                    }
                                ) {
                                    DeviceItem(
                                        device = device,
                                        isSelected = isSelected,
                                        onClick = { viewModel.selectDevice(device) },
                                        onRename = {
                                            deviceToRename = it
                                            newName = it.customName ?: ""
                                        }
                                    )
                                }
                            }
                        }

                        // Scrim overlay - intercepts taps to close revealed swipe
                        if (revealedDeviceId != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .zIndex(1f)
                                    .pointerInput(revealedDeviceId) {
                                        detectTapGestures { revealedDeviceId = null }
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanOptionFab(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        // Label
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 2.dp
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Mini FAB
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp),
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}


