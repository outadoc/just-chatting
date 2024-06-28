package fr.outadoc.justchatting.component.twitch.http.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Pagination(
    @SerialName("cursor")
    val cursor: String? = null,
)
