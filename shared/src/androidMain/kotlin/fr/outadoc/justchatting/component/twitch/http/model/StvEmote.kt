package fr.outadoc.justchatting.component.twitch.http.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StvEmote(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("mime")
    val mime: String,
    @SerialName("visibility_simple")
    val visibility: List<String>,
    @SerialName("urls")
    val urls: List<List<String>>,
)
