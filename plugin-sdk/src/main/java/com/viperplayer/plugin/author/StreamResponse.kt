package com.viperplayer.plugin.author

import android.os.ParcelFileDescriptor
import com.viperplayer.plugin.model.DashStream
import com.viperplayer.plugin.model.DrmConfig
import com.viperplayer.plugin.model.HlsStream
import com.viperplayer.plugin.model.PcmStream
import com.viperplayer.plugin.model.StreamSource
import com.viperplayer.plugin.model.UrlStream

/**
 * What [SourceProvider.resolveStream] returns: the serializable [StreamSource] metadata plus, for
 * PCM, the live read FD the host will pull audio from. The SDK splits these into the result Bundle
 * (metadata as payload, FD as a handle) so raw audio never crosses the binder buffer.
 *
 * Build with the factories rather than constructing directly.
 */
class StreamResponse private constructor(
    val source: StreamSource,
    val fd: ParcelFileDescriptor?,
) {
    companion object {
        fun url(
            url: String,
            mimeType: String? = null,
            headers: Map<String, String> = emptyMap(),
            replayGainDb: Float? = null,
            peakAmplitude: Float? = null,
            albumReplayGainDb: Float? = null,
            albumPeakAmplitude: Float? = null,
        ): StreamResponse = StreamResponse(
            UrlStream(url, mimeType, headers, replayGainDb, peakAmplitude, albumReplayGainDb, albumPeakAmplitude), null
        )

        fun dash(
            manifest: String? = null,
            manifestUrl: String? = null,
            replayGainDb: Float? = null,
            peakAmplitude: Float? = null,
            albumReplayGainDb: Float? = null,
            albumPeakAmplitude: Float? = null,
        ): StreamResponse = StreamResponse(
            DashStream(manifest, manifestUrl, replayGainDb, peakAmplitude, albumReplayGainDb, albumPeakAmplitude), null
        )

        fun hls(
            url: String,
            drm: DrmConfig? = null,
            replayGainDb: Float? = null,
            peakAmplitude: Float? = null,
            albumReplayGainDb: Float? = null,
            albumPeakAmplitude: Float? = null,
        ): StreamResponse = StreamResponse(HlsStream(url, drm, replayGainDb, peakAmplitude, albumReplayGainDb, albumPeakAmplitude), null)

        /** Stream raw PCM from a writer. The writer's read FD is handed to the host automatically. */
        fun pcm(
            writer: AudioStreamWriter,
            replayGainDb: Float? = null,
            peakAmplitude: Float? = null,
            albumReplayGainDb: Float? = null,
            albumPeakAmplitude: Float? = null,
        ): StreamResponse = StreamResponse(
            PcmStream(
                streamId = writer.streamId,
                format = writer.format,
                durationMs = writer.durationMs,
                seekable = writer.seekable,
                replayGainDb = replayGainDb,
                peakAmplitude = peakAmplitude,
                albumReplayGainDb = albumReplayGainDb,
                albumPeakAmplitude = albumPeakAmplitude,
            ),
            writer.hostFd,
        )
    }
}
