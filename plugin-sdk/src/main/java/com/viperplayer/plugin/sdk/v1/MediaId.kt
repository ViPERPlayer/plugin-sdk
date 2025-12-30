package com.viperplayer.plugin.sdk.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Composite key for uniquely identifying media across plugins.
 * Format: "pluginId:sourceId"
 * 
 * @property pluginId The unique identifier of the plugin
 * @property sourceId The source-specific identifier (e.g., a streaming service track ID)
 */
@Parcelize
data class MediaId(
    val pluginId: String,
    val sourceId: String
) : Parcelable {
    /**
     * Returns the composite key string.
     */
    override fun toString(): String = "$pluginId:$sourceId"
    
    companion object {
        /**
         * Parse a composite key string into a MediaId.
         * @throws IllegalArgumentException if the format is invalid
         */
        fun fromString(compositeKey: String): MediaId {
            val parts = compositeKey.split(":", limit = 2)
            require(parts.size == 2) { "Invalid MediaId format: $compositeKey" }
            return MediaId(parts[0], parts[1])
        }
    }
}
