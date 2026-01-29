package com.samcod3.alldebrid.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Standardized spacing scale for consistent UI layout.
 * Based on 4dp base unit following Material 3 guidelines.
 */
object Spacing {
    /** 4.dp - Minimal separators, tight elements */
    val xs = 4.dp

    /** 8.dp - Compact elements, icon gaps */
    val sm = 8.dp

    /** 12.dp - Between list items, medium gaps */
    val md = 12.dp

    /** 16.dp - Standard padding, card content */
    val lg = 16.dp

    /** 20.dp - Card outer padding, larger gaps */
    val xl = 20.dp

    /** 24.dp - Section separators */
    val xxl = 24.dp

    /** 32.dp - Large section separators */
    val xxxl = 32.dp
}

/**
 * Standardized alpha/opacity values for consistent transparency.
 * Use with Color.copy(alpha = Alpha.xxx)
 */
object Alpha {
    /** 0.7f - Muted text, secondary emphasis */
    const val muted = 0.7f

    /** 0.5f - Disabled elements, tertiary emphasis */
    const val disabled = 0.5f

    /** 0.1f - Subtle backgrounds, badges */
    const val subtle = 0.1f
}
