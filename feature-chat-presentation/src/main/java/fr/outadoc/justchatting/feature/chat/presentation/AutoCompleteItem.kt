package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.component.twitch.model.Chatter

sealed class AutoCompleteItem {
    data class User(val chatter: Chatter) : AutoCompleteItem()
    data class Emote(val emote: fr.outadoc.justchatting.component.twitch.model.Emote) :
        AutoCompleteItem()
}