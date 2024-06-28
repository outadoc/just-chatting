package fr.outadoc.justchatting.component.twitch.http.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TwitchEmote(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("emote_set_id")
    val setId: String,
    @SerialName("owner_id")
    val ownerId: String,
    @SerialName("format")
    val format: List<String>,
    @SerialName("scale")
    val scale: List<String>,
    @SerialName("theme_mode")
    val themeMode: List<String>,
    @SerialName("images")
    val images: Map<String, String>,
)
