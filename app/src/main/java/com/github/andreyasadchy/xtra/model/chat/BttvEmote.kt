package com.github.andreyasadchy.xtra.model.chat

import com.github.andreyasadchy.xtra.ui.view.chat.emoteQuality

class BttvEmote(
        val id: String,
        override val name: String,
        private val imageType: String) : Emote() {

    override val url: String
        get() = "https://cdn.betterttv.net/emote/$id/${emoteQuality}x"

    override val isPng: String
        get() = "image/$imageType"

    override val zerowidth: Boolean
        get() = name == "SoSnowy"||name == "IceCold"||name == "SantaHat"||name == "TopHat"||name == "ReinDeer"||name == "CandyCane"||name == "cvMask"||name == "cvHazmat"
}