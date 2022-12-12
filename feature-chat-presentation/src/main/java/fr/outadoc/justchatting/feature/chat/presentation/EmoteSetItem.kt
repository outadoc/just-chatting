package fr.outadoc.justchatting.feature.chat.presentation

sealed class EmoteSetItem {
    data class Header(val title: String?) : EmoteSetItem()
    data class Emote(val emote: fr.outadoc.justchatting.component.twitch.model.Emote) : EmoteSetItem()
}