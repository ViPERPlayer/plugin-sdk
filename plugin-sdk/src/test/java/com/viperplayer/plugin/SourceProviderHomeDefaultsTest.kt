package com.viperplayer.plugin

import com.viperplayer.plugin.author.SourceProvider
import com.viperplayer.plugin.author.StreamResponse
import com.viperplayer.plugin.model.HomeChip
import com.viperplayer.plugin.model.HomeContent
import com.viperplayer.plugin.model.ListSection
import com.viperplayer.plugin.model.MediaType
import com.viperplayer.plugin.model.SearchRequest
import com.viperplayer.plugin.model.SearchResult
import com.viperplayer.plugin.model.Song
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the backward-compatibility contract of the OPTIONAL chip/continuation home verbs on the
 * *author* side: a plugin that only implements the base [SourceProvider.getHome] and overrides
 * neither `getHome(chipId)` nor `getHomeContinuation` still behaves correctly —
 *  - chip-filtered requests fall back to the base feed (chip ignored),
 *  - continuation requests return an empty page (no pagination).
 * A plugin that DOES emit chips + a continuation can, of course, override them.
 */
class SourceProviderHomeDefaultsTest {

    /** A minimal source that only knows its base home feed — like a plugin built before chips existed. */
    private class LegacySource : SourceProvider {
        val base = HomeContent(
            sections = listOf(ListSection(id = "recent", title = "Recent")),
        )

        override suspend fun getHome(): HomeContent = base

        // Required abstract members — irrelevant to this test.
        override suspend fun search(request: SearchRequest): SearchResult = SearchResult()
        override suspend fun getSong(id: String): Song = Song(id = id, title = "T")
        override suspend fun resolveStream(songId: String, type: MediaType, maxBitrateKbps: Int?): StreamResponse =
            StreamResponse.url("http://x")
    }

    /** A modern source that overrides the chip + continuation verbs. */
    private class ModernSource : SourceProvider {
        override suspend fun getHome(): HomeContent = HomeContent(
            sections = listOf(ListSection(id = "base", title = "Base")),
            chips = listOf(HomeChip(id = "relax", title = "Relax")),
            continuation = "page-2",
        )

        override suspend fun getHome(chipId: String?): HomeContent =
            if (chipId == null) getHome()
            else HomeContent(sections = listOf(ListSection(id = "filtered-$chipId", title = "Filtered")))

        override suspend fun getHomeContinuation(continuation: String): HomeContent =
            HomeContent(sections = listOf(ListSection(id = "more-$continuation", title = "More")))

        override suspend fun search(request: SearchRequest): SearchResult = SearchResult()
        override suspend fun getSong(id: String): Song = Song(id = id, title = "T")
        override suspend fun resolveStream(songId: String, type: MediaType, maxBitrateKbps: Int?): StreamResponse =
            StreamResponse.url("http://x")
    }

    @Test
    fun legacySource_chipFilteredHome_fallsBackToBaseFeed() = runBlocking {
        val source = LegacySource()
        // A chip tap on a plugin that doesn't override getHome(chipId) returns the base feed unchanged.
        assertEquals(source.base, source.getHome("some-chip"))
        assertEquals(source.base, source.getHome(null))
    }

    @Test
    fun legacySource_continuation_returnsEmptyPage() = runBlocking {
        val source = LegacySource()
        val page = source.getHomeContinuation("whatever")
        assertTrue("no sections", page.sections.isEmpty())
        assertEquals("no further token", null, page.continuation)
    }

    @Test
    fun modernSource_honorsChipAndContinuation() = runBlocking {
        val source = ModernSource()
        assertEquals("relax", source.getHome().chips.single().id)
        assertEquals("filtered-relax", source.getHome("relax").sections.single().id)
        assertEquals("more-page-2", source.getHomeContinuation("page-2").sections.single().id)
    }
}
