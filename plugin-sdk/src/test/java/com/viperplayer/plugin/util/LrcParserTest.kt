package com.viperplayer.plugin.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LrcParserTest {

    @Test
    fun `looksLikeLrc detects timestamps and rejects plain text`() {
        assertTrue(LrcParser.looksLikeLrc("[00:12.00]hello"))
        assertFalse(LrcParser.looksLikeLrc("just some plain lyrics\nwith no timestamps"))
    }

    @Test
    fun `standard line-timed LRC parses to timed lines without words`() {
        val lrc = """
            [00:01.00]First line
            [00:03.50]Second line
        """.trimIndent()
        val lyrics = LrcParser.parse(lrc)
        assertTrue(lyrics.synced)
        assertEquals(2, lyrics.lines.size)
        assertEquals(1_000L, lyrics.lines[0].startMs)
        assertEquals("First line", lyrics.lines[0].text)
        assertEquals(3_500L, lyrics.lines[1].startMs)
        assertTrue("line-level LRC has no word timings", lyrics.lines[0].words.isEmpty())
    }

    @Test
    fun `metadata and blank lines are ignored`() {
        val lrc = """
            [ar:Some Artist]
            [ti:Some Title]
            [offset:0]

            [00:00.00]Only real line
        """.trimIndent()
        val lyrics = LrcParser.parse(lrc)
        assertEquals(1, lyrics.lines.size)
        assertEquals("Only real line", lyrics.lines[0].text)
    }

    @Test
    fun `enhanced A2 LRC parses per-word timings and reconstructs the line`() {
        val lrc = "[00:10.00] <00:10.00>Hey <00:10.40>there <00:10.90>world"
        val lyrics = LrcParser.parse(lrc)
        assertEquals(1, lyrics.lines.size)
        val line = lyrics.lines[0]
        assertEquals(10_000L, line.startMs)
        assertEquals("Hey there world", line.text)
        assertEquals(3, line.words.size)
        assertEquals(10_000L, line.words[0].startMs)
        assertEquals("Hey ", line.words[0].text)
        assertEquals(10_400L, line.words[1].startMs)
        assertEquals(10_900L, line.words[2].startMs)
        // Concatenating word texts reproduces the full line.
        assertEquals("Hey there world", line.words.joinToString("") { it.text })
    }

    @Test
    fun `two and three digit fractional seconds both parse`() {
        val two = LrcParser.parse("[00:05.25]x").lines[0].startMs
        val three = LrcParser.parse("[00:05.250]x").lines[0].startMs
        assertEquals(5_250L, two)
        assertEquals(5_250L, three)
    }

    @Test
    fun `a line repeated at multiple timestamps expands and keeps word offsets`() {
        val lrc = "[00:01.00][00:05.00] <00:00.00>la <00:00.50>la"
        val lyrics = LrcParser.parse(lrc)
        assertEquals(2, lyrics.lines.size)
        // Sorted by start; each occurrence re-bases word starts to its own line start.
        assertEquals(1_000L, lyrics.lines[0].startMs)
        assertEquals(1_000L, lyrics.lines[0].words[0].startMs)
        assertEquals(1_500L, lyrics.lines[0].words[1].startMs)
        assertEquals(5_000L, lyrics.lines[1].startMs)
        assertEquals(5_000L, lyrics.lines[1].words[0].startMs)
        assertEquals(5_500L, lyrics.lines[1].words[1].startMs)
    }

    @Test
    fun `offset tag shifts all timestamps`() {
        // +500ms offset delays every line.
        val lyrics = LrcParser.parse("[offset:500]\n[00:10.00]line")
        assertEquals(10_500L, lyrics.lines[0].startMs)
    }

    @Test
    fun `plain text without timestamps yields an empty non-synced result`() {
        val lyrics = LrcParser.parse("no timestamps here\nat all")
        assertFalse(lyrics.synced)
        assertTrue(lyrics.lines.isEmpty())
    }
}
