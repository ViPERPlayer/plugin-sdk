package com.viperplayer.plugin.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The kinds of catalog entities a source plugin can return. */
@Serializable
enum class MediaType { SONG, VIDEO, ALBUM, ARTIST, PLAYLIST }

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

/**
 * A playable catalog entity: audio ([Song]) or video ([Video]). They share a common surface so a
 * mixed playlist/album can carry both. The host plays both through the same (audio) player but tells
 * the plugin which kind it is at resolve time, since a plugin may use different stream endpoints for
 * audio and video.
 */
@Serializable
sealed interface PlayableItem : MediaItem {
    val title: String
    val artists: List<Artist>
    val album: Album?
    val durationMs: Long?
    val artwork: Artwork?
    val trackNumber: Int?
    val discNumber: Int?
    val isExplicit: Boolean
    val isPlayable: Boolean
    val replayGainDb: Float?
    val peakAmplitude: Float?
    val extras: Map<String, String>
}

/**
 * Fallback for a media kind a newer peer added that this peer doesn't understand. The codec decodes
 * an unknown `#t` discriminator into this (instead of throwing and failing the whole list); the host
 * skips it. Keeps a single new item type from breaking older hosts.
 */
@Serializable
@SerialName("unknown")
data class UnknownMediaItem(override val id: String = "") : MediaItem

/** [PlayableItem] equivalent of [UnknownMediaItem] — an unknown playable kind inside a mixed list. */
@Serializable
@SerialName("unknown_playable")
data class UnknownPlayableItem(
    override val id: String = "",
    override val title: String = "",
    override val artists: List<Artist> = emptyList(),
    override val album: Album? = null,
    override val durationMs: Long? = null,
    override val artwork: Artwork? = null,
    override val trackNumber: Int? = null,
    override val discNumber: Int? = null,
    override val isExplicit: Boolean = false,
    override val isPlayable: Boolean = false,
    override val replayGainDb: Float? = null,
    override val peakAmplitude: Float? = null,
    override val extras: Map<String, String> = emptyMap(),
) : PlayableItem

@Serializable
@SerialName("song")
data class Song(
    override val id: String,
    override val title: String,
    override val artists: List<Artist> = emptyList(),
    override val album: Album? = null,
    override val durationMs: Long? = null,
    override val artwork: Artwork? = null,
    override val trackNumber: Int? = null,
    override val discNumber: Int? = null,
    val releaseYear: Int? = null,
    val genres: List<String> = emptyList(),
    override val isExplicit: Boolean = false,
    override val isPlayable: Boolean = true,
    val requiresInternet: Boolean = true,
    override val replayGainDb: Float? = null,
    override val peakAmplitude: Float? = null,
    /** Open-ended, plugin-specific data the host passes through untouched. */
    override val extras: Map<String, String> = emptyMap(),
) : PlayableItem

@Serializable
@SerialName("video")
data class Video(
    override val id: String,
    override val title: String,
    override val artists: List<Artist> = emptyList(),
    override val album: Album? = null,
    override val durationMs: Long? = null,
    override val artwork: Artwork? = null,
    override val trackNumber: Int? = null,
    override val discNumber: Int? = null,
    override val isExplicit: Boolean = false,
    override val isPlayable: Boolean = true,
    override val replayGainDb: Float? = null,
    override val peakAmplitude: Float? = null,
    /** Open-ended, plugin-specific data the host passes through untouched. */
    override val extras: Map<String, String> = emptyMap(),
) : PlayableItem

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
    /** Populated on a detail fetch; empty when the album appears inside a list. May mix audio/video. */
    val songs: List<PlayableItem> = emptyList(),
    val extras: Map<String, String> = emptyMap(),
) : MediaItem

@Serializable
@SerialName("artist")
data class Artist(
    override val id: String,
    val name: String,
    val artwork: Artwork? = null,
    val bio: String? = null,
    val topSongs: List<PlayableItem> = emptyList(),
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
    /** Populated on a detail fetch; empty when the playlist appears inside a list. May mix audio/video. */
    val songs: List<PlayableItem> = emptyList(),
    val extras: Map<String, String> = emptyMap(),
) : MediaItem
