package fr.outadoc.justchatting.component.chatapi.domain.model

class FfzEmote(
    override val name: String,
    urls: Map<String, String>
) : Emote() {

    private val urlsMap: List<Pair<Float, String>> =
        urls.map { (key, url) -> key.trimEnd('x').toFloat() to url }

    override fun getUrl(animate: Boolean, screenDensity: Float, isDarkTheme: Boolean): String {
        return urlsMap.minByOrNull { url -> screenDensity - url.first }
            ?.second ?: error("No URLs were provided for this FfzEmote")
    }
}
