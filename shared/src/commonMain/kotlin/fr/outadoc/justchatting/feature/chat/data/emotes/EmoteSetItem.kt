package fr.outadoc.justchatting.feature.chat.data.emotes

import dev.icerock.moko.resources.desc.StringDesc

sealed class EmoteSetItem {
    data class Header(
        val title: StringDesc?,
        val source: StringDesc?,
        val iconUrl: String? = null,
    ) : EmoteSetItem()

    data class Emote(val emote: fr.outadoc.justchatting.component.chatapi.common.Emote) :
        EmoteSetItem()
}
