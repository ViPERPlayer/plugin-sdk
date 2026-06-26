package com.viperplayer.plugin.host

import android.os.Bundle
import android.os.IBinder
import com.viperplayer.plugin.ipc.IInitCallback
import com.viperplayer.plugin.ipc.IResultCallback
import com.viperplayer.plugin.ipc.IViperHost
import com.viperplayer.plugin.ipc.IViperPlugin
import com.viperplayer.plugin.model.HostHandshake
import com.viperplayer.plugin.model.IdRequest
import com.viperplayer.plugin.model.PluginManifest
import com.viperplayer.plugin.model.PluginErrorBody
import com.viperplayer.plugin.model.PluginErrorCode
import com.viperplayer.plugin.model.PluginException
import com.viperplayer.plugin.protocol.CallResult
import com.viperplayer.plugin.protocol.Envelope
import com.viperplayer.plugin.protocol.HostVerbs
import com.viperplayer.plugin.protocol.Protocol
import com.viperplayer.plugin.protocol.RequestIds
import com.viperplayer.plugin.protocol.awaitCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * A live, handshaken connection to one plugin. Created by [connect] from an already-bound binder
 * (the host keeps its own bind/unbind/retry lifecycle). Exposes typed calls through [client] and
 * routes the plugin's back-channel to the host's [HostBridge].
 */
class PluginConnection private constructor(
    val pluginId: String,
    private val plugin: IViperPlugin,
    private val scope: CoroutineScope,
    private val hostBridge: HostBridge,
) {
    private val requestIds = RequestIds()

    /** The authoritative manifest returned at handshake — capabilities, settings activity, etc. */
    lateinit var manifest: PluginManifest
        private set

    /** Typed entry point for everything the host calls on this plugin. */
    val client: PluginClient by lazy { PluginClient(this, manifest) }

    /** Issue a verb and await its single result. Internal — facades wrap this with typed payloads. */
    internal suspend fun invokeRaw(verb: String, args: Bundle): CallResult {
        val requestId = requestIds.next()
        return awaitCall(
            requestId = requestId,
            onCancel = { id -> runCatching { plugin.cancel(id) } },
        ) { callback ->
            plugin.invoke(verb, requestId, args, callback)
        }
    }

    /** Release the plugin. Best-effort; safe to call once. */
    fun shutdown() {
        runCatching { plugin.shutdown() }
    }

    private val hostStub = object : IViperHost.Stub() {
        override fun emitEvent(verb: String?, data: Bundle?) {
            when (verb) {
                HostVerbs.Event.CONTENT_CHANGED -> hostBridge.onContentChanged(pluginId)
                HostVerbs.Event.METADATA_UPDATED -> {
                    val id = data?.let { Envelope.payloadOrNull<IdRequest>(it) }?.id ?: return
                    hostBridge.onMetadataUpdated(pluginId, id)
                }
                HostVerbs.Event.AUTH_STATE_CHANGED -> hostBridge.onAuthStateChanged(pluginId)
                HostVerbs.Event.ERROR -> {
                    val body = data?.let { Envelope.payloadOrNull<PluginErrorBody>(it) }
                    hostBridge.onError(pluginId, body?.code ?: PluginErrorCode.UNKNOWN, body?.message)
                }
            }
        }

        override fun request(verb: String?, requestId: Long, args: Bundle?, callback: IResultCallback?) {
            if (callback == null) return
            scope.launch {
                try {
                    when (verb) {
                        HostVerbs.PLAYER_STATE ->
                            callback.onComplete(Envelope.of(hostBridge.getPlayerState()))
                        else ->
                            callback.onError(PluginErrorCode.UNSUPPORTED, "Unknown host verb: $verb", null)
                    }
                } catch (e: Throwable) {
                    callback.onError(PluginErrorCode.INTERNAL, e.message, null)
                }
            }
        }
    }

    private suspend fun handshake(hostHandshake: HostHandshake): PluginManifest =
        suspendCancellableCoroutine { cont ->
            val callback = object : IInitCallback.Stub() {
                override fun onInitialized(manifestBundle: Bundle?) {
                    if (!cont.isActive) return
                    try {
                        cont.resume(Envelope.payload<PluginManifest>(manifestBundle!!))
                    } catch (e: Throwable) {
                        cont.resumeWithException(e)
                    }
                }

                override fun onInitializationFailed(code: Int, message: String?) {
                    if (cont.isActive) cont.resumeWithException(PluginException(code, message))
                }
            }
            try {
                plugin.initialize(Envelope.of(hostHandshake), hostStub, callback)
            } catch (e: Throwable) {
                if (cont.isActive) cont.resumeWithException(e)
            }
        }

    companion object {
        private const val HANDSHAKE_TIMEOUT_MS = 10_000L

        /**
         * Wrap a bound [binder] and perform the handshake. Returns a ready connection or throws
         * [PluginException] if the plugin rejects the host or the handshake fails.
         */
        suspend fun connect(
            pluginId: String,
            binder: IBinder,
            hostBridge: HostBridge,
            scope: CoroutineScope,
            hostVersion: String,
            hostFeatures: Set<String> = emptySet(),
        ): PluginConnection {
            val plugin = IViperPlugin.Stub.asInterface(binder)
                ?: throw PluginException(PluginErrorCode.INTERNAL, "Binder is not an IViperPlugin")
            val connection = PluginConnection(pluginId, plugin, scope, hostBridge)
            // A silent plugin (initialize returns but never invokes the init callback) must not hang
            // the connection forever; time out so the binding can be cleaned up.
            connection.manifest = withTimeout(HANDSHAKE_TIMEOUT_MS) {
                connection.handshake(HostHandshake(Protocol.VERSION, hostVersion, hostFeatures))
            }
            // The codec is part of the negotiated contract; if the plugin speaks one this host can't
            // decode, fail cleanly instead of throwing opaque deserialization errors on every call.
            if (connection.manifest.codec != Protocol.CODEC_JSON) {
                throw PluginException(
                    PluginErrorCode.INCOMPATIBLE_HOST,
                    "Plugin uses unsupported codec '${connection.manifest.codec}'",
                )
            }
            return connection
        }
    }
}
