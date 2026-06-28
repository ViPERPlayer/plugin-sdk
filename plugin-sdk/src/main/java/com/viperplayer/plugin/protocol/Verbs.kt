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

        const val BROWSE_CATEGORIES = "source.browse.categories"
        const val CATEGORY_CONTENTS = "source.browse.category"

        const val HOME = "source.home"
        const val FILTER_SECTION = "source.filterSection"

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
}

/** Verbs the plugin sends *to* the host over `IViperHost`. */
object HostVerbs {
    /** request: read the current host-owned player state. */
    const val PLAYER_STATE = "host.playerState"

    object Event {
        const val CONTENT_CHANGED = "host.event.contentChanged"
        const val METADATA_UPDATED = "host.event.metadataUpdated"
        const val AUTH_STATE_CHANGED = "host.event.authStateChanged"
        const val ERROR = "host.event.error"
    }
}
