package com.github.andreyasadchy.xtra.model.chat

import com.github.andreyasadchy.xtra.ui.view.chat.ChatView

class BttvEmote(
    val id: String,
    override val name: String,
    private val imageType: String
) : Emote() {

    companion object {
        private val ZERO_WIDTH_EMOTES = setOf(
            "SoSnowy",
            "IceCold",
            "SantaHat",
            "TopHat",
            "ReinDeer",
            "CandyCane",
            "cvMask",
            "cvHazmat"
        )
    }

    override val url: String
        get() {
            val quality = when (ChatView.emoteQuality) {
                "4" -> ("3x")
                "3" -> ("2x")
                "2" -> ("2x")
                else -> ("1x")
            }

            return "https://cdn.betterttv.net/emote/$id/$quality"
        }

    override val type: String
        get() = "image/$imageType"

    override val isZeroWidth: Boolean
        get() = name in ZERO_WIDTH_EMOTES
}
