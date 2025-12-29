// IArtistCallback.sdk - Callback for single artist operations
package com.viperplayer.plugin.sdk.v1;

import com.viperplayer.plugin.sdk.v1.Artist;

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

