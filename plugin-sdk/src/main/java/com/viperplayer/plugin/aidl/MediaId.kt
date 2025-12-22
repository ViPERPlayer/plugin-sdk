package com.viperplayer.plugin.aidl

import android.os.Parcel
import android.os.Parcelable

/**
 * Composite key for uniquely identifying media across plugins.
 * Format: "pluginId:sourceId"
 * 
 * @property pluginId The unique identifier of the plugin
 * @property sourceId The source-specific identifier (e.g., a streaming service track ID)
 */
data class MediaId(
    val pluginId: String,
    val sourceId: String
) : Parcelable {
    
    constructor(parcel: Parcel) : this(
        pluginId = parcel.readString() ?: "",
        sourceId = parcel.readString() ?: ""
    )
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(pluginId)
        parcel.writeString(sourceId)
    }
    
    override fun describeContents(): Int = 0
    
    /**
     * Returns the composite key string.
     */
    override fun toString(): String = "$pluginId:$sourceId"
    
    companion object CREATOR : Parcelable.Creator<MediaId> {
        override fun createFromParcel(parcel: Parcel): MediaId = MediaId(parcel)
        override fun newArray(size: Int): Array<MediaId?> = arrayOfNulls(size)
        
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

