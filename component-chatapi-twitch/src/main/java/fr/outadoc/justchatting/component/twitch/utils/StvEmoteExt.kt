package fr.outadoc.justchatting.component.twitch.utils

import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.component.chatapi.common.EmoteUrls
import fr.outadoc.justchatting.component.twitch.http.model.StvEmote

fun StvEmote.map(): Emote {
    return Emote(
        name = name,
        ownerId = null,
        isZeroWidth = "ZERO_WIDTH" in visibility,
        urls = EmoteUrls(
            urls.associate { (density, url) ->
                density.toFloat() to url
            },
        ),
    )
}
