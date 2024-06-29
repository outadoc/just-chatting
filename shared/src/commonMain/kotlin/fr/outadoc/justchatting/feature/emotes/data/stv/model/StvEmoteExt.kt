package fr.outadoc.justchatting.feature.emotes.data.stv.model

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls

private const val BASE_URL = "https://cdn.7tv.app/emote/"
private const val FLAG_IS_ZERO_WIDTH = 1 shl 8

internal fun StvEmote.map(): Emote {
    return Emote(
        name = name,
        ownerId = null,
        isZeroWidth = flags.hasFlag(FLAG_IS_ZERO_WIDTH),
        urls = EmoteUrls(
            anyTheme = mapOf(
                formatUrl(density = 1f, versionId = "1x", emoteId = id),
                formatUrl(density = 2f, versionId = "2x", emoteId = id),
                formatUrl(density = 4f, versionId = "4x", emoteId = id),
            ),
        ),
    )
}

/**
 * Checks if the given bit position is set on the bitfield.
 *
 * @receiver The bitfield.
 * @param flag The bit position to check.
 */
private fun Int.hasFlag(flag: Int): Boolean {
    return this and flag == flag
}

private fun formatUrl(density: Float, versionId: String, emoteId: String): Pair<Float, String> {
    return density to Uri.parse(BASE_URL)
        .buildUpon()
        .appendPath(emoteId)
        .appendPath("$versionId.webp")
        .build()
        .toString()
}
