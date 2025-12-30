package com.viperplayer.plugin.sdk.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchSuggestionsResultV1(
    val suggestions: List<String>,
    val items: List<SearchSuggestionsItemV1>
) : Parcelable