package fr.outadoc.justchatting.component.chatapi.data.model

class RecentEmote(
    override val name: String,
    val url: String,
    val usedAt: Long
) : Emote() {

    override fun getUrl(animate: Boolean, screenDensity: Float, isDarkTheme: Boolean): String {
        return url
    }
}
