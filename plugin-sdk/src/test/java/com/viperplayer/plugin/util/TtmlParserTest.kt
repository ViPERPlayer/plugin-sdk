package com.viperplayer.plugin.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TtmlParserTest {

    @Test
    fun `looksLikeTtml detects a tt document and rejects plain text`() {
        assertTrue(TtmlParser.looksLikeTtml("<tt xmlns=\"...\"><body/></tt>"))
        assertTrue(TtmlParser.looksLikeTtml("<p begin=\"1s\">hi</p>"))
        assertFalse(TtmlParser.looksLikeTtml("just plain lyrics"))
    }

    @Test
    fun `line-timed paragraphs without spans parse to timed lines without words`() {
        val ttml = """
            <tt><body><div>
              <p begin="00:00:01.000" end="00:00:03.000">First line</p>
              <p begin="00:00:03.500" end="00:00:05.000">Second line</p>
            </div></body></tt>
        """.trimIndent()
        val lyrics = TtmlParser.parse(ttml)
        assertTrue(lyrics.synced)
        assertEquals(2, lyrics.lines.size)
        assertEquals(1_000L, lyrics.lines[0].startMs)
        assertEquals("First line", lyrics.lines[0].text)
        assertEquals(3_500L, lyrics.lines[1].startMs)
        assertTrue(lyrics.lines[0].words.isEmpty())
        assertEquals("ttml", lyrics.source)
    }

    @Test
    fun `word-timed spans populate per-word timings and reconstruct the line`() {
        val ttml = """
            <tt><body><div>
              <p begin="00:00:10.000" end="00:00:11.500">
                <span begin="00:00:10.000" end="00:00:10.400">Hey</span>
                <span begin="00:00:10.400" end="00:00:10.900">there</span>
                <span begin="00:00:10.900" end="00:00:11.500">world</span>
              </p>
            </div></body></tt>
        """.trimIndent()
        val lyrics = TtmlParser.parse(ttml)
        assertEquals(1, lyrics.lines.size)
        val line = lyrics.lines[0]
        assertEquals(10_000L, line.startMs)
        assertEquals(3, line.words.size)
        assertEquals(10_000L, line.words[0].startMs)
        assertEquals(10_400L, line.words[1].startMs)
        assertEquals(10_900L, line.words[2].startMs)
        // The gap between spans is preserved as a trailing separator, so the words rejoin cleanly.
        assertEquals("Hey there world", line.words.joinToString("") { it.text })
        assertEquals("Hey there world", line.text)
    }

    @Test
    fun `namespaced ttm begin attributes are read`() {
        val ttml = """
            <tt xmlns:ttm="http://www.w3.org/ns/ttml#metadata">
              <body><div><p ttm:begin="5s" ttm:end="6s">ns line</p></div></body>
            </tt>
        """.trimIndent()
        val lyrics = TtmlParser.parse(ttml)
        assertEquals(1, lyrics.lines.size)
        assertEquals(5_000L, lyrics.lines[0].startMs)
        assertEquals("ns line", lyrics.lines[0].text)
    }

    @Test
    fun `offset-time units parse to milliseconds`() {
        fun start(begin: String) = TtmlParser.parse("<p begin=\"$begin\">x</p>").lines[0].startMs
        assertEquals(12_500L, start("12.5s"))
        assertEquals(1_200L, start("1200ms"))
        assertEquals(120_000L, start("2m"))
        assertEquals(3_600_000L, start("1h"))
    }

    @Test
    fun `clock-time with and without hours and comma fraction parse`() {
        fun start(begin: String) = TtmlParser.parse("<p begin=\"$begin\">x</p>").lines[0].startMs
        assertEquals(65_250L, start("01:05.25"))     // mm:ss.ff -> 1m 5.25s
        assertEquals(3_665_100L, start("1:01:05.100")) // hh:mm:ss.fff
        assertEquals(5_250L, start("00:05,250"))       // comma fraction
    }

    @Test
    fun `xml entities in text are decoded`() {
        val ttml = "<p begin=\"1s\">Rock &amp; Roll &lt;3</p>"
        assertEquals("Rock & Roll <3", TtmlParser.parse(ttml).lines[0].text)
    }

    @Test
    fun `paragraphs are sorted by start time`() {
        val ttml = """
            <p begin="3s">third</p>
            <p begin="1s">first</p>
            <p begin="2s">second</p>
        """.trimIndent()
        val lyrics = TtmlParser.parse(ttml)
        assertEquals(listOf("first", "second", "third"), lyrics.lines.map { it.text })
    }

    @Test
    fun `paragraph without begin is skipped`() {
        val ttml = """
            <p>no timing</p>
            <p begin="2s">timed</p>
        """.trimIndent()
        val lyrics = TtmlParser.parse(ttml)
        assertEquals(1, lyrics.lines.size)
        assertEquals("timed", lyrics.lines[0].text)
    }

    @Test
    fun `document with no timed paragraphs yields empty non-synced result`() {
        val lyrics = TtmlParser.parse("<tt><body><div></div></body></tt>")
        assertFalse(lyrics.synced)
        assertTrue(lyrics.lines.isEmpty())
    }

    @Test
    fun `span with no begin is treated as whitespace when it has no siblings`() {
        // A <span> that carries no begin should not create a bogus zero-timed word; if no span is
        // timed the paragraph falls back to plain text from its begin.
        val ttml = "<p begin=\"2s\"><span>plain</span> tail</p>"
        val lyrics = TtmlParser.parse(ttml)
        assertEquals(1, lyrics.lines.size)
        assertEquals(2_000L, lyrics.lines[0].startMs)
        assertTrue(lyrics.lines[0].words.isEmpty())
        assertEquals("plain tail", lyrics.lines[0].text)
    }

    @Test
    fun `frames unit is unsupported and skips the paragraph`() {
        assertNull(TtmlParser.parse("<p begin=\"10f\">x</p>").lines.firstOrNull())
    }
}
