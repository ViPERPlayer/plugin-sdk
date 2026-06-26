package com.viperplayer.plugin.author

import com.viperplayer.plugin.model.DspAudioFormat
import com.viperplayer.plugin.model.DspDescriptor

/**
 * An interleaved block of 32-bit float PCM the host hands to a [DspSession] for in-place
 * processing. Layout is `[f0c0, f0c1, f1c0, f1c1, …]`; valid length is `frames * channels`.
 */
class AudioBuffer internal constructor(
    val channels: Int,
    /** The reusable backing array. Only the first `frames * channels` entries are valid. */
    val samples: FloatArray,
) {
    var frames: Int = 0
        internal set
}

/**
 * A live, host-owned processing session running in the plugin's process. The host streams audio
 * blocks to it over shared memory (set up by the SDK); you just transform samples in place.
 *
 * Because this runs out-of-process, it cannot sit inside the real-time audio callback — the host
 * buffers ahead, so favour throughput and never block indefinitely inside [process].
 */
abstract class DspSession {

    /** Push new parameter values / enabled state. Called off the audio block thread. */
    open fun configure(params: Map<String, Float>, enabled: Boolean) {}

    /** Transform [buffer] in place. Called once per block on a dedicated worker thread. */
    abstract fun process(buffer: AudioBuffer)

    /** Release any resources. The shared memory and threads are torn down by the SDK. */
    open fun close() {}
}

/**
 * Implemented by a plugin that processes the audio path. Declare the effects you offer in
 * [descriptors]; the host enables any subset and opens a [DspSession] per active stream.
 */
interface DspProvider {
    val descriptors: List<DspDescriptor>

    fun createSession(format: DspAudioFormat, maxFramesPerBlock: Int): DspSession
}
