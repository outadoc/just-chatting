package fr.outadoc.justchatting.feature.emotes.domain.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.utils.resources.StringDesc2

@Immutable
internal sealed class EmoteSetItem {
    data class Header(
        val title: StringDesc2?,
        val source: StringDesc2?,
        val iconUrl: String? = null,
    ) : EmoteSetItem()

    data class Emote(val emote: fr.outadoc.justchatting.feature.emotes.domain.model.Emote) : EmoteSetItem()
}
