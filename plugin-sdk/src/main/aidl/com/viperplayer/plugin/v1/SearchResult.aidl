package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.MediaItemV1;

parcelable SearchResult {
    List<MediaItemV1> items;
    @nullable String nextCursor;

    const int TYPE_SONG = 1;
    const int TYPE_ALBUM = 2;
    const int TYPE_ARTIST = 4;
    const int TYPE_PLAYLIST = 8;
    const int TYPE_ALL = 15;
}
