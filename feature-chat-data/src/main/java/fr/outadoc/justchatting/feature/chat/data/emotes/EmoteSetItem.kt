package fr.outadoc.justchatting.feature.chat.data.emotes

import fr.outadoc.justchatting.utils.core.StringOrRes

sealed class EmoteSetItem {
    data class Header(
        val title: StringOrRes?,
        val source: StringOrRes?,
        val iconUrl: String? = null
    ) : EmoteSetItem()

    data class Emote(val emote: fr.outadoc.justchatting.component.twitch.model.Emote) :
        EmoteSetItem()
}