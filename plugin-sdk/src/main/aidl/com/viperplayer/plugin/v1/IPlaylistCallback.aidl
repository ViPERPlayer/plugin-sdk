// IPlaylistCallback.sdk - Callback for single playlist operations
package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.Playlist;

/**
 * Callback interface for operations that return a single playlist.
 * On error, the implementation should throw a PluginException.
 */
interface IPlaylistCallback {
    /**
     * Called when playlist is fetched successfully.
     * @throws PluginException if fetching fails
     */
    void onSuccess(in Playlist playlist);
}

