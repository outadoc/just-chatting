package fr.outadoc.justchatting.feature.chat.data.http

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TwitchBadgesResponse(
    @SerialName("data")
    val badgeSets: List<TwitchBadgeSet>,
)
