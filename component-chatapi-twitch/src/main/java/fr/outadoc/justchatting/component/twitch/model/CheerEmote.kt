package fr.outadoc.justchatting.component.twitch.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheerEmote(
    @SerialName("prefix")
    val prefix: String,
    @SerialName("tiers")
    val tiers: List<CheerEmoteTier>
)
