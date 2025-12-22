// IArtistsCallback.aidl - Callback for artists list operations
package com.viperplayer.plugin.aidl;

import com.viperplayer.plugin.aidl.Artist;

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

