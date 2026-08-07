package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import kotlin.test.Test
import kotlin.test.assertEquals

internal class EmoteUrlsExtTest {
    private val urls =
        EmoteUrls(
            anyTheme =
            mapOf(
                1f to "1x",
                2f to "2x",
                4f to "4x",
            ),
        )

    @Test
    fun `Picks the exact density when available`() {
        assertEquals("2x", urls.getBestUrl(screenDensity = 2f, isDarkTheme = false))
    }

    @Test
    fun `Picks the closest density, not the largest`() {
        assertEquals("1x", urls.getBestUrl(screenDensity = 1.2f, isDarkTheme = false))
        assertEquals("2x", urls.getBestUrl(screenDensity = 2.5f, isDarkTheme = false))
    }

    @Test
    fun `Picks the largest density when the screen is denser than all candidates`() {
        assertEquals("4x", urls.getBestUrl(screenDensity = 8f, isDarkTheme = false))
    }
}
