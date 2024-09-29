package fr.outadoc.justchatting.feature.emotes.domain.model

import androidx.compose.runtime.Immutable
import dev.icerock.moko.resources.desc.StringDesc

@Immutable
internal sealed class EmoteSetItem {
    data class Header(
        val title: StringDesc?,
        val source: StringDesc?,
        val iconUrl: String? = null,
    ) : EmoteSetItem()

    data class Emote(val emote: fr.outadoc.justchatting.feature.emotes.domain.model.Emote) : EmoteSetItem()
}
