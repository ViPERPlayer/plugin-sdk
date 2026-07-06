package com.viperplayer.plugin.host

import com.viperplayer.plugin.model.PlayerState

/**
 * The host implements this to answer the back-channel calls a plugin makes (`IViperHost`). The host
 * owns the player, so plugins can only *read* state and *notify* — never command transport.
 */
interface HostBridge {

    /** Current player state, returned when a plugin asks for it. */
    suspend fun getPlayerState(): PlayerState = PlayerState()

    /** A plugin reports its content changed; the host should refresh anything cached from it. */
    fun onContentChanged(pluginId: String) {}

    /** A plugin reports metadata for [mediaId] changed. */
    fun onMetadataUpdated(pluginId: String, mediaId: String) {}

    /** A plugin reports its authentication state changed (e.g. signed in/out). */
    fun onAuthStateChanged(pluginId: String) {}

    /** A plugin reports its required-action status changed; the host should re-query its status. */
    fun onStatusChanged(pluginId: String) {}

    /** A plugin surfaced an error for display. */
    fun onError(pluginId: String, code: Int, message: String?) {}
}
