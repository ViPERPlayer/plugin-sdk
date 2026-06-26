package com.viperplayer.plugin

import com.viperplayer.plugin.model.Album
import com.viperplayer.plugin.model.AlbumType
import com.viperplayer.plugin.model.MediaItem
import com.viperplayer.plugin.model.SearchResult
import com.viperplayer.plugin.model.Song
import com.viperplayer.plugin.model.StreamSource
import com.viperplayer.plugin.model.UnknownMediaItem
import com.viperplayer.plugin.model.UnknownStream
import com.viperplayer.plugin.protocol.Codec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the codec's forward-compatibility promise: payloads from a newer peer that use enum
 * values or sealed subtypes this build doesn't know must degrade gracefully instead of throwing.
 */
class CodecForwardCompatTest {

    @Test
    fun unknownEnumValueCoercesToFieldDefault() {
        val json = """{"id":"a1","name":"X","#t":"album","type":"BOXSET"}"""
        val album = Codec.json.decodeFromString<Album>(json)
        assertEquals(AlbumType.ALBUM, album.type) // coerced to default, not thrown
    }

    @Test
    fun unknownMediaItemSubtypeDecodesToUnknown() {
        val json = """{"items":[{"#t":"song","id":"s1","title":"T"},{"#t":"podcast","id":"p1"}]}"""
        val result = Codec.json.decodeFromString<SearchResult>(json)
        assertEquals(2, result.items.size)
        assertTrue(result.items[0] is Song)
        assertTrue(result.items[1] is UnknownMediaItem) // a single new kind doesn't fail the page
    }

    @Test
    fun unknownStreamSubtypeDecodesToUnknown() {
        val json = """{"#t":"hls-v2","url":"x"}"""
        val source = Codec.json.decodeFromString<StreamSource>(json)
        assertTrue(source is UnknownStream)
    }

    @Test
    fun knownTypesRoundTrip() {
        val song: MediaItem = Song(id = "s1", title = "Hello")
        assertEquals(song, Codec.decode<MediaItem>(Codec.encode(song)))
    }
}
