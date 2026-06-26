// IViperPlugin.aidl
//
//  ┌──────────────────────────────────────────────────────────────────────────┐
//  │  FROZEN WIRE ABI — DO NOT CHANGE.                                          │
//  │                                                                            │
//  │  Never add, remove, reorder, or change the signature of a method here,     │
//  │  and never change a method's transaction id (= N). The host and every      │
//  │  plugin — built at different times against different SDK versions — must   │
//  │  agree on these transaction codes forever, so that binding ALWAYS          │
//  │  succeeds and no transaction is ever "unknown".                            │
//  │                                                                            │
//  │  New functionality is NOT added here. It is added as new *verbs*, new      │
//  │  Bundle keys, and new capability flags negotiated at handshake. See        │
//  │  com.viperplayer.plugin.protocol.Verbs / Capabilities.                     │
//  └──────────────────────────────────────────────────────────────────────────┘
package com.viperplayer.plugin.ipc;

import com.viperplayer.plugin.ipc.IViperHost;
import com.viperplayer.plugin.ipc.IInitCallback;
import com.viperplayer.plugin.ipc.IResultCallback;

interface IViperPlugin {

    /**
     * Handshake. The host introduces itself (protocol version, host features) and hands the
     * plugin a back-channel binder. The plugin replies — via the callback — with its manifest
     * (id, version, the capabilities it advertises, the protocol version it speaks).
     *
     * Capability negotiation happens entirely in the exchanged Bundles, so this signature is
     * stable no matter how many capabilities are added later.
     *
     * @param hostInfo  Codec-encoded host handshake (see protocol.Handshake).
     * @param host      Back-channel for plugin -> host calls (events, player-state queries).
     * @param callback  Receives the plugin manifest, or an initialization failure.
     */
    void initialize(in Bundle hostInfo, IViperHost host, IInitCallback callback) = 1;

    /**
     * The single, universal RPC. Every operation — search, getAlbum, resolveStream, DSP setup,
     * lyrics, scrobble — travels through here, distinguished by {@code verb}.
     *
     * Asynchronous by design: the call is {@code oneway} so it never blocks the caller's binder
     * thread, and every result (including errors and paged/streamed output) comes back on
     * {@code callback}. {@code requestId} ties a later {@link #cancel} to this call.
     *
     * {@code args} carries small fields, the codec-encoded request payload (as a byte[]), and any
     * live resources (ParcelFileDescriptor / IBinder) the operation needs. Bulk data NEVER travels
     * inline — large lists page via cursor, audio/PCM/DSP ride a shared FD or shared memory.
     */
    oneway void invoke(String verb, long requestId, in Bundle args, IResultCallback callback) = 2;

    /**
     * Request cancellation of a previously-issued {@link #invoke} by its {@code requestId}.
     * Best-effort: the plugin should stop work and release resources for that request.
     */
    oneway void cancel(long requestId) = 3;

    /**
     * The host is releasing this plugin. The plugin should cancel in-flight work, close streams
     * and shared memory, and drop its reference to the host binder.
     */
    oneway void shutdown() = 4;
}
