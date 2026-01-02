// IArtistsCallback.sdk - Callback for artists list operations
package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.Artist;

/**
 * Callback interface for operations that return a list of artists.
 * On error, the implementation should throw a PluginException.
 */
interface IArtistsCallback {
    /**
     * Called when artists are fetched successfully.
     * @throws PluginException if fetching fails
     */
    void onSuccess(in List<Artist> artists, String nextCursor);
}

