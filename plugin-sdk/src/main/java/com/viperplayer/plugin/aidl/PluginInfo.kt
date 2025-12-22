package com.viperplayer.plugin.aidl

import android.os.Parcel
import android.os.Parcelable

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
data class PluginInfo(
    val id: String,
    val name: String,
    val version: String,
    val apiVersion: Int,
    val description: String? = null,
    val author: String? = null,
    val iconUrl: String? = null,
) : Parcelable {
    
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        name = parcel.readString() ?: "",
        version = parcel.readString() ?: "",
        apiVersion = parcel.readInt(),
        description = parcel.readString(),
        author = parcel.readString(),
        iconUrl = parcel.readString()
    )
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(version)
        parcel.writeInt(apiVersion)
        parcel.writeString(description)
        parcel.writeString(author)
        parcel.writeString(iconUrl)
    }
    
    override fun describeContents(): Int = 0
    
    companion object CREATOR : Parcelable.Creator<PluginInfo> {
        override fun createFromParcel(parcel: Parcel): PluginInfo = PluginInfo(parcel)
        override fun newArray(size: Int): Array<PluginInfo?> = arrayOfNulls(size)
        
        /** Current API version of the SDK */
        const val CURRENT_API_VERSION = 1
    }
}

