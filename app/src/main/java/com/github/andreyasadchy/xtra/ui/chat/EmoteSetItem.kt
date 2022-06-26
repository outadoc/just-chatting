package com.github.andreyasadchy.xtra.ui.chat

sealed class EmoteSetItem {
    data class Header(val title: String?) : EmoteSetItem()
    data class Emote(val emote: com.github.andreyasadchy.xtra.model.chat.Emote) : EmoteSetItem()
}
