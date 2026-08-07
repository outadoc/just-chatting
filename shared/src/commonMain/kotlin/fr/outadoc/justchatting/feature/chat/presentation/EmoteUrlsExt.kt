package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import kotlin.math.abs

public fun EmoteUrls.getBestUrl(
    screenDensity: Float,
    isDarkTheme: Boolean,
): String = (if (isDarkTheme) dark else light)
    .minByOrNull { (density, _) -> abs(screenDensity - density) }
    ?.value
    ?: error("No urls available for this emote")
