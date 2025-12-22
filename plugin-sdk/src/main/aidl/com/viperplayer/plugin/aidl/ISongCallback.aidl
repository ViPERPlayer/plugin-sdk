// ISongCallback.aidl - Callback for single song operations
package com.viperplayer.plugin.aidl;

import com.viperplayer.plugin.aidl.Song;

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

