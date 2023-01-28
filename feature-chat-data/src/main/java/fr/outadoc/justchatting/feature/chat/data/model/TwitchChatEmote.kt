package fr.outadoc.justchatting.feature.chat.data.model

import android.net.Uri

data class TwitchChatEmote(
    val id: String,
    val name: String,
) {

    companion object {
        private const val BASE_EMOTE_URL = "https://static-cdn.jtvnw.net/emoticons/v2"
        private val SUPPORTED_SCALES = listOf(1f, 2f, 3f).associateWith { it.toString() }.toList()
    }

    fun getUrl(animate: Boolean, screenDensity: Float, isDarkTheme: Boolean): String {
        val closestDensity: String = SUPPORTED_SCALES
            .minByOrNull { density -> screenDensity - density.first }
            ?.second
            ?: "1.0"

        val preferredFormat = if (animate) "default" else "static"
        val preferredTheme = if (isDarkTheme) "dark" else "light"

        return Uri.parse(BASE_EMOTE_URL)
            .buildUpon()
            .appendPath(id)
            .appendPath(preferredFormat)
            .appendPath(preferredTheme)
            .appendPath(closestDensity)
            .build()
            .toString()
    }
}
