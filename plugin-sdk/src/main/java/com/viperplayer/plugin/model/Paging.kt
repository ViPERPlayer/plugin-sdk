package com.viperplayer.plugin.model

import kotlinx.serialization.Serializable

/**
 * A bounded page of results plus an opaque cursor for the next page (null = no more). Cursor-based
 * paging is how lists stay under the binder transaction limit: the plugin returns a small page and
 * the host asks for the next one with [PageRequest.cursor].
 */
@Serializable
data class Page<T>(
    val items: List<T> = emptyList(),
    val nextCursor: String? = null,
    val totalCount: Int? = null,
)

/** Pagination input: where to continue from, and how many items to return. */
@Serializable
data class PageRequest(
    val cursor: String? = null,
    val limit: Int = 20,
)
