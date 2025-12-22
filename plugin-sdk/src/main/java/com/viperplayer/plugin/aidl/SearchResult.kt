package com.viperplayer.plugin.aidl

import android.os.Parcel
import android.os.Parcelable

/**
 * Paginated search results containing multiple result types.
 * 
 * @property songs Matching songs
 * @property albums Matching albums
 * @property artists Matching artists
 * @property playlists Matching playlists
 * @property nextCursor Cursor for fetching next page (null if no more results)
 * @property totalSongs Total number of matching songs (if known)
 * @property totalAlbums Total number of matching albums (if known)
 * @property totalArtists Total number of matching artists (if known)
 * @property totalPlaylists Total number of matching playlists (if known)
 */
data class SearchResult(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val nextCursor: String? = null,
    val totalSongs: Int? = null,
    val totalAlbums: Int? = null,
    val totalArtists: Int? = null,
    val totalPlaylists: Int? = null
) : Parcelable {
    
    constructor(parcel: Parcel) : this(
        songs = mutableListOf<Song>().apply {
            parcel.readTypedList(this, Song.CREATOR)
        },
        albums = mutableListOf<Album>().apply {
            parcel.readTypedList(this, Album.CREATOR)
        },
        artists = mutableListOf<Artist>().apply {
            parcel.readTypedList(this, Artist.CREATOR)
        },
        playlists = mutableListOf<Playlist>().apply {
            parcel.readTypedList(this, Playlist.CREATOR)
        },
        nextCursor = parcel.readString(),
        totalSongs = parcel.readValue(Int::class.java.classLoader) as? Int,
        totalAlbums = parcel.readValue(Int::class.java.classLoader) as? Int,
        totalArtists = parcel.readValue(Int::class.java.classLoader) as? Int,
        totalPlaylists = parcel.readValue(Int::class.java.classLoader) as? Int
    )
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(songs)
        parcel.writeTypedList(albums)
        parcel.writeTypedList(artists)
        parcel.writeTypedList(playlists)
        parcel.writeString(nextCursor)
        parcel.writeValue(totalSongs)
        parcel.writeValue(totalAlbums)
        parcel.writeValue(totalArtists)
        parcel.writeValue(totalPlaylists)
    }
    
    override fun describeContents(): Int = 0
    
    val isEmpty: Boolean
        get() = songs.isEmpty() && albums.isEmpty() && artists.isEmpty() && playlists.isEmpty()
    
    val hasMore: Boolean
        get() = nextCursor != null
    
    companion object CREATOR : Parcelable.Creator<SearchResult> {
        override fun createFromParcel(parcel: Parcel): SearchResult = SearchResult(parcel)
        override fun newArray(size: Int): Array<SearchResult?> = arrayOfNulls(size)
        
        /** Search type flags */
        const val TYPE_SONG = 1
        const val TYPE_ALBUM = 2
        const val TYPE_ARTIST = 4
        const val TYPE_PLAYLIST = 8
        const val TYPE_ALL = TYPE_SONG or TYPE_ALBUM or TYPE_ARTIST or TYPE_PLAYLIST
    }
}

