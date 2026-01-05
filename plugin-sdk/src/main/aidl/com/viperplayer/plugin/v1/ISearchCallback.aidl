// ISearchCallback.aidl - Callback for search operations
package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.SearchResult;

interface ISearchCallback {
    /**
     * Called when search completes successfully.
     */
    void onSuccess(in SearchResult result);

    /**
     * Called when search suggestions call fails.
     *
     * @param errorCode Error code.
     * @param message Error message.
     */
    void onFailure(int errorCode, String message);
}
