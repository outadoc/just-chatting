package fr.outadoc.justchatting.ui.chat

sealed class EmoteSetItem {
    data class Header(val title: String?) : EmoteSetItem()
    data class Emote(val emote: fr.outadoc.justchatting.component.twitch.model.chat.Emote) : EmoteSetItem()
}
