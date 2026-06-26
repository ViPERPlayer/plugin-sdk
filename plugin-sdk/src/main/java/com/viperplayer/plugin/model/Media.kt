package com.viperplayer.plugin.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The kinds of catalog entities a source plugin can return. */
@Serializable
enum class MediaType { SONG, ALBUM, ARTIST, PLAYLIST }

/** Cover/thumbnail art. URLs are resolved by the host's image pipeline (or a `content://` URI). */
@Serializable
data class Artwork(
    val thumbnailUrl: String? = null,
    val fullUrl: String? = null,
)

@Serializable
enum class AlbumType { ALBUM, SINGLE, EP, COMPILATION }

/**
 * A catalog entity. Sealed so it can travel polymorphically in mixed lists (search results, home
 * sections) — the codec tags each item with its concrete type.
 *
 * Every `id` is opaque to the host and only meaningful to the plugin that produced it; the host
 * pairs it with the plugin id to form a globally-unique reference.
 */
@Serializable
sealed interface MediaItem {
    val id: String
}

@Serializable
@SerialName("song")
data class Song(
    override val id: String,
    val title: String,
    val artists: List<Artist> = emptyList(),
    val album: Album? = null,
    val durationMs: Long? = null,
    val artwork: Artwork? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val releaseYear: Int? = null,
    val genres: List<String> = emptyList(),
    val isExplicit: Boolean = false,
    val isPlayable: Boolean = true,
    val requiresInternet: Boolean = true,
    val replayGainDb: Float? = null,
    val peakAmplitude: Float? = null,
    /** Open-ended, plugin-specific data the host passes through untouched. */
    val extras: Map<String, String> = emptyMap(),
) : MediaItem

@Serializable
@SerialName("album")
data class Album(
    override val id: String,
    val name: String,
    val artists: List<Artist> = emptyList(),
    val artwork: Artwork? = null,
    val releaseYear: Int? = null,
    val trackCount: Int? = null,
    val isExplicit: Boolean = false,
    val type: AlbumType = AlbumType.ALBUM,
    /** Populated on a detail fetch; empty when the album appears inside a list. */
    val songs: List<Song> = emptyList(),
    val extras: Map<String, String> = emptyMap(),
) : MediaItem

@Serializable
@SerialName("artist")
data class Artist(
    override val id: String,
    val name: String,
    val artwork: Artwork? = null,
    val bio: String? = null,
    val topSongs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val singles: List<Album> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val similarArtists: List<Artist> = emptyList(),
    val extras: Map<String, String> = emptyMap(),
) : MediaItem

@Serializable
@SerialName("playlist")
data class Playlist(
    override val id: String,
    val name: String,
    val description: String? = null,
    val artwork: Artwork? = null,
    val ownerName: String? = null,
    val trackCount: Int? = null,
    val isEditable: Boolean = false,
    /** Populated on a detail fetch; empty when the playlist appears inside a list. */
    val songs: List<Song> = emptyList(),
    val extras: Map<String, String> = emptyMap(),
) : MediaItem
