// IViperHost.aidl
//
//  FROZEN WIRE ABI — DO NOT CHANGE. See IViperPlugin.aidl for the rules.
//
//  The back-channel a plugin uses to talk to the host. The host owns the player; plugins do not
//  drive transport directly. Instead they emit events (something changed, a scrobble, an auth
//  state change) and may query host-owned state (player state, settings) — all through the same
//  verb + Bundle mechanism as the forward channel, so this interface is equally frozen.
package com.viperplayer.plugin.ipc;

import com.viperplayer.plugin.ipc.IResultCallback;

interface IViperHost {

    /**
     * Fire-and-forget notification from plugin to host, distinguished by {@code verb}
     * (e.g. content changed, metadata updated, authentication state changed, error report).
     */
    oneway void emitEvent(String verb, in Bundle data) = 1;

    /**
     * Plugin -> host request that expects a reply (e.g. read current player state). Asynchronous:
     * the reply arrives on {@code callback}. The host decides what a plugin is allowed to ask for.
     */
    oneway void request(String verb, long requestId, in Bundle args, IResultCallback callback) = 2;
}
