package com.viperplayer.plugin.model

import kotlinx.serialization.Serializable

// ---- Search ----

@Serializable
data class SearchRequest(
    val query: String,
    /** Empty = search all supported types. */
    val types: Set<MediaType> = emptySet(),
    val page: PageRequest = PageRequest(),
)

@Serializable
data class SearchResult(
    val items: List<MediaItem> = emptyList(),
    val nextCursor: String? = null,
)

@Serializable
data class SearchSuggestions(
    val suggestions: List<String> = emptyList(),
    val items: List<MediaItem> = emptyList(),
)

// ---- Browse ----

@Serializable
enum class CategoryContentType { CATEGORIES, PLAYLISTS, ALBUMS, ARTISTS, SONGS, MIXED }

@Serializable
data class BrowseCategory(
    val id: String,
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val contentType: CategoryContentType = CategoryContentType.MIXED,
)

// ---- Home ----

@Serializable
enum class SectionLayout { LIST, GRID }

@Serializable
data class HomeSection(
    val id: String,
    val title: String,
    val items: List<MediaItem> = emptyList(),
    val layout: SectionLayout = SectionLayout.LIST,
)

@Serializable
data class HomeContent(
    val quickPicks: List<MediaItem> = emptyList(),
    val sections: List<HomeSection> = emptyList(),
)
