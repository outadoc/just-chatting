package fr.outadoc.justchatting.component.chatapi.domain.model

data class TwitchEmote(
    val id: String,
    override val name: String,
    val setId: String? = null,
    override val ownerId: String? = null,
    private val supportedFormats: List<String>,
    private val supportedScales: List<String>,
    private val supportedThemes: List<String>,
    private val urlTemplate: String
) : Emote() {

    private val supportedScalesMap: List<Pair<String, Float>> =
        supportedScales.associateWith { it.toFloat() }.toList()

    override fun getUrl(animate: Boolean, screenDensity: Float, isDarkTheme: Boolean): String {
        val closestDensity: String =
            supportedScalesMap
                .minByOrNull { density -> screenDensity - density.second }
                ?.first
                ?: "1.0"

        val preferredFormat = if (animate) "default" else "static"
        val preferredTheme = if (isDarkTheme) "dark" else "light"

        return createUrlForEmote(
            id = id,
            format = supportedFormats.firstOrNull { it == preferredFormat }
                ?: "default",
            theme = supportedThemes.firstOrNull { it == preferredTheme }
                ?: supportedThemes.first(),
            scale = closestDensity
        )
    }

    private fun createUrlForEmote(
        id: String,
        format: String,
        theme: String,
        scale: String
    ): String {
        return urlTemplate
            .replace("{{id}}", id)
            .replace("{{format}}", format)
            .replace("{{theme_mode}}", theme)
            .replace("{{scale}}", scale)
    }
}
