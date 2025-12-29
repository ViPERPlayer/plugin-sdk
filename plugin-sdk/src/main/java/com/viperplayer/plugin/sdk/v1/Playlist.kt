package com.viperplayer.plugin.sdk.v1

import android.os.Parcel
import android.os.Parcelable

/**
 * Represents a playlist.
 * 
 * @property id Unique identifier for this playlist
 * @property name Playlist name/title
 * @property description Playlist description
 * @property artworkUrl URL to playlist cover art
 * @property ownerName Name of the playlist owner/creator
 * @property songCount Number of songs in the playlist
 * @property isPublic Whether the playlist is publicly visible
 * @property isEditable Whether the current user can edit this playlist
 * @property songs Tracks in this playlist (only populated when fetching full details)
 */
data class Playlist(
    val id: MediaId,
    val name: String,
    val description: String? = null,
    val artworkUrl: String? = null,
    val ownerName: String? = null,
    val songCount: Int = 0,
    val isPublic: Boolean = true,
    val isEditable: Boolean = false,
    val songs: List<Song>? = null
) : Parcelable {
    
    constructor(parcel: Parcel) : this(
        id = parcel.readParcelable(MediaId::class.java.classLoader)!!,
        name = parcel.readString() ?: "",
        description = parcel.readString(),
        artworkUrl = parcel.readString(),
        ownerName = parcel.readString(),
        songCount = parcel.readInt(),
        isPublic = parcel.readInt() != 0,
        isEditable = parcel.readInt() != 0,
        songs = parcel.readInt().let { hasSongs ->
            if (hasSongs == 1) {
                mutableListOf<Song>().apply {
                    parcel.readTypedList(this, Song.CREATOR)
                }
            } else null
        }
    )
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(id, flags)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(artworkUrl)
        parcel.writeString(ownerName)
        parcel.writeInt(songCount)
        parcel.writeInt(if (isPublic) 1 else 0)
        parcel.writeInt(if (isEditable) 1 else 0)
        if (songs != null) {
            parcel.writeInt(1)
            parcel.writeTypedList(songs)
        } else {
            parcel.writeInt(0)
        }
    }
    
    override fun describeContents(): Int = 0
    
    companion object CREATOR : Parcelable.Creator<Playlist> {
        override fun createFromParcel(parcel: Parcel): Playlist = Playlist(parcel)
        override fun newArray(size: Int): Array<Playlist?> = arrayOfNulls(size)
    }
}

