package com.viperplayer.plugin.util

import com.viperplayer.plugin.model.Lyrics

/**
 * Pure helpers for locating and parsing *sidecar* lyric files (a `.lrc`/`.ttml`/`.srt` sitting next
 * to an audio file) — the file-system I/O itself lives in the host/plugin, but the naming and
 * format-dispatch rules are pure and live here so they can be unit-tested without a device.
 *
 * Preference order (a track usually only ships one): word-timed/enhanced LRC and TTML are richest,
 * then SRT, then plain-LRC/plain-text. [sidecarCandidates] returns the candidate paths in the order
 * the caller should probe the file system, and [parseSidecar] parses a file's content by extension.
 */
object LyricsFiles {

    /** Sidecar extensions we understand, in descending preference (probe order). */
    val SUPPORTED_EXTENSIONS = listOf("lrc", "ttml", "srt")

    /**
     * Candidate sidecar paths for an audio file at [audioPath], most-preferred first. For
     * `/music/Song.flac` this yields `/music/Song.lrc`, `/music/Song.ttml`, `/music/Song.srt`. When
     * [audioPath] has no extension the names are still derived from the full base name. Returns an
     * empty list for a blank path.
     */
    fun sidecarCandidates(audioPath: String): List<String> {
        if (audioPath.isBlank()) return emptyList()
        val slash = audioPath.lastIndexOf('/')
        val dir = if (slash >= 0) audioPath.substring(0, slash + 1) else ""
        val name = if (slash >= 0) audioPath.substring(slash + 1) else audioPath
        val dot = name.lastIndexOf('.')
        val base = if (dot > 0) name.substring(0, dot) else name
        if (base.isEmpty()) return emptyList()
        return SUPPORTED_EXTENSIONS.map { "$dir$base.$it" }
    }

    /**
     * Parse sidecar [content] using the parser implied by [extensionOrPath] (a bare extension like
     * `"ttml"` or a full path/filename whose extension is used). Returns null when the extension is
     * unrecognised or the content produced nothing timed *and* had no plain text to show.
     */
    fun parseSidecar(extensionOrPath: String, content: String): Lyrics? {
        val ext = extensionOrPath.substringAfterLast('.', extensionOrPath).lowercase()
        val parsed = when (ext) {
            "lrc" -> LrcParser.parse(content)
            "ttml" -> TtmlParser.parse(content)
            "srt" -> SrtParser.parse(content)
            else -> return null
        }
        if (parsed.lines.isNotEmpty()) return parsed
        // Nothing timed parsed. An .lrc that was actually plain text is still usable as plain text.
        val trimmed = content.trim()
        return if (ext == "lrc" && trimmed.isNotEmpty()) {
            Lyrics(synced = false, lines = emptyList(), plainText = trimmed, source = "lrc")
        } else {
            null
        }
    }

    /**
     * Given several parsed lyrics (e.g. embedded + a sidecar, or several sidecars), pick the best:
     * a synced result always beats an unsynced one; among synced, more lines wins; among unsynced,
     * the first non-blank plain text wins. Returns null when none carry anything usable.
     */
    fun pickBest(candidates: List<Lyrics?>): Lyrics? {
        val usable = candidates.filterNotNull().filter { !it.isEmptyLyrics() }
        if (usable.isEmpty()) return null
        val synced = usable.filter { it.synced }
        if (synced.isNotEmpty()) return synced.maxByOrNull { it.lines.size }
        return usable.first()
    }

    private fun Lyrics.isEmptyLyrics(): Boolean =
        lines.isEmpty() && plainText.isNullOrBlank()
}
