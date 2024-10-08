package fr.outadoc.justchatting.feature.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class TwitchAuthValidationResponse(
    @SerialName("client_id")
    val clientId: String,
    @SerialName("login")
    val login: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("scopes")
    val scopes: List<String>,
)
