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
     * [StreamResponse.dash], [StreamResponse.pcm], …).
     */
    suspend fun resolveStream(songId: String, type: MediaType): StreamResponse

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
