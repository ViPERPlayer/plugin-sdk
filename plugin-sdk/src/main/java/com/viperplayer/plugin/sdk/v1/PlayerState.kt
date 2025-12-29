package com.viperplayer.plugin.sdk.v1

import android.os.Parcel
import android.os.Parcelable

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
    
    constructor(parcel: Parcel) : this(
        state = PlaybackState.entries[parcel.readInt()],
        currentSong = parcel.readParcelable(Song::class.java.classLoader),
        positionMs = parcel.readLong(),
        durationMs = parcel.readLong(),
        shuffleEnabled = parcel.readInt() != 0,
        repeatMode = RepeatMode.entries[parcel.readInt()],
        volume = parcel.readFloat(),
        queueSize = parcel.readInt(),
        queuePosition = parcel.readInt()
    )
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(state.ordinal)
        parcel.writeParcelable(currentSong, flags)
        parcel.writeLong(positionMs)
        parcel.writeLong(durationMs)
        parcel.writeInt(if (shuffleEnabled) 1 else 0)
        parcel.writeInt(repeatMode.ordinal)
        parcel.writeFloat(volume)
        parcel.writeInt(queueSize)
        parcel.writeInt(queuePosition)
    }
    
    override fun describeContents(): Int = 0
    
    val isPlaying: Boolean
        get() = state == PlaybackState.PLAYING
    
    val isPaused: Boolean
        get() = state == PlaybackState.PAUSED
    
    val hasContent: Boolean
        get() = currentSong != null
    
    val progress: Float
        get() = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
    
    companion object CREATOR : Parcelable.Creator<PlayerState> {
        override fun createFromParcel(parcel: Parcel): PlayerState = PlayerState(parcel)
        override fun newArray(size: Int): Array<PlayerState?> = arrayOfNulls(size)
        
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

