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
 * An interactive chip on a section (e.g. a genre selector). Tapping it asks the owning plugin to
 * [com.viperplayer.plugin.author.SourceProvider.filterSection] this section by [key]; the host swaps
 * the returned items in and marks the tapped chip [selected].
 */
@Serializable
data class SectionFilter(
    val label: String,
    /** Opaque, plugin-defined filter id passed back to `filterSection`. */
    val key: String,
    val selected: Boolean = false,
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

/**
 * Horizontal scroller of cards — the most common design. [rows] > 1 stacks items into that many
 * rows that scroll together horizontally (a multi-row "shelf"); the default 1 is a single row.
 */
@Serializable
@SerialName("carousel")
data class CarouselSection(
    override val id: String,
    override val title: String = "",
    override val subtitle: String? = null,
    override val action: SectionAction? = null,
    val items: List<MediaItem> = emptyList(),
    val itemShape: ItemShape = ItemShape.SQUARE,
    val rows: Int = 1,
    val filters: List<SectionFilter> = emptyList(),
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

/**
 * A big, full-bleed swipeable carousel of large artwork cards, each with a gradient scrim and a
 * title/subtitle overlay — modelled on the reference design's "Daily Discover". Rendered with M3's
 * `HorizontalMultiBrowseCarousel`, so adjacent cards peek in at the edges.
 */
@Serializable
@SerialName("multibrowse")
data class MultiBrowseCarouselSection(
    override val id: String,
    override val title: String = "",
    override val subtitle: String? = null,
    override val action: SectionAction? = null,
    val items: List<MediaItem> = emptyList(),
    val itemShape: ItemShape = ItemShape.WIDE,
    /** Draw a darkening gradient scrim behind the overlaid title/subtitle (off for flat tiles). */
    val showBackdrop: Boolean = true,
) : HomeSection

/**
 * A multi-row, horizontally-snapping grid of compact item rows — modelled on the reference design's "Quick
 * Picks" / "Forgotten favorites". Rendered with `LazyHorizontalGrid(rows = Fixed(rows))` + a snap
 * fling so each column snaps into place. [rows] is the number of stacked rows per column.
 */
@Serializable
@SerialName("quickpicks")
data class QuickPicksSection(
    override val id: String,
    override val title: String = "",
    override val subtitle: String? = null,
    override val action: SectionAction? = null,
    val items: List<MediaItem> = emptyList(),
    val rows: Int = 4,
) : HomeSection

/**
 * A horizontal row of LARGE rich cards (prominent artwork + title + subtitle + a play affordance) —
 * modelled on the reference design's "From the community", for featured playlists/albums. Bigger than a
 * [CarouselSection] card.
 */
@Serializable
@SerialName("featured")
data class FeaturedCardsSection(
    override val id: String,
    override val title: String = "",
    override val subtitle: String? = null,
    override val action: SectionAction? = null,
    val items: List<MediaItem> = emptyList(),
    val itemShape: ItemShape = ItemShape.SQUARE,
) : HomeSection

/**
 * A single mood/genre chip on a [MoodGridSection]. Tapping it navigates to the plugin's category
 * screen for [targetId] (via `SourceProvider.getCategoryContents`), like a [BrowseCategory] tile.
 */
@Serializable
data class MoodChip(
    val label: String,
    /** Opaque, plugin-defined category id opened when the chip is tapped. */
    val targetId: String,
    val artworkUrl: String? = null,
    /** Optional accent colour as an `#RRGGBB` / `#AARRGGBB` hex string; the host tints the chip. */
    val accentColor: String? = null,
)

/**
 * A grid/flow of mood/genre chips — modelled on the reference design's "Mood and genres". Rendered as a
 * multi-column grid of colored chip cards; tapping one opens that category.
 */
@Serializable
@SerialName("moodgrid")
data class MoodGridSection(
    override val id: String,
    override val title: String = "",
    override val subtitle: String? = null,
    override val action: SectionAction? = null,
    val chips: List<MoodChip> = emptyList(),
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

/**
 * A top-level filter chip on the Home feed (e.g. "Relax", "Workout", "Podcasts") — modelled on
 * the reference design's home chip row. Tapping one asks the plugin to re-fetch the whole feed
 * filtered by [id] via [com.viperplayer.plugin.author.SourceProvider.getHome] with a chipId; the
 * host replaces the feed with the returned content.
 *
 * OPTIONAL and backward-compatible: a plugin that emits no chips ([HomeContent.chips] empty) shows no
 * chip row at all, and the chip-aware getHome overload defaults to ignoring the chip.
 */
@Serializable
data class HomeChip(
    /** Opaque, plugin-defined chip id passed back to `getHome(chipId)`. */
    val id: String,
    val title: String,
)

@Serializable
data class HomeContent(
    val quickPicks: List<MediaItem> = emptyList(),
    val sections: List<HomeSection> = emptyList(),
    /**
     * OPTIONAL top-level filter chips shown above the feed. Empty (the default) = no chip row, so
     * existing plugins are unaffected. Wire-compatible: absent in an old payload decodes to empty.
     */
    val chips: List<HomeChip> = emptyList(),
    /**
     * OPTIONAL opaque pagination token for infinite scroll. Non-null = more sections can be fetched
     * via [com.viperplayer.plugin.author.SourceProvider.getHomeContinuation]; null (the default) = no
     * more pages. Wire-compatible: absent in an old payload decodes to null.
     */
    val continuation: String? = null,
)
