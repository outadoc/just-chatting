package com.github.andreyasadchy.xtra.model.chat

class CheerEmote(
    override val name: String,
    val minBits: Int,
    val color: String? = null,
    private val staticUrls: Map<Float, String>,
    private val animatedUrls: Map<Float, String>
) : Emote() {

    override fun getUrl(animate: Boolean, screenDensity: Float, isDarkTheme: Boolean): String {
        return (animatedUrls.ifEmpty { staticUrls })
            .toList()
            .minByOrNull { url -> screenDensity - url.first }
            ?.second
            ?: error("No URLs were provided for this CheerEmote")
    }
}
