package com.samcod3.alldebrid.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DownloadCard(
    magnet: Magnet,
    onCopyLink: (String) -> Unit,
    onPlay: (link: String, title: String) -> Unit,
    onFetchFiles: suspend (Long) -> Result<List<FlatFile>>,
    onDelete: ((Long) -> Unit)? = null,
    onShareLink: ((link: String, filename: String) -> Unit)? = null,
    showDeleteButton: Boolean = false,
    refreshCallback: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isLoadingForShare by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showAllFiles by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
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

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Eliminar magnet") },
            text = {
                Column {
                    Text(
                        text = magnet.filename,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text("Esta acción eliminará el magnet de AllDebrid permanentemente.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        showBottomSheet = false
                        onDelete?.invoke(magnet.id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // BottomSheet for file list
    if (showBottomSheet) {
        DownloadBottomSheet(
            magnet = magnet,
            files = files,
            isLoadingFiles = isLoadingFiles,
            filesError = filesError,
            showAllFiles = showAllFiles,
            onToggleShowAllFiles = { showAllFiles = !showAllFiles },
            statusColor = statusColor,
            sheetState = sheetState,
            onDismiss = { showBottomSheet = false },
            onPlay = onPlay,
            onShareLink = onShareLink,
            onCopyLink = onCopyLink,
            onDeleteRequest = if (onDelete != null) {{ showDeleteConfirmation = true }} else null,
            refreshCallback = refreshCallback
        )
    }

    // Long-press handler: if 1 media file -> share directly, else open BottomSheet
    val handleLongPress: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (magnet.status == "Ready" && onShareLink != null) {
            if (files.isNotEmpty()) {
                val mediaFilesLoaded = files.filter { it.filename.isMediaFile() }
                if (mediaFilesLoaded.size == 1) {
                    // Share directly
                    onShareLink(mediaFilesLoaded[0].link, mediaFilesLoaded[0].filename)
                } else {
                    // Open BottomSheet to choose
                    showBottomSheet = true
                }
            } else {
                // Need to load files first
                isLoadingForShare = true
                scope.launch {
                    onFetchFiles(magnet.id)
                        .onSuccess { fetchedFiles ->
                            files = fetchedFiles
                            isLoadingForShare = false
                            val mediaFilesLoaded = fetchedFiles.filter { it.filename.isMediaFile() }
                            if (mediaFilesLoaded.size == 1) {
                                onShareLink(mediaFilesLoaded[0].link, mediaFilesLoaded[0].filename)
                            } else {
                                showBottomSheet = true
                            }
                        }
                        .onFailure {
                            isLoadingForShare = false
                            showBottomSheet = true
                        }
                }
            }
        }
    }

    // Card
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { showBottomSheet = true },
                onLongClick = if (magnet.status == "Ready" && onShareLink != null) handleLongPress else null
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
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

                        // Loading indicator for share
                        if (isLoadingForShare) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                        }

                        // Media count (show if files loaded)
                        if (files.isNotEmpty() && !isLoadingForShare) {
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

                // Delete button for downloading items
                if (showDeleteButton && onDelete != null) {
                    FilledIconButton(
                        onClick = { onDelete(magnet.id) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Cancelar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Progress bar for downloading
        if (magnet.status == "Downloading" && magnet.downloaded > 0) {
            Column(
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
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


