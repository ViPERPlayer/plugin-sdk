package com.viperplayer.plugin.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents an artist.
 * 
 * @property id Unique identifier for this artist
 * @property name Artist name
 * @property imageUrl URL to artist image/photo
 * @property genres List of genres associated with this artist
 * @property followerCount Number of followers (if available)
 * @property bio Short biography (if available)
 */
@Parcelize
data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val genres: List<String> = emptyList(),
    val followerCount: Long? = null,
    val bio: String? = null
) : Parcelable
