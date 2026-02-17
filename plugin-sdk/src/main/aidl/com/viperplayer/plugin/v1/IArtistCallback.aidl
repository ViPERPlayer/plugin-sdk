// IArtistCallback.aidl - Callback for single artist operations
package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.Artist;

interface IArtistCallback {
    /**
     * Called when artist is fetched successfully.
     */
    void onSuccess(in Artist artist) = 1;

    /**
     * Called when artist call fails.
     *
     * @param errorCode Error code.
     * @param message Error message.
     */
    void onFailure(int errorCode, String message) = 2;
}
