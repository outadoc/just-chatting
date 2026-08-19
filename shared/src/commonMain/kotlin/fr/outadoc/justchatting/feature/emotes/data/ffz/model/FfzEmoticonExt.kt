package fr.outadoc.justchatting.feature.emotes.data.ffz.model

import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import fr.outadoc.justchatting.utils.core.filterKeysNotNull
import fr.outadoc.justchatting.utils.core.filterValuesNotNull

internal fun FfzEmoticon.map(): Emote =
    Emote(
        name = name,
        ownerId = null,
        isZeroWidth = false,
        ratio = width.toFloat() / height.toFloat(),
        urls =
            EmoteUrls(
                urls
                    .mapKeys { (key, _) -> key.toFloatOrNull() }
                    .filterKeysNotNull()
                    .filterValuesNotNull(),
            ),
    )
