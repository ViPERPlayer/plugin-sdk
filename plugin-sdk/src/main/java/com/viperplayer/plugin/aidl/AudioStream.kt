package com.viperplayer.plugin.aidl

import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.Parcelable

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
data class AudioStream(
    val streamId: String,
    val mediaId: MediaId,
    val format: AudioFormat,
    val durationMs: Long,
    val pipe: ParcelFileDescriptor,
    val canSeek: Boolean = true
) : Parcelable {
    
    constructor(parcel: Parcel) : this(
        streamId = parcel.readString() ?: "",
        mediaId = parcel.readParcelable(MediaId::class.java.classLoader)!!,
        format = parcel.readParcelable(AudioFormat::class.java.classLoader)!!,
        durationMs = parcel.readLong(),
        pipe = parcel.readParcelable(ParcelFileDescriptor::class.java.classLoader)!!,
        canSeek = parcel.readInt() != 0
    )
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(streamId)
        parcel.writeParcelable(mediaId, flags)
        parcel.writeParcelable(format, flags)
        parcel.writeLong(durationMs)
        parcel.writeParcelable(pipe, flags)
        parcel.writeInt(if (canSeek) 1 else 0)
    }
    
    override fun describeContents(): Int = Parcelable.CONTENTS_FILE_DESCRIPTOR
    
    companion object CREATOR : Parcelable.Creator<AudioStream> {
        override fun createFromParcel(parcel: Parcel): AudioStream = AudioStream(parcel)
        override fun newArray(size: Int): Array<AudioStream?> = arrayOfNulls(size)
    }
}

