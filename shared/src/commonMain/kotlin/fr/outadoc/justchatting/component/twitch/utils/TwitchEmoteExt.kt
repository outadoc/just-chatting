package fr.outadoc.justchatting.component.twitch.utils

import fr.outadoc.justchatting.component.chatapi.common.ChatEmote
import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.component.chatapi.common.EmoteUrls
import fr.outadoc.justchatting.component.twitch.http.model.TwitchEmote

private const val BASE_EMOTE_URL: String =
    "https://static-cdn.jtvnw.net/emoticons/v2/{{id}}/{{format}}/{{theme_mode}}/{{scale}}"

private val scales: List<Float> = listOf(1f, 2f, 3f)

private fun createUrlForEmote(
    templateUrl: String = BASE_EMOTE_URL,
    id: String,
    format: String = "default",
    theme: String,
    scale: String,
): String {
    return templateUrl
        .replace("{{id}}", id)
        .replace("{{format}}", format)
        .replace("{{theme_mode}}", theme)
        .replace("{{scale}}", scale)
}

fun TwitchEmote.map(templateUrl: String): Emote {
    return Emote(
        name = name,
        ownerId = ownerId,
        isZeroWidth = false,
        urls = EmoteUrls(
            light = scales.associateWith { scale ->
                createUrlForEmote(
                    templateUrl = templateUrl,
                    id = id,
                    theme = "light",
                    scale = scale.toString(),
                )
            },
            dark = scales.associateWith { scale ->
                createUrlForEmote(
                    templateUrl = templateUrl,
                    id = id,
                    theme = "dark",
                    scale = scale.toString(),
                )
            },
        ),
    )
}

fun ChatEmote.map(): Emote {
    return Emote(
        name = name,
        ownerId = null,
        isZeroWidth = false,
        urls = EmoteUrls(
            light = scales.associateWith { scale ->
                createUrlForEmote(
                    id = id,
                    theme = "light",
                    scale = scale.toString(),
                )
            },
            dark = scales.associateWith { scale ->
                createUrlForEmote(
                    id = id,
                    theme = "dark",
                    scale = scale.toString(),
                )
            },
        ),
    )
}
