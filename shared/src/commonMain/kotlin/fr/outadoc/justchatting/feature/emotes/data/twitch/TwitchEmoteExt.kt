package fr.outadoc.justchatting.feature.emotes.data.twitch

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEmote
import fr.outadoc.justchatting.feature.emotes.data.twitch.model.TwitchEmote
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import fr.outadoc.justchatting.feature.shared.data.ApiEndpoints

private const val BASE_EMOTE_URL: String =
    "${ApiEndpoints.TWITCH_EMOTE_CDN}/v2/{{id}}/{{format}}/{{theme_mode}}/{{scale}}"

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

internal fun TwitchEmote.map(templateUrl: String): Emote {
    return Emote(
        name = name,
        ownerId = ownerId,
        isZeroWidth = false,
        urls =
            EmoteUrls(
                light =
                    scales.associateWith { scale ->
                        createUrlForEmote(
                            templateUrl = templateUrl,
                            id = id,
                            theme = "light",
                            scale = scale.toString(),
                        )
                    },
                dark =
                    scales.associateWith { scale ->
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

internal fun ChatEmote.map(): Emote {
    return Emote(
        name = name,
        ownerId = null,
        isZeroWidth = false,
        urls =
            EmoteUrls(
                light =
                    scales.associateWith { scale ->
                        createUrlForEmote(
                            id = id,
                            theme = "light",
                            scale = scale.toString(),
                        )
                    },
                dark =
                    scales.associateWith { scale ->
                        createUrlForEmote(
                            id = id,
                            theme = "dark",
                            scale = scale.toString(),
                        )
                    },
            ),
    )
}
