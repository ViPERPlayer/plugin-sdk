package com.viperplayer.plugin.protocol

import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.SharedMemory
import androidx.annotation.RequiresApi
import androidx.core.os.BundleCompat

/**
 * Builds and reads the Bundles carried by `IViperPlugin.invoke` and the result callbacks.
 *
 * The rule enforced here: a Bundle carries verbs, small fields, the codec-encoded payload (bytes),
 * and *handles* to live resources (FDs / shared memory / binders) — never bulk data. Large lists
 * page via cursor (inside the payload); audio/PCM/DSP ride the FD / shared-memory slots.
 */
object Envelope {

    fun empty(): Bundle = Bundle()

    /** Build an args/result Bundle from a typed payload plus optional live resources. */
    inline fun <reified T> of(
        payload: T,
        fd: ParcelFileDescriptor? = null,
        fd2: ParcelFileDescriptor? = null,
        token: IBinder? = null,
    ): Bundle = Bundle().apply {
        putByteArray(Protocol.KEY_PAYLOAD, Codec.encode(payload))
        if (fd != null) putParcelable(Protocol.KEY_FD, fd)
        if (fd2 != null) putParcelable(Protocol.KEY_FD2, fd2)
        if (token != null) putBinder(Protocol.KEY_TOKEN, token)
    }

    /** A Bundle that carries only live resources (no structured payload). */
    fun ofResources(
        fd: ParcelFileDescriptor? = null,
        fd2: ParcelFileDescriptor? = null,
        token: IBinder? = null,
    ): Bundle = Bundle().apply {
        if (fd != null) putParcelable(Protocol.KEY_FD, fd)
        if (fd2 != null) putParcelable(Protocol.KEY_FD2, fd2)
        if (token != null) putBinder(Protocol.KEY_TOKEN, token)
    }

    /** Decode the required typed payload, or throw if absent. */
    inline fun <reified T> payload(bundle: Bundle): T {
        val bytes = bundle.getByteArray(Protocol.KEY_PAYLOAD)
            ?: error("Envelope is missing a payload")
        return Codec.decode(bytes)
    }

    /** Decode the typed payload if present, otherwise null (for no-arg verbs). */
    inline fun <reified T> payloadOrNull(bundle: Bundle): T? {
        val bytes = bundle.getByteArray(Protocol.KEY_PAYLOAD) ?: return null
        return Codec.decode(bytes)
    }

    fun fd(bundle: Bundle): ParcelFileDescriptor? =
        BundleCompat.getParcelable(bundle, Protocol.KEY_FD, ParcelFileDescriptor::class.java)

    fun fd2(bundle: Bundle): ParcelFileDescriptor? =
        BundleCompat.getParcelable(bundle, Protocol.KEY_FD2, ParcelFileDescriptor::class.java)

    fun token(bundle: Bundle): IBinder? = bundle.getBinder(Protocol.KEY_TOKEN)

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    fun sharedMemory(bundle: Bundle): SharedMemory? =
        BundleCompat.getParcelable(bundle, Protocol.KEY_SHM, SharedMemory::class.java)

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    fun sharedMemory2(bundle: Bundle): SharedMemory? =
        BundleCompat.getParcelable(bundle, Protocol.KEY_SHM2, SharedMemory::class.java)
}
