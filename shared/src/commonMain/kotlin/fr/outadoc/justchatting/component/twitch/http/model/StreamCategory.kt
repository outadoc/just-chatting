package fr.outadoc.justchatting.component.twitch.http.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamCategory(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
)
