package fr.outadoc.justchatting.feature.chat.data.emotes

sealed class EmoteSetItem {
    data class Header(
        val title: String?,
        val source: String?
    ) : EmoteSetItem()

    data class Emote(val emote: fr.outadoc.justchatting.component.twitch.model.Emote) :
        EmoteSetItem()
}