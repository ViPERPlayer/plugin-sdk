// IResultCallback.aidl - Generic callback for async operations
package com.viperplayer.plugin.aidl;

import com.viperplayer.plugin.aidl.SearchResult;
import com.viperplayer.plugin.aidl.Song;
import com.viperplayer.plugin.aidl.Album;
import com.viperplayer.plugin.aidl.Artist;
import com.viperplayer.plugin.aidl.Playlist;
import com.viperplayer.plugin.aidl.BrowseCategory;
import com.viperplayer.plugin.aidl.AudioStream;

/**
 * Generic callback interface for async operations.
 * Used to return results from plugin to host.
 */
interface IResultCallback {
    // Search results
    void onSearchResult(in SearchResult result);
    void onSearchError(int errorCode, String message);
    
    // Single item results
    void onSongResult(in Song song);
    void onAlbumResult(in Album album);
    void onArtistResult(in Artist artist);
    void onPlaylistResult(in Playlist playlist);
    
    // List results with cursor
    void onSongsResult(in List<Song> songs, String nextCursor);
    void onAlbumsResult(in List<Album> albums, String nextCursor);
    void onArtistsResult(in List<Artist> artists, String nextCursor);
    void onPlaylistsResult(in List<Playlist> playlists, String nextCursor);
    void onCategoriesResult(in List<BrowseCategory> categories, String nextCursor);
    
    // Audio stream
    void onAudioStreamReady(in AudioStream stream);
    void onAudioStreamError(int errorCode, String message);
    
    // Generic error
    void onError(int errorCode, String message);
}

