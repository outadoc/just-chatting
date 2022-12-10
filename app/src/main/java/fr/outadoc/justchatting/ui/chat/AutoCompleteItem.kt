package fr.outadoc.justchatting.ui.chat

sealed class AutoCompleteItem {
    data class User(val username: String) : AutoCompleteItem()
    data class Emote(val emote: fr.outadoc.justchatting.model.chat.Emote) : AutoCompleteItem()
}
