package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.Song;
import com.viperplayer.plugin.v1.Album;
import com.viperplayer.plugin.v1.Artist;
import com.viperplayer.plugin.v1.Playlist;

parcelable MediaItemV1 {
    Type type;
    @nullable Song song;
    @nullable Album album;
    @nullable Artist artist;
    @nullable Playlist playlist;

    enum Type {
        SONG,
        ALBUM,
        ARTIST,
        PLAYLIST
    }
}
