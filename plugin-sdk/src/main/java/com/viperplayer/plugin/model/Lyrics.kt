package com.viperplayer.plugin.model

import kotlinx.serialization.Serializable

/** A single timed word/syllable within a lyric line (for word-by-word "karaoke" lyrics). */
@Serializable
data class LyricWord(
    val startMs: Long,
    val text: String,
)

/**
 * A single timed lyric line (for synced lyrics). [words], when non-empty, carries per-word timings
 * for word-by-word highlighting; concatenating the word texts reproduces [text].
 */
@Serializable
data class LyricLine(
    val startMs: Long,
    val text: String,
    val words: List<LyricWord> = emptyList(),
)

/**
 * Lyrics for a track. [synced] reports whether [lines] carry real timestamps; [plainText] is an
 * unsynced fallback. A lyrics-capable plugin fills whichever it has.
 */
@Serializable
data class Lyrics(
    val synced: Boolean = false,
    val lines: List<LyricLine> = emptyList(),
    val plainText: String? = null,
    val source: String? = null,
)

/** What a track to fetch lyrics for is identified by. The id is the source plugin's own song id. */
@Serializable
data class LyricsRequest(
    val songId: String,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val durationMs: Long? = null,
)
