package com.viperplayer.plugin.sdk.v1

import android.os.Parcel
import android.os.Parcelable

/**
 * Describes the PCM audio format for streaming.
 * 
 * @property sampleRate Sample rate in Hz (e.g., 44100, 48000)
 * @property channelCount Number of audio channels (1 = mono, 2 = stereo)
 * @property encoding PCM encoding type
 * @property bitDepth Bits per sample (16, 24, or 32)
 */
data class AudioFormat(
    val sampleRate: Int = 44100,
    val channelCount: Int = 2,
    val encoding: PcmEncoding = PcmEncoding.PCM_16BIT,
    val bitDepth: Int = 16
) : Parcelable {
    
    constructor(parcel: Parcel) : this(
        sampleRate = parcel.readInt(),
        channelCount = parcel.readInt(),
        encoding = PcmEncoding.entries[parcel.readInt()],
        bitDepth = parcel.readInt()
    )
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(sampleRate)
        parcel.writeInt(channelCount)
        parcel.writeInt(encoding.ordinal)
        parcel.writeInt(bitDepth)
    }
    
    override fun describeContents(): Int = 0
    
    /** Bytes per sample (all channels) */
    val bytesPerFrame: Int
        get() = channelCount * (bitDepth / 8)
    
    /** Bytes per second of audio */
    val bytesPerSecond: Int
        get() = sampleRate * bytesPerFrame
    
    companion object CREATOR : Parcelable.Creator<AudioFormat> {
        override fun createFromParcel(parcel: Parcel): AudioFormat = AudioFormat(parcel)
        override fun newArray(size: Int): Array<AudioFormat?> = arrayOfNulls(size)
        
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

