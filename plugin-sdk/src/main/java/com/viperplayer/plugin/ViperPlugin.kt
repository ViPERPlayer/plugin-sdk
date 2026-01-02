package com.viperplayer.plugin

import com.viperplayer.plugin.v1.Album
import com.viperplayer.plugin.v1.Artist
import com.viperplayer.plugin.v1.BrowseCategory
import com.viperplayer.plugin.v1.IHostCallbackV1
import com.viperplayer.plugin.v1.Playlist
import com.viperplayer.plugin.v1.PluginCapabilities
import com.viperplayer.plugin.v1.SearchResult
import com.viperplayer.plugin.v1.SearchSuggestionsResultV1
import com.viperplayer.plugin.v1.Song

/**
 * Base interface that plugins implement.
 * Provides a clean suspend/Flow-based API for plugin developers.
 * 
 * The SDK handles conversion between this interface and the AIDL interface.
 * 
 * Example implementation:
 * ```kotlin
 * class MyMusicPlugin : ViperPlugin {
 *     override val pluginInfo = PluginInfo(
 *         id = "com.example.myplugin",
 *         name = "My Music Service",
 *         version = "1.0.0",
 *         apiVersion = PluginConstants.CURRENT_API_VERSION
 *     )
 *     
 *     override suspend fun search(query: String, types: Int, cursor: String?, limit: Int): SearchResult {
 *         // Implement search logic
 *     }
 *     
 *     // ... implement other methods
 * }
 * ```
 */
interface ViperPlugin {
    // ==================== Plugin Info ====================
    
    /**
     * Plugin capabilities.
     * Override to specify what features your plugin supports.
     */
    val capabilities: PluginCapabilities
        get() = PluginCapabilities()
    
    // ==================== Lifecycle ====================
    
    /**
     * Called when the host connects to this plugin.
     * @param host Controller for communicating with the host
     */
    suspend fun onConnect(hostCallback: IHostCallbackV1) {}
    
    /**
     * Called when the host disconnects from this plugin.
     */
    suspend fun onDisconnect() {}
    
    // ==================== Search ====================

    abstract suspend fun getSearchSuggestions(
        query: String
    ): SearchSuggestionsResultV1

    /**
     * Search for content.
     * 
     * @param query Search query string
     * @param types Bitmask of types (SearchResult.TYPE_*)
     * @param cursor Pagination cursor (null for first page)
     * @param limit Maximum results per type
     * @return Search results
     */
    abstract suspend fun search(
        query: String,
        types: Int = SearchResult.Companion.TYPE_ALL,
        cursor: String? = null,
        limit: Int = 20
    ): SearchResult
    
    // ==================== Browse ====================
    
    /**
     * Get root browse categories.
     */
    suspend fun getBrowseCategories(
        cursor: String? = null,
        limit: Int = 20
    ): PagedResult<BrowseCategory> = PagedResult.empty()
    
    /**
     * Get contents of a category.
     */
    suspend fun getCategoryContents(
        categoryId: String,
        cursor: String? = null,
        limit: Int = 20
    ): SearchResult = SearchResult()
    
    // ==================== Library ====================
    
    /**
     * Get user's saved songs.
     */
    suspend fun getLibrarySongs(
        cursor: String? = null,
        limit: Int = 50
    ): PagedResult<Song> = PagedResult.empty()
    
    /**
     * Get user's saved albums.
     */
    suspend fun getLibraryAlbums(
        cursor: String? = null,
        limit: Int = 50
    ): PagedResult<Album> = PagedResult.empty()
    
    /**
     * Get user's followed artists.
     */
    suspend fun getLibraryArtists(
        cursor: String? = null,
        limit: Int = 50
    ): PagedResult<Artist> = PagedResult.empty()
    
    /**
     * Get user's playlists.
     */
    suspend fun getLibraryPlaylists(
        cursor: String? = null,
        limit: Int = 50
    ): PagedResult<Playlist> = PagedResult.empty()
    
    // ==================== Details ====================
    
    /**
     * Get song details.
     */
    abstract suspend fun getSong(mediaId: String): Song
    
    /**
     * Get album details with tracks.
     */
    abstract suspend fun getAlbum(mediaId: String): Album
    
    /**
     * Get artist details.
     */
    abstract suspend fun getArtist(mediaId: String): Artist
    
    /**
     * Get artist's songs.
     */
    suspend fun getArtistSongs(
        artistId: String,
        cursor: String? = null,
        limit: Int = 50
    ): PagedResult<Song> = PagedResult.empty()
    
    /**
     * Get artist's albums.
     */
    suspend fun getArtistAlbums(
        artistId: String,
        cursor: String? = null,
        limit: Int = 50
    ): PagedResult<Album> = PagedResult.empty()
    
    /**
     * Get playlist details with tracks.
     */
    abstract suspend fun getPlaylist(mediaId: String): Playlist
    
    /**
     * Get playlist songs.
     */
    suspend fun getPlaylistSongs(
        playlistId: String,
        cursor: String? = null,
        limit: Int = 50
    ): PagedResult<Song> = PagedResult.empty()
    
    // ==================== Audio Streaming ====================
    
    /**
     * Get an audio stream for a song.
     * The plugin should decode the audio and stream PCM data.
     * 
     * Example implementation:
     * ```kotlin
     * override suspend fun getAudioStream(mediaId: String): AudioStreamWriter {
     *     val writer = AudioStreamWriter.create(
     *         mediaId = mediaId,
     *         format = AudioFormat.CD_QUALITY,
     *         durationMs = getSongDuration(mediaId)
     *     )
     *     
     *     // Start decoding in background
     *     scope.launch {
     *         decodeAndWrite(mediaId, writer)
     *     }
     *     
     *     return writer
     * }
     * ```
     */
    abstract suspend fun getAudioStream(mediaId: String): AudioStreamWriter
    
    /**
     * Stop an active audio stream.
     * @param streamId The stream ID to stop
     */
    suspend fun stopAudioStream(streamId: String) {}
    
    /**
     * Seek within an audio stream.
     * @param streamId The stream ID
     * @param positionMs Target position in milliseconds
     * @return true if seek was successful
     */
    suspend fun seekAudioStream(streamId: String, positionMs: Long): Boolean = false
    
    // ==================== Settings ====================
    
    /**
     * Get the class name of the settings Activity.
     * Return null if no custom settings UI is needed.
     */
    fun getSettingsActivityClass(): String? = null
    
    /**
     * Get the class name of an embeddable settings View.
     * Return null if not supported.
     */
    fun getSettingsViewClass(): String? = null
}

