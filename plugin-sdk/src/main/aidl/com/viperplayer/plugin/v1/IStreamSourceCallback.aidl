// IStreamSourceCallback.aidl - Callback for stream source operations
package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.StreamSource;

/**
 * Callback interface for operations that return a StreamSource.
 * On error, the implementation should throw a PluginException.
 */
interface IStreamSourceCallback {
    /**
     * Called when stream source is fetched successfully.
     * @param source The stream source (URL, DASH XML, or AudioStream)
     * @throws PluginException if fetching fails
     */
    void onSuccess(in StreamSource source);
}

