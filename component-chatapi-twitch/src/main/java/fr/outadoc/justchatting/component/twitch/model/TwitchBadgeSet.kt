package fr.outadoc.justchatting.component.twitch.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TwitchBadgeSet(
    @SerialName("versions")
    val versions: Map<String, TwitchBadgeVersion>,
)
