package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.chat.domain.model.Chatter

public sealed class AutoCompleteItem {
    public data class User(
        val chatter: Chatter,
    ) : AutoCompleteItem()

    public data class Emote(
        val emote: fr.outadoc.justchatting.feature.emotes.domain.model.Emote,
    ) : AutoCompleteItem()
}
