package com.viperplayer.plugin.v1;

parcelable BrowseCategory {
    String id;
    String pluginId;
    String name;
    @nullable String description;
    @nullable String imageUrl;
    CategoryContentType contentType = CategoryContentType.MIXED;
    
    enum CategoryContentType {
        CATEGORIES,
        PLAYLISTS,
        ALBUMS,
        ARTISTS,
        SONGS,
        MIXED
    }
}
