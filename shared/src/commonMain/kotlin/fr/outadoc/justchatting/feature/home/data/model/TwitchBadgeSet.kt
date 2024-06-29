package fr.outadoc.justchatting.feature.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TwitchBadgeSet(
    @SerialName("set_id")
    val setId: String,
    @SerialName("versions")
    val versions: List<TwitchBadgeVersion>,
)
