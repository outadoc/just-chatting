package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls

internal fun EmoteUrls.getBestUrl(screenDensity: Float, isDarkTheme: Boolean): String {
    return (if (isDarkTheme) dark else light)
        .minByOrNull { (density, _) -> screenDensity - density }
        ?.value
        ?: error("No urls available for this emote")
}
