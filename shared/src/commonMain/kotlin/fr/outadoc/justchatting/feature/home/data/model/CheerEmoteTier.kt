package fr.outadoc.justchatting.feature.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CheerEmoteTier(
    @SerialName("id")
    val id: String,
    @SerialName("min_bits")
    val minBits: Int,
    @SerialName("color")
    val color: String? = null,
    @SerialName("images")
    val images: Map<String, Map<String, Map<String, String>>>,
)
