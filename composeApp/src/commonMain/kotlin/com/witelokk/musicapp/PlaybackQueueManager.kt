package com.witelokk.musicapp

import com.witelokk.musicapp.api.models.Song

class PlaybackQueueManager {
    private var queue = mutableListOf<Song>()
    private var currentIndex: Int = -1

    fun snapshot(): QueueSnapshot {
        val currentSong = queue.getOrNull(currentIndex)
        return QueueSnapshot(
            queue = queue.toList(),
            currentSongIndex = currentIndex,
            currentSong = currentSong,
            previousTrackAvailable = currentIndex > 0,
            nextTrackAvailable = currentIndex in 0 until queue.lastIndex
        )
    }

    fun setQueue(songs: List<Song>, startIndex: Int): QueueSnapshot {
        queue = songs.toMutableList()
        currentIndex = if (songs.isEmpty()) -1 else startIndex.coerceIn(songs.indices)
        return snapshot()
    }

    fun playAt(index: Int): QueueSnapshot {
        if (index in queue.indices) {
            currentIndex = index
        }
        return snapshot()
    }

    fun moveToNext(): QueueSnapshot {
        if (currentIndex in 0 until queue.lastIndex) {
            currentIndex++
        }
        return snapshot()
    }

    fun moveToPrevious(): QueueSnapshot {
        if (currentIndex > 0) {
            currentIndex--
        }
        return snapshot()
    }

    fun onCurrentItemChanged(index: Int): QueueSnapshot {
        if (index in queue.indices) {
            currentIndex = index
        }
        return snapshot()
    }

    fun addNext(song: Song): AddResult {
        if (queue.isEmpty()) {
            queue.add(song)
            currentIndex = 0
            return AddResult(
                snapshot = snapshot(),
                insertedIndex = 0,
                shouldStartNewQueue = true
            )
        }

        val existingIndex = queue.indexOfFirst { it.id == song.id }
        if (existingIndex != -1) {
            removeAtInternal(existingIndex)
        }

        val insertIndex = (currentIndex + 1).coerceAtMost(queue.size)
        queue.add(insertIndex, song)

        if (currentIndex >= insertIndex) {
            currentIndex++
        }

        return AddResult(
            snapshot = snapshot(),
            insertedIndex = insertIndex,
            shouldStartNewQueue = false
        )
    }

    fun removeAt(index: Int): RemoveResult {
        if (index !in queue.indices) {
            return RemoveResult(snapshot(), removedCurrentSong = false, queueBecameEmpty = queue.isEmpty())
        }

        val removedCurrentSong = index == currentIndex
        removeAtInternal(index)

        return RemoveResult(
            snapshot = snapshot(),
            removedCurrentSong = removedCurrentSong,
            queueBecameEmpty = queue.isEmpty()
        )
    }

    fun updateSong(song: Song): QueueSnapshot {
        val index = queue.indexOfFirst { it.id == song.id }
        if (index != -1) {
            queue[index] = song
        }
        return snapshot()
    }

    fun clear(): QueueSnapshot {
        queue.clear()
        currentIndex = -1
        return snapshot()
    }

    private fun removeAtInternal(index: Int) {
        queue.removeAt(index)

        currentIndex = when {
            queue.isEmpty() -> -1
            index < currentIndex -> currentIndex - 1
            index == currentIndex -> currentIndex.coerceAtMost(queue.lastIndex)
            else -> currentIndex
        }
    }
}

data class QueueSnapshot(
    val queue: List<Song>,
    val currentSongIndex: Int,
    val currentSong: Song?,
    val previousTrackAvailable: Boolean,
    val nextTrackAvailable: Boolean,
)

data class AddResult(
    val snapshot: QueueSnapshot,
    val insertedIndex: Int,
    val shouldStartNewQueue: Boolean,
)

data class RemoveResult(
    val snapshot: QueueSnapshot,
    val removedCurrentSong: Boolean,
    val queueBecameEmpty: Boolean,
)