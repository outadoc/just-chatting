package fr.outadoc.justchatting.ui.view.chat

import fr.outadoc.justchatting.model.chat.Chatter
import fr.outadoc.justchatting.model.chat.Emote

sealed class AutoCompleteItem {
    data class EmoteItem(val emote: Emote) : AutoCompleteItem() {
        override fun toString() = ":${emote.name}"
    }

    data class UserItem(val chatter: Chatter) : AutoCompleteItem() {
        override fun toString() = "@${chatter.name}"
    }
}
