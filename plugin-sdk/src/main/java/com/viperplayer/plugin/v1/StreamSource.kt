package com.viperplayer.plugin.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Unified stream source that can be a URL, DASH XML, or AudioStream.
 * Uses a type discriminator to determine which variant is valid.
 */
@Parcelize
data class StreamSource(
    val type: Type,
    val url: String? = null,
    val dashXml: String? = null,
    val audioStream: AudioStream? = null
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
        fun url(url: String) = StreamSource(Type.URL, url = url)
        fun dash(dashXml: String) = StreamSource(Type.DASH, dashXml = dashXml)
        fun audioStream(audioStream: AudioStream) = StreamSource(Type.AUDIO_STREAM, audioStream = audioStream)
    }
}

/**
 * Kotlin sealed class for type-safe stream source handling.
 * This is the preferred way to handle StreamSource in Kotlin code.
 */
sealed class StreamSourceTypeSealed {
    data class Url(val url: String) : StreamSourceTypeSealed()
    data class Dash(val xml: String) : StreamSourceTypeSealed()
    data class AudioStream(val stream: com.viperplayer.plugin.v1.AudioStream) : StreamSourceTypeSealed()
    
    companion object {
        fun from(streamSource: StreamSource): StreamSourceTypeSealed {
            return when (streamSource.type) {
                StreamSource.Type.URL -> Url(requireNotNull(streamSource.url) { "URL type requires url field" })
                StreamSource.Type.DASH -> Dash(requireNotNull(streamSource.dashXml) { "DASH type requires dashXml field" })
                StreamSource.Type.AUDIO_STREAM -> AudioStream(requireNotNull(streamSource.audioStream) { "AUDIO_STREAM type requires audioStream field" })
            }
        }
    }
}

