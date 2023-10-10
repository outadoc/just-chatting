package fr.outadoc.justchatting.component.twitch.utils

import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.component.chatapi.common.EmoteUrls
import fr.outadoc.justchatting.component.twitch.http.model.BttvEmote

private val zeroWidthEmotes = setOf(
    "SoSnowy",
    "IceCold",
    "SantaHat",
    "TopHat",
    "ReinDeer",
    "CandyCane",
    "cvMask",
    "cvHazmat",
)

private val availableDensities = mapOf(
    1.0f to "1x",
    2.0f to "2x",
    3.0f to "3x",
)

fun BttvEmote.map(): Emote {
    return Emote(
        name = code,
        ownerId = null,
        isZeroWidth = code in zeroWidthEmotes,
        urls = EmoteUrls(
            availableDensities.mapValues { (_, densityStr) ->
                "https://cdn.betterttv.net/emote/$id/$densityStr"
            },
        ),
    )
}
