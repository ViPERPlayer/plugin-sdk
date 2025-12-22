// IArtistCallback.aidl - Callback for single artist operations
package com.viperplayer.plugin.aidl;

import com.viperplayer.plugin.aidl.Artist;

/**
 * Callback interface for operations that return a single artist.
 * On error, the implementation should throw a PluginException.
 */
interface IArtistCallback {
    /**
     * Called when artist is fetched successfully.
     * @throws PluginException if fetching fails
     */
    void onSuccess(in Artist artist);
}

