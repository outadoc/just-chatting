package fr.outadoc.justchatting.component.twitch.model

import com.google.gson.annotations.SerializedName

data class TwitchBadgeVersion(
    val title: String,
    val description: String,
    @SerializedName("image_url_1x")
    val image1x: String,
    @SerializedName("image_url_2x")
    val image2x: String,
    @SerializedName("image_url_4x")
    val image4x: String,
)