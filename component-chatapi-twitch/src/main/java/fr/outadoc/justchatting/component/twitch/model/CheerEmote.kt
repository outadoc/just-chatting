package fr.outadoc.justchatting.component.twitch.model

data class CheerEmote(
    val name: String,
    val minBits: Int,
    val color: String? = null,
    val images: List<Image>
) {
    data class Image(
        val theme: String,
        val isAnimated: Boolean,
        val dpiScale: Float,
        val url: String
    )
}
