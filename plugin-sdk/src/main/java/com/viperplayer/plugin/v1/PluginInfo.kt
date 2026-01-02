package com.viperplayer.plugin.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Metadata about a plugin.
 * 
 * @property id Unique plugin identifier (e.g., "com.example.sixthsource")
 * @property name Human-readable plugin name (e.g., "a streaming service")
 * @property version Plugin version string (e.g., "1.0.0")
 * @property apiVersion The plugin SDK API version this plugin targets
 * @property description Short description of the plugin (optional)
 * @property author Plugin author/developer name (optional)
 * @property iconUrl URL to plugin icon (optional)
 */
@Parcelize
data class PluginInfo(
    val id: String,
    val name: String,
    val version: String,
    val apiVersion: Int,
    val description: String? = null,
    val author: String? = null,
) : Parcelable {
    companion object {
        /** Current API version of the SDK */
        const val CURRENT_API_VERSION = 1
    }
}
