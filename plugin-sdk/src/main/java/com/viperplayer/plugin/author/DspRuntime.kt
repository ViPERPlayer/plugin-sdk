package com.viperplayer.plugin.author

import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.SharedMemory
import androidx.annotation.RequiresApi
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Plugin-side runtime for one open DSP session. Audio is exchanged through two host-allocated
 * shared-memory regions (input/output, mmap'd here) with a tiny doorbell protocol over a
 * socketpair: the host writes a block of frames and sends the frame count; this worker processes
 * the block in place and echoes the count back. No bulk audio ever touches the binder.
 *
 * The 4-byte control word is the number of frames to process, or a negative value to stop.
 */
@RequiresApi(Build.VERSION_CODES.O_MR1)
internal class DspRuntime(
    private val session: DspSession,
    private val channels: Int,
    maxFramesPerBlock: Int,
    private val inputShm: SharedMemory,
    private val outputShm: SharedMemory,
    private val control: ParcelFileDescriptor,
) {
    private val inputMapped: ByteBuffer = inputShm.mapReadWrite().order(ByteOrder.nativeOrder())
    private val outputMapped: ByteBuffer = outputShm.mapReadWrite().order(ByteOrder.nativeOrder())
    private val inputBuffer: FloatBuffer = inputMapped.asFloatBuffer()
    private val outputBuffer: FloatBuffer = outputMapped.asFloatBuffer()

    private val audioBuffer = AudioBuffer(channels, FloatArray(maxFramesPerBlock * channels))

    private val controlIn = DataInputStream(FileInputStream(control.fileDescriptor))
    private val controlOut = DataOutputStream(FileOutputStream(control.fileDescriptor))

    @Volatile
    private var running = true

    private val thread = Thread({ loop() }, "viper-dsp-worker").apply {
        priority = Thread.MAX_PRIORITY
        isDaemon = true
    }

    fun start() = thread.start()

    fun configure(params: Map<String, Float>, enabled: Boolean) = session.configure(params, enabled)

    private fun loop() {
        try {
            while (running) {
                val frames = controlIn.readInt() // blocks until the host rings the doorbell
                if (frames < 0) break

                val count = frames * channels
                if (count > audioBuffer.samples.size) break // more than negotiated; bail before overflow
                inputBuffer.position(0)
                inputBuffer.get(audioBuffer.samples, 0, count)
                audioBuffer.frames = frames

                session.process(audioBuffer)

                outputBuffer.position(0)
                outputBuffer.put(audioBuffer.samples, 0, count)
                controlOut.writeInt(frames) // ack: output is ready
                controlOut.flush()
            }
        } catch (_: Throwable) {
            // Pipe closed or read failed — the host went away; fall through to cleanup.
        } finally {
            cleanup()
        }
    }

    fun stop() {
        running = false
        runCatching { control.close() } // unblocks the worker's readInt()
    }

    private fun cleanup() {
        runCatching { session.close() }
        runCatching { SharedMemory.unmap(inputMapped) }
        runCatching { SharedMemory.unmap(outputMapped) }
        runCatching { inputShm.close() }
        runCatching { outputShm.close() }
        // controlIn/controlOut wrap control's fd but don't own it; close the PFD once instead of
        // closing each stream (which would double/triple-close the same fd).
        runCatching { control.close() }
    }
}
