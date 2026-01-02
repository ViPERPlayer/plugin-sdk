package com.viperplayer.plugin.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a playlist.
 * 
 * @property id Unique identifier for this playlist
 * @property name Playlist name/title
 * @property description Playlist description
 * @property artworkUrl URL to playlist cover art
 * @property ownerName Name of the playlist owner/creator
 * @property songCount Number of songs in the playlist
 * @property isPublic Whether the playlist is publicly visible
 * @property isEditable Whether the current user can edit this playlist
 * @property songs Tracks in this playlist (only populated when fetching full details)
 */
@Parcelize
data class Playlist(
    val id: String,
    val name: String,
    val description: String? = null,
    val artworkUrl: String? = null,
    val ownerName: String? = null,
    val songCount: Int = 0,
    val isPublic: Boolean = true,
    val isEditable: Boolean = false,
    val songs: List<Song>? = null
) : Parcelable
