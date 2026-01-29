package com.samcod3.alldebrid.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f),
            dragHandle = {
                // Línea superior blanca
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
            ) {
                // Contenido scrollable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(Spacing.xl))

                // Header Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.xl),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {
                        // Folder icon
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.FolderOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = magnet.filename,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(Spacing.md))

                            // Info chips
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Status chip
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = statusColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = magnet.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                                    )
                                }

                                // Size chip
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = formatSize(magnet.size),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                                    )
                                }

                                // Files count chip
                                if (files.isNotEmpty()) {
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "${files.size} files",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                // Progress section for Downloading items
                if (magnet.status != "Ready") {
                    // Auto-refresh every 10 seconds while sheet is open
                    LaunchedEffect(Unit) {
                        while (true) {
                            kotlinx.coroutines.delay(10000)
                            refreshCallback?.invoke()
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.xl)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = statusColor
                                    )
                                    Text(
                                        text = magnet.status,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = statusColor
                                    )
                                }

                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "${formatSize(magnet.downloadSpeed)}/s",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(Spacing.lg))

                            val progress = if (magnet.size > 0) magnet.downloaded.toFloat() / magnet.size.toFloat() else 0f
                            val progressPercent = (progress * 100).toInt()

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Spacing.sm),
                                trackColor = MaterialTheme.colorScheme.surface,
                                color = statusColor,
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )

                            Spacer(modifier = Modifier.height(Spacing.md))

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
                                    text = "$progressPercent% • ${magnet.seeders} seeds",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.xl))
                }

                // Files section
                Column(
                    modifier = Modifier.padding(horizontal = Spacing.lg)
                ) {
                    // Loading state
                    if (isLoadingFiles) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.xxxl),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(Spacing.md))
                                Text(
                                    text = "Cargando archivos...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else if (filesError != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "Error: $filesError",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(Spacing.lg)
                            )
                        }
                    } else {
                        // Media files
                        if (mediaFiles.isNotEmpty()) {
                            Text(
                                text = "MEDIA FILES (${mediaFiles.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = Spacing.md)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                mediaFiles.forEach { file ->
                                    FileLinkItem(
                                        file = file,
                                        onPlay = {
                                            onPlay(file.link, file.filename)
                                            showBottomSheet = false
                                        },
                                        onShare = { link ->
                                            onShareLink?.invoke(link, file.filename)
                                        }
                                    )
                                }
                            }
                        }

                        // Other files with toggle
                        if (hasOtherFiles) {
                            Spacer(modifier = Modifier.height(Spacing.xl))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "OTHER FILES (${otherFiles.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(onClick = { showAllFiles = !showAllFiles }) {
                                    Icon(
                                        imageVector = if (showAllFiles) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.xs))
                                    Text(if (showAllFiles) "Ocultar" else "Ver")
                                }
                            }

                            if (showAllFiles) {
                                Text(
                                    text = "(mantén pulsado para copiar enlace)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Alpha.muted),
                                    modifier = Modifier.padding(bottom = Spacing.md)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                    otherFiles.forEach { file ->
                                        OtherFileItem(
                                            file = file,
                                            onLongPress = {
                                                onCopyLink(file.link)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Empty state
                        if (files.isEmpty() && !isLoadingFiles && magnet.status == "Ready") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Spacing.xxxl),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No se encontraron archivos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.lg))
                } // Fin del Column scrollable

                // Delete zone - fija abajo
                if (onDelete != null) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.lg),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            OutlinedButton(
                                onClick = { showDeleteConfirmation = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)
                                )
                            ) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(Spacing.sm))
                                Text(
                                    "Eliminar magnet",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            Spacer(modifier = Modifier.height(Spacing.xs))

                            Text(
                                text = "Esta acción es permanente",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error.copy(alpha = Alpha.muted)
                            )
                        }
                    }
                }
            }
        }
    }

    // Long-press handler: if 1 media file -> share directly, else open BottomSheet
    val handleLongPress: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (magnet.status == "Ready" && onShareLink != null) {
            // If files already loaded, check count
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

@Composable
private fun FileLinkItem(
    file: FlatFile,
    onPlay: () -> Unit,
    onShare: (String) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // Play button - only this is clickable for play
            Surface(
                onClick = onPlay,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Reproducir",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // File info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.filename,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = formatSize(file.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Share button
            Surface(
                onClick = { onShare(file.link) },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Compartir",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
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

