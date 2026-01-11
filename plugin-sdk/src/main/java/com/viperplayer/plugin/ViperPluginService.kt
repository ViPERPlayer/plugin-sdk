package com.viperplayer.plugin

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.viperplayer.plugin.v1.IAlbumCallback
import com.viperplayer.plugin.v1.IAlbumsCallback
import com.viperplayer.plugin.v1.IArtistCallback
import com.viperplayer.plugin.v1.IArtistsCallback
import com.viperplayer.plugin.v1.ICategoriesCallback
import com.viperplayer.plugin.v1.IHostCallbackV1
import com.viperplayer.plugin.v1.IPlaylistCallback
import com.viperplayer.plugin.v1.IPlaylistsCallback
import com.viperplayer.plugin.v1.ISearchCallback
import com.viperplayer.plugin.v1.ISearchSuggestionsCallback
import com.viperplayer.plugin.v1.ISongCallback
import com.viperplayer.plugin.v1.ISongsCallback
import com.viperplayer.plugin.v1.IStreamSourceCallback
import com.viperplayer.plugin.v1.IViperPluginV1
import com.viperplayer.plugin.v1.PluginCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private lateinit var plugin: ViperPlugin

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")
        plugin = createPlugin(this)
    }
    
    /**
     * Create your plugin implementation.
     */
    protected abstract fun createPlugin(context: Context): ViperPlugin

    private val binder = object : IViperPluginV1.Stub() {
        override fun onConnect(hostCallback: IHostCallbackV1?, callback: IConnectCallback) {
            if (hostCallback == null) return
            scope.launch {
                try {
                    plugin.onConnect(hostCallback)
                    callback.onSuccess()
                } catch (e: Exception) {
                    callback.onFailure(ErrorCodes.UNKNOWN, e.message)
                }
            }
        }

        override fun onDisconnect() {
            scope.launch { plugin.onDisconnect() }
        }

        override fun getCapabilities(): PluginCapabilities {
            return plugin.capabilities
        }

        override fun getSearchSuggestions(
            query: String?,
            callback: ISearchSuggestionsCallback?
        ) {
            if (query == null || callback == null) {
                callback?.onFailure(ErrorCodes.INVALID_REQUEST, "Invalid request")
                return
            }
            scope.launch {
                try {
                    callback.onSuccess(plugin.getSearchSuggestions(query))
                } catch (e: Exception) {
                    callback.onFailure(ErrorCodes.UNKNOWN, e.message)
                }
            }
        }

        override fun search(
            query: String?,
            types: Int,
            cursor: String?,
            limit: Int,
            callback: ISearchCallback?
        ) {
            if (query == null || callback == null) return
            scope.launch {
                try {
                    callback.onSuccess(plugin.search(query, types, cursor, limit))
                } catch (e: Exception) {
                    callback.onFailure(ErrorCodes.UNKNOWN, e.message)
                }
            }
        }

        override fun getBrowseCategories(
            cursor: String?,
            limit: Int,
            callback: ICategoriesCallback?
        ) {
            callback?.onSuccess(emptyList(), null)
        }

        override fun getCategoryContents(
            categoryId: String?,
            cursor: String?,
            limit: Int,
            callback: ISearchCallback?
        ) {
            callback?.onSuccess(null)
        }

        override fun getLibrarySongs(
            cursor: String?,
            limit: Int,
            callback: ISongsCallback?
        ) {
            callback?.onSuccess(emptyList(), null)
        }

        override fun getLibraryAlbums(
            cursor: String?,
            limit: Int,
            callback: IAlbumsCallback?
        ) {
            callback?.onSuccess(emptyList(), null)
        }

        override fun getLibraryArtists(
            cursor: String?,
            limit: Int,
            callback: IArtistsCallback?
        ) {
            callback?.onSuccess(emptyList(), null)
        }

        override fun getLibraryPlaylists(
            cursor: String?,
            limit: Int,
            callback: IPlaylistsCallback?
        ) {
            callback?.onSuccess(emptyList(), null)
        }

        override fun getSong(
            id: String?,
            callback: ISongCallback?
        ) {
            callback?.onSuccess(null)
        }

        override fun getAlbum(
            id: String?,
            callback: IAlbumCallback?
        ) {
            if (id == null || callback == null) return
            scope.launch {
                try {
                    callback.onSuccess(plugin.getAlbum(id))
                } catch (e: Exception) {
                    callback.onFailure(ErrorCodes.UNKNOWN, e.message)
                }
            }
        }

        override fun getArtist(
            id: String?,
            callback: IArtistCallback?
        ) {
            if (id == null || callback == null) return
            scope.launch {
                try {
                    callback.onSuccess(plugin.getArtist(id))
                } catch (e: Exception) {
                    callback.onFailure(ErrorCodes.UNKNOWN, e.message)
                }
            }
        }

        override fun getArtistSongs(
            artistId: String?,
            cursor: String?,
            limit: Int,
            callback: ISongsCallback?
        ) {
            callback?.onSuccess(emptyList(), null)
        }

        override fun getArtistAlbums(
            artistId: String?,
            cursor: String?,
            limit: Int,
            callback: IAlbumsCallback?
        ) {
            callback?.onSuccess(emptyList(), null)
        }

        override fun getPlaylist(
            mediaId: String?,
            callback: IPlaylistCallback?
        ) {
            if (mediaId == null || callback == null) return
            scope.launch {
                try {
                    callback.onSuccess(plugin.getPlaylist(mediaId))
                } catch (e: Exception) {
                    callback.onFailure(ErrorCodes.UNKNOWN, e.message)
                }
            }
        }

        override fun getPlaylistSongs(
            playlistId: String?,
            cursor: String?,
            limit: Int,
            callback: ISongsCallback?
        ) {
            callback?.onSuccess(emptyList(), null)
        }

        override fun getStream(
            mediaId: String?,
            callback: IStreamSourceCallback?
        ) {
            if (mediaId == null || callback == null) return
            scope.launch {
                try {
                    val streamSource = plugin.getStream(mediaId)
                    callback.onSuccess(streamSource)
                } catch (e: Exception) {
                    // Error handling is done via PluginException in the callback
                    Log.e(TAG, "Error getting stream", e)
                    // Note: IStreamSourceCallback doesn't have onFailure, so we can't report errors
                    // The plugin should handle errors internally
                }
            }
        }


        override fun stopAudioStream(streamId: String?) {
            TODO("Not yet implemented")
        }

        override fun seekAudioStream(
            streamId: String?,
            positionMs: Long
        ): Boolean {
            TODO("Not yet implemented")
        }
    }
    
    override fun onBind(intent: Intent): IBinder? {
        return binder
    }
    
    override fun onDestroy() {
        Log.d(TAG, "onDestroy() called")
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ViperPluginService"
    }
}
