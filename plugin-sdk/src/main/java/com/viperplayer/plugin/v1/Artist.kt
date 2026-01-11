package com.viperplayer.plugin.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents an artist.
 * 
 * @property id Unique identifier for this artist
 * @property name Artist name
 * @property artwork Artist image/photo
 * @property topSongs List of top songs by this artist
 * @property albums List of albums by this artist
 * @property playlists List of playlists by this artist
 * @property featuring List of artists featuring this artist in playlists
 * @property appearsOn List of media items this artist appears on
 * @property similarArtists List of similar artists to this artist
 */
@Parcelize
data class Artist(
    val id: String,
    val name: String,
    val artwork: Artwork? = null,
    val topSongs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val featuring: List<Playlist> = emptyList(),
    val appearsOn: List<MediaItemV1> = emptyList(),
    val similarArtists: List<Artist> = emptyList()
) : Parcelable
