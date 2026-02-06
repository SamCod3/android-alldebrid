package com.samcod3.alldebrid.ui.screens.downloads

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samcod3.alldebrid.data.model.Device
import com.samcod3.alldebrid.data.model.DeviceType
import com.samcod3.alldebrid.data.repository.DlnaQueueItem
import com.samcod3.alldebrid.ui.theme.Spacing

@Composable
internal fun IpAuthorizationDialog(
    onDismiss: () -> Unit,
    onAuthorize: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Warning, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("IP Authorization Required") },
        text = {
            Text("AllDebrid has detected a new IP address (VPN?). You need to authorize this IP to continue using the service.")
        },
        confirmButton = {
            Button(onClick = onAuthorize) {
                Text("Authorize IP")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
internal fun NoDeviceSelectedDialog(
    hasDevices: Boolean,
    onDismiss: () -> Unit,
    onNavigateToDevices: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Cast, null) },
        title = { Text("No Device Selected") },
        text = {
            Column {
                Text("Please select a device to cast media to.")
                if (!hasDevices) {
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        "No devices found. Go to Devices tab to discover devices on your network.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onNavigateToDevices) {
                Text(if (!hasDevices) "Discover Devices" else "Select Device")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
internal fun KodiQueueDialog(
    onDismiss: () -> Unit,
    onPlayNow: () -> Unit,
    onAddToQueue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.PlayArrow, null) },
        title = { Text("Kodi is Playing") },
        text = { Text("Kodi is currently playing content. Do you want to play this now or add it to the queue?") },
        confirmButton = {
            Button(onClick = onPlayNow) {
                Text("Play Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onAddToQueue) {
                Text("Add to Queue")
            }
        }
    )
}

@Composable
internal fun DlnaQueueDialog(
    queueSize: Int,
    onDismiss: () -> Unit,
    onPlayNow: () -> Unit,
    onAddToQueue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Cast, null) },
        title = { Text("Queue has items") },
        text = {
            Column {
                Text("You have $queueSize video(s) in queue.")
                Spacer(Modifier.height(Spacing.sm))
                Text("Do you want to play this now or add it to the queue?")
            }
        },
        confirmButton = {
            Button(onClick = onPlayNow) {
                Text("Play Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onAddToQueue) {
                Text("Add to Queue")
            }
        }
    )
}

@Composable
internal fun DeviceSelectorDialog(
    devices: List<Device>,
    selectedDevice: Device?,
    dlnaQueue: List<DlnaQueueItem>,
    onDismiss: () -> Unit,
    onSelectDevice: (Device) -> Unit,
    onNavigateToDevices: () -> Unit,
    onPlayNext: () -> Unit,
    onClearQueue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Cast, null) },
        title = { Text("Select Device") },
        text = {
            Column {
                if (devices.isEmpty()) {
                    Text("No devices found. Tap 'Discover' to scan.")
                } else {
                    devices.forEach { device ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectDevice(device) }
                                .padding(vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDevice?.id == device.id,
                                onClick = { onSelectDevice(device) }
                            )
                            Spacer(Modifier.width(Spacing.sm))
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

                    if (selectedDevice?.type == DeviceType.DLNA && dlnaQueue.isNotEmpty()) {
                        Spacer(Modifier.height(Spacing.md))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Queue: ${dlnaQueue.size} video(s)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row {
                                TextButton(onClick = onPlayNext) {
                                    Text("Play Next")
                                }
                                TextButton(onClick = onClearQueue) {
                                    Text("Clear", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onNavigateToDevices) {
                Icon(Icons.Rounded.Search, null, Modifier.size(18.dp))
                Spacer(Modifier.width(Spacing.xs))
                Text("Discover")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
