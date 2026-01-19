package com.viperplayer.plugin.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a custom section on the home screen provided by a plugin.
 */
@Parcelize
data class HomeSection(
    val id: String,
    val title: String,
    val items: List<MediaItemV1>,
    val layout: Layout = Layout.LIST
) : Parcelable {

    enum class Layout {
        LIST,
        GRID
    }
}
