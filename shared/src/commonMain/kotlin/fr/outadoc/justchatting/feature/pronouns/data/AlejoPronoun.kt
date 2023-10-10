package fr.outadoc.justchatting.feature.pronouns.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlejoPronoun(
    @SerialName("name")
    val id: String,
    @SerialName("display")
    val display: String,
)
