package com.viperplayer.plugin.sdk.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Describes what features a plugin supports.
 * Used for graceful degradation when plugins don't support certain features.
 */
@Parcelize
data class PluginCapabilities(
    /** Can search for songs */
    val canSearch: Boolean = true,
    
    /** Can browse categories/discovery */
    val canBrowse: Boolean = true,
    
    /** Has user library access */
    val hasLibrary: Boolean = true,
    
    /** Can provide playlists */
    val hasPlaylists: Boolean = true,
    
    /** Supports seeking in audio streams */
    val canSeek: Boolean = true,
    
    /** Can provide lyrics */
    val hasLyrics: Boolean = false,
    
    /** Can provide high-quality audio (lossless) */
    val hasHighQuality: Boolean = false,
    
    /** Supports offline playback */
    val supportsOffline: Boolean = false,
    
    /** Has custom settings UI */
    val hasSettings: Boolean = false,
    
    /** Can modify playlists (add/remove songs) */
    val canEditPlaylists: Boolean = false,
    
    /** Can like/save songs to library */
    val canSaveToLibrary: Boolean = false,
    
    /** Supports radio/similar songs */
    val hasRadio: Boolean = false,
    
    /** Supported audio qualities (bitrates in kbps) */
    val supportedQualities: List<Int> = listOf(128, 256, 320)
) : Parcelable
