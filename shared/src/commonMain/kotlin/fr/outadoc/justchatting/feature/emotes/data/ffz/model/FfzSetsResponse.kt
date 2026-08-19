package fr.outadoc.justchatting.feature.emotes.data.ffz.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Shared shape of the /set/global and /room/id/:id responses; both expose a "sets" map.
@Serializable
internal data class FfzSetsResponse(
    @SerialName("sets")
    val sets: Map<String, FfzSet>,
)
