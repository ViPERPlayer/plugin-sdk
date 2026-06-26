// IInitCallback.aidl
//
//  FROZEN WIRE ABI — DO NOT CHANGE. See IViperPlugin.aidl for the rules.
package com.viperplayer.plugin.ipc;

interface IInitCallback {

    /**
     * Handshake succeeded. {@code manifest} is the codec-encoded PluginManifest: the plugin's id,
     * name, version, the protocol version it speaks, and the set of capabilities/verbs it
     * advertises. The host calls only what the plugin advertises here.
     */
    oneway void onInitialized(in Bundle manifest) = 1;

    /**
     * Handshake failed (e.g. the plugin needs a newer host, or could not start). {@code code} is
     * a stable {@code PluginErrorCode}.
     */
    oneway void onInitializationFailed(int code, String message) = 2;
}
