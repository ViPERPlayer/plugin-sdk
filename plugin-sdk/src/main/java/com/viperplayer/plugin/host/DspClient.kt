package com.viperplayer.plugin.host

import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.SharedMemory
import androidx.annotation.RequiresApi
import com.viperplayer.plugin.model.DspAudioFormat
import com.viperplayer.plugin.model.DspCapabilities
import com.viperplayer.plugin.model.DspClose
import com.viperplayer.plugin.model.DspConfigure
import com.viperplayer.plugin.model.DspOpenRequest
import com.viperplayer.plugin.model.DspOpenResponse
import com.viperplayer.plugin.protocol.Codec
import com.viperplayer.plugin.protocol.Envelope
import com.viperplayer.plugin.protocol.Protocol
import com.viperplayer.plugin.protocol.Verbs
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Host-side driver for a plugin's DSP capability. Opening a [DspChannel] allocates the shared-memory
 * regions and the control socketpair, hands dup'd copies to the plugin, and returns a channel the
 * host audio pipeline pushes blocks through. Requires Android 8.1+ (shared memory).
 */
class DspClient internal constructor(
    private val connection: PluginConnection,
    val capabilities: DspCapabilities,
) {
    @RequiresApi(Build.VERSION_CODES.O_MR1)
    suspend fun open(format: DspAudioFormat, maxFramesPerBlock: Int): DspChannel {
        val bytes = maxFramesPerBlock * format.channelCount * Float.SIZE_BYTES
        val inputShm = SharedMemory.create("viper-dsp-in", bytes)
        val outputShm = SharedMemory.create("viper-dsp-out", bytes)
        val sockets = ParcelFileDescriptor.createSocketPair() // [0] host, [1] plugin

        val args = Bundle().apply {
            putByteArray(Protocol.KEY_PAYLOAD, Codec.encode(DspOpenRequest(format, maxFramesPerBlock)))
            putParcelable(Protocol.KEY_SHM, inputShm)
            putParcelable(Protocol.KEY_SHM2, outputShm)
            putParcelable(Protocol.KEY_FD, sockets[1])
        }

        try {
            val result = connection.invokeRaw(Verbs.Dsp.OPEN, args)
            val response = Envelope.payload<DspOpenResponse>(result.result)
            // The plugin end has been dup'd into the plugin process; release our copy.
            sockets[1].close()
            return DspChannel(
                connection = connection,
                sessionId = response.sessionId,
                channels = format.channelCount,
                inputShm = inputShm,
                outputShm = outputShm,
                hostSocket = sockets[0],
                latencyFrames = response.latencyFrames,
            )
        } catch (e: Throwable) {
            runCatching { sockets[0].close() }
            runCatching { sockets[1].close() }
            runCatching { inputShm.close() }
            runCatching { outputShm.close() }
            throw e
        }
    }
}

/**
 * A live DSP session. The host writes a block of interleaved float frames, rings the doorbell, and
 * reads the processed block back — all through shared memory, so no audio crosses the binder.
 *
 * [process] is blocking and meant to be called from the host's buffering/decode thread (never the
 * real-time output callback).
 */
@RequiresApi(Build.VERSION_CODES.O_MR1)
class DspChannel internal constructor(
    private val connection: PluginConnection,
    val sessionId: String,
    private val channels: Int,
    private val inputShm: SharedMemory,
    private val outputShm: SharedMemory,
    private val hostSocket: ParcelFileDescriptor,
    val latencyFrames: Int,
) {
    private val inputMapped: ByteBuffer = inputShm.mapReadWrite().order(ByteOrder.nativeOrder())
    private val outputMapped: ByteBuffer = outputShm.mapReadWrite().order(ByteOrder.nativeOrder())
    private val inputFloat = inputMapped.asFloatBuffer()
    private val outputFloat = outputMapped.asFloatBuffer()

    private val controlIn = DataInputStream(FileInputStream(hostSocket.fileDescriptor))
    private val controlOut = DataOutputStream(FileOutputStream(hostSocket.fileDescriptor))

    @Volatile
    private var closed = false

    /** Process [frames] of interleaved float PCM in [samples] in place. */
    fun process(samples: FloatArray, frames: Int) {
        if (closed) return
        val count = frames * channels
        inputFloat.position(0)
        inputFloat.put(samples, 0, count)
        controlOut.writeInt(frames)
        controlOut.flush()
        controlIn.readInt() // wait for the plugin to finish this block
        outputFloat.position(0)
        outputFloat.get(samples, 0, count)
    }

    /** Push parameter values / enabled state to the plugin. */
    suspend fun configure(params: Map<String, Float>, enabled: Boolean = true) {
        connection.invokeRaw(Verbs.Dsp.CONFIGURE, Envelope.of(DspConfigure(sessionId, params, enabled)))
    }

    /** Tear down the session (signals the plugin worker to stop, then releases local resources). */
    fun close() {
        if (closed) return
        closed = true
        runCatching { controlOut.writeInt(-1); controlOut.flush() }
        runCatching { connection } // session also reclaimed plugin-side when the socket closes
        runCatching { SharedMemory.unmap(inputMapped) }
        runCatching { SharedMemory.unmap(outputMapped) }
        runCatching { controlIn.close() }
        runCatching { controlOut.close() }
        runCatching { hostSocket.close() }
        runCatching { inputShm.close() }
        runCatching { outputShm.close() }
    }

    /** Optional explicit remote close (the socket signal above already reclaims the session). */
    suspend fun closeRemote() {
        runCatching { connection.invokeRaw(Verbs.Dsp.CLOSE, Envelope.of(DspClose(sessionId))) }
    }
}
