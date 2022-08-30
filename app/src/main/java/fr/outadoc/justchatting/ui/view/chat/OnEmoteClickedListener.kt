package fr.outadoc.justchatting.ui.view.chat

import fr.outadoc.justchatting.model.chat.Emote

fun interface OnEmoteClickedListener {
    fun onEmoteClicked(emote: Emote)
}
