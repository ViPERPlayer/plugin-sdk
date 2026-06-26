package com.viperplayer.plugin.protocol

import android.os.Bundle
import com.viperplayer.plugin.ipc.IResultCallback
import com.viperplayer.plugin.model.PluginErrorBody
import com.viperplayer.plugin.model.PluginException
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Terminal result of a remote call: the completion Bundle plus any pages streamed before it. */
class CallResult internal constructor(
    val result: Bundle,
    val pages: List<Bundle>,
)

/**
 * Bridges the frozen callback transport to coroutines for whichever side is *issuing* a call (the
 * host calling a plugin's `invoke`, or a plugin calling the host's `request`).
 *
 * Issues the call, suspends until the single terminal callback, accumulates any `onPage` pages,
 * maps `onError` to a typed [PluginException], and forwards coroutine cancellation back across the
 * binder via [onCancel] so in-flight work is actually stopped.
 */
internal suspend fun awaitCall(
    requestId: Long,
    onCancel: (Long) -> Unit,
    issue: (IResultCallback) -> Unit,
): CallResult = suspendCancellableCoroutine { cont ->
    val pages = ArrayList<Bundle>()
    val done = AtomicBoolean(false) // exactly one terminal resume, even if a plugin misbehaves
    val callback = object : IResultCallback.Stub() {
        override fun onPage(page: Bundle) {
            synchronized(pages) { pages.add(page) }
        }

        override fun onComplete(result: Bundle) {
            if (done.compareAndSet(false, true) && cont.isActive) {
                cont.resume(CallResult(result, synchronized(pages) { pages.toList() }))
            }
        }

        override fun onError(code: Int, message: String?, extras: Bundle?) {
            if (!done.compareAndSet(false, true)) return
            // Decode defensively: a corrupt extras Bundle must not throw on the binder thread and
            // leave the caller hung.
            val body = runCatching { extras?.let { Envelope.payloadOrNull<PluginErrorBody>(it) } }.getOrNull()
            if (cont.isActive) cont.resumeWithException(
                if (body != null) PluginException.from(body) else PluginException(code, message)
            )
        }
    }
    cont.invokeOnCancellation { runCatching { onCancel(requestId) } }
    try {
        issue(callback)
    } catch (t: Throwable) {
        if (done.compareAndSet(false, true) && cont.isActive) cont.resumeWithException(t)
    }
}

/** Monotonic request-id source, one per connection/side. */
internal class RequestIds {
    private val next = AtomicLong(1)
    fun next(): Long = next.getAndIncrement()
}
