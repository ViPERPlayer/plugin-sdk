package com.viperplayer.plugin.sdk.v1

import android.os.ParcelFileDescriptor
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents an active audio stream from a plugin.
 * Contains a ParcelFileDescriptor that the host reads PCM data from.
 * 
 * The plugin is responsible for:
 * 1. Decoding the audio source
 * 2. Writing PCM data to the pipe
 * 3. Handling seek requests
 * 4. Closing the pipe when done or stopped
 * 
 * @property streamId Unique identifier for this stream (used for control operations)
 * @property mediaId The song being streamed
 * @property format Audio format of the PCM data
 * @property durationMs Total duration in milliseconds (if known)
 * @property pipe File descriptor for reading PCM data (host reads from this)
 * @property canSeek Whether this stream supports seeking
 */
@Parcelize
data class AudioStream(
    val streamId: String,
    val mediaId: MediaId,
    val format: AudioFormat,
    val durationMs: Long,
    val pipe: ParcelFileDescriptor,
    val canSeek: Boolean = true
) : Parcelable
