package com.samcod3.alldebrid.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.samcod3.alldebrid.data.model.SearchResult

@Composable
fun SearchResultItem(
    result: SearchResult,
    onAddToDebrid: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                result.failed -> MaterialTheme.colorScheme.errorContainer
                result.addedToDebrid && result.isDownloading -> MaterialTheme.colorScheme.tertiaryContainer // Orange-ish for downloading
                result.addedToDebrid -> MaterialTheme.colorScheme.primaryContainer // Blue/green for cached
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = MaterialTheme.shapes.medium // Less rounded as requested
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Title
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp), // Slightly bolder/larger
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Meta Info Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Size Badge (Surface background)
                    androidx.compose.material3.Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.extraSmall,
                    ) {
                        Text(
                            text = formatSize(result.size ?: 0),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Seeders Badge
                    result.seeders?.let { seeders ->
                        androidx.compose.material3.Surface(
                            color = if (seeders > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text = "S: $seeders",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (seeders > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    // Tracker
                    result.tracker?.let { tracker ->
                        Text(
                            text = tracker,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            // Action Button
            IconButton(
                onClick = onAddToDebrid,
                enabled = !result.addedToDebrid && !result.failed,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                androidx.compose.material3.FilledIconButton(
                    onClick = onAddToDebrid,
                    enabled = !result.addedToDebrid && !result.failed,
                    colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(
                         containerColor = MaterialTheme.colorScheme.surface // Subtle contrast
                    )
                ) {
                     Icon(
                        imageVector = when {
                            result.failed -> Icons.Default.Close
                            result.addedToDebrid -> Icons.Default.Check
                            else -> Icons.Default.Add
                        },
                        contentDescription = "Add to AllDebrid",
                        tint = when {
                            result.failed -> MaterialTheme.colorScheme.error
                            result.addedToDebrid -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
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
