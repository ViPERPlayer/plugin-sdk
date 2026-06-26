package com.viperplayer.plugin.model

import kotlinx.serialization.Serializable

/** Coarse capability domains a plugin can advertise. The host gates whole feature areas on these. */
@Serializable
enum class Capability { SOURCE, DSP, LYRICS, SCROBBLE, METADATA }

/** Fine-grained declaration of what a SOURCE plugin actually supports, so the host calls only what
 *  exists and shows only relevant UI. New flags default to false/sensible values for old plugins. */
@Serializable
data class SourceCapabilities(
    val search: Boolean = true,
    val searchSuggestions: Boolean = false,
    val browse: Boolean = false,
    val library: Boolean = false,
    val playlists: Boolean = false,
    val artistDetails: Boolean = true,
    val albumDetails: Boolean = true,
    val home: Boolean = false,
    val radio: Boolean = false,
    val canSeek: Boolean = true,
    val supportsOffline: Boolean = false,
)

@Serializable
data class DspCapabilities(
    /** The effects this plugin offers; the host can enable any subset in its chain. */
    val effects: List<DspDescriptor> = emptyList(),
)

@Serializable
data class LyricsCapabilities(
    val synced: Boolean = false,
)

@Serializable
data class ScrobbleCapabilities(
    val nowPlaying: Boolean = true,
)

/**
 * The authoritative description a plugin returns at handshake. Discovery-time `<meta-data>` is only
 * a pre-connect hint; this is the source of truth once connected.
 *
 * Forward/backward compatible by construction: a newer plugin may include capability blocks an
 * older host ignores; an older plugin omits blocks a newer host defaults. The host should only ever
 * call verbs backed by a capability present here.
 */
@Serializable
data class PluginManifest(
    val id: String,
    val name: String,
    val version: String,
    /** Protocol version the plugin speaks; the host uses the lower of the two. */
    val protocolVersion: Int,
    val codec: String,
    val capabilities: Set<Capability> = emptySet(),
    val source: SourceCapabilities? = null,
    val dsp: DspCapabilities? = null,
    val lyrics: LyricsCapabilities? = null,
    val scrobble: ScrobbleCapabilities? = null,
    /** Fully-qualified Activity class for in-app settings, or null. */
    val settingsActivity: String? = null,
    val author: String? = null,
    val description: String? = null,
)

/** The host's side of the handshake, sent in `initialize`. */
@Serializable
data class HostHandshake(
    val protocolVersion: Int,
    val hostVersion: String,
    /** Optional opt-in host feature flags a plugin can adapt to. */
    val features: Set<String> = emptySet(),
)
