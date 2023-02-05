package fr.outadoc.justchatting.feature.chat.presentation

sealed class AutoCompleteItem {
    data class User(val chatter: Chatter) : AutoCompleteItem()
    data class Emote(val emote: fr.outadoc.justchatting.component.chatapi.common.Emote) :
        AutoCompleteItem()
}
