package com.viperplayer.plugin.protocol

/**
 * The verb namespace dispatched through `IViperPlugin.invoke`. Adding a feature = adding a verb
 * here (plus its request/response models and a capability flag) — never a new AIDL method.
 *
 * Verbs are an internal routing detail: plugin authors and host callers work with typed methods on
 * the provider/client facades and never type these strings.
 */
object Verbs {
    object Source {
        const val SEARCH = "source.search"
        const val SEARCH_SUGGESTIONS = "source.searchSuggestions"

        const val GET_SONG = "source.getSong"
        const val GET_ALBUM = "source.getAlbum"
        const val GET_ARTIST = "source.getArtist"
        const val GET_PLAYLIST = "source.getPlaylist"

        const val ARTIST_SONGS = "source.artistSongs"
        const val ARTIST_ALBUMS = "source.artistAlbums"
        const val PLAYLIST_SONGS = "source.playlistSongs"
        const val RELATED_SONGS = "source.relatedSongs"

        const val LIBRARY_SONGS = "source.library.songs"
        const val LIBRARY_ALBUMS = "source.library.albums"
        const val LIBRARY_ARTISTS = "source.library.artists"
        const val LIBRARY_PLAYLISTS = "source.library.playlists"

        // ---- Library WRITE (push): optional, gated on SourceCapabilities.libraryWrite. Plugins that
        // don't implement these answer UNSUPPORTED and the host keeps the change queued as skipped. ----
        const val SET_LIKED = "source.library.setLiked"
        const val SET_FOLLOWED = "source.library.setFollowed"
        const val PLAYLIST_CREATE = "source.library.playlist.create"
        const val PLAYLIST_RENAME = "source.library.playlist.rename"
        const val PLAYLIST_DELETE = "source.library.playlist.delete"
        const val PLAYLIST_ADD_TRACK = "source.library.playlist.addTrack"
        const val PLAYLIST_REMOVE_TRACK = "source.library.playlist.removeTrack"

        const val BROWSE_CATEGORIES = "source.browse.categories"
        const val CATEGORY_CONTENTS = "source.browse.category"

        const val HOME = "source.home"
        const val FILTER_SECTION = "source.filterSection"

        // ---- Home: chips + infinite scroll (optional). A plugin (or older APK) that doesn't
        // implement these answers UNSUPPORTED; the host falls back to the base feed / no more pages. ----
        /** Re-fetch the Home feed filtered by a tapped top-level chip. */
        const val HOME_FILTERED = "source.home.filtered"
        /** Fetch the next page of Home sections for a continuation token. */
        const val HOME_CONTINUATION = "source.home.continuation"

        const val RESOLVE_STREAM = "source.resolveStream"
        const val SEEK_STREAM = "source.seekStream"
        const val CLOSE_STREAM = "source.closeStream"
    }

    object Dsp {
        const val DESCRIBE = "dsp.describe"
        const val OPEN = "dsp.open"
        const val CONFIGURE = "dsp.configure"
        const val CLOSE = "dsp.close"
    }

    object Lyrics {
        const val GET = "lyrics.get"
    }

    object Scrobble {
        const val NOW_PLAYING = "scrobble.nowPlaying"
        const val SUBMIT = "scrobble.submit"
    }

    object Metadata {
        const val ENRICH = "metadata.enrich"
    }

    object Status {
        /** Query the plugin's [com.viperplayer.plugin.model.PluginStatus] (pending user actions). */
        const val GET = "status.get"
    }
}

/** Verbs the plugin sends *to* the host over `IViperHost`. */
object HostVerbs {
    /** request: read the current host-owned player state. */
    const val PLAYER_STATE = "host.playerState"

    object Event {
        const val CONTENT_CHANGED = "host.event.contentChanged"
        const val METADATA_UPDATED = "host.event.metadataUpdated"
        const val AUTH_STATE_CHANGED = "host.event.authStateChanged"
        const val STATUS_CHANGED = "host.event.statusChanged"
        const val ERROR = "host.event.error"
    }
}
