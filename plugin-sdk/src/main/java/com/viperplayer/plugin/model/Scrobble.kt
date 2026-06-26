package com.viperplayer.plugin.model

import kotlinx.serialization.Serializable

@Serializable
enum class PlaybackEventKind { START, RESUME, PAUSE, COMPLETE, SCROBBLE }

/**
 * A playback-reporting event the host sends to a scrobble-capable plugin. The host owns the player
 * and decides when these fire; the plugin just records/forwards them (e.g. to Last.fm).
 *
 * [timestampMs] is wall-clock epoch millis (the host stamps it) so the plugin needn't read the
 * clock itself.
 */
@Serializable
data class PlaybackEvent(
    val mediaId: String,
    val title: String,
    val artist: String? = null,
    val album: String? = null,
    val durationMs: Long? = null,
    val positionMs: Long = 0,
    val timestampMs: Long = 0,
    val kind: PlaybackEventKind,
)
