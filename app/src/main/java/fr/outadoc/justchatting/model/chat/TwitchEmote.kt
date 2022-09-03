package fr.outadoc.justchatting.model.chat

import com.google.gson.annotations.SerializedName
import okhttp3.HttpUrl.Companion.toHttpUrl

data class TwitchEmote(
    @SerializedName("_id")
    override val name: String,
    val id: String,
    var begin: Int = 0,
    var end: Int = 0,
    val setId: String? = null,
    override val ownerId: String? = null,
    private val supportedFormats: List<String> = listOf("default"),
    private val supportedScales: Map<Float, String> = mapOf(
        1f to "1.0",
        2f to "2.0",
        3f to "3.0"
    ),
    private val supportedThemes: List<String> = listOf("dark")
) : Emote() {

    companion object {
        private const val BASE_EMOTE_URL = "https://static-cdn.jtvnw.net/emoticons/v2"
    }

    override fun getUrl(animate: Boolean, screenDensity: Float, isDarkTheme: Boolean): String {
        val closestDensity: String = supportedScales
            .toList()
            .minByOrNull { density -> screenDensity - density.first }
            ?.second
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
        return BASE_EMOTE_URL.toHttpUrl()
            .newBuilder()
            .addPathSegment(id)
            .addPathSegment(format)
            .addPathSegment(theme)
            .addPathSegment(scale)
            .build()
            .toString()
    }
}
