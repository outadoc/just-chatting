package fr.outadoc.justchatting.feature.pronouns.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserPronounResponse(
    @SerialName("id")
    val id: String,
    @SerialName("login")
    val login: String,
    @SerialName("pronoun_id")
    val pronounId: String,
    @SerialName("alt_pronoun_id")
    val altPronounId: String? = null,
)
