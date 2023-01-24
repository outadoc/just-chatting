package fr.outadoc.justchatting.component.twitch.model

import com.google.gson.annotations.SerializedName

data class StvEmote(
    val id: String,
    val name: String,
    val mime: String,
    @SerializedName("visibility_simple")
    val visibility: List<String>,
    val urls: List<List<String>>
)

val StvEmote.isZeroWidth: Boolean
    get() = "ZERO_WIDTH" in visibility
