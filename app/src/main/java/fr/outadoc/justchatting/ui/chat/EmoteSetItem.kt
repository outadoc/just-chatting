package fr.outadoc.justchatting.ui.chat

sealed class EmoteSetItem {
    data class Header(val title: String?) : EmoteSetItem()
    data class Emote(val emote: fr.outadoc.justchatting.model.chat.Emote) : EmoteSetItem()
}
