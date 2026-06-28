package com.viperplayer.plugin.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PcmEncoding { PCM_16BIT, PCM_24BIT, PCM_FLOAT }

@Serializable
data class AudioFormat(
    val sampleRate: Int = 44100,
    val channelCount: Int = 2,
    val encoding: PcmEncoding = PcmEncoding.PCM_16BIT,
)

/**
 * How a track should be played. The host owns the player; this just tells it where/how to get the
 * audio. Sealed so new delivery methods can be added as new variants.
 *
 * For [PcmStream] the actual data does not live in this object — it flows through a
 * [android.os.ParcelFileDescriptor] carried alongside this metadata in the transaction Bundle, so
 * raw audio never hits the binder limit.
 */
@Serializable
sealed interface StreamSource {
    val replayGainDb: Float?
    val peakAmplitude: Float?
}

/** Fallback for a stream kind a newer plugin added that this host doesn't understand. */
@Serializable
@SerialName("unknown")
data class UnknownStream(
    override val replayGainDb: Float? = null,
    override val peakAmplitude: Float? = null,
) : StreamSource

/** A directly playable progressive/HTTP(S) URL (or `content://`). */
@Serializable
@SerialName("url")
data class UrlStream(
    val url: String,
    val mimeType: String? = null,
    val headers: Map<String, String> = emptyMap(),
    override val replayGainDb: Float? = null,
    override val peakAmplitude: Float? = null,
) : StreamSource

/** A DASH stream, given either inline manifest XML or a manifest URL. */
@Serializable
@SerialName("dash")
data class DashStream(
    val manifest: String? = null,
    val manifestUrl: String? = null,
    override val replayGainDb: Float? = null,
    override val peakAmplitude: Float? = null,
) : StreamSource

/**
 * DRM parameters for an encrypted stream. [scheme] names the DRM system (e.g. "widevine"); the host
 * maps it to the matching content-protection UUID. [licenseUrl] is the license server endpoint the
 * host POSTs key requests to, with [licenseHeaders] attached. Kept generic so other schemes can be
 * added without a wire change.
 */
@Serializable
data class DrmConfig(
    val scheme: String,
    val licenseUrl: String,
    val licenseHeaders: Map<String, String> = emptyMap(),
)

/** An HLS stream URL, optionally protected by [drm] (e.g. Widevine). */
@Serializable
@SerialName("hls")
data class HlsStream(
    val url: String,
    val drm: DrmConfig? = null,
    override val replayGainDb: Float? = null,
    override val peakAmplitude: Float? = null,
) : StreamSource

/**
 * Raw PCM produced by the plugin and streamed to the host through a pipe FD (carried in the
 * Bundle). [streamId] correlates later seek/close calls; [seekable] reports whether the plugin can
 * honour a seek.
 */
@Serializable
@SerialName("pcm")
data class PcmStream(
    val streamId: String,
    val format: AudioFormat,
    val durationMs: Long? = null,
    val seekable: Boolean = false,
    override val replayGainDb: Float? = null,
    override val peakAmplitude: Float? = null,
) : StreamSource
