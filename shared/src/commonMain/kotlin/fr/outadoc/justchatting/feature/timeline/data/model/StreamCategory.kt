package fr.outadoc.justchatting.feature.timeline.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class StreamCategory(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
)
