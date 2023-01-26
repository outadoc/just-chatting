package fr.outadoc.justchatting.component.twitch.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ValidationResponse(
    @SerialName("client_id")
    val clientId: String,
    @SerialName("login")
    val login: String,
    @SerialName("user_id")
    val userId: String
)
