package fr.outadoc.justchatting.feature.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Reward(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("cost")
    val cost: Int,
    @SerialName("background_color")
    val backgroundColor: String? = null,
)
