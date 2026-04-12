package fr.outadoc.justchatting.feature.emotes.data.bttv.model

import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import fr.outadoc.justchatting.utils.core.filterKeysNotNull
import fr.outadoc.justchatting.utils.core.filterValuesNotNull

internal fun FfzEmote.map(): Emote = Emote(
    name = code,
    ownerId = null,
    isZeroWidth = false,
    urls =
    EmoteUrls(
        images
            .mapKeys { (key, _) ->
                key
                    .removeSuffix("x")
                    .toFloatOrNull()
            }.filterKeysNotNull()
            .filterValuesNotNull(),
    ),
)
