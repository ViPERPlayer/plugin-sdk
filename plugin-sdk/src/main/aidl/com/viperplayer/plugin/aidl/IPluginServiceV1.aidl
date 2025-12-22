// IPluginServiceV1.aidl - Main plugin interface (V1 API)
package com.viperplayer.plugin.aidl;

import com.viperplayer.plugin.aidl.PluginInfo;
import com.viperplayer.plugin.aidl.PluginCapabilities;
import com.viperplayer.plugin.aidl.MediaId;
import com.viperplayer.plugin.aidl.Song;
import com.viperplayer.plugin.aidl.Album;
import com.viperplayer.plugin.aidl.Artist;
import com.viperplayer.plugin.aidl.Playlist;
import com.viperplayer.plugin.aidl.BrowseCategory;
import com.viperplayer.plugin.aidl.IResultCallback;
import com.viperplayer.plugin.aidl.IHostCallbackV1;

/**
 * Main interface that plugins must implement.
 * This is the V1 API - newer versions will extend or create new interfaces.
 * 
 * All async methods use callbacks. The SDK provides Kotlin suspend wrappers.
 * 
 * API Version: 1
 */
interface IPluginServiceV1 {
    
    // ==================== Plugin Lifecycle ====================
    
    /**
     * Called when the host connects to the plugin.
     * Plugin receives a callback to communicate with the host.
     * Returns the plugin's API version.
     */
    int connect(IHostCallbackV1 hostCallback);
    
    /**
     * Called when the host disconnects from the plugin.
     * Plugin should clean up any resources.
     */
    void disconnect();
    
    /**
     * Get plugin capabilities (what features it supports).
     */
    PluginCapabilities getCapabilities();
    
    // ==================== Search ====================
    
    /**
     * Search for content across this plugin.
     * @param query The search query string
     * @param types Bitmask of types to search (SONG=1, ALBUM=2, ARTIST=4, PLAYLIST=8)
     * @param cursor Pagination cursor (null for first page)
     * @param limit Maximum results per type
     * @param callback Callback to receive results
     */
    void search(String query, int types, String cursor, int limit, IResultCallback callback);
    
    // ==================== Browse / Discovery ====================
    
    /**
     * Get root browse categories (e.g., "Genres", "Moods", "New Releases").
     */
    void getBrowseCategories(String cursor, int limit, IResultCallback callback);
    
    /**
     * Get contents of a category.
     * @param categoryId The category to browse
     */
    void getCategoryContents(String categoryId, String cursor, int limit, IResultCallback callback);
    
    // ==================== Library Access ====================
    
    /**
     * Get user's saved/liked songs.
     */
    void getLibrarySongs(String cursor, int limit, IResultCallback callback);
    
    /**
     * Get user's saved albums.
     */
    void getLibraryAlbums(String cursor, int limit, IResultCallback callback);
    
    /**
     * Get user's followed artists.
     */
    void getLibraryArtists(String cursor, int limit, IResultCallback callback);
    
    /**
     * Get user's playlists.
     */
    void getLibraryPlaylists(String cursor, int limit, IResultCallback callback);
    
    // ==================== Detail Fetching ====================
    
    /**
     * Get full song details by ID.
     */
    void getSong(in MediaId mediaId, IResultCallback callback);
    
    /**
     * Get album details and tracks.
     */
    void getAlbum(in MediaId mediaId, IResultCallback callback);
    
    /**
     * Get artist details.
     */
    void getArtist(in MediaId mediaId, IResultCallback callback);
    
    /**
     * Get artist's top songs.
     */
    void getArtistSongs(in MediaId artistId, String cursor, int limit, IResultCallback callback);
    
    /**
     * Get artist's albums.
     */
    void getArtistAlbums(in MediaId artistId, String cursor, int limit, IResultCallback callback);
    
    /**
     * Get playlist details and tracks.
     */
    void getPlaylist(in MediaId mediaId, IResultCallback callback);
    
    /**
     * Get playlist tracks.
     */
    void getPlaylistSongs(in MediaId playlistId, String cursor, int limit, IResultCallback callback);
    
    // ==================== Audio Streaming ====================
    
    /**
     * Get an audio stream for playback.
     * Plugin should start a background thread to decode and stream PCM data.
     * @param mediaId The song to stream
     * @param callback Callback to receive the AudioStream (contains ParcelFileDescriptor)
     */
    void getAudioStream(in MediaId mediaId, IResultCallback callback);
    
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
    
    // ==================== Settings ====================
    
    /**
     * Get the fully qualified class name of the settings Activity.
     * Return null if no custom settings UI is needed.
     * The host will launch this activity for plugin configuration.
     */
    String getSettingsActivityClass();
    
    /**
     * Get the component name for an embeddable settings view.
     * This should be a RemoteViews-compatible view or a custom View class.
     * Return null if not supported.
     */
    String getSettingsViewClass();
}

