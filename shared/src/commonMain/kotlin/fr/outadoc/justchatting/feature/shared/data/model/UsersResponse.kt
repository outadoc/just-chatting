package fr.outadoc.justchatting.feature.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UsersResponse(
    @SerialName("data")
    val data: List<User>,
)
