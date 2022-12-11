package fr.outadoc.justchatting.ui.chat

sealed class EmoteSetItem {
    data class Header(val title: String?) : EmoteSetItem()
    data class Emote(val emote: fr.outadoc.justchatting.component.twitch.parser.model.Emote) : EmoteSetItem()
}
