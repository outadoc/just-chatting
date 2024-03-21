package fr.outadoc.justchatting.component.twitch.http.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StvEmoteData(
    @SerialName("animated")
    val animated: Boolean,
)
