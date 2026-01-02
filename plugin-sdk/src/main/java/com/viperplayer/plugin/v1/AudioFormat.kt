package com.viperplayer.plugin.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Describes the PCM audio format for streaming.
 * 
 * @property sampleRate Sample rate in Hz (e.g., 44100, 48000)
 * @property channelCount Number of audio channels (1 = mono, 2 = stereo)
 * @property encoding PCM encoding type
 * @property bitDepth Bits per sample (16, 24, or 32)
 */
@Parcelize
data class AudioFormat(
    val sampleRate: Int = 44100,
    val channelCount: Int = 2,
    val encoding: PcmEncoding = PcmEncoding.PCM_16BIT,
    val bitDepth: Int = 16
) : Parcelable {
    /** Bytes per sample (all channels) */
    val bytesPerFrame: Int
        get() = channelCount * (bitDepth / 8)
    
    /** Bytes per second of audio */
    val bytesPerSecond: Int
        get() = sampleRate * bytesPerFrame
    
    companion object {
        /** Standard CD quality */
        val CD_QUALITY = AudioFormat(44100, 2, PcmEncoding.PCM_16BIT, 16)
        
        /** High-res audio */
        val HIGH_RES = AudioFormat(96000, 2, PcmEncoding.PCM_24BIT, 24)
    }
}

/**
 * PCM encoding types.
 */
enum class PcmEncoding {
    /** 16-bit signed integer PCM */
    PCM_16BIT,
    /** 24-bit signed integer PCM (packed) */
    PCM_24BIT,
    /** 32-bit floating point PCM */
    PCM_FLOAT
}
