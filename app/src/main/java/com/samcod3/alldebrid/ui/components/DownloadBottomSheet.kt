package com.samcod3.alldebrid.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.samcod3.alldebrid.data.model.FlatFile
import com.samcod3.alldebrid.data.model.Magnet
import com.samcod3.alldebrid.ui.theme.Alpha
import com.samcod3.alldebrid.ui.theme.Spacing
import com.samcod3.alldebrid.ui.util.formatSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DownloadBottomSheet(
    magnet: Magnet,
    files: List<FlatFile>,
    isLoadingFiles: Boolean,
    filesError: String?,
    showAllFiles: Boolean,
    onToggleShowAllFiles: () -> Unit,
    statusColor: Color,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onPlay: (link: String, title: String) -> Unit,
    onShareLink: ((link: String, filename: String) -> Unit)?,
    onCopyLink: (String) -> Unit,
    onDeleteRequest: (() -> Unit)?,
    refreshCallback: (() -> Unit)?
) {
    val mediaFiles = files.filter { it.filename.isMediaFile() }
    val otherFiles = files.filter { !it.filename.isMediaFile() }
    val hasOtherFiles = otherFiles.isNotEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color.White.copy(alpha = 0.8f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(Spacing.xl))

                // Header Card
                HeaderCard(magnet, files, statusColor)

                Spacer(modifier = Modifier.height(Spacing.xl))

                // Progress section for Downloading items
                if (magnet.status != "Ready") {
                    LaunchedEffect(Unit) {
                        while (true) {
                            kotlinx.coroutines.delay(10000)
                            refreshCallback?.invoke()
                        }
                    }

                    ProgressSection(magnet, statusColor)

                    Spacer(modifier = Modifier.height(Spacing.xl))
                }

                // Files section
                FilesSection(
                    isLoadingFiles = isLoadingFiles,
                    filesError = filesError,
                    mediaFiles = mediaFiles,
                    otherFiles = otherFiles,
                    hasOtherFiles = hasOtherFiles,
                    showAllFiles = showAllFiles,
                    onToggleShowAllFiles = onToggleShowAllFiles,
                    magnetStatus = magnet.status,
                    filesEmpty = files.isEmpty(),
                    onPlay = { file -> onPlay(file.link, file.filename); onDismiss() },
                    onShareLink = onShareLink,
                    onCopyLink = onCopyLink
                )

                Spacer(modifier = Modifier.height(Spacing.lg))
            }

            // Delete zone - fixed at bottom
            if (onDeleteRequest != null) {
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
                            onClick = onDeleteRequest,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                brush = SolidColor(MaterialTheme.colorScheme.error)
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

@Composable
private fun HeaderCard(magnet: Magnet, files: List<FlatFile>, statusColor: Color) {
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
}

@Composable
private fun ProgressSection(magnet: Magnet, statusColor: Color) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(Spacing.xl)) {
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
                strokeCap = StrokeCap.Round
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
}

@Composable
private fun FilesSection(
    isLoadingFiles: Boolean,
    filesError: String?,
    mediaFiles: List<FlatFile>,
    otherFiles: List<FlatFile>,
    hasOtherFiles: Boolean,
    showAllFiles: Boolean,
    onToggleShowAllFiles: () -> Unit,
    magnetStatus: String,
    filesEmpty: Boolean,
    onPlay: (FlatFile) -> Unit,
    onShareLink: ((link: String, filename: String) -> Unit)?,
    onCopyLink: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = Spacing.lg)) {
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
                            onPlay = { onPlay(file) },
                            onShare = { link ->
                                onShareLink?.invoke(link, file.filename)
                            }
                        )
                    }
                }
            }

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
                    TextButton(onClick = onToggleShowAllFiles) {
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
                                onLongPress = { onCopyLink(file.link) }
                            )
                        }
                    }
                }
            }

            if (filesEmpty && !isLoadingFiles && magnetStatus == "Ready") {
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
}
