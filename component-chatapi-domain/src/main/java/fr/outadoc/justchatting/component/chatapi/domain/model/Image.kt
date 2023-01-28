package fr.outadoc.justchatting.component.chatapi.domain.model

data class Image(
    val url: String,
    var start: Int,
    var end: Int,
    val isEmote: Boolean,
    val isZeroWidth: Boolean = false,
)
