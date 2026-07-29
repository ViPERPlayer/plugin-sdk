package com.viperplayer.plugin

import com.viperplayer.plugin.model.Album
import com.viperplayer.plugin.model.AlbumType
import com.viperplayer.plugin.model.FeaturedCardsSection
import com.viperplayer.plugin.model.HomeChip
import com.viperplayer.plugin.model.HomeContent
import com.viperplayer.plugin.model.HomeSection
import com.viperplayer.plugin.model.ItemShape
import com.viperplayer.plugin.model.ListSection
import com.viperplayer.plugin.model.MediaItem
import com.viperplayer.plugin.model.MoodChip
import com.viperplayer.plugin.model.MoodGridSection
import com.viperplayer.plugin.model.MultiBrowseCarouselSection
import com.viperplayer.plugin.model.QuickPicksSection
import com.viperplayer.plugin.model.SearchResult
import com.viperplayer.plugin.model.Song
import com.viperplayer.plugin.model.StreamSource
import com.viperplayer.plugin.model.UnknownMediaItem
import com.viperplayer.plugin.model.UnknownSection
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

    @Test
    fun unknownHomeSectionSubtypeDecodesToUnknown() {
        val json = """{"#t":"stories","id":"x1","title":"New design"}"""
        val section = Codec.json.decodeFromString<HomeSection>(json)
        assertTrue(section is UnknownSection) // a design this build doesn't know is skipped, not thrown
        assertEquals("x1", section.id)
    }

    @Test
    fun multiBrowseCarouselSectionRoundTrips() {
        val section: HomeSection = MultiBrowseCarouselSection(
            id = "daily", title = "Daily Discover",
            items = listOf(Song(id = "s1", title = "T")),
            itemShape = ItemShape.WIDE, showBackdrop = false,
        )
        assertEquals(section, Codec.decode<HomeSection>(Codec.encode(section)))
    }

    @Test
    fun quickPicksSectionRoundTrips() {
        val section: HomeSection = QuickPicksSection(
            id = "quick", title = "Quick Picks",
            items = listOf(Song(id = "s1", title = "T")), rows = 3,
        )
        assertEquals(section, Codec.decode<HomeSection>(Codec.encode(section)))
    }

    @Test
    fun featuredCardsSectionRoundTrips() {
        val section: HomeSection = FeaturedCardsSection(
            id = "featured", title = "From the community",
            items = listOf(Song(id = "s1", title = "T")), itemShape = ItemShape.SQUARE,
        )
        assertEquals(section, Codec.decode<HomeSection>(Codec.encode(section)))
    }

    @Test
    fun moodGridSectionRoundTrips() {
        val section: HomeSection = MoodGridSection(
            id = "moods", title = "Mood and genres",
            chips = listOf(
                MoodChip(label = "Chill", targetId = "c1", accentColor = "#FF8800"),
                MoodChip(label = "Focus", targetId = "c2", artworkUrl = "http://art"),
            ),
        )
        assertEquals(section, Codec.decode<HomeSection>(Codec.encode(section)))
    }

    // ---- HomeContent new OPTIONAL fields (chips + continuation): forward/backward compatibility ----

    @Test
    fun homeContentWithoutNewFields_backwardCompat_decodesToDefaults() {
        // An OLD plugin's payload predates chips + continuation; the new host must fill the defaults.
        val json = """{"sections":[{"#t":"list","id":"l1","title":"Recent"}]}"""
        val content = Codec.json.decodeFromString<HomeContent>(json)
        assertEquals(1, content.sections.size)
        assertTrue("chips absent decodes to empty", content.chips.isEmpty())
        assertEquals("continuation absent decodes to null", null, content.continuation)
    }

    @Test
    fun homeContentWithNewFields_roundTrips() {
        val content = HomeContent(
            sections = listOf(ListSection(id = "l1", title = "Recent", items = listOf(Song(id = "s", title = "T")))),
            chips = listOf(HomeChip(id = "relax", title = "Relax"), HomeChip(id = "focus", title = "Focus")),
            continuation = "page-2",
        )
        assertEquals(content, Codec.decode<HomeContent>(Codec.encode(content)))
    }

    @Test
    fun homeContentWithNewFields_forwardCompat_oldHostIgnoresUnknownExtras() {
        // A NEW plugin also sends fields (and a chip field) an OLD host doesn't know — they're dropped,
        // but the known fields (including our new chips/continuation) still decode.
        val json = """
            {"sections":[],"chips":[{"id":"relax","title":"Relax","emoji":"😌"}],
             "continuation":"tok","futureField":123}
        """.trimIndent()
        val content = Codec.json.decodeFromString<HomeContent>(json)
        assertEquals(1, content.chips.size)
        assertEquals("relax", content.chips[0].id)
        assertEquals("tok", content.continuation)
    }
}
