package com.github.andreyasadchy.xtra.ui.view.chat

import com.github.andreyasadchy.xtra.model.chat.Emote

fun interface OnEmoteClickedListener {
    fun onEmoteClicked(emote: Emote)
}
