// IAlbumCallback.aidl - Callback for single album operations
package com.viperplayer.plugin.aidl;

import com.viperplayer.plugin.aidl.Album;

/**
 * Callback interface for operations that return a single album.
 * On error, the implementation should throw a PluginException.
 */
interface IAlbumCallback {
    /**
     * Called when album is fetched successfully.
     * @throws PluginException if fetching fails
     */
    void onSuccess(in Album album);
}

