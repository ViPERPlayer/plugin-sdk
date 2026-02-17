// IPlaylistCallback.aidl - Callback for single playlist operations
package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.Playlist;

interface IPlaylistCallback {
    /**
     * Called when playlist is fetched successfully.
     */
    void onSuccess(in Playlist playlist) = 1;

    /**
     * Called when playlist call fails.
     *
     * @param errorCode Error code.
     * @param message Error message.
     */
    void onFailure(int errorCode, String message) = 2;
}
