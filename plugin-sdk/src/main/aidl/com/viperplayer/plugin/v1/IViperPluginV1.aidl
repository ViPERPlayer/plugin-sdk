// IViperPluginV1.aidl - Main plugin interface (V1 API)
package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.PluginInfo;
import com.viperplayer.plugin.v1.PluginCapabilities;
import com.viperplayer.plugin.v1.Song;
import com.viperplayer.plugin.v1.Album;
import com.viperplayer.plugin.v1.Artist;
import com.viperplayer.plugin.v1.Playlist;
import com.viperplayer.plugin.v1.BrowseCategory;
import com.viperplayer.plugin.v1.ISearchCallback;
import com.viperplayer.plugin.v1.ISearchSuggestionsCallback;
import com.viperplayer.plugin.v1.ICategoriesCallback;
import com.viperplayer.plugin.v1.ISongsCallback;
import com.viperplayer.plugin.v1.IAlbumsCallback;
import com.viperplayer.plugin.v1.IArtistsCallback;
import com.viperplayer.plugin.v1.IPlaylistsCallback;
import com.viperplayer.plugin.v1.ISongCallback;
import com.viperplayer.plugin.v1.IAlbumCallback;
import com.viperplayer.plugin.v1.IArtistCallback;
import com.viperplayer.plugin.v1.IPlaylistCallback;
import com.viperplayer.plugin.v1.IStreamSourceCallback;
import com.viperplayer.plugin.v1.StreamSource;
import com.viperplayer.plugin.v1.IHostCallbackV1;
import com.viperplayer.plugin.v1.IHomeContentCallback;
import com.viperplayer.plugin.IConnectCallback;

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
    void onConnect(IHostCallbackV1 hostCallback, IConnectCallback callback) = 1;
    
    /**
     * Called when the host disconnects from the plugin.
     * Plugin should clean up any resources.
     */
    void onDisconnect() = 2;
    
    /**
     * Get plugin capabilities (what features it supports).
     */
    PluginCapabilities getCapabilities() = 3;
    
    /**
     * Get the fully qualified class name of the plugin's settings activity.
     * If the plugin does not have a settings activity, return null.
     */
    String getSettingsActivityClass() = 4;

    // ==================== Search ====================

    /**
    * Get search suggestions for a query.
    * @param query The search query string
    * @param callback Callback to receive results
    */
    void getSearchSuggestions(String query, ISearchSuggestionsCallback callback) = 11;
    
    /**
     * Search for content across this plugin.
     * @param query The search query string
     * @param types Bitmask of types to search (SONG=1, ALBUM=2, ARTIST=4, PLAYLIST=8)
     * @param cursor Pagination cursor (null for first page)
     * @param limit Maximum results per type
     * @param callback Callback to receive results
     */
    void search(String query, int types, String cursor, int limit, ISearchCallback callback) = 12;

    // ==================== Browse / Discovery ====================
    
    /**
     * Get root browse categories (e.g., "Genres", "Moods", "New Releases").
     */
    void getBrowseCategories(String cursor, int limit, ICategoriesCallback callback) = 21;

    /**
     * Get contents of a category.
     * @param categoryId The category to browse
     */
    void getCategoryContents(String categoryId, String cursor, int limit, ISearchCallback callback) = 22;

    // ==================== Library Access ====================
    
    /**
     * Get user's saved/liked songs.
     */
    void getLibrarySongs(String cursor, int limit, ISongsCallback callback) = 31;

    /**
     * Get user's saved albums.
     */
    void getLibraryAlbums(String cursor, int limit, IAlbumsCallback callback) = 32;

    /**
     * Get user's followed artists.
     */
    void getLibraryArtists(String cursor, int limit, IArtistsCallback callback) = 33;

    /**
     * Get user's playlists.
     */
    void getLibraryPlaylists(String cursor, int limit, IPlaylistsCallback callback) = 34;

    // ==================== Detail Fetching ====================
    
    /**
     * Get full song details by ID.
     */
    void getSong(in String id, ISongCallback callback) = 41;

    /**
     * Get album details and tracks.
     */
    void getAlbum(in String id, IAlbumCallback callback) = 42;

    /**
     * Get artist details.
     */
    void getArtist(in String id, IArtistCallback callback) = 43;

    /**
     * Get artist's top songs.
     */
    void getArtistSongs(in String artistId, String cursor, int limit, ISongsCallback callback) = 44;

    /**
     * Get artist's albums.
     */
    void getArtistAlbums(in String artistId, String cursor, int limit, IAlbumsCallback callback) = 45;

    /**
     * Get playlist details and tracks.
     */
    void getPlaylist(in String id, IPlaylistCallback callback) = 46;

    /**
     * Get playlist tracks.
     */
    void getPlaylistSongs(in String playlistId, String cursor, int limit, ISongsCallback callback) = 47;

    // ==================== Home Screen ====================

    /**
     * Get content for the home screen.
     */
    void getHomeSections(IHomeContentCallback callback) = 61;

    // ==================== Audio Streaming ====================
    
    /**
     * Get a stream source for playback.
     * Can return a URL, DASH XML, or AudioStream based on what the plugin supports.
     * @param id The song to get stream for
     * @param callback Callback to receive the StreamSource
     */
    void getStream(in String id, IStreamSourceCallback callback) = 71;

    /**
     * Stop an active audio stream.
     * Plugin should stop decoding and close the pipe.
     * @param streamId The stream ID from AudioStream
     */
    void stopAudioStream(String streamId) = 72;
    
    /**
     * Seek within an active audio stream.
     * @param streamId The stream ID
     * @param positionMs Target position in milliseconds
     * @return true if seek is supported and successful
     */
    boolean seekAudioStream(String streamId, long positionMs) = 73;
}
