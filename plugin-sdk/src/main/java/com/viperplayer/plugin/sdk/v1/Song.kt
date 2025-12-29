package com.viperplayer.plugin.sdk.v1

import android.os.Parcel
import android.os.Parcelable

/**
 * Represents a song/track.
 * Hierarchical model - contains embedded Artist and Album info.
 * 
 * @property id Unique identifier for this song
 * @property title Song title
 * @property artists Artists performing this song
 * @property album Album this song belongs to (may be null for singles)
 * @property durationMs Duration in milliseconds
 * @property artworkUrl URL to artwork (falls back to album artwork)
 * @property trackNumber Track number on the album
 * @property discNumber Disc number for multi-disc albums
 * @property isExplicit Whether the song has explicit content
 * @property isPlayable Whether the song can be played (may be unavailable in region)
 * @property releaseYear Year the song was released
 * @property genres Genres associated with this song
 */
data class Song(
    val id: MediaId,
    val title: String,
    val artists: List<Artist> = emptyList(),
    val album: Album? = null,
    val durationMs: Long = 0,
    val artworkUrl: String? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val isExplicit: Boolean = false,
    val isPlayable: Boolean = true,
    val releaseYear: Int? = null,
    val genres: List<String> = emptyList()
) : Parcelable {
    
    constructor(parcel: Parcel) : this(
        id = parcel.readParcelable(MediaId::class.java.classLoader)!!,
        title = parcel.readString() ?: "",
        artists = mutableListOf<Artist>().apply {
            parcel.readTypedList(this, Artist.CREATOR)
        },
        album = parcel.readParcelable(Album::class.java.classLoader),
        durationMs = parcel.readLong(),
        artworkUrl = parcel.readString(),
        trackNumber = parcel.readValue(Int::class.java.classLoader) as? Int,
        discNumber = parcel.readValue(Int::class.java.classLoader) as? Int,
        isExplicit = parcel.readInt() != 0,
        isPlayable = parcel.readInt() != 0,
        releaseYear = parcel.readValue(Int::class.java.classLoader) as? Int,
        genres = mutableListOf<String>().apply {
            parcel.readStringList(this)
        }
    )
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(id, flags)
        parcel.writeString(title)
        parcel.writeTypedList(artists)
        parcel.writeParcelable(album, flags)
        parcel.writeLong(durationMs)
        parcel.writeString(artworkUrl)
        parcel.writeValue(trackNumber)
        parcel.writeValue(discNumber)
        parcel.writeInt(if (isExplicit) 1 else 0)
        parcel.writeInt(if (isPlayable) 1 else 0)
        parcel.writeValue(releaseYear)
        parcel.writeStringList(genres)
    }
    
    override fun describeContents(): Int = 0
    
    /** Primary artist name for display */
    val artistName: String
        get() = artists.firstOrNull()?.name ?: "Unknown Artist"
    
    /** Album name for display */
    val albumName: String
        get() = album?.name ?: ""
    
    /** Effective artwork URL (song's own or album's) */
    val effectiveArtworkUrl: String?
        get() = artworkUrl ?: album?.artworkUrl
    
    /** Duration formatted as MM:SS or HH:MM:SS */
    val durationFormatted: String
        get() {
            val seconds = (durationMs / 1000) % 60
            val minutes = (durationMs / (1000 * 60)) % 60
            val hours = durationMs / (1000 * 60 * 60)
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }
    
    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song = Song(parcel)
        override fun newArray(size: Int): Array<Song?> = arrayOfNulls(size)
    }
}

