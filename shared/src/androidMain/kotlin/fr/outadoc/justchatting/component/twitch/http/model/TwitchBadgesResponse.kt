package fr.outadoc.justchatting.component.twitch.http.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TwitchBadgesResponse(
    @SerialName("data")
    val badgeSets: List<TwitchBadgeSet>,
)
