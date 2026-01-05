// IAudioStreamCallback.aidl - Callback for audio stream operations
package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.AudioStream;

/**
 * Callback interface for audio stream operations.
 * On error, the implementation should throw a PluginException.
 */
interface IAudioStreamCallback {
    /**
     * Called when audio stream is ready.
     * @throws PluginException if stream creation fails
     */
    void onSuccess(in AudioStream stream);
}

