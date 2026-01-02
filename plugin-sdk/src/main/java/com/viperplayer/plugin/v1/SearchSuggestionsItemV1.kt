package com.viperplayer.plugin.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchSuggestionsItemV1(
    val type: Type,
    val song: Song? = null,
    val artist: Artist? = null,
    val album: Album? = null,
    val playlist: Playlist? = null,
) : Parcelable {
    enum class Type {
        SONG,
        ARTIST,
        ALBUM,
        PLAYLIST,
    }
}