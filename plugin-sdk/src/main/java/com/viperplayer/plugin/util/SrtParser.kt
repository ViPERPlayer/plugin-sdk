package com.viperplayer.plugin.util

import com.viperplayer.plugin.model.LyricLine
import com.viperplayer.plugin.model.Lyrics

/**
 * Parser for SubRip (`.srt`) subtitle files, sometimes shipped alongside audio as line-synced
 * lyrics. Each cue is `index` / `HH:MM:SS,mmm --> HH:MM:SS,mmm` / one-or-more text lines, separated
 * by a blank line. SRT carries only line-level timing (no per-word timing), so [LyricLine.words] is
 * always empty. A cue's multiple text lines are joined with spaces into a single lyric line, HTML-ish
 * tags (`<i>`, `<b>`, `<font>`, …) are stripped, and cues are returned sorted by start time.
 */
object SrtParser {

    // Start of a cue's timing line: HH:MM:SS,mmm --> HH:MM:SS,mmm (also tolerates '.' as the ms sep).
    private val TIME_LINE = Regex("""(\d{1,2}):(\d{2}):(\d{2})[,.](\d{1,3})\s*-->\s*(\d{1,2}):(\d{2}):(\d{2})[,.](\d{1,3})""")
    private val TAG = Regex("""<[^>]*>""")

    /** Cheap heuristic: does this text contain an SRT-style `-->` timing arrow? */
    fun looksLikeSrt(text: String): Boolean = TIME_LINE.containsMatchIn(text)

    fun parse(text: String): Lyrics {
        val out = ArrayList<LyricLine>()
        // Split into blank-line-delimited blocks; normalise CRLF first.
        val blocks = text.replace("\r\n", "\n").replace('\r', '\n').split(Regex("""\n[ \t]*\n"""))
        for (block in blocks) {
            val lines = block.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) continue

            // The timing line is the first line containing `-->`; a leading numeric index is optional.
            val timeIdx = lines.indexOfFirst { TIME_LINE.containsMatchIn(it) }
            if (timeIdx < 0) continue
            val m = TIME_LINE.find(lines[timeIdx]) ?: continue
            val start = timeMs(m.groupValues, 1)

            val textLines = lines.drop(timeIdx + 1)
            if (textLines.isEmpty()) continue
            val plain = textLines.joinToString(" ") { stripTags(it) }.trim().replace(WHITESPACE, " ")
            if (plain.isEmpty()) continue

            out.add(LyricLine(startMs = start.coerceAtLeast(0L), text = plain))
        }
        out.sortBy { it.startMs }
        return Lyrics(synced = out.isNotEmpty(), lines = out, plainText = null, source = "srt")
    }

    private fun timeMs(g: List<String>, base: Int): Long {
        val h = g[base].toLong()
        val m = g[base + 1].toLong()
        val s = g[base + 2].toLong()
        val frac = g[base + 3]
        val ms = frac.take(3).padEnd(3, '0').toLong()
        return ((h * 60 + m) * 60 + s) * 1000 + ms
    }

    private fun stripTags(s: String): String = TAG.replace(s, "")

    private val WHITESPACE = Regex("""\s+""")
}
