// ISearchCallback.sdk - Callback for search operations
package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.SearchResult;

/**
 * Callback interface for search operations.
 * On error, the implementation should throw a PluginException.
 */
interface ISearchCallback {
    /**
     * Called when search completes successfully.
     * @throws PluginException if search fails
     */
    void onSuccess(in SearchResult result);
}
