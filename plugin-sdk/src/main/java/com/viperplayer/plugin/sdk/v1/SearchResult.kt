package com.viperplayer.plugin.sdk.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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
@Parcelize
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
    val isEmpty: Boolean
        get() = songs.isEmpty() && albums.isEmpty() && artists.isEmpty() && playlists.isEmpty()
    
    val hasMore: Boolean
        get() = nextCursor != null
    
    companion object {
        /** Search type flags */
        const val TYPE_SONG = 1
        const val TYPE_ALBUM = 2
        const val TYPE_ARTIST = 4
        const val TYPE_PLAYLIST = 8
        const val TYPE_ALL = TYPE_SONG or TYPE_ALBUM or TYPE_ARTIST or TYPE_PLAYLIST
    }
}
