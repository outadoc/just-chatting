package fr.outadoc.justchatting.component.twitch.utils

import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.component.chatapi.common.EmoteUrls
import fr.outadoc.justchatting.component.twitch.http.model.CheerEmoteTier

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

private fun Map<String, Map<String, String>>?.map(): Map<Float, String> =
    this?.get("animated")?.mapKeys { it.key.toFloat() }.orEmpty()
