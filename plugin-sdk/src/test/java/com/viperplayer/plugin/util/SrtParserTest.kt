package com.viperplayer.plugin.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SrtParserTest {

    @Test
    fun `looksLikeSrt detects the timing arrow and rejects plain text`() {
        assertTrue(SrtParser.looksLikeSrt("1\n00:00:01,000 --> 00:00:03,000\nhi"))
        assertFalse(SrtParser.looksLikeSrt("just plain text\nno cues"))
    }

    @Test
    fun `standard srt cues parse to line-timed lyrics without words`() {
        val srt = """
            1
            00:00:01,000 --> 00:00:03,000
            First line

            2
            00:00:03,500 --> 00:00:05,000
            Second line
        """.trimIndent()
        val lyrics = SrtParser.parse(srt)
        assertTrue(lyrics.synced)
        assertEquals(2, lyrics.lines.size)
        assertEquals(1_000L, lyrics.lines[0].startMs)
        assertEquals("First line", lyrics.lines[0].text)
        assertEquals(3_500L, lyrics.lines[1].startMs)
        assertTrue(lyrics.lines.all { it.words.isEmpty() })
        assertEquals("srt", lyrics.source)
    }

    @Test
    fun `hours minutes seconds and millis all contribute`() {
        val srt = "1\n01:02:03,456 --> 01:02:05,000\nx"
        val expected = ((1L * 60 + 2) * 60 + 3) * 1000 + 456
        assertEquals(expected, SrtParser.parse(srt).lines[0].startMs)
    }

    @Test
    fun `multi-line cue joins its text lines with a space`() {
        val srt = """
            1
            00:00:01,000 --> 00:00:04,000
            line one
            line two
        """.trimIndent()
        val lyrics = SrtParser.parse(srt)
        assertEquals(1, lyrics.lines.size)
        assertEquals("line one line two", lyrics.lines[0].text)
    }

    @Test
    fun `formatting tags are stripped`() {
        val srt = "1\n00:00:01,000 --> 00:00:04,000\n<i>italic</i> and <font color=\"red\">red</font>"
        assertEquals("italic and red", SrtParser.parse(srt).lines[0].text)
    }

    @Test
    fun `crlf line endings and dot millisecond separator parse`() {
        val srt = "1\r\n00:00:02.250 --> 00:00:04.000\r\nwindows cue\r\n"
        val lyrics = SrtParser.parse(srt)
        assertEquals(1, lyrics.lines.size)
        assertEquals(2_250L, lyrics.lines[0].startMs)
        assertEquals("windows cue", lyrics.lines[0].text)
    }

    @Test
    fun `cue without a leading index still parses`() {
        val srt = "00:00:01,000 --> 00:00:03,000\nno index here"
        val lyrics = SrtParser.parse(srt)
        assertEquals(1, lyrics.lines.size)
        assertEquals("no index here", lyrics.lines[0].text)
    }

    @Test
    fun `cues are returned sorted by start time`() {
        val srt = """
            1
            00:00:05,000 --> 00:00:06,000
            later

            2
            00:00:01,000 --> 00:00:02,000
            earlier
        """.trimIndent()
        val lyrics = SrtParser.parse(srt)
        assertEquals(listOf("earlier", "later"), lyrics.lines.map { it.text })
    }

    @Test
    fun `a cue with a timing line but no text is skipped`() {
        val srt = """
            1
            00:00:01,000 --> 00:00:03,000

            2
            00:00:04,000 --> 00:00:06,000
            real line
        """.trimIndent()
        val lyrics = SrtParser.parse(srt)
        assertEquals(1, lyrics.lines.size)
        assertEquals("real line", lyrics.lines[0].text)
    }

    @Test
    fun `text without any cues yields empty non-synced result`() {
        val lyrics = SrtParser.parse("no timings here\nat all")
        assertFalse(lyrics.synced)
        assertTrue(lyrics.lines.isEmpty())
    }
}
