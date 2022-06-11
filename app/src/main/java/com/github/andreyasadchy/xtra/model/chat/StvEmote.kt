package com.github.andreyasadchy.xtra.model.chat

class StvEmote(
    override val name: String,
    override val type: String,
    override val url: String,
    override val isZeroWidth: Boolean
) : Emote()
