package com.viperplayer.plugin.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaItemV1(
    val type: Type,
    val song: Song? = null,
    val album: Album? = null,
    val artist: Artist? = null,
    val playlist: Playlist? = null
) : Parcelable {
    enum class Type {
        SONG,
        ALBUM,
        ARTIST,
        PLAYLIST
    }

    companion object {
        fun song(song: Song) = MediaItemV1(Type.SONG, song = song)
        fun album(album: Album) = MediaItemV1(Type.ALBUM, album = album)
        fun artist(artist: Artist) = MediaItemV1(Type.ARTIST, artist = artist)
        fun playlist(playlist: Playlist) = MediaItemV1(Type.PLAYLIST, playlist = playlist)
    }
}
