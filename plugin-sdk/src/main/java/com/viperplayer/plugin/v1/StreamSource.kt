package com.viperplayer.plugin.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Unified stream source that can be a URL, DASH XML, or AudioStream.
 * Uses a type discriminator to determine which variant is valid.
 * 
 * @property replayGainDb Optional ReplayGain value in dB to apply as player volume.
 *                      Some plugins may only know this when requesting the stream.
 *                      Will be converted to linear when applied to player.
 * @property peakAmplitude Optional peak amplitude (0.0-1.0+). Some plugins may only know this when requesting the stream.
 */
@Parcelize
data class StreamSource(
    val type: Type,
    val url: String? = null,
    val dashXml: String? = null,
    val audioStream: AudioStream? = null,
    val replayGainDb: Float? = null, // ReplayGain in dB
    val peakAmplitude: Float? = null // Peak amplitude (0.0-1.0+)
) : Parcelable {
    enum class Type {
        /** Direct URL to audio stream */
        URL,
        /** DASH manifest XML */
        DASH,
        /** PCM audio stream via ParcelFileDescriptor */
        AUDIO_STREAM
    }
    
    companion object {
        fun url(url: String, replayGainDb: Float? = null, peakAmplitude: Float? = null) = StreamSource(Type.URL, url = url, replayGainDb = replayGainDb, peakAmplitude = peakAmplitude)
        fun dash(dashXml: String, replayGainDb: Float? = null, peakAmplitude: Float? = null) = StreamSource(Type.DASH, dashXml = dashXml, replayGainDb = replayGainDb, peakAmplitude = peakAmplitude)
        fun audioStream(audioStream: AudioStream, replayGainDb: Float? = null, peakAmplitude: Float? = null) = StreamSource(Type.AUDIO_STREAM, audioStream = audioStream, replayGainDb = replayGainDb, peakAmplitude = peakAmplitude)
    }
}
