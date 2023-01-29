package fr.outadoc.justchatting.component.twitch.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Reward(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("cost")
    val cost: Int,
    @SerialName("background_color")
    val backgroundColor: String? = null,
)
