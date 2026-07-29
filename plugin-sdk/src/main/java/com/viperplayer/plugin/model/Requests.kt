package com.viperplayer.plugin.model

import kotlinx.serialization.Serializable

/** Generic single-id request (getSong/getAlbum/getArtist/getPlaylist/closeStream, etc.). */
@Serializable
data class IdRequest(val id: String)

/** Id + pagination (artist songs/albums, playlist songs). */
@Serializable
data class IdPageRequest(val id: String, val page: PageRequest = PageRequest())

/** A bare free-text query (search suggestions). */
@Serializable
data class QueryRequest(val query: String)

/** Browse a category's contents, paged. */
@Serializable
data class CategoryContentsRequest(val categoryId: String, val page: PageRequest = PageRequest())

/** Re-fetch a home section's items for a tapped filter chip. */
@Serializable
data class FilterSectionRequest(val sectionId: String, val filterKey: String)

/**
 * Fetch the Home feed, optionally filtered by a tapped top-level [HomeChip]. [chipId] null = the base
 * feed. Tolerant of an older host that only sent an empty bundle (decodes to a null chip).
 */
@Serializable
data class HomeRequest(val chipId: String? = null)

/** Fetch the next page of Home sections for an opaque [continuation] token (infinite scroll). */
@Serializable
data class HomeContinuationRequest(val continuation: String)

/**
 * Resolve a playable's stream. [type] tells the plugin whether this is audio ([MediaType.SONG]) or
 * video ([MediaType.VIDEO]) so it can use the right endpoint. Defaults to SONG for tolerant decoding
 * of an older host that only sent an id.
 */
@Serializable
data class ResolveStreamRequest(
    val id: String,
    val type: MediaType = MediaType.SONG,
    /** Host's max audio bitrate (kbps) from the user's quality setting; null = no cap (highest). */
    val maxBitrateKbps: Int? = null,
)

@Serializable
data class SeekStreamRequest(val streamId: String, val positionMs: Long)

/** A single boolean response (e.g. whether a seek succeeded). */
@Serializable
data class BoolResult(val value: Boolean)

// ---- Library WRITE (push) requests/responses ----
// These back the optional account-library write verbs. All fields use the plugin's own source ids.

/** Set (or clear) a track's liked/saved state on the account. */
@Serializable
data class SetLikedRequest(val trackId: String, val liked: Boolean)

/** Follow (or unfollow) an artist on the account. */
@Serializable
data class SetFollowedRequest(val artistId: String, val followed: Boolean)

/** Create a playlist on the account. */
@Serializable
data class CreatePlaylistRequest(val name: String)

/**
 * The id the account assigned to a freshly created playlist. The host stores it so later
 * add/remove/rename mutations for that playlist route to the right remote id.
 */
@Serializable
data class CreatePlaylistResult(val playlistId: String)

/** Rename a playlist on the account. */
@Serializable
data class RenamePlaylistRequest(val playlistId: String, val name: String)

/** A single playlist target (delete). */
@Serializable
data class PlaylistRequest(val playlistId: String)

/** Add/remove a track within a playlist on the account. */
@Serializable
data class PlaylistTrackRequest(val playlistId: String, val trackId: String)
