package fr.outadoc.justchatting.feature.chat.data.http

import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls

internal fun CheerEmoteTier.map(prefix: String): Emote {
    return Emote(
        name = "${prefix}$id",
        bitsValue = minBits,
        colorHex = color,
        urls = EmoteUrls(
            dark = images["dark"].map(),
            light = images["light"].map(),
        ),
    )
}

private fun Map<String, Map<String, String>>?.map(): Map<Float, String> = this?.get("animated")?.mapKeys { it.key.toFloat() }.orEmpty()
