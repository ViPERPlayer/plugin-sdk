// ICategoriesCallback.aidl - Callback for browse categories
package com.viperplayer.plugin.aidl;

import com.viperplayer.plugin.aidl.BrowseCategory;

/**
 * Callback interface for browse category operations.
 * On error, the implementation should throw a PluginException.
 */
interface ICategoriesCallback {
    /**
     * Called when categories are fetched successfully.
     * @throws PluginException if fetching fails
     */
    void onSuccess(in List<BrowseCategory> categories, String nextCursor);
}

