package com.viperplayer.plugin.host

import com.viperplayer.plugin.model.Album
import com.viperplayer.plugin.model.Artist
import com.viperplayer.plugin.model.BoolResult
import com.viperplayer.plugin.model.BrowseCategory
import com.viperplayer.plugin.model.Capability
import com.viperplayer.plugin.model.CategoryContentsRequest
import com.viperplayer.plugin.model.CreatePlaylistRequest
import com.viperplayer.plugin.model.CreatePlaylistResult
import com.viperplayer.plugin.model.FilterSectionRequest
import com.viperplayer.plugin.model.HomeContent
import com.viperplayer.plugin.model.HomeContinuationRequest
import com.viperplayer.plugin.model.HomeRequest
import com.viperplayer.plugin.model.IdPageRequest
import com.viperplayer.plugin.model.IdRequest
import com.viperplayer.plugin.model.Lyrics
import com.viperplayer.plugin.model.LyricsRequest
import com.viperplayer.plugin.model.MediaType
import com.viperplayer.plugin.model.ResolveStreamRequest
import com.viperplayer.plugin.model.PluginErrorCode
import com.viperplayer.plugin.model.PluginException
import com.viperplayer.plugin.model.PluginManifest
import com.viperplayer.plugin.model.PluginStatus
import com.viperplayer.plugin.model.Page
import com.viperplayer.plugin.model.PageRequest
import com.viperplayer.plugin.model.PlaybackEvent
import com.viperplayer.plugin.model.Playlist
import com.viperplayer.plugin.model.PlaylistRequest
import com.viperplayer.plugin.model.PlaylistTrackRequest
import com.viperplayer.plugin.model.QueryRequest
import com.viperplayer.plugin.model.RenamePlaylistRequest
import com.viperplayer.plugin.model.SetFollowedRequest
import com.viperplayer.plugin.model.SetLikedRequest
import com.viperplayer.plugin.model.SearchRequest
import com.viperplayer.plugin.model.SearchResult
import com.viperplayer.plugin.model.SearchSuggestions
import com.viperplayer.plugin.model.SeekStreamRequest
import com.viperplayer.plugin.model.Song
import com.viperplayer.plugin.model.SourceCapabilities
import com.viperplayer.plugin.model.StreamSource
import com.viperplayer.plugin.protocol.Envelope
import com.viperplayer.plugin.protocol.Verbs

/**
 * The host's typed entry point to a connected plugin. Sub-clients are present only when the plugin
 * advertised the matching capability, so `client.source?.search(...)` naturally degrades to a no-op
 * for plugins that don't provide that surface.
 */
class PluginClient internal constructor(
    private val connection: PluginConnection,
    val manifest: PluginManifest,
) {
    /**
     * The plugin's pending user actions (permission, login, verification...). Plugins predating
     * the status verb answer UNSUPPORTED, which degrades to an empty (all-good) status.
     */
    suspend fun getStatus(): PluginStatus = try {
        Envelope.payload(connection.invokeRaw(Verbs.Status.GET, Envelope.empty()).result)
    } catch (e: PluginException) {
        if (e.code == PluginErrorCode.UNSUPPORTED) PluginStatus() else throw e
    }

    val source: SourceClient? =
        manifest.source?.let { SourceClient(connection, it) }

    val lyrics: LyricsClient? =
        if (Capability.LYRICS in manifest.capabilities) LyricsClient(connection) else null

    val scrobble: ScrobbleClient? =
        if (Capability.SCROBBLE in manifest.capabilities) ScrobbleClient(connection) else null

    val metadata: MetadataClient? =
        if (Capability.METADATA in manifest.capabilities) MetadataClient(connection) else null

    val dsp: DspClient? =
        manifest.dsp?.let { DspClient(connection, it) }
}

class SourceClient internal constructor(
    private val connection: PluginConnection,
    val capabilities: SourceCapabilities,
) {
    suspend fun search(request: SearchRequest): SearchResult =
        Envelope.payload(connection.invokeRaw(Verbs.Source.SEARCH, Envelope.of(request)).result)

    suspend fun getSearchSuggestions(query: String): SearchSuggestions =
        Envelope.payload(
            connection.invokeRaw(Verbs.Source.SEARCH_SUGGESTIONS, Envelope.of(QueryRequest(query))).result
        )

    suspend fun getSong(id: String): Song =
        Envelope.payload(connection.invokeRaw(Verbs.Source.GET_SONG, Envelope.of(IdRequest(id))).result)

    suspend fun getAlbum(id: String): Album =
        Envelope.payload(connection.invokeRaw(Verbs.Source.GET_ALBUM, Envelope.of(IdRequest(id))).result)

    suspend fun getArtist(id: String): Artist =
        Envelope.payload(connection.invokeRaw(Verbs.Source.GET_ARTIST, Envelope.of(IdRequest(id))).result)

    suspend fun getPlaylist(id: String): Playlist =
        Envelope.payload(connection.invokeRaw(Verbs.Source.GET_PLAYLIST, Envelope.of(IdRequest(id))).result)

    suspend fun getArtistSongs(artistId: String, page: PageRequest = PageRequest()): Page<Song> =
        Envelope.payload(connection.invokeRaw(Verbs.Source.ARTIST_SONGS, Envelope.of(IdPageRequest(artistId, page))).result)

    suspend fun getArtistAlbums(artistId: String, page: PageRequest = PageRequest()): Page<Album> =
        Envelope.payload(connection.invokeRaw(Verbs.Source.ARTIST_ALBUMS, Envelope.of(IdPageRequest(artistId, page))).result)

    suspend fun getPlaylistSongs(playlistId: String, page: PageRequest = PageRequest()): Page<Song> =
        Envelope.payload(connection.invokeRaw(Verbs.Source.PLAYLIST_SONGS, Envelope.of(IdPageRequest(playlistId, page))).result)

    suspend fun getRelatedSongs(songId: String): Page<Song> =
        Envelope.payload(connection.invokeRaw(Verbs.Source.RELATED_SONGS, Envelope.of(IdRequest(songId))).result)

    suspend fun getLibrarySongs(page: PageRequest = PageRequest()): Page<Song> =
        Envelope.payload(connection.invokeRaw(Verbs.Source.LIBRARY_SONGS, Envelope.of(page)).result)

    suspend fun getLibraryAlbums(page: PageRequest = PageRequest()): Page<Album> =
        Envelope.payload(connection.invokeRaw(Verbs.Source.LIBRARY_ALBUMS, Envelope.of(page)).result)

    suspend fun getLibraryArtists(page: PageRequest = PageRequest()): Page<Artist> =
        Envelope.payload(connection.invokeRaw(Verbs.Source.LIBRARY_ARTISTS, Envelope.of(page)).result)

    suspend fun getLibraryPlaylists(page: PageRequest = PageRequest()): Page<Playlist> =
        Envelope.payload(connection.invokeRaw(Verbs.Source.LIBRARY_PLAYLISTS, Envelope.of(page)).result)

    suspend fun getBrowseCategories(page: PageRequest = PageRequest()): Page<BrowseCategory> =
        Envelope.payload(connection.invokeRaw(Verbs.Source.BROWSE_CATEGORIES, Envelope.of(page)).result)

    suspend fun getCategoryContents(categoryId: String, page: PageRequest = PageRequest()): SearchResult =
        Envelope.payload(connection.invokeRaw(Verbs.Source.CATEGORY_CONTENTS, Envelope.of(CategoryContentsRequest(categoryId, page))).result)

    suspend fun getHome(): HomeContent =
        Envelope.payload(connection.invokeRaw(Verbs.Source.HOME, Envelope.empty()).result)

    /**
     * Re-fetch the Home feed filtered by a top-level chip. A plugin (or older APK) that doesn't
     * implement the chip verb answers UNSUPPORTED; the caller (host data source) catches that and
     * falls back to the base [getHome].
     */
    suspend fun getHome(chipId: String?): HomeContent =
        Envelope.payload(connection.invokeRaw(Verbs.Source.HOME_FILTERED, Envelope.of(HomeRequest(chipId))).result)

    /**
     * Fetch the next page of Home sections for a continuation token (infinite scroll). A plugin (or
     * older APK) without the verb answers UNSUPPORTED; the caller degrades to "no more pages".
     */
    suspend fun getHomeContinuation(continuation: String): HomeContent =
        Envelope.payload(connection.invokeRaw(Verbs.Source.HOME_CONTINUATION, Envelope.of(HomeContinuationRequest(continuation))).result)

    suspend fun filterSection(sectionId: String, filterKey: String): SearchResult =
        Envelope.payload(connection.invokeRaw(Verbs.Source.FILTER_SECTION, Envelope.of(FilterSectionRequest(sectionId, filterKey))).result)

    suspend fun resolveStream(songId: String, type: MediaType, maxBitrateKbps: Int? = null): ResolvedStream {
        val result = connection.invokeRaw(
            Verbs.Source.RESOLVE_STREAM, Envelope.of(ResolveStreamRequest(songId, type, maxBitrateKbps))
        ).result
        return ResolvedStream(Envelope.payload<StreamSource>(result), Envelope.fd(result))
    }

    suspend fun seekStream(streamId: String, positionMs: Long): Boolean =
        Envelope.payload<BoolResult>(connection.invokeRaw(Verbs.Source.SEEK_STREAM, Envelope.of(SeekStreamRequest(streamId, positionMs))).result).value

    suspend fun closeStream(streamId: String) {
        connection.invokeRaw(Verbs.Source.CLOSE_STREAM, Envelope.of(IdRequest(streamId)))
    }

    // ---- Library WRITE (push) — the host side of two-way sync. Gated on
    // [SourceCapabilities.libraryWrite]; a plugin that predates these verbs (or doesn't implement
    // them) answers UNSUPPORTED, which the push sync manager turns into a "kept/skipped" outbox
    // entry rather than an error. ----

    suspend fun setLiked(trackId: String, liked: Boolean) {
        connection.invokeRaw(Verbs.Source.SET_LIKED, Envelope.of(SetLikedRequest(trackId, liked)))
    }

    suspend fun setFollowed(artistId: String, followed: Boolean) {
        connection.invokeRaw(Verbs.Source.SET_FOLLOWED, Envelope.of(SetFollowedRequest(artistId, followed)))
    }

    suspend fun createPlaylist(name: String): String =
        Envelope.payload<CreatePlaylistResult>(
            connection.invokeRaw(Verbs.Source.PLAYLIST_CREATE, Envelope.of(CreatePlaylistRequest(name))).result
        ).playlistId

    suspend fun renamePlaylist(playlistId: String, name: String) {
        connection.invokeRaw(Verbs.Source.PLAYLIST_RENAME, Envelope.of(RenamePlaylistRequest(playlistId, name)))
    }

    suspend fun deletePlaylist(playlistId: String) {
        connection.invokeRaw(Verbs.Source.PLAYLIST_DELETE, Envelope.of(PlaylistRequest(playlistId)))
    }

    suspend fun addTrackToPlaylist(playlistId: String, trackId: String) {
        connection.invokeRaw(Verbs.Source.PLAYLIST_ADD_TRACK, Envelope.of(PlaylistTrackRequest(playlistId, trackId)))
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        connection.invokeRaw(Verbs.Source.PLAYLIST_REMOVE_TRACK, Envelope.of(PlaylistTrackRequest(playlistId, trackId)))
    }
}

class LyricsClient internal constructor(private val connection: PluginConnection) {
    suspend fun getLyrics(request: LyricsRequest): Lyrics? =
        Envelope.payload(connection.invokeRaw(Verbs.Lyrics.GET, Envelope.of(request)).result)
}

class ScrobbleClient internal constructor(private val connection: PluginConnection) {
    suspend fun nowPlaying(event: PlaybackEvent) {
        connection.invokeRaw(Verbs.Scrobble.NOW_PLAYING, Envelope.of(event))
    }

    suspend fun submit(event: PlaybackEvent) {
        connection.invokeRaw(Verbs.Scrobble.SUBMIT, Envelope.of(event))
    }
}

class MetadataClient internal constructor(private val connection: PluginConnection) {
    suspend fun enrich(song: Song): Song =
        Envelope.payload(connection.invokeRaw(Verbs.Metadata.ENRICH, Envelope.of(song)).result)
}
