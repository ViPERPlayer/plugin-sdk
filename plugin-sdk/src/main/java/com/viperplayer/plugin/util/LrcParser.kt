package com.viperplayer.plugin.util

import com.viperplayer.plugin.model.LyricLine
import com.viperplayer.plugin.model.LyricWord
import com.viperplayer.plugin.model.Lyrics

/**
 * Parser for LRC lyric text — both standard line-timed LRC (`[mm:ss.xx]text`) and the "enhanced" A2
 * extension that adds per-word timings (`[mm:ss.xx] <mm:ss.xx>word <mm:ss.xx>word`). Produces a
 * timed [Lyrics]; when a line carries word tags they populate [LyricLine.words] for word-by-word
 * ("karaoke") highlighting.
 *
 * Robust to: multiple time tags on one line (repeated lyric), 2- or 3-digit fractional seconds,
 * `.`/`:` fraction separators, interleaved metadata tags (`[ar:]`, `[ti:]`, `[offset:]`), and blank
 * lines. Untimed lines and metadata are dropped. Lines are returned sorted by start time.
 */
object LrcParser {

    private val LINE_TAG = Regex("""\[(\d{1,3}):(\d{2})(?:[.:](\d{1,3}))?]""")
    private val WORD_TAG = Regex("""<(\d{1,3}):(\d{2})(?:[.:](\d{1,3}))?>""")
    private val OFFSET_TAG = Regex("""\[offset:\s*([+-]?\d+)\s*]""", RegexOption.IGNORE_CASE)

    /** Cheap heuristic: does this text contain at least one `[mm:ss]`-style line timestamp? */
    fun looksLikeLrc(text: String): Boolean = LINE_TAG.containsMatchIn(text)

    /**
     * Parse [text] as LRC. Returns synced [Lyrics] with any word timings preserved; if nothing timed
     * is found, returns an empty (non-synced) result so callers can fall back to plain text.
     */
    fun parse(text: String): Lyrics {
        // Global offset (ms) shifts every timestamp; positive delays, negative advances.
        val offset = OFFSET_TAG.find(text)?.groupValues?.get(1)?.toLongOrNull() ?: 0L

        val out = ArrayList<LyricLine>()
        for (raw in text.lineSequence()) {
            val line = raw.trim()
            if (line.isEmpty()) continue

            // Collect the leading, contiguous line time-tags (a lyric may repeat at several times).
            val starts = ArrayList<Long>()
            var cursor = 0
            while (true) {
                val m = LINE_TAG.find(line, cursor) ?: break
                if (m.range.first != cursor) break
                starts.add(timeMs(m) + offset)
                cursor = m.range.last + 1
            }
            if (starts.isEmpty()) continue // metadata or untimed line

            val content = line.substring(cursor)
            val words = parseWords(content, offset)
            val plain = (if (words.isNotEmpty()) words.joinToString("") { it.text } else stripWordTags(content)).trim()
            if (plain.isEmpty() && words.isEmpty()) continue

            for (start in starts) {
                // Re-base word start times relative to nothing (they are absolute already); a repeated
                // line at a different time reuses the same relative word offsets shifted to that start.
                val lineWords = if (words.isEmpty()) emptyList() else {
                    val delta = start - words.first().startMs
                    if (delta == 0L) words else words.map { it.copy(startMs = it.startMs + delta) }
                }
                out.add(LyricLine(startMs = start.coerceAtLeast(0L), text = plain, words = lineWords))
            }
        }
        out.sortBy { it.startMs }
        return Lyrics(synced = out.isNotEmpty(), lines = out, plainText = null, source = "lrc")
    }

    /** Split enhanced-LRC content (`<t>word <t>word`) into timed words, or empty if not word-timed. */
    private fun parseWords(content: String, offset: Long): List<LyricWord> {
        val tags = WORD_TAG.findAll(content).toList()
        if (tags.isEmpty()) return emptyList()
        val words = ArrayList<LyricWord>(tags.size)
        for (i in tags.indices) {
            val start = timeMs(tags[i]) + offset
            val from = tags[i].range.last + 1
            val to = if (i + 1 < tags.size) tags[i + 1].range.first else content.length
            val segment = content.substring(from, to)
            if (segment.isNotEmpty()) words.add(LyricWord(startMs = start.coerceAtLeast(0L), text = segment))
        }
        return words
    }

    private fun stripWordTags(content: String): String = content.replace(WORD_TAG, "")

    private fun timeMs(m: MatchResult): Long {
        val minutes = m.groupValues[1].toLong()
        val seconds = m.groupValues[2].toLong()
        val fraction = m.groupValues[3]
        val millis = when (fraction.length) {
            0 -> 0L
            1 -> fraction.toLong() * 100
            2 -> fraction.toLong() * 10
            else -> fraction.take(3).toLong()
        }
        return (minutes * 60 + seconds) * 1000 + millis
    }
}
