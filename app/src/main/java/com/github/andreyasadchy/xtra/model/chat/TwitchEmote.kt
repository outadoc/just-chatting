package com.github.andreyasadchy.xtra.model.chat

import com.github.andreyasadchy.xtra.ui.view.chat.emoteQuality
import com.google.gson.annotations.SerializedName

class TwitchEmote(
        @SerializedName("_id")
        override val name: String,
        var begin: Int = 0,
        var end: Int = 0,
        override val type: String = "image/png",
        override val url: String = "https://static-cdn.jtvnw.net/emoticons/v2/$name/default/dark/$emoteQuality.0") : Emote()
