package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.MediaItemV1;

parcelable HomeSection {
    String id;
    String title;
    List<MediaItemV1> items;
    Layout layout = Layout.LIST;

    enum Layout {
        LIST,
        GRID
    }
}
