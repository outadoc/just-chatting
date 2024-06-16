package fr.outadoc.justchatting.feature.chat.data.emotes

sealed class EmoteSetItem {
    data class Header(
        val title: String?,
        val source: String?,
        val iconUrl: String? = null,
    ) : EmoteSetItem()

    data class Emote(val emote: fr.outadoc.justchatting.component.chatapi.common.Emote) :
        EmoteSetItem()
}
