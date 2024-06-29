package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.chat.domain.model.Chatter

internal sealed class AutoCompleteItem {
    data class User(val chatter: Chatter) : AutoCompleteItem()
    data class Emote(val emote: fr.outadoc.justchatting.feature.emotes.domain.model.Emote) : AutoCompleteItem()
}
