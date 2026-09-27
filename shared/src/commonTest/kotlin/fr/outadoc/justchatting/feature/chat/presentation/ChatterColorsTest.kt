package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ChatterColorsTest {
    private val white = 0xFFFFFFFF.toInt()
    private val black = 0xFF000000.toInt()

    @Test
    fun `readable color is kept as is`() {
        assertEquals(0xFF000080.toInt(), ChatterColors.accessibleColor(hexColor = "#000080", background = white))
    }

    @Test
    fun `unreadable color is made lighter`() {
        val adjusted = assertNotNull(ChatterColors.accessibleColor(hexColor = "#000080", background = black))

        fun Int.luminanceSum() = ((this shr 16) and 0xFF) + ((this shr 8) and 0xFF) + (this and 0xFF)
        assertTrue(adjusted.luminanceSum() > 0xFF000080.toInt().luminanceSum())
        assertEquals(0xFF, (adjusted ushr 24) and 0xFF)
    }

    @Test
    fun `missing or invalid colors fall back to the palette`() {
        assertNull(ChatterColors.accessibleColor(hexColor = null, background = white))
        assertNull(ChatterColors.accessibleColor(hexColor = "#nothex", background = white))
    }

    @Test
    fun `fallback index is stable and in range`() {
        val chatter = Chatter(id = "12345", login = "chatter", displayName = "Chatter")
        val index = ChatterColors.fallbackIndex(chatter = chatter, paletteSize = 15)

        assertTrue(index in 0 until 15)
        assertEquals(index, ChatterColors.fallbackIndex(chatter = chatter.copy(displayName = "Renamed"), paletteSize = 15))
    }

    @Test
    fun `fallback index matches the pick the Compose UI used to make`() {
        val chatter = Chatter(id = "12345", login = "chatter", displayName = "Chatter")
        val palette = (0 until 15).toSet()

        assertEquals(
            palette.random(Random(chatter.hashCode())),
            ChatterColors.fallbackIndex(chatter = chatter, paletteSize = palette.size),
        )
    }
}
