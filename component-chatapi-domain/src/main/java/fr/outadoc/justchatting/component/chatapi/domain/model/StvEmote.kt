package fr.outadoc.justchatting.component.chatapi.domain.model

class StvEmote(
    override val name: String,
    override val isZeroWidth: Boolean,
    private val urls: Map<Float, String>,
) : Emote() {

    override fun getUrl(animate: Boolean, screenDensity: Float, isDarkTheme: Boolean): String {
        return (urls)
            .toList()
            .minByOrNull { url -> screenDensity - url.first }
            ?.second
            ?: error("No URLs were provided for this StvEmote")
    }
}
