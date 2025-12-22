// ISearchCallback.aidl - Callback for search operations
package com.viperplayer.plugin.aidl;

import com.viperplayer.plugin.aidl.SearchResult;

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
