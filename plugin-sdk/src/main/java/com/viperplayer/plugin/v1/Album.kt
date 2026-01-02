package com.viperplayer.plugin.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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
@Parcelize
data class Album(
    val id: String,
    val name: String,
    val artists: List<Artist> = emptyList(),
    val artworkUrl: String? = null,
    val releaseYear: Int? = null,
    val trackCount: Int = 0,
    val isExplicit: Boolean = false,
    val type: AlbumType = AlbumType.ALBUM,
    val songs: List<Song>? = null // Null when only basic info is needed
) : Parcelable {
    /** Primary artist name for display */
    val artistName: String
        get() = artists.firstOrNull()?.name ?: "Unknown Artist"
}

enum class AlbumType {
    ALBUM,
    SINGLE,
    EP,
    COMPILATION
}
