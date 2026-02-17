package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.Artwork;
import com.viperplayer.plugin.v1.Song;

parcelable Playlist {
    String id;
    String name;
    @nullable String description;
    @nullable Artwork artwork;
    @nullable String ownerName;
    int songCount = 0;
    List<Song> songs;
}
