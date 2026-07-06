package com.viperplayer.plugin.author

import com.viperplayer.plugin.model.Capability
import com.viperplayer.plugin.model.DspCapabilities
import com.viperplayer.plugin.model.LyricsCapabilities
import com.viperplayer.plugin.model.PluginManifest
import com.viperplayer.plugin.model.PluginStatus
import com.viperplayer.plugin.model.ScrobbleCapabilities
import com.viperplayer.plugin.model.SourceCapabilities
import com.viperplayer.plugin.protocol.Protocol

/**
 * What a plugin is: its identity plus the providers it offers. Build one in
 * [ViperPluginService.onCreatePlugin] via [pluginRegistration]. The SDK derives the advertised
 * manifest (capabilities + verbs) from which providers you register, so the host only ever calls
 * what you actually implement.
 */
class PluginRegistration internal constructor(
    val id: String,
    val name: String,
    val version: String,
    val author: String?,
    val description: String?,
    val settingsActivity: String?,
    val source: SourceProvider?,
    val sourceCapabilities: SourceCapabilities?,
    val dsp: DspProvider?,
    val lyrics: LyricsProvider?,
    val lyricsCapabilities: LyricsCapabilities?,
    val scrobble: ScrobbleSink?,
    val scrobbleCapabilities: ScrobbleCapabilities?,
    val metadata: MetadataProvider?,
    internal val status: (suspend () -> PluginStatus)?,
    internal val onConnect: (suspend (PluginHost) -> Unit)?,
    internal val onShutdown: (() -> Unit)?,
) {
    internal fun toManifest(): PluginManifest {
        val caps = buildSet {
            if (source != null) add(Capability.SOURCE)
            if (dsp != null) add(Capability.DSP)
            if (lyrics != null) add(Capability.LYRICS)
            if (scrobble != null) add(Capability.SCROBBLE)
            if (metadata != null) add(Capability.METADATA)
        }
        return PluginManifest(
            id = id,
            name = name,
            version = version,
            protocolVersion = Protocol.VERSION,
            codec = Protocol.CODEC_JSON,
            capabilities = caps,
            source = if (source != null) (sourceCapabilities ?: SourceCapabilities()) else null,
            dsp = if (dsp != null) DspCapabilities(dsp.descriptors) else null,
            lyrics = if (lyrics != null) (lyricsCapabilities ?: LyricsCapabilities()) else null,
            scrobble = if (scrobble != null) (scrobbleCapabilities ?: ScrobbleCapabilities()) else null,
            settingsActivity = settingsActivity,
            author = author,
            description = description,
        )
    }

    class Builder(
        private val id: String,
        private val name: String,
        private val version: String,
    ) {
        private var author: String? = null
        private var description: String? = null
        private var settingsActivity: String? = null
        private var source: SourceProvider? = null
        private var sourceCapabilities: SourceCapabilities? = null
        private var dsp: DspProvider? = null
        private var lyrics: LyricsProvider? = null
        private var lyricsCapabilities: LyricsCapabilities? = null
        private var scrobble: ScrobbleSink? = null
        private var scrobbleCapabilities: ScrobbleCapabilities? = null
        private var metadata: MetadataProvider? = null
        private var status: (suspend () -> PluginStatus)? = null
        private var onConnect: (suspend (PluginHost) -> Unit)? = null
        private var onShutdown: (() -> Unit)? = null

        fun author(value: String?) = apply { author = value }
        fun description(value: String?) = apply { description = value }
        fun settingsActivity(className: String?) = apply { settingsActivity = className }

        fun source(provider: SourceProvider, capabilities: SourceCapabilities = SourceCapabilities()) =
            apply { source = provider; sourceCapabilities = capabilities }

        fun dsp(provider: DspProvider) = apply { dsp = provider }

        fun lyrics(provider: LyricsProvider, capabilities: LyricsCapabilities = LyricsCapabilities()) =
            apply { lyrics = provider; lyricsCapabilities = capabilities }

        fun scrobble(sink: ScrobbleSink, capabilities: ScrobbleCapabilities = ScrobbleCapabilities()) =
            apply { scrobble = sink; scrobbleCapabilities = capabilities }

        fun metadata(provider: MetadataProvider) = apply { metadata = provider }

        /**
         * Reports the plugin's pending user actions ([PluginStatus]) whenever the host asks —
         * at connect, after [PluginHost.notifyStatusChanged], and when the user returns from a
         * resolution step. Keep it fast and side-effect free.
         */
        fun status(provider: suspend () -> PluginStatus) = apply { status = provider }

        /** Called once after the host connects, with a [PluginHost] for talking back to the host. */
        fun onConnect(block: suspend (PluginHost) -> Unit) = apply { onConnect = block }

        /** Called when the host releases the plugin. */
        fun onShutdown(block: () -> Unit) = apply { onShutdown = block }

        fun build(): PluginRegistration = PluginRegistration(
            id, name, version, author, description, settingsActivity,
            source, sourceCapabilities, dsp, lyrics, lyricsCapabilities,
            scrobble, scrobbleCapabilities, metadata, status, onConnect, onShutdown,
        )
    }
}

/** DSL entry point for [PluginRegistration]. */
fun pluginRegistration(
    id: String,
    name: String,
    version: String,
    block: PluginRegistration.Builder.() -> Unit,
): PluginRegistration = PluginRegistration.Builder(id, name, version).apply(block).build()
