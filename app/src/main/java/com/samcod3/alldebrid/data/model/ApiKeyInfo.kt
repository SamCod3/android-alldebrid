package com.samcod3.alldebrid.data.model

/**
 * Represents an AllDebrid API key from the dashboard
 */
data class ApiKeyInfo(
    val name: String,
    val key: String,
    val isSelected: Boolean = false
)
