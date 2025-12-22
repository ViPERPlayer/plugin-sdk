// IPlaylistsCallback.aidl - Callback for playlists list operations
package com.viperplayer.plugin.aidl;

import com.viperplayer.plugin.aidl.Playlist;

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

