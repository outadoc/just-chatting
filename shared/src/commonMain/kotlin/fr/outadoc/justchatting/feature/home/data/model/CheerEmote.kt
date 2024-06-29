package fr.outadoc.justchatting.feature.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CheerEmote(
    @SerialName("prefix")
    val prefix: String,
    @SerialName("tiers")
    val tiers: List<CheerEmoteTier>,
)
