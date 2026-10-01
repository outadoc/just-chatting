package fr.outadoc.justchatting.feature.chat.data.http

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserEmotesResponse(
    @SerialName("data")
    val data: List<UserEmote>,
    @SerialName("pagination")
    val pagination: Pagination? = null,
) {
    @Serializable
    data class UserEmote(
        @SerialName("emote_set_id")
        val emoteSetId: String,
    )

    @Serializable
    data class Pagination(
        @SerialName("cursor")
        val cursor: String? = null,
    )
}
