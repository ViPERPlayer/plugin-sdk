package com.viperplayer.plugin.sdk.v1

import android.os.Parcel
import android.os.Parcelable

/**
 * Describes what features a plugin supports.
 * Used for graceful degradation when plugins don't support certain features.
 */
data class PluginCapabilities(
    /** Can search for songs */
    val canSearch: Boolean = true,
    
    /** Can browse categories/discovery */
    val canBrowse: Boolean = true,
    
    /** Has user library access */
    val hasLibrary: Boolean = true,
    
    /** Can provide playlists */
    val hasPlaylists: Boolean = true,
    
    /** Supports seeking in audio streams */
    val canSeek: Boolean = true,
    
    /** Can provide lyrics */
    val hasLyrics: Boolean = false,
    
    /** Can provide high-quality audio (lossless) */
    val hasHighQuality: Boolean = false,
    
    /** Supports offline playback */
    val supportsOffline: Boolean = false,
    
    /** Has custom settings UI */
    val hasSettings: Boolean = false,
    
    /** Can modify playlists (add/remove songs) */
    val canEditPlaylists: Boolean = false,
    
    /** Can like/save songs to library */
    val canSaveToLibrary: Boolean = false,
    
    /** Supports radio/similar songs */
    val hasRadio: Boolean = false,
    
    /** Supported audio qualities (bitrates in kbps) */
    val supportedQualities: List<Int> = listOf(128, 256, 320)
) : Parcelable {
    
    constructor(parcel: Parcel) : this(
        canSearch = parcel.readInt() != 0,
        canBrowse = parcel.readInt() != 0,
        hasLibrary = parcel.readInt() != 0,
        hasPlaylists = parcel.readInt() != 0,
        canSeek = parcel.readInt() != 0,
        hasLyrics = parcel.readInt() != 0,
        hasHighQuality = parcel.readInt() != 0,
        supportsOffline = parcel.readInt() != 0,
        hasSettings = parcel.readInt() != 0,
        canEditPlaylists = parcel.readInt() != 0,
        canSaveToLibrary = parcel.readInt() != 0,
        hasRadio = parcel.readInt() != 0,
        supportedQualities = mutableListOf<Int>().apply {
            val size = parcel.readInt()
            repeat(size) { add(parcel.readInt()) }
        }
    )
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(if (canSearch) 1 else 0)
        parcel.writeInt(if (canBrowse) 1 else 0)
        parcel.writeInt(if (hasLibrary) 1 else 0)
        parcel.writeInt(if (hasPlaylists) 1 else 0)
        parcel.writeInt(if (canSeek) 1 else 0)
        parcel.writeInt(if (hasLyrics) 1 else 0)
        parcel.writeInt(if (hasHighQuality) 1 else 0)
        parcel.writeInt(if (supportsOffline) 1 else 0)
        parcel.writeInt(if (hasSettings) 1 else 0)
        parcel.writeInt(if (canEditPlaylists) 1 else 0)
        parcel.writeInt(if (canSaveToLibrary) 1 else 0)
        parcel.writeInt(if (hasRadio) 1 else 0)
        parcel.writeInt(supportedQualities.size)
        supportedQualities.forEach { parcel.writeInt(it) }
    }
    
    override fun describeContents(): Int = 0
    
    companion object CREATOR : Parcelable.Creator<PluginCapabilities> {
        override fun createFromParcel(parcel: Parcel): PluginCapabilities = PluginCapabilities(parcel)
        override fun newArray(size: Int): Array<PluginCapabilities?> = arrayOfNulls(size)
    }
}

