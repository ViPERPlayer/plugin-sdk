// ISongsCallback.sdk - Callback for songs list operations
package com.viperplayer.plugin.sdk.v1;

import com.viperplayer.plugin.sdk.v1.Song;

/**
 * Callback interface for operations that return a list of songs.
 * On error, the implementation should throw a PluginException.
 */
interface ISongsCallback {
    /**
     * Called when songs are fetched successfully.
     * @throws PluginException if fetching fails
     */
    void onSuccess(in List<Song> songs, String nextCursor);
}

