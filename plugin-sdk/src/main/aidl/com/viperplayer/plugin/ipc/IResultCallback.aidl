// IResultCallback.aidl
//
//  FROZEN WIRE ABI — DO NOT CHANGE. See IViperPlugin.aidl for the rules.
//
//  One result channel for every verb. A request produces zero or more onPage callbacks
//  (streamed / cursor-paged output) and then exactly one terminal callback: onComplete or
//  onError. The shape of what each Bundle carries is defined by the codec, not by AIDL, so
//  this interface never has to change as new verbs are added.
package com.viperplayer.plugin.ipc;

interface IResultCallback {

    /**
     * An intermediate page of a streamed/paginated result. May be called any number of times
     * before the terminal callback. Each page Bundle stays well under the binder limit (bounded
     * item count + a cursor for the next page).
     */
    oneway void onPage(in Bundle page) = 1;

    /**
     * Terminal success. {@code result} carries the codec-encoded response (and, where relevant,
     * a cursor or a live resource handle). For verbs that page everything via {@link #onPage},
     * this may be empty.
     */
    oneway void onComplete(in Bundle result) = 2;

    /**
     * Terminal failure. {@code code} is a stable {@code PluginErrorCode}, {@code message} is
     * human-readable, and {@code extras} optionally carries a codec-encoded structured error.
     */
    oneway void onError(int code, String message, in Bundle extras) = 3;
}
