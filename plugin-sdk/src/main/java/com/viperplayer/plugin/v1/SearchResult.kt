package com.viperplayer.plugin.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Paginated search results containing multiple result types.
 * 
 * @property items Matching items
 * @property nextCursor Cursor for fetching next page (null if no more results)
 */
@Parcelize
data class SearchResult(
    val sections: List<Section> = emptyList(),
    val nextCursor: String? = null,
) : Parcelable {
    @Parcelize
    data class Section(
        val type: Type,
        val items: List<SearchSuggestionsItemV1>,
    ): Parcelable {
        enum class Type {
            TOP_RESULT,
            OTHER
        }
    }

    companion object {
        /** Search type flags */
        const val TYPE_SONG = 1
        const val TYPE_ALBUM = 2
        const val TYPE_ARTIST = 4
        const val TYPE_PLAYLIST = 8
        const val TYPE_ALL = TYPE_SONG or TYPE_ALBUM or TYPE_ARTIST or TYPE_PLAYLIST
    }
}
