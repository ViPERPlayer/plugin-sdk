// IAlbumsCallback.sdk - Callback for albums list operations
package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.Album;

/**
 * Callback interface for operations that return a list of albums.
 * On error, the implementation should throw a PluginException.
 */
interface IAlbumsCallback {
    /**
     * Called when albums are fetched successfully.
     * @throws PluginException if fetching fails
     */
    void onSuccess(in List<Album> albums, String nextCursor);
}

