package com.samcod3.alldebrid.ui.components

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.samcod3.alldebrid.data.model.Magnet
import com.samcod3.alldebrid.data.model.MagnetLink
import com.samcod3.alldebrid.ui.theme.StatusDownloading
import com.samcod3.alldebrid.ui.theme.StatusError
import com.samcod3.alldebrid.ui.theme.StatusQueued
import com.samcod3.alldebrid.ui.theme.StatusReady

// Media file extensions
private val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "mpg", "mpeg", "3gp")
private val AUDIO_EXTENSIONS = setOf("mp3", "flac", "wav", "aac", "ogg", "m4a", "wma", "ape", "opus")

private fun String.isMediaFile(): Boolean {
    val extension = this.substringAfterLast('.', "").lowercase()
    return extension in VIDEO_EXTENSIONS || extension in AUDIO_EXTENSIONS
}

@Composable
fun DownloadCard(
    magnet: Magnet,
    onDelete: () -> Unit,
    onUnlock: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showAllFiles by remember { mutableStateOf(false) }
    
    val statusColor = when (magnet.status) {
        "Ready" -> StatusReady
        "Downloading" -> StatusDownloading
        "Queued" -> StatusQueued
        else -> StatusError
    }
    
    // Filter links
    val mediaLinks = magnet.links.filter { it.filename.isMediaFile() }
    val otherLinks = magnet.links.filter { !it.filename.isMediaFile() }
    val hasOtherFiles = otherLinks.isNotEmpty()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = magnet.filename,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = magnet.status,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = formatSize(magnet.size),
                            style = MaterialTheme.typography.labelMedium
                        )
                        if (mediaLinks.isNotEmpty()) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "${mediaLinks.size} media",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Row {
                    if (magnet.links.isNotEmpty()) {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Progress bar for downloading
            if (magnet.status == "Downloading" && magnet.downloaded > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (magnet.downloaded.toFloat() / magnet.size.toFloat()) },
                    modifier = Modifier.fillMaxWidth(),
                    color = StatusDownloading
                )
                Text(
                    text = "${((magnet.downloaded.toFloat() / magnet.size.toFloat()) * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
            
            // Expanded links
            if (expanded && magnet.links.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // Media files (always shown when expanded)
                if (mediaLinks.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        mediaLinks.forEach { link ->
                            LinkItem(
                                link = link,
                                onUnlock = { onUnlock(link.link) },
                                isMedia = true
                            )
                        }
                    }
                }
                
                // Other files with toggle
                if (hasOtherFiles) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${otherLinks.size} other file${if (otherLinks.size > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { showAllFiles = !showAllFiles }) {
                            Icon(
                                imageVector = if (showAllFiles) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showAllFiles) "Hide" else "Show")
                        }
                    }
                    
                    if (showAllFiles) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            otherLinks.forEach { link ->
                                LinkItem(
                                    link = link,
                                    onUnlock = { onUnlock(link.link) },
                                    isMedia = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkItem(
    link: MagnetLink,
    onUnlock: () -> Unit,
    isMedia: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (isMedia) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = link.filename,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (isMedia) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = onUnlock) {
            Text("Unlock")
        }
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000 -> String.format("%.2f GB", bytes / 1_000_000_000.0)
        bytes >= 1_000_000 -> String.format("%.2f MB", bytes / 1_000_000.0)
        bytes >= 1_000 -> String.format("%.2f KB", bytes / 1_000.0)
        else -> "$bytes B"
    }
}
