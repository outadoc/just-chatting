package fr.outadoc.justchatting.component.twitch.model

import com.google.gson.annotations.SerializedName

data class TwitchEmote(
    val id: String,
    val name: String,
    @SerializedName("emote_set_id")
    val setId: String,
    @SerializedName("owner_id")
    val ownerId: String,
    val format: List<String>,
    val scale: List<String>,
    @SerializedName("theme_mode")
    val themeMode: List<String>,
    val images: Map<String, String>
)
