package fr.outadoc.justchatting.feature.emotes.data.stv.model

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls

private const val FLAG_IS_ZERO_WIDTH = 1 shl 8

internal fun StvEmote.map(): Emote = Emote(
    name = name,
    ownerId = null,
    isZeroWidth = flags.hasFlag(FLAG_IS_ZERO_WIDTH),
    ratio =
    supportedFiles.first().let { file ->
        file.width.toFloat() / file.height.toFloat()
    },
    urls =
    EmoteUrls(
        anyTheme =
        mapOf(
            0f to
                Uri
                    .parse("https:${data.host.baseUrl}")
                    .buildUpon()
                    .appendPath(supportedFiles.first().name)
                    .build()
                    .toString(),
        ),
    ),
)

private val StvEmote.supportedFiles: List<StvEmoteFiles>
    get() =
        data.host.files
            .filter { file -> file.format == "WEBP" }
            .sortedByDescending { file -> file.width }

/**
 * Checks if the given bit position is set on the bitfield.
 *
 * @receiver The bitfield.
 * @param flag The bit position to check.
 */
private fun Int.hasFlag(flag: Int): Boolean = this and flag == flag
