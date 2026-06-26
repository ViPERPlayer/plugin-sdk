package com.viperplayer.plugin.model

import kotlinx.serialization.Serializable

@Serializable
enum class PlaybackState { IDLE, BUFFERING, PLAYING, PAUSED, STOPPED, ERROR }

@Serializable
enum class RepeatMode { OFF, ONE, ALL }

/**
 * A read-only snapshot of the host-owned player, returned when a plugin asks via
 * `IViperHost.request(host.playerState)`. Plugins observe; they do not own transport.
 */
@Serializable
data class PlayerState(
    val state: PlaybackState = PlaybackState.IDLE,
    val currentMediaId: String? = null,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queueSize: Int = 0,
    val queuePosition: Int = 0,
)
