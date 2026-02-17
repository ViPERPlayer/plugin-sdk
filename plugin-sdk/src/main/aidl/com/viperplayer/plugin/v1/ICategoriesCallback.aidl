// ICategoriesCallback.aidl - Callback for browse categories
package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.BrowseCategory;

/**
 * Callback interface for browse category operations.
 * On error, the implementation should throw a PluginException.
 */
interface ICategoriesCallback {
    /**
     * Called when categories are fetched successfully.
     * @throws PluginException if fetching fails
     */
    void onSuccess(in List<BrowseCategory> categories, String nextCursor) = 1;
}
