package com.viperplayer.plugin.sdk.v1

import android.util.Log
import com.viperplayer.plugin.sdk.PluginException

/**
 * Wrapper around IHostCallbackV1 that provides a cleaner API for plugins.
 * This is provided to plugins when they connect to the host.
 */
class HostController internal constructor(
    private val callback: IHostCallbackV1
) {
    companion object {
        private const val TAG = "HostController"
    }
    
    // ==================== Player Control ====================
    
    /**
     * Request the host to play a specific song.
     */
    fun play(mediaId: MediaId) {
        Log.d(TAG, "play() called: mediaId=${mediaId.pluginId}:${mediaId.sourceId}")
        callback.play(mediaId)
    }
    
    /**
     * Pause playback.
     */
    fun pause() {
        Log.d(TAG, "pause() called")
        callback.pause()
    }
    
    /**
     * Resume playback.
     */
    fun resume() {
        Log.d(TAG, "resume() called")
        callback.resume()
    }
    
    /**
     * Stop playback.
     */
    fun stop() {
        Log.d(TAG, "stop() called")
        callback.stop()
    }
    
    /**
     * Skip to the next track.
     */
    fun skipToNext() {
        Log.d(TAG, "skipToNext() called")
        callback.skipToNext()
    }
    
    /**
     * Skip to the previous track.
     */
    fun skipToPrevious() {
        Log.d(TAG, "skipToPrevious() called")
        callback.skipToPrevious()
    }
    
    /**
     * Seek to a position.
     * @param positionMs Position in milliseconds
     */
    fun seekTo(positionMs: Long) {
        Log.d(TAG, "seekTo() called: positionMs=$positionMs")
        callback.seekTo(positionMs)
    }
    
    // ==================== Queue Management ====================
    
    /**
     * Add a song to the end of the queue.
     */
    fun addToQueue(song: Song) {
        Log.d(TAG, "addToQueue() called: song=${song.title}")
        callback.addToQueue(song)
    }
    
    /**
     * Add a song to play next.
     */
    fun playNext(song: Song) {
        Log.d(TAG, "playNext() called: song=${song.title}")
        callback.playNext(song)
    }
    
    /**
     * Clear the queue.
     */
    fun clearQueue() {
        Log.d(TAG, "clearQueue() called")
        callback.clearQueue()
    }
    
    // ==================== State Queries ====================
    
    /**
     * Get current player state.
     */
    val playerState: PlayerState
        get() {
            val state = callback.playerState
            Log.d(TAG, "playerState: state=${state.state}, position=${state.positionMs}ms/${state.durationMs}ms")
            return state
        }
    
    /**
     * Get currently playing song.
     */
    val currentSong: Song?
        get() {
            val song = callback.currentSong
            Log.d(TAG, "currentSong: ${song?.title ?: "null"}")
            return song
        }
    
    /**
     * Get current playback position in milliseconds.
     */
    val playbackPosition: Long
        get() {
            val position = callback.playbackPosition
            Log.d(TAG, "playbackPosition: $position ms")
            return position
        }
    
    // ==================== Notifications ====================
    
    /**
     * Notify host that plugin content has changed.
     */
    fun notifyContentChanged() {
        Log.d(TAG, "notifyContentChanged() called")
        callback.notifyContentChanged()
    }
    
    /**
     * Notify host that metadata for an item has been updated.
     */
    fun notifyMetadataUpdated(mediaId: MediaId) {
        Log.d(TAG, "notifyMetadataUpdated() called: mediaId=${mediaId.pluginId}:${mediaId.sourceId}")
        callback.notifyMetadataUpdated(mediaId)
    }
    
    /**
     * Report an error to the host.
     */
    fun reportError(errorCode: Int, message: String) {
        Log.e(TAG, "reportError() called: code=$errorCode, message=$message")
        callback.reportError(errorCode, message)
    }
    
    /**
     * Report an exception to the host.
     */
    fun reportError(exception: PluginException) {
        Log.e(TAG, "reportError() called: code=${exception.errorCode}, message=${exception.message}")
        callback.reportError(exception.errorCode, exception.message ?: "Unknown error")
    }
}

