package fr.outadoc.justchatting.component.twitch.model.chat

interface RemoteImage {
    fun getUrl(screenDensity: Float): String?
}
