package fr.outadoc.justchatting.feature.emotes.domain.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.utils.resources.StringDesc

@Immutable
public sealed class EmoteSetItem {
    public data class Header(
        val title: StringDesc?,
        val source: StringDesc?,
        val iconUrl: String? = null,
    ) : EmoteSetItem()

    public data class Emote(
        val emote: fr.outadoc.justchatting.feature.emotes.domain.model.Emote,
    ) : EmoteSetItem()
}
