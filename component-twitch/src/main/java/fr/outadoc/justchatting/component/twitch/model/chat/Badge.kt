package fr.outadoc.justchatting.component.twitch.model.chat

import com.google.gson.annotations.SerializedName

data class Badge(
    @SerializedName("_id")
    val id: String,
    val version: String
)
