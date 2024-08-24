package fr.outadoc.justchatting.feature.chat.data.http

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CheerEmote(
    @SerialName("prefix")
    val prefix: String,
    @SerialName("tiers")
    val tiers: List<CheerEmoteTier>,
)
