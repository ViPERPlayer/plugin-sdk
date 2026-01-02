package com.viperplayer.plugin.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PluginConfiguration(
    val searchDebounceMs: Int? = null
) : Parcelable
