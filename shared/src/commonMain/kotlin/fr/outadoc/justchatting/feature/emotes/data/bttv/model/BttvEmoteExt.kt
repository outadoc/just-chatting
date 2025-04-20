package fr.outadoc.justchatting.feature.emotes.data.bttv.model

import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import fr.outadoc.justchatting.feature.shared.data.ApiEndpoints

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

internal fun BttvEmote.map(): Emote {
    return Emote(
        name = code,
        ownerId = null,
        isZeroWidth = code in zeroWidthEmotes,
        ratio = if (height != null && width != null) {
            width.toFloat() / height.toFloat()
        } else {
            1f
        },
        urls = EmoteUrls(
            availableDensities.mapValues { (_, densityStr) ->
                "${ApiEndpoints.BTTV_EMOTE_CDN}/$id/$densityStr"
            },
        ),
    )
}
