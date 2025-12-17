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
        return next
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

