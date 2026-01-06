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
