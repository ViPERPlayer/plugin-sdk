package com.viperplayer.plugin.v1;

parcelable PluginCapabilities {
    boolean canSearch = true;
    boolean canBrowse = true;
    boolean hasLibrary = true;
    boolean hasPlaylists = true;
    boolean canSeek = true;
    boolean hasLyrics = false;
    boolean hasHighQuality = false;
    boolean supportsOffline = false;
    boolean hasSettings = false;
    boolean canEditPlaylists = false;
    boolean canSaveToLibrary = false;
    boolean hasRadio = false;
    int[] supportedQualities = {128, 256, 320};
}
