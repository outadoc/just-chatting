package fr.outadoc.justchatting.component.twitch.model

data class StvEmote(
    val name: String,
    val isZeroWidth: Boolean,
    val urls: Map<Float, String>
)
