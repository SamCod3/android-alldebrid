package com.samcod3.alldebrid.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.samcod3.alldebrid.data.model.SearchResult
import com.samcod3.alldebrid.ui.theme.Alpha
import com.samcod3.alldebrid.ui.theme.Spacing
import com.samcod3.alldebrid.ui.util.formatSize

@Composable
fun SearchResultItem(
    result: SearchResult,
    onAddToDebrid: () -> Unit,
    isAdding: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cardColor = when {
        result.failed -> MaterialTheme.colorScheme.errorContainer
        result.addedToDebrid && result.isDownloading -> MaterialTheme.colorScheme.tertiaryContainer
        result.addedToDebrid -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val actionIcon = when {
        result.failed -> Icons.Rounded.Close
        result.addedToDebrid -> Icons.Rounded.Check
        else -> Icons.Rounded.Add
    }

    val iconTint = when {
        result.failed -> MaterialTheme.colorScheme.error
        result.addedToDebrid -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = Alpha.disabled),
                        shape = MaterialTheme.shapes.extraSmall,
                    ) {
                        Text(
                            text = formatSize(result.size ?: 0),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                        )
                    }

                    result.seeders?.let { seeders ->
                        Surface(
                            color = if (seeders > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text = "S: $seeders",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (seeders > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                            )
                        }
                    }

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

            // Fixed: Removed redundant IconButton wrapper
            FilledIconButton(
                onClick = onAddToDebrid,
                enabled = !result.addedToDebrid && !result.failed && !isAdding,
                modifier = Modifier.padding(start = Spacing.sm),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = "Add to AllDebrid",
                    tint = iconTint
                )
            }
        }
    }
}
