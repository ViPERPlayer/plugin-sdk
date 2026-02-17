package com.viperplayer.plugin.v1;

parcelable PluginInfo {
    String id;
    String name;
    String version;
    int apiVersion;
    @nullable String description;
    @nullable String author;
    
    const int CURRENT_API_VERSION = 1;
}
