// IPlaylistsCallback.sdk - Callback for playlists list operations
package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.Playlist;

/**
 * Callback interface for operations that return a list of playlists.
 * On error, the implementation should throw a PluginException.
 */
interface IPlaylistsCallback {
    /**
     * Called when playlists are fetched successfully.
     * @throws PluginException if fetching fails
     */
    void onSuccess(in List<Playlist> playlists, String nextCursor);
}

