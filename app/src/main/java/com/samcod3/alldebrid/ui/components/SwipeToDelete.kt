package com.samcod3.alldebrid.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class SwipeState { Closed, Open }

/**
 * Container that wraps content with swipe-to-delete functionality.
 * Swipe left to reveal delete button. The content slides to show a red background
 * with a delete confirmation button.
 *
 * @param onDelete Callback when delete is confirmed
 * @param swipePercentage How much of the width can be swiped (0.0 to 1.0), default 0.5 (50%)
 * @param isRevealed Whether THIS item should be revealed (controlled by parent)
 * @param onRevealChange Callback when this container opens or closes
 * @param content The composable content to wrap
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> SwipeToDeleteContainer(
    item: T,
    onDelete: (T) -> Unit,
    swipePercentage: Float = 0.5f,
    isRevealed: Boolean = false,
    onRevealChange: ((Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var containerWidth by remember { mutableStateOf(0f) }

    val openPosition = -containerWidth * swipePercentage

    val anchoredDraggableState = remember {
        AnchoredDraggableState(
            initialValue = SwipeState.Closed,
            positionalThreshold = { distance -> distance * 0.3f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            snapAnimationSpec = androidx.compose.animation.core.tween(200),
            decayAnimationSpec = androidx.compose.animation.core.exponentialDecay()
        )
    }

    // Get offset handling NaN
    val currentOffset = anchoredDraggableState.offset.takeIf { !it.isNaN() } ?: 0f

    // Calculate swipe progress (0 = closed, 1 = open)
    val swipeProgress = if (openPosition != 0f) {
        (currentOffset / openPosition).coerceIn(0f, 1.2f)
    } else 0f

    // Check if swipe is open (more than 50% revealed) - use derivedStateOf for proper observation
    val isOpen by remember {
        derivedStateOf {
            val offset = anchoredDraggableState.offset.takeIf { !it.isNaN() } ?: 0f
            val openPos = -containerWidth * swipePercentage
            val progress = if (openPos != 0f) (offset / openPos).coerceIn(0f, 1.2f) else 0f
            progress > 0.5f
        }
    }

    // Sync with parent: close when parent says we're not revealed
    LaunchedEffect(isRevealed) {
        android.util.Log.e("SwipeToDelete", ">>> isRevealed: $isRevealed, offset: ${anchoredDraggableState.offset}")
        if (!isRevealed) {
            android.util.Log.e("SwipeToDelete", ">>> Closing swipe!")
            // Force drag to closed position (offset = 0)
            anchoredDraggableState.anchoredDrag {
                dragTo(0f)
            }
            android.util.Log.e("SwipeToDelete", ">>> After dragTo, offset: ${anchoredDraggableState.offset}")
        }
    }

    // Notify parent when local state changes
    LaunchedEffect(isOpen) {
        android.util.Log.e("SwipeToDelete", ">>> isOpen changed to: $isOpen, isRevealed: $isRevealed")
        onRevealChange?.invoke(isOpen)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                containerWidth = size.width.toFloat()
                // Update anchors when we have the real size
                val anchors = DraggableAnchors {
                    SwipeState.Closed at 0f
                    SwipeState.Open at -size.width * swipePercentage
                }
                anchoredDraggableState.updateAnchors(anchors)
            }
    ) {
        // Background with delete button - always present, alpha based on progress
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    MaterialTheme.colorScheme.errorContainer.copy(
                        alpha = (swipeProgress * 1.5f).coerceIn(0f, 1f)
                    ),
                    MaterialTheme.shapes.medium
                )
                .padding(end = 8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            // Show button when there's swipe progress
            if (swipeProgress > 0.1f) {
                Button(
                    onClick = { onDelete(item) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(
                            alpha = swipeProgress.coerceIn(0f, 1f)
                        )
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.graphicsLayer {
                        alpha = (swipeProgress * 2f).coerceIn(0f, 1f)
                        scaleX = (0.5f + swipeProgress * 0.5f).coerceIn(0.5f, 1f)
                        scaleY = (0.5f + swipeProgress * 0.5f).coerceIn(0.5f, 1f)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Eliminar")
                }
            }
        }

        // Main content that slides
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(currentOffset.roundToInt(), 0) }
                .anchoredDraggable(
                    state = anchoredDraggableState,
                    orientation = Orientation.Horizontal
                )
        ) {
            content()

            // Overlay that intercepts clicks when open and closes the swipe
            if (isOpen) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // No ripple effect
                        ) {
                            // Close the swipe by settling towards closed position
                            scope.launch {
                                anchoredDraggableState.settle(1000f) // Positive velocity = move right = close
                            }
                        }
                )
            }
        }
    }
}

/**
 * Simplified version without generic type parameter
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    swipePercentage: Float = 0.5f,
    isRevealed: Boolean = false,
    onRevealChange: ((Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    SwipeToDeleteContainer(
        item = Unit,
        onDelete = { onDelete() },
        swipePercentage = swipePercentage,
        isRevealed = isRevealed,
        onRevealChange = onRevealChange,
        content = content
    )
}
