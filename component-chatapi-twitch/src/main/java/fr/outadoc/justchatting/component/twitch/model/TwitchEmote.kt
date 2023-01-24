package fr.outadoc.justchatting.component.twitch.model

import com.google.gson.annotations.SerializedName

data class TwitchEmote(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val setId: String? = null,
    val ownerId: String? = null,
    val supportedFormats: List<String>,
    val supportedScales: List<String>,
    val supportedThemes: List<String>,
    val urlTemplate: String
)
