package fr.outadoc.justchatting.ui.chat

import fr.outadoc.justchatting.component.twitch.parser.model.Chatter

sealed class AutoCompleteItem {
    data class User(val chatter: Chatter) : AutoCompleteItem()
    data class Emote(val emote: fr.outadoc.justchatting.component.twitch.parser.model.Emote) :
        AutoCompleteItem()
}
