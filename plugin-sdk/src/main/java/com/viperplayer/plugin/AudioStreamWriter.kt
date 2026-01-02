package com.viperplayer.plugin

import android.os.ParcelFileDescriptor
import android.util.Log
import com.viperplayer.plugin.v1.AudioFormat
import com.viperplayer.plugin.v1.AudioStream
import java.io.Closeable
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Helper class for plugins to create and manage audio streams.
 * Handles the creation of a pipe and provides methods to write PCM data.
 * 
 * Usage:
 * ```kotlin
 * val writer = AudioStreamWriter.create(mediaId, format, durationMs)
 * val stream = writer.audioStream // Return this to the host
 * 
 * // In a background thread:
 * try {
 *     while (hasMoreData && !writer.isClosed) {
 *         val pcmData = decodeSomePcm()
 *         writer.write(pcmData)
 *     }
 * } finally {
 *     writer.close()
 * }
 * ```
 */
class AudioStreamWriter private constructor(
    val streamId: String,
    val mediaId: String,
    val format: AudioFormat,
    val durationMs: Long,
    val canSeek: Boolean,
    private val readPipe: ParcelFileDescriptor,
    private val writePipe: ParcelFileDescriptor
) : Closeable {
    companion object {
        private const val TAG = "AudioStreamWriter"
        
        /**
         * Create a new audio stream writer.
         * 
         * @param mediaId The song being streamed
         * @param format Audio format of the PCM data
         * @param durationMs Total duration in milliseconds
         * @param canSeek Whether seeking is supported
         * @return A new AudioStreamWriter
         */
        fun create(
            mediaId: String,
            format: AudioFormat,
            durationMs: Long,
            canSeek: Boolean = true
        ): AudioStreamWriter {
            val streamId = UUID.randomUUID().toString()
            Log.d(TAG, "Creating audio stream: streamId=$streamId, mediaId=$mediaId, format=$format, duration=${durationMs}ms, canSeek=$canSeek")
            val pipes = ParcelFileDescriptor.createPipe()
            
            val writer = AudioStreamWriter(
                streamId = streamId,
                mediaId = mediaId,
                format = format,
                durationMs = durationMs,
                canSeek = canSeek,
                readPipe = pipes[0],  // Host reads from this
                writePipe = pipes[1]  // Plugin writes to this
            )
            Log.d(TAG, "Audio stream created successfully: streamId=$streamId")
            return writer
        }
    }
    
    private val outputStream = FileOutputStream(writePipe.fileDescriptor)
    private val closed = AtomicBoolean(false)
    
    /**
     * The AudioStream to return to the host.
     * The host will read PCM data from this stream's pipe.
     */
    val audioStream: AudioStream
        get() = AudioStream(
            streamId = streamId,
            mediaId = mediaId,
            format = format,
            durationMs = durationMs,
            pipe = readPipe,
            canSeek = canSeek
        )
    
    /**
     * Whether this stream has been closed.
     */
    val isClosed: Boolean
        get() = closed.get()
    
    /**
     * Write PCM data to the stream.
     * @param data PCM audio data
     * @throws IOException if the stream is closed or write fails
     */
    @Throws(IOException::class)
    fun write(data: ByteArray) {
        if (closed.get()) {
            Log.w(TAG, "Attempted to write to closed stream: streamId=$streamId")
            throw IOException("Stream is closed")
        }
        Log.v(TAG, "Writing ${data.size} bytes to stream: streamId=$streamId")
        outputStream.write(data)
    }
    
    /**
     * Write PCM data to the stream.
     * @param data PCM audio data
     * @param offset Starting offset in the array
     * @param length Number of bytes to write
     * @throws IOException if the stream is closed or write fails
     */
    @Throws(IOException::class)
    fun write(data: ByteArray, offset: Int, length: Int) {
        if (closed.get()) {
            Log.w(TAG, "Attempted to write to closed stream: streamId=$streamId")
            throw IOException("Stream is closed")
        }
        Log.v(TAG, "Writing $length bytes (offset=$offset) to stream: streamId=$streamId")
        outputStream.write(data, offset, length)
    }
    
    /**
     * Flush the output stream.
     */
    @Throws(IOException::class)
    fun flush() {
        if (!closed.get()) {
            Log.v(TAG, "Flushing stream: streamId=$streamId")
            outputStream.flush()
        } else {
            Log.w(TAG, "Attempted to flush closed stream: streamId=$streamId")
        }
    }
    
    /**
     * Close the stream and release resources.
     */
    override fun close() {
        if (closed.compareAndSet(false, true)) {
            Log.d(TAG, "Closing stream: streamId=$streamId")
            try {
                outputStream.close()
                Log.d(TAG, "Output stream closed: streamId=$streamId")
            } catch (e: IOException) {
                Log.w(TAG, "Error closing output stream: streamId=$streamId", e)
            }
            try {
                writePipe.close()
                Log.d(TAG, "Write pipe closed: streamId=$streamId")
            } catch (e: IOException) {
                Log.w(TAG, "Error closing write pipe: streamId=$streamId", e)
            }
            // Note: readPipe is owned by the host and should be closed by them
            Log.d(TAG, "Stream closed: streamId=$streamId")
        } else {
            Log.d(TAG, "Stream already closed: streamId=$streamId")
        }
    }
}

