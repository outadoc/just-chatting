package fr.outadoc.justchatting.feature.chat.data.http

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TwitchBadgeVersion(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("image_url_1x")
    val image1x: String,
    @SerialName("image_url_2x")
    val image2x: String,
    @SerialName("image_url_4x")
    val image4x: String,
)
