package com.viperplayer.plugin.model

import kotlinx.serialization.SerialName
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

/** Shape hint for the artwork of the items in a section. */
@Serializable
enum class ItemShape {
    /** Square artwork — tracks, playlists, albums (the default). */
    SQUARE,

    /** Circular artwork — artists / users. */
    CIRCLE,

    /** Wide 16:9 artwork — videos, stations, promo tiles. */
    WIDE,
}

/** Optional "see all" / navigation affordance shown on a section header. */
@Serializable
data class SectionAction(
    val label: String,
    /** Opaque, plugin-defined target (e.g. a playlist id or browse cursor); null = no navigation. */
    val targetId: String? = null,
)

/**
 * A home-screen section. Sealed so each visual design carries exactly the fields it needs and can
 * travel polymorphically (the codec tags each with its `#t` discriminator). A design a newer plugin
 * emits that this host doesn't understand decodes to [UnknownSection] and is skipped, rather than
 * breaking the whole feed — see [com.viperplayer.plugin.protocol.Codec].
 */
@Serializable
sealed interface HomeSection {
    val id: String
    val title: String
    val subtitle: String?
    val action: SectionAction?
}

/** Horizontal scroller of cards — the most common design. */
@Serializable
@SerialName("carousel")
data class CarouselSection(
    override val id: String,
    override val title: String = "",
    override val subtitle: String? = null,
    override val action: SectionAction? = null,
    val items: List<MediaItem> = emptyList(),
    val itemShape: ItemShape = ItemShape.SQUARE,
) : HomeSection

/** Multi-column grid of cards. */
@Serializable
@SerialName("grid")
data class GridSection(
    override val id: String,
    override val title: String = "",
    override val subtitle: String? = null,
    override val action: SectionAction? = null,
    val items: List<MediaItem> = emptyList(),
    val columns: Int = 2,
    val itemShape: ItemShape = ItemShape.SQUARE,
) : HomeSection

/** Vertical list of rows — e.g. a chart or a track list. */
@Serializable
@SerialName("list")
data class ListSection(
    override val id: String,
    override val title: String = "",
    override val subtitle: String? = null,
    override val action: SectionAction? = null,
    val items: List<MediaItem> = emptyList(),
) : HomeSection

/** A single, prominently featured item with an optional backdrop and blurb. */
@Serializable
@SerialName("hero")
data class HeroSection(
    override val id: String,
    override val title: String = "",
    override val subtitle: String? = null,
    override val action: SectionAction? = null,
    val item: MediaItem,
    val backgroundImageUrl: String? = null,
    val description: String? = null,
) : HomeSection

/** A promotional banner: text / image + an action, with no media items of its own. */
@Serializable
@SerialName("banner")
data class BannerSection(
    override val id: String,
    override val title: String = "",
    override val subtitle: String? = null,
    override val action: SectionAction? = null,
    val text: String? = null,
    val imageUrl: String? = null,
) : HomeSection

/** Forward-compat fallback for a design a newer plugin emits that this host doesn't understand. */
@Serializable
@SerialName("unknown")
data class UnknownSection(
    override val id: String = "",
    override val title: String = "",
    override val subtitle: String? = null,
    override val action: SectionAction? = null,
) : HomeSection

@Serializable
data class HomeContent(
    val quickPicks: List<MediaItem> = emptyList(),
    val sections: List<HomeSection> = emptyList(),
)
