package com.github.andreyasadchy.xtra.model.chat

class CheerEmote(
    override val name: String,
    val minBits: Int,
    val color: String? = null,
    override val type: String,
    override val url: String
) : Emote()
