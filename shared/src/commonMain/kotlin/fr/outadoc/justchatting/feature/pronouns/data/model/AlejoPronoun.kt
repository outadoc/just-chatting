package fr.outadoc.justchatting.feature.pronouns.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AlejoPronoun(
    @SerialName("name")
    val id: String,
    @SerialName("display")
    val display: String,
)
