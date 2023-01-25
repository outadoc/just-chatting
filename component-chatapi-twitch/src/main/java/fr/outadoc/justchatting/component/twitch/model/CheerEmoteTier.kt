package fr.outadoc.justchatting.component.twitch.model

import com.google.gson.annotations.SerializedName

data class CheerEmoteTier(
    val id: String,
    @SerializedName("min_bits")
    val minBits: Int,
    val color: String? = null,
    val images: Map<String, Map<String, Map<String, String>>>
)
