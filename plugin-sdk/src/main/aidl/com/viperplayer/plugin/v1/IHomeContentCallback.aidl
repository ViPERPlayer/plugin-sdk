// IHomeContentCallback.aidl - Callback for home content operations
package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.HomeContent;

/**
 * Callback interface for operations that return home content.
 * On error, the implementation should throw a PluginException.
 */
interface IHomeContentCallback {
    /**
     * Called when home content is fetched successfully.
     * @throws PluginException if fetching fails
     */
    void onSuccess(in HomeContent content) = 1;

    /**
     * Called when fetching home content fails.
     * @param errorMessage Description of the error
     */
    void onFailure(String errorMessage) = 2;
}
