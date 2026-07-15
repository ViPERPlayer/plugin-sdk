package com.viperplayer.plugin.util

import com.viperplayer.plugin.model.LyricLine
import com.viperplayer.plugin.model.LyricWord
import com.viperplayer.plugin.model.Lyrics

/**
 * Parser for TTML (Timed Text Markup Language) lyric documents — the format Apple Music and
 * Musixmatch ship word-timed ("syllable") lyrics in. Each timed line is a `<p begin=… end=…>` inside
 * `<div>`/`<body>`; when a line carries `<span begin=…>word</span>` children they populate
 * [LyricLine.words] for word-by-word ("karaoke") highlighting. A `<p>` with no timed spans becomes a
 * plain line-timed entry using the paragraph's own `begin`.
 *
 * Timestamps accept the TTML clock-time forms `h:mm:ss(.fff)`, `mm:ss(.fff)`, and offset-time
 * (`12.5s`, `1200ms`, `2m`, `1h`). Robust to XML namespaces/prefixes on the timing attributes
 * (`ttm:`, `itunes:`, …), self-closing/nested tags, and HTML/XML entities in text. Lines are returned
 * sorted by start time; a document with no timed lines yields an empty, non-synced result.
 */
object TtmlParser {

    // A <p ...>...</p> paragraph. DOTALL so inner content (which may contain nested <span>s and
    // newlines) is captured whole; non-greedy so adjacent paragraphs don't merge.
    private val P_TAG = Regex("""<p\b([^>]*)>(.*?)</p>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    // A <span ...>text</span> (word/syllable). Non-greedy inner capture.
    private val SPAN_TAG = Regex("""<span\b([^>]*)>(.*?)</span>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    // Any XML tag, used to strip markup from a paragraph's plain text.
    private val ANY_TAG = Regex("""<[^>]*>""")
    // begin="..." / end="..." on a tag, ignoring any namespace prefix on the attribute name.
    private val BEGIN_ATTR = Regex("""\bbegin\s*=\s*"([^"]*)"""", RegexOption.IGNORE_CASE)

    /** Cheap heuristic: does this text look like a TTML document with timed paragraphs? */
    fun looksLikeTtml(text: String): Boolean =
        text.contains("<tt", ignoreCase = true) || (text.contains("<p", ignoreCase = true) && text.contains("begin", ignoreCase = true))

    fun parse(text: String): Lyrics {
        val out = ArrayList<LyricLine>()
        for (p in P_TAG.findAll(text)) {
            val attrs = p.groupValues[1]
            val body = p.groupValues[2]
            val lineStart = BEGIN_ATTR.find(attrs)?.let { parseTime(it.groupValues[1]) } ?: continue

            val words = parseWords(body)
            val plain = if (words.isNotEmpty()) {
                words.joinToString("") { it.text }.trim()
            } else {
                decodeEntities(ANY_TAG.replace(body, "")).trim().replace(WHITESPACE, " ")
            }
            if (plain.isEmpty()) continue

            out.add(LyricLine(startMs = lineStart.coerceAtLeast(0L), text = plain, words = words))
        }
        out.sortBy { it.startMs }
        return Lyrics(synced = out.isNotEmpty(), lines = out, plainText = null, source = "ttml")
    }

    /** Split a paragraph body's timed `<span>`s into words; empty when no span carries a `begin`. */
    private fun parseWords(body: String): List<LyricWord> {
        val spans = SPAN_TAG.findAll(body).toList()
        if (spans.isEmpty()) return emptyList()
        val words = ArrayList<LyricWord>(spans.size)
        var lastEnd = 0
        for (span in spans) {
            val begin = BEGIN_ATTR.find(span.groupValues[1])?.let { parseTime(it.groupValues[1]) }
                ?: run {
                    // An untimed span still consumes its region; advancing lastEnd past it keeps its
                    // stripped text out of the next word's leading separator.
                    lastEnd = span.range.last + 1
                    return@run null
                }
                ?: continue
            // Preserve any literal whitespace that sits *between* spans (word separators).
            val gap = body.substring(lastEnd, span.range.first)
            val sep = decodeEntities(ANY_TAG.replace(gap, "")).replace(WHITESPACE, " ")
            if (sep.isNotEmpty() && words.isNotEmpty()) {
                val prev = words.removeAt(words.size - 1)
                words.add(prev.copy(text = prev.text + sep))
            }
            // A span may nest further spans; take its innermost text.
            val inner = decodeEntities(ANY_TAG.replace(span.groupValues[2], "")).replace(WHITESPACE, " ")
            words.add(LyricWord(startMs = begin.coerceAtLeast(0L), text = inner))
            lastEnd = span.range.last + 1
        }
        return words
    }

    /**
     * Parse a TTML time expression to milliseconds. Supports clock-time (`hh:mm:ss.fff`,
     * `mm:ss.fff`) and offset-time with a metric unit (`h`, `m`, `s`, `ms`, `f`/frames treated as
     * unsupported → 0). Returns null when unparseable.
     */
    private fun parseTime(raw: String): Long? {
        val s = raw.trim()
        if (s.isEmpty()) return null

        // Offset-time: a number followed by a unit (s / ms / m / h). Order matters: check "ms" first.
        OFFSET_TIME.matchEntire(s)?.let { m ->
            val value = m.groupValues[1].toDoubleOrNull() ?: return null
            return when (m.groupValues[2].lowercase()) {
                "ms" -> value
                "s" -> value * 1000
                "m" -> value * 60_000
                "h" -> value * 3_600_000
                else -> return null
            }.toLong()
        }

        // Clock-time: [hh:]mm:ss[.fff] (fraction may use . or ,).
        val parts = s.split(":")
        if (parts.size < 2 || parts.size > 3) return null
        val secParts = parts.last().replace(',', '.').split(".")
        val whole = secParts[0].toLongOrNull() ?: return null
        val fracMs = secParts.getOrNull(1)?.let { frac ->
            val f = frac.take(3).padEnd(3, '0')
            f.toLongOrNull() ?: return null
        } ?: 0L
        var totalMs = whole * 1000 + fracMs
        val minutes = parts[parts.size - 2].toLongOrNull() ?: return null
        totalMs += minutes * 60_000
        if (parts.size == 3) {
            val hours = parts[0].toLongOrNull() ?: return null
            totalMs += hours * 3_600_000
        }
        return totalMs
    }

    /** Decode the handful of XML/HTML entities that appear in lyric text. */
    private fun decodeEntities(s: String): String =
        if (s.indexOf('&') < 0) s else s
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")

    private val WHITESPACE = Regex("""\s+""")
    private val OFFSET_TIME = Regex("""([0-9]*\.?[0-9]+)\s*(ms|s|m|h|f)""", RegexOption.IGNORE_CASE)
}
