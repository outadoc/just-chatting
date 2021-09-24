package com.github.andreyasadchy.xtra.model.chat

import com.github.andreyasadchy.xtra.ui.view.chat.emoteQuality
import com.google.gson.annotations.SerializedName

private const val BTTV_URL = "https://cdn.betterttv.net/emote/"

class BttvEmote(
        val id: String,
        @SerializedName("code")
        override val name: String,
        val imageType: String) : Emote() {

    override val url: String
        get() = "$BTTV_URL$id/${emoteQuality}x"

    override val isPng: String
        get() = "image/$imageType"

    override val zerowidth: Boolean
        get() = name == "SoSnowy"||name == "IceCold"||name == "SantaHat"||name == "TopHat"||name == "ReinDeer"||name == "CandyCane"||name == "cvMask"||name == "cvHazmat"
}