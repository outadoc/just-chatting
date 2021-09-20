package com.github.andreyasadchy.xtra.model.chat

import com.github.andreyasadchy.xtra.ui.view.chat.emoteQuality
import com.google.gson.annotations.SerializedName

class TwitchEmote(
        @SerializedName("_id")
        override val name: String,
        var begin: Int,
        var end: Int,
        override val isPng: String = "image/png") : Emote() {

    override val url: String
        get() = "https://static-cdn.jtvnw.net/emoticons/v2/$name/default/dark/$emoteQuality.0"
}
