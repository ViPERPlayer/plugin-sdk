package com.viperplayer.plugin.author

import com.viperplayer.plugin.ipc.IViperHost
import com.viperplayer.plugin.model.HostHandshake
import com.viperplayer.plugin.model.IdRequest
import com.viperplayer.plugin.model.PlayerState
import com.viperplayer.plugin.model.PluginErrorBody
import com.viperplayer.plugin.protocol.Envelope
import com.viperplayer.plugin.protocol.HostVerbs
import com.viperplayer.plugin.protocol.RequestIds
import com.viperplayer.plugin.protocol.awaitCall

/**
 * The host, as seen by a plugin. Replaces raw binder access with typed calls. The host owns the
 * player, so a plugin observes state and emits notifications rather than driving transport.
 *
 * Provided to your code via [PluginRegistration.Builder.onConnect].
 */
class PluginHost internal constructor(
    private val host: IViperHost,
    hostHandshake: HostHandshake,
) {
    private val requestIds = RequestIds()

    /** Protocol version the host speaks; compare against [com.viperplayer.plugin.protocol.Protocol.VERSION]. */
    val hostProtocolVersion: Int = hostHandshake.protocolVersion

    /** The host app's version string. */
    val hostVersion: String = hostHandshake.hostVersion

    /** Opt-in host feature flags this plugin can adapt to. */
    val hostFeatures: Set<String> = hostHandshake.features

    /** Read the current host-owned player state. */
    suspend fun getPlayerState(): PlayerState {
        val requestId = requestIds.next()
        val result = awaitCall(requestId, onCancel = { /* host requests are not cancellable */ }) { cb ->
            host.request(HostVerbs.PLAYER_STATE, requestId, Envelope.empty(), cb)
        }
        return Envelope.payload(result.result)
    }

    /** Tell the host this plugin's content changed and cached data should be refreshed. */
    fun notifyContentChanged() {
        host.emitEvent(HostVerbs.Event.CONTENT_CHANGED, Envelope.empty())
    }

    /** Tell the host that metadata for a specific item changed. */
    fun notifyMetadataUpdated(mediaId: String) {
        host.emitEvent(HostVerbs.Event.METADATA_UPDATED, Envelope.of(IdRequest(mediaId)))
    }

    /** Tell the host that authentication state changed (e.g. signed in/out). */
    fun notifyAuthStateChanged() {
        host.emitEvent(HostVerbs.Event.AUTH_STATE_CHANGED, Envelope.empty())
    }

    /**
     * Tell the host this plugin's required-action status changed (permission granted, login
     * expired, verification needed...). The host re-queries the registration's status provider.
     */
    fun notifyStatusChanged() {
        host.emitEvent(HostVerbs.Event.STATUS_CHANGED, Envelope.empty())
    }

    /** Surface an error to the host for display. */
    fun reportError(code: Int, message: String?) {
        host.emitEvent(HostVerbs.Event.ERROR, Envelope.of(PluginErrorBody(code, message)))
    }
}
