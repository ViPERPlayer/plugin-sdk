package com.viperplayer.plugin.sdk.v1

import android.os.Parcel
import android.os.Parcelable

/**
 * Represents an album.
 * 
 * @property id Unique identifier for this album
 * @property name Album name/title
 * @property artists Artists on this album (hierarchical - embedded, not referenced)
 * @property artworkUrl URL to album cover art
 * @property releaseYear Year the album was released
 * @property trackCount Number of tracks on the album
 * @property type Album type (album, single, EP, compilation)
 * @property songs Tracks on this album (only populated when fetching full details)
 */
data class Album(
    val id: MediaId,
    val name: String,
    val artists: List<Artist> = emptyList(),
    val artworkUrl: String? = null,
    val releaseYear: Int? = null,
    val trackCount: Int = 0,
    val type: AlbumType = AlbumType.ALBUM,
    val songs: List<Song>? = null // Null when only basic info is needed
) : Parcelable {
    
    constructor(parcel: Parcel) : this(
        id = parcel.readParcelable(MediaId::class.java.classLoader)!!,
        name = parcel.readString() ?: "",
        artists = mutableListOf<Artist>().apply {
            parcel.readTypedList(this, Artist.CREATOR)
        },
        artworkUrl = parcel.readString(),
        releaseYear = parcel.readValue(Int::class.java.classLoader) as? Int,
        trackCount = parcel.readInt(),
        type = AlbumType.entries[parcel.readInt()],
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
        parcel.writeTypedList(artists)
        parcel.writeString(artworkUrl)
        parcel.writeValue(releaseYear)
        parcel.writeInt(trackCount)
        parcel.writeInt(type.ordinal)
        if (songs != null) {
            parcel.writeInt(1)
            parcel.writeTypedList(songs)
        } else {
            parcel.writeInt(0)
        }
    }
    
    override fun describeContents(): Int = 0
    
    /** Primary artist name for display */
    val artistName: String
        get() = artists.firstOrNull()?.name ?: "Unknown Artist"
    
    companion object CREATOR : Parcelable.Creator<Album> {
        override fun createFromParcel(parcel: Parcel): Album = Album(parcel)
        override fun newArray(size: Int): Array<Album?> = arrayOfNulls(size)
    }
}

enum class AlbumType {
    ALBUM,
    SINGLE,
    EP,
    COMPILATION
}

