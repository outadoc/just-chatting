package fr.outadoc.justchatting.feature.search.data.model

import fr.outadoc.justchatting.feature.shared.data.model.Pagination
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChannelSearchResponse(
    @SerialName("data")
    val data: List<ChannelSearch>,
    @SerialName("pagination")
    val pagination: Pagination,
)
