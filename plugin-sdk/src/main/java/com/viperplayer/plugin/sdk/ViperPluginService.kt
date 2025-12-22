package com.viperplayer.plugin.sdk

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.viperplayer.plugin.aidl.*
import kotlinx.coroutines.*

/**
 * Base Service class for plugins.
 * Extend this class and implement your plugin logic.
 * 
 * Example:
 * ```kotlin
 * class MyPluginService : ViperPluginService() {
 *     override fun createPlugin(): ViperPlugin = MyMusicPlugin()
 * }
 * ```
 * 
 * In your AndroidManifest.xml:
 * ```xml
 * <service
 *     android:name=".MyPluginService"
 *     android:exported="true">
 *     <intent-filter>
 *         <action android:name="com.viperplayer.plugin.PLUGIN_SERVICE" />
 *     </intent-filter>
 *     <meta-data
 *         android:name="com.viperplayer.plugin.API_VERSION"
 *         android:value="1" />
 *     <meta-data
 *         android:name="com.viperplayer.plugin.ID"
 *         android:value="com.example.myplugin" />
 *     <meta-data
 *         android:name="com.viperplayer.plugin.NAME"
 *         android:value="My Music Plugin" />
 * </service>
 * ```
 */
abstract class ViperPluginService : Service() {
    companion object {
        private const val TAG = "ViperPluginService"
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var plugin: ViperPlugin? = null
    private var hostController: HostController? = null
    private val activeStreams = mutableMapOf<String, AudioStreamWriter>()
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called for service: ${this::class.java.name}")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand() called: intent=${intent?.action}, flags=$flags, startId=$startId")
        return START_NOT_STICKY // Don't restart if killed
    }
    
    /**
     * Create your plugin implementation.
     */
    protected abstract fun createPlugin(): ViperPlugin
    
    /**
     * Get the plugin instance (creates it if needed).
     */
    protected fun getPlugin(): ViperPlugin {
        return plugin ?: createPlugin().also { 
            plugin = it
            Log.d(TAG, "Plugin instance created")
        }
    }
    
    private val binder = object : IPluginServiceV1.Stub() {
        
        override fun connect(hostCallback: IHostCallbackV1): Int {
            Log.d(TAG, "connect() called")
            val ctrl = HostController(hostCallback)
            hostController = ctrl
            val p = getPlugin()
            
            scope.launch {
                try {
                    Log.d(TAG, "Calling plugin.onConnect()")
                    p.onConnect(ctrl)
                    Log.d(TAG, "Plugin.onConnect() completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error in plugin.onConnect()", e)
                    ctrl.reportError(ErrorCodes.PLUGIN_ERROR, "Connect failed: ${e.message}")
                }
            }
            
            Log.d(TAG, "connect() returning API version 1")
            return 1
        }
        
        override fun disconnect() {
            Log.d(TAG, "disconnect() called")
            scope.launch {
                try {
                    Log.d(TAG, "Calling plugin.onDisconnect()")
                    getPlugin().onDisconnect()
                    Log.d(TAG, "Plugin.onDisconnect() completed")
                } catch (e: Exception) {
                    Log.w(TAG, "Error in plugin.onDisconnect(), ignoring", e)
                }
            }
            hostController = null
            
            // Close all active streams
            Log.d(TAG, "Closing ${activeStreams.size} active streams")
            activeStreams.values.forEach { it.close() }
            activeStreams.clear()
            Log.d(TAG, "disconnect() completed")
        }
        
        override fun getCapabilities(): PluginCapabilities {
            Log.d(TAG, "getCapabilities() called")
            val capabilities = getPlugin().capabilities
            Log.d(TAG, "Returning capabilities: canSearch=${capabilities.canSearch}, canBrowse=${capabilities.canBrowse}, hasLibrary=${capabilities.hasLibrary}")
            return capabilities
        }
        
        // ==================== Search ====================
        
        override fun search(
            query: String,
            types: Int,
            cursor: String?,
            limit: Int,
            callback: IResultCallback
        ) {
            Log.d(TAG, "search() called: query=$query, types=$types, cursor=$cursor, limit=$limit")
            scope.launch {
                try {
                    val result = getPlugin().search(query, types, cursor, limit)
                    Log.d(TAG, "Search completed: songs=${result.songs.size}, albums=${result.albums.size}, artists=${result.artists.size}, playlists=${result.playlists.size}")
                    callback.onSearchResult(result)
                } catch (e: PluginException) {
                    Log.e(TAG, "Search failed with PluginException: code=${e.errorCode}, message=${e.message}")
                    callback.onSearchError(e.errorCode, e.message ?: "Search failed")
                } catch (e: Exception) {
                    Log.e(TAG, "Search failed with exception", e)
                    callback.onSearchError(ErrorCodes.UNKNOWN, e.message ?: "Search failed")
                }
            }
        }
        
        // ==================== Browse ====================
        
        override fun getBrowseCategories(cursor: String?, limit: Int, callback: IResultCallback) {
            Log.d(TAG, "getBrowseCategories() called: cursor=$cursor, limit=$limit")
            scope.launch {
                try {
                    val result = getPlugin().getBrowseCategories(cursor, limit)
                    Log.d(TAG, "Browse categories completed: count=${result.items.size}, nextCursor=${result.nextCursor}")
                    callback.onCategoriesResult(result.items, result.nextCursor)
                } catch (e: PluginException) {
                    Log.e(TAG, "Failed to get browse categories: code=${e.errorCode}, message=${e.message}")
                    callback.onError(e.errorCode, e.message ?: "Failed to get categories")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get browse categories", e)
                    callback.onError(ErrorCodes.UNKNOWN, e.message ?: "Failed to get categories")
                }
            }
        }
        
        override fun getCategoryContents(
            categoryId: String,
            cursor: String?,
            limit: Int,
            callback: IResultCallback
        ) {
            Log.d(TAG, "getCategoryContents() called: categoryId=$categoryId, cursor=$cursor, limit=$limit")
            scope.launch {
                try {
                    val result = getPlugin().getCategoryContents(categoryId, cursor, limit)
                    Log.d(TAG, "Category contents completed: songs=${result.songs.size}, albums=${result.albums.size}")
                    callback.onSearchResult(result)
                } catch (e: PluginException) {
                    Log.e(TAG, "Failed to get category contents: code=${e.errorCode}, message=${e.message}")
                    callback.onError(e.errorCode, e.message ?: "Failed to get category contents")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get category contents", e)
                    callback.onError(ErrorCodes.UNKNOWN, e.message ?: "Failed to get category contents")
                }
            }
        }
        
        // ==================== Library ====================
        
        override fun getLibrarySongs(cursor: String?, limit: Int, callback: IResultCallback) {
            Log.d(TAG, "getLibrarySongs() called: cursor=$cursor, limit=$limit")
            scope.launch {
                try {
                    val result = getPlugin().getLibrarySongs(cursor, limit)
                    Log.d(TAG, "Library songs completed: count=${result.items.size}, nextCursor=${result.nextCursor}")
                    callback.onSongsResult(result.items, result.nextCursor)
                } catch (e: PluginException) {
                    Log.e(TAG, "Failed to get library songs: code=${e.errorCode}, message=${e.message}")
                    callback.onError(e.errorCode, e.message ?: "Failed to get library songs")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get library songs", e)
                    callback.onError(ErrorCodes.UNKNOWN, e.message ?: "Failed to get library songs")
                }
            }
        }
        
        override fun getLibraryAlbums(cursor: String?, limit: Int, callback: IResultCallback) {
            Log.d(TAG, "getLibraryAlbums() called: cursor=$cursor, limit=$limit")
            scope.launch {
                try {
                    val result = getPlugin().getLibraryAlbums(cursor, limit)
                    Log.d(TAG, "Library albums completed: count=${result.items.size}, nextCursor=${result.nextCursor}")
                    callback.onAlbumsResult(result.items, result.nextCursor)
                } catch (e: PluginException) {
                    Log.e(TAG, "Failed to get library albums: code=${e.errorCode}, message=${e.message}")
                    callback.onError(e.errorCode, e.message ?: "Failed to get library albums")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get library albums", e)
                    callback.onError(ErrorCodes.UNKNOWN, e.message ?: "Failed to get library albums")
                }
            }
        }
        
        override fun getLibraryArtists(cursor: String?, limit: Int, callback: IResultCallback) {
            Log.d(TAG, "getLibraryArtists() called: cursor=$cursor, limit=$limit")
            scope.launch {
                try {
                    val result = getPlugin().getLibraryArtists(cursor, limit)
                    Log.d(TAG, "Library artists completed: count=${result.items.size}, nextCursor=${result.nextCursor}")
                    callback.onArtistsResult(result.items, result.nextCursor)
                } catch (e: PluginException) {
                    Log.e(TAG, "Failed to get library artists: code=${e.errorCode}, message=${e.message}")
                    callback.onError(e.errorCode, e.message ?: "Failed to get library artists")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get library artists", e)
                    callback.onError(ErrorCodes.UNKNOWN, e.message ?: "Failed to get library artists")
                }
            }
        }
        
        override fun getLibraryPlaylists(cursor: String?, limit: Int, callback: IResultCallback) {
            Log.d(TAG, "getLibraryPlaylists() called: cursor=$cursor, limit=$limit")
            scope.launch {
                try {
                    val result = getPlugin().getLibraryPlaylists(cursor, limit)
                    Log.d(TAG, "Library playlists completed: count=${result.items.size}, nextCursor=${result.nextCursor}")
                    callback.onPlaylistsResult(result.items, result.nextCursor)
                } catch (e: PluginException) {
                    Log.e(TAG, "Failed to get library playlists: code=${e.errorCode}, message=${e.message}")
                    callback.onError(e.errorCode, e.message ?: "Failed to get library playlists")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get library playlists", e)
                    callback.onError(ErrorCodes.UNKNOWN, e.message ?: "Failed to get library playlists")
                }
            }
        }
        
        // ==================== Details ====================
        
        override fun getSong(mediaId: MediaId, callback: IResultCallback) {
            Log.d(TAG, "getSong() called: mediaId=${mediaId.pluginId}:${mediaId.sourceId}")
            scope.launch {
                try {
                    val song = getPlugin().getSong(mediaId)
                    Log.d(TAG, "getSong() completed: title=${song.title}")
                    callback.onSongResult(song)
                } catch (e: PluginException) {
                    Log.e(TAG, "Failed to get song: code=${e.errorCode}, message=${e.message}")
                    callback.onError(e.errorCode, e.message ?: "Failed to get song")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get song", e)
                    callback.onError(ErrorCodes.UNKNOWN, e.message ?: "Failed to get song")
                }
            }
        }
        
        override fun getAlbum(mediaId: MediaId, callback: IResultCallback) {
            Log.d(TAG, "getAlbum() called: mediaId=${mediaId.pluginId}:${mediaId.sourceId}")
            scope.launch {
                try {
                    val album = getPlugin().getAlbum(mediaId)
                    Log.d(TAG, "getAlbum() completed: name=${album.name}")
                    callback.onAlbumResult(album)
                } catch (e: PluginException) {
                    Log.e(TAG, "Failed to get album: code=${e.errorCode}, message=${e.message}")
                    callback.onError(e.errorCode, e.message ?: "Failed to get album")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get album", e)
                    callback.onError(ErrorCodes.UNKNOWN, e.message ?: "Failed to get album")
                }
            }
        }
        
        override fun getArtist(mediaId: MediaId, callback: IResultCallback) {
            Log.d(TAG, "getArtist() called: mediaId=${mediaId.pluginId}:${mediaId.sourceId}")
            scope.launch {
                try {
                    val artist = getPlugin().getArtist(mediaId)
                    Log.d(TAG, "getArtist() completed: name=${artist.name}")
                    callback.onArtistResult(artist)
                } catch (e: PluginException) {
                    Log.e(TAG, "Failed to get artist: code=${e.errorCode}, message=${e.message}")
                    callback.onError(e.errorCode, e.message ?: "Failed to get artist")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get artist", e)
                    callback.onError(ErrorCodes.UNKNOWN, e.message ?: "Failed to get artist")
                }
            }
        }
        
        override fun getArtistSongs(
            artistId: MediaId,
            cursor: String?,
            limit: Int,
            callback: IResultCallback
        ) {
            Log.d(TAG, "getArtistSongs() called: artistId=${artistId.pluginId}:${artistId.sourceId}, cursor=$cursor, limit=$limit")
            scope.launch {
                try {
                    val result = getPlugin().getArtistSongs(artistId, cursor, limit)
                    Log.d(TAG, "getArtistSongs() completed: count=${result.items.size}, nextCursor=${result.nextCursor}")
                    callback.onSongsResult(result.items, result.nextCursor)
                } catch (e: PluginException) {
                    Log.e(TAG, "Failed to get artist songs: code=${e.errorCode}, message=${e.message}")
                    callback.onError(e.errorCode, e.message ?: "Failed to get artist songs")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get artist songs", e)
                    callback.onError(ErrorCodes.UNKNOWN, e.message ?: "Failed to get artist songs")
                }
            }
        }
        
        override fun getArtistAlbums(
            artistId: MediaId,
            cursor: String?,
            limit: Int,
            callback: IResultCallback
        ) {
            Log.d(TAG, "getArtistAlbums() called: artistId=${artistId.pluginId}:${artistId.sourceId}, cursor=$cursor, limit=$limit")
            scope.launch {
                try {
                    val result = getPlugin().getArtistAlbums(artistId, cursor, limit)
                    Log.d(TAG, "getArtistAlbums() completed: count=${result.items.size}, nextCursor=${result.nextCursor}")
                    callback.onAlbumsResult(result.items, result.nextCursor)
                } catch (e: PluginException) {
                    Log.e(TAG, "Failed to get artist albums: code=${e.errorCode}, message=${e.message}")
                    callback.onError(e.errorCode, e.message ?: "Failed to get artist albums")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get artist albums", e)
                    callback.onError(ErrorCodes.UNKNOWN, e.message ?: "Failed to get artist albums")
                }
            }
        }
        
        override fun getPlaylist(mediaId: MediaId, callback: IResultCallback) {
            Log.d(TAG, "getPlaylist() called: mediaId=${mediaId.pluginId}:${mediaId.sourceId}")
            scope.launch {
                try {
                    val playlist = getPlugin().getPlaylist(mediaId)
                    Log.d(TAG, "getPlaylist() completed: name=${playlist.name}")
                    callback.onPlaylistResult(playlist)
                } catch (e: PluginException) {
                    Log.e(TAG, "Failed to get playlist: code=${e.errorCode}, message=${e.message}")
                    callback.onError(e.errorCode, e.message ?: "Failed to get playlist")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get playlist", e)
                    callback.onError(ErrorCodes.UNKNOWN, e.message ?: "Failed to get playlist")
                }
            }
        }
        
        override fun getPlaylistSongs(
            playlistId: MediaId,
            cursor: String?,
            limit: Int,
            callback: IResultCallback
        ) {
            Log.d(TAG, "getPlaylistSongs() called: playlistId=${playlistId.pluginId}:${playlistId.sourceId}, cursor=$cursor, limit=$limit")
            scope.launch {
                try {
                    val result = getPlugin().getPlaylistSongs(playlistId, cursor, limit)
                    Log.d(TAG, "getPlaylistSongs() completed: count=${result.items.size}, nextCursor=${result.nextCursor}")
                    callback.onSongsResult(result.items, result.nextCursor)
                } catch (e: PluginException) {
                    Log.e(TAG, "Failed to get playlist songs: code=${e.errorCode}, message=${e.message}")
                    callback.onError(e.errorCode, e.message ?: "Failed to get playlist songs")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get playlist songs", e)
                    callback.onError(ErrorCodes.UNKNOWN, e.message ?: "Failed to get playlist songs")
                }
            }
        }
        
        // ==================== Audio Streaming ====================
        
        override fun getAudioStream(mediaId: MediaId, callback: IResultCallback) {
            Log.d(TAG, "getAudioStream() called: mediaId=${mediaId.pluginId}:${mediaId.sourceId}")
            scope.launch {
                try {
                    val writer = getPlugin().getAudioStream(mediaId)
                    activeStreams[writer.streamId] = writer
                    Log.d(TAG, "Audio stream created: streamId=${writer.streamId}, format=${writer.format}, duration=${writer.durationMs}ms")
                    callback.onAudioStreamReady(writer.audioStream)
                } catch (e: PluginException) {
                    Log.e(TAG, "Failed to get audio stream: code=${e.errorCode}, message=${e.message}")
                    callback.onAudioStreamError(e.errorCode, e.message ?: "Failed to get audio stream")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get audio stream", e)
                    callback.onAudioStreamError(ErrorCodes.UNKNOWN, e.message ?: "Failed to get audio stream")
                }
            }
        }
        
        override fun stopAudioStream(streamId: String) {
            Log.d(TAG, "stopAudioStream() called: streamId=$streamId")
            scope.launch {
                try {
                    activeStreams.remove(streamId)?.close()
                    getPlugin().stopAudioStream(streamId)
                    Log.d(TAG, "Audio stream stopped: streamId=$streamId")
                } catch (e: Exception) {
                    Log.w(TAG, "Error stopping audio stream: streamId=$streamId, ignoring", e)
                }
            }
        }
        
        override fun seekAudioStream(streamId: String, positionMs: Long): Boolean {
            Log.d(TAG, "seekAudioStream() called: streamId=$streamId, positionMs=$positionMs")
            return runBlocking {
                try {
                    val result = getPlugin().seekAudioStream(streamId, positionMs)
                    Log.d(TAG, "seekAudioStream() result: $result")
                    result
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to seek audio stream: streamId=$streamId", e)
                    false
                }
            }
        }
        
        // ==================== Settings ====================
        
        override fun getSettingsActivityClass(): String? {
            Log.d(TAG, "getSettingsActivityClass() called")
            val className = getPlugin().getSettingsActivityClass()
            Log.d(TAG, "getSettingsActivityClass() returned: $className")
            return className
        }
        
        override fun getSettingsViewClass(): String? {
            Log.d(TAG, "getSettingsViewClass() called")
            val className = getPlugin().getSettingsViewClass()
            Log.d(TAG, "getSettingsViewClass() returned: $className")
            return className
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind() called for service: ${this::class.java.name}")
        Log.d(TAG, "Intent details: action=${intent?.action}, component=${intent?.component}, package=${intent?.`package`}")
        
        // Accept ALL bindings to this service - if someone is binding to us, they want the plugin service
        // The action check is mainly for implicit bindings, but explicit component bindings are always valid
        val hasPluginAction = intent?.action == PluginConstants.ACTION_PLUGIN_SERVICE
        val hasExplicitComponent = intent?.component != null
        val actionIsNull = intent?.action == null
        
        // Accept if:
        // 1. Action matches plugin service action, OR
        // 2. Explicit component is set (explicit binding), OR  
        // 3. Action is null but we're being bound to (Android sometimes does this with explicit components)
        if (hasPluginAction || hasExplicitComponent || actionIsNull) {
            Log.d(TAG, "Accepting binding (action=$hasPluginAction, explicitComponent=$hasExplicitComponent, actionNull=$actionIsNull), returning binder")
            Log.d(TAG, "Returning binder for plugin service")
            return binder
        }
        
        Log.w(TAG, "Rejecting binding - doesn't match criteria")
        Log.w(TAG, "Expected action: ${PluginConstants.ACTION_PLUGIN_SERVICE}, got: ${intent?.action}")
        Log.w(TAG, "Component: ${intent?.component}")
        return null
    }
    
    override fun onDestroy() {
        Log.d(TAG, "onDestroy() called")
        scope.cancel()
        Log.d(TAG, "Closing ${activeStreams.size} active streams")
        activeStreams.values.forEach { it.close() }
        activeStreams.clear()
        super.onDestroy()
        Log.d(TAG, "onDestroy() completed")
    }
}

