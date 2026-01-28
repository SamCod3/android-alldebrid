package com.samcod3.alldebrid.ui.util

/**
 * Formats file size in bytes to human-readable string.
 * Uses SI units (1000-based) for consistency with AllDebrid API.
 */
fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000 -> String.format("%.2f GB", bytes / 1_000_000_000.0)
        bytes >= 1_000_000 -> String.format("%.2f MB", bytes / 1_000_000.0)
        bytes >= 1_000 -> String.format("%.2f KB", bytes / 1_000.0)
        else -> "$bytes B"
    }
}
