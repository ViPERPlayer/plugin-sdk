package com.viperplayer.plugin.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Container for home screen content provided by a plugin.
 */
@Parcelize
data class HomeContent(
    val quickPicks: List<MediaItemV1>? = null,
    val sections: List<HomeSection> = emptyList()
) : Parcelable
