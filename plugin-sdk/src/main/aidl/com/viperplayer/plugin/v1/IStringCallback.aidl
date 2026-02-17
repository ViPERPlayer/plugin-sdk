// IStringCallback.aidl - Callback for string operations
package com.viperplayer.plugin.v1;

/**
 * Callback interface for operations that return a string (e.g., stream URL).
 * On error, the implementation should throw a PluginException.
 */
interface IStringCallback {
    /**
     * Called when string is fetched successfully.
     * @param value The string value (null if not available)
     * @throws PluginException if fetching fails
     */
    void onSuccess(String value) = 1;
}
