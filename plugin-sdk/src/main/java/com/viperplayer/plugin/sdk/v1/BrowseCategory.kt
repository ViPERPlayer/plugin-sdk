package com.viperplayer.plugin.sdk.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a browsable category for discovery.
 * Categories can contain sub-categories, playlists, albums, or artists.
 * 
 * @property id Unique identifier for this category
 * @property name Category name (e.g., "Rock", "New Releases", "Charts")
 * @property description Optional category description
 * @property imageUrl URL to category image
 * @property contentType What type of content this category contains
 */
@Parcelize
data class BrowseCategory(
    val id: String,
    val pluginId: String,
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val contentType: CategoryContentType = CategoryContentType.MIXED
) : Parcelable

/**
 * Describes what type of content a category contains.
 */
enum class CategoryContentType {
    /** Contains sub-categories */
    CATEGORIES,
    /** Contains playlists */
    PLAYLISTS,
    /** Contains albums */
    ALBUMS,
    /** Contains artists */
    ARTISTS,
    /** Contains songs */
    SONGS,
    /** Contains mixed content types */
    MIXED
}
