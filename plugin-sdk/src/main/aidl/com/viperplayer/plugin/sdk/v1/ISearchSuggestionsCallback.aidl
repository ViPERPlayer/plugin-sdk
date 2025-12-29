// ISearchSuggetsionsCallback.sdk - Callback for search suggestions
package com.viperplayer.plugin.sdk.v1;

import com.viperplayer.plugin.sdk.v1.SearchResult;

interface ISearchSuggestionsCallback {
    /**
     * Called when search suggestions call completes successfully.
     * @throws PluginException if search fails
     */
    void onSuccess(in List<String> result);
}
