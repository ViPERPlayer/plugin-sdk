package com.viperplayer.plugin.author

import com.viperplayer.plugin.model.Album
import com.viperplayer.plugin.model.Artist
import com.viperplayer.plugin.model.BrowseCategory
import com.viperplayer.plugin.model.HomeContent
import com.viperplayer.plugin.model.Lyrics
import com.viperplayer.plugin.model.LyricsRequest
import com.viperplayer.plugin.model.MediaType
import com.viperplayer.plugin.model.Page
import com.viperplayer.plugin.model.PageRequest
import com.viperplayer.plugin.model.PlaybackEvent
import com.viperplayer.plugin.model.Playlist
import com.viperplayer.plugin.model.PluginErrorCode
import com.viperplayer.plugin.model.PluginException
import com.viperplayer.plugin.model.SearchRequest
import com.viperplayer.plugin.model.SearchResult
import com.viperplayer.plugin.model.SearchSuggestions
import com.viperplayer.plugin.model.Song

/**
 * Implemented by a plugin that provides a music catalog and playable streams. Override only what
 * you support and declare it in [com.viperplayer.plugin.model.SourceCapabilities] — the host never
 * calls a capability you didn't advertise, so unsupported methods keep their defaults.
 *
 * Every method is a plain `suspend fun` over typed models. The SDK handles all IPC, serialization,
 * paging, cancellation, and error mapping; you never touch AIDL, Bundles, or binder threads.
 */
interface SourceProvider {

    suspend fun search(request: SearchRequest): SearchResult

    suspend fun getSearchSuggestions(query: String): SearchSuggestions = SearchSuggestions()

    suspend fun getSong(id: String): Song

    /** Override when [com.viperplayer.plugin.model.SourceCapabilities.albumDetails] is advertised. */
    suspend fun getAlbum(id: String): Album =
        throw PluginException(PluginErrorCode.UNSUPPORTED, "Album details are not supported")

    /** Override when [com.viperplayer.plugin.model.SourceCapabilities.artistDetails] is advertised. */
    suspend fun getArtist(id: String): Artist =
        throw PluginException(PluginErrorCode.UNSUPPORTED, "Artist details are not supported")

    /** Override when [com.viperplayer.plugin.model.SourceCapabilities.playlists] is advertised. */
    suspend fun getPlaylist(id: String): Playlist =
        throw PluginException(PluginErrorCode.UNSUPPORTED, "Playlists are not supported")

    suspend fun getArtistSongs(artistId: String, page: PageRequest): Page<Song> = Page()

    suspend fun getArtistAlbums(artistId: String, page: PageRequest): Page<Album> = Page()

    suspend fun getPlaylistSongs(playlistId: String, page: PageRequest): Page<Song> = Page()

    /**
     * Songs related to [songId] — a radio / autoplay seed. The host appends these to keep playback
     * going when the queue runs out (a playlist/album ends, or a single song from search finishes).
     * Default returns empty, i.e. no autoplay for this plugin.
     */
    suspend fun getRelatedSongs(songId: String): Page<Song> = Page()

    suspend fun getLibrarySongs(page: PageRequest): Page<Song> = Page()

    suspend fun getLibraryAlbums(page: PageRequest): Page<Album> = Page()

    suspend fun getLibraryArtists(page: PageRequest): Page<Artist> = Page()

    suspend fun getLibraryPlaylists(page: PageRequest): Page<Playlist> = Page()

    suspend fun getBrowseCategories(page: PageRequest): Page<BrowseCategory> = Page()

    suspend fun getCategoryContents(categoryId: String, page: PageRequest): SearchResult = SearchResult()

    suspend fun getHome(): HomeContent = HomeContent()

    /**
     * Re-fetch the Home feed filtered by a tapped top-level [com.viperplayer.plugin.model.HomeChip].
     * [chipId] is the chosen chip's id, or `null` for the base feed. Override this ONLY if you emit
     * [com.viperplayer.plugin.model.HomeContent.chips]. The default ignores the chip and returns the
     * base [getHome], so a plugin that doesn't support chip filtering keeps working: the host shows the
     * base feed on chip taps. A plugin from an even older SDK that lacks this verb entirely answers
     * UNSUPPORTED, which the host also degrades to the base feed.
     */
    suspend fun getHome(chipId: String?): HomeContent = getHome()

    /**
     * Fetch the next page of Home sections for an opaque [continuation] token (infinite scroll). The
     * returned content's [com.viperplayer.plugin.model.HomeContent.sections] are APPENDED to the feed,
     * and its [com.viperplayer.plugin.model.HomeContent.continuation] becomes the next token (null =
     * end). Override this ONLY if [getHome] returns a non-null continuation. The default returns an
     * empty page with no further token, so a plugin without pagination is unaffected.
     */
    suspend fun getHomeContinuation(continuation: String): HomeContent = HomeContent()

    /**
     * Re-fetch a home section's items for the tapped filter chip. [sectionId] is the section the chip
     * belongs to and [filterKey] is the chosen [com.viperplayer.plugin.model.SectionFilter.key]. The
     * host keeps the section's title/rows/chips and swaps in the returned items. Default returns empty
     * so plugins without filterable sections are unaffected.
     */
    suspend fun filterSection(sectionId: String, filterKey: String): SearchResult = SearchResult()

    /**
     * Resolve how a playable should be played. [type] is [MediaType.SONG] for audio or
     * [MediaType.VIDEO] for a music video, so a plugin with separate audio/video endpoints can pick
     * the right one. Return a [StreamResponse] built via its factories ([StreamResponse.url],
     * [StreamResponse.dash], [StreamResponse.pcm], …). [maxBitrateKbps] is the host's audio-quality
     * cap (from the user's setting); null means no cap — serve the highest quality available.
     */
    suspend fun resolveStream(songId: String, type: MediaType, maxBitrateKbps: Int? = null): StreamResponse

    // ---- Library WRITE (push) — optional. Override + declare
    // [com.viperplayer.plugin.model.SourceCapabilities.libraryWrite] to let ViPER propagate local
    // library changes up to the signed-in account. Every method defaults to UNSUPPORTED, so a plugin
    // that doesn't implement writes still compiles and connects; the host keeps such changes queued. ----

    /** Set (or clear) a track's liked/saved state on the account. */
    suspend fun setLiked(trackId: String, liked: Boolean): Unit =
        throw PluginException(PluginErrorCode.UNSUPPORTED, "Library write is not supported")

    /** Follow (or unfollow) an artist on the account. */
    suspend fun setFollowed(artistId: String, followed: Boolean): Unit =
        throw PluginException(PluginErrorCode.UNSUPPORTED, "Library write is not supported")

    /** Create a playlist on the account, returning the account-assigned playlist id. */
    suspend fun createPlaylist(name: String): String =
        throw PluginException(PluginErrorCode.UNSUPPORTED, "Library write is not supported")

    /** Rename a playlist on the account. */
    suspend fun renamePlaylist(playlistId: String, name: String): Unit =
        throw PluginException(PluginErrorCode.UNSUPPORTED, "Library write is not supported")

    /** Delete a playlist from the account. */
    suspend fun deletePlaylist(playlistId: String): Unit =
        throw PluginException(PluginErrorCode.UNSUPPORTED, "Library write is not supported")

    /** Add a track to a playlist on the account. */
    suspend fun addTrackToPlaylist(playlistId: String, trackId: String): Unit =
        throw PluginException(PluginErrorCode.UNSUPPORTED, "Library write is not supported")

    /** Remove a track from a playlist on the account. */
    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String): Unit =
        throw PluginException(PluginErrorCode.UNSUPPORTED, "Library write is not supported")

    /** Seek a live PCM stream, if [com.viperplayer.plugin.model.PcmStream.seekable]. */
    suspend fun seekStream(streamId: String, positionMs: Long): Boolean = false

    /** Stop and release a live PCM stream. */
    suspend fun closeStream(streamId: String) {}
}

/** Implemented by a plugin that supplies lyrics for tracks (its own or, generically, by metadata). */
interface LyricsProvider {
    suspend fun getLyrics(request: LyricsRequest): Lyrics?
}

/** Implemented by a plugin that records playback (e.g. scrobbling). The host owns the player and
 *  pushes events; the sink forwards/persists them. */
interface ScrobbleSink {
    suspend fun onNowPlaying(event: PlaybackEvent) {}
    suspend fun onPlaybackEvent(event: PlaybackEvent)
}

/** Implemented by a plugin that enriches a song's metadata (artwork, tags, replay gain, …). */
interface MetadataProvider {
    suspend fun enrich(song: Song): Song
}
