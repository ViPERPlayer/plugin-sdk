package com.viperplayer.plugin.util

import com.viperplayer.plugin.model.LyricLine
import com.viperplayer.plugin.model.Lyrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricsFilesTest {

    @Test
    fun `sidecarCandidates swaps the extension for each supported format in preference order`() {
        assertEquals(
            listOf("/music/Song.lrc", "/music/Song.ttml", "/music/Song.srt"),
            LyricsFiles.sidecarCandidates("/music/Song.flac"),
        )
    }

    @Test
    fun `sidecarCandidates handles a dotted base name and keeps the directory`() {
        assertEquals(
            listOf("/a/b/01. Track feat. X.lrc", "/a/b/01. Track feat. X.ttml", "/a/b/01. Track feat. X.srt"),
            LyricsFiles.sidecarCandidates("/a/b/01. Track feat. X.mp3"),
        )
    }

    @Test
    fun `sidecarCandidates works for a bare filename with no directory`() {
        assertEquals(
            listOf("Song.lrc", "Song.ttml", "Song.srt"),
            LyricsFiles.sidecarCandidates("Song.opus"),
        )
    }

    @Test
    fun `sidecarCandidates returns empty for a blank path`() {
        assertTrue(LyricsFiles.sidecarCandidates("").isEmpty())
        assertTrue(LyricsFiles.sidecarCandidates("   ").isEmpty())
    }

    @Test
    fun `resolveSidecarName finds an uppercase-extension sidecar case-insensitively`() {
        val siblings = listOf("cover.jpg", "Song.LRC", "notes.txt")
        assertEquals("Song.LRC", LyricsFiles.resolveSidecarName("/music/Song.flac", siblings))
    }

    @Test
    fun `resolveSidecarName matches mixed-case extension and base name`() {
        val siblings = listOf("SONG.Ttml")
        assertEquals("SONG.Ttml", LyricsFiles.resolveSidecarName("/music/song.mp3", siblings))
    }

    @Test
    fun `resolveSidecarName prefers lrc then ttml then srt when several exist`() {
        val siblings = listOf("Song.SRT", "Song.Ttml", "Song.LRC")
        assertEquals("Song.LRC", LyricsFiles.resolveSidecarName("/music/Song.flac", siblings))
        val onlyTtmlAndSrt = listOf("Song.SRT", "Song.Ttml")
        assertEquals("Song.Ttml", LyricsFiles.resolveSidecarName("/music/Song.flac", onlyTtmlAndSrt))
    }

    @Test
    fun `resolveSidecarName returns null when no matching sidecar is present`() {
        val siblings = listOf("Other.lrc", "Song.jpg")
        assertNull(LyricsFiles.resolveSidecarName("/music/Song.flac", siblings))
        assertNull(LyricsFiles.resolveSidecarName("", listOf("Song.lrc")))
    }

    @Test
    fun `parseSidecar dispatches by extension`() {
        val lrc = LyricsFiles.parseSidecar("song.lrc", "[00:01.00]hi")!!
        assertEquals("lrc", lrc.source)
        assertTrue(lrc.synced)

        val ttml = LyricsFiles.parseSidecar("ttml", "<p begin=\"1s\">hi</p>")!!
        assertEquals("ttml", ttml.source)
        assertTrue(ttml.synced)

        val srt = LyricsFiles.parseSidecar("/x/y.srt", "1\n00:00:01,000 --> 00:00:02,000\nhi")!!
        assertEquals("srt", srt.source)
        assertTrue(srt.synced)
    }

    @Test
    fun `parseSidecar returns null for an unknown extension`() {
        assertNull(LyricsFiles.parseSidecar("cover.jpg", "not lyrics"))
    }

    @Test
    fun `parseSidecar keeps a plain-text lrc as unsynced plain text`() {
        val result = LyricsFiles.parseSidecar("song.lrc", "just some lyrics\nwith no timestamps")!!
        assertTrue(!result.synced)
        assertTrue(result.lines.isEmpty())
        assertEquals("just some lyrics\nwith no timestamps", result.plainText)
    }

    @Test
    fun `parseSidecar returns null for a ttml with nothing timed`() {
        assertNull(LyricsFiles.parseSidecar("song.ttml", "<tt><body/></tt>"))
    }

    @Test
    fun `pickBest prefers synced over unsynced`() {
        val unsynced = Lyrics(synced = false, plainText = "plain")
        val synced = Lyrics(synced = true, lines = listOf(LyricLine(0L, "a")))
        assertEquals(synced, LyricsFiles.pickBest(listOf(unsynced, synced)))
    }

    @Test
    fun `pickBest picks the synced result with the most lines`() {
        val few = Lyrics(synced = true, lines = listOf(LyricLine(0L, "a")))
        val many = Lyrics(synced = true, lines = listOf(LyricLine(0L, "a"), LyricLine(1L, "b")))
        assertEquals(many, LyricsFiles.pickBest(listOf(few, many)))
    }

    @Test
    fun `pickBest falls back to first usable unsynced when none synced`() {
        val a = Lyrics(synced = false, plainText = "first")
        val b = Lyrics(synced = false, plainText = "second")
        assertEquals(a, LyricsFiles.pickBest(listOf(a, b)))
    }

    @Test
    fun `pickBest ignores nulls and empty lyrics`() {
        val empty = Lyrics(synced = false, lines = emptyList(), plainText = null)
        val blank = Lyrics(synced = false, plainText = "   ")
        val good = Lyrics(synced = false, plainText = "real")
        assertEquals(good, LyricsFiles.pickBest(listOf(null, empty, blank, good)))
    }

    @Test
    fun `pickBest returns null when nothing usable`() {
        assertNull(LyricsFiles.pickBest(listOf(null, Lyrics(synced = false, plainText = null))))
    }
}
