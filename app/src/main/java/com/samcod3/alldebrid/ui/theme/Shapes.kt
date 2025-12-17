package com.samcod3.alldebrid.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(12.dp),    // Was 4dp or 8dp usually
    medium = RoundedCornerShape(16.dp),   // Was 12dp
    large = RoundedCornerShape(24.dp),    // Was 0dp or 16dp
    extraLarge = RoundedCornerShape(32.dp) // Was 28dp
)
