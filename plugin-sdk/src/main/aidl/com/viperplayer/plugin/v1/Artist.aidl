package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.Artwork;
import com.viperplayer.plugin.v1.Song;
import com.viperplayer.plugin.v1.Album;
import com.viperplayer.plugin.v1.Playlist;
import com.viperplayer.plugin.v1.MediaItemV1;

parcelable Artist {
    String id;
    String name;
    @nullable Artwork artwork;
    List<Song> topSongs;
    List<Album> albums;
    List<Playlist> playlists;
    List<Playlist> featuring;
    List<MediaItemV1> appearsOn;
    List<Artist> similarArtists;
}
