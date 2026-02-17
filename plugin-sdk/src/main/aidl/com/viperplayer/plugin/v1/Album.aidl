package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.Artist;
import com.viperplayer.plugin.v1.Artwork;
import com.viperplayer.plugin.v1.Song;

parcelable Album {
    String id;
    String name;
    List<Artist> artists;
    @nullable Artwork artwork;
    
    int releaseYear = 0;
    boolean hasReleaseYear = false;

    int trackCount = 0;
    boolean isExplicit = false;
    AlbumType type = AlbumType.ALBUM;
    List<Song> songs;

    enum AlbumType {
        ALBUM,
        SINGLE,
        EP,
        COMPILATION
    }
}
