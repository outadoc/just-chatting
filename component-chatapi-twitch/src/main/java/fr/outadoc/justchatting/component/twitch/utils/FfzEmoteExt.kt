package fr.outadoc.justchatting.component.twitch.utils

import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.component.chatapi.common.EmoteUrls
import fr.outadoc.justchatting.component.twitch.http.model.FfzEmote
import fr.outadoc.justchatting.utils.core.filterKeysNotNull
import fr.outadoc.justchatting.utils.core.filterValuesNotNull

fun FfzEmote.map(): Emote {
    return Emote(
        name = code,
        ownerId = null,
        isZeroWidth = false,
        urls = EmoteUrls(
            images.mapKeys { (key, _) ->
                key.removeSuffix("x")
                    .toFloatOrNull()
            }
                .filterKeysNotNull()
                .filterValuesNotNull(),
        ),
    )
}
