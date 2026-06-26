package com.viperplayer.plugin.model

import kotlinx.serialization.Serializable

@Serializable
enum class DspParamType { FLOAT, INT, BOOL, ENUM }

/** A user-tunable DSP parameter the host can render as a control and push values for. */
@Serializable
data class DspParam(
    val key: String,
    val label: String,
    val type: DspParamType = DspParamType.FLOAT,
    val min: Float = 0f,
    val max: Float = 1f,
    val default: Float = 0f,
    /** Labels for [DspParamType.ENUM] values, indexed by the float value cast to int. */
    val options: List<String> = emptyList(),
)

/** Static description of a DSP effect: its identity and the parameters it exposes. */
@Serializable
data class DspDescriptor(
    val id: String,
    val name: String,
    val description: String? = null,
    val params: List<DspParam> = emptyList(),
)

/** PCM format the host will feed the DSP chain. DSP always works in deinterleaved float frames. */
@Serializable
data class DspAudioFormat(
    val sampleRate: Int,
    val channelCount: Int,
)

/**
 * Host -> plugin request to open a processing session. The host proposes a format and the maximum
 * number of frames it will hand over per processing block (so the plugin can size its buffers).
 * The shared-memory regions for audio are passed alongside this payload in the Bundle.
 */
@Serializable
data class DspOpenRequest(
    val format: DspAudioFormat,
    val maxFramesPerBlock: Int,
)

@Serializable
data class DspOpenResponse(
    val sessionId: String,
    /** Frames of latency the effect introduces, for the host to compensate. */
    val latencyFrames: Int = 0,
)

/** Push new parameter values to an open session. */
@Serializable
data class DspConfigure(
    val sessionId: String,
    val params: Map<String, Float> = emptyMap(),
    val enabled: Boolean = true,
)

@Serializable
data class DspClose(val sessionId: String)
