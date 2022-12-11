package fr.outadoc.justchatting.component.twitch.model.chat

data class Image(
    val url: String,
    var start: Int,
    var end: Int,
    val isEmote: Boolean,
    val isZeroWidth: Boolean = false
)
