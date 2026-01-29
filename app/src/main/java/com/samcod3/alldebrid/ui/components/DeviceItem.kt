package com.samcod3.alldebrid.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samcod3.alldebrid.data.model.Device
import com.samcod3.alldebrid.data.model.DeviceType
import com.samcod3.alldebrid.ui.theme.Alpha
import com.samcod3.alldebrid.ui.theme.Spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceItem(
    device: Device,
    isSelected: Boolean,
    onClick: () -> Unit,
    onRename: (Device) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onRename(device) }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (device.type) {
                    DeviceType.KODI -> Icons.Rounded.Tv
                    DeviceType.DLNA -> Icons.Rounded.Videocam
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(Spacing.lg))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                if (device.customName != null) {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = Alpha.disabled) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Alpha.muted)
                    )
                }
                Text(
                    text = "${device.address}:${device.port}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = Alpha.muted) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = when (device.type) {
                        DeviceType.KODI -> "Kodi (JSON-RPC)"
                        DeviceType.DLNA -> "DLNA / UPnP"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

