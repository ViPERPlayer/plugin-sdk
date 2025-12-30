package com.viperplayer.plugin.sdk.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Current state of the host player.
 * 
 * @property state Current playback state
 * @property currentSong Currently playing song (null if nothing playing)
 * @property positionMs Current playback position in milliseconds
 * @property durationMs Total duration of current track
 * @property shuffleEnabled Whether shuffle is enabled
 * @property repeatMode Current repeat mode
 * @property volume Current volume level (0.0 to 1.0)
 * @property queueSize Number of songs in the queue
 * @property queuePosition Current position in the queue (0-indexed)
 */
@Parcelize
data class PlayerState(
    val state: PlaybackState = PlaybackState.IDLE,
    val currentSong: Song? = null,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val volume: Float = 1.0f,
    val queueSize: Int = 0,
    val queuePosition: Int = 0
) : Parcelable {
    val isPlaying: Boolean
        get() = state == PlaybackState.PLAYING
    
    val isPaused: Boolean
        get() = state == PlaybackState.PAUSED
    
    val hasContent: Boolean
        get() = currentSong != null
    
    val progress: Float
        get() = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
    
    companion object {
        val IDLE = PlayerState()
    }
}

/**
 * Playback states.
 */
enum class PlaybackState {
    /** Player is idle, nothing loaded */
    IDLE,
    /** Player is loading/buffering */
    BUFFERING,
    /** Player is playing */
    PLAYING,
    /** Player is paused */
    PAUSED,
    /** Player stopped */
    STOPPED,
    /** An error occurred */
    ERROR
}

/**
 * Repeat modes.
 */
enum class RepeatMode {
    /** No repeat */
    OFF,
    /** Repeat current track */
    ONE,
    /** Repeat entire queue */
    ALL
}
