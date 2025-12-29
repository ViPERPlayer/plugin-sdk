// ISongCallback.sdk - Callback for single song operations
package com.viperplayer.plugin.sdk.v1;

import com.viperplayer.plugin.sdk.v1.Song;

/**
 * Callback interface for operations that return a single song.
 * On error, the implementation should throw a PluginException.
 */
interface ISongCallback {
    /**
     * Called when song is fetched successfully.
     * @throws PluginException if fetching fails
     */
    void onSuccess(in Song song);
}

