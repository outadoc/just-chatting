package fr.outadoc.justchatting.model.chat

class TwitchBadge(
    val id: String,
    val version: String,
    private val urls: Map<Float, String>,
    val title: String? = null
) {
    fun getUrl(screenDensity: Float): String {
        return urls
            .toList()
            .minByOrNull { url -> screenDensity - url.first }
            ?.second
            ?: error("No URLs were provided for this TwitchBadge")
    }
}
