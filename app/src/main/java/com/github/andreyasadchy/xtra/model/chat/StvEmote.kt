package com.github.andreyasadchy.xtra.model.chat

class StvEmote(
    override val name: String,
    val mime: String,
    override val url: String) : Emote() {

    override val isPng: Boolean
        get() = !mime.endsWith("gif", true)
}