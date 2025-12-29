package com.viperplayer.plugin.sdk.v1

import android.os.Parcel
import android.os.Parcelable

/**
 * Represents an artist.
 * 
 * @property id Unique identifier for this artist
 * @property name Artist name
 * @property imageUrl URL to artist image/photo
 * @property genres List of genres associated with this artist
 * @property followerCount Number of followers (if available)
 * @property bio Short biography (if available)
 */
data class Artist(
    val id: MediaId,
    val name: String,
    val imageUrl: String? = null,
    val genres: List<String> = emptyList(),
    val followerCount: Long? = null,
    val bio: String? = null
) : Parcelable {
    
    constructor(parcel: Parcel) : this(
        id = parcel.readParcelable(MediaId::class.java.classLoader)!!,
        name = parcel.readString() ?: "",
        imageUrl = parcel.readString(),
        genres = mutableListOf<String>().apply {
            parcel.readStringList(this)
        },
        followerCount = parcel.readValue(Long::class.java.classLoader) as? Long,
        bio = parcel.readString()
    )
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(id, flags)
        parcel.writeString(name)
        parcel.writeString(imageUrl)
        parcel.writeStringList(genres)
        parcel.writeValue(followerCount)
        parcel.writeString(bio)
    }
    
    override fun describeContents(): Int = 0
    
    companion object CREATOR : Parcelable.Creator<Artist> {
        override fun createFromParcel(parcel: Parcel): Artist = Artist(parcel)
        override fun newArray(size: Int): Array<Artist?> = arrayOfNulls(size)
    }
}

