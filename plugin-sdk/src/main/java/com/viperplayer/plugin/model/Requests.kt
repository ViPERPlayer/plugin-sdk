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
 * Resolve a playable's stream. [type] tells the plugin whether this is audio ([MediaType.SONG]) or
 * video ([MediaType.VIDEO]) so it can use the right endpoint. Defaults to SONG for tolerant decoding
 * of an older host that only sent an id.
 */
@Serializable
data class ResolveStreamRequest(val id: String, val type: MediaType = MediaType.SONG)

@Serializable
data class SeekStreamRequest(val streamId: String, val positionMs: Long)

/** A single boolean response (e.g. whether a seek succeeded). */
@Serializable
data class BoolResult(val value: Boolean)
