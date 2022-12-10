package fr.outadoc.justchatting.ui.chat

import fr.outadoc.justchatting.model.chat.Chatter

sealed class AutoCompleteItem {
    data class User(val chatter: Chatter) : AutoCompleteItem()
    data class Emote(val emote: fr.outadoc.justchatting.model.chat.Emote) : AutoCompleteItem()
}
