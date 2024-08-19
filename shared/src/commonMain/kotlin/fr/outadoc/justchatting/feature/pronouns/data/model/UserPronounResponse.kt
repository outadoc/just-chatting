package fr.outadoc.justchatting.feature.pronouns.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserPronounResponse(
    @SerialName("channel_id")
    val userId: String,
    @SerialName("pronoun_id")
    val pronounId: String,
    @SerialName("alt_pronoun_id")
    val altPronounId: String?,
)
