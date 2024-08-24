package fr.outadoc.justchatting.feature.chat.data.http

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TwitchBadgeSet(
    @SerialName("set_id")
    val setId: String,
    @SerialName("versions")
    val versions: List<TwitchBadgeVersion>,
)
