// IAlbumCallback.aidl - Callback for single album operations
package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.Album;

interface IAlbumCallback {
    /**
     * Called when album is fetched successfully.
     */
    void onSuccess(in Album album);

    /**
     * Called when album call fails.
     *
     * @param errorCode Error code.
     * @param message Error message.
     */
    void onFailure(int errorCode, String message);
}

