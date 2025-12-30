// IViperPluginV1.sdk - Main plugin interface (V1 API)
package com.viperplayer.plugin.sdk.v1;

import com.viperplayer.plugin.sdk.v1.PluginInfo;
import com.viperplayer.plugin.sdk.v1.PluginCapabilities;
import com.viperplayer.plugin.sdk.v1.MediaId;
import com.viperplayer.plugin.sdk.v1.Song;
import com.viperplayer.plugin.sdk.v1.Album;
import com.viperplayer.plugin.sdk.v1.Artist;
import com.viperplayer.plugin.sdk.v1.Playlist;
import com.viperplayer.plugin.sdk.v1.BrowseCategory;
import com.viperplayer.plugin.sdk.v1.ISearchCallback;
import com.viperplayer.plugin.sdk.v1.ISearchSuggestionsCallback;
import com.viperplayer.plugin.sdk.v1.ICategoriesCallback;
import com.viperplayer.plugin.sdk.v1.ISongsCallback;
import com.viperplayer.plugin.sdk.v1.IAlbumsCallback;
import com.viperplayer.plugin.sdk.v1.IArtistsCallback;
import com.viperplayer.plugin.sdk.v1.IPlaylistsCallback;
import com.viperplayer.plugin.sdk.v1.ISongCallback;
import com.viperplayer.plugin.sdk.v1.IAlbumCallback;
import com.viperplayer.plugin.sdk.v1.IArtistCallback;
import com.viperplayer.plugin.sdk.v1.IPlaylistCallback;
import com.viperplayer.plugin.sdk.v1.IAudioStreamCallback;
import com.viperplayer.plugin.sdk.v1.IHostCallbackV1;
import com.viperplayer.plugin.sdk.IConnectCallback;

/**
 * Main interface that plugins must implement.
 * This is the V1 API - newer versions will extend or create new interfaces.
 * 
 * All async methods use callbacks. The SDK provides Kotlin suspend wrappers.
 */
interface IViperPluginV1 {
    
    // ==================== Plugin Lifecycle ====================
    
    /**
     * Called when the host connects to the plugin.
     * Plugin receives a callback to communicate with the host.
     */
    void onConnect(IHostCallbackV1 hostCallback, IConnectCallback callback);
    
    /**
     * Called when the host disconnects from the plugin.
     * Plugin should clean up any resources.
     */
    void onDisconnect();
    
    /**
     * Get plugin capabilities (what features it supports).
     */
    PluginCapabilities getCapabilities();
    
    // ==================== Search ====================

    /**
    * Get search suggestions for a query.
    * @param query The search query string
    * @param callback Callback to receive results
    */
    void getSearchSuggestions(String query, ISearchSuggestionsCallback callback);
    
    /**
     * Search for content across this plugin.
     * @param query The search query string
     * @param types Bitmask of types to search (SONG=1, ALBUM=2, ARTIST=4, PLAYLIST=8)
     * @param cursor Pagination cursor (null for first page)
     * @param limit Maximum results per type
     * @param callback Callback to receive results
     */
    void search(String query, int types, String cursor, int limit, ISearchCallback callback);

    // ==================== Browse / Discovery ====================
    
    /**
     * Get root browse categories (e.g., "Genres", "Moods", "New Releases").
     */
    void getBrowseCategories(String cursor, int limit, ICategoriesCallback callback);

    /**
     * Get contents of a category.
     * @param categoryId The category to browse
     */
    void getCategoryContents(String categoryId, String cursor, int limit, ISearchCallback callback);

    // ==================== Library Access ====================
    
    /**
     * Get user's saved/liked songs.
     */
    void getLibrarySongs(String cursor, int limit, ISongsCallback callback);

    /**
     * Get user's saved albums.
     */
    void getLibraryAlbums(String cursor, int limit, IAlbumsCallback callback);

    /**
     * Get user's followed artists.
     */
    void getLibraryArtists(String cursor, int limit, IArtistsCallback callback);

    /**
     * Get user's playlists.
     */
    void getLibraryPlaylists(String cursor, int limit, IPlaylistsCallback callback);

    // ==================== Detail Fetching ====================
    
    /**
     * Get full song details by ID.
     */
    void getSong(in MediaId mediaId, ISongCallback callback);

    /**
     * Get album details and tracks.
     */
    void getAlbum(in MediaId mediaId, IAlbumCallback callback);

    /**
     * Get artist details.
     */
    void getArtist(in MediaId mediaId, IArtistCallback callback);

    /**
     * Get artist's top songs.
     */
    void getArtistSongs(in MediaId artistId, String cursor, int limit, ISongsCallback callback);

    /**
     * Get artist's albums.
     */
    void getArtistAlbums(in MediaId artistId, String cursor, int limit, IAlbumsCallback callback);

    /**
     * Get playlist details and tracks.
     */
    void getPlaylist(in MediaId mediaId, IPlaylistCallback callback);

    /**
     * Get playlist tracks.
     */
    void getPlaylistSongs(in MediaId playlistId, String cursor, int limit, ISongsCallback callback);

    // ==================== Audio Streaming ====================
    
    /**
     * Get an audio stream for playback.
     * Plugin should start a background thread to decode and stream PCM data.
     * @param mediaId The song to stream
     * @param callback Callback to receive the AudioStream (contains ParcelFileDescriptor)
     */
    void getAudioStream(in MediaId mediaId, IAudioStreamCallback callback);

    /**
     * Stop an active audio stream.
     * Plugin should stop decoding and close the pipe.
     * @param streamId The stream ID from AudioStream
     */
    void stopAudioStream(String streamId);
    
    /**
     * Seek within an active audio stream.
     * @param streamId The stream ID
     * @param positionMs Target position in milliseconds
     * @return true if seek is supported and successful
     */
    boolean seekAudioStream(String streamId, long positionMs);
}

