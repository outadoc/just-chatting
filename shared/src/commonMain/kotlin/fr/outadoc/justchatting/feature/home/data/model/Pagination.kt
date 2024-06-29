package fr.outadoc.justchatting.feature.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Pagination(
    @SerialName("cursor")
    val cursor: String? = null,
)
