package com.viperplayer.plugin.author

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.viperplayer.plugin.ipc.IInitCallback
import com.viperplayer.plugin.ipc.IResultCallback
import com.viperplayer.plugin.ipc.IViperHost
import com.viperplayer.plugin.ipc.IViperPlugin
import com.viperplayer.plugin.model.CategoryContentsRequest
import com.viperplayer.plugin.model.DspCapabilities
import com.viperplayer.plugin.model.DspClose
import com.viperplayer.plugin.model.DspConfigure
import com.viperplayer.plugin.model.DspOpenRequest
import com.viperplayer.plugin.model.DspOpenResponse
import com.viperplayer.plugin.model.HostHandshake
import com.viperplayer.plugin.model.IdPageRequest
import com.viperplayer.plugin.model.IdRequest
import com.viperplayer.plugin.model.Lyrics
import com.viperplayer.plugin.model.PageRequest
import com.viperplayer.plugin.model.PlaybackEvent
import com.viperplayer.plugin.model.PluginErrorBody
import com.viperplayer.plugin.model.PluginErrorCode
import com.viperplayer.plugin.model.PluginException
import com.viperplayer.plugin.model.QueryRequest
import com.viperplayer.plugin.model.SeekStreamRequest
import com.viperplayer.plugin.model.BoolResult
import com.viperplayer.plugin.model.SearchRequest
import com.viperplayer.plugin.model.Song
import com.viperplayer.plugin.protocol.Envelope
import com.viperplayer.plugin.protocol.Protocol
import com.viperplayer.plugin.protocol.Verbs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

/**
 * Base [Service] every plugin extends. It owns the entire IPC story — handshake, verb dispatch,
 * (de)serialization, paging, cancellation, error mapping, and the DSP shared-memory runtime — so a
 * plugin author writes only typed providers and registers them here.
 *
 * ```
 * class ExampleService : ViperPluginService() {
 *     override fun onCreatePlugin(context: Context) = pluginRegistration(
 *         id = "com.example.musicsource", name = "Example Source", version = "1.0",
 *     ) {
 *         source(ExampleSource(context), SourceCapabilities(home = true, searchSuggestions = true))
 *     }
 * }
 * ```
 *
 * Manifest:
 * ```
 * <service android:name=".ExampleService" android:exported="true">
 *     <intent-filter><action android:name="com.viperplayer.plugin.ViperPluginService"/></intent-filter>
 * </service>
 * ```
 */
abstract class ViperPluginService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val requests = ConcurrentHashMap<Long, Job>()
    private val dspSessions = ConcurrentHashMap<String, Any>()

    private lateinit var registration: PluginRegistration

    /** Build the plugin's registration — its identity and the providers it offers. */
    protected abstract fun onCreatePlugin(context: Context): PluginRegistration

    override fun onCreate() {
        super.onCreate()
        registration = onCreatePlugin(this)
    }

    final override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private val binder = object : IViperPlugin.Stub() {

        override fun initialize(hostInfo: Bundle?, host: IViperHost?, callback: IInitCallback?) {
            if (host == null || callback == null) return
            val handshake = hostInfo?.let { Envelope.payloadOrNull<HostHandshake>(it) }
                ?: HostHandshake(Protocol.VERSION, "", emptySet())
            val pluginHost = PluginHost(host, handshake)
            val onConnect = registration.onConnect
            if (onConnect == null) {
                callback.onInitialized(Envelope.of(registration.toManifest()))
                return
            }
            scope.launch {
                try {
                    onConnect(pluginHost)
                    callback.onInitialized(Envelope.of(registration.toManifest()))
                } catch (e: Throwable) {
                    callback.onInitializationFailed(errorCodeOf(e), e.message)
                }
            }
        }

        override fun invoke(verb: String?, requestId: Long, args: Bundle?, callback: IResultCallback?) {
            if (verb == null || callback == null) return
            val argsBundle = args ?: Bundle()
            // Register the Job before starting it: a fast verb could otherwise reach the finally{}
            // remove() before we put it in the map, leaving a stale entry that's never removed (and
            // a cancel() in that window would find nothing to cancel).
            val job = scope.launch(start = CoroutineStart.LAZY) {
                try {
                    dispatch(verb, argsBundle, callback)
                } catch (c: CancellationException) {
                    throw c
                } catch (e: Throwable) {
                    callback.onError(errorCodeOf(e), e.message, errorExtras(e))
                } finally {
                    requests.remove(requestId)
                }
            }
            requests[requestId] = job
            job.start()
        }

        override fun cancel(requestId: Long) {
            requests.remove(requestId)?.cancel()
        }

        override fun shutdown() {
            runCatching { registration.onShutdown?.invoke() }
            closeAllDspSessions()
            requests.values.forEach { it.cancel() }
            requests.clear()
        }
    }

    private suspend fun dispatch(verb: String, args: Bundle, cb: IResultCallback) {
        when (verb) {
            // ---- Source: search & detail ----
            Verbs.Source.SEARCH ->
                cb.onComplete(Envelope.of(source().search(Envelope.payload<SearchRequest>(args))))
            Verbs.Source.SEARCH_SUGGESTIONS ->
                cb.onComplete(Envelope.of(source().getSearchSuggestions(Envelope.payload<QueryRequest>(args).query)))
            Verbs.Source.GET_SONG ->
                cb.onComplete(Envelope.of(source().getSong(idOf(args))))
            Verbs.Source.GET_ALBUM ->
                cb.onComplete(Envelope.of(source().getAlbum(idOf(args))))
            Verbs.Source.GET_ARTIST ->
                cb.onComplete(Envelope.of(source().getArtist(idOf(args))))
            Verbs.Source.GET_PLAYLIST ->
                cb.onComplete(Envelope.of(source().getPlaylist(idOf(args))))

            // ---- Source: paged relations ----
            Verbs.Source.ARTIST_SONGS -> idPage(args).let {
                cb.onComplete(Envelope.of(source().getArtistSongs(it.id, it.page)))
            }
            Verbs.Source.ARTIST_ALBUMS -> idPage(args).let {
                cb.onComplete(Envelope.of(source().getArtistAlbums(it.id, it.page)))
            }
            Verbs.Source.PLAYLIST_SONGS -> idPage(args).let {
                cb.onComplete(Envelope.of(source().getPlaylistSongs(it.id, it.page)))
            }

            // ---- Source: library ----
            Verbs.Source.LIBRARY_SONGS ->
                cb.onComplete(Envelope.of(source().getLibrarySongs(pageOf(args))))
            Verbs.Source.LIBRARY_ALBUMS ->
                cb.onComplete(Envelope.of(source().getLibraryAlbums(pageOf(args))))
            Verbs.Source.LIBRARY_ARTISTS ->
                cb.onComplete(Envelope.of(source().getLibraryArtists(pageOf(args))))
            Verbs.Source.LIBRARY_PLAYLISTS ->
                cb.onComplete(Envelope.of(source().getLibraryPlaylists(pageOf(args))))

            // ---- Source: browse & home ----
            Verbs.Source.BROWSE_CATEGORIES ->
                cb.onComplete(Envelope.of(source().getBrowseCategories(pageOf(args))))
            Verbs.Source.CATEGORY_CONTENTS -> Envelope.payload<CategoryContentsRequest>(args).let {
                cb.onComplete(Envelope.of(source().getCategoryContents(it.categoryId, it.page)))
            }
            Verbs.Source.HOME ->
                cb.onComplete(Envelope.of(source().getHome()))

            // ---- Source: streaming ----
            Verbs.Source.RESOLVE_STREAM -> {
                val response = source().resolveStream(idOf(args))
                cb.onComplete(Envelope.of(response.source, fd = response.fd))
                // onComplete already dup'd the read end into the host; close our local copy so the
                // plugin process doesn't leak a descriptor per resolved PCM stream.
                response.fd?.let { runCatching { it.close() } }
            }
            Verbs.Source.SEEK_STREAM -> Envelope.payload<SeekStreamRequest>(args).let {
                cb.onComplete(Envelope.of(BoolResult(source().seekStream(it.streamId, it.positionMs))))
            }
            Verbs.Source.CLOSE_STREAM -> {
                source().closeStream(idOf(args))
                cb.onComplete(Envelope.empty())
            }

            // ---- Lyrics ----
            Verbs.Lyrics.GET -> {
                val lyrics: Lyrics? = lyrics().getLyrics(Envelope.payload(args))
                cb.onComplete(Envelope.of(lyrics))
            }

            // ---- Scrobble ----
            Verbs.Scrobble.NOW_PLAYING -> {
                scrobble().onNowPlaying(Envelope.payload<PlaybackEvent>(args))
                cb.onComplete(Envelope.empty())
            }
            Verbs.Scrobble.SUBMIT -> {
                scrobble().onPlaybackEvent(Envelope.payload<PlaybackEvent>(args))
                cb.onComplete(Envelope.empty())
            }

            // ---- Metadata ----
            Verbs.Metadata.ENRICH ->
                cb.onComplete(Envelope.of(metadata().enrich(Envelope.payload<Song>(args))))

            // ---- DSP ----
            Verbs.Dsp.DESCRIBE ->
                cb.onComplete(Envelope.of(DspCapabilities(dsp().descriptors)))
            Verbs.Dsp.OPEN ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) handleDspOpen(args, cb)
                else cb.onError(PluginErrorCode.UNSUPPORTED, "DSP requires Android 8.1+", null)
            Verbs.Dsp.CONFIGURE ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) handleDspConfigure(args, cb)
                else cb.onError(PluginErrorCode.UNSUPPORTED, "DSP requires Android 8.1+", null)
            Verbs.Dsp.CLOSE ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) handleDspClose(args, cb)
                else cb.onError(PluginErrorCode.UNSUPPORTED, "DSP requires Android 8.1+", null)

            else -> cb.onError(PluginErrorCode.UNSUPPORTED, "Unknown verb: $verb", null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    private fun handleDspOpen(args: Bundle, cb: IResultCallback) {
        val provider = registration.dsp
            ?: throw PluginException(PluginErrorCode.UNSUPPORTED, "No DSP provider")
        val request = Envelope.payload<DspOpenRequest>(args)
        // These are dup'd handles we now own; close them if the open can't complete.
        val inputShm = Envelope.sharedMemory(args)
        val outputShm = Envelope.sharedMemory2(args)
        val control = Envelope.fd(args)
        if (inputShm == null || outputShm == null || control == null) {
            runCatching { inputShm?.close() }
            runCatching { outputShm?.close() }
            runCatching { control?.close() }
            throw PluginException(PluginErrorCode.INVALID_REQUEST, "Missing DSP shared memory / control descriptor")
        }

        val sessionId = UUID.randomUUID().toString()
        val runtime = try {
            val session = provider.createSession(request.format, request.maxFramesPerBlock)
            DspRuntime(
                session = session,
                channels = request.format.channelCount,
                maxFramesPerBlock = request.maxFramesPerBlock,
                inputShm = inputShm,
                outputShm = outputShm,
                control = control,
            )
        } catch (e: Throwable) {
            // The DspRuntime now owns these once constructed; on failure before that, close them.
            runCatching { inputShm.close() }
            runCatching { outputShm.close() }
            runCatching { control.close() }
            throw e
        }
        dspSessions[sessionId] = runtime
        runtime.start()
        cb.onComplete(Envelope.of(DspOpenResponse(sessionId)))
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    private fun handleDspConfigure(args: Bundle, cb: IResultCallback) {
        val request = Envelope.payload<DspConfigure>(args)
        (dspSessions[request.sessionId] as? DspRuntime)?.configure(request.params, request.enabled)
        cb.onComplete(Envelope.empty())
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    private fun handleDspClose(args: Bundle, cb: IResultCallback) {
        val request = Envelope.payload<DspClose>(args)
        (dspSessions.remove(request.sessionId) as? DspRuntime)?.stop()
        cb.onComplete(Envelope.empty())
    }

    private fun closeAllDspSessions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return
        dspSessions.values.forEach { (it as? DspRuntime)?.stop() }
        dspSessions.clear()
    }

    // ---- provider accessors ----

    private fun source(): SourceProvider = registration.source
        ?: throw PluginException(PluginErrorCode.UNSUPPORTED, "Plugin has no source provider")

    private fun lyrics(): LyricsProvider = registration.lyrics
        ?: throw PluginException(PluginErrorCode.UNSUPPORTED, "Plugin has no lyrics provider")

    private fun scrobble(): ScrobbleSink = registration.scrobble
        ?: throw PluginException(PluginErrorCode.UNSUPPORTED, "Plugin has no scrobble sink")

    private fun metadata(): MetadataProvider = registration.metadata
        ?: throw PluginException(PluginErrorCode.UNSUPPORTED, "Plugin has no metadata provider")

    private fun dsp(): DspProvider = registration.dsp
        ?: throw PluginException(PluginErrorCode.UNSUPPORTED, "Plugin has no DSP provider")

    // ---- arg helpers ----

    private fun idOf(args: Bundle): String = Envelope.payload<IdRequest>(args).id
    private fun pageOf(args: Bundle): PageRequest = Envelope.payloadOrNull<PageRequest>(args) ?: PageRequest()
    private fun idPage(args: Bundle): IdPageRequest = Envelope.payload(args)

    private fun errorCodeOf(e: Throwable): Int =
        (e as? PluginException)?.code ?: PluginErrorCode.INTERNAL

    private fun errorExtras(e: Throwable): Bundle {
        val body = (e as? PluginException)?.toBody()
            ?: PluginErrorBody(PluginErrorCode.INTERNAL, e.message)
        return Envelope.of(body)
    }
}
