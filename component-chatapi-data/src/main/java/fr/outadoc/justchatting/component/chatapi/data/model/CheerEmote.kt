package fr.outadoc.justchatting.component.chatapi.data.model

class CheerEmote(
    override val name: String,
    val minBits: Int,
    val color: String? = null,
    private val images: List<Image>
) : Emote() {

    data class Image(
        val theme: String,
        val isAnimated: Boolean,
        val dpiScale: Float,
        val url: String
    )

    override fun getUrl(animate: Boolean, screenDensity: Float, isDarkTheme: Boolean): String {
        val preferredTheme = if (isDarkTheme) "dark" else "light"
        return images.filter { image -> image.isAnimated == animate }.ifEmpty { images }
            .filter { image -> image.theme == preferredTheme }.ifEmpty { images }
            .minByOrNull { image -> screenDensity - image.dpiScale }
            ?.url
            ?: error("No URLs were provided for this CheerEmote")
    }
}
