// ISearchSuggetsionsCallback.aidl - Callback for search suggestions
package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.SearchSuggestionsResultV1;

interface ISearchSuggestionsCallback {
    /**
     * Called when search suggestions call completes successfully.
     */
    void onSuccess(in SearchSuggestionsResultV1 result);

    /**
     * Called when search suggestions call fails.
     *
     * @param errorCode Error code.
     * @param message Error message.
     */
    void onFailure(int errorCode, String message);
}
