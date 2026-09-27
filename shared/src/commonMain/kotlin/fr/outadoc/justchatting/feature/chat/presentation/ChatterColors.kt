package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.utils.presentation.ensureColorIsAccessible
import fr.outadoc.justchatting.utils.presentation.parseHexColorArgb
import kotlin.random.Random

/**
 * How to color a chatter's name. Colors are ARGB ints.
 */
public object ChatterColors {
    /**
     * The color the chatter picked, [hexColor], adjusted to be readable on [background]; or null if
     * they haven't picked one, or if it can't be made readable.
     */
    public fun accessibleColor(
        hexColor: String?,
        background: Int,
    ): Int? =
        hexColor
            ?.parseHexColorArgb()
            ?.let { color -> ensureColorIsAccessible(foreground = color, background = background) }

    /**
     * Index of the color to use in a palette of [paletteSize] fallback colors, for chatters who
     * don't have a readable color of their own. Always the same for a given chatter, on every
     * platform.
     */
    public fun fallbackIndex(
        chatter: Chatter,
        paletteSize: Int,
    ): Int = Random(chatter.hashCode()).nextInt(paletteSize)
}
