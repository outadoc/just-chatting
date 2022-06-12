package com.github.andreyasadchy.xtra.model.chat

class FfzEmote(
    override val name: String,
    private val urls: Map<Float, String>
) : Emote() {

    override fun getUrl(animate: Boolean, screenDensity: Float, isDarkTheme: Boolean): String {
        return urls
            .toList()
            .minByOrNull { url -> screenDensity - url.first }
            ?.second
            ?: error("No URLs were provided for this FfzEmote")
    }
}
