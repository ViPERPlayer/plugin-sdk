// ISearchSuggetsionsCallback.aidl - Callback for search suggestions
package com.viperplayer.plugin.aidl;

import com.viperplayer.plugin.aidl.SearchResult;

interface ISearchSuggestionsCallback {
    /**
     * Called when search suggestions call completes successfully.
     * @throws PluginException if search fails
     */
    void onSuccess(in List<String> result);
}
