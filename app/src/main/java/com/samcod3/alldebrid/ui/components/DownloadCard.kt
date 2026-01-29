package com.samcod3.alldebrid.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.samcod3.alldebrid.data.model.FlatFile
import com.samcod3.alldebrid.data.model.Magnet
import com.samcod3.alldebrid.ui.theme.Alpha
import com.samcod3.alldebrid.ui.theme.Spacing
import com.samcod3.alldebrid.ui.theme.StatusDownloading
import com.samcod3.alldebrid.ui.util.formatSize
import com.samcod3.alldebrid.ui.theme.StatusError
import com.samcod3.alldebrid.ui.theme.StatusQueued
import com.samcod3.alldebrid.ui.theme.StatusReady
import kotlinx.coroutines.launch

// Media file extensions
private val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "mpg", "mpeg", "3gp")
private val AUDIO_EXTENSIONS = setOf("mp3", "flac", "wav", "aac", "ogg", "m4a", "wma", "ape", "opus")

private fun String.isMediaFile(): Boolean {
    val extension = this.substringAfterLast('.', "").lowercase()
    return extension in VIDEO_EXTENSIONS || extension in AUDIO_EXTENSIONS
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DownloadCard(
    magnet: Magnet,
    onCopyLink: (String) -> Unit,
    onPlay: (link: String, title: String) -> Unit,
    onFetchFiles: suspend (Long) -> Result<List<FlatFile>>,
    refreshCallback: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var showAllFiles by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    
    // File loading state
    var files by remember { mutableStateOf<List<FlatFile>>(emptyList()) }
    var isLoadingFiles by remember { mutableStateOf(false) }
    var filesError by remember { mutableStateOf<String?>(null) }
    
    val statusColor = when (magnet.status) {
        "Ready" -> StatusReady
        "Downloading" -> StatusDownloading
        "Queued" -> StatusQueued
        else -> StatusError
    }
    
    // Filter files into media and other
    val mediaFiles = files.filter { it.filename.isMediaFile() }
    val otherFiles = files.filter { !it.filename.isMediaFile() }
    val hasOtherFiles = otherFiles.isNotEmpty()

    // Fetch files when bottom sheet opens
    LaunchedEffect(showBottomSheet) {
        if (showBottomSheet && magnet.status == "Ready" && files.isEmpty()) {
            isLoadingFiles = true
            filesError = null
            onFetchFiles(magnet.id)
                .onSuccess { fetchedFiles ->
                    files = fetchedFiles
                    isLoadingFiles = false
                }
                .onFailure { error ->
                    filesError = error.message
                    isLoadingFiles = false
                }
        }
    }

    // BottomSheet for file list
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .padding(bottom = Spacing.xxxl)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Text(
                    text = magnet.filename,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                // Progress Header for Downloading items
                if (magnet.status != "Ready") {
                    // Auto-refresh every 10 seconds while sheet is open
                    LaunchedEffect(Unit) {
                        while(true) {
                            kotlinx.coroutines.delay(10000)
                            refreshCallback?.invoke()
                        }
                    }

                    // Progress Header for Downloading items
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.sm)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                             // Status with Icon
                             Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                 CircularProgressIndicator(
                                     modifier = Modifier.size(16.dp),
                                     strokeWidth = 2.dp,
                                     color = statusColor
                                 )
                                 Text(
                                    text = magnet.status,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = statusColor
                                )
                             }
                            
                            // Speed
                            androidx.compose.material3.Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PlayArrow, // Rotation needed for download arrow, usually ArrowDownward
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp).rotate(90f),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "${formatSize(magnet.downloadSpeed)}/s",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(Spacing.lg))
                        
                        // Progress
                        val progress = if (magnet.size > 0) magnet.downloaded.toFloat() / magnet.size.toFloat() else 0f
                        val progressPercent = (progress * 100).toInt()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Progress",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$progressPercent%",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(Spacing.sm))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(Spacing.sm),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            color = statusColor,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${formatSize(magnet.downloaded)} / ${formatSize(magnet.size)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                             Text(
                                text = "${magnet.seeders} seeds",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    Spacer(modifier = Modifier.height(Spacing.lg))
                } else {
                    Text(
                        text = "${formatSize(magnet.size)} • ${files.size} files",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(Spacing.lg))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Spacing.lg))
                
                // Loading state
                if (isLoadingFiles) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.xxxl),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            Text(
                                text = "Loading files...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (filesError != null) {
                    Text(
                        text = "Error: $filesError",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(Spacing.lg)
                    )
                } else {
                    // Media files
                    if (mediaFiles.isNotEmpty()) {
                        Text(
                            text = "Media Files (${mediaFiles.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            mediaFiles.forEach { file ->
                                FileLinkItem(
                                    file = file,
                                    onClick = { 
                                        onPlay(file.link, file.filename)
                                        showBottomSheet = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Other files with toggle
                    if (hasOtherFiles) {
                        Spacer(modifier = Modifier.height(Spacing.lg))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Other Files (${otherFiles.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { showAllFiles = !showAllFiles }) {
                                Icon(
                                    imageVector = if (showAllFiles) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(Spacing.xs))
                                Text(if (showAllFiles) "Hide" else "Show")
                            }
                        }
                        
                        if (showAllFiles) {
                            Text(
                                text = "(long-press to copy link)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Alpha.muted),
                                modifier = Modifier.padding(bottom = Spacing.sm)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                otherFiles.forEach { file ->
                                    OtherFileItem(
                                        file = file,
                                        onLongPress = { 
                                            onCopyLink(file.link)
                                            // Don't close sheet - user can close manually
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Empty state
                    if (files.isEmpty() && !isLoadingFiles && magnet.status == "Ready") {
                        Text(
                            text = "No files found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(Spacing.lg)
                        )
                    }
                }
            }
        }
    }

    // Compact Card
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = magnet.status == "Ready" || magnet.status != "Ready") { 
                showBottomSheet = true 
            },
        shape = MaterialTheme.shapes.medium, // Reduced radius
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg) // Adjusted padding
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    
                    // Status Badge Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        // Status Badge
                        androidx.compose.material3.Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = statusColor.copy(alpha = Alpha.subtle)
                        ) {
                            Text(
                                text = magnet.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Size
                        Text(
                            text = formatSize(magnet.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Media count (show if files loaded)
                        if (files.isNotEmpty()) {
                            val mediaCount = files.count { it.filename.isMediaFile() }
                            if (mediaCount > 0) {
                                androidx.compose.material3.Surface(
                                    shape = MaterialTheme.shapes.extraSmall,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "$mediaCount media",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Progress bar for downloading
            if (magnet.status == "Downloading" && magnet.downloaded > 0) {
                Spacer(modifier = Modifier.height(Spacing.md))
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    LinearProgressIndicator(
                        progress = { (magnet.downloaded.toFloat() / magnet.size.toFloat()) },
                        modifier = Modifier.fillMaxWidth().height(Spacing.xs),
                        color = StatusDownloading,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${((magnet.downloaded.toFloat() / magnet.size.toFloat()) * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${formatSize(magnet.downloadSpeed)}/s",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FileLinkItem(
    file: FlatFile,
    onClick: () -> Unit
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Surface(
                 shape = androidx.compose.foundation.shape.CircleShape,
                 color = MaterialTheme.colorScheme.primaryContainer,
                 modifier = Modifier.size(32.dp)
            ) {
                 Box(contentAlignment = Alignment.Center) {
                     Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                 }
            }
            Spacer(modifier = Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.filename,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatSize(file.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OtherFileItem(
    file: FlatFile,
    onLongPress: () -> Unit
) {
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface.copy(alpha = Alpha.disabled)
    ) {
         Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { },
                    onLongClick = onLongPress
                )
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.InsertDriveFile,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                // Fallback: extract filename from link URL if filename is empty or blank
                val displayName = file.filename.trim().ifEmpty { 
                    try {
                        java.net.URLDecoder.decode(
                            file.link.substringAfterLast('/').substringBefore('?'), 
                            "UTF-8"
                        ).ifEmpty { "Unknown file" }
                    } catch (e: Exception) {
                        file.link.substringAfterLast('/').substringBefore('?').ifEmpty { "Unknown file" }
                    }
                }
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodySmall, // Smaller text
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alpha.muted) // Muted
                )
                Text(
                    text = formatSize(file.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Alpha.disabled)
                )
            }
        }
    }
}

