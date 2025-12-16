package com.samcod3.alldebrid.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Queue item representing a video to play via DLNA
 */
data class DlnaQueueItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val url: String,
    val title: String,
    val addedAt: Long = System.currentTimeMillis()
)

/**
 * Manages a queue of videos for DLNA playback.
 * Since DLNA doesn't support native queuing, this handles it app-side.
 */
@Singleton
class DlnaQueueManager @Inject constructor() {
    
    private val _queue = MutableStateFlow<List<DlnaQueueItem>>(emptyList())
    val queue: StateFlow<List<DlnaQueueItem>> = _queue.asStateFlow()
    
    private val _currentlyPlaying = MutableStateFlow<DlnaQueueItem?>(null)
    val currentlyPlaying: StateFlow<DlnaQueueItem?> = _currentlyPlaying.asStateFlow()
    
    /**
     * Add a video to the queue
     */
    fun addToQueue(url: String, title: String) {
        val item = DlnaQueueItem(url = url, title = title)
        _queue.value = _queue.value + item
    }
    
    /**
     * Remove an item from the queue
     */
    fun removeFromQueue(itemId: String) {
        _queue.value = _queue.value.filter { it.id != itemId }
    }
    
    /**
     * Clear the entire queue
     */
    fun clearQueue() {
        _queue.value = emptyList()
    }
    
    /**
     * Get the next item to play and remove it from queue
     */
    fun popNext(): DlnaQueueItem? {
        val items = _queue.value
        if (items.isEmpty()) return null
        
        val next = items.first()
        _queue.value = items.drop(1)
        _currentlyPlaying.value = next
        return next
    }
    
    /**
     * Peek at the next item without removing it
     */
    fun peekNext(): DlnaQueueItem? {
        return _queue.value.firstOrNull()
    }
    
    /**
     * Set the currently playing item (for display purposes)
     */
    fun setCurrentlyPlaying(item: DlnaQueueItem?) {
        _currentlyPlaying.value = item
    }
    
    /**
     * Move an item up in the queue
     */
    fun moveUp(itemId: String) {
        val items = _queue.value.toMutableList()
        val index = items.indexOfFirst { it.id == itemId }
        if (index > 0) {
            val item = items.removeAt(index)
            items.add(index - 1, item)
            _queue.value = items
        }
    }
    
    /**
     * Move an item down in the queue
     */
    fun moveDown(itemId: String) {
        val items = _queue.value.toMutableList()
        val index = items.indexOfFirst { it.id == itemId }
        if (index >= 0 && index < items.size - 1) {
            val item = items.removeAt(index)
            items.add(index + 1, item)
            _queue.value = items
        }
    }
    
    /**
     * Get queue size
     */
    fun queueSize(): Int = _queue.value.size
    
    /**
     * Check if queue is empty
     */
    fun isEmpty(): Boolean = _queue.value.isEmpty()
}
