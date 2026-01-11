package com.viperplayer.plugin.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a song/track.
 * Hierarchical model - contains embedded Artist and Album info.
 * 
 * @property id Unique identifier for this song
 * @property title Song title
 * @property artists Artists performing this song
 * @property album Album this song belongs to (may be null for singles)
 * @property durationMs Duration in milliseconds
 * @property artwork Artwork (falls back to album artwork)
 * @property trackNumber Track number on the album
 * @property discNumber Disc number for multi-disc albums
 * @property isExplicit Whether the song has explicit content
 * @property isPlayable Whether the song can be played (may be unavailable in region)
 * @property requiresInternet Whether the song requires internet connection to play (false for local files)
 * @property releaseYear Year the song was released
 * @property genres Genres associated with this song
 */
@Parcelize
data class Song(
    val id: String,
    val title: String,
    val artists: List<Artist> = emptyList(),
    val album: Album? = null,
    val durationMs: Long? = 0,
    val replayGainDb: Float? = null, // ReplayGain value in dB (will be converted to linear for volume)
    val peakAmplitude: Float? = null, // Peak amplitude (0.0-1.0+)
    val artwork: Artwork? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val isExplicit: Boolean = false,
    val isPlayable: Boolean = true,
    val requiresInternet: Boolean = true, // Default to true for streaming services
    val releaseYear: Int? = null,
    val genres: List<String> = emptyList()
) : Parcelable {
    /** Primary artist name for display */
    val artistName: String
        get() = artists.firstOrNull()?.name ?: "Unknown Artist"
    
    /** Album name for display */
    val albumName: String
        get() = album?.name ?: ""

//    /** Effective artwork URL (song's own or album's) */
//    val effectiveArtworkUrl: String?
//        get() = artworkUrl ?: album?.artworkUrl
}
