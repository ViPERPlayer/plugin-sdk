package com.viperplayer.plugin.author

import android.os.ParcelFileDescriptor
import com.viperplayer.plugin.model.AudioFormat
import java.io.Closeable
import java.io.IOException
import java.io.OutputStream
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Helper for plugins that produce raw PCM. Creates a *reliable* pipe (so the host can tell a clean
 * end-of-stream from a writer that died), hands the read end to the host inside the stream's
 * Bundle, and lets you write decoded PCM to the write end from a background coroutine.
 *
 * The bytes flow through the kernel pipe, never the binder buffer, so stream size is unbounded.
 *
 * ```
 * override suspend fun resolveStream(songId: String): StreamResponse {
 *     val writer = AudioStreamWriter.create(format, durationMs, seekable = true)
 *     scope.launch { try { decodeInto(writer) } finally { writer.close() } }
 *     return StreamResponse.pcm(writer)
 * }
 * ```
 */
class AudioStreamWriter private constructor(
    val streamId: String,
    val format: AudioFormat,
    val durationMs: Long?,
    val seekable: Boolean,
    private val readEnd: ParcelFileDescriptor,
    private val writeEnd: ParcelFileDescriptor,
) : Closeable {

    private val output: OutputStream = ParcelFileDescriptor.AutoCloseOutputStream(writeEnd)
    private val closed = AtomicBoolean(false)

    /** The FD handed to the host (read side of the pipe). Used internally by [StreamResponse]. */
    internal val hostFd: ParcelFileDescriptor get() = readEnd

    val isClosed: Boolean get() = closed.get()

    @Throws(IOException::class)
    fun write(data: ByteArray, offset: Int = 0, length: Int = data.size) {
        if (closed.get()) throw IOException("Stream $streamId is closed")
        output.write(data, offset, length)
    }

    @Throws(IOException::class)
    fun flush() {
        if (!closed.get()) output.flush()
    }

    /**
     * Close the write side. On a reliable pipe, closing normally signals clean EOF to the host;
     * call [closeWithError] instead to surface a failure.
     */
    override fun close() {
        if (closed.compareAndSet(false, true)) {
            runCatching { output.close() }
        }
    }

    /** Close the write side reporting an error, which the host observes when it finishes reading. */
    fun closeWithError(message: String) {
        if (closed.compareAndSet(false, true)) {
            runCatching { output.flush() }
            runCatching { writeEnd.closeWithError(message) }
            runCatching { output.close() }
        }
    }

    companion object {
        fun create(
            format: AudioFormat,
            durationMs: Long? = null,
            seekable: Boolean = false,
            streamId: String = UUID.randomUUID().toString(),
        ): AudioStreamWriter {
            val pipe = ParcelFileDescriptor.createReliablePipe()
            return AudioStreamWriter(
                streamId = streamId,
                format = format,
                durationMs = durationMs,
                seekable = seekable,
                readEnd = pipe[0],
                writeEnd = pipe[1],
            )
        }
    }
}
