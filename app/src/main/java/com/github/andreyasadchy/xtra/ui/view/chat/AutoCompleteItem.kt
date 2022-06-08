package com.github.andreyasadchy.xtra.ui.view.chat

import com.github.andreyasadchy.xtra.model.chat.Chatter
import com.github.andreyasadchy.xtra.model.chat.Emote

sealed class AutoCompleteItem {
    data class EmoteItem(val emote: Emote) : AutoCompleteItem()
    data class UserItem(val chatter: Chatter) : AutoCompleteItem()
}
